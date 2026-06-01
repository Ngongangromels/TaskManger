@echo off
setlocal enabledelayedexpansion
REM ==========================================================
REM  Production de l'installeur Windows (.exe) pour STRMS.
REM
REM  Etapes :
REM   1. Nettoyer le projet
REM   2. Generer le runtime jlink (mini JRE + classes app)
REM   3. Lancer jpackage pour creer un installeur .exe
REM
REM  Pre-requis :
REM   - JDK 17+ avec jpackage et jlink (Liberica/Adoptium "Full")
REM   - WiX Toolset 3.x ou 4.x si on cree un .msi
REM     (pour le format .exe simple, WiX n'est pas requis)
REM ==========================================================

cd /d "%~dp0"

echo =====================================================
echo   STRMS - Generation de l'installeur Windows
echo =====================================================
echo.

REM ---------- Verifier jpackage ----------
where jpackage >nul 2>nul
set "JPACKAGE_FOUND=%ERRORLEVEL%"
if not "%JPACKAGE_FOUND%"=="0" (
    REM Tentative via JAVA_HOME
    if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jpackage.exe" (
        set "PATH=%JAVA_HOME%\bin;%PATH%"
        set "JPACKAGE_FOUND=0"
    )
)
if not "%JPACKAGE_FOUND%"=="0" (
    REM Tentative via Liberica
    if exist "%USERPROFILE%\.jdks\liberica-full-23.0.2\bin\jpackage.exe" (
        set "PATH=%USERPROFILE%\.jdks\liberica-full-23.0.2\bin;%PATH%"
        set "JPACKAGE_FOUND=0"
    )
)
if not "%JPACKAGE_FOUND%"=="0" (
    echo [ERREUR] jpackage est introuvable.
    echo Installez un JDK 17+ et ajoutez son dossier bin au PATH.
    pause
    exit /b 1
)

REM ---------- Etape 1 : nettoyer ----------
echo [1/3] Nettoyage du projet...
call .\mvnw.cmd -B -q clean
if errorlevel 1 (
    echo [ERREUR] mvnw clean a echoue.
    pause
    exit /b 1
)

REM ---------- Etape 2 : jlink ----------
echo [2/3] Generation du runtime jlink (mini JRE auto-suffisant)...
call .\mvnw.cmd -B -q javafx:jlink
if errorlevel 1 (
    echo [ERREUR] mvnw javafx:jlink a echoue.
    pause
    exit /b 1
)

REM ---------- Etape 3 : jpackage ----------
echo [3/3] Generation de l'installeur .exe avec jpackage...
if exist "dist" rmdir /s /q "dist"

jpackage ^
    --type app-image ^
    --runtime-image "target\strms-runtime" ^
    --module fr.eseo.strms/fr.eseo.strms.ui.MainApp ^
    --name STRMS ^
    --app-version 1.0.0 ^
    --vendor "ESEO E3e S6 - Spring 2026" ^
    --description "Smart Task and Resource Management System" ^
    --copyright "(c) 2026 STRMS Team" ^
    --dest "dist"

if errorlevel 1 (
    echo [ERREUR] jpackage a echoue.
    pause
    exit /b 1
)

REM ---------- Etape 4 : creer un launcher .bat de fallback ----------
REM Sur certaines machines Windows 11 avec "Smart App Control" active,
REM les .exe non signes sont bloques. Le launcher .bat ci-dessous evite
REM ce probleme : il appelle directement le java.exe du runtime embarque,
REM ce qui n'est pas considere comme une nouvelle application a verifier.
echo [INFO] Creation du launcher .bat de fallback...
> "dist\STRMS\STRMS.bat" echo @echo off
>> "dist\STRMS\STRMS.bat" echo REM Lance STRMS via le runtime Java embarque (contourne Smart App Control).
>> "dist\STRMS\STRMS.bat" echo set DIR=%%~dp0
>> "dist\STRMS\STRMS.bat" echo "%%DIR%%runtime\bin\java.exe" -m fr.eseo.strms/fr.eseo.strms.ui.MainApp %%*

echo.
echo =====================================================
echo  [SUCCES] Distribution generee dans : dist\STRMS\
echo.
echo  Deux launchers sont disponibles :
echo    1) dist\STRMS\STRMS.exe  (executable natif)
echo    2) dist\STRMS\STRMS.bat  (fallback si l'exe est bloque
echo                              par Smart App Control de Windows 11)
echo.
echo  Distribution a vos collegues :
echo    Compressez le dossier "dist\STRMS" et envoyez-le.
echo    Apres extraction, ils peuvent double-cliquer sur STRMS.exe ;
echo    si Windows bloque, ils utilisent STRMS.bat.
echo  =====================================================
echo.

endlocal
