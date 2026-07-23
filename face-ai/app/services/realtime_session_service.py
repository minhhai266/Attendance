import time
from collections import deque


SESSION_STALE_SECONDS = 2.0
SESSION_BUFFER_MAXLEN = 10
FRAME_DEBUG_MAXLEN = 10
SUCCESS_REQUIRED_FRAMES = 6
SUCCESS_REAL_VOTES = 5
DENIED_REQUIRED_FRAMES = 10
DENIED_FAKE_VOTES = 8
UNKNOWN_REQUIRED_FRAMES = 10
UNKNOWN_VOTES = 8

_presence_sessions = {}


def _face_area(item: dict) -> float:
    bbox = item.get("bbox") or []
    if len(bbox) < 4:
        return 0.0
    x1, y1, x2, y2 = [float(v) for v in bbox[:4]]
    return max(0.0, x2 - x1) * max(0.0, y2 - y1)


def _session_key(action: str, item: dict) -> str:
    if item.get("recognized") and item.get("student_id"):
        return f"student:{item['student_id']}:{action}"
    return f"unknown:{action}"


def _get_session(key: str) -> dict:
    now = time.monotonic()
    session = _presence_sessions.get(key)
    if session is None or now - session["last_seen"] > SESSION_STALE_SECONDS:
        session = {
            "votes": deque(maxlen=SESSION_BUFFER_MAXLEN),
            "frames": deque(maxlen=FRAME_DEBUG_MAXLEN),
            "last_seen": now,
            "finalized": None,
        }
        _presence_sessions[key] = session
    if "frames" not in session:
        session["frames"] = deque(maxlen=FRAME_DEBUG_MAXLEN)
    session["last_seen"] = now
    return session


def _cleanup_presence_sessions(seen_keys: set[str]) -> None:
    now = time.monotonic()
    stale_keys = [
        key
        for key, session in _presence_sessions.items()
        if key not in seen_keys and now - session["last_seen"] > SESSION_STALE_SECONDS
    ]
    for key in stale_keys:
        _presence_sessions.pop(key, None)


def _bbox_iou(a: list, b: list) -> float:
    if len(a) < 4 or len(b) < 4:
        return 0.0
    ax1, ay1, ax2, ay2 = [float(v) for v in a[:4]]
    bx1, by1, bx2, by2 = [float(v) for v in b[:4]]
    ix1 = max(ax1, bx1)
    iy1 = max(ay1, by1)
    ix2 = min(ax2, bx2)
    iy2 = min(ay2, by2)
    inter = max(0.0, ix2 - ix1) * max(0.0, iy2 - iy1)
    area_a = max(0.0, ax2 - ax1) * max(0.0, ay2 - ay1)
    area_b = max(0.0, bx2 - bx1) * max(0.0, by2 - by1)
    union = area_a + area_b - inter
    return inter / union if union > 0 else 0.0


def _bbox_center_distance_ratio(a: list, b: list) -> float:
    if len(a) < 4 or len(b) < 4:
        return 1.0
    ax1, ay1, ax2, ay2 = [float(v) for v in a[:4]]
    bx1, by1, bx2, by2 = [float(v) for v in b[:4]]
    acx = (ax1 + ax2) / 2.0
    acy = (ay1 + ay2) / 2.0
    bcx = (bx1 + bx2) / 2.0
    bcy = (by1 + by2) / 2.0
    scale = max(ax2 - ax1, ay2 - ay1, bx2 - bx1, by2 - by1, 1.0)
    return (((acx - bcx) ** 2 + (acy - bcy) ** 2) ** 0.5) / scale


def _matches_session_bbox(item: dict, session: dict) -> bool:
    item_bbox = item.get("bbox") or []
    session_bbox = session.get("bbox") or []
    return _bbox_iou(item_bbox, session_bbox) >= 0.15 or _bbox_center_distance_ratio(item_bbox, session_bbox) <= 0.35


def _matching_active_student_session(action: str, item: dict) -> tuple[str, dict] | None:
    now = time.monotonic()
    suffix = f":{action}"
    candidates = [
        (key, session)
        for key, session in _presence_sessions.items()
        if key.startswith("student:")
        and key.endswith(suffix)
        and now - session["last_seen"] <= SESSION_STALE_SECONDS
        and _matches_session_bbox(item, session)
    ]
    if not candidates:
        return None
    return max(candidates, key=lambda pair: pair[1]["last_seen"])


def _remember_student(session: dict, item: dict) -> None:
    session["student"] = {
        "student_id": item.get("student_id"),
        "student_code": item.get("student_code"),
        "full_name": item.get("full_name"),
    }
    session["bbox"] = item.get("bbox")


def _apply_display_student(item: dict, session: dict) -> None:
    student = session.get("student") or {}
    if student.get("student_id"):
        item["display_student_id"] = student.get("student_id")
        item["display_student_code"] = student.get("student_code")
        item["display_full_name"] = student.get("full_name")


def _vote_from_liveness(item: dict) -> str | None:
    status = item.get("liveness_status")
    if status == "live":
        return "real"
    if status == "fake":
        return "fake"
    return None


