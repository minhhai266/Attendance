import asyncio
from contextlib import asynccontextmanager, suppress
import sys
from pathlib import Path

if __package__ in {None, ""}:
    sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from fastapi import FastAPI, Request
from fastapi.responses import FileResponse, RedirectResponse
from fastapi.staticfiles import StaticFiles
from starlette.middleware.trustedhost import TrustedHostMiddleware
from app.core.csrf import csrf_protect_middleware
from app.core.config import settings
from app.db import get_setting, init_db
from app.routers import (
    access_logs,
    alerts,
    attendance,
    audit_logs,
    auth,
    dashboard,
    files,
    realtime,
    settings as settings_router,
    student_portal,
    students,
    users,
)
from app.routers.deps import ADMIN_ROLE, LAB_MANAGER_ROLE, STUDENT_ROLE, user_from_request
from app.services.attendance_service import mark_missing_checkouts
from app.services.face_service import face_service
from app.services.liveness_service import liveness_service


async def missing_checkout_scheduler():
    while True:
        try:
            created = mark_missing_checkouts()
            if created:
                print(f"Created {created} missing checkout alert(s).")
            interval = int(get_setting("missing_checkout_scan_interval_seconds", settings.missing_checkout_scan_interval_seconds))
        except Exception as exc:
            print(f"Missing checkout scheduler error: {exc}")
            interval = settings.missing_checkout_scan_interval_seconds
        await asyncio.sleep(max(30, interval))


@asynccontextmanager
async def lifespan(app: FastAPI):
    init_db()
    created = mark_missing_checkouts()
    if created:
        print(f"Created {created} missing checkout alert(s) on startup.")
    missing_checkout_task = asyncio.create_task(missing_checkout_scheduler())
    face_service.load()
    liveness_service.load()
    try:
        yield
    finally:
        missing_checkout_task.cancel()
        with suppress(asyncio.CancelledError):
            await missing_checkout_task


def _csv_setting(value: str) -> list[str]:
    return [item.strip() for item in value.split(",") if item.strip()]


def _docs_url(path: str) -> str | None:
    return path if settings.public_docs_enabled else None


SECURITY_HEADERS = {
    "Content-Security-Policy": (
        "default-src 'self'; "
        "script-src 'self' 'unsafe-inline'; "
        "style-src 'self' 'unsafe-inline'; "
        "img-src 'self' data: blob:; "
        "media-src 'self' blob:; "
        "connect-src 'self' ws: wss:; "
        "object-src 'none'; "
        "base-uri 'self'; "
        "frame-ancestors 'none'; "
        "form-action 'self'"
    ),
    "X-Frame-Options": "DENY",
    "X-Content-Type-Options": "nosniff",
    "Referrer-Policy": "strict-origin-when-cross-origin",
    "Permissions-Policy": "camera=(self), microphone=(), geolocation=()",
}


async def security_headers_middleware(request: Request, call_next):
    response = await call_next(request)
    for header, value in SECURITY_HEADERS.items():
        response.headers.setdefault(header, value)
    if settings.https_enabled:
        response.headers.setdefault("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
    return response


app = FastAPI(
    title=settings.app_name,
    version="1.0.0",
    lifespan=lifespan,
    docs_url=_docs_url("/docs"),
    redoc_url=_docs_url("/redoc"),
    openapi_url=_docs_url("/openapi.json"),
)
trusted_hosts = _csv_setting(settings.trusted_hosts)
if trusted_hosts:
    app.add_middleware(TrustedHostMiddleware, allowed_hosts=trusted_hosts)
app.middleware("http")(security_headers_middleware)
app.middleware("http")(csrf_protect_middleware)

app.mount("/static", StaticFiles(directory="web/static"), name="static")

app.include_router(auth.router)
app.include_router(students.router)
app.include_router(access_logs.router)
app.include_router(alerts.router)
app.include_router(attendance.router)
app.include_router(audit_logs.router)
app.include_router(dashboard.router)
app.include_router(files.router)
app.include_router(settings_router.router)
app.include_router(realtime.router)
app.include_router(users.router)
app.include_router(student_portal.router)


def no_cache_file(path: str):
    return FileResponse(
        path,
        headers={
            "Cache-Control": "no-store, no-cache, must-revalidate, max-age=0",
            "Pragma": "no-cache",
            "Expires": "0",
        },
    )


@app.get("/login")
def login_page():
    return no_cache_file("web/templates/login.html")


@app.get("/")
def root(request: Request):
    user = user_from_request(request)
    if not user:
        return RedirectResponse("/login")
    if user.get("role") == STUDENT_ROLE:
        return no_cache_file("web/templates/student_dashboard.html")
    if user.get("role") in {LAB_MANAGER_ROLE, ADMIN_ROLE}:
        return no_cache_file("web/templates/dashboard.html")
    return RedirectResponse("/login")


@app.get("/admin/users")
def users_page(request: Request):
    user = user_from_request(request)
    if not user:
        return RedirectResponse("/login")
    if user.get("role") not in {ADMIN_ROLE, LAB_MANAGER_ROLE}:
        return RedirectResponse("/")
    return RedirectResponse("/?page=accounts")


@app.get("/health")
def health():
    if not settings.health_details_enabled:
        return {"ok": True}
    return {
        "ok": True,
        "face_model_loaded": face_service.loaded,
        "face_model_error": face_service.load_error,
        "liveness_enabled": liveness_service.is_enabled(),
        "liveness_threshold": liveness_service.threshold(),
        "liveness_real_class_index": liveness_service.real_class_index(),
        "liveness_crop_scale": liveness_service.crop_scale(),
        "liveness_model_loaded": liveness_service.loaded,
        "liveness_model_error": liveness_service.load_error,
    }


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app.main:app", host="127.0.0.1", port=8002, reload=True)
