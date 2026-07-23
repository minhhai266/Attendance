import base64
import os
from datetime import datetime
from uuid import uuid4

from app.db import get_db
from app.services.attendance_service import (
    current_presence_state,
    mark_stale_checkin_missing_checkout,
    update_attendance_record,
)
from app.services.private_storage import PRIVATE_EVIDENCE_DIR, evidence_relative_path

EVIDENCE_DIR = str(PRIVATE_EVIDENCE_DIR)


def validate_attendance_transition(student_id: int, action: str) -> tuple[bool, str | None]:
    state = current_presence_state(student_id)
    if action == "check_in" and state == "inside":
        if mark_stale_checkin_missing_checkout(student_id):
            return True, None
        return False, "Sinh viên đã check-in, chưa check-out."
    if action == "check_out" and state == "outside":
        return False, "Sinh viên chưa check-in, không thể check-out."
    return True, None


def seconds_since_last_success(student_id: int, action: str) -> float | None:
    with get_db() as db:
        row = db.execute(
            """
            SELECT created_at FROM access_logs
            WHERE student_id=? AND action=? AND result='success'
            ORDER BY id DESC
            LIMIT 1
            """,
            (student_id, action),
        ).fetchone()
    if not row or not row["created_at"]:
        return None
    try:
        created_at = datetime.fromisoformat(row["created_at"])
    except ValueError:
        return None
    return max(0.0, (datetime.now() - created_at).total_seconds())


def save_evidence_image(image_data: str | None) -> str | None:
    if not image_data:
        return None

    try:
        payload = image_data.split(",", 1)[1] if "," in image_data else image_data
        image_bytes = base64.b64decode(payload)
    except Exception:
        return None

    now = datetime.now()
    day = now.strftime("%Y%m%d")
    folder = os.path.join(EVIDENCE_DIR, day)
    os.makedirs(folder, exist_ok=True)
    filename = f"{now.strftime('%H%M%S')}_{uuid4().hex[:10]}.jpg"
    file_path = os.path.join(folder, filename)
    with open(file_path, "wb") as file_obj:
        file_obj.write(image_bytes)
    return evidence_relative_path(day, filename)


def log_access(student, action: str, result: str, confidence=None, note=None, evidence_image_path=None):
    created_at = datetime.now()
    with get_db() as db:
        db.execute(
            """
            INSERT INTO access_logs(student_id, student_code, full_name, action, result, confidence, note, evidence_image_path, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                student.get("student_id") if student else None,
                student.get("student_code") if student else "Unknown",
                student.get("full_name") if student else "Unknown",
                action,
                result,
                confidence,
                note,
                evidence_image_path,
                created_at.isoformat(timespec="seconds"),
            ),
        )
    if result == "success":
        update_attendance_record(student, action, created_at)


def create_alert(alert_type: str, message: str, evidence_image_path=None, event_date: str | None = None):
    created_at = datetime.now()
    with get_db() as db:
        db.execute(
            "INSERT INTO alerts(type, message, status, evidence_image_path, event_date, created_at) VALUES (?, ?, 'new', ?, ?, ?)",
            (alert_type, message, evidence_image_path, event_date or created_at.date().isoformat(), created_at.isoformat(timespec="seconds")),
        )
