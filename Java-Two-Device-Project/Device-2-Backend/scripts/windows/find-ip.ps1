# ==============================================================================
# DREMLDS — DEVICE 2 IP ADDRESS DETECTOR (WINDOWS 11)
# Detects local IPv4 addresses and displays the exact URL to enter on Device 1
# ==============================================================================

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " DREMLDS — Device 2 (Backend) Local Network IP Detector" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$adapters = Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
    $_.IPAddress -ne "127.0.0.1" -and -not $_.IPAddress.StartsWith("169.254.")
}

if ($adapters) {
    Write-Host "`nFound active IPv4 address(es) on Device 2:`n" -ForegroundColor Green
    foreach ($adapter in $adapters) {
        $alias = (Get-NetIPInterface -InterfaceIndex $adapter.InterfaceIndex -AddressFamily IPv4).InterfaceAlias
        Write-Host "   Adapter: [$alias]" -ForegroundColor Yellow
        Write-Host "   IPv4 Address: $($adapter.IPAddress)" -ForegroundColor White
        Write-Host "   ===> Backend URL for Device 1: http://$($adapter.IPAddress):8080" -ForegroundColor Cyan
        Write-Host ""
    }
    Write-Host "----------------------------------------------------------------------" -ForegroundColor Gray
    Write-Host " INSTRUCTION FOR DEVICE 1:" -ForegroundColor Yellow
    Write-Host " On Laptop 1 (Frontend), open the browser, click the connection badge" -ForegroundColor White
    Write-Host " in the top-right corner, and paste the URL above!" -ForegroundColor White
    Write-Host "----------------------------------------------------------------------`n" -ForegroundColor Gray
} else {
    Write-Host "`n[WARNING] No active IPv4 network adapter found." -ForegroundColor Red
    Write-Host "Ensure Device 2 is connected to Wi-Fi or a Mobile Hotspot.`n" -ForegroundColor Yellow
}
