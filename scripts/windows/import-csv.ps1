# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# Windows 11 CSV Batch Ingestion Script
# ==============================================================================

param(
    [string]$FilePath = "data\relief_requests.csv",
    [string]$EndpointUrl = "http://localhost:8080/api/import/csv"
)

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$targetFile = if ([System.IO.Path]::IsPathRooted($FilePath)) { $FilePath } else { Join-Path $projectRoot $FilePath }

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Ingesting Disaster Relief Requests CSV" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

if (-not (Test-Path $targetFile)) {
    Write-Error "CSV file not found at: $targetFile"
    exit 1
}

Write-Host "Source File: $targetFile" -ForegroundColor Gray
Write-Host "Target API:  $EndpointUrl" -ForegroundColor Gray

try {
    # Check if application is reachable
    $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get -TimeoutSec 3 -ErrorAction Stop
    Write-Host "Application is ONLINE. Triggering ingestion..." -ForegroundColor Green

    $response = Invoke-RestMethod -Uri $EndpointUrl -Method Post -TimeoutSec 10
    Write-Host "`nImport Summary:" -ForegroundColor White
    Write-Host "  File:         $($response.filename)" -ForegroundColor Gray
    Write-Host "  Total Rows:   $($response.totalRows)" -ForegroundColor Gray
    Write-Host "  Successful:   $($response.successfulRows)" -ForegroundColor Green
    Write-Host "  Rejected:     $($response.rejectedRows)" -ForegroundColor $(if ($response.rejectedRows -gt 0) { "Yellow" } else { "Gray" })

    if ($response.importedZoneCodes) {
        Write-Host "  Imported Zones: $($response.importedZoneCodes -join ', ')" -ForegroundColor Cyan
    }
    if ($response.errorMessages -and $response.errorMessages.Count -gt 0) {
        Write-Host "`nRejected Rows Detail:" -ForegroundColor Yellow
        foreach ($msg in $response.errorMessages) {
            Write-Host "  * $msg" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "Error connecting to application at $EndpointUrl" -ForegroundColor Red
    Write-Host "Please ensure application is running (`.\scripts\windows\start-app.ps1`)" -ForegroundColor Yellow
    Write-Host "Details: $($_.Exception.Message)" -ForegroundColor Gray
}
Write-Host "`n======================================================================`n" -ForegroundColor Cyan

