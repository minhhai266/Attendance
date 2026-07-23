from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException, Request
from pydantic import BaseModel, Field

from app.core.security import hash_password
from app.db import get_db, row_to_dict
from app.routers.deps import ADMIN_ROLE, LAB_MANAGER_ROLE, STUDENT_ROLE, require_admin_or_lab_manager
from app.services.audit_service import audit_diff, write_audit_log
from app.services.session_service import revoke_user_sessions

router = APIRouter(prefix="/api/users", tags=["users"])


class UserCreate(BaseModel):
    username: str = Field(min_length=3, max_length=64)
    password: str = Field(min_length=6, max_length=128)
    role: str = Field(pattern="^(admin|lab_manager|student)$")
    student_id: int | None = None


class UserUpdate(BaseModel):
    password: str | None = Field(default=None, min_length=6, max_length=128)
    status: str | None = Field(default=None, pattern="^(active|inactive)$")
    student_id: int | None = None


def _is_lab_manager(user: dict) -> bool:
    return user.get("role") == LAB_MANAGER_ROLE


def _validate_student_link(db, student_id: int | None) -> None:
    if not student_id:
        raise HTTPException(status_code=400, detail="Tài khoản sinh viên phải liên kết với hồ sơ sinh viên.")
    student = db.execute("SELECT id FROM students WHERE id=?", (student_id,)).fetchone()
    if not student:
        raise HTTPException(status_code=404, detail="Không tìm thấy sinh viên để liên kết.")


@router.get("")
def list_users(actor=Depends(require_admin_or_lab_manager)):
    where = "1=1"
    params = []
    if _is_lab_manager(actor):
        # Lab Manager chỉ xem/quản lý tài khoản sinh viên, không xem tài khoản admin khác.
        where = "u.role = ?"
        params.append(STUDENT_ROLE)
    with get_db() as db:
        rows = db.execute(
            f"""
            SELECT u.id, u.username, u.role, u.student_id, u.status, u.created_at,
                   s.student_code, s.full_name, s.class_name
            FROM users u
            LEFT JOIN students s ON s.id = u.student_id
            WHERE {where}
            ORDER BY u.id DESC
            """,
            params,
        ).fetchall()
    return {"items": [row_to_dict(r) for r in rows], "current_role": actor.get("role")}


@router.post("")
def create_user(payload: UserCreate, request: Request, actor=Depends(require_admin_or_lab_manager)):
    if _is_lab_manager(actor) and payload.role != STUDENT_ROLE:
        raise HTTPException(status_code=403, detail="Quản lý phòng lab chỉ được tạo tài khoản sinh viên.")
    if payload.role == STUDENT_ROLE and not payload.student_id:
        raise HTTPException(status_code=400, detail="Tài khoản sinh viên phải liên kết với hồ sơ sinh viên.")
    if payload.role in {ADMIN_ROLE, LAB_MANAGER_ROLE} and payload.student_id:
        raise HTTPException(status_code=400, detail="Tài khoản admin/lab_manager không cần student_id.")
    try:
        with get_db() as db:
            if payload.role == STUDENT_ROLE:
                _validate_student_link(db, payload.student_id)
            cur = db.execute(
                """
                INSERT INTO users(username, password_hash, role, student_id, status, created_at)
                VALUES (?, ?, ?, ?, 'active', ?)
                """,
                (
                    payload.username.strip(),
                    hash_password(payload.password),
                    payload.role,
                    payload.student_id if payload.role == STUDENT_ROLE else None,
                    datetime.now().isoformat(timespec="seconds"),
                ),
            )
            row = db.execute(
                "SELECT id, username, role, student_id, status, created_at FROM users WHERE id=?",
                (cur.lastrowid,),
            ).fetchone()
            write_audit_log(
                db,
                actor,
                "users.create",
                "user",
                row["id"],
                f"{row['username']} ({row['role']})",
                {
                    "username": row["username"],
                    "role": row["role"],
                    "student_id": row["student_id"],
                    "status": row["status"],
                },
                request,
            )
    except HTTPException:
        raise
    except Exception as exc:
        message = str(exc).lower()
        if "unique" in message:
            raise HTTPException(status_code=409, detail="Username hoặc sinh viên này đã có tài khoản.")
        raise
    return {"ok": True, "item": row_to_dict(row)}


