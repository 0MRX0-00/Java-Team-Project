@echo off
setlocal
title DREMLDS - Device 1 Frontend Command Center
cd /d "%~dp0Device-1-Frontend"
call "%~dp0Device-1-Frontend\start-frontend.bat"
