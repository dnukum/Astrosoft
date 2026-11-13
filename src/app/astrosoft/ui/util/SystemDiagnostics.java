package app.astrosoft.ui.util;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import app.astrosoft.pref.AstrosoftPref;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.util.Version;

/**
 * Collects runtime system, graphics, font, and log diagnostics to aid troubleshooting
 * user environments (e.g. Tamil font fallback, missing glyphs, platform DPI).
 */
public class SystemDiagnostics {

    private static final Logger log = Logger.getLogger(SystemDiagnostics.class.getName());

    public static class FontCandidateInfo {
        private final String fontName;
        private final boolean isInstalled;
        private final boolean canDisplayTamil;

        public FontCandidateInfo(String fontName, boolean isInstalled, boolean canDisplayTamil) {
            this.fontName = fontName;
            this.isInstalled = isInstalled;
            this.canDisplayTamil = canDisplayTamil;
        }

        public String getFontName() { return fontName; }
        public boolean isInstalled() { return isInstalled; }
        public boolean canDisplayTamil() { return canDisplayTamil; }

        @Override
        public String toString() {
            return String.format("%-22s : Installed=%-5s | Tamil Glyphs=%s", 
                    fontName, isInstalled ? "YES" : "NO", canDisplayTamil ? "YES" : "NO");
        }
    }

    /**
     * Resolves the primary log file (astrosoft.log) based on platform defaults and JUL properties.
     */
    public static File getLogFile() {
        // 1. Check explicit system property pattern if configured
        String pattern = System.getProperty("java.util.logging.FileHandler.pattern");
        if (pattern != null && !pattern.trim().isEmpty()) {
            String resolved = pattern.replace("%h", System.getProperty("user.home"))
                                     .replace("%t", System.getProperty("java.io.tmpdir"));
            File file = new File(resolved);
            if (file.exists() || file.getParentFile() != null && file.getParentFile().exists()) {
                return file;
            }
        }

        // 2. Check standard macOS location
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac")) {
            File macLog = new File(System.getProperty("user.home"), "Library/Logs/Astrosoft20/astrosoft.log");
            if (macLog.exists()) {
                return macLog;
            }
        }

        // 3. Check current working directory
        File localLog = new File(System.getProperty("user.dir"), "astrosoft.log");
        if (localLog.exists()) {
            return localLog;
        }

