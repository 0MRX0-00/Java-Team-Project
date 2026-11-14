# ==============================================================================
# DREMLDS -- WINDOWS DEFENDER FIREWALL CONFIGURATOR (DEVICE 2)
# Adds an inbound rule for TCP Port 8080 so Device 1 can connect across LAN.
# ==============================================================================

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Configuring Windows Defender Firewall for Port 8080" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

$ruleName = "DREMLDS Backend REST API (Port 8080)"

# 1. Check if the rule already exists
try {
    $existing = Get-NetFirewallRule -DisplayName $ruleName -ErrorAction SilentlyContinue
    if ($existing) {
        Write-Host " [OK] Firewall rule '$ruleName' is already configured and active." -ForegroundColor Green
        Write-Host " Device 1 can connect to port 8080 over your local network." -ForegroundColor Green
        Write-Host "======================================================================" -ForegroundColor Cyan
        exit 0
    }
} catch {
    # Proceed to check admin
}

# 2. Check if running with Administrator privileges
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host " [ELEVATION REQUIRED] Modifying Windows Firewall requires Administrator rights." -ForegroundColor Yellow
    Write-Host " Requesting Administrator elevation via Windows UAC prompt..." -ForegroundColor Cyan
    Write-Host ""

    try {
        # Trigger elevated netsh command to add the rule
        $netshCmd = "netsh advfirewall firewall add rule name=`"$ruleName`" dir=in action=allow protocol=TCP localport=8080"
        Start-Process powershell.exe -Verb RunAs -ArgumentList "-NoProfile -Command $netshCmd" -Wait

        # Verify whether rule was successfully added
        $check = Get-NetFirewallRule -DisplayName $ruleName -ErrorAction SilentlyContinue
        if ($check) {
            Write-Host " [SUCCESS] Firewall rule successfully added via elevated prompt!" -ForegroundColor Green
        } else {
            Write-Host " [INFO] If UAC prompt was approved, port 8080 is now open." -ForegroundColor Green
        }
    } catch {
        Write-Host " [WARNING] Could not automatically elevate prompt: $($_.Exception.Message)" -ForegroundColor Yellow
        Write-Host ""
        Write-Host " To add the rule manually, open PowerShell AS ADMINISTRATOR and run:" -ForegroundColor White
        Write-Host "   netsh advfirewall firewall add rule name=`"$ruleName`" dir=in action=allow protocol=TCP localport=8080" -ForegroundColor Cyan
    }
} else {
    try {
        New-NetFirewallRule -DisplayName $ruleName `
            -Direction Inbound `
            -LocalPort 8080 `
            -Protocol TCP `
            -Action Allow `
            -Profile Any `
            -Description "Allows Device 1 Frontend to communicate with DREMLDS Backend on Device 2" `
            -ErrorAction Stop | Out-Null
        Write-Host " [SUCCESS] Inbound rule for TCP Port 8080 created successfully!" -ForegroundColor Green
    } catch {
        try {
            netsh advfirewall firewall add rule name="$ruleName" dir=in action=allow protocol=TCP localport=8080 | Out-Null
            Write-Host " [SUCCESS] Inbound rule for TCP Port 8080 created via netsh!" -ForegroundColor Green
        } catch {
            Write-Host " [ERROR] Failed to create rule: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
