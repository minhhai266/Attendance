import base64
import os
from datetime import datetime, time, timedelta
from uuid import uuid4
from app.db import get_db, get_setting
from app.services.private_storage import PRIVATE_EVIDENCE_DIR, evidence_relative_path

_last_success = {}
_last_events = {}
EVIDENCE_DIR = str(PRIVATE_EVIDENCE_DIR)
MISSING_CHECKOUT_ALERT_TYPE = "missing_checkout"
DEFAULT_MISSING_CHECKOUT_CUTOFF_TIME = "23:59"
DEFAULT_WORK_START_TIME = "08:00"
DEFAULT_WORK_END_TIME = "17:00"
DEFAULT_LATE_GRACE_MINUTES = 5
DEFAULT_EARLY_LEAVE_GRACE_MINUTES = 10


def can_log_event(key: str, cooldown_seconds: int) -> bool:
    now = datetime.now()
    last = _last_events.get(key)
    if last and now - last < timedelta(seconds=cooldown_seconds):
        return False
    _last_events[key] = now
    return True


def can_log(student_id: int, action: str, cooldown_seconds: int) -> bool:
    key = f"{student_id}:{action}"
    now = datetime.now()
    last = _last_success.get(key)
    if last and now - last < timedelta(seconds=cooldown_seconds):
        return False
    _last_success[key] = now
    return True


def current_presence_state(student_id: int) -> str:
    with get_db() as db:
        row = db.execute(
            """
            SELECT action FROM access_logs
            WHERE student_id=? AND result='success' AND action IN ('check_in', 'check_out')
            ORDER BY id DESC
            LIMIT 1
            """,
            (student_id,),
        ).fetchone()
    if row and row["action"] == "check_in":
        return "inside"
    return "outside"


def _parse_cutoff_time(value: str | None) -> time:
    raw = (value or DEFAULT_MISSING_CHECKOUT_CUTOFF_TIME).strip()
    try:
        hour_text, minute_text = raw.split(":", 1)
        return time(hour=int(hour_text), minute=int(minute_text))
    except (TypeError, ValueError):
        return time(hour=23, minute=59)


def _parse_log_time(value: str | None) -> datetime | None:
    if not value:
        return None
    try:
        return datetime.fromisoformat(value)
    except ValueError:
        return None


def _setting_int(key: str, default: int) -> int:
    try:
        return int(get_setting(key, default))
    except (TypeError, ValueError):
        return default


def _setting_time(key: str, default: str) -> time:
    return _parse_cutoff_time(get_setting(key, default))


def _parse_time_value(value: str | None, default: str) -> time:
    return _parse_cutoff_time(value or default)


def _ceil_minutes(delta: timedelta) -> int:
    seconds = max(0, int(delta.total_seconds()))
    return (seconds + 59) // 60


def _date_text(value: datetime) -> str:
    return value.date().isoformat()


def _combine_date_time(date_text: str, value: time) -> datetime:
    return datetime.combine(datetime.fromisoformat(date_text).date(), value)


def _workday_has_ended(date_text: str, work_end: time | None = None, now: datetime | None = None) -> bool:
    current = now or datetime.now()
    end_time = work_end or _setting_time("work_end_time", DEFAULT_WORK_END_TIME)
    return current >= _combine_date_time(date_text, end_time)


def _student_attendance_config(db, student_id: int) -> dict:
    row = db.execute(
        "SELECT work_start_time, work_end_time FROM student_attendance_settings WHERE student_id=?",
        (student_id,),
    ).fetchone()
    return {
        "work_start_time": _parse_time_value(row["work_start_time"] if row else None, get_setting("work_start_time", DEFAULT_WORK_START_TIME)),
        "work_end_time": _parse_time_value(row["work_end_time"] if row else None, get_setting("work_end_time", DEFAULT_WORK_END_TIME)),
        "late_grace_minutes": _setting_int("late_grace_minutes", DEFAULT_LATE_GRACE_MINUTES),
        "early_leave_grace_minutes": _setting_int("early_leave_grace_minutes", DEFAULT_EARLY_LEAVE_GRACE_MINUTES),
    }


