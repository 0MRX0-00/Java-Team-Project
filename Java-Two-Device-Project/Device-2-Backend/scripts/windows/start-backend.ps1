# ==============================================================================
# DREMLDS — DEVICE 2 (BACKEND) STARTUP SCRIPT (WINDOWS 11)
# Checks MySQL connectivity, displays local IPv4, and starts Spring Boot backend.
# ==============================================================================

param(
    [switch]$BuildFirst = $false
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendRoot = (Resolve-Path (Join-Path $scriptDir "..\..")).Path
Set-Location $backendRoot

# 1. Load .env configuration if present
$envFile = Join-Path $backendRoot "config\.env"
if (-not (Test-Path $envFile)) {
    $envFile = Join-Path $backendRoot ".env"
}
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
Write-Host " DREMLDS — Starting Device 2 Backend & Database Server" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

# 2. Detect and print local IPv4 address
$adapters = Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
    $_.IPAddress -ne "127.0.0.1" -and -not $_.IPAddress.StartsWith("169.254.")
}
if ($adapters) {
    Write-Host " Device 2 IPv4 Address(es):" -ForegroundColor Green
    foreach ($adapter in $adapters) {
        Write-Host "   -> http://$($adapter.IPAddress):8080" -ForegroundColor Yellow
    }
} else {
    Write-Host " [WARNING] No external Wi-Fi IP detected. Using localhost." -ForegroundColor Yellow
}

# 3. Check Database Connection
$dbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$dbPort = if ($env:DB_PORT) { [int]$env:DB_PORT } else { 3307 }

Write-Host "`n Checking MySQL Database on ${dbHost}:${dbPort}..." -NoNewline
try {
    $tcp = Test-NetConnection -ComputerName $dbHost -Port $dbPort -WarningAction SilentlyContinue
    if ($tcp.TcpTestSucceeded) {
        Write-Host " [ONLINE]" -ForegroundColor Green
    } else {
        Write-Host " [WARNING: Offline / Unreachable]" -ForegroundColor Yellow
        Write-Host " Note: If MySQL is not running on port $dbPort, Spring Boot will use in-memory database for testing." -ForegroundColor Gray
    }
} catch {
    Write-Host " [CHECK SKIPPED]" -ForegroundColor Gray
}

# 4. Build or Start Spring Boot Backend
$jarPath = Join-Path $backendRoot "target\disaster-relief-dispatch-1.0.0.jar"
$mvnw = Join-Path $backendRoot "mvnw.cmd"

if ($BuildFirst -or (-not (Test-Path $jarPath))) {
    Write-Host "`n Building executable JAR package with Maven Wrapper..." -ForegroundColor Cyan
    & $mvnw clean package -DskipTests
}

if (Test-Path $jarPath) {
    Write-Host "`n Launching DREMLDS Backend JAR (Listening on 0.0.0.0:8080)...`n" -ForegroundColor Green
    java -jar $jarPath
} else {
    Write-Host "`n Launching Spring Boot with Maven Wrapper...`n" -ForegroundColor Green
    & $mvnw spring-boot:run
}

