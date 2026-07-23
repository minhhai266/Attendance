import os
import shutil
from pathlib import Path


PRIVATE_STORAGE_ROOT = Path("storage/private")
PRIVATE_FACE_DIR = PRIVATE_STORAGE_ROOT / "faces"
PRIVATE_EVIDENCE_DIR = PRIVATE_STORAGE_ROOT / "evidence"
PUBLIC_STATIC_ROOT = Path("web/static")


def ensure_private_storage() -> None:
    PRIVATE_FACE_DIR.mkdir(parents=True, exist_ok=True)
    PRIVATE_EVIDENCE_DIR.mkdir(parents=True, exist_ok=True)


def face_relative_path(filename: str) -> str:
    return f"faces/{Path(filename).name}"


def evidence_relative_path(day: str, filename: str) -> str:
    return f"evidence/{Path(day).name}/{Path(filename).name}"


def resolve_private_file(stored_path: str | None, kind: str) -> Path | None:
    relative_path = normalize_private_path(stored_path, kind)
    if not relative_path:
        return None

    root = PRIVATE_STORAGE_ROOT.resolve()
    target = (root / relative_path).resolve()
    if root != target and root not in target.parents:
        return None
    if target.is_file():
        return target

    legacy = legacy_public_file(stored_path, kind)
    if legacy and legacy.is_file():
        return legacy
    return None


def normalize_private_path(stored_path: str | None, kind: str) -> str | None:
    if not stored_path:
        return None

    value = stored_path.replace("\\", "/").lstrip("/")
    if value.startswith("storage/private/"):
        value = value.removeprefix("storage/private/")
    elif value.startswith("static/uploads/faces/"):
        value = "faces/" + value.removeprefix("static/uploads/faces/")
    elif value.startswith("web/static/uploads/faces/"):
        value = "faces/" + value.removeprefix("web/static/uploads/faces/")
    elif value.startswith("static/uploads/evidence/"):
        value = "evidence/" + value.removeprefix("static/uploads/evidence/")
    elif value.startswith("web/static/uploads/evidence/"):
        value = "evidence/" + value.removeprefix("web/static/uploads/evidence/")

    expected_prefix = "faces/" if kind == "face" else "evidence/"
    if not value.startswith(expected_prefix):
        return None

    parts = Path(value).parts
    if any(part in {"", ".", ".."} for part in parts):
        return None
    return value


def legacy_public_file(stored_path: str | None, kind: str) -> Path | None:
    if not stored_path:
        return None

    value = stored_path.replace("\\", "/").lstrip("/")
    if value.startswith("static/uploads/"):
        relative = value.removeprefix("static/")
    elif value.startswith("web/static/uploads/"):
        relative = value.removeprefix("web/static/")
    else:
        return None

    expected_prefix = "uploads/faces/" if kind == "face" else "uploads/evidence/"
    if not relative.startswith(expected_prefix):
        return None

    root = PUBLIC_STATIC_ROOT.resolve()
    target = (root / relative).resolve()
    if root != target and root not in target.parents:
        return None
    return target


def private_disk_path(relative_path: str) -> Path:
    root = PRIVATE_STORAGE_ROOT.resolve()
    target = (root / relative_path).resolve()
    if root != target and root not in target.parents:
        raise ValueError("Unsafe private storage path.")
    return target


def migrate_public_uploads_to_private(db) -> None:
    ensure_private_storage()
    _migrate_student_faces(db)
    _migrate_evidence_paths(db, "access_logs")
    _migrate_evidence_paths(db, "alerts")
    _move_remaining_public_uploads()


def _migrate_student_faces(db) -> None:
    rows = db.execute(
        """
        SELECT id, image_path
        FROM student_faces
        WHERE image_path LIKE '/static/uploads/faces/%'
           OR image_path LIKE 'web/static/uploads/faces/%'
           OR image_path LIKE 'static/uploads/faces/%'
           OR image_path LIKE 'storage/private/faces/%'
        """
    ).fetchall()
    for row in rows:
        private_path = normalize_private_path(row["image_path"], "face")
        if not private_path:
            continue
        _move_legacy_file(row["image_path"], private_path, "face")
        db.execute("UPDATE student_faces SET image_path=? WHERE id=?", (private_path, row["id"]))


def _migrate_evidence_paths(db, table_name: str) -> None:
    rows = db.execute(
        f"""
        SELECT id, evidence_image_path
        FROM {table_name}
        WHERE evidence_image_path LIKE '/static/uploads/evidence/%'
           OR evidence_image_path LIKE 'web/static/uploads/evidence/%'
           OR evidence_image_path LIKE 'static/uploads/evidence/%'
           OR evidence_image_path LIKE 'storage/private/evidence/%'
        """
    ).fetchall()
    for row in rows:
        private_path = normalize_private_path(row["evidence_image_path"], "evidence")
        if not private_path:
            continue
        _move_legacy_file(row["evidence_image_path"], private_path, "evidence")
        db.execute(f"UPDATE {table_name} SET evidence_image_path=? WHERE id=?", (private_path, row["id"]))


def _move_legacy_file(stored_path: str | None, private_path: str, kind: str) -> None:
    destination = private_disk_path(private_path)
    destination.parent.mkdir(parents=True, exist_ok=True)
    if destination.exists():
        return

    source = legacy_public_file(stored_path, kind)
    if not source or not source.exists():
        return

    os.makedirs(destination.parent, exist_ok=True)
    shutil.move(str(source), str(destination))


def _move_remaining_public_uploads() -> None:
    _move_public_tree(PUBLIC_STATIC_ROOT / "uploads/faces", PRIVATE_FACE_DIR)
    _move_public_tree(PUBLIC_STATIC_ROOT / "uploads/evidence", PRIVATE_EVIDENCE_DIR)


def _move_public_tree(source_root: Path, destination_root: Path) -> None:
    if not source_root.exists():
        return

    for source in sorted(source_root.rglob("*")):
        if not source.is_file():
            continue
        relative = source.relative_to(source_root)
        destination = _unique_destination(destination_root / relative)
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.move(str(source), str(destination))


def _unique_destination(destination: Path) -> Path:
    if not destination.exists():
        return destination

    stem = destination.stem
    suffix = destination.suffix
    parent = destination.parent
    counter = 1
    while True:
        candidate = parent / f"{stem}_{counter}{suffix}"
        if not candidate.exists():
            return candidate
        counter += 1