def _attendance_day_logs(db, student_id: int, attendance_date: str):
    return db.execute(
        """
        SELECT action, created_at, note FROM access_logs
        WHERE student_id=?
            AND result='success'
            AND action IN ('check_in', 'check_out')
            AND date(created_at)=date(?)
        ORDER BY created_at ASC, id ASC
        """,
        (student_id, attendance_date),
    ).fetchall()


def _time_text(value: datetime | None) -> str:
    return value.strftime("%H:%M") if value else "--:--"


def _duration_text(minutes: int) -> str:
    if minutes <= 0:
        return "0p"
    hours = minutes // 60
    rest = minutes % 60
    if not hours:
        return f"{rest}p"
    return f"{hours}h {rest}p" if rest else f"{hours}h"


def _iso_text(value: datetime | None) -> str | None:
    return value.isoformat(timespec="seconds") if value else None


def _attendance_day_summary(db, student_id: int, attendance_date: str) -> dict:
    rows = _attendance_day_logs(db, student_id, attendance_date)
    first_check_in = None
    last_check_out = None
    open_check_in = None
    pending_out_at = None
    last_action = None
    last_log_at = None
    total_minutes = 0
    sessions = []
    outside_periods = []
    logs = []

    for row in rows:
        created_at = _parse_log_time(row["created_at"])
        if not created_at:
            continue
        action = row["action"]
        logs.append({
            "action": action,
            "created_at": created_at.isoformat(timespec="seconds"),
            "note": row["note"] if "note" in row.keys() else None,
        })
        last_action = action
        last_log_at = created_at
        if action == "check_in":
            if first_check_in is None or created_at < first_check_in:
                first_check_in = created_at
            if pending_out_at and created_at >= pending_out_at:
                outside_minutes = _ceil_minutes(created_at - pending_out_at)
                outside_periods.append({
                    "start_at": pending_out_at.isoformat(timespec="seconds"),
                    "end_at": created_at.isoformat(timespec="seconds"),
                    "minutes": max(0, outside_minutes),
                })
                pending_out_at = None
            open_check_in = created_at
            continue
        if action == "check_out":
            if last_check_out is None or created_at > last_check_out:
                last_check_out = created_at
            if open_check_in and created_at >= open_check_in:
                session_minutes = int((created_at - open_check_in).total_seconds() // 60)
                total_minutes += session_minutes
                sessions.append({
                    "start_at": open_check_in.isoformat(timespec="seconds"),
                    "end_at": created_at.isoformat(timespec="seconds"),
                    "minutes": max(0, session_minutes),
                })
                open_check_in = None
            pending_out_at = created_at

    current_out_since_at = pending_out_at if last_action == "check_out" else None
    outside_minutes = sum(item["minutes"] for item in outside_periods)

    return {
        "first_check_in_at": _iso_text(first_check_in),
        "last_check_out_at": _iso_text(last_check_out),
        "last_action": last_action,
        "last_log_at": _iso_text(last_log_at),
        "open_check_in_at": _iso_text(open_check_in),
        "current_out_since_at": _iso_text(current_out_since_at),
        "presence_status": "in_lab" if last_action == "check_in" else ("out_of_lab" if last_action == "check_out" else None),
        "total_minutes": max(0, total_minutes),
        "outside_count": len(outside_periods),
        "outside_minutes": max(0, outside_minutes),
        "sessions": sessions,
        "outside_periods": outside_periods,
        "logs": logs,
    }


def _attendance_status(
    attendance_date: str,
    first_check_in_at: str | None,
    last_check_out_at: str | None,
    missing_checkout: bool,
    config: dict | None = None,
    now: datetime | None = None,
    last_action: str | None = None,
) -> tuple[str, int, int, str | None]:
    first_check_in = _parse_log_time(first_check_in_at)
    last_check_out = _parse_log_time(last_check_out_at)
    active_config = config or {
        "work_start_time": _setting_time("work_start_time", DEFAULT_WORK_START_TIME),
        "work_end_time": _setting_time("work_end_time", DEFAULT_WORK_END_TIME),
        "late_grace_minutes": _setting_int("late_grace_minutes", DEFAULT_LATE_GRACE_MINUTES),
        "early_leave_grace_minutes": _setting_int("early_leave_grace_minutes", DEFAULT_EARLY_LEAVE_GRACE_MINUTES),
    }
    start_time = active_config["work_start_time"]
    end_time = active_config["work_end_time"]
    late_grace = active_config["late_grace_minutes"]
    early_grace = active_config["early_leave_grace_minutes"]

    if not first_check_in:
        if _workday_has_ended(attendance_date, end_time, now):
            return "absent", 0, 0, "Không có check-in trong ngày."
        return "pending", 0, 0, "Chưa có check-in."

    late_after = _combine_date_time(attendance_date, start_time) + timedelta(minutes=late_grace)
    late_minutes = _ceil_minutes(first_check_in - late_after) if first_check_in > late_after else 0

    early_leave_minutes = 0
    workday_ended = _workday_has_ended(attendance_date, end_time, now)
    if last_check_out and last_action == "check_out" and workday_ended:
        early_before = _combine_date_time(attendance_date, end_time) - timedelta(minutes=early_grace)
        if last_check_out < early_before:
            early_leave_minutes = _ceil_minutes(early_before - last_check_out)

    if missing_checkout:
        return "missing_checkout", late_minutes, early_leave_minutes, "Đã check-in nhưng chưa check-out."
    if not workday_ended:
        return "unfinalized", late_minutes, 0, None
    if last_action == "check_in":
        return "unfinalized", late_minutes, 0, None
    if late_minutes and early_leave_minutes:
        return "late_and_early_leave", late_minutes, early_leave_minutes, None
    if late_minutes:
        return "late", late_minutes, early_leave_minutes, None
    if early_leave_minutes:
        return "early_leave", late_minutes, early_leave_minutes, None
    return "present_on_time", late_minutes, early_leave_minutes, None


def _attendance_note(status: str, summary: dict, fallback_note: str | None = None) -> str | None:
    if fallback_note:
        return fallback_note
    if status == "absent":
        return "Không có check-in trong ngày."
    if status == "pending":
        return "Chưa có check-in."
    if status == "missing_checkout":
        return "Đã check-in nhưng chưa check-out."

    current_out_since = _parse_log_time(summary.get("current_out_since_at"))
    outside_count = int(summary.get("outside_count") or 0)
    outside_minutes = int(summary.get("outside_minutes") or 0)

    if status in {"early_leave", "late_and_early_leave"} and current_out_since:
        note = f"Ra cuối lúc {_time_text(current_out_since)}, không quay lại."
        if outside_count:
            note += f" Đã ra ngoài {outside_count} lần, tổng {_duration_text(outside_minutes)}."
        return note
    if current_out_since:
        return f"Đã ra ngoài từ {_time_text(current_out_since)}."
    if outside_count:
        return f"Đã ra ngoài {outside_count} lần, tổng {_duration_text(outside_minutes)}."
    if status == "unfinalized":
        return "Đã check-in, chưa chốt ca."
    return None


def _resolution_note(resolution_type: str | None, reason: str | None = None, checkout_at: str | None = None) -> str | None:
    clean_reason = (reason or "").strip()
    suffix = f" Lý do: {clean_reason}" if clean_reason else ""
    checkout_time = _time_text(_parse_log_time(checkout_at))
    if resolution_type == "auto_work_end":
        return f"Tự chốt check-out theo giờ kết thúc ca lúc {checkout_time} do thiếu check-out."
    if resolution_type == "keep_zero":
        return f"Giữ thiếu check-out, tính 0h.{suffix}"
    if resolution_type == "work_end":
        return f"Admin chốt check-out theo giờ kết thúc ca lúc {checkout_time}.{suffix}"
    if resolution_type == "manual_time":
        return f"Admin nhập giờ ra {checkout_time} do thiếu check-out.{suffix}"
    return None


def _upsert_attendance_record(db, student, attendance_date: str, missing_checkout: bool | None = None) -> None:
    student_id = student["student_id"] if "student_id" in student.keys() else student["id"]
    existing = db.execute(
        "SELECT * FROM attendance_records WHERE student_id=? AND attendance_date=?",
        (student_id, attendance_date),
    ).fetchone()
    summary = _attendance_day_summary(db, student_id, attendance_date)
    first_check_in_at = summary["first_check_in_at"]
    last_check_out_at = summary["last_check_out_at"]
    total_minutes = summary["total_minutes"]
    current_missing = bool(existing["missing_checkout"]) if existing else False
    next_missing = bool(missing_checkout) if missing_checkout is not None else current_missing
    if last_check_out_at and missing_checkout is None:
        next_missing = False
    status, late_minutes, early_leave_minutes, note = _attendance_status(
        attendance_date,
        first_check_in_at,
        last_check_out_at,
        next_missing,
        _student_attendance_config(db, student_id),
        last_action=summary["last_action"],
    )
    note = _attendance_note(status, summary, note)
    resolution_type = existing["missing_checkout_resolution"] if existing and "missing_checkout_resolution" in existing.keys() else None
    resolution_reason = existing["resolution_reason"] if existing and "resolution_reason" in existing.keys() else None
    resolution_checkout_at = existing["resolution_checkout_at"] if existing and "resolution_checkout_at" in existing.keys() else None
    force_zero = bool(existing["force_zero_minutes"]) if existing and "force_zero_minutes" in existing.keys() else False
    if resolution_type == "keep_zero" and force_zero:
        status = "missing_checkout"
        total_minutes = 0
        next_missing = True
        note = _resolution_note(resolution_type, resolution_reason)
    elif resolution_type in {"work_end", "manual_time", "auto_work_end"}:
        note = _resolution_note(resolution_type, resolution_reason, resolution_checkout_at)
    now_text = datetime.now().isoformat(timespec="seconds")
    student_code = student["student_code"]
    full_name = student["full_name"]

    if existing:
        db.execute(
            """
            UPDATE attendance_records
            SET student_code=?, full_name=?, first_check_in_at=?, last_check_out_at=?,
                status=?, late_minutes=?, early_leave_minutes=?, total_minutes=?,
                missing_checkout=?, note=?, updated_at=?
            WHERE id=?
            """,
            (
                student_code,
                full_name,
                first_check_in_at,
                last_check_out_at,
                status,
                late_minutes,
                early_leave_minutes,
                total_minutes,
                1 if next_missing else 0,
                note,
                now_text,
                existing["id"],
            ),
        )
        return

    db.execute(
        """
        INSERT INTO attendance_records(
            student_id, student_code, full_name, attendance_date,
            first_check_in_at, last_check_out_at, status, late_minutes,
            early_leave_minutes, total_minutes, missing_checkout, note, created_at, updated_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            student_id,
            student_code,
            full_name,
            attendance_date,
            first_check_in_at,
            last_check_out_at,
            status,
            late_minutes,
            early_leave_minutes,
            total_minutes,
            1 if next_missing else 0,
            note,
            now_text,
            now_text,
        ),
    )


def attendance_record_context(db, student_id: int, attendance_date: str) -> dict:
    return _attendance_day_summary(db, student_id, attendance_date)


def attendance_record_detail(record_id: int) -> dict | None:
    with get_db() as db:
        record = db.execute("SELECT * FROM attendance_records WHERE id=?", (record_id,)).fetchone()
        if not record:
            return None
        summary = _attendance_day_summary(db, record["student_id"], record["attendance_date"])
        return {
            "record": {
                "id": record["id"],
                "student_id": record["student_id"],
                "student_code": record["student_code"],
                "full_name": record["full_name"],
                "attendance_date": record["attendance_date"],
                "status": record["status"],
                "late_minutes": record["late_minutes"],
                "early_leave_minutes": record["early_leave_minutes"],
                "total_minutes": record["total_minutes"],
                "note": record["note"],
                "missing_checkout_resolution": record["missing_checkout_resolution"] if "missing_checkout_resolution" in record.keys() else None,
                "resolution_reason": record["resolution_reason"] if "resolution_reason" in record.keys() else None,
                "resolution_checkout_at": record["resolution_checkout_at"] if "resolution_checkout_at" in record.keys() else None,
                "force_zero_minutes": record["force_zero_minutes"] if "force_zero_minutes" in record.keys() else 0,
            },
            "summary": summary,
        }


def _parse_manual_checkout_time(attendance_date: str, checkout_time: str) -> datetime:
    try:
        hour_text, minute_text = (checkout_time or "").split(":", 1)
        checkout = time(hour=int(hour_text), minute=int(minute_text))
    except (TypeError, ValueError):
        raise ValueError("Giờ ra không hợp lệ.")
    return _combine_date_time(attendance_date, checkout)


def _missing_checkout_resolution_type(record) -> str | None:
    return record["missing_checkout_resolution"] if "missing_checkout_resolution" in record.keys() else None


def _delete_resolution_checkout_log(db, record) -> None:
    resolution_type = _missing_checkout_resolution_type(record)
    if resolution_type not in {"auto_work_end", "work_end", "manual_time"}:
        return
    checkout_at = record["resolution_checkout_at"] if "resolution_checkout_at" in record.keys() else None
    if not checkout_at:
        return
    db.execute(
        """
        DELETE FROM access_logs
        WHERE student_id=? AND action='check_out' AND result='success' AND created_at=?
        """,
        (record["student_id"], checkout_at),
    )


def _clear_missing_checkout_resolution(db, record_id: int) -> None:
    db.execute(
        """
        UPDATE attendance_records
        SET missing_checkout_resolution=NULL,
            resolution_reason=NULL,
            resolution_checkout_at=NULL,
            force_zero_minutes=0
        WHERE id=?
        """,
        (record_id,),
    )


def resolve_missing_checkout_record(
    record_id: int,
    resolution_type: str,
    reason: str,
    checkout_time: str | None = None,
) -> dict | None:
    clean_reason = (reason or "").strip()
    if not clean_reason:
        raise ValueError("Vui lòng nhập lý do xử lý thiếu check-out.")
    if resolution_type not in {"work_end", "manual_time", "keep_zero"}:
        raise ValueError("Cách xử lý thiếu check-out không hợp lệ.")

    now_text = datetime.now().isoformat(timespec="seconds")
    with get_db() as db:
        record = db.execute("SELECT * FROM attendance_records WHERE id=?", (record_id,)).fetchone()
        if not record:
            return None
        current_resolution = _missing_checkout_resolution_type(record)
        if record["status"] != "missing_checkout" and current_resolution not in {"auto_work_end", "work_end", "manual_time", "keep_zero"}:
            raise ValueError("Chỉ xử lý được bản ghi thiếu check-out hoặc đã chốt do thiếu check-out.")
        student = db.execute(
            "SELECT id, student_code, full_name FROM students WHERE id=?",
            (record["student_id"],),
        ).fetchone()
        if not student:
            return None

        _delete_resolution_checkout_log(db, record)
        _clear_missing_checkout_resolution(db, record_id)
        checkout_at = None
        if resolution_type == "keep_zero":
            _upsert_attendance_record(db, student, record["attendance_date"], missing_checkout=True)
            db.execute(
                """
                UPDATE attendance_records
                SET status='missing_checkout', total_minutes=0, missing_checkout=1, note=?,
                    missing_checkout_resolution=?, resolution_reason=?,
                    resolution_checkout_at=NULL, force_zero_minutes=1, updated_at=?
                WHERE id=?
                """,
                (
                    _resolution_note("keep_zero", clean_reason),
                    resolution_type,
                    clean_reason,
                    now_text,
                    record_id,
                ),
            )
        else:
            if resolution_type == "work_end":
                work_end = _student_attendance_config(db, record["student_id"])["work_end_time"]
                checkout_at = _combine_date_time(record["attendance_date"], work_end)
            else:
                if not checkout_time:
                    raise ValueError("Vui lòng nhập giờ ra.")
                checkout_at = _parse_manual_checkout_time(record["attendance_date"], checkout_time)

            summary = _attendance_day_summary(db, record["student_id"], record["attendance_date"])
            open_check_in = _parse_log_time(summary.get("open_check_in_at"))
            if open_check_in and checkout_at < open_check_in:
                raise ValueError("Giờ ra phải sau lần check-in cuối cùng.")

            note = _resolution_note(resolution_type, clean_reason, checkout_at.isoformat(timespec="seconds"))
            db.execute(
                """
                INSERT INTO access_logs(student_id, student_code, full_name, action, result, confidence, note, evidence_image_path, created_at)
                VALUES (?, ?, ?, 'check_out', 'success', NULL, ?, NULL, ?)
                """,
                (
                    student["id"],
                    student["student_code"],
                    student["full_name"],
                    note,
                    checkout_at.isoformat(timespec="seconds"),
                ),
            )
            db.execute(
                """
                UPDATE attendance_records
                SET missing_checkout_resolution=?, resolution_reason=?,
                    resolution_checkout_at=?, force_zero_minutes=0, updated_at=?
                WHERE id=?
                """,
                (
                    resolution_type,
                    clean_reason,
                    checkout_at.isoformat(timespec="seconds"),
                    now_text,
                    record_id,
                ),
            )
            _upsert_attendance_record(db, student, record["attendance_date"])

        updated = db.execute("SELECT * FROM attendance_records WHERE id=?", (record_id,)).fetchone()
        return dict(updated) if updated else None


def _auto_close_open_checkin(db, checkin_log, attendance_date: str) -> bool:
    student_id = checkin_log["student_id"]
    if student_id is None:
        return False

    existing = db.execute(
        "SELECT * FROM attendance_records WHERE student_id=? AND attendance_date=?",
        (student_id, attendance_date),
    ).fetchone()
    if existing:
        resolution = existing["missing_checkout_resolution"] if "missing_checkout_resolution" in existing.keys() else None
        force_zero = bool(existing["force_zero_minutes"]) if "force_zero_minutes" in existing.keys() else False
        if resolution in {"keep_zero", "work_end", "manual_time", "auto_work_end"} or force_zero:
            return False

    summary = _attendance_day_summary(db, student_id, attendance_date)
    if summary.get("last_action") != "check_in":
        return False
    open_check_in = _parse_log_time(summary.get("open_check_in_at"))
    if not open_check_in:
        return False

    config = _student_attendance_config(db, student_id)
    checkout_at = _combine_date_time(attendance_date, config["work_end_time"])
    if checkout_at < open_check_in:
        return False

    note = _resolution_note(
        "auto_work_end",
        "Hệ thống tự chốt do quá giờ chốt thiếu check-out.",
        checkout_at.isoformat(timespec="seconds"),
    )
    db.execute(
        """
        INSERT INTO access_logs(student_id, student_code, full_name, action, result, confidence, note, evidence_image_path, created_at)
        VALUES (?, ?, ?, 'check_out', 'success', NULL, ?, NULL, ?)
        """,
        (
            student_id,
            checkin_log["student_code"],
            checkin_log["full_name"],
            note,
            checkout_at.isoformat(timespec="seconds"),
        ),
    )
    _upsert_attendance_record(db, checkin_log, attendance_date)
    now_text = datetime.now().isoformat(timespec="seconds")
    db.execute(
        """
        UPDATE attendance_records
        SET missing_checkout_resolution='auto_work_end',
            resolution_reason=?,
            resolution_checkout_at=?,
            force_zero_minutes=0,
            missing_checkout=0,
            note=?,
            updated_at=?
        WHERE student_id=? AND attendance_date=?
        """,
        (
            "Hệ thống tự chốt do quá giờ chốt thiếu check-out.",
            checkout_at.isoformat(timespec="seconds"),
            note,
            now_text,
            student_id,
            attendance_date,
        ),
    )
    return True


def update_attendance_record(student, action: str, event_time: datetime | None = None) -> None:
    if not student or action not in {"check_in", "check_out"}:
        return
    student_id = student.get("student_id") or student.get("id")
    if not student_id:
        return
    event_at = event_time or datetime.now()
    attendance_date = _date_text(event_at)
    with get_db() as db:
        row = db.execute(
            "SELECT id, student_code, full_name FROM students WHERE id=?",
            (student_id,),
        ).fetchone()
        if row:
            _upsert_attendance_record(db, row, attendance_date)


def ensure_attendance_records(attendance_date: str | None = None) -> int:
    target_date = attendance_date or _date_text(datetime.now())
    with get_db() as db:
        students = db.execute("SELECT id, student_code, full_name FROM students WHERE status='active'").fetchall()
        for student in students:
            _upsert_attendance_record(db, student, target_date)
    return len(students)


def recalculate_attendance_records(date_from: str | None = None, date_to: str | None = None) -> int:
    mark_missing_checkouts()
    start = datetime.fromisoformat(date_from).date() if date_from else datetime.now().date()
    end = datetime.fromisoformat(date_to).date() if date_to else start
    if end < start:
        start, end = end, start
    created_or_updated = 0
    current = start
    while current <= end:
        created_or_updated += ensure_attendance_records(current.isoformat())
        current += timedelta(days=1)
    return created_or_updated


def recalculate_student_attendance_records(student_id: int) -> int:
    mark_missing_checkouts()
    with get_db() as db:
        student = db.execute(
            "SELECT id, student_code, full_name FROM students WHERE id=?",
            (student_id,),
        ).fetchone()
        if not student:
            return 0
        rows = db.execute(
            """
            SELECT DISTINCT date_text FROM (
                SELECT date(created_at) date_text FROM access_logs WHERE student_id=?
                UNION
                SELECT attendance_date date_text FROM attendance_records WHERE student_id=?
                UNION
                SELECT date('now','localtime') date_text
            )
            WHERE date_text IS NOT NULL
            ORDER BY date_text
            """,
            (student_id, student_id),
        ).fetchall()
        for row in rows:
            _upsert_attendance_record(db, student, row["date_text"])
    return len(rows)


def recalculate_student_attendance_record(student_id: int, attendance_date: str) -> bool:
    mark_missing_checkouts()
    with get_db() as db:
        student = db.execute(
            "SELECT id, student_code, full_name FROM students WHERE id=?",
            (student_id,),
        ).fetchone()
        if not student:
            return False
        _upsert_attendance_record(db, student, attendance_date)
    return True


def _missing_checkout_message(checkin_log) -> str:
    created_at = _parse_log_time(checkin_log["created_at"])
    if created_at:
        day_text = created_at.strftime("%d/%m/%Y")
        time_text = created_at.strftime("%H:%M")
    else:
        day_text = checkin_log["created_at"] or "không rõ ngày"
        time_text = "--:--"
    student_code = checkin_log["student_code"] or "Unknown"
    return f"Sinh viên {student_code} đã check-in ngày {day_text} lúc {time_text} nhưng chưa check-out."


def _missing_checkout_dedupe_tokens(checkin_log) -> tuple[str, str]:
    created_at = _parse_log_time(checkin_log["created_at"])
    day_text = created_at.strftime("%d/%m/%Y") if created_at else (checkin_log["created_at"] or "không rõ ngày")
    student_code = checkin_log["student_code"] or "Unknown"
    return student_code, day_text


def _missing_checkout_event_date(checkin_log) -> str:
    created_at = _parse_log_time(checkin_log["created_at"])
    return _date_text(created_at) if created_at else datetime.now().date().isoformat()


def _missing_checkout_alert_at(checkin_log, cutoff_value: str | None = None) -> datetime:
    created_at = _parse_log_time(checkin_log["created_at"])
    if not created_at:
        return datetime.now()
    cutoff_text = cutoff_value or get_setting("missing_checkout_cutoff_time", DEFAULT_MISSING_CHECKOUT_CUTOFF_TIME)
    alert_at = datetime.combine(created_at.date(), _parse_cutoff_time(cutoff_text))
    if created_at > alert_at:
        alert_at += timedelta(days=1)
    return alert_at


def _create_missing_checkout_alert(db, checkin_log, alert_at: datetime | None = None) -> bool:
    message = _missing_checkout_message(checkin_log)
    student_code, day_text = _missing_checkout_dedupe_tokens(checkin_log)
    event_date = _missing_checkout_event_date(checkin_log)
    alert_time = alert_at or _missing_checkout_alert_at(checkin_log)
    existing = db.execute(
        "SELECT id FROM alerts WHERE type=? AND message LIKE ? AND message LIKE ? LIMIT 1",
        (MISSING_CHECKOUT_ALERT_TYPE, f"%{student_code}%", f"%{day_text}%"),
    ).fetchone()
    if existing:
        return False
    db.execute(
        "INSERT INTO alerts(type, message, status, evidence_image_path, event_date, created_at) VALUES (?, ?, 'new', NULL, ?, ?)",
        (MISSING_CHECKOUT_ALERT_TYPE, message, event_date, alert_time.isoformat(timespec="seconds")),
    )
    return True


def _finalize_missing_checkout(db, checkin_log, attendance_date: str, alert_at: datetime | None = None) -> bool:
    if not _auto_close_open_checkin(db, checkin_log, attendance_date):
        _upsert_attendance_record(db, checkin_log, attendance_date, missing_checkout=True)
    return _create_missing_checkout_alert(db, checkin_log, alert_at)


def mark_missing_checkouts(now: datetime | None = None, cutoff_value: str | None = None) -> int:
    current = now or datetime.now()
    cutoff_text = cutoff_value or get_setting("missing_checkout_cutoff_time", DEFAULT_MISSING_CHECKOUT_CUTOFF_TIME)
    with get_db() as db:
        rows = db.execute(
            """
            SELECT * FROM access_logs
            WHERE student_id IS NOT NULL
                AND result = 'success'
                AND action IN ('check_in', 'check_out')
                AND created_at <= ?
            ORDER BY created_at ASC, id ASC
            """,
            (current.isoformat(timespec="seconds"),),
        ).fetchall()

        open_checkins = {}
        created = 0
        for row in rows:
            student_id = row["student_id"]
            if row["action"] == "check_out":
                open_checkins.pop(student_id, None)
                continue

            previous = open_checkins.get(student_id)
            previous_at = _parse_log_time(previous["created_at"]) if previous else None
            current_at = _parse_log_time(row["created_at"])
            if previous and previous_at and current_at and previous_at.date() != current_at.date():
                alert_at = _missing_checkout_alert_at(previous, cutoff_text)
                if alert_at <= current:
                    previous_date = _date_text(previous_at)
                    if _finalize_missing_checkout(db, previous, previous_date, alert_at):
                        created += 1
            open_checkins[student_id] = row

        for row in open_checkins.values():
            created_at = _parse_log_time(row["created_at"])
            alert_at = _missing_checkout_alert_at(row, cutoff_text)
            if created_at and alert_at <= current:
                attendance_date = _date_text(created_at)
                if _finalize_missing_checkout(db, row, attendance_date, alert_at):
                    created += 1
        return created


def mark_stale_checkin_missing_checkout(student_id: int, now: datetime | None = None) -> bool:
    current = now or datetime.now()
    with get_db() as db:
        row = db.execute(
            """
            SELECT * FROM access_logs
            WHERE student_id=? AND result='success' AND action IN ('check_in', 'check_out')
            ORDER BY created_at DESC, id DESC
            LIMIT 1
            """,
            (student_id,),
        ).fetchone()
        if not row or row["action"] != "check_in":
            return False
        created_at = _parse_log_time(row["created_at"])
        if not created_at or created_at.date() >= current.date():
            return False
        attendance_date = _date_text(created_at)
        _finalize_missing_checkout(db, row, attendance_date)
        return True


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
    with open(file_path, "wb") as f:
        f.write(image_bytes)
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
