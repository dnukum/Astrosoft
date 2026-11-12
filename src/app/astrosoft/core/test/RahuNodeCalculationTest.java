package app.astrosoft.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.EnumMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import swisseph.SweDate;
import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.Ayanamsa;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.RahuNode;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.export.XMLHelper;
import app.astrosoft.pref.AstrosoftPref;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.util.SwissHelper;

public class RahuNodeCalculationTest {

    private RahuNode originalPreference;

    @Before
    public void setUp() {
        try {
            AstrosoftPref pref = AstroSoft.getPreferences();
            if (pref != null) {
                originalPreference = pref.getRahuNode();
            }
        } catch (Exception e) {
            // Ignore in headless test context
        }
    }

    @After
    public void tearDown() {
        try {
            AstrosoftPref pref = AstroSoft.getPreferences();
            if (pref != null && originalPreference != null) {
                pref.setRahuNode(originalPreference);
            }
        } catch (Exception e) {
            // Ignore in headless test context
        }
    }

    @Test
    public void testRahuNodeDefaultsAndPreferences() {
        assertEquals("Default RahuNode enum must be MEAN", RahuNode.MEAN, RahuNode.getDefault());
        assertEquals("Mean Node Swiss ID must be 10", 10, RahuNode.MEAN.swissPlanetNo());
        assertEquals("True Node Swiss ID must be 11", 11, RahuNode.TRUE.swissPlanetNo());

        AstrosoftPref pref = AstroSoft.getPreferences();
        if (pref != null) {
            pref.setRahuNode(RahuNode.TRUE);
            assertEquals(RahuNode.TRUE, pref.getRahuNode());

            pref.setRahuNode(RahuNode.MEAN);
            assertEquals(RahuNode.MEAN, pref.getRahuNode());
        }
    }

    @Test
    public void testKetuOppositionInvariant() {
        // Verify that Ketu is strictly (Rahu + 180°) % 360 in both MEAN and TRUE modes
        SwissHelper sh = new SwissHelper(2000, 8, 9, 12.0); // Aug 9, 2000 GMT
        sh.setAyanamsa(Ayanamsa.LAHARI);

        // Test with MEAN
        sh.setRahuNode(RahuNode.MEAN);
        EnumMap<Planet, Double> meanPos = sh.getPlanetaryPosition();
        double meanRahu = meanPos.get(Planet.Rahu);
        double meanKetu = meanPos.get(Planet.Ketu);
        double expectedMeanKetu = (meanRahu + 180.0) % 360.0;
        assertEquals("Ketu must be exactly 180° opposite Rahu (MEAN)", expectedMeanKetu, meanKetu, 0.000001);

        // Test with TRUE
        sh.setRahuNode(RahuNode.TRUE);
        EnumMap<Planet, Double> truePos = sh.getPlanetaryPosition();
        double trueRahu = truePos.get(Planet.Rahu);
        double trueKetu = truePos.get(Planet.Ketu);
        double expectedTrueKetu = (trueRahu + 180.0) % 360.0;
        assertEquals("Ketu must be exactly 180° opposite Rahu (TRUE)", expectedTrueKetu, trueKetu, 0.000001);
    }

    @Test
    public void testMeanNodeStrictlyNegativeSpeed() {
        // Mean Node velocity is mathematically guaranteed to be strictly negative
        SwissHelper sh = new SwissHelper(1999, 8, 20, 6.5);
        sh.setRahuNode(RahuNode.MEAN);
        double speed = sh.getPlanetSpeed(Planet.Rahu);
        assertTrue("Mean Node speed must be strictly negative", speed < 0.0);
        // Approximately -0.05295 deg/day
        assertEquals("Mean Node daily speed is approximately -0.053°/day", -0.05295, speed, 0.005);
    }

    @Test
    public void testDirectMotionOnTrueNode() {
        // Aug 20, 1999: True node is temporarily in direct motion (speed > 0)
        SwissHelper sh = new SwissHelper(1999, 8, 20, 6.5);

        sh.setRahuNode(RahuNode.MEAN);
        assertTrue("Mean node speed on Aug 20 1999 must be negative", sh.getPlanetSpeed(Planet.Rahu) < 0.0);

        sh.setRahuNode(RahuNode.TRUE);
        assertTrue("True node speed on Aug 20 1999 must be positive (Direct motion)", sh.getPlanetSpeed(Planet.Rahu) > 0.0);
    }

