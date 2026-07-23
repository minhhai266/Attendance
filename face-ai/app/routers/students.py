import sqlite3
from datetime import datetime

from fastapi import APIRouter, Depends, File, HTTPException, Request, UploadFile
from pydantic import BaseModel, Field, field_validator

from app.core.config import settings
from app.core.time_utils import validate_time_text
from app.db import get_db, get_setting, row_to_dict
from app.routers.deps import require_admin, require_admin_or_lab_manager
from app.services.attendance_service import recalculate_student_attendance_records
from app.services.audit_service import audit_diff, write_audit_log
from app.services.face_cache_service import face_embedding_cache
from app.services.face_service import face_service
from app.services.student_face_service import (
    FACE_SCAN_REQUIRED_FILES,
    MAX_FACE_EMBEDDINGS_PER_STUDENT,
    delete_face_image_files as _delete_face_image_files,
    delete_student_faces as _delete_student_faces,
    duplicate_warnings as _duplicate_warnings,
    prepare_face_upload as _prepare_face_upload,
    raise_if_duplicate_face as _raise_if_duplicate_face,
    read_limited_image_upload as _read_limited_image_upload,
    save_face_image as _save_face_image,
    trim_old_face_embeddings as _trim_old_face_embeddings,
)

router = APIRouter(prefix="/api/students", tags=["students"])


class StudentCreate(BaseModel):
    student_code: str
    full_name: str
    class_name: str | None = None
    status: str = "active"

    @field_validator("student_code", "full_name")
    @classmethod
    def validate_required_text(cls, value: str) -> str:
        value = (value or "").strip()
        if not value:
            raise ValueError("KhÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.")
        return value

    @field_validator("class_name")
    @classmethod
    def normalize_optional_text(cls, value: str | None) -> str | None:
        value = (value or "").strip()
        return value or None

    @field_validator("status")
    @classmethod
    def validate_status(cls, value: str) -> str:
        value = (value or "active").strip().lower()
        if value not in {"active", "inactive"}:
            raise ValueError("Tráº¡ng thÃ¡i chá»‰ Ä‘Æ°á»£c lÃ  active hoáº·c inactive.")
        return value


class StudentUpdate(StudentCreate):
    pass


class FaceAnalyzeRequest(BaseModel):
    image: str


class StudentWorkTimeUpdate(BaseModel):
    work_start_time: str = Field(pattern=r"^\d{2}:\d{2}$")
    work_end_time: str = Field(pattern=r"^\d{2}:\d{2}$")

    @field_validator("work_start_time", "work_end_time")
    @classmethod
    def validate_time_range(cls, value: str) -> str:
        return validate_time_text(value)


def _get_student_or_404(student_id: int):
    with get_db() as db:
        student = db.execute("SELECT * FROM students WHERE id = ?", (student_id,)).fetchone()
    if not student:
        raise HTTPException(status_code=404, detail="KhÃ´ng tÃ¬m tháº¥y sinh viÃªn.")
    return student


@router.get("", dependencies=[Depends(require_admin_or_lab_manager)])
def list_students(q: str = ""):
    default_start = get_setting("work_start_time", settings.work_start_time)
    default_end = get_setting("work_end_time", settings.work_end_time)
    with get_db() as db:
        if q:
            rows = db.execute(
                """
                SELECT s.*, COUNT(f.id) AS face_count,
                    COALESCE(a.work_start_time, ?) AS work_start_time,
                    COALESCE(a.work_end_time, ?) AS work_end_time
                FROM students s
                LEFT JOIN student_faces f ON s.id = f.student_id
                LEFT JOIN student_attendance_settings a ON a.student_id = s.id
                WHERE (s.student_code LIKE ? OR s.full_name LIKE ?)
                  AND s.status='active'
                GROUP BY s.id
                ORDER BY s.id DESC
                """,
                (default_start, default_end, f"%{q}%", f"%{q}%"),
            ).fetchall()
        else:
            rows = db.execute(
                """
                SELECT s.*, COUNT(f.id) AS face_count,
                    COALESCE(a.work_start_time, ?) AS work_start_time,
                    COALESCE(a.work_end_time, ?) AS work_end_time
                FROM students s
                LEFT JOIN student_faces f ON s.id = f.student_id
                LEFT JOIN student_attendance_settings a ON a.student_id = s.id
                WHERE s.status='active'
                GROUP BY s.id
                ORDER BY s.id DESC
                """,
                (default_start, default_end),
            ).fetchall()
    return {"items": [row_to_dict(r) for r in rows], "max_faces": MAX_FACE_EMBEDDINGS_PER_STUDENT}


