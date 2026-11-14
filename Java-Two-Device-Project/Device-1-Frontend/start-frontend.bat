@echo off
setlocal
title DREMLDS - Device 1 Frontend Command Center
cd /d "%~dp0"

echo ======================================================================
echo  DREMLDS -- DEVICE 1 (FRONTEND) STARTUP
echo  Starting local web server and launching Command Center UI...
echo ======================================================================
echo.

:: Run PowerShell native lightweight server with ExecutionPolicy Bypass
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-frontend.ps1"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [INFO] Falling back to direct browser launch...
    start "" "%~dp0src\index.html"
    pause
)
