import hashlib
import time

from app.core.config import settings


def login_attempt_key(request, username: str) -> str:
    client_ip = request.client.host if request.client else "unknown"
    normalized_username = username.strip().casefold()
    raw_key = f"{client_ip}\0{normalized_username}".encode("utf-8")
    return hashlib.sha256(raw_key).hexdigest()


def login_retry_after(db, attempt_key: str, now: int | None = None) -> int:
    now = int(time.time()) if now is None else now
    row = db.execute(
        """
        SELECT first_failed_at, locked_until
        FROM login_rate_limits
        WHERE attempt_key = ?
        """,
        (attempt_key,),
    ).fetchone()
    if not row:
        return 0

    locked_until = row["locked_until"]
    if locked_until and locked_until > now:
        return locked_until - now

    window_start = now - settings.login_attempt_window_seconds
    if row["first_failed_at"] <= window_start or (locked_until and locked_until <= now):
        db.execute("DELETE FROM login_rate_limits WHERE attempt_key = ?", (attempt_key,))
    return 0


def record_login_failure(db, attempt_key: str, now: int | None = None) -> dict:
    now = int(time.time()) if now is None else now
    window_start = now - settings.login_attempt_window_seconds
    max_attempts = settings.login_max_failed_attempts
    lock_expires_at = now + settings.login_lockout_seconds

    db.execute(
        """
        INSERT INTO login_rate_limits(
            attempt_key, failed_attempts, first_failed_at, locked_until, updated_at
        )
        VALUES (?, 1, ?, NULL, ?)
        ON CONFLICT(attempt_key) DO UPDATE SET
            failed_attempts = CASE
                WHEN login_rate_limits.first_failed_at <= ?
                  OR (
                      login_rate_limits.locked_until IS NOT NULL
                      AND login_rate_limits.locked_until <= ?
                  )
                THEN 1
                ELSE login_rate_limits.failed_attempts + 1
            END,
            first_failed_at = CASE
                WHEN login_rate_limits.first_failed_at <= ?
                  OR (
                      login_rate_limits.locked_until IS NOT NULL
                      AND login_rate_limits.locked_until <= ?
                  )
                THEN ?
                ELSE login_rate_limits.first_failed_at
            END,
            locked_until = CASE
                WHEN login_rate_limits.first_failed_at <= ?
                  OR (
                      login_rate_limits.locked_until IS NOT NULL
                      AND login_rate_limits.locked_until <= ?
                  )
                THEN NULL
                WHEN login_rate_limits.failed_attempts + 1 >= ?
                THEN ?
                ELSE login_rate_limits.locked_until
            END,
            updated_at = ?
        """,
        (
            attempt_key,
            now,
            now,
            window_start,
            now,
            window_start,
            now,
            now,
            window_start,
            now,
            max_attempts,
            lock_expires_at,
            now,
        ),
    )
    row = db.execute(
        """
        SELECT failed_attempts, locked_until
        FROM login_rate_limits
        WHERE attempt_key = ?
        """,
        (attempt_key,),
    ).fetchone()
    failed_attempts = row["failed_attempts"]
    return {
        "failed_attempts": failed_attempts,
        "remaining_attempts": max(0, max_attempts - failed_attempts),
        "locked_until": row["locked_until"],
    }


def clear_login_failures(db, attempt_key: str) -> None:
    db.execute("DELETE FROM login_rate_limits WHERE attempt_key = ?", (attempt_key,))
