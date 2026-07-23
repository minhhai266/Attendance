import json
import os
import re
import sqlite3
from contextlib import contextmanager
from datetime import datetime
from app.core.config import settings
from app.core.security import hash_password


os.makedirs(os.path.dirname(settings.database_path), exist_ok=True)


@contextmanager
def get_db():
    conn = sqlite3.connect(settings.database_path)
    conn.execute("PRAGMA foreign_keys = ON")
    conn.execute("PRAGMA busy_timeout = 5000")
    conn.row_factory = sqlite3.Row
    try:
        yield conn
        conn.commit()
    finally:
        conn.close()


def init_db():
    with get_db() as db:
        db.execute("PRAGMA journal_mode = WAL")
        db.executescript(
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'admin',
                student_id INTEGER,
                status TEXT NOT NULL DEFAULT 'active',
                created_at TEXT NOT NULL,
                FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE SET NULL
            );

            CREATE TABLE IF NOT EXISTS students (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_code TEXT NOT NULL UNIQUE,
                full_name TEXT NOT NULL,
                class_name TEXT,
                status TEXT NOT NULL DEFAULT 'active',
                created_at TEXT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS student_faces (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                image_path TEXT,
                embedding TEXT NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE
            );

            CREATE TABLE IF NOT EXISTS student_attendance_settings (
                student_id INTEGER PRIMARY KEY,
                work_start_time TEXT NOT NULL,
                work_end_time TEXT NOT NULL,
                updated_at TEXT NOT NULL,
                FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE
            );

            CREATE TABLE IF NOT EXISTS access_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER,
                student_code TEXT,
                full_name TEXT,
                action TEXT NOT NULL,
                result TEXT NOT NULL,
                confidence REAL,
                note TEXT,
                evidence_image_path TEXT,
                created_at TEXT NOT NULL,
                FOREIGN KEY(student_id) REFERENCES students(id)
            );

            CREATE TABLE IF NOT EXISTS attendance_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                student_code TEXT,
                full_name TEXT,
                attendance_date TEXT NOT NULL,
                first_check_in_at TEXT,
                last_check_out_at TEXT,
                status TEXT NOT NULL DEFAULT 'pending',
                late_minutes INTEGER NOT NULL DEFAULT 0,
                early_leave_minutes INTEGER NOT NULL DEFAULT 0,
                total_minutes INTEGER NOT NULL DEFAULT 0,
                missing_checkout INTEGER NOT NULL DEFAULT 0,
                note TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL,
                UNIQUE(student_id, attendance_date),
                FOREIGN KEY(student_id) REFERENCES students(id)
            );

            CREATE TABLE IF NOT EXISTS alerts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                message TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'new',
                evidence_image_path TEXT,
                created_at TEXT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS audit_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                actor_user_id INTEGER,
                actor_username TEXT,
                actor_role TEXT,
                action TEXT NOT NULL,
                entity_type TEXT NOT NULL,
                entity_id TEXT,
                entity_label TEXT,
                details_json TEXT,
                ip_address TEXT,
                user_agent TEXT,
                created_at TEXT NOT NULL,
                FOREIGN KEY(actor_user_id) REFERENCES users(id) ON DELETE SET NULL
            );

            CREATE TABLE IF NOT EXISTS login_rate_limits (
                attempt_key TEXT PRIMARY KEY,
                failed_attempts INTEGER NOT NULL,
                first_failed_at INTEGER NOT NULL,
                locked_until INTEGER,
                updated_at INTEGER NOT NULL
            );

            CREATE TABLE IF NOT EXISTS user_sessions (
                session_id TEXT PRIMARY KEY,
                user_id INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                expires_at INTEGER NOT NULL,
                revoked_at INTEGER,
                ip_address TEXT,
                user_agent TEXT,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
            );
            """
        )
        _migrate_students_remove_contact_columns(db)
        user_columns = {
            row["name"]
            for row in db.execute("PRAGMA table_info(users)").fetchall()
        }
        if "student_id" not in user_columns:
            db.execute("ALTER TABLE users ADD COLUMN student_id INTEGER")
        if "status" not in user_columns:
            db.execute("ALTER TABLE users ADD COLUMN status TEXT NOT NULL DEFAULT 'active'")
        db.execute("CREATE INDEX IF NOT EXISTS idx_users_role ON users(role)")
        db.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_users_student_id_unique ON users(student_id) WHERE student_id IS NOT NULL")
        db.execute("CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id)")
        db.execute("CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions(expires_at)")
        access_log_columns = {
            row["name"]
            for row in db.execute("PRAGMA table_info(access_logs)").fetchall()
        }
        if "evidence_image_path" not in access_log_columns:
            db.execute("ALTER TABLE access_logs ADD COLUMN evidence_image_path TEXT")
        alert_columns = {
            row["name"]
            for row in db.execute("PRAGMA table_info(alerts)").fetchall()
        }
        if "evidence_image_path" not in alert_columns:
            db.execute("ALTER TABLE alerts ADD COLUMN evidence_image_path TEXT")
        if "event_date" not in alert_columns:
            db.execute("ALTER TABLE alerts ADD COLUMN event_date TEXT")
        _backfill_alert_event_dates(db)
        from app.services.private_storage import migrate_public_uploads_to_private

        migrate_public_uploads_to_private(db)
        attendance_columns = {
            row["name"]
            for row in db.execute("PRAGMA table_info(attendance_records)").fetchall()
        }
        if "missing_checkout_resolution" not in attendance_columns:
            db.execute("ALTER TABLE attendance_records ADD COLUMN missing_checkout_resolution TEXT")
        if "resolution_reason" not in attendance_columns:
            db.execute("ALTER TABLE attendance_records ADD COLUMN resolution_reason TEXT")
        if "resolution_checkout_at" not in attendance_columns:
            db.execute("ALTER TABLE attendance_records ADD COLUMN resolution_checkout_at TEXT")
        if "force_zero_minutes" not in attendance_columns:
            db.execute("ALTER TABLE attendance_records ADD COLUMN force_zero_minutes INTEGER NOT NULL DEFAULT 0")
        db.execute("CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at)")
        db.execute("CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action)")
        db.execute("CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_type ON audit_logs(entity_type)")
        db.execute("CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_username ON audit_logs(actor_username)")
        admin = db.execute("SELECT id FROM users WHERE username = ?", (settings.default_admin_username,)).fetchone()
        if not admin:
            db.execute(
                "INSERT INTO users(username, password_hash, role, created_at) VALUES (?, ?, 'admin', ?)",
                (settings.default_admin_username, hash_password(settings.default_admin_password), datetime.now().isoformat(timespec="seconds")),
            )
        default_settings = {
            "face_threshold": str(settings.face_threshold),
            "check_cooldown_seconds": str(settings.check_cooldown_seconds),
            "frame_skip": str(settings.frame_skip),
            "camera_mode": "check_in",
            "check_in_camera_device_id": settings.check_in_camera_device_id,
            "check_out_camera_device_id": settings.check_out_camera_device_id,
            "liveness_enabled": "true" if settings.liveness_enabled else "false",
            "liveness_threshold": str(settings.liveness_threshold),
            "liveness_real_class_index": str(settings.liveness_real_class_index),
            "liveness_crop_scale": str(settings.liveness_crop_scale),
            "liveness_min_face_size": str(settings.liveness_min_face_size),
            "liveness_min_brightness": str(settings.liveness_min_brightness),
            "liveness_min_blur": str(settings.liveness_min_blur),
            "liveness_edge_margin": str(settings.liveness_edge_margin),
            "missing_checkout_cutoff_time": settings.missing_checkout_cutoff_time,
            "missing_checkout_scan_interval_seconds": str(settings.missing_checkout_scan_interval_seconds),
            "work_start_time": settings.work_start_time,
            "work_end_time": settings.work_end_time,
            "late_grace_minutes": str(settings.late_grace_minutes),
            "early_leave_grace_minutes": str(settings.early_leave_grace_minutes),
        }
        for key, value in default_settings.items():
            db.execute("INSERT OR IGNORE INTO settings(key, value) VALUES (?, ?)", (key, value))


def _backfill_alert_event_dates(db):
    rows = db.execute(
        "SELECT id, message, created_at FROM alerts WHERE event_date IS NULL OR event_date=''"
    ).fetchall()
    for row in rows:
        event_date = None
        match = re.search(r"ngày\s+(\d{2})/(\d{2})/(\d{4})", row["message"] or "", re.IGNORECASE)
        if match:
            day, month, year = match.groups()
            event_date = f"{year}-{month}-{day}"
        elif row["created_at"]:
            event_date = row["created_at"][:10]
        if event_date:
            db.execute("UPDATE alerts SET event_date=? WHERE id=?", (event_date, row["id"]))


def _migrate_students_remove_contact_columns(db):
    student_columns = {
        row["name"]
        for row in db.execute("PRAGMA table_info(students)").fetchall()
    }
    if "email" not in student_columns and "phone" not in student_columns:
        return

    db.execute("PRAGMA foreign_keys=OFF")
    db.executescript(
        """
        DROP TABLE IF EXISTS students_without_contact;
        CREATE TABLE students_without_contact (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_code TEXT NOT NULL UNIQUE,
            full_name TEXT NOT NULL,
            class_name TEXT,
            status TEXT NOT NULL DEFAULT 'active',
            created_at TEXT NOT NULL
        );
        INSERT INTO students_without_contact(id, student_code, full_name, class_name, status, created_at)
        SELECT id, student_code, full_name, class_name, status, created_at
        FROM students;
        DROP TABLE students;
        ALTER TABLE students_without_contact RENAME TO students;
        """
    )
    db.execute("PRAGMA foreign_keys=ON")


def row_to_dict(row):
    return dict(row) if row is not None else None


def get_setting(key: str, default=None):
    with get_db() as db:
        row = db.execute("SELECT value FROM settings WHERE key = ?", (key,)).fetchone()
        return row["value"] if row else default


def set_setting(key: str, value: str):
    with get_db() as db:
        db.execute("INSERT INTO settings(key, value) VALUES(?, ?) ON CONFLICT(key) DO UPDATE SET value=excluded.value", (key, value))
