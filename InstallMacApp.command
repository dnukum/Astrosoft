#!/bin/bash
set -e

echo "========================================================"
echo "  Astrosoft20 - macOS Application Installer"
echo "========================================================"

# Resolve source directory where this script lives
SOURCE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SOURCE_DIR"

# Parse command line options
TERMINAL_DEBUG=false
INSTALL_DIR=""

for arg in "$@"; do
    case "$arg" in
        -T|--terminal|-t)
            TERMINAL_DEBUG=true
            ;;
        -h|--help)
            echo "Usage: $0 [-T] [target_applications_dir]"
            echo "Options:"
            echo "  -T, --terminal  Enable Terminal Debug Mode: launches Astrosoft with a live Terminal window"
            echo "                  spewing all logs and errors for easy troubleshooting."
            exit 0
            ;;
        *)
            if [ -z "$INSTALL_DIR" ]; then
                INSTALL_DIR="$arg"
            fi
            ;;
    esac
done

# Determine target Applications directory
if [ -z "$INSTALL_DIR" ]; then
    if [ -w "/Applications" ]; then
        INSTALL_DIR="/Applications"
    else
        INSTALL_DIR="$HOME/Applications"
        mkdir -p "$INSTALL_DIR"
    fi
fi

APP_NAME="Astrosoft20"
APP_BUNDLE="$INSTALL_DIR/${APP_NAME}.app"
CONTENTS_DIR="$APP_BUNDLE/Contents"
MACOS_DIR="$CONTENTS_DIR/MacOS"
RESOURCES_DIR="$CONTENTS_DIR/Resources"

echo "Installing to: $APP_BUNDLE"
if [ "$TERMINAL_DEBUG" = "true" ]; then
    echo "  [Terminal Debug Mode: ENABLED (-T)]"
    echo "  -> App will launch with a visible Terminal window streaming all logs & errors."
fi

# Ensure standard user log directory exists (if permitted)
mkdir -p "$HOME/Library/Logs/Astrosoft20" 2>/dev/null || true

# Check for existing installation and handle custom .ash files
if [ -d "$APP_BUNDLE" ]; then
    # Look for any .ash files outside of the samples folder
    CUSTOM_ASH_FILES=$(find "$APP_BUNDLE" -name "*.ash" ! -path "*/samples/*" 2>/dev/null || true)
    
    if [ -n "$CUSTOM_ASH_FILES" ]; then
        echo ""
        echo "⚠️  WARNING: Found custom horoscope (.ash) files in existing installation:"
        echo "$CUSTOM_ASH_FILES"
        echo ""
        
        # Ask user via terminal prompt, or fallback to AppleScript dialog if double-clicked in Finder without tty
        CONFIRMED=""
        if [ -n "$FORCE_OVERWRITE" ]; then
            CONFIRMED="$FORCE_OVERWRITE"
        elif [ -t 0 ]; then
            read -p "Do you want to delete the existing installation and overwrite these files? (y/N): " CONFIRMED
        elif command -v osascript >/dev/null 2>&1; then
            GUI_CHOICE=$(osascript -e 'button returned of (display dialog "Custom horoscope (.ash) files were found in the existing Astrosoft20 installation.\n\nDo you want to delete and overwrite the existing installation?" buttons {"Cancel", "Delete & Overwrite"} default button "Cancel" with icon caution)' 2>/dev/null || echo "Cancel")
            if [ "$GUI_CHOICE" = "Delete & Overwrite" ]; then
                CONFIRMED="y"
            fi
        fi
        
        case "$CONFIRMED" in
            [yY]|[yY][eE][sS])
                echo "Proceeding with removal of previous installation..."
                ;;
            *)
                echo "❌ Installation cancelled to protect your .ash files."
                echo "Please backup your .ash files from $APP_BUNDLE before reinstalling."
                exit 0
                ;;
        esac
    fi
    rm -rf "$APP_BUNDLE"
fi

mkdir -p "$MACOS_DIR" "$RESOURCES_DIR"

# 1. Copy runtime directories and files into Contents/Resources (Option 1B: Self-Contained Bundle)
for dir in bin lib resources db ephe123 fonts samples deploy; do
    if [ -d "$SOURCE_DIR/$dir" ]; then
        echo "  Copying $dir/ ..."
        cp -R "$SOURCE_DIR/$dir" "$RESOURCES_DIR/"
    fi
done

for file in RunAstroSoft20.sh AstroSoft.policy names.csv pdfGen.xsl simple.fo server.properties; do
    if [ -f "$SOURCE_DIR/$file" ]; then
        cp "$SOURCE_DIR/$file" "$RESOURCES_DIR/"
    fi
done

chmod +x "$RESOURCES_DIR/RunAstroSoft20.sh" 2>/dev/null || true
chmod +x "$RESOURCES_DIR/bin/RunAstroSoft20.sh" 2>/dev/null || true

# 2. Set up macOS App Icon (AppIcon.icns)
if [ -f "$SOURCE_DIR/resources/images/AppIcon.icns" ]; then
    cp "$SOURCE_DIR/resources/images/AppIcon.icns" "$RESOURCES_DIR/AppIcon.icns"
