from fastapi import APIRouter, Depends
from app.db import get_db, row_to_dict
from app.routers.deps import require_student
from app.routers.students import MAX_FACE_EMBEDDINGS_PER_STUDENT
from app.services.attendance_service import attendance_record_context, ensure_attendance_records

router = APIRouter(prefix="/api/student", tags=["student"], dependencies=[Depends(require_student)])


def _current_student(user):
    with get_db() as db:
        student = db.execute(
            """
            SELECT s.*, COUNT(f.id) AS face_count
            FROM students s
            LEFT JOIN student_faces f ON f.student_id = s.id
            WHERE s.id = ?
            GROUP BY s.id
            """,
            (user["student_id"],),
        ).fetchone()
    return row_to_dict(student)


@router.get("/me")
def student_me(user=Depends(require_student)):
    return {"user": user, "student": _current_student(user)}


@router.get("/faces")
def my_registered_faces(user=Depends(require_student)):
    with get_db() as db:
        rows = db.execute(
            """
            SELECT id, image_path, created_at
            FROM student_faces
            WHERE student_id = ?
            ORDER BY id DESC
            """,
            (user["student_id"],),
        ).fetchall()
    items = [row_to_dict(row) for row in rows]
    latest_update = items[0]["created_at"] if items else None
    return {
        "items": items,
        "count": len(items),
        "max_faces": MAX_FACE_EMBEDDINGS_PER_STUDENT,
        "latest_update": latest_update,
    }


@router.get("/access-logs")
def my_access_logs(
    limit: int = 100,
    date_from: str | None = None,
    date_to: str | None = None,
    action: str | None = None,
    result: str | None = None,
    user=Depends(require_student),
):
    clauses = ["student_id=?"]
    params = [user["student_id"]]
    if date_from:
        clauses.append("date(created_at) >= date(?)")
        params.append(date_from)
    if date_to:
        clauses.append("date(created_at) <= date(?)")
        params.append(date_to)
    if action:
        clauses.append("action=?")
        params.append(action)
    if result:
        clauses.append("result=?")
        params.append(result)
    safe_limit = max(1, min(int(limit), 300))
    with get_db() as db:
        rows = db.execute(
            f"""
            SELECT * FROM access_logs
            WHERE {' AND '.join(clauses)}
            ORDER BY id DESC
            LIMIT ?
            """,
            (*params, safe_limit),
        ).fetchall()
    return {"items": [row_to_dict(r) for r in rows], "count": len(rows)}


@router.get("/attendance-records")
def my_attendance_records(
    limit: int = 100,
    date_from: str | None = None,
    date_to: str | None = None,
    status: str | None = None,
    user=Depends(require_student),
):
    ensure_attendance_records()
    clauses = ["student_id=?"]
    params = [user["student_id"]]
    if date_from:
        clauses.append("date(attendance_date) >= date(?)")
        params.append(date_from)
    if date_to:
        clauses.append("date(attendance_date) <= date(?)")
        params.append(date_to)
    if status:
        clauses.append("status=?")
        params.append(status)
    safe_limit = max(1, min(int(limit), 300))
    with get_db() as db:
        rows = db.execute(
            f"""
            SELECT * FROM attendance_records
            WHERE {' AND '.join(clauses)}
            ORDER BY attendance_date DESC, id DESC
            LIMIT ?
            """,
            (*params, safe_limit),
        ).fetchall()
        items = []
        for row in rows:
            item = row_to_dict(row)
            context = attendance_record_context(db, item["student_id"], item["attendance_date"])
            item.update(context)
            items.append(item)
    return {"items": items, "count": len(items)}
