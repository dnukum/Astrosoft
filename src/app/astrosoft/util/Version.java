package app.astrosoft.util;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides application build version and timestamp.
 */
public class Version {

    private static final Logger log = Logger.getLogger(Version.class.getName());
    private static String version = null;
    private static String buildDate = null;

    public static synchronized String getVersion() {
        if (version != null) {
            return version;
        }
        loadVersionInfo();
        return version;
    }

    public static synchronized String getBuildDate() {
        if (buildDate != null) {
            return buildDate;
        }
        loadVersionInfo();
        return buildDate;
    }

    /**
     * Returns a human-friendly version string including formatted build date,
     * e.g., "20261112:053924-PST (Built on Nov 12, 2026 at 05:39:24 PST)" or "Development Build".
     */
    public static synchronized String getDisplayVersion() {
        String ver = getVersion();
        String dateStr = getBuildDate();

        if (ver == null || ver.equals("Development Build")) {
            return "Development Build";
        }

        String formatted = formatDisplay(ver, dateStr,
                new String[] { "MM/dd/yyyy HH:mm:ss z", "MM/dd/yyyy HH:mm:ss" });
        if (formatted != null) {
            return formatted;
        }

        formatted = formatDisplay(ver, ver,
                new String[] { "yyyyMMdd:HHmmss-z", "yyyyMMdd:HHmmss" });
        if (formatted != null) {
            return formatted;
        }

        return ver;
    }

    private static String formatDisplay(String ver, String raw, String[] inPatterns) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String trimmed = raw.trim();
        for (String inPattern : inPatterns) {
            try {
                java.text.SimpleDateFormat inFmt = new java.text.SimpleDateFormat(inPattern, java.util.Locale.US);
                java.util.Date d = inFmt.parse(trimmed);
                boolean hasTz = inPattern.endsWith("z");
                String outPattern = hasTz ? "MMM d, yyyy 'at' HH:mm:ss z" : "MMM d, yyyy 'at' HH:mm:ss";
                java.text.SimpleDateFormat outFmt = new java.text.SimpleDateFormat(outPattern, java.util.Locale.US);
                if (hasTz) {
                    outFmt.setTimeZone(inFmt.getTimeZone());
                }
                return ver + " (Built on " + outFmt.format(d) + ")";
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static void loadVersionInfo() {
        // 1. Try reading from /resources/version.properties
        try (InputStream in = Version.class.getResourceAsStream("/resources/version.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                String val = props.getProperty("build.version");
                if (val != null && !val.trim().isEmpty()) {
                    version = val.trim();
                }
                String dVal = props.getProperty("build.date");
                if (dVal != null && !dVal.trim().isEmpty()) {
                    buildDate = dVal.trim();
                }
            }
        } catch (Exception e) {
            log.log(Level.FINE, "Could not load version from /resources/version.properties", e);
        }

        // 2. Fallback to /version.properties
        if (version == null) {
            try (InputStream in = Version.class.getResourceAsStream("/version.properties")) {
                if (in != null) {
                    Properties props = new Properties();
                    props.load(in);
                    String val = props.getProperty("build.version");
                    if (val != null && !val.trim().isEmpty()) {
                        version = val.trim();
                    }
                    String dVal = props.getProperty("build.date");
                    if (dVal != null && !dVal.trim().isEmpty()) {
                        buildDate = dVal.trim();
                    }
                }
            } catch (Exception e) {
                log.log(Level.FINE, "Could not load version from /version.properties", e);
            }
        }

        // 3. Fallback to MANIFEST.MF Implementation-Version
        if (version == null) {
            Package pkg = Version.class.getPackage();
            if (pkg != null) {
                String implVer = pkg.getImplementationVersion();
                if (implVer != null && !implVer.trim().isEmpty()) {
                    version = implVer.trim();
                }
            }
        }

        // 4. Default fallback if not found (e.g. running unpackaged prior to first build)
        if (version == null) {
            version = "Development Build";
        }
    }

    /**
     * Resets the cached version (primarily used for unit testing).
     */
    public static synchronized void resetCache() {
        version = null;
        buildDate = null;
    }
}
