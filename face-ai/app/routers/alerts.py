from fastapi import APIRouter, Depends, HTTPException, Request
from pydantic import BaseModel

from app.db import get_db, row_to_dict
from app.routers.deps import require_admin, require_admin_or_lab_manager
from app.routers.query_filters import append_alert_event_date_filters
from app.services.attendance_service import mark_missing_checkouts
from app.services.audit_service import write_audit_log

router = APIRouter(prefix="/api", tags=["alerts"])


class AlertStatusUpdate(BaseModel):
    status: str


@router.get("/alerts", dependencies=[Depends(require_admin_or_lab_manager)])
def alerts(
    limit: int = 100,
    date_from: str | None = None,
    date_to: str | None = None,
    type: str | None = None,
    status: str | None = None,
    q: str | None = None,
):
    clauses = ["1=1"]
    params = []
    append_alert_event_date_filters(clauses, params, date_from, date_to)
    if type:
        clauses.append("type=?")
        params.append(type)
    if status:
        clauses.append("status=?")
        params.append(status)
    if q:
        clauses.append("message LIKE ?")
        params.append(f"%{q}%")
    safe_limit = max(1, min(int(limit), 500))
    with get_db() as db:
        rows = db.execute(
            f"SELECT * FROM alerts WHERE {' AND '.join(clauses)} ORDER BY id DESC LIMIT ?",
            (*params, safe_limit),
        ).fetchall()
    return {"items": [row_to_dict(r) for r in rows], "count": len(rows)}


@router.post("/alerts/scan-missing-checkouts")
def scan_missing_checkouts(request: Request, actor=Depends(require_admin_or_lab_manager)):
    created = mark_missing_checkouts()
    with get_db() as db:
        write_audit_log(
            db,
            actor,
            "alerts.scan_missing_checkouts",
            "alert",
            details={"created": created},
            request=request,
        )
    return {"ok": True, "created": created}


@router.put("/alerts/{alert_id}/status")
def update_alert_status(alert_id: int, payload: AlertStatusUpdate, request: Request, actor=Depends(require_admin_or_lab_manager)):
    if payload.status not in {"new", "resolved", "ignored"}:
        raise HTTPException(status_code=400, detail="Trạng thái cảnh báo không hợp lệ.")
    with get_db() as db:
        current = db.execute("SELECT id, type, message, status FROM alerts WHERE id=?", (alert_id,)).fetchone()
        if not current:
            raise HTTPException(status_code=404, detail="Không tìm thấy cảnh báo.")
        db.execute("UPDATE alerts SET status=? WHERE id=?", (payload.status, alert_id))
        row = db.execute("SELECT * FROM alerts WHERE id=?", (alert_id,)).fetchone()
        if current["status"] != payload.status:
            write_audit_log(
                db,
                actor,
                "alerts.status.update",
                "alert",
                alert_id,
                current["message"],
                {
                    "type": current["type"],
                    "changes": {"status": {"old": current["status"], "new": payload.status}},
                },
                request,
            )
    return {"item": row_to_dict(row)}


@router.delete("/alerts/{alert_id}")
def delete_alert(alert_id: int, request: Request, actor=Depends(require_admin)):
    with get_db() as db:
        current = db.execute("SELECT id, type, message, status FROM alerts WHERE id=?", (alert_id,)).fetchone()
        if not current:
            raise HTTPException(status_code=404, detail="Không tìm thấy cảnh báo.")
        db.execute("DELETE FROM alerts WHERE id=?", (alert_id,))
        write_audit_log(
            db,
            actor,
            "alerts.delete",
            "alert",
            alert_id,
            current["message"],
            {"type": current["type"], "status": current["status"]},
            request,
        )
    return {"ok": True}
