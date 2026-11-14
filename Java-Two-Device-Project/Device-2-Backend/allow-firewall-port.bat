@echo off
setlocal
title DREMLDS - Open Firewall Port 8080
cd /d "%~dp0"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\windows\allow-firewall-port.ps1"
echo.
pause
