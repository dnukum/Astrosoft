package app.astrosoft.core.test;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;
import java.util.ResourceBundle;

import junit.framework.TestCase;
import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.Sex;
import app.astrosoft.core.Horoscope;

/**
 * Regression tests for locale-independent .ash DateTime persistence
 * and English resource bundle labels.
 */
public class HoroscopeLocaleRoundTripTest extends TestCase {

    private File tempDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tempDir = new File(System.getProperty("java.io.tmpdir"),
                "astrosoft_locale_test_" + System.currentTimeMillis());
        tempDir.mkdirs();
    }

    @Override
    protected void tearDown() throws Exception {
        if (tempDir != null && tempDir.exists()) {
            File[] files = tempDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            tempDir.delete();
        }
        super.tearDown();
    }

    public void testSaveAndOpenAshAcrossNonUsLocales() {
        String[] localeTags = {"en-US", "en-IN", "en-GB", "en-AU", "ta-IN", "ml-IN", "hi-IN", "fr-FR"};
        Locale original = Locale.getDefault();
        try {
            for (String tag : localeTags) {
                Locale.setDefault(Locale.forLanguageTag(tag));

                Place place = new Place("Sample City", "Sample State", "Sample Country",
                        10.31, 76.13, "Asia/Kolkata");
                BirthData bd = new BirthData("Sample Chart", Sex.Male,
                        11, Calendar.SEPTEMBER + 1, 1967, 4, 39, 0, place);
                Horoscope originalH = new Horoscope(bd);

                File ashFile = new File(tempDir, "sample_" + tag + ".ash");
                originalH.saveToFile(ashFile.getAbsolutePath());

                Horoscope loaded = Horoscope.createFromFile(ashFile.getAbsolutePath());
                assertNotNull("Horoscope should load under locale " + tag, loaded);
                assertEquals("Birth year mismatch under locale " + tag,
                        1967, loaded.getBirthData().year());
                assertEquals("Birth month mismatch under locale " + tag,
                        Calendar.SEPTEMBER + 1, loaded.getBirthData().month());
                assertEquals("Birth day mismatch under locale " + tag,
                        11, loaded.getBirthData().date());
                assertEquals("Birth hour mismatch under locale " + tag,
                        4, loaded.getBirthData().hour());
                assertEquals("Birth minute mismatch under locale " + tag,
                        39, loaded.getBirthData().minutes());
            }
        } finally {
            Locale.setDefault(original);
        }
    }

    public void testLagnaLabelSpellingInEnglishBundle() {
        ResourceBundle bundle = ResourceBundle.getBundle("resources.AstrosoftBundle", Locale.ENGLISH);
        assertEquals("Lagna", bundle.getString("LANGA_STR"));
    }
}
