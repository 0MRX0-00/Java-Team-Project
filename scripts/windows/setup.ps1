# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# Local Windows 11 Workspace Setup Script
# ==============================================================================

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Setting up DREMLDS Local Workspace for Windows 11" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path

# 1. Create required runtime directories
$dirs = @("exports", "logs", "data", "target")
foreach ($d in $dirs) {
    $fullPath = Join-Path $projectRoot $d
    if (-not (Test-Path $fullPath)) {
        New-Item -ItemType Directory -Path $fullPath -Force | Out-Null
        Write-Host "Created directory: $d" -ForegroundColor Green
    }
}

# 2. Check for .env file
$envExample = Join-Path $projectRoot ".env.example"
$envFile = Join-Path $projectRoot ".env"
if (-not (Test-Path $envFile) -and (Test-Path $envExample)) {
    Copy-Item $envExample $envFile
    Write-Host "Created .env configuration from .env.example" -ForegroundColor Green
} else {
    Write-Host ".env configuration file already exists." -ForegroundColor Gray
}

# 3. Verify sample data file
$csvSample = Join-Path $projectRoot "data\relief_requests.csv"
if (Test-Path $csvSample) {
    Write-Host "Verified sample CSV file: data\relief_requests.csv" -ForegroundColor Green
}

# 4. Check environment
& (Join-Path $PSScriptRoot "check-environment.ps1")

Write-Host "`nSetup complete! You can now run:" -ForegroundColor Cyan
Write-Host "  .\scripts\windows\build.ps1        (to compile and package)" -ForegroundColor White
Write-Host "  .\scripts\windows\run-tests.ps1    (to run JUnit test suite)" -ForegroundColor White
Write-Host "  .\scripts\windows\start-app.ps1     (to start Spring Boot server)`n" -ForegroundColor White

