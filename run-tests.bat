@echo off
setlocal enabledelayedexpansion
REM Lance la suite de tests JUnit 5 du projet STRMS via le Maven Wrapper.
REM Pre-requis : JDK 17+ installe.

cd /d "%~dp0"

echo =====================================================
echo   STRMS - Execution des tests JUnit
echo =====================================================
echo.

where java >nul 2>nul
if errorlevel 1 (
    echo [ERREUR] Java n'est pas dans le PATH. Installez un JDK 17+.
    pause
    exit /b 1
)

call .\mvnw.cmd test
if errorlevel 1 (
    echo.
    echo [ERREUR] Les tests ont echoue.
    pause
    exit /b 1
)

echo.
echo [OK] Tests termines avec succes.
endlocal
