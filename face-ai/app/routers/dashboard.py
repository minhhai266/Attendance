from fastapi import APIRouter, Depends

from app.db import get_db, row_to_dict
from app.routers.deps import require_admin_or_lab_manager
from app.services.attendance_service import ensure_attendance_records

router = APIRouter(prefix="/api", tags=["dashboard"])


@router.get("/dashboard", dependencies=[Depends(require_admin_or_lab_manager)])
def dashboard():
    ensure_attendance_records()
    with get_db() as db:
        total_students = db.execute("SELECT COUNT(*) c FROM students").fetchone()["c"]
        active_students = db.execute("SELECT COUNT(*) c FROM students WHERE status='active'").fetchone()["c"]
        face_registered = db.execute(
            """
            SELECT COUNT(DISTINCT f.student_id) c
            FROM student_faces f
            JOIN students s ON s.id = f.student_id
            WHERE s.status='active'
            """
        ).fetchone()["c"]
        checkin_today = db.execute("SELECT COUNT(*) c FROM access_logs WHERE action='check_in' AND result='success' AND date(created_at)=date('now','localtime')").fetchone()["c"]
        checkout_today = db.execute("SELECT COUNT(*) c FROM access_logs WHERE action='check_out' AND result='success' AND date(created_at)=date('now','localtime')").fetchone()["c"]
        alerts_today = db.execute("SELECT COUNT(*) c FROM alerts WHERE date(COALESCE(event_date, created_at))=date('now','localtime')").fetchone()["c"]
        on_time_today = db.execute("SELECT COUNT(*) c FROM attendance_records WHERE status='present_on_time' AND date(attendance_date)=date('now','localtime')").fetchone()["c"]
        late_today = db.execute("SELECT COUNT(*) c FROM attendance_records WHERE status IN ('late','late_and_early_leave') AND date(attendance_date)=date('now','localtime')").fetchone()["c"]
        absent_today = db.execute("SELECT COUNT(*) c FROM attendance_records WHERE status='absent' AND date(attendance_date)=date('now','localtime')").fetchone()["c"]
        missing_checkout_today = db.execute("SELECT COUNT(*) c FROM attendance_records WHERE status='missing_checkout' AND date(attendance_date)=date('now','localtime')").fetchone()["c"]
        recent = db.execute("SELECT * FROM access_logs ORDER BY id DESC LIMIT 5").fetchall()
    return {
        "stats": {
            "total_students": total_students,
            "active_students": active_students,
            "face_registered": face_registered,
            "not_registered": max(total_students - face_registered, 0),
            "checkin_today": checkin_today,
            "checkout_today": checkout_today,
            "alerts_today": alerts_today,
            "on_time_today": on_time_today,
            "late_today": late_today,
            "absent_today": absent_today,
            "missing_checkout_today": missing_checkout_today,
        },
        "recent_logs": [row_to_dict(r) for r in recent],
    }
