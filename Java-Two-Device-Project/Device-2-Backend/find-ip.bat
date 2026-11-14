@echo off
setlocal
title DREMLDS - Find Device 2 IPv4 Address
cd /d "%~dp0"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\windows\find-ip.ps1"
echo.
pause

