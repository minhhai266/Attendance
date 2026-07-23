import secrets
import time

from app.core.config import settings


def create_user_session(db, user_id: int, request=None, now: int | None = None) -> str:
    now = int(time.time()) if now is None else now
    session_id = secrets.token_urlsafe(32)
    db.execute(
        """
        INSERT INTO user_sessions(
            session_id, user_id, created_at, expires_at, revoked_at,
            ip_address, user_agent
        )
        VALUES (?, ?, ?, ?, NULL, ?, ?)
        """,
        (
            session_id,
            user_id,
            now,
            now + settings.session_max_age_seconds,
            _client_ip(request),
            request.headers.get("user-agent") if request is not None else None,
        ),
    )
    return session_id


def revoke_session(db, session_id: str | None, now: int | None = None) -> None:
    if not session_id:
        return
    now = int(time.time()) if now is None else now
    db.execute(
        """
        UPDATE user_sessions
        SET revoked_at = COALESCE(revoked_at, ?)
        WHERE session_id = ?
        """,
        (now, session_id),
    )


def revoke_user_sessions(db, user_id: int, now: int | None = None) -> None:
    now = int(time.time()) if now is None else now
    db.execute(
        """
        UPDATE user_sessions
        SET revoked_at = COALESCE(revoked_at, ?)
        WHERE user_id = ? AND revoked_at IS NULL
        """,
        (now, user_id),
    )


def active_session_row(db, session_id: str, user_id: int, now: int | None = None):
    now = int(time.time()) if now is None else now
    return db.execute(
        """
        SELECT session_id, user_id, expires_at, revoked_at
        FROM user_sessions
        WHERE session_id = ?
          AND user_id = ?
          AND revoked_at IS NULL
          AND expires_at > ?
        """,
        (session_id, user_id, now),
    ).fetchone()


def _client_ip(request) -> str | None:
    if request is None:
        return None
    forwarded_for = request.headers.get("x-forwarded-for")
    if forwarded_for:
        return forwarded_for.split(",", 1)[0].strip()
    return request.client.host if request.client else None