elif [ -f "$SOURCE_DIR/resources/images/Planetary Positions.gif" ] && command -v sips >/dev/null 2>&1 && command -v iconutil >/dev/null 2>&1; then
    ICONSET_TMP="$(mktemp -d)/Astrosoft20.iconset"
    mkdir -p "$ICONSET_TMP"
    SRC_IMG="$SOURCE_DIR/resources/images/Planetary Positions.gif"
    for size in 16 32 128 256 512; do
        sips -s format png -z $size $size "$SRC_IMG" --out "$ICONSET_TMP/icon_${size}x${size}.png" >/dev/null 2>&1
        size2x=$((size * 2))
        sips -s format png -z $size2x $size2x "$SRC_IMG" --out "$ICONSET_TMP/icon_${size}x${size}@2x.png" >/dev/null 2>&1
    done
    iconutil -c icns "$ICONSET_TMP" -o "$RESOURCES_DIR/AppIcon.icns" >/dev/null 2>&1 || true
    rm -rf "$(dirname "$ICONSET_TMP")"
fi

# 3. Create Contents/Info.plist
cat > "$CONTENTS_DIR/Info.plist" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key>
    <string>Astrosoft20</string>
    <key>CFBundleDisplayName</key>
    <string>Astrosoft20</string>
    <key>CFBundleIdentifier</key>
    <string>app.astrosoft.ui.Astrosoft20</string>
    <key>CFBundleVersion</key>
    <string>2.0</string>
    <key>CFBundleShortVersionString</key>
    <string>2.0</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleExecutable</key>
    <string>Astrosoft20</string>
    <key>CFBundleIconFile</key>
    <string>AppIcon</string>
    <key>LSMinimumSystemVersion</key>
    <string>11.0</string>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
EOF

# 4. Create Contents/MacOS/Astrosoft20 executable launcher
cat > "$MACOS_DIR/Astrosoft20" << EOF
#!/bin/bash

RESOURCES_DIR="\$(cd "\$(dirname "\$0")/../Resources" && pwd)"
cd "\$RESOURCES_DIR"

# Check if terminal debug mode is enabled by install default ($TERMINAL_DEBUG) or runtime argument (-T)
TERMINAL_MODE="${TERMINAL_DEBUG}"
FILTERED_ARGS=()
for a in "\$@"; do
    if [ "\$a" = "-T" ] || [ "\$a" = "--terminal" ]; then
        TERMINAL_MODE=true
    else
        FILTERED_ARGS+=("\$a")
    fi
done

# If Terminal mode is active and not running in a terminal (e.g. launched from Finder/Dock),
# launch inside a visible macOS Terminal window to spew all output & errors live!
if [ "\$TERMINAL_MODE" = "true" ] && [ ! -t 1 ]; then
    osascript << APPLESCRIPT
tell application "Terminal"
    activate
    do script "cd '\$RESOURCES_DIR' && ./RunAstroSoft20.sh \${FILTERED_ARGS[*]}"
end tell
APPLESCRIPT
    exit 0
fi

# Ensure log directory exists
mkdir -p "\$HOME/Library/Logs/Astrosoft20"

# Locate Java 21
if [ -d "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
    export PATH="\$JAVA_HOME/bin:\$PATH"
elif /usr/libexec/java_home -v 21 >/dev/null 2>&1; then
    export JAVA_HOME=\$(/usr/libexec/java_home -v 21)
    export PATH="\$JAVA_HOME/bin:\$PATH"
elif [ -d "/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
    export PATH="\$JAVA_HOME/bin:\$PATH"
elif [ -d "/usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home" ]; then
    export JAVA_HOME="/usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
    export PATH="\$JAVA_HOME/bin:\$PATH"
fi

if ! command -v java >/dev/null 2>&1; then
    osascript -e 'display alert "Java 21 Required" message "Astrosoft20 requires Java 21 LTS (Eclipse Adoptium Temurin 21). Please install Java 21 and try again." as critical'
    exit 1
fi

exec ./RunAstroSoft20.sh "\${FILTERED_ARGS[@]}"
EOF

chmod +x "$MACOS_DIR/Astrosoft20"

# 5. Clear macOS Gatekeeper quarantine attributes and refresh Finder icon
xattr -cr "$APP_BUNDLE" 2>/dev/null || true
touch "$APP_BUNDLE"

echo ""
echo "✅ Successfully installed Astrosoft20 to: $APP_BUNDLE"
echo "👉 You can now launch Astrosoft20 from Applications or drag it to your Dock!"
echo ""

if [ "$TERMINAL_DEBUG" = "true" ]; then
    echo "🚀 Launching Astrosoft20 in a new Terminal window with live debugging..."
    osascript << APPLESCRIPT
tell application "Terminal"
    activate
    do script "cd '$RESOURCES_DIR' && ./RunAstroSoft20.sh"
end tell
APPLESCRIPT
else
    # Reveal the installed application in Finder
    if command -v open >/dev/null 2>&1; then
        open -R "$APP_BUNDLE" 2>/dev/null || true
    fi
fi

