# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# Windows 11 Automated Test Execution Script
# ==============================================================================

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
Set-Location $projectRoot

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Running Automated JUnit 5 & Mockito Tests via Maven Surefire" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$mvnw = Join-Path $projectRoot "mvnw.cmd"

Write-Host "Executing: .\mvnw.cmd test..." -ForegroundColor Gray
& $mvnw test

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n======================================================================" -ForegroundColor Green
    Write-Host " ALL TESTS PASSED SUCCESSFULLY!" -ForegroundColor Green
    Write-Host " Test reports generated in: target\surefire-reports\" -ForegroundColor White
    Write-Host "======================================================================`n" -ForegroundColor Green
} else {
    Write-Host "`n======================================================================" -ForegroundColor Red
    Write-Host " TESTS FAILED (Exit Code: $LASTEXITCODE)" -ForegroundColor Red
    Write-Host " Review reports in target\surefire-reports\ for details." -ForegroundColor Yellow
    Write-Host "======================================================================`n" -ForegroundColor Red
    exit $LASTEXITCODE
}