        // 4. Default to expected platform log target
        if (os.contains("mac")) {
            return new File(System.getProperty("user.home"), "Library/Logs/Astrosoft20/astrosoft.log");
        }
        return localLog;
    }

    /**
     * Resolves the missing translations log file if present.
     */
    public static File getMissingTranslationsLogFile() {
        return new File("logs/missing_translations.log");
    }

    /**
     * Gathers font candidate status across known macOS, Windows, and Linux Tamil fonts.
     */
    public static List<FontCandidateInfo> getCandidateFontStatus() {
        List<FontCandidateInfo> results = new ArrayList<>();
        Set<String> installedFamilies = new HashSet<>();
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            installedFamilies.addAll(Arrays.asList(ge.getAvailableFontFamilyNames()));
        } catch (Throwable t) {
            log.fine("Could not retrieve system font families: " + t.getMessage());
        }

        String testWord = "தமிழ்";
        String[] candidates = new String[] {
            "Noto Sans Tamil",
            "Tamil Sangam MN",
            "Tamil MN",
            "Nirmala UI",
            "Latha",
            "Vijaya",
            "Lohit Tamil",
            "Arial Unicode MS"
        };

        for (String candidate : candidates) {
            boolean installed = installedFamilies.contains(candidate);
            boolean canDisplay = false;
            if (installed) {
                try {
                    Font f = new Font(candidate, Font.PLAIN, 12);
                    canDisplay = (f.canDisplayUpTo(testWord) == -1);
                } catch (Throwable ignored) {}
            }
            results.add(new FontCandidateInfo(candidate, installed, canDisplay));
        }
        return results;
    }

    /**
     * Generates a comprehensive plain-text diagnostic report.
     */
    public static String generateReport() {
        StringBuilder sb = new StringBuilder();
        String testWord = "தமிழ்";

        sb.append("================================================================\n");
        sb.append("                 ASTROSOFT 2.0 SYSTEM DIAGNOSTICS               \n");
        sb.append("================================================================\n\n");

        // 1. Application & Version
        sb.append("[1] APPLICATION & BUILD\n");
        sb.append("  Version              : ").append(Version.getDisplayVersion()).append("\n");
        sb.append("  Application Name     : Astrosoft 2.0\n\n");

        // 2. Operating System & Hardware
        sb.append("[2] OPERATING SYSTEM\n");
        sb.append("  OS Name              : ").append(System.getProperty("os.name")).append("\n");
        sb.append("  OS Version           : ").append(System.getProperty("os.version")).append("\n");
        sb.append("  Architecture         : ").append(System.getProperty("os.arch")).append("\n");
        sb.append("  User Home            : ").append(System.getProperty("user.home")).append("\n");
        sb.append("  Working Dir          : ").append(System.getProperty("user.dir")).append("\n\n");

        // 3. Java Runtime
        sb.append("[3] JAVA RUNTIME\n");
        sb.append("  Java Version         : ").append(System.getProperty("java.version")).append("\n");
        sb.append("  Java Vendor          : ").append(System.getProperty("java.vendor")).append("\n");
        sb.append("  VM Name              : ").append(System.getProperty("java.vm.name")).append(" (").append(System.getProperty("java.vm.version")).append(")\n");
        sb.append("  Java Home            : ").append(System.getProperty("java.home")).append("\n\n");

        // 4. Graphics & Display
        sb.append("[4] DISPLAY & GRAPHICS\n");
        try {
            Dimension screenSize = AstroSoft.getScreenSize();
            sb.append("  Screen Size          : ").append(screenSize.width).append(" x ").append(screenSize.height).append("\n");
            
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            AffineTransform tx = gd.getDefaultConfiguration().getDefaultTransform();
            sb.append("  Display Scaling      : X=").append(String.format("%.2f", tx.getScaleX()))
              .append(", Y=").append(String.format("%.2f", tx.getScaleY())).append("\n");
            sb.append("  Total Font Families  : ").append(ge.getAvailableFontFamilyNames().length).append("\n");
        } catch (Throwable t) {
            sb.append("  Graphics Info Error  : ").append(t.getMessage()).append("\n");
        }
        sb.append("\n");

        // 5. Active Preferences
        sb.append("[5] ASTROSOFT PREFERENCES\n");
        try {
            AstrosoftPref pref = AstroSoft.getPreferences();
            if (pref != null) {
                sb.append("  Language             : ").append(pref.getLanguage()).append("\n");
                sb.append("  Ayanamsa             : ").append(pref.getAyanamsa()).append("\n");
                sb.append("  Rahu Node            : ").append(pref.getRahuNode()).append("\n");
                sb.append("  Chart Theme          : ").append(pref.getChartTheme()).append("\n");
                sb.append("  Chart Style          : ").append(pref.getChartStyle()).append("\n");
            }
        } catch (Throwable t) {
            sb.append("  Preferences Error    : ").append(t.getMessage()).append("\n");
        }
        sb.append("\n");

        // 6. Font Resolution Diagnostics
        sb.append("[6] FONT & TAMIL GLYPH DIAGNOSTICS\n");
        String resolvedFont = UIUtil.getTamilFontName();
        sb.append("  Resolved Tamil Font  : ").append(resolvedFont).append("\n");
        
        boolean resolvedCanDisplay = false;
        try {
            Font rf = new Font(resolvedFont, Font.PLAIN, 12);
            resolvedCanDisplay = (rf.canDisplayUpTo(testWord) == -1);
        } catch (Throwable ignored) {}
        sb.append("  Test Glyph Coverage  : ").append(testWord).append(" -> ")
          .append(resolvedCanDisplay ? "PASS (Glyphs supported)" : "FAIL (Missing glyphs / Tofu boxes)").append("\n\n");

        sb.append("  Candidate Font Inventory:\n");
        for (FontCandidateInfo info : getCandidateFontStatus()) {
            sb.append("    * ").append(info.toString()).append("\n");
        }
        sb.append("\n");

        // 7. Log Files
        sb.append("[7] LOG FILE STATUS\n");
        File logFile = getLogFile();
        sb.append("  Application Log File : ").append(logFile.getAbsolutePath()).append("\n");
        if (logFile.exists()) {
            sb.append("  Log File Size        : ").append(logFile.length()).append(" bytes (")
              .append(String.format("%.1f KB", logFile.length() / 1024.0)).append(")\n");
            sb.append("  Last Modified        : ").append(new java.util.Date(logFile.lastModified())).append("\n");
        } else {
            sb.append("  Log File Status      : Not created yet (will be created during session)\n");
        }

        File missingFile = getMissingTranslationsLogFile();
        if (missingFile.exists()) {
            sb.append("  Missing I18n Log     : ").append(missingFile.getAbsolutePath())
              .append(" (").append(missingFile.length()).append(" bytes)\n");
        }
        sb.append("================================================================\n");

        return sb.toString();
    }

    /**
     * Copies diagnostic report text to the system clipboard.
     */
    public static boolean copyToClipboard(String text) {
        try {
            StringSelection selection = new StringSelection(text);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            return true;
        } catch (Throwable t) {
            log.warning("Failed to copy diagnostics to clipboard: " + t.getMessage());
            return false;
        }
    }

    /**
     * Opens the folder containing the log file in the operating system's native file manager.
     */
    public static boolean openLogFolder() {
        File logFile = getLogFile();
        File folder = logFile != null && logFile.getParentFile() != null && logFile.getParentFile().exists()
                ? logFile.getParentFile()
                : new File(System.getProperty("user.dir"));

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(folder);
                return true;
            }
        } catch (Throwable t) {
            log.fine("Desktop.open failed, trying platform fallback: " + t.getMessage());
        }

        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", folder.getAbsolutePath()});
                return true;
            } else if (os.contains("windows")) {
                Runtime.getRuntime().exec(new String[]{"explorer.exe", folder.getAbsolutePath()});
                return true;
            } else if (os.contains("linux")) {
                Runtime.getRuntime().exec(new String[]{"xdg-open", folder.getAbsolutePath()});
                return true;
            }
        } catch (Throwable t) {
            log.warning("Platform open log folder failed: " + t.getMessage());
        }
        return false;
    }
}