def _remember_frame(session: dict, item: dict, vote: str | None) -> None:
    if "frames" not in session:
        session["frames"] = deque(maxlen=FRAME_DEBUG_MAXLEN)
    liveness = item.get("liveness") or {}
    quality = item.get("quality") or {}
    session["frames"].append(
        {
            "liveness_status": item.get("liveness_status", "uncertain"),
            "quality_status": item.get("quality_status") or quality.get("reason"),
            "quality_ok": bool(quality.get("ok")),
            "vote": vote,
            "real_score": liveness.get("real_score"),
            "fake_score": liveness.get("fake_score"),
            "recognized": bool(item.get("recognized")),
            "confidence": item.get("confidence"),
            "message": item.get("note"),
        }
    )
    item["liveness_history"] = list(session["frames"])


def _has_spoof_majority(votes: list) -> bool:
    denied_votes = votes[-DENIED_REQUIRED_FRAMES:]
    return len(denied_votes) == DENIED_REQUIRED_FRAMES and denied_votes.count("fake") >= DENIED_FAKE_VOTES


def _liveness_history_note(item: dict) -> str:
    history = item.get("liveness_history") or []
    if not history:
        return ""
    parts = []
    for frame in history[-FRAME_DEBUG_MAXLEN:]:
        score = frame.get("real_score")
        score_text = "-" if score is None else str(score)
        quality = frame.get("quality_status") or "-"
        status = frame.get("liveness_status") or "uncertain"
        vote = frame.get("vote") or "-"
        parts.append(f"{status}/score={score_text}/quality={quality}/vote={vote}")
    return " | Frames: " + "; ".join(parts)


def _spoof_alert_message(item: dict) -> str:
    quality_status = item.get("quality_status")
    if quality_status and quality_status != "ok":
        return f"Nghi ngờ giả mạo khuôn mặt. Chất lượng ảnh chưa đạt: {quality_status}."
    score = (item.get("liveness") or {}).get("real_score")
    if score is not None:
        return f"Nghi ngờ giả mạo khuôn mặt. Điểm mặt thật: {score}."
    return "Nghi ngờ giả mạo khuôn mặt."


def _apply_denied_item(item: dict, note: str) -> None:
    item["decision"] = "denied"
    item["session_state"] = "denied"
    item["display_status"] = "denied"
    item["note"] = note
    item["spoof_detected"] = False


def _apply_recent_duplicate_item(item: dict, note: str) -> None:
    item["decision"] = "success"
    item["session_state"] = "success"
    item["display_status"] = "success"
    item["note"] = note
    item["spoof_detected"] = False
    item["logged"] = False


def _session_decision(action: str, item: dict) -> tuple[str | None, str, str]:
    key = _session_key(action, item)
    session = _get_session(key)
    if session["finalized"] and session["finalized"] != "success":
        _remember_frame(session, item, None)
        _apply_display_student(item, session)
        return None, key, session["finalized"]

    if item.get("recognized"):
        _remember_student(session, item)
        vote = _vote_from_liveness(item)
        _remember_frame(session, item, vote)
        if vote is None:
            _apply_display_student(item, session)
            return None, key, "pending"

        session["votes"].append(vote)
        votes = list(session["votes"])
        if _has_spoof_majority(votes):
            session["finalized"] = "denied"
            _apply_display_student(item, session)
            return "denied", key, "denied"
        if session["finalized"] == "success":
            _apply_display_student(item, session)
            return None, key, "success"
        success_votes = votes[-SUCCESS_REQUIRED_FRAMES:]
        if len(success_votes) == SUCCESS_REQUIRED_FRAMES and success_votes.count("real") >= SUCCESS_REAL_VOTES:
            session["finalized"] = "success"
            _apply_display_student(item, session)
            return "success", key, "success"
        _apply_display_student(item, session)
        return None, key, "pending"

    active_student = _matching_active_student_session(action, item)
    if active_student:
        active_key, active_session = active_student
        active_session["last_seen"] = time.monotonic()
        vote = _vote_from_liveness(item)
        _remember_frame(active_session, item, vote)
        if vote is None:
            _apply_display_student(item, active_session)
            return None, active_key, "pending"
        if vote == "fake":
            active_session["votes"].append(vote)
            votes = list(active_session["votes"])
            if _has_spoof_majority(votes):
                active_session["finalized"] = "denied"
                _apply_display_student(item, active_session)
                return "denied", active_key, "denied"
        _apply_display_student(item, active_session)
        return None, active_key, active_session["finalized"] or "pending"

    vote = _vote_from_liveness(item)
    _remember_frame(session, item, vote)
    if vote == "fake":
        session["votes"].append(vote)
        votes = list(session["votes"])
        if _has_spoof_majority(votes):
            session["finalized"] = "denied"
            return "denied", key, "denied"
        return None, key, "pending"

    if vote is None:
        return None, key, "pending"

    session["votes"].append("unknown")
    votes = list(session["votes"])
    unknown_votes = votes[-UNKNOWN_REQUIRED_FRAMES:]
    if len(unknown_votes) == UNKNOWN_REQUIRED_FRAMES and unknown_votes.count("unknown") >= UNKNOWN_VOTES:
        session["finalized"] = "warning"
        return "warning", key, "warning"
    return None, key, "pending"


face_area = _face_area
build_session_key = _session_key
cleanup_presence_sessions = _cleanup_presence_sessions
liveness_history_note = _liveness_history_note
spoof_alert_message = _spoof_alert_message
apply_denied_item = _apply_denied_item
apply_recent_duplicate_item = _apply_recent_duplicate_item
session_decision = _session_decision
