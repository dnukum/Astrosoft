#!/bin/bash

# Dynamically point this specific script to Java 21 if installed
if /usr/libexec/java_home -v 21 >/dev/null 2>&1; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 21)
    export PATH="$JAVA_HOME/bin:$PATH"
fi

java --add-exports java.desktop/sun.awt=ALL-UNNAMED --add-opens java.desktop/sun.awt=ALL-UNNAMED --add-exports java.desktop/sun.awt.shell=ALL-UNNAMED -Djava.util.logging.config.file=./bin/logging.properties -Djava.util.prefs.PreferencesFactory=app.astrosoft.pref.UserPreferencesFactory -jar bin/AstroSoft.jar "$@"
