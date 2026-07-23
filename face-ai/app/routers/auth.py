from fastapi import APIRouter, Depends, Request, Response, HTTPException
from pydantic import BaseModel
from app.core.config import settings
from app.core.csrf import create_csrf_token, delete_csrf_cookie, set_csrf_cookie
from app.core.security import create_session_token, hash_password, parse_session_token, password_needs_rehash, verify_password
from app.db import get_db, row_to_dict
from app.routers.deps import current_user, user_from_request
from app.services.audit_service import write_audit_log
from app.services.login_rate_limit import (
    clear_login_failures,
    login_attempt_key,
    login_retry_after,
    record_login_failure,
)
from app.services.session_service import create_user_session, revoke_session

router = APIRouter(prefix="/api/auth", tags=["auth"])


class LoginRequest(BaseModel):
    username: str
    password: str


@router.post("/login")
def login(payload: LoginRequest, response: Response, request: Request):
    attempt_key = login_attempt_key(request, payload.username)
    with get_db() as db:
        retry_after = login_retry_after(db, attempt_key)
        if retry_after:
            write_audit_log(
                db,
                None,
                "auth.login_rate_limited",
                "user",
                entity_label=payload.username,
                details={"username": payload.username, "retry_after_seconds": retry_after},
                request=request,
            )
            raise HTTPException(
                status_code=429,
                detail="Dang nhap sai qua nhieu lan. Vui long thu lai sau.",
                headers={"Retry-After": str(retry_after)},
            )

    with get_db() as db:
        row = db.execute("SELECT * FROM users WHERE username = ?", (payload.username,),).fetchone()
    if not row or not verify_password(payload.password, row["password_hash"]):
        with get_db() as db:
            limit_state = record_login_failure(db, attempt_key)
            write_audit_log(
                db,
                None,
                "auth.login_failed",
                "user",
                entity_label=payload.username,
                details={
                    "username": payload.username,
                    "failed_attempts": limit_state["failed_attempts"],
                    "remaining_attempts": limit_state["remaining_attempts"],
                },
                request=request,
            )
        raise HTTPException(status_code=401, detail="Sai tài khoản hoặc mật khẩu.")
    if row["status"] != "active":
        with get_db() as db:
            write_audit_log(
                db,
                row_to_dict(row),
                "auth.login_blocked",
                "user",
                row["id"],
                row["username"],
                {"status": row["status"]},
                request,
            )
        raise HTTPException(status_code=403, detail="Tài khoản đã bị khóa.")
    with get_db() as db:
        if password_needs_rehash(row["password_hash"]):
            db.execute(
                "UPDATE users SET password_hash=? WHERE id=?",
                (hash_password(payload.password), row["id"]),
            )
        clear_login_failures(db, attempt_key)
        session_id = create_user_session(db, row["id"], request)
        write_audit_log(
            db,
            row_to_dict(row),
            "auth.login_success",
            "user",
            row["id"],
            row["username"],
            {"role": row["role"]},
            request,
        )
    token = create_session_token(row["id"], session_id)
    response.set_cookie(
        "session_token",
        token,
        httponly=True,
        samesite="lax",
        secure=settings.https_enabled,
        max_age=settings.session_max_age_seconds,
    )
    set_csrf_cookie(response, create_csrf_token(), settings.session_max_age_seconds)
    return {"ok": True, "user": {"id": row["id"], "username": row["username"], "role": row["role"]}}


@router.post("/logout")
def logout(response: Response, request: Request):
    user = user_from_request(request)
    token_data = parse_session_token(request.cookies.get("session_token", ""))
    with get_db() as db:
        if token_data:
            revoke_session(db, token_data["session_id"])
        if user:
            write_audit_log(db, user, "auth.logout", "user", user["id"], user["username"], request=request)
    response.delete_cookie("session_token")
    delete_csrf_cookie(response)
    return {"ok": True}


@router.get("/me")
def me(user=Depends(current_user)):
    return {"ok": True, "user": user}
