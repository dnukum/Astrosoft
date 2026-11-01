package app.astrosoft.core.test;

import junit.framework.TestCase;
import app.astrosoft.util.Version;
import java.util.regex.Pattern;

public class VersionTest extends TestCase {

    public void testVersionFormat() {
        String version = Version.getVersion();
        assertNotNull("Version should not be null", version);
        assertFalse("Version should not be empty", version.trim().isEmpty());

        // Either it matches the yyyyMMdd:HHmmss build timestamp pattern or Development Build
        Pattern pattern = Pattern.compile("^\\d{8}:\\d{6}$");
        boolean matchesTimestamp = pattern.matcher(version).matches();
        boolean isDevBuild = version.equals("Development Build");

        assertTrue("Version '" + version + "' should match yyyyMMdd:HHmmss or be Development Build",
                   matchesTimestamp || isDevBuild);
    }

    public void testDisplayVersion() {
        String displayVersion = Version.getDisplayVersion();
        assertNotNull("Display version should not be null", displayVersion);
        assertFalse("Display version should not be empty", displayVersion.trim().isEmpty());

        if (Version.getVersion().equals("Development Build")) {
            assertEquals("Development Build", displayVersion);
        } else {
            assertTrue("Display version should contain version id: " + displayVersion,
                    displayVersion.startsWith(Version.getVersion()));
            assertTrue("Display version should mention 'Built on': " + displayVersion,
                    displayVersion.contains("(Built on "));
        }
    }
}
