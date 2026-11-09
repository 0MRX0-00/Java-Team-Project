# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# Windows 11 Build & Package Script (Maven Wrapper)
# ==============================================================================

param(
    [switch]$SkipTests = $false
)

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
Set-Location $projectRoot

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Building DREMLDS Application via Maven Wrapper" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$mvnw = Join-Path $projectRoot "mvnw.cmd"
if (-not (Test-Path $mvnw)) {
    Write-Error "mvnw.cmd not found in $projectRoot"
    exit 1
}

$buildArgs = @("clean", "package")
if ($SkipTests) {
    $buildArgs += "-DskipTests"
    Write-Host "Notice: Test execution will be skipped as requested." -ForegroundColor Yellow
}

Write-Host "Executing: .\mvnw.cmd $($buildArgs -join ' ')..." -ForegroundColor Gray
& $mvnw $buildArgs

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n======================================================================" -ForegroundColor Green
    Write-Host " BUILD SUCCESSFUL!" -ForegroundColor Green
    Write-Host " Application artifact created at: target\disaster-relief-dispatch-1.0.0.jar" -ForegroundColor White
    Write-Host "======================================================================`n" -ForegroundColor Green
} else {
    Write-Host "`n======================================================================" -ForegroundColor Red
    Write-Host " BUILD FAILED (Exit Code: $LASTEXITCODE)" -ForegroundColor Red
    Write-Host "======================================================================`n" -ForegroundColor Red
    exit $LASTEXITCODE
}