    @Test
    public void testBoundaryChartSignFlipAriesTaurus() {
        // Jan 31, 1985:
        // Mean Rahu is in Mesha (Aries, ~29.88°)
        // True Rahu flips into Vrishabha (Taurus, ~0.52°)
        String path = "tests/options/rahu_cusp_aries_taurus.ash";
        File file = new File(path);
        assertTrue("tests/options/rahu_cusp_aries_taurus.ash must exist", file.exists());

        Horoscope h = Horoscope.createFromFile(path);
        assertNotNull("Horoscope should load successfully", h);

        // Under MEAN node (default):
        h.setRahuNode(RahuNode.MEAN);
        double meanRahu = h.getPlanetaryPosition(Planet.Rahu);
        Rasi meanRahuRasi = Rasi.ofDeg(meanRahu);
        assertEquals("Mean Rahu must be in Mesha (Aries)", Rasi.Mesha, meanRahuRasi);
        assertTrue("Mean Rahu must be near end of Mesha (> 29°)", (meanRahu % 30.0) > 29.0);

        // Ketu under MEAN node must be in Thula (Libra)
        double meanKetu = h.getPlanetaryPosition(Planet.Ketu);
        assertEquals("Mean Ketu must be in Thula (Libra)", Rasi.Thula, Rasi.ofDeg(meanKetu));

        // Under TRUE node (option):
        h.setRahuNode(RahuNode.TRUE);
        double trueRahu = h.getPlanetaryPosition(Planet.Rahu);
        Rasi trueRahuRasi = Rasi.ofDeg(trueRahu);
        assertEquals("True Rahu must cross sign boundary into Vrishabha (Taurus)", Rasi.Vrishabha, trueRahuRasi);
        assertTrue("True Rahu must be near start of Vrishabha (< 1°)", (trueRahu % 30.0) < 1.0);

        // Ketu under TRUE node must flip into Vrichika (Scorpio)
        double trueKetu = h.getPlanetaryPosition(Planet.Ketu);
        assertEquals("True Ketu must cross sign boundary into Vrichika (Scorpio)", Rasi.Vrichika, Rasi.ofDeg(trueKetu));

        // Switch back to MEAN node: verify idempotent state restoration
        h.setRahuNode(RahuNode.MEAN);
        assertEquals("Switching back to Mean node restores Mesha", Rasi.Mesha, Rasi.ofDeg(h.getPlanetaryPosition(Planet.Rahu)));
    }

    @Test
    public void testBoundaryChartSignFlipPiscesAquarius() {
        // Feb 29, 1988:
        // Mean Rahu is in Meena (Pisces, ~0.35°)
        // True Rahu flips into Kumbha (Aquarius, ~29.45°)
        String path = "tests/options/rahu_cusp_pisces_aquarius.ash";
        File file = new File(path);
        assertTrue("tests/options/rahu_cusp_pisces_aquarius.ash must exist", file.exists());

        Horoscope h = Horoscope.createFromFile(path);
        assertNotNull("Horoscope should load successfully", h);

        // MEAN node:
        h.setRahuNode(RahuNode.MEAN);
        double meanRahu = h.getPlanetaryPosition(Planet.Rahu);
        assertEquals("Mean Rahu must be in Meena (Pisces)", Rasi.Meena, Rasi.ofDeg(meanRahu));

        // TRUE node:
        h.setRahuNode(RahuNode.TRUE);
        double trueRahu = h.getPlanetaryPosition(Planet.Rahu);
        assertEquals("True Rahu must cross backward into Kumbha (Aquarius)", Rasi.Kumbha, Rasi.ofDeg(trueRahu));
    }

