#!/bin/bash

# Determine project directory from script location
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Dynamically point this specific script to Java 21 if installed
if [ -d "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
    export PATH="$JAVA_HOME/bin:$PATH"
elif /usr/libexec/java_home -v 21 >/dev/null 2>&1; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 21)
    export PATH="$JAVA_HOME/bin:$PATH"
fi

MACOS_ARGS=()
if [[ "$OSTYPE" == "darwin"* ]]; then
    mkdir -p "$HOME/Library/Logs/Astrosoft20"
    MACOS_ARGS+=("-Xdock:name=Astrosoft20" "-Dapple.awt.application.name=Astrosoft20")
    MACOS_ARGS+=("-Djava.util.logging.FileHandler.pattern=%h/Library/Logs/Astrosoft20/astrosoft.log")
    if [ -f "./resources/images/AppIcon.icns" ]; then
        MACOS_ARGS+=("-Xdock:icon=./resources/images/AppIcon.icns")
    fi
fi

java "${MACOS_ARGS[@]}" \
     --add-exports java.desktop/sun.awt=ALL-UNNAMED \
     --add-opens java.desktop/sun.awt=ALL-UNNAMED \
     --add-exports java.desktop/sun.awt.shell=ALL-UNNAMED \
     -Djava.util.logging.config.file=./bin/logging.properties \
     -Djava.util.prefs.PreferencesFactory=app.astrosoft.pref.UserPreferencesFactory \
     -cp "deploy:bin/AstroSoft.jar:lib/*" \
     app.astrosoft.ui.AstroSoft "$@"
