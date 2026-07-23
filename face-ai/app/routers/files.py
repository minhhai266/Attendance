from pathlib import Path

from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import FileResponse

from app.db import get_db
from app.routers.deps import STAFF_ROLES, STUDENT_ROLE, current_user
from app.services.private_storage import resolve_private_file


router = APIRouter(prefix="/api/files", tags=["files"])


def _no_store_file(path: Path) -> FileResponse:
    return FileResponse(
        path,
        media_type="image/jpeg",
        headers={
            "Cache-Control": "no-store, no-cache, must-revalidate, max-age=0",
            "Pragma": "no-cache",
            "Expires": "0",
        },
    )


def _is_staff(user: dict) -> bool:
    return user.get("role") in STAFF_ROLES


def _require_owner_or_staff(user: dict, student_id: int | None) -> None:
    if _is_staff(user):
        return
    if user.get("role") == STUDENT_ROLE and student_id and user.get("student_id") == student_id:
        return
    raise HTTPException(status_code=403, detail="Ban khong co quyen xem tep nay.")


@router.get("/face/{face_id}")
def face_file(face_id: int, user=Depends(current_user)):
    with get_db() as db:
        row = db.execute(
            """
            SELECT id, student_id, image_path
            FROM student_faces
            WHERE id = ?
            """,
            (face_id,),
        ).fetchone()
    if not row:
        raise HTTPException(status_code=404, detail="Khong tim thay anh khuon mat.")

    _require_owner_or_staff(user, row["student_id"])
    path = resolve_private_file(row["image_path"], "face")
    if not path:
        raise HTTPException(status_code=404, detail="Khong tim thay file anh khuon mat.")
    return _no_store_file(path)


@router.get("/evidence/{log_id}")
@router.get("/evidence/access-log/{log_id}")
def access_log_evidence_file(log_id: int, user=Depends(current_user)):
    with get_db() as db:
        row = db.execute(
            """
            SELECT id, student_id, evidence_image_path
            FROM access_logs
            WHERE id = ?
            """,
            (log_id,),
        ).fetchone()
    if not row:
        raise HTTPException(status_code=404, detail="Khong tim thay lich su.")
    if not row["evidence_image_path"]:
        raise HTTPException(status_code=404, detail="Lich su nay khong co anh bang chung.")

    _require_owner_or_staff(user, row["student_id"])
    path = resolve_private_file(row["evidence_image_path"], "evidence")
    if not path:
        raise HTTPException(status_code=404, detail="Khong tim thay file anh bang chung.")
    return _no_store_file(path)


@router.get("/evidence/alert/{alert_id}")
def alert_evidence_file(alert_id: int, user=Depends(current_user)):
    if not _is_staff(user):
        raise HTTPException(status_code=403, detail="Ban khong co quyen xem anh canh bao.")

    with get_db() as db:
        row = db.execute(
            """
            SELECT id, evidence_image_path
            FROM alerts
            WHERE id = ?
            """,
            (alert_id,),
        ).fetchone()
    if not row:
        raise HTTPException(status_code=404, detail="Khong tim thay canh bao.")
    if not row["evidence_image_path"]:
        raise HTTPException(status_code=404, detail="Canh bao nay khong co anh bang chung.")

    path = resolve_private_file(row["evidence_image_path"], "evidence")
    if not path:
        raise HTTPException(status_code=404, detail="Khong tim thay file anh bang chung.")
    return _no_store_file(path)
