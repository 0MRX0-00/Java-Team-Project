@echo off
setlocal
title DREMLDS - Device 2 Backend & Database Server
cd /d "%~dp0Device-2-Backend"
call "%~dp0Device-2-Backend\start-backend.bat"
