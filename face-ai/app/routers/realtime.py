import base64
import binascii
from io import BytesIO
import json
import logging
from urllib.parse import urlsplit
from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from PIL import Image, UnidentifiedImageError
from app.core.config import settings
from app.db import get_db, get_setting
from app.routers.deps import user_from_session_token
from app.services.face_cache_service import face_embedding_cache
from app.services.face_service import face_service
from app.services.realtime_session_service import (
    apply_denied_item,
    apply_recent_duplicate_item,
    cleanup_presence_sessions,
    face_area,
    liveness_history_note,
    build_session_key,
    session_decision,
    spoof_alert_message,
)
from app.services.attendance_service import (
    can_log,
    can_log_event,
)
from app.services.realtime_access_service import (
    create_alert,
    log_access,
    save_evidence_image,
    seconds_since_last_success,
    validate_attendance_transition,
)

router = APIRouter(tags=["realtime"])
logger = logging.getLogger(__name__)
WARNING_LOG_COOLDOWN_SECONDS = 10
INVALID_TRANSITION_GRACE_SECONDS = 10
WS_POLICY_VIOLATION = 1008
WS_MESSAGE_TOO_BIG = 1009
SAFE_FRAME_ERROR_MESSAGE = "Khong xu ly duoc khung hinh."
ALLOWED_REALTIME_IMAGE_FORMATS = {"JPEG", "PNG"}


class RealtimePayloadError(ValueError):
    def __init__(self, code: str, message: str = SAFE_FRAME_ERROR_MESSAGE, close_code: int | None = None):
        super().__init__(message)
        self.code = code
        self.close_code = close_code
        self.client_message = message


def _configured_websocket_origins() -> set[str]:
    return {
        origin.strip().rstrip("/")
        for origin in settings.websocket_allowed_origins.split(",")
        if origin.strip()
    }


def is_allowed_websocket_origin(websocket: WebSocket) -> bool:
    origin = (websocket.headers.get("origin") or "").strip().rstrip("/")
    if not origin:
        return False
    if origin in _configured_websocket_origins():
        return True

    host = (websocket.headers.get("host") or "").strip().lower()
    parsed = urlsplit(origin)
    return parsed.scheme in {"http", "https"} and parsed.netloc.lower() == host


def _utf8_size(text: str) -> int:
    return len(text.encode("utf-8"))


def parse_realtime_payload(payload: str) -> str | None:
    if _utf8_size(payload) > settings.websocket_max_message_bytes:
        raise RealtimePayloadError("message_too_large", "Khung hinh qua lon.", WS_MESSAGE_TOO_BIG)

    try:
        data = json.loads(payload)
    except json.JSONDecodeError as exc:
        raise RealtimePayloadError("invalid_payload") from exc

    if not isinstance(data, dict):
        raise RealtimePayloadError("invalid_payload")

    image_data = data.get("image")
    if image_data is None:
        return None
    if not isinstance(image_data, str):
        raise RealtimePayloadError("invalid_payload")
    return image_data