    @Test
    public void testRahuMeanTestChart() {
        // Rahu_mean_Test: Mar 5, 1971 02:12 AM at Vijayawada (16N30, 80E38, IST)
        // True Node: Rahu = 00:01 Kumbha (Aquarius), Ketu = 00:01 Simha (Leo)
        // Mean Node: Rahu flips across 0° cusp into Makara (Capricorn 29° 10'), Ketu into Kataka (Cancer 29° 10')
        String path = "tests/options/Rahu_mean_Test.ash";
        File file = new File(path);
        assertTrue("tests/options/Rahu_mean_Test.ash must exist", file.exists());

        Horoscope h = Horoscope.createFromFile(path);
        assertNotNull("Rahu_mean_Test chart should load", h);

        // TRUE Node mode:
        h.setRahuNode(RahuNode.TRUE);
        double trueRahu = h.getPlanetaryPosition(Planet.Rahu);
        double trueKetu = h.getPlanetaryPosition(Planet.Ketu);
        assertEquals("True Rahu is in Kumbha (Aquarius)", Rasi.Kumbha, Rasi.ofDeg(trueRahu));
        assertTrue("True Rahu is at 00:01 Kumbha (< 0.1°)", (trueRahu % 30.0) < 0.1);
        assertEquals("True Ketu is in Simha (Leo)", Rasi.Simha, Rasi.ofDeg(trueKetu));

        // MEAN Node mode (Default):
        h.setRahuNode(RahuNode.MEAN);
        double meanRahu = h.getPlanetaryPosition(Planet.Rahu);
        double meanKetu = h.getPlanetaryPosition(Planet.Ketu);
        assertEquals("Mean Rahu flips across boundary into Makara (Capricorn)", Rasi.Makara, Rasi.ofDeg(meanRahu));
        assertTrue("Mean Rahu is at ~29° 10' Makara (> 29.0°)", (meanRahu % 30.0) > 29.0);
        assertEquals("Mean Ketu flips into Kataka (Cancer)", Rasi.Kataka, Rasi.ofDeg(meanKetu));
        assertTrue("Mean Ketu is at ~29° 10' Kataka (> 29.0°)", (meanKetu % 30.0) > 29.0);
    }

    @Test
    public void testNonBoundaryChartsMainPath() {
        // For non-boundary charts (~96.7% of all horoscopes), Rahu and Ketu remain in the
        // identical Rasi in both Mean and True modes, differing only by a minor perturbation (< 1.85°).
        String[] sampleFiles = {
            "samples/Test.ash",
            "samples/raja.ash",
            "samples/A.Elango.ash"
        };

        for (String samplePath : sampleFiles) {
            File f = new File(samplePath);
            if (!f.exists()) {
                continue;
            }

            Horoscope h = Horoscope.createFromFile(samplePath);
            assertNotNull("Horoscope should load: " + samplePath, h);

            // Compute with MEAN node (default)
            h.setRahuNode(RahuNode.MEAN);
            double meanRahu = h.getPlanetaryPosition(Planet.Rahu);
            double meanKetu = h.getPlanetaryPosition(Planet.Ketu);
            Rasi meanRahuRasi = Rasi.ofDeg(meanRahu);
            Rasi meanKetuRasi = Rasi.ofDeg(meanKetu);

            // Compute with TRUE node (option)
            h.setRahuNode(RahuNode.TRUE);
            double trueRahu = h.getPlanetaryPosition(Planet.Rahu);
            double trueKetu = h.getPlanetaryPosition(Planet.Ketu);
            Rasi trueRahuRasi = Rasi.ofDeg(trueRahu);
            Rasi trueKetuRasi = Rasi.ofDeg(trueKetu);

            // In main path non-boundary charts:
            // 1. Rasi sign for Rahu must be identical
            assertEquals("Rahu sign must be invariant in non-boundary chart: " + samplePath, meanRahuRasi, trueRahuRasi);
            // 2. Rasi sign for Ketu must be identical
            assertEquals("Ketu sign must be invariant in non-boundary chart: " + samplePath, meanKetuRasi, trueKetuRasi);
            // 3. Absolute perturbation difference must be small (< 1.85 degrees)
            double diff = Math.abs(meanRahu - trueRahu);
            if (diff > 180.0) {
                diff = 360.0 - diff;
            }
            assertTrue("Perturbation difference between Mean and True Rahu must be < 1.85°: " + diff, diff < 1.85);
        }
    }
}
