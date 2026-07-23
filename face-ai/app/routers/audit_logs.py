from fastapi import APIRouter, Depends

from app.db import get_db, row_to_dict
from app.routers.deps import require_admin
from app.routers.query_filters import append_created_date_filters


router = APIRouter(prefix="/api/audit-logs", tags=["audit_logs"])


@router.get("", dependencies=[Depends(require_admin)])
def audit_logs(
    limit: int = 200,
    date_from: str | None = None,
    date_to: str | None = None,
    actor: str | None = None,
    action: str | None = None,
    entity_type: str | None = None,
    q: str | None = None,
):
    clauses = ["1=1"]
    params = []
    append_created_date_filters(clauses, params, date_from, date_to)
    if actor:
        clauses.append("actor_username LIKE ?")
        params.append(f"%{actor}%")
    if action:
        clauses.append("action LIKE ?")
        params.append(f"%{action}%")
    if entity_type:
        clauses.append("entity_type=?")
        params.append(entity_type)
    if q:
        clauses.append("(entity_label LIKE ? OR details_json LIKE ? OR action LIKE ?)")
        like = f"%{q}%"
        params.extend([like, like, like])

    safe_limit = max(1, min(int(limit), 1000))
    with get_db() as db:
        rows = db.execute(
            f"""
            SELECT *
            FROM audit_logs
            WHERE {' AND '.join(clauses)}
            ORDER BY id DESC
            LIMIT ?
            """,
            (*params, safe_limit),
        ).fetchall()
    return {"items": [row_to_dict(row) for row in rows], "count": len(rows)}
