from fastapi import APIRouter, Depends, HTTPException, Request

from app.db import get_db, row_to_dict
from app.routers.deps import require_admin, require_admin_or_lab_manager
from app.routers.query_filters import append_created_date_filters
from app.services.attendance_service import recalculate_student_attendance_record
from app.services.audit_service import write_audit_log

router = APIRouter(prefix="/api", tags=["access_logs"])


@router.get("/access-logs", dependencies=[Depends(require_admin_or_lab_manager)])
def access_logs(
    limit: int = 100,
    date_from: str | None = None,
    date_to: str | None = None,
    action: str | None = None,
    result: str | None = None,
    q: str | None = None,
):
    clauses = ["1=1"]
    params = []
    append_created_date_filters(clauses, params, date_from, date_to)
    if action:
        clauses.append("action=?")
        params.append(action)
    if result:
        clauses.append("result=?")
        params.append(result)
    if q:
        clauses.append("(student_code LIKE ? OR full_name LIKE ? OR note LIKE ?)")
        like = f"%{q}%"
        params.extend([like, like, like])
    safe_limit = max(1, min(int(limit), 500))
    with get_db() as db:
        rows = db.execute(
            f"SELECT * FROM access_logs WHERE {' AND '.join(clauses)} ORDER BY id DESC LIMIT ?",
            (*params, safe_limit),
        ).fetchall()
    return {"items": [row_to_dict(r) for r in rows], "count": len(rows)}


@router.delete("/access-logs/{log_id}")
def delete_access_log(log_id: int, request: Request, actor=Depends(require_admin)):
    should_recalculate = False
    student_id = None
    attendance_date = None
    with get_db() as db:
        current = db.execute(
            """
            SELECT id, student_id, student_code, full_name, action, result, date(created_at) AS attendance_date
            FROM access_logs
            WHERE id=?
            """,
            (log_id,),
        ).fetchone()
        if not current:
            raise HTTPException(status_code=404, detail="Không tìm thấy lịch sử.")
        student_id = current["student_id"]
        attendance_date = current["attendance_date"]
        should_recalculate = (
            student_id is not None
            and attendance_date is not None
            and current["result"] == "success"
            and current["action"] in {"check_in", "check_out"}
        )
        db.execute("DELETE FROM access_logs WHERE id=?", (log_id,))
        write_audit_log(
            db,
            actor,
            "access_logs.delete",
            "access_log",
            log_id,
            f"{current['student_code'] or 'Unknown'} {current['action']} {current['attendance_date'] or ''}".strip(),
            {
                "student_id": current["student_id"],
                "student_code": current["student_code"],
                "full_name": current["full_name"],
                "action": current["action"],
                "result": current["result"],
                "attendance_date": current["attendance_date"],
            },
            request,
        )
    recalculated = (
        recalculate_student_attendance_record(student_id, attendance_date)
        if should_recalculate
        else False
    )
    return {"ok": True, "attendance_recalculated": recalculated}
