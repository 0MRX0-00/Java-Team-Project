# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# Windows 11 Environment & Dependency Verification Script
# ==============================================================================

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " DREMLDS -- Windows 11 Environment Diagnostics" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

# Load .env file if present
$envPath = Join-Path $PSScriptRoot "..\..\.env"
if (Test-Path $envPath) {
    Get-Content $envPath | ForEach-Object {
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

$allPass = $true

# 1. Check Java JDK (Must be 17 or 21)
Write-Host "`n[1/5] Checking Java Development Kit..." -NoNewline
try {
    $javaVer = java -version 2>&1 | Out-String
    if ($javaVer -match 'version "(17|21)') {
        Write-Host " [OK]" -ForegroundColor Green
        Write-Host "   java : $($javaVer.Split([Environment]::NewLine)[0])" -ForegroundColor Gray
    } else {
        Write-Host " [WARNING]" -ForegroundColor Yellow
        Write-Host "   Found Java, but expected JDK 17 or 21: $($javaVer.Split([Environment]::NewLine)[0])" -ForegroundColor Yellow
    }
} catch {
    Write-Host " [NOT FOUND]" -ForegroundColor Red
    Write-Host "   Java is not installed or not in PATH." -ForegroundColor Red
    $allPass = $false
}

# 2. Check Maven Wrapper
Write-Host "`n[2/5] Checking Maven Wrapper (mvnw.cmd)..." -NoNewline
$mvnwPath = Join-Path $PSScriptRoot "..\..\mvnw.cmd"
if (Test-Path $mvnwPath) {
    Write-Host " [OK]" -ForegroundColor Green
    Write-Host "   Found Maven Wrapper at: $mvnwPath" -ForegroundColor Gray
} else {
    Write-Host " [MISSING]" -ForegroundColor Red
    Write-Host "   mvnw.cmd not found in project root." -ForegroundColor Red
    $allPass = $false
}

# 3. Check MySQL / MariaDB Server
$dbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$dbPort = if ($env:DB_PORT) { [int]$env:DB_PORT } else { 3307 }
Write-Host "`n[3/5] Checking Database Port ($dbPort)..." -NoNewline

try {
    $tcp = Test-NetConnection -ComputerName $dbHost -Port $dbPort -WarningAction SilentlyContinue
    if ($tcp.TcpTestSucceeded) {
        Write-Host " [ONLINE]" -ForegroundColor Green
        Write-Host "   Successfully reached database at ${dbHost}:${dbPort}" -ForegroundColor Gray
    } else {
        Write-Host " [OFFLINE / UNREACHABLE]" -ForegroundColor Yellow
        Write-Host "   MySQL is not listening on ${dbHost}:${dbPort}." -ForegroundColor Gray
        Write-Host "   (Note: Application and tests will automatically run with embedded in-memory database)" -ForegroundColor Gray
    }
} catch {
    Write-Host " [UNABLE TO CHECK]" -ForegroundColor Yellow
}

# 4. Check Environment Variables
Write-Host "`n[4/5] Checking Application Environment Variables..." -NoNewline
Write-Host " [OK]" -ForegroundColor Green
Write-Host "   DB_HOST = $(if ($env:DB_HOST) { $env:DB_HOST } else { 'localhost' })" -ForegroundColor Gray
Write-Host "   DB_PORT = $(if ($env:DB_PORT) { $env:DB_PORT } else { '3307' })" -ForegroundColor Gray
Write-Host "   DB_NAME = $(if ($env:DB_NAME) { $env:DB_NAME } else { 'disaster_relief_db' })" -ForegroundColor Gray
Write-Host "   DB_USER = $(if ($env:DB_USER) { $env:DB_USER } else { 'host' })" -ForegroundColor Gray

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
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""
