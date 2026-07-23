from fastapi import APIRouter, Depends, HTTPException, Request
from pydantic import BaseModel, Field

from app.db import get_db, row_to_dict
from app.routers.deps import require_admin_or_lab_manager
from app.routers.query_filters import append_attendance_date_filters
from app.services.audit_service import write_audit_log
from app.services.attendance_service import (
    attendance_record_context,
    attendance_record_detail,
    recalculate_attendance_records,
    resolve_missing_checkout_record,
)

router = APIRouter(prefix="/api", tags=["attendance"])


class AttendanceRecalculateRequest(BaseModel):
    date_from: str | None = None
    date_to: str | None = None


class MissingCheckoutResolutionRequest(BaseModel):
    resolution_type: str
    checkout_time: str | None = Field(default=None, pattern=r"^\d{2}:\d{2}$")
    reason: str


def _attendance_record_item(db, row) -> dict:
    item = row_to_dict(row)
    context = attendance_record_context(db, item["student_id"], item["attendance_date"])
    item.update({
        "presence_status": context["presence_status"],
        "last_action": context["last_action"],
        "last_log_at": context["last_log_at"],
        "current_out_since_at": context["current_out_since_at"],
        "outside_count": context["outside_count"],
        "outside_minutes": context["outside_minutes"],
    })
    return item


@router.get("/attendance-records", dependencies=[Depends(require_admin_or_lab_manager)])
def attendance_records(
    limit: int = 300,
    date_from: str | None = None,
    date_to: str | None = None,
    status: str | None = None,
    q: str | None = None,
):
    recalculate_attendance_records(date_from, date_to)
    clauses = ["1=1"]
    params = []
    append_attendance_date_filters(clauses, params, date_from, date_to)
    if status:
        clauses.append("status=?")
        params.append(status)
    if q:
        clauses.append("(student_code LIKE ? OR full_name LIKE ?)")
        like = f"%{q}%"
        params.extend([like, like])
    safe_limit = max(1, min(int(limit), 1000))
    with get_db() as db:
        rows = db.execute(
            f"""
            SELECT * FROM attendance_records
            WHERE {' AND '.join(clauses)}
            ORDER BY attendance_date DESC, full_name ASC, id ASC
            LIMIT ?
            """,
            (*params, safe_limit),
        ).fetchall()
        items = [_attendance_record_item(db, r) for r in rows]
    return {"items": items, "count": len(rows)}


@router.get("/attendance-records/{record_id}/details", dependencies=[Depends(require_admin_or_lab_manager)])
def attendance_record_details(record_id: int):
    detail = attendance_record_detail(record_id)
    if not detail:
        raise HTTPException(status_code=404, detail="Không tìm thấy bản ghi điểm danh.")
    return detail


@router.post("/attendance-records/{record_id}/resolve-missing-checkout")
def resolve_missing_checkout(record_id: int, payload: MissingCheckoutResolutionRequest, request: Request, actor=Depends(require_admin_or_lab_manager)):
    try:
        item = resolve_missing_checkout_record(
            record_id,
            payload.resolution_type,
            payload.reason,
            payload.checkout_time,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    if not item:
        raise HTTPException(status_code=404, detail="Không tìm thấy bản ghi điểm danh.")
    with get_db() as db:
        write_audit_log(
            db,
            actor,
            "attendance.missing_checkout.resolve",
            "attendance_record",
            record_id,
            f"{item.get('student_code')} - {item.get('full_name')} {item.get('attendance_date')}",
            {
                "resolution_type": payload.resolution_type,
                "checkout_time": payload.checkout_time,
                "reason": payload.reason,
                "status": item.get("status"),
            },
            request,
        )
    return {"ok": True, "item": item}


@router.post("/attendance-records/recalculate")
def recalculate_attendance(payload: AttendanceRecalculateRequest, request: Request, actor=Depends(require_admin_or_lab_manager)):
    updated = recalculate_attendance_records(payload.date_from, payload.date_to)
    with get_db() as db:
        write_audit_log(
            db,
            actor,
            "attendance.recalculate",
            "attendance_record",
            details={"date_from": payload.date_from, "date_to": payload.date_to, "updated": updated},
            request=request,
        )
    return {"ok": True, "updated": updated}
