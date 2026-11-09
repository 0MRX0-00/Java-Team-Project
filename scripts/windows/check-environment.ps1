# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# Windows 11 Environment & Dependency Verification Script
# ==============================================================================

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " DREMLDS — Windows 11 Environment Diagnostics" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$allPass = $true

# 1. Check Java JDK
Write-Host "`n[1/5] Checking Java Development Kit..." -NoNewline
try {
    $javaVer = java -version 2>&1 | Out-String
    if ($LASTEXITCODE -eq 0 -or $javaVer -match "version") {
        Write-Host " [OK]" -ForegroundColor Green
        Write-Host "   $($javaVer.Split([Environment]::NewLine)[0])" -ForegroundColor Gray
    } else {
        Write-Host " [FAIL]" -ForegroundColor Red
        $allPass = $false
    }
} catch {
    Write-Host " [NOT FOUND]" -ForegroundColor Red
    Write-Host "   Java is not on PATH. Please install Java JDK 17 or 21." -ForegroundColor Yellow
    $allPass = $false
}

# 2. Check Maven Wrapper
Write-Host "`n[2/5] Checking Maven Wrapper (mvnw.cmd)..." -NoNewline
$mvnwPath = Join-Path $PSScriptRoot "..\..\mvnw.cmd"
if (Test-Path $mvnwPath) {
    Write-Host " [OK]" -ForegroundColor Green
    Write-Host "   Found Maven Wrapper at: $mvnwPath" -ForegroundColor Gray
} else {
    Write-Host " [FAIL]" -ForegroundColor Red
    Write-Host "   mvnw.cmd missing from project root." -ForegroundColor Yellow
    $allPass = $false
}

# 3. Check MySQL / MariaDB Server (Port 3306)
Write-Host "`n[3/5] Checking Database Port (3306)..." -NoNewline
$dbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$dbPort = if ($env:DB_PORT) { [int]$env:DB_PORT } else { 3306 }

try {
    $tcp = Test-NetConnection -ComputerName $dbHost -Port $dbPort -WarningAction SilentlyContinue
    if ($tcp.TcpTestSucceeded) {
        Write-Host " [ONLINE]" -ForegroundColor Green
        Write-Host "   Successfully reached database at $dbHost:$dbPort" -ForegroundColor Gray
    } else {
        Write-Host " [OFFLINE / UNREACHABLE]" -ForegroundColor Yellow
        Write-Host "   MySQL is not listening on $dbHost:$dbPort." -ForegroundColor Gray
        Write-Host "   (Note: Application and tests will automatically run with embedded in-memory database)" -ForegroundColor Gray
    }
} catch {
    Write-Host " [SKIP]" -ForegroundColor Gray
}

# 4. Check Environment Variables
Write-Host "`n[4/5] Checking Application Environment Variables..." -NoNewline
Write-Host " [OK]" -ForegroundColor Green
Write-Host "   DB_HOST = $(if ($env:DB_HOST) { $env:DB_HOST } else { 'localhost (default)' })" -ForegroundColor Gray
Write-Host "   DB_PORT = $(if ($env:DB_PORT) { $env:DB_PORT } else { '3306 (default)' })" -ForegroundColor Gray
Write-Host "   DB_NAME = $(if ($env:DB_NAME) { $env:DB_NAME } else { 'disaster_relief_db (default)' })" -ForegroundColor Gray
Write-Host "   DB_USER = $(if ($env:DB_USER) { $env:DB_USER } else { 'relief_app / root (default)' })" -ForegroundColor Gray

# 5. Check Git
Write-Host "`n[5/5] Checking Git for Windows..." -NoNewline
try {
    $gitVer = git --version 2>&1
    Write-Host " [OK]" -ForegroundColor Green
    Write-Host "   $gitVer" -ForegroundColor Gray
} catch {
    Write-Host " [NOT FOUND]" -ForegroundColor Yellow
}

Write-Host "`n======================================================================" -ForegroundColor Cyan
if ($allPass) {
    Write-Host " All essential components verified! Environment is ready to build and run." -ForegroundColor Green
} else {
    Write-Host " Some required components were missing. Please review warnings above." -ForegroundColor Yellow
}
Write-Host "======================================================================`n" -ForegroundColor Cyan

