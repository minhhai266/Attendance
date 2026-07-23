import os
from pathlib import Path
from uuid import uuid4

import cv2
from fastapi import HTTPException, UploadFile, status

from app.services.face_service import face_service
from app.services.private_storage import PRIVATE_FACE_DIR, face_relative_path, resolve_private_file

FACE_SCAN_REQUIRED_FILES = 5
MAX_FACE_EMBEDDINGS_PER_STUDENT = 10
MAX_FACE_UPLOAD_BYTES = 5 * 1024 * 1024
FACE_UPLOAD_READ_CHUNK = 1024 * 1024
DUPLICATE_FACE_THRESHOLD = 0.75
ALLOWED_FACE_CONTENT_TYPES = {"image/jpeg", "image/png"}
FACE_UPLOAD_DIR = str(PRIVATE_FACE_DIR)


async def read_limited_image_upload(file: UploadFile, label: str) -> bytes:
    content_type = (file.content_type or "").split(";", 1)[0].strip().lower()
    if content_type not in ALLOWED_FACE_CONTENT_TYPES:
        raise HTTPException(status_code=400, detail=f"{label}: chỉ chấp nhận file JPG hoặc PNG.")

    chunks = []
    total = 0
    while True:
        chunk = await file.read(FACE_UPLOAD_READ_CHUNK)
        if not chunk:
            break
        total += len(chunk)
        if total > MAX_FACE_UPLOAD_BYTES:
            raise HTTPException(status_code=400, detail=f"{label}: file vượt quá 5MB.")
        chunks.append(chunk)

    data = b"".join(chunks)
    if not data:
        raise HTTPException(status_code=400, detail=f"{label}: file rỗng.")
    actual_content_type = _image_signature_content_type(data)
    if actual_content_type is None:
        raise HTTPException(status_code=400, detail=f"{label}: nội dung file không phải JPG/PNG hợp lệ.")
    if actual_content_type != content_type:
        raise HTTPException(status_code=400, detail=f"{label}: MIME type không khớp với nội dung file.")
    return data


def prepare_face_upload(data: bytes, label: str) -> dict:
    try:
        image = face_service.read_image_from_bytes(data)
        embedding, bbox = face_service.extract_single_embedding(image)
        quality = face_service.evaluate_liveness_quality(image, bbox)
    except Exception as exc:
        raise HTTPException(status_code=400, detail=f"{label}: {exc}")
    if not quality.get("ok"):
        message = quality.get("message") or "Ảnh chưa đủ chất lượng để đăng ký khuôn mặt."
        raise HTTPException(status_code=400, detail=f"{label}: {message}")

    ok, encoded = cv2.imencode(".jpg", image, [int(cv2.IMWRITE_JPEG_QUALITY), 92])
    if not ok:
        raise HTTPException(status_code=400, detail=f"{label}: không chuẩn hóa được ảnh.")
    return {
        "data": encoded.tobytes(),
        "embedding": embedding,
        "bbox": bbox,
        "quality": quality,
    }


def save_face_image(student, data: bytes) -> str:
    os.makedirs(FACE_UPLOAD_DIR, exist_ok=True)
    safe_code = student["student_code"].replace("/", "_").replace("\\", "_")
    filename = f"{safe_code}_{uuid4().hex}.jpg"
    path = os.path.join(FACE_UPLOAD_DIR, filename)
    with open(path, "wb") as file_obj:
        file_obj.write(data)
    return face_relative_path(filename)


def delete_face_image_files(image_paths: list[str]) -> None:
    for image_path in image_paths:
        target = _face_image_disk_path(image_path)
        if target is None:
            continue
        try:
            target.unlink()
        except (FileNotFoundError, OSError):
            pass


def delete_student_faces(db, student_id: int) -> list[str]:
    face_rows = db.execute("SELECT image_path FROM student_faces WHERE student_id=?", (student_id,)).fetchall()
    image_paths = [row["image_path"] for row in face_rows if row["image_path"]]
    db.execute("DELETE FROM student_faces WHERE student_id=?", (student_id,))
    return image_paths


def trim_old_face_embeddings(
    db,
    student_id: int,
    max_count: int = MAX_FACE_EMBEDDINGS_PER_STUDENT,
) -> tuple[int, list[str]]:
    count = db.execute("SELECT COUNT(*) c FROM student_faces WHERE student_id = ?", (student_id,)).fetchone()["c"]
    overflow = max(count - max_count, 0)
    if overflow:
        rows = db.execute(
            """
            SELECT id, image_path FROM student_faces
            WHERE student_id = ?
            ORDER BY created_at ASC, id ASC
            LIMIT ?
            """,
            (student_id, overflow),
        ).fetchall()
        ids = [row["id"] for row in rows]
        image_paths = [row["image_path"] for row in rows if row["image_path"]]
        db.execute(
            f"DELETE FROM student_faces WHERE id IN ({','.join('?' for _ in ids)})",
            ids,
        )
        return max_count, image_paths
    return count, []


def duplicate_warnings(db, student_id: int, embeddings: list) -> list[dict]:
    rows = db.execute(
        """
        SELECT f.embedding, s.id student_id, s.student_code, s.full_name
        FROM student_faces f
        JOIN students s ON s.id = f.student_id
        WHERE s.status='active' AND s.id != ?
        """,
        (student_id,),
    ).fetchall()
    warnings = {}
    for embedding in embeddings:
        for row in rows:
            known_embedding = face_service.deserialize_embedding(row["embedding"])
            score = face_service.cosine_similarity(embedding, known_embedding)
            if score < DUPLICATE_FACE_THRESHOLD:
                continue
            key = row["student_id"]
            current = warnings.get(key)
            if current is None or score > current["score"]:
                warnings[key] = {
                    "student_id": row["student_id"],
                    "student_code": row["student_code"],
                    "full_name": row["full_name"],
                    "score": round(float(score), 4),
                    "threshold": DUPLICATE_FACE_THRESHOLD,
                }
    return sorted(warnings.values(), key=lambda item: item["score"], reverse=True)


def raise_if_duplicate_face(duplicate_matches: list[dict]) -> None:
    if not duplicate_matches:
        return
    raise HTTPException(
        status_code=status.HTTP_409_CONFLICT,
        detail={
            "message": "Khuôn mặt bị trùng với sinh viên đã đăng ký.",
            "duplicate_warnings": duplicate_matches,
        },
    )


def _face_image_disk_path(image_path: str | None) -> Path | None:
    return resolve_private_file(image_path, "face")


def _image_signature_content_type(data: bytes) -> str | None:
    if data.startswith(b"\xff\xd8\xff"):
        return "image/jpeg"
    if data.startswith(b"\x89PNG\r\n\x1a\n"):
        return "image/png"
    return None
