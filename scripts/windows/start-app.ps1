# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# Windows 11 Application Startup Script
# ==============================================================================

param(
    [switch]$Background = $false,
    [int]$Port = 8080
)

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
Set-Location $projectRoot

# Load .env configuration if present
$envFile = Join-Path $projectRoot ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $parts = $line.Split("=", 2)
            $varName = $parts[0].Trim()
            $varVal = $parts[1].Trim()
            if (-not [string]::IsNullOrEmpty($varName) -and -not (Test-Path "env:$varName")) {
                [Environment]::SetEnvironmentVariable($varName, $varVal, "Process")
            }
        }
    }
}

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Starting DREMLDS Application on Windows 11" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$jarPath = Join-Path $projectRoot "target\disaster-relief-dispatch-1.0.0.jar"

if (-not (Test-Path $jarPath)) {
    Write-Host "Application JAR not found. Building project first..." -ForegroundColor Yellow
    & (Join-Path $PSScriptRoot "build.ps1")
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Build failed. Cannot start application."
        exit 1
    }
}

$env:SERVER_PORT = $Port
Write-Host "Target Port: $Port" -ForegroundColor Gray
Write-Host "Command Center URL: http://localhost:$Port" -ForegroundColor Green
Write-Host "Swagger UI URL:     http://localhost:$Port/swagger-ui.html" -ForegroundColor Green

if ($Background) {
    Write-Host "`nLaunching Spring Boot process in background..." -ForegroundColor Cyan
    $proc = Start-Process java -ArgumentList "-jar", "`"$jarPath`"" -PassThru -WindowStyle Minimized
    $pidFile = Join-Path $projectRoot "logs\app.pid"
    $proc.Id | Out-File -FilePath $pidFile -Force
    Write-Host "Started process PID $($proc.Id) (written to logs\app.pid)" -ForegroundColor Green
} else {
    Write-Host "`nStarting in interactive foreground mode (Press Ctrl+C to stop)...`n" -ForegroundColor Gray
    java -jar $jarPath
}

