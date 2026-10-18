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

    public static synchronized String getVersion() {
        if (version != null) {
            return version;
        }

        // 1. Try reading from /resources/version.properties
        try (InputStream in = Version.class.getResourceAsStream("/resources/version.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                String val = props.getProperty("build.version");
                if (val != null && !val.trim().isEmpty()) {
                    version = val.trim();
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

        return version;
    }

    /**
     * Resets the cached version (primarily used for unit testing).
     */
    public static synchronized void resetCache() {
        version = null;
    }
}
