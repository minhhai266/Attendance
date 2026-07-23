from fastapi import APIRouter, Depends, Request
from pydantic import BaseModel, Field, field_validator
from typing import Literal
from app.core.time_utils import validate_time_text
from app.db import get_db, row_to_dict
from app.routers.deps import require_admin, require_admin_or_lab_manager
from app.services.audit_service import audit_diff, write_audit_log

router = APIRouter(prefix="/api/settings", tags=["settings"])


class SettingsUpdate(BaseModel):
    face_threshold: float = Field(ge=0, le=1)
    check_cooldown_seconds: int = Field(ge=1)
    frame_skip: int = Field(ge=1)
    camera_mode: Literal["check_in", "check_out"] | None = None
    check_in_camera_device_id: str | None = None
    check_out_camera_device_id: str | None = None
    liveness_enabled: bool | None = None
    liveness_threshold: float | None = Field(default=None, ge=0, le=1)
    liveness_real_class_index: int | None = Field(default=None, ge=0, le=2)
    liveness_crop_scale: float | None = Field(default=None, ge=1, le=3)
    liveness_min_face_size: int | None = Field(default=None, ge=32, le=512)
    liveness_min_brightness: float | None = Field(default=None, ge=0, le=255)
    liveness_min_blur: float | None = Field(default=None, ge=0, le=1000)
    missing_checkout_cutoff_time: str | None = Field(default=None, pattern=r"^\d{2}:\d{2}$")
    work_start_time: str | None = Field(default=None, pattern=r"^\d{2}:\d{2}$")
    work_end_time: str | None = Field(default=None, pattern=r"^\d{2}:\d{2}$")
    late_grace_minutes: int | None = Field(default=None, ge=0, le=240)
    early_leave_grace_minutes: int | None = Field(default=None, ge=0, le=240)

    @field_validator("missing_checkout_cutoff_time", "work_start_time", "work_end_time")
    @classmethod
    def validate_time_value(cls, value: str | None) -> str | None:
        return validate_time_text(value)


@router.get("", dependencies=[Depends(require_admin_or_lab_manager)])
def get_settings():
    with get_db() as db:
        rows = db.execute("SELECT key, value FROM settings").fetchall()
    return {r["key"]: r["value"] for r in rows}


@router.put("")
def update_settings(payload: SettingsUpdate, request: Request, actor=Depends(require_admin)):
    values = {
        "face_threshold": str(payload.face_threshold),
        "check_cooldown_seconds": str(payload.check_cooldown_seconds),
        "frame_skip": str(payload.frame_skip),
    }
    optional_values = {
        "camera_mode": payload.camera_mode,
        "check_in_camera_device_id": payload.check_in_camera_device_id,
        "check_out_camera_device_id": payload.check_out_camera_device_id,
        "liveness_enabled": "true" if payload.liveness_enabled else "false" if payload.liveness_enabled is not None else None,
        "liveness_threshold": str(payload.liveness_threshold) if payload.liveness_threshold is not None else None,
        "liveness_real_class_index": str(payload.liveness_real_class_index) if payload.liveness_real_class_index is not None else None,
        "liveness_crop_scale": str(payload.liveness_crop_scale) if payload.liveness_crop_scale is not None else None,
        "liveness_min_face_size": str(payload.liveness_min_face_size) if payload.liveness_min_face_size is not None else None,
        "liveness_min_brightness": str(payload.liveness_min_brightness) if payload.liveness_min_brightness is not None else None,
        "liveness_min_blur": str(payload.liveness_min_blur) if payload.liveness_min_blur is not None else None,
        "missing_checkout_cutoff_time": payload.missing_checkout_cutoff_time,
        "work_start_time": payload.work_start_time,
        "work_end_time": payload.work_end_time,
        "late_grace_minutes": str(payload.late_grace_minutes) if payload.late_grace_minutes is not None else None,
        "early_leave_grace_minutes": str(payload.early_leave_grace_minutes) if payload.early_leave_grace_minutes is not None else None,
    }
    values.update({key: value for key, value in optional_values.items() if value is not None})

    with get_db() as db:
        before = {row["key"]: row["value"] for row in db.execute("SELECT key, value FROM settings").fetchall()}
        for key, value in values.items():
            db.execute(
                "INSERT INTO settings(key, value) VALUES(?, ?) ON CONFLICT(key) DO UPDATE SET value=excluded.value",
                (key, value),
            )
        after = {**before, **values}
        changes = audit_diff(before, after, list(values.keys()))
        if changes:
            write_audit_log(
                db,
                actor,
                "settings.update",
                "settings",
                details={"changes": changes},
                request=request,
            )
    return {"ok": True}
