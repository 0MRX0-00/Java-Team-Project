# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# Windows 11 Application Stop Script
# ==============================================================================

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$pidFile = Join-Path $projectRoot "logs\app.pid"

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Stopping DREMLDS Application on Windows 11" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$stopped = $false

if (Test-Path $pidFile) {
    $procId = Get-Content $pidFile
    try {
        $proc = Get-Process -Id $procId -ErrorAction Stop
        Write-Host "Stopping process PID $procId ($($proc.ProcessName))..." -ForegroundColor Yellow
        Stop-Process -Id $procId -Force
        Remove-Item $pidFile -Force
        Write-Host "Application stopped successfully." -ForegroundColor Green
        $stopped = $true
    } catch {
        Write-Host "PID $procId from logs\app.pid is not actively running." -ForegroundColor Gray
        Remove-Item $pidFile -Force
    }
}

if (-not $stopped) {
    # Check for Java processes listening on port 8080
    Write-Host "Searching for processes listening on port 8080..." -ForegroundColor Gray
    $portLine = netstat -ano | findstr ":8080" | findstr "LISTENING"
    if ($portLine) {
        $tokens = ($portLine -replace '\s+', ' ').Trim().Split(' ')
        $targetPid = $tokens[$tokens.Length - 1]
        Write-Host "Found listening process on port 8080 with PID: $targetPid" -ForegroundColor Yellow
        Stop-Process -Id $targetPid -Force -ErrorAction SilentlyContinue
        Write-Host "Process PID $targetPid stopped." -ForegroundColor Green
    } else {
        Write-Host "No active application found running on port 8080." -ForegroundColor Gray
    }
}
Write-Host "`n======================================================================`n" -ForegroundColor Cyan

