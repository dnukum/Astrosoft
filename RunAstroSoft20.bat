@echo off
setlocal

rem Determine project root directory (current directory of this script)
set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

rem 1. Check for JDK 21 in standard installation paths
set "JAVA_EXE="

if exist "C:\Program Files\Eclipse Adoptium\jdk-21*\bin\javaw.exe" (
    for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do set "JAVA_EXE=%%D\bin\javaw.exe"
) else if exist "C:\Program Files\Java\jdk-21*\bin\javaw.exe" (
    for /d %%D in ("C:\Program Files\Java\jdk-21*") do set "JAVA_EXE=%%D\bin\javaw.exe"
) else if exist "C:\Program Files\Microsoft\jdk-21*\bin\javaw.exe" (
    for /d %%D in ("C:\Program Files\Microsoft\jdk-21*") do set "JAVA_EXE=%%D\bin\javaw.exe"
) else if exist "C:\Program Files\Amazon Corretto\jdk21*\bin\javaw.exe" (
    for /d %%D in ("C:\Program Files\Amazon Corretto\jdk21*") do set "JAVA_EXE=%%D\bin\javaw.exe"
) else if exist "%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-21*\bin\javaw.exe" (
    for /d %%D in ("%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-21*") do set "JAVA_EXE=%%D\bin\javaw.exe"
)

rem 2. If no specific JDK 21 path found, check if JAVA_HOME points to a valid javaw
if not defined JAVA_EXE (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVA_EXE=%JAVA_HOME%\bin\javaw.exe"
    )
)

rem 3. Fall back to javaw in PATH
if not defined JAVA_EXE (
    set "JAVA_EXE=javaw.exe"
)

rem 4. Launch Astrosoft using isolated javaw without keeping console window open
start "" "%JAVA_EXE%" ^
     --add-exports java.desktop/sun.awt=ALL-UNNAMED ^
     --add-opens java.desktop/sun.awt=ALL-UNNAMED ^
     --add-exports java.desktop/sun.awt.shell=ALL-UNNAMED ^
     -Djava.util.logging.config.file=./bin/logging.properties ^
     -Djava.util.prefs.PreferencesFactory=app.astrosoft.pref.UserPreferencesFactory ^
     -jar bin/AstroSoft.jar %*
