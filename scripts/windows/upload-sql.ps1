# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# Windows 11 to OCI Database Server Schema Upload Script
# ==============================================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$DbPublicIp,

    [Parameter(Mandatory=$true)]
    [string]$PrivateKeyPath,

    [string]$RemoteUser = "ubuntu"
)

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$sqlPath = Join-Path $projectRoot "database\disaster_relief_schema.sql"

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Uploading Database Schema to OCI Database VM ($DbPublicIp)" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

if (-not (Test-Path $sqlPath)) {
    Write-Error "SQL file not found at: $sqlPath"
    exit 1
}

if (-not (Test-Path $PrivateKeyPath)) {
    Write-Error "SSH Key not found at: $PrivateKeyPath"
    exit 1
}

# 1. SCP SQL file
Write-Host "`n[1/2] Transferring disaster_relief_schema.sql via SCP..." -ForegroundColor Cyan
scp -i "$PrivateKeyPath" "$sqlPath" "${RemoteUser}@${DbPublicIp}:/tmp/"

# 2. Execute SQL Import on Remote Database
Write-Host "`n[2/2] Importing Schema into MySQL on OCI VM..." -ForegroundColor Cyan
$cmd = "sudo mysql < /tmp/disaster_relief_schema.sql && echo 'Schema and demonstration data successfully imported into disaster_relief_db!'"
ssh -i "$PrivateKeyPath" "${RemoteUser}@${DbPublicIp}" "$cmd"

Write-Host "`n======================================================================" -ForegroundColor Green
Write-Host " Database Schema Upload & Initialization Complete!" -ForegroundColor Green
Write-Host "======================================================================`n" -ForegroundColor Green

