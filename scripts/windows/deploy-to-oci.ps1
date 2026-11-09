# ==============================================================================
# DISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
# Windows 11 to Oracle Cloud Infrastructure (OCI) Deployment Script
# Uses OpenSSH and SCP (Native Windows 11)
# ==============================================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$AppPublicIp,

    [Parameter(Mandatory=$true)]
    [string]$PrivateKeyPath,

    [string]$RemoteUser = "ubuntu"
)

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
Set-Location $projectRoot

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Deploying DREMLDS to OCI Application Server VM ($AppPublicIp)" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

# 1. Verify SSH Key
if (-not (Test-Path $PrivateKeyPath)) {
    Write-Error "Private SSH key not found at: $PrivateKeyPath"
    exit 1
}

# 2. Build Artifact if not present
$jarPath = Join-Path $projectRoot "target\disaster-relief-dispatch-1.0.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "Building project artifact first..." -ForegroundColor Yellow
    & (Join-Path $PSScriptRoot "build.ps1")
    if ($LASTEXITCODE -ne 0) { exit 1 }
}

# 3. Transfer JAR to Application Server
Write-Host "`n[1/3] Uploading Application JAR via SCP..." -ForegroundColor Cyan
scp -i "$PrivateKeyPath" "$jarPath" "${RemoteUser}@${AppPublicIp}:/tmp/"

# 4. Transfer Setup & Deployment Scripts
Write-Host "`n[2/3] Uploading OCI Deployment Scripts..." -ForegroundColor Cyan
$deployScript = Join-Path $projectRoot "scripts\oci\deploy-app.sh"
scp -i "$PrivateKeyPath" "$deployScript" "${RemoteUser}@${AppPublicIp}:/tmp/"

# 5. Execute Remote Installation
Write-Host "`n[3/3] Executing Remote Deployment on OCI VM..." -ForegroundColor Cyan
$remoteCommands = "sudo mv /tmp/disaster-relief-dispatch-1.0.0.jar /opt/disaster-relief/ && sudo chmod +x /tmp/deploy-app.sh && sudo /tmp/deploy-app.sh"
ssh -i "$PrivateKeyPath" "${RemoteUser}@${AppPublicIp}" "$remoteCommands"

Write-Host "`n======================================================================" -ForegroundColor Green
Write-Host " Deployment command sequence completed!" -ForegroundColor Green
Write-Host " Access Web Console at: http://${AppPublicIp}:8080" -ForegroundColor White
Write-Host " Access Swagger UI at:   http://${AppPublicIp}:8080/swagger-ui.html" -ForegroundColor White
Write-Host "======================================================================`n" -ForegroundColor Green

