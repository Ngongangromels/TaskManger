@echo off
setlocal enabledelayedexpansion
REM ==========================================================
REM Lancement de l'application STRMS sous Windows.
REM
REM Cette version utilise le Maven Wrapper (mvnw.cmd) embarque
REM dans le projet : aucune installation de Maven requise.
REM
REM Pre-requis : JDK 17+ installe (variable JAVA_HOME ou PATH).
REM ==========================================================

cd /d "%~dp0"

echo =====================================================
echo   STRMS - Smart Task and Resource Management System
echo =====================================================
echo.

REM ---------- Verifier Java ----------
where java >nul 2>nul
if errorlevel 1 (
    echo [ERREUR] Java n'est pas detecte dans le PATH.
    echo Installez un JDK 17+ :
    echo   - Eclipse Adoptium : https://adoptium.net/
    echo   - Liberica         : https://bell-sw.com/pages/downloads/
    pause
    exit /b 1
)

echo [INFO] Lancement via le Maven Wrapper...
echo        ^(la premiere fois, le wrapper telecharge Maven et JavaFX^)
echo.

call .\mvnw.cmd -q javafx:run
set "RC=%ERRORLEVEL%"

if not "%RC%"=="0" (
    echo.
    echo =========================================================
    echo [ERREUR] Le lancement a echoue ^(code %RC%^).
    echo.
    echo Causes les plus frequentes :
    echo   1. Connexion Internet absente ^(le wrapper doit telecharger
    echo      Maven et les dependances JavaFX au premier lancement^).
    echo   2. Windows Defender bloque la creation de "target\".
    echo      Solution : deplacez le projet hors de "Documents",
    echo      par exemple dans C:\Dev\strms.
    echo.
    echo Voir docs/INSTALLATION.md pour plus de details.
    pause
    exit /b %RC%
)

endlocal