@router.post("")
def create_student(payload: StudentCreate, request: Request, actor=Depends(require_admin_or_lab_manager)):
    try:
        with get_db() as db:
            cur = db.execute(
                """
                INSERT INTO students(student_code, full_name, class_name, status, created_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                (
                    payload.student_code,
                    payload.full_name,
                    payload.class_name,
                    payload.status,
                    datetime.now().isoformat(timespec="seconds"),
                ),
            )
            student_id = cur.lastrowid
            write_audit_log(
                db,
                actor,
                "students.create",
                "student",
                student_id,
                f"{payload.student_code} - {payload.full_name}",
                {
                    "student_code": payload.student_code,
                    "full_name": payload.full_name,
                    "class_name": payload.class_name,
                    "status": payload.status,
                },
                request,
            )
        return {"ok": True, "id": student_id}
    except sqlite3.IntegrityError:
        raise HTTPException(status_code=400, detail="MÃ£ sinh viÃªn Ä‘Ã£ tá»“n táº¡i.")
    except Exception as exc:
        raise HTTPException(status_code=400, detail=f"KhÃ´ng táº¡o Ä‘Æ°á»£c sinh viÃªn: {exc}")


@router.get("/{student_id}", dependencies=[Depends(require_admin_or_lab_manager)])
def get_student(student_id: int):
    with get_db() as db:
        student = db.execute("SELECT * FROM students WHERE id = ?", (student_id,)).fetchone()
        faces = db.execute(
            "SELECT id, image_path, created_at FROM student_faces WHERE student_id = ? ORDER BY id DESC",
            (student_id,),
        ).fetchall()
    if not student:
        raise HTTPException(status_code=404, detail="KhÃ´ng tÃ¬m tháº¥y sinh viÃªn.")
    return {
        "student": row_to_dict(student),
        "faces": [row_to_dict(f) for f in faces],
        "face_count": len(faces),
        "max_faces": MAX_FACE_EMBEDDINGS_PER_STUDENT,
    }


@router.put("/{student_id}")
def update_student(student_id: int, payload: StudentUpdate, request: Request, actor=Depends(require_admin_or_lab_manager)):
    image_paths = []
    invalidate_face_cache = False
    with get_db() as db:
        student = db.execute("SELECT * FROM students WHERE id = ?", (student_id,)).fetchone()
        if not student:
            raise HTTPException(status_code=404, detail="KhÃ´ng tÃ¬m tháº¥y sinh viÃªn.")
        new_status = payload.status
        db.execute(
            """
            UPDATE students SET student_code=?, full_name=?, class_name=?, status=? WHERE id=?
            """,
            (payload.student_code, payload.full_name, payload.class_name, new_status, student_id),
        )
        if new_status != "active":
            image_paths = _delete_student_faces(db, student_id)
        before = row_to_dict(student)
        after = {
            **before,
            "student_code": payload.student_code,
            "full_name": payload.full_name,
            "class_name": payload.class_name,
            "status": new_status,
        }
        changes = audit_diff(before, after, ["student_code", "full_name", "class_name", "status"])
        invalidate_face_cache = any(key in changes for key in ("student_code", "full_name", "status"))
        if changes:
            write_audit_log(
                db,
                actor,
                "students.update",
                "student",
                student_id,
                f"{payload.student_code} - {payload.full_name}",
                {"changes": changes, "deleted_face_count": len(image_paths)},
                request,
            )
    if invalidate_face_cache:
        face_embedding_cache.invalidate()
    _delete_face_image_files(image_paths)
    return {"ok": True, "deleted_face_count": len(image_paths)}


@router.put("/{student_id}/work-time")
def update_student_work_time(student_id: int, payload: StudentWorkTimeUpdate, request: Request, actor=Depends(require_admin_or_lab_manager)):
    with get_db() as db:
        student = db.execute("SELECT id, student_code, full_name FROM students WHERE id = ?", (student_id,)).fetchone()
        if not student:
            raise HTTPException(status_code=404, detail="KhÃ´ng tÃ¬m tháº¥y sinh viÃªn.")
        before = db.execute(
            "SELECT work_start_time, work_end_time FROM student_attendance_settings WHERE student_id=?",
            (student_id,),
        ).fetchone()
        db.execute(
            """
            INSERT INTO student_attendance_settings(student_id, work_start_time, work_end_time, updated_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(student_id) DO UPDATE SET
                work_start_time=excluded.work_start_time,
                work_end_time=excluded.work_end_time,
                updated_at=excluded.updated_at
            """,
            (
                student_id,
                payload.work_start_time,
                payload.work_end_time,
                datetime.now().isoformat(timespec="seconds"),
            ),
        )
        changes = audit_diff(
            row_to_dict(before) or {},
            {"work_start_time": payload.work_start_time, "work_end_time": payload.work_end_time},
            ["work_start_time", "work_end_time"],
        )
        if changes:
            write_audit_log(
                db,
                actor,
                "students.work_time.update",
                "student",
                student_id,
                f"{student['student_code']} - {student['full_name']}",
                {"changes": changes},
                request,
            )
    updated = recalculate_student_attendance_records(student_id)
    return {"ok": True, "updated": updated}


@router.delete("/{student_id}/work-time")
def reset_student_work_time(student_id: int, request: Request, actor=Depends(require_admin_or_lab_manager)):
    with get_db() as db:
        student = db.execute("SELECT id, student_code, full_name FROM students WHERE id = ?", (student_id,)).fetchone()
        if not student:
            raise HTTPException(status_code=404, detail="KhÃ´ng tÃ¬m tháº¥y sinh viÃªn.")
        before = db.execute(
            "SELECT work_start_time, work_end_time FROM student_attendance_settings WHERE student_id=?",
            (student_id,),
        ).fetchone()
        db.execute("DELETE FROM student_attendance_settings WHERE student_id=?", (student_id,))
        if before:
            write_audit_log(
                db,
                actor,
                "students.work_time.reset",
                "student",
                student_id,
                f"{student['student_code']} - {student['full_name']}",
                {"previous": row_to_dict(before)},
                request,
            )
    updated = recalculate_student_attendance_records(student_id)
    return {"ok": True, "updated": updated}


@router.delete("/{student_id}")
def delete_student(student_id: int, request: Request, actor=Depends(require_admin)):
    with get_db() as db:
        student = db.execute("SELECT id, student_code, full_name, class_name, status FROM students WHERE id=?", (student_id,)).fetchone()
        if not student:
            raise HTTPException(status_code=404, detail="KhÃ´ng tÃ¬m tháº¥y sinh viÃªn.")
        db.execute("UPDATE access_logs SET student_id=NULL WHERE student_id=?", (student_id,))
        db.execute("DELETE FROM student_attendance_settings WHERE student_id=?", (student_id,))
        db.execute("DELETE FROM attendance_records WHERE student_id=?", (student_id,))
        image_paths = _delete_student_faces(db, student_id)
        db.execute("DELETE FROM students WHERE id=?", (student_id,))
        write_audit_log(
            db,
            actor,
            "students.delete",
            "student",
            student_id,
            f"{student['student_code']} - {student['full_name']}",
            {
                "student_code": student["student_code"],
                "full_name": student["full_name"],
                "class_name": student["class_name"],
                "status": student["status"],
                "deleted_face_count": len(image_paths),
            },
            request,
        )
    face_embedding_cache.invalidate()
    _delete_face_image_files(image_paths)
    return {"ok": True}


@router.post("/face-scan/analyze", dependencies=[Depends(require_admin_or_lab_manager)])
def analyze_face_scan(payload: FaceAnalyzeRequest):
    try:
        image = face_service.read_image_from_base64(payload.image)
        return face_service.analyze_single_face_pose(image)
    except Exception as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.post("/{student_id}/faces/upload")
async def upload_face(student_id: int, request: Request, file: UploadFile = File(...), actor=Depends(require_admin_or_lab_manager)):
    student = _get_student_or_404(student_id)
    if student["status"] != "active":
        raise HTTPException(status_code=400, detail="KhÃ´ng thá»ƒ Ä‘Äƒng kÃ½ khuÃ´n máº·t cho sinh viÃªn inactive.")
    data = await _read_limited_image_upload(file, "Anh")
    prepared = _prepare_face_upload(data, "Anh")

    image_path = _save_face_image(student, prepared["data"])
    embedding_text = face_service.serialize_embedding(prepared["embedding"])
    try:
        with get_db() as db:
            duplicate_warnings = _duplicate_warnings(db, student_id, [prepared["embedding"]])
            _raise_if_duplicate_face(duplicate_warnings)
            db.execute(
                "INSERT INTO student_faces(student_id, image_path, embedding, created_at) VALUES (?, ?, ?, ?)",
                (student_id, image_path, embedding_text, datetime.now().isoformat(timespec="seconds")),
            )
            face_id = db.execute("SELECT last_insert_rowid() id").fetchone()["id"]
            face_count, deleted_image_paths = _trim_old_face_embeddings(db, student_id)
            write_audit_log(
                db,
                actor,
                "faces.upload",
                "student_face",
                face_id,
                f"{student['student_code']} - {student['full_name']}",
                {
                    "student_id": student_id,
                    "student_code": student["student_code"],
                    "face_count": face_count,
                    "trimmed_face_count": len(deleted_image_paths),
                    "duplicate_warnings": duplicate_warnings,
                },
                request,
            )
    except Exception:
        _delete_face_image_files([image_path])
        raise
    face_embedding_cache.invalidate()
    _delete_face_image_files(deleted_image_paths)
    return {
        "ok": True,
        "bbox": prepared["bbox"],
        "quality": prepared["quality"],
        "face_count": face_count,
        "max_faces": MAX_FACE_EMBEDDINGS_PER_STUDENT,
        "duplicate_warnings": duplicate_warnings,
        "message": "ÄÃ£ thÃªm áº£nh khuÃ´n máº·t bá»• sung.",
    }


@router.post("/{student_id}/faces/scan")
async def replace_face_scan(student_id: int, request: Request, files: list[UploadFile] = File(...), actor=Depends(require_admin_or_lab_manager)):
    student = _get_student_or_404(student_id)
    if student["status"] != "active":
        raise HTTPException(status_code=400, detail="KhÃ´ng thá»ƒ quÃ©t khuÃ´n máº·t cho sinh viÃªn inactive.")
    if len(files) != FACE_SCAN_REQUIRED_FILES:
        raise HTTPException(status_code=400, detail=f"Vui lÃ²ng gá»­i Ä‘á»§ {FACE_SCAN_REQUIRED_FILES} áº£nh quÃ©t.")

    validated = []
    for index, file in enumerate(files, start=1):
        data = await _read_limited_image_upload(file, f"Anh {index}")
        prepared = _prepare_face_upload(data, f"Anh {index}")
        validated.append({
            "data": prepared["data"],
            "embedding": prepared["embedding"],
            "bbox": prepared["bbox"],
            "quality": prepared["quality"],
        })

    created_at = datetime.now().isoformat(timespec="seconds")
    saved = []
    for item in validated:
        saved.append({
            "image_path": _save_face_image(student, item["data"]),
            "embedding": face_service.serialize_embedding(item["embedding"]),
            "bbox": item["bbox"],
        })

    try:
        with get_db() as db:
            duplicate_warnings = _duplicate_warnings(db, student_id, [item["embedding"] for item in validated])
            _raise_if_duplicate_face(duplicate_warnings)
            old_rows = db.execute("SELECT image_path FROM student_faces WHERE student_id = ?", (student_id,)).fetchall()
            old_image_paths = [row["image_path"] for row in old_rows if row["image_path"]]
            db.execute("DELETE FROM student_faces WHERE student_id = ?", (student_id,))
            for item in saved:
                db.execute(
                    "INSERT INTO student_faces(student_id, image_path, embedding, created_at) VALUES (?, ?, ?, ?)",
                    (student_id, item["image_path"], item["embedding"], created_at),
                )
            write_audit_log(
                db,
                actor,
                "faces.scan_replace",
                "student",
                student_id,
                f"{student['student_code']} - {student['full_name']}",
                {
                    "student_id": student_id,
                    "student_code": student["student_code"],
                    "new_face_count": len(saved),
                    "deleted_face_count": len(old_rows),
                    "duplicate_warnings": duplicate_warnings,
                },
                request,
            )
    except Exception:
        _delete_face_image_files([item["image_path"] for item in saved])
        raise
    face_embedding_cache.invalidate()
    _delete_face_image_files(old_image_paths)

    return {
        "ok": True,
        "face_count": len(saved),
        "max_faces": MAX_FACE_EMBEDDINGS_PER_STUDENT,
        "bboxes": [item["bbox"] for item in saved],
        "qualities": [item["quality"] for item in validated],
        "duplicate_warnings": duplicate_warnings,
        "message": "ÄÃ£ cáº­p nháº­t bá»™ khuÃ´n máº·t má»›i.",
    }


@router.delete("/{student_id}/faces/{face_id}")
def delete_face(student_id: int, face_id: int, request: Request, actor=Depends(require_admin_or_lab_manager)):
    with get_db() as db:
        student = db.execute("SELECT student_code, full_name FROM students WHERE id=?", (student_id,)).fetchone()
        row = db.execute("SELECT image_path FROM student_faces WHERE id=? AND student_id=?", (face_id, student_id)).fetchone()
        db.execute("DELETE FROM student_faces WHERE id=? AND student_id=?", (face_id, student_id))
        if row:
            write_audit_log(
                db,
                actor,
                "faces.delete",
                "student_face",
                face_id,
                f"{student['student_code']} - {student['full_name']}" if student else None,
                {"student_id": student_id},
                request,
            )
    if row and row["image_path"]:
        face_embedding_cache.invalidate()
        _delete_face_image_files([row["image_path"]])
    elif row:
        face_embedding_cache.invalidate()
    return {"ok": True}
