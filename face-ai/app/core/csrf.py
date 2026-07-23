import hmac
import secrets

from fastapi import Request
from fastapi.responses import JSONResponse
from app.core.config import settings


CSRF_COOKIE_NAME = "csrf_token"
CSRF_HEADER_NAME = "X-CSRF-Token"
SAFE_METHODS = {"GET", "HEAD", "OPTIONS", "TRACE"}
EXEMPT_PATHS = {"/api/auth/login"}


def create_csrf_token() -> str:
    return secrets.token_urlsafe(32)


def set_csrf_cookie(response, token: str, max_age: int) -> None:
    response.set_cookie(
        CSRF_COOKIE_NAME,
        token,
        httponly=False,
        samesite="lax",
        secure=settings.https_enabled,
        max_age=max_age,
    )


def delete_csrf_cookie(response) -> None:
    response.delete_cookie(CSRF_COOKIE_NAME)


async def csrf_protect_middleware(request: Request, call_next):
    if should_check_csrf(request):
        cookie_token = request.cookies.get(CSRF_COOKIE_NAME)
        header_token = request.headers.get(CSRF_HEADER_NAME)
        if not valid_csrf_tokens(cookie_token, header_token):
            return JSONResponse(
                {"detail": "CSRF token khong hop le hoac bi thieu."},
                status_code=403,
            )
    return await call_next(request)


def should_check_csrf(request: Request) -> bool:
    if request.method.upper() in SAFE_METHODS:
        return False
    if request.url.path in EXEMPT_PATHS:
        return False
    return bool(request.cookies.get("session_token"))


def valid_csrf_tokens(cookie_token: str | None, header_token: str | None) -> bool:
    if not cookie_token or not header_token:
        return False
    return hmac.compare_digest(cookie_token, header_token)
