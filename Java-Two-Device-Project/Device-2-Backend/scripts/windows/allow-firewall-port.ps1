# ==============================================================================
# DREMLDS — WINDOWS DEFENDER FIREWALL CONFIGURATOR (DEVICE 2)
# Adds an inbound rule for TCP Port 8080 so Device 1 can connect across LAN.
# Requires Administrator privileges.
# ==============================================================================

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Configuring Windows Defender Firewall for Port 8080" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$ruleName = "DREMLDS Backend REST API (Port 8080)"

# Check if rule exists
$existing = Get-NetFirewallRule -DisplayName $ruleName -ErrorAction SilentlyContinue

if ($existing) {
    Write-Host " [OK] Firewall rule '$ruleName' already exists and is active." -ForegroundColor Green
} else {
    try {
        New-NetFirewallRule -DisplayName $ruleName `
            -Direction Inbound `
            -LocalPort 8080 `
            -Protocol TCP `
            -Action Allow `
            -Profile Any `
            -Description "Allows Device 1 Frontend to communicate with DREMLDS Backend on Device 2" | Out-Null
        Write-Host " [SUCCESS] Inbound rule for TCP Port 8080 created successfully!" -ForegroundColor Green
    } catch {
        Write-Host " [WARNING] Could not create firewall rule: $($_.Exception.Message)" -ForegroundColor Yellow
        Write-Host " If not running as Administrator, please right-click PowerShell, select 'Run as Administrator', and re-run this script." -ForegroundColor Yellow
    }
}

Write-Host "======================================================================`n" -ForegroundColor Cyan
