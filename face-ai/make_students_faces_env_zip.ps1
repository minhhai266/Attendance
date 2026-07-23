[CmdletBinding()]
param(
    [string]$SourceRoot = "",
    [string]$OutputZip = "",
    [switch]$KeepStudentUsers
)

if ([string]::IsNullOrWhiteSpace($SourceRoot)) {
    $SourceRoot = $PSScriptRoot
}

$source = (Resolve-Path -LiteralPath $SourceRoot).Path.TrimEnd("\", "/")
if ([string]::IsNullOrWhiteSpace($OutputZip)) {
    $OutputZip = Join-Path (Split-Path -Parent $source) "face_lab_fastapi_html_lab_manager_students_faces_env.zip"
}

$output = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($OutputZip)
$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("face_lab_students_faces_" + [System.Guid]::NewGuid().ToString("N"))
$pythonHelper = $null

$excludedExact = @(
    "data/face_lab.db",
    "data/server_stdout.log",
    "data/server_stderr.log",
    "server_stdout.log",
    "server_stderr.log"
)

$excludedPrefixes = @(
    "data/backups/",
    "data/debug_liveness_crop/",
    "storage/private/",
    "web/static/uploads/",
    "dist/",
    "build/"
)

$excludedParts = @(
    ".git",
    ".mypy_cache",
    ".pytest_cache",
    ".ruff_cache",
    ".venv",
    "__pycache__",
    "htmlcov",
    "node_modules",
    "venv"
)

function Test-ExcludedPath {
    param([string]$RelativePath, [string]$FileName)

    $relativeUnix = $RelativePath.Replace("\", "/")
    if ($excludedExact -icontains $relativeUnix) {
        return $true
    }

    foreach ($prefix in $excludedPrefixes) {
        if ($relativeUnix.StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)) {
            return $true
        }
    }

    $parts = $relativeUnix -split "/"
    foreach ($part in $parts) {
        if ($excludedParts -icontains $part) {
            return $true
        }
    }

    if ($FileName -like "*.pyc" -or $FileName -like "*.pyo" -or $FileName -like "*.zip") {
        return $true
    }

    if ($relativeUnix -match "^data/[^/]+\.(db|sqlite|sqlite3|log)$") {
        return $true
    }

    return $false
}

function Resolve-PythonCommand {
    $python = Get-Command python -ErrorAction SilentlyContinue
    if ($python) {
        return @($python.Source)
    }

    $py = Get-Command py -ErrorAction SilentlyContinue
    if ($py) {
        return @($py.Source, "-3")
    }

    throw "Python was not found. This script needs Python sqlite3 to create the filtered database."
}

New-Item -ItemType Directory -Path $tempRoot -Force | Out-Null

try {
    Get-ChildItem -LiteralPath $source -Recurse -File -Force | ForEach-Object {
        $relativePath = $_.FullName.Substring($source.Length).TrimStart("\", "/")
        if (Test-ExcludedPath -RelativePath $relativePath -FileName $_.Name) {
            return
        }

        $destination = Join-Path $tempRoot $relativePath
        $destinationDir = Split-Path -Parent $destination
        New-Item -ItemType Directory -Path $destinationDir -Force | Out-Null
        Copy-Item -LiteralPath $_.FullName -Destination $destination -Force
    }

    $sourceDb = Join-Path $source "data/face_lab.db"
    $destDb = Join-Path $tempRoot "data/face_lab.db"
    if (Test-Path -LiteralPath $sourceDb) {
        New-Item -ItemType Directory -Path (Split-Path -Parent $destDb) -Force | Out-Null

        $env:FACE_LAB_PACKAGE_SOURCE_ROOT = $source
        $env:FACE_LAB_PACKAGE_DEST_ROOT = $tempRoot
        $env:FACE_LAB_PACKAGE_SOURCE_DB = $sourceDb
        $env:FACE_LAB_PACKAGE_DEST_DB = $destDb
        $env:FACE_LAB_PACKAGE_KEEP_STUDENT_USERS = if ($KeepStudentUsers) { "1" } else { "0" }

        $pythonScript = @'
import os
import shutil
import sqlite3
from pathlib import Path

source_root = Path(os.environ["FACE_LAB_PACKAGE_SOURCE_ROOT"]).resolve()
dest_root = Path(os.environ["FACE_LAB_PACKAGE_DEST_ROOT"]).resolve()
source_db = Path(os.environ["FACE_LAB_PACKAGE_SOURCE_DB"]).resolve()
dest_db = Path(os.environ["FACE_LAB_PACKAGE_DEST_DB"]).resolve()
keep_student_users = os.environ.get("FACE_LAB_PACKAGE_KEEP_STUDENT_USERS") == "1"

if dest_db.exists():
    dest_db.unlink()

shutil.copy2(source_db, dest_db)

conn = sqlite3.connect(dest_db)
conn.row_factory = sqlite3.Row
try:
    conn.execute("PRAGMA foreign_keys=OFF")

    tables_to_clear = [
        "access_logs",
        "attendance_records",
        "alerts",
        "audit_logs",
        "login_rate_limits",
        "user_sessions",
    ]
    for table in tables_to_clear:
        try:
            conn.execute(f"DELETE FROM {table}")
        except sqlite3.OperationalError:
            pass

    try:
        if keep_student_users:
            conn.execute(
                """
                DELETE FROM users
                WHERE role <> 'student'
                   OR student_id IS NULL
                   OR student_id NOT IN (SELECT id FROM students)
                """
            )
        else:
            conn.execute("DELETE FROM users")
    except sqlite3.OperationalError:
        pass

    conn.commit()

    face_rows = []
    try:
        face_rows = conn.execute(
            "SELECT id, image_path FROM student_faces WHERE image_path IS NOT NULL AND image_path <> ''"
        ).fetchall()
    except sqlite3.OperationalError:
        face_rows = []

    def normalize_face_path(value):
        value = (value or "").replace("\\", "/").lstrip("/")
        if value.startswith("storage/private/"):
            value = value.removeprefix("storage/private/")
        elif value.startswith("static/uploads/faces/"):
            value = "faces/" + value.removeprefix("static/uploads/faces/")
        elif value.startswith("web/static/uploads/faces/"):
            value = "faces/" + value.removeprefix("web/static/uploads/faces/")
        if not value.startswith("faces/"):
            return None
        parts = Path(value).parts
        if any(part in {"", ".", ".."} for part in parts):
            return None
        return value

    def source_candidates(original, normalized):
        original = (original or "").replace("\\", "/").lstrip("/")
        candidates = []
        candidates.append(source_root / "storage/private" / normalized)
        if original.startswith("web/static/uploads/faces/"):
            candidates.append(source_root / original)
        elif original.startswith("static/uploads/faces/"):
            candidates.append(source_root / "web" / original)
        elif original.startswith("storage/private/"):
            candidates.append(source_root / original)
        return candidates

    copied_faces = 0
    missing_faces = []
    for row in face_rows:
        normalized = normalize_face_path(row["image_path"])
        if not normalized:
            missing_faces.append(str(row["image_path"]))
            continue

        destination = (dest_root / "storage/private" / normalized).resolve()
        if dest_root not in destination.parents and dest_root != destination:
            missing_faces.append(str(row["image_path"]))
            continue

        source_file = None
        for candidate in source_candidates(row["image_path"], normalized):
            candidate = candidate.resolve()
            if candidate.is_file():
                source_file = candidate
                break

        if not source_file:
            missing_faces.append(str(row["image_path"]))
            continue

        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source_file, destination)
        copied_faces += 1

    conn.execute("VACUUM")

    student_count = conn.execute("SELECT COUNT(*) FROM students").fetchone()[0]
    face_count = conn.execute("SELECT COUNT(*) FROM student_faces").fetchone()[0]
    setting_count = 0
    try:
        setting_count = conn.execute("SELECT COUNT(*) FROM settings").fetchone()[0]
    except sqlite3.OperationalError:
        pass

    print(f"Filtered database: {dest_db}")
    print(f"Students: {student_count}")
    print(f"Face embeddings: {face_count}")
    print(f"Face images copied: {copied_faces}")
    print(f"Settings kept: {setting_count}")
    if keep_student_users:
        try:
            user_count = conn.execute("SELECT COUNT(*) FROM users").fetchone()[0]
            print(f"Student users kept: {user_count}")
        except sqlite3.OperationalError:
            pass
    if missing_faces:
        print("Missing face image files:")
        for item in missing_faces:
            print(f" - {item}")
finally:
    conn.close()
'@

        $pythonCommand = @(Resolve-PythonCommand)
        $pythonExe = $pythonCommand[0]
        $pythonArgs = @()
        if ($pythonCommand.Length -gt 1) {
            $pythonArgs = $pythonCommand[1..($pythonCommand.Length - 1)]
        }
        $pythonHelper = Join-Path ([System.IO.Path]::GetTempPath()) ("face_lab_package_helper_" + [System.Guid]::NewGuid().ToString("N") + ".py")
        Set-Content -LiteralPath $pythonHelper -Value $pythonScript -Encoding UTF8
        & $pythonExe @pythonArgs $pythonHelper
        if ((-not $?) -or $LASTEXITCODE -ne 0) {
            throw "Failed to create filtered student/face database."
        }
    }
    else {
        Write-Warning "Source database not found: $sourceDb"
    }

    if (Test-Path -LiteralPath $output) {
        Remove-Item -LiteralPath $output -Force
    }

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::CreateFromDirectory($tempRoot, $output)
    Write-Output "Created students/faces/env zip: $output"
}
finally {
    Remove-Item Env:\FACE_LAB_PACKAGE_SOURCE_ROOT -ErrorAction SilentlyContinue
    Remove-Item Env:\FACE_LAB_PACKAGE_DEST_ROOT -ErrorAction SilentlyContinue
    Remove-Item Env:\FACE_LAB_PACKAGE_SOURCE_DB -ErrorAction SilentlyContinue
    Remove-Item Env:\FACE_LAB_PACKAGE_DEST_DB -ErrorAction SilentlyContinue
    Remove-Item Env:\FACE_LAB_PACKAGE_KEEP_STUDENT_USERS -ErrorAction SilentlyContinue

    if ($pythonHelper -and (Test-Path -LiteralPath $pythonHelper)) {
        Remove-Item -LiteralPath $pythonHelper -Force
    }

    if (Test-Path -LiteralPath $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}
