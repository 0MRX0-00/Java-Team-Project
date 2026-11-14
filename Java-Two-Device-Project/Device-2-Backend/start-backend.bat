@echo off
setlocal
title DREMLDS - Device 2 Backend & Database Server
cd /d "%~dp0"

echo ======================================================================
echo  DREMLDS -- DEVICE 2 (BACKEND) STARTUP
echo  Checking environment, network IP, database, and launching Spring Boot...
echo ======================================================================
echo.

:: Run PowerShell backend startup script with ExecutionPolicy Bypass
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\windows\start-backend.ps1"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ======================================================================
    echo  Fallback Direct Java Launch
    echo ======================================================================
    if exist "%~dp0target\disaster-relief-dispatch-1.0.0.jar" (
        echo Launching target\disaster-relief-dispatch-1.0.0.jar directly with Java...
        java -jar "%~dp0target\disaster-relief-dispatch-1.0.0.jar"
    ) else (
        echo [ERROR] JAR file not found. Building project with Maven Wrapper...
        call "%~dp0mvnw.cmd" clean package -DskipTests
        if exist "%~dp0target\disaster-relief-dispatch-1.0.0.jar" (
            java -jar "%~dp0target\disaster-relief-dispatch-1.0.0.jar"
        ) else (
            echo [FATAL] Build failed or JAR not created.
        )
    )
    pause
)