def decode_realtime_image(image_data: str):
    if "," in image_data:
        header, image_data = image_data.split(",", 1)
        header = header.lower()
        if header.startswith("data:") and not (header.startswith("data:image/jpeg") or header.startswith("data:image/png")):
            raise RealtimePayloadError("invalid_image")

    max_base64_len = ((settings.websocket_max_image_bytes + 2) // 3) * 4 + 4
    if len(image_data) > max_base64_len:
        raise RealtimePayloadError("image_too_large", "Khung hinh qua lon.", WS_MESSAGE_TOO_BIG)

    try:
        image_bytes = base64.b64decode(image_data, validate=True)
    except (binascii.Error, ValueError) as exc:
        raise RealtimePayloadError("invalid_image") from exc

    if not image_bytes:
        raise RealtimePayloadError("invalid_image")
    if len(image_bytes) > settings.websocket_max_image_bytes:
        raise RealtimePayloadError("image_too_large", "Khung hinh qua lon.", WS_MESSAGE_TOO_BIG)

    try:
        with Image.open(BytesIO(image_bytes)) as probe:
            if probe.format not in ALLOWED_REALTIME_IMAGE_FORMATS:
                raise RealtimePayloadError("invalid_image")
            width, height = probe.size
    except RealtimePayloadError:
        raise
    except (UnidentifiedImageError, OSError, ValueError) as exc:
        raise RealtimePayloadError("invalid_image") from exc

    if width <= 0 or height <= 0 or width * height > settings.websocket_max_image_pixels:
        raise RealtimePayloadError("image_too_large", "Khung hinh qua lon.", WS_MESSAGE_TOO_BIG)

    return face_service.read_image_from_bytes(image_bytes)


def load_known_faces():
    with get_db() as db:
        rows = db.execute(
            """
            SELECT f.embedding, s.id student_id, s.student_code, s.full_name, s.status
            FROM student_faces f
            JOIN students s ON s.id = f.student_id
            WHERE s.status='active'
            """
        ).fetchall()
    known = []
    for r in rows:
        known.append({
            "student_id": r["student_id"],
            "student_code": r["student_code"],
            "full_name": r["full_name"],
            "embedding": face_service.deserialize_embedding(r["embedding"]),
        })
    return known


def realtime_face_min_size() -> int:
    try:
        return int(get_setting("liveness_min_face_size", settings.liveness_min_face_size))
    except (TypeError, ValueError):
        return int(settings.liveness_min_face_size)


def is_actionable_realtime_face(item: dict, min_size: int | None = None) -> bool:
    bbox = item.get("bbox") or []
    if len(bbox) < 4:
        return False
    x1, y1, x2, y2 = [float(value) for value in bbox[:4]]
    face_w = max(0.0, x2 - x1)
    face_h = max(0.0, y2 - y1)
    required_size = realtime_face_min_size() if min_size is None else min_size
    return face_w >= required_size and face_h >= required_size


def apply_secondary_display_item(item: dict, note: str | None = None) -> None:
    item["decision"] = "secondary"
    item["session_state"] = "secondary"
    item["display_status"] = "secondary"
    item["display_full_name"] = item.get("full_name")
    item["display_student_code"] = item.get("student_code")
    item["logged"] = False
    if note:
        item["note"] = note


async def websocket_auth(websocket: WebSocket) -> bool:
    token = websocket.cookies.get("session_token")
    user = user_from_session_token(token)
    return bool(user and user["role"] in {"admin", "lab_manager"} and user["status"] == "active")


@router.websocket("/ws/recognize")
async def recognize_ws(websocket: WebSocket):
    if not is_allowed_websocket_origin(websocket):
        await websocket.close(code=WS_POLICY_VIOLATION)
        return

    await websocket.accept()
    if not await websocket_auth(websocket):
        await websocket.send_json({"type": "error", "message": "Chưa đăng nhập hoặc phiên hết hạn."})
        await websocket.close()
        return
    frame_count = 0
    try:
        while True:
            payload = await websocket.receive_text()
            if not await websocket_auth(websocket):
                await websocket.send_json({"type": "error", "message": "Phien dang nhap da het han hoac bi thu hoi."})
                await websocket.close()
                return
            frame_count += 1
            try:
                image_data = parse_realtime_payload(payload)
            except RealtimePayloadError as exc:
                await websocket.send_json({"type": "error", "code": exc.code, "message": exc.client_message})
                if exc.close_code:
                    await websocket.close(code=exc.close_code)
                    return
                continue
            if not image_data:
                continue
            frame_skip = int(get_setting("frame_skip", 5))
            if frame_skip > 1 and frame_count % frame_skip != 0:
                await websocket.send_json({"type": "skip"})
                continue
            threshold = float(get_setting("face_threshold", 0.55))
            cooldown = int(get_setting("check_cooldown_seconds", 30))
            requested_action = websocket.query_params.get("action")
            action = requested_action if requested_action in {"check_in", "check_out"} else get_setting("camera_mode", "check_in")
            try:
                image = decode_realtime_image(image_data)
            except RealtimePayloadError as exc:
                await websocket.send_json({"type": "error", "code": exc.code, "message": exc.client_message})
                if exc.close_code:
                    await websocket.close(code=exc.close_code)
                    return
                continue
            known = face_embedding_cache.get_known_faces(load_known_faces)
            results = face_service.recognize_faces(image, known, threshold)
            actionable_results = []
            for item in results:
                if is_actionable_realtime_face(item):
                    actionable_results.append(item)
                else:
                    apply_secondary_display_item(item, "Khuon mat qua nho, khong dung de diem danh.")
            evidence_image_path = None

            def get_evidence_image_path():
                nonlocal evidence_image_path
                if evidence_image_path is None:
                    evidence_image_path = save_evidence_image(image_data)
                return evidence_image_path

            seen_session_keys = set()
            primary_item = max(actionable_results, key=face_area, default=None)
            if len(actionable_results) > 1:
                for item in actionable_results:
                    if item is primary_item:
                        item["decision"] = "denied"
                        item["session_state"] = "denied"
                        item["display_status"] = "denied"
                        item["warning_type"] = "multiple_faces"
                        item["note"] = "Chi nen co 1 nguoi trong khung hinh de diem danh."
                        item["display_full_name"] = item.get("full_name")
                        item["display_student_code"] = item.get("student_code")
                    else:
                        apply_secondary_display_item(item)
                    if item is primary_item:
                        item["logged"] = False
                cleanup_presence_sessions(seen_session_keys)
                await websocket.send_json({"type": "result", "action": action, "items": results})
                continue

            for item in actionable_results:
                if item is not primary_item:
                    apply_secondary_display_item(item)
                    continue

                if item.get("recognized") and item.get("student_id"):
                    transition_ok, transition_note = validate_attendance_transition(item["student_id"], action)
                    if not transition_ok:
                        current_session_key = build_session_key(action, item)
                        seen_session_keys.add(current_session_key)
                        event_key = f"invalid_transition:{action}:{item['student_id']}"

                        last_success_age = seconds_since_last_success(item["student_id"], action)
                        if last_success_age is not None and last_success_age < INVALID_TRANSITION_GRACE_SECONDS:
                            apply_recent_duplicate_item(item, transition_note)
                            continue

                        if not can_log_event(event_key, WARNING_LOG_COOLDOWN_SECONDS):
                            apply_recent_duplicate_item(item, transition_note)
                            continue

                        apply_denied_item(item, transition_note)
                        evidence_path = get_evidence_image_path()
                        log_access(item, action, "denied", item.get("confidence"), transition_note, evidence_path)
                        item["logged"] = True
                        item["evidence_image_path"] = evidence_path
                        continue

                decision, current_session_key, display_status = session_decision(action, item)
                seen_session_keys.add(current_session_key)
                item["decision"] = decision or "pending"
                item["session_state"] = display_status
                item["display_status"] = display_status
                if decision is None:
                    item["logged"] = False
                    continue

                if decision == "denied":
                    note = item.get("note") or "Phát hiện nghi ngờ dùng ảnh/màn hình để giả mạo."
                    note = note + liveness_history_note(item)
                    alert_message = spoof_alert_message(item)
                    student_key = item.get("student_id") or "unknown"
                    event_key = f"denied:{action}:{student_key}"
                    if can_log_event(event_key, WARNING_LOG_COOLDOWN_SECONDS):
                        evidence_path = get_evidence_image_path()
                        log_access(item if item.get("student_id") else None, action, "denied", item.get("confidence"), note, evidence_path)
                        create_alert("spoof_detected", alert_message, evidence_path)
                        item["logged"] = True
                        item["evidence_image_path"] = evidence_path
                    else:
                        item["logged"] = False
                        item["note"] = "Cooldown: không ghi cảnh báo giả mạo quá gần."
                    continue

                if decision == "success":
                    if can_log(item["student_id"], action, cooldown):
                        evidence_path = get_evidence_image_path()
                        log_access(item, action, "success", item["confidence"], "Camera realtime", evidence_path)
                        item["logged"] = True
                        item["evidence_image_path"] = evidence_path
                    else:
                        item["logged"] = False
                        item["note"] = "Cooldown: không ghi log trùng quá gần."
                elif decision == "warning":
                    event_key = f"unknown:{action}"
                    if can_log_event(event_key, WARNING_LOG_COOLDOWN_SECONDS):
                        evidence_path = get_evidence_image_path()
                        log_access(None, action, "warning", item.get("confidence"), "Khuôn mặt lạ", evidence_path)
                        create_alert("unknown_face", "Phát hiện khuôn mặt lạ từ camera realtime.", evidence_path)
                        item["logged"] = True
                        item["evidence_image_path"] = evidence_path
                    else:
                        item["logged"] = False
                        item["note"] = "Cooldown: không ghi cảnh báo khuôn mặt lạ quá gần."
            cleanup_presence_sessions(seen_session_keys)
            await websocket.send_json({"type": "result", "action": action, "items": results})
    except WebSocketDisconnect:
        return
    except Exception:
        logger.exception("Unexpected realtime websocket error")
        await websocket.send_json({"type": "error", "code": "internal_error", "message": SAFE_FRAME_ERROR_MESSAGE})
