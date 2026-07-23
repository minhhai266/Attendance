import json
from datetime import datetime


def write_audit_log(
    db,
    actor: dict | None,
    action: str,
    entity_type: str,
    entity_id: int | str | None = None,
    entity_label: str | None = None,
    details: dict | None = None,
    request=None,
) -> None:
    actor = actor or {}
    db.execute(
        """
        INSERT INTO audit_logs(
            actor_user_id, actor_username, actor_role, action, entity_type,
            entity_id, entity_label, details_json, ip_address, user_agent, created_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            actor.get("id"),
            actor.get("username"),
            actor.get("role"),
            action,
            entity_type,
            str(entity_id) if entity_id is not None else None,
            entity_label,
            json.dumps(details or {}, ensure_ascii=False, sort_keys=True),
            _client_ip(request),
            request.headers.get("user-agent") if request is not None else None,
            datetime.now().isoformat(timespec="seconds"),
        ),
    )


def audit_diff(before: dict, after: dict, keys: list[str]) -> dict:
    diff = {}
    for key in keys:
        old = before.get(key)
        new = after.get(key)
        if old != new:
            diff[key] = {"old": old, "new": new}
    return diff


def _client_ip(request) -> str | None:
    if request is None:
        return None
    forwarded_for = request.headers.get("x-forwarded-for")
    if forwarded_for:
        return forwarded_for.split(",", 1)[0].strip()
    return request.client.host if request.client else None
