from fastapi import Cookie, HTTPException, Request

from app.core.security import parse_session_token
from app.db import get_db, row_to_dict
from app.services.session_service import active_session_row


ADMIN_ROLE = "admin"
LAB_MANAGER_ROLE = "lab_manager"
STUDENT_ROLE = "student"
STAFF_ROLES = {ADMIN_ROLE, LAB_MANAGER_ROLE}
ALL_ROLES = {ADMIN_ROLE, LAB_MANAGER_ROLE, STUDENT_ROLE}


ROLE_LABELS = {
    ADMIN_ROLE: "Admin",
    LAB_MANAGER_ROLE: "Quản lý phòng lab",
    STUDENT_ROLE: "Sinh viên",
}


def user_from_session_token(session_token: str | None):
    if not session_token:
        return None
    data = parse_session_token(session_token)
    if not data:
        return None

    with get_db() as db:
        session = active_session_row(db, data["session_id"], data["user_id"])
        if not session:
            return None
        user = db.execute(
            """
            SELECT id, username, role, student_id, status
            FROM users
            WHERE id = ?
            """,
            (data["user_id"],),
        ).fetchone()

    if not user:
        return None
    return row_to_dict(user)


def current_user(session_token: str | None = Cookie(default=None)):
    if not session_token:
        raise HTTPException(status_code=401, detail="Chưa đăng nhập.")
    user = user_from_session_token(session_token)
    if not user:
        raise HTTPException(status_code=401, detail="Phiên đăng nhập không hợp lệ, đã hết hạn hoặc đã bị thu hồi.")
    if user.get("status") != "active":
        raise HTTPException(status_code=403, detail="Tài khoản đã bị khóa.")
    return user


def require_roles(*roles: str):
    def checker(session_token: str | None = Cookie(default=None)):
        user = current_user(session_token)
        if user.get("role") not in roles:
            raise HTTPException(status_code=403, detail="Bạn không có quyền thực hiện chức năng này.")
        return user
    return checker


def require_admin(session_token: str | None = Cookie(default=None)):
    user = current_user(session_token)
    if user.get("role") != ADMIN_ROLE:
        raise HTTPException(status_code=403, detail="Chỉ admin được dùng chức năng này.")
    return user


def require_lab_manager(session_token: str | None = Cookie(default=None)):
    user = current_user(session_token)
    if user.get("role") != LAB_MANAGER_ROLE:
        raise HTTPException(status_code=403, detail="Chỉ quản lý phòng lab được dùng chức năng này.")
    return user


def require_admin_or_lab_manager(session_token: str | None = Cookie(default=None)):
    user = current_user(session_token)
    if user.get("role") not in STAFF_ROLES:
        raise HTTPException(status_code=403, detail="Chỉ admin hoặc quản lý phòng lab được dùng chức năng này.")
    return user


def require_student(session_token: str | None = Cookie(default=None)):
    user = current_user(session_token)
    if user.get("role") != STUDENT_ROLE:
        raise HTTPException(status_code=403, detail="Chỉ sinh viên được dùng chức năng này.")
    if not user.get("student_id"):
        raise HTTPException(status_code=403, detail="Tài khoản sinh viên chưa liên kết hồ sơ sinh viên.")
    return user


def user_from_request(request: Request):
    user = user_from_session_token(request.cookies.get("session_token"))
    if not user or user.get("status") != "active":
        return None
    return user