@router.put("/{user_id}")
def update_user(user_id: int, payload: UserUpdate, request: Request, actor=Depends(require_admin_or_lab_manager)):
    try:
        with get_db() as db:
            current = db.execute("SELECT * FROM users WHERE id=?", (user_id,)).fetchone()
            if not current:
                raise HTTPException(status_code=404, detail="Không tìm thấy tài khoản.")
            if _is_lab_manager(actor) and current["role"] != STUDENT_ROLE:
                raise HTTPException(status_code=403, detail="Quản lý phòng lab chỉ được sửa tài khoản sinh viên.")
            if payload.student_id is not None:
                if current["role"] != STUDENT_ROLE:
                    raise HTTPException(status_code=400, detail="Chỉ tài khoản student mới được liên kết student_id.")
                _validate_student_link(db, payload.student_id)
                db.execute("UPDATE users SET student_id=? WHERE id=?", (payload.student_id, user_id))
            if payload.password:
                db.execute("UPDATE users SET password_hash=? WHERE id=?", (hash_password(payload.password), user_id))
            if payload.status:
                if current["role"] == ADMIN_ROLE and payload.status != "active":
                    active_admin_count = db.execute(
                        "SELECT COUNT(*) c FROM users WHERE role='admin' AND status='active' AND id<>?",
                        (user_id,),
                    ).fetchone()["c"]
                    if active_admin_count <= 0:
                        raise HTTPException(status_code=400, detail="Không được khóa admin active cuối cùng.")
                db.execute("UPDATE users SET status=? WHERE id=?", (payload.status, user_id))
            sessions_revoked = bool(payload.password or payload.status == "inactive")
            if sessions_revoked:
                revoke_user_sessions(db, user_id)
            row = db.execute(
                "SELECT id, username, role, student_id, status, created_at FROM users WHERE id=?",
                (user_id,),
            ).fetchone()
            before = row_to_dict(current)
            after = row_to_dict(row)
            changes = audit_diff(before, after, ["student_id", "status"])
            if payload.password:
                changes["password_changed"] = True
            if sessions_revoked:
                changes["sessions_revoked"] = True
            if changes:
                write_audit_log(
                    db,
                    actor,
                    "users.update",
                    "user",
                    row["id"],
                    f"{row['username']} ({row['role']})",
                    {"changes": changes},
                    request,
                )
    except HTTPException:
        raise
    except Exception as exc:
        if "unique" in str(exc).lower():
            raise HTTPException(status_code=409, detail="Sinh viên này đã có tài khoản khác.")
        raise
    return {"ok": True, "item": row_to_dict(row)}


@router.delete("/{user_id}")
def delete_user(user_id: int, request: Request, actor=Depends(require_admin_or_lab_manager)):
    with get_db() as db:
        current = db.execute("SELECT id, username, role, student_id, status FROM users WHERE id=?", (user_id,)).fetchone()
        if not current:
            raise HTTPException(status_code=404, detail="Không tìm thấy tài khoản.")
        if _is_lab_manager(actor) and current["role"] != STUDENT_ROLE:
            raise HTTPException(status_code=403, detail="Quản lý phòng lab chỉ được xóa tài khoản sinh viên.")
        admin_count = db.execute("SELECT COUNT(*) c FROM users WHERE role='admin' AND status='active'").fetchone()["c"]
        if current["role"] == ADMIN_ROLE and admin_count <= 1:
            raise HTTPException(status_code=400, detail="Không được xóa admin active cuối cùng.")
        revoke_user_sessions(db, user_id)
        db.execute("DELETE FROM users WHERE id=?", (user_id,))
        write_audit_log(
            db,
            actor,
            "users.delete",
            "user",
            current["id"],
            f"{current['username']} ({current['role']})",
            {
                "username": current["username"],
                "role": current["role"],
                "student_id": current["student_id"],
                "status": current["status"],
            },
            request,
        )
    return {"ok": True}
