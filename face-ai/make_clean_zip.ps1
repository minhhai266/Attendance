[CmdletBinding()]
param(
    [string]$SourceRoot = "",
    [string]$OutputZip = ""
)

if ([string]::IsNullOrWhiteSpace($SourceRoot)) {
    $SourceRoot = $PSScriptRoot
}

$source = (Resolve-Path -LiteralPath $SourceRoot).Path.TrimEnd("\", "/")
if ([string]::IsNullOrWhiteSpace($OutputZip)) {
    $OutputZip = Join-Path (Split-Path -Parent $source) "face_lab_fastapi_html_lab_manager_clean.zip"
}

$output = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($OutputZip)
$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("face_lab_clean_" + [System.Guid]::NewGuid().ToString("N"))

$excludedExact = @(
    ".env",
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

    if (Test-Path -LiteralPath $output) {
        Remove-Item -LiteralPath $output -Force
    }

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::CreateFromDirectory($tempRoot, $output)
    Write-Output "Created clean zip: $output"
}
finally {
    if (Test-Path -LiteralPath $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}
