# ==============================================================================
# DREMLDS -- RUN AUTOMATED TEST SUITE (DEVICE 2 BACKEND)
# Runs all 47 JUnit 5 & Mockito automated tests via Maven Wrapper.
# ==============================================================================

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendRoot = (Resolve-Path (Join-Path $scriptDir "..\..")).Path
Set-Location $backendRoot

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Running Automated Backend Tests (Device 2)" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$mvnw = Join-Path $backendRoot "mvnw.cmd"
& $mvnw test
