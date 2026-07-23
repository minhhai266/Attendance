import base64
import hashlib
import hmac
import os
import time
from typing import Optional
from app.core.config import settings


PASSWORD_HASH_ALGORITHM = "pbkdf2_sha256"
LEGACY_PBKDF2_ITERATIONS = 120_000
PASSWORD_SALT_BYTES = 16
PASSWORD_DIGEST_BYTES = 32


def _b64encode(data: bytes) -> str:
    return base64.b64encode(data).decode("utf-8")


def _b64decode(text: str) -> bytes:
    return base64.b64decode(text.encode("utf-8"), validate=True)


def hash_password(password: str, salt: Optional[bytes] = None) -> str:
    salt = salt or os.urandom(PASSWORD_SALT_BYTES)
    iterations = int(settings.password_pbkdf2_iterations)
    digest = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, iterations)
    return f"{PASSWORD_HASH_ALGORITHM}${iterations}${_b64encode(salt)}${_b64encode(digest)}"


def verify_password(password: str, stored_hash: str) -> bool:
    parsed = _parse_password_hash(stored_hash)
    if not parsed:
        return False
    _, iterations, salt, old_digest = parsed
    new_digest = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, iterations)
    return hmac.compare_digest(old_digest, new_digest)


def password_needs_rehash(stored_hash: str) -> bool:
    parsed = _parse_password_hash(stored_hash)
    if not parsed:
        return True
    algorithm, iterations, _, _ = parsed
    return algorithm != PASSWORD_HASH_ALGORITHM or iterations < int(settings.password_pbkdf2_iterations)


def _parse_password_hash(stored_hash: str) -> tuple[str, int, bytes, bytes] | None:
    if not stored_hash:
        return None
    if stored_hash.startswith(f"{PASSWORD_HASH_ALGORITHM}$"):
        return _parse_versioned_password_hash(stored_hash)
    return _parse_legacy_password_hash(stored_hash)


def _parse_versioned_password_hash(stored_hash: str) -> tuple[str, int, bytes, bytes] | None:
    try:
        algorithm, iterations_text, salt_text, digest_text = stored_hash.split("$", 3)
        if algorithm != PASSWORD_HASH_ALGORITHM:
            return None
        iterations = int(iterations_text)
        if iterations <= 0:
            return None
        salt = _b64decode(salt_text)
        digest = _b64decode(digest_text)
        if len(salt) != PASSWORD_SALT_BYTES or len(digest) != PASSWORD_DIGEST_BYTES:
            return None
        return algorithm, iterations, salt, digest
    except Exception:
        return None


def _parse_legacy_password_hash(stored_hash: str) -> tuple[str, int, bytes, bytes] | None:
    try:
        raw = _b64decode(stored_hash)
        if len(raw) != PASSWORD_SALT_BYTES + PASSWORD_DIGEST_BYTES:
            return None
        salt, old_digest = raw[:PASSWORD_SALT_BYTES], raw[PASSWORD_SALT_BYTES:]
        return "legacy_pbkdf2_sha256", LEGACY_PBKDF2_ITERATIONS, salt, old_digest
    except Exception:
        return None


def create_session_token(user_id: int, session_id: str) -> str:
    payload = f"{user_id}:{session_id}:{int(time.time())}"
    signature = hmac.new(settings.secret_key.encode(), payload.encode(), hashlib.sha256).hexdigest()
    return base64.urlsafe_b64encode(f"{payload}:{signature}".encode()).decode()


def parse_session_token(token: str) -> Optional[dict]:
    try:
        decoded = base64.urlsafe_b64decode(token.encode()).decode()
        user_id, session_id, issued_at, signature = decoded.split(":", 3)
        payload = f"{user_id}:{session_id}:{issued_at}"
        expected = hmac.new(settings.secret_key.encode(), payload.encode(), hashlib.sha256).hexdigest()
        if not hmac.compare_digest(signature, expected):
            return None
        issued_at_int = int(issued_at)
        if time.time() - issued_at_int > settings.session_max_age_seconds:
            return None
        return {
            "user_id": int(user_id),
            "session_id": session_id,
            "issued_at": issued_at_int,
        }
    except Exception:
        return None
