/**
 * TamilFontSupportTest.java
 * Verifies that Tamil astrological terms and bundle messages resolve to proper UTF-8 Unicode,
 * and that UI renderers and Charts display them without glyph omission (-1).
 */
package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.util.Date;
import java.util.EnumMap;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import app.astrosoft.core.Horoscope;
import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.DisplayFormat;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Thithi;
import app.astrosoft.consts.Yoga;
import app.astrosoft.consts.AmirthathiYoga;
import app.astrosoft.consts.Varga;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.AstrosoftTabbedPane;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.comp.modern.ModernBirthDataPanel;
import app.astrosoft.ui.dlg.DefaultTransitChartComputation;
import app.astrosoft.ui.dlg.TransitChartDialog;
import app.astrosoft.ui.table.PlanetCellRenderer;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.ui.view.MyMuhurtaView;
import app.astrosoft.util.Internalization;

public class TamilFontSupportTest {

    private Language originalLanguage;

    @Before
    public void setUp() {
        originalLanguage = AstroSoft.getPreferences().getLanguage();
    }

    @After
    public void tearDown() {
        if (originalLanguage != null) {
            AstroSoft.getPreferences().setLanguage(originalLanguage);
        }
    }

    @Test
    public void testTamilFontResolution() {
        Font tamilFont = UIUtil.getFont(Language.TAMIL, Font.PLAIN, 12);
        assertNotNull("Resolved Tamil font must not be null", tamilFont);
        assertEquals(-1, tamilFont.canDisplayUpTo("சூரியன்"));
        String resolvedName = UIUtil.getTamilFontName();
        assertNotNull("Resolved Tamil font name must not be null", resolvedName);
    }

    @Test
    public void testTamilGlyphCoverageForCoreAstrologicalTerms() {
        Font tamilFont = UIUtil.getFont(Language.TAMIL, Font.PLAIN, 12);

        // Core Rasi names in Tamil Unicode
        for (Rasi r : Rasi.values()) {
            String tamilRasi = r.toString(Language.TAMIL);
            assertNotNull("Tamil name for " + r + " must not be null", tamilRasi);
            int unrenderableIndex = tamilFont.canDisplayUpTo(tamilRasi);
            assertEquals("All glyphs in " + r + " ('" + tamilRasi + "') must be displayable",
                    -1, unrenderableIndex);
        }

        // Core Planets in Tamil Unicode
        AstroSoft.getPreferences().setLanguage(Language.TAMIL);
        for (Planet p : Planet.planetsAsc()) {
            String fullName = p.toString();
            String symbol = p.toString(DisplayFormat.SYMBOL);
            assertNotNull("Tamil full name for " + p + " must not be null", fullName);
            assertEquals("All glyphs in " + p + " ('" + fullName + "') must be displayable",
                    -1, tamilFont.canDisplayUpTo(fullName));
            assertEquals("All glyphs in symbol for " + p + " ('" + symbol + "') must be displayable",
                    -1, tamilFont.canDisplayUpTo(symbol));
        }

        // Core Display Strings in Tamil Unicode
        DisplayStrings[] sampleStrings = {
            DisplayStrings.ASHTAVARGA_STR,
            DisplayStrings.NAK_STR,
            DisplayStrings.NAKPADA_STR,
            DisplayStrings.SUNRISE_SET_STR,
            DisplayStrings.AYANAMSA_STR,
            DisplayStrings.SID_TIME_STR
        };

        for (DisplayStrings ds : sampleStrings) {
            String val = ds.toString(Language.TAMIL);
            assertNotNull("Tamil string for " + ds + " must not be null", val);
            int unrenderableIndex = tamilFont.canDisplayUpTo(val);
            assertEquals("All glyphs in " + ds + " ('" + val + "') must be displayable",
                    -1, unrenderableIndex);
        }
    }

    @Test
    public void testChartAndPlanetCellRendererUseTamilFontWhenTamilSelected() {
        AstroSoft.getPreferences().setLanguage(Language.TAMIL);

        // 1. PlanetCellRenderer must resolve font and render Tamil symbols cleanly
        PlanetCellRenderer pcr = new PlanetCellRenderer(new DefaultTableCellRenderer());
        JTable dummyTable = new JTable();
        dummyTable.setFont(UIUtil.getFont(Font.BOLD, 12));
        for (Planet p : Planet.planetsAsc()) {
            Component c = pcr.getTableCellRendererComponent(dummyTable, p, false, false, 0, 0);
            assertTrue("Renderer component must be JLabel", c instanceof JLabel);
            JLabel lbl = (JLabel) c;
            assertEquals("Planet cell text for " + p + " ('" + lbl.getText() + "') must be displayable",
                    -1, lbl.getFont().canDisplayUpTo(lbl.getText()));
        }

        // 2. Chart center badge title must render Tamil title
        EnumMap<Planet, Integer> positions = new EnumMap<>(Planet.class);
        EnumMap<Planet, Boolean> directions = new EnumMap<>(Planet.class);
        for (Planet p : Planet.planetsAsc()) {
            positions.put(p, 1);
            directions.put(p, false);
        }
        Chart rasiChart = new Chart(new PlanetChartData(Varga.Rasi, positions, directions), new Dimension(400, 400));
        rasiChart.setTitleBadge(UIConsts.NAVY_BADGE_BG, UIConsts.NAVY_BADGE_BG, Color.WHITE);
        String chartTitle = rasiChart.getChartData().getChartName();
        Font titleFont = UIUtil.getFont(Font.BOLD, 12);
        assertEquals("Chart title ('" + chartTitle + "') must be displayable",
                -1, titleFont.canDisplayUpTo(chartTitle));
    }

    @Test
    public void testSubscreensAndPanelsTamilCoverage() {
        AstroSoft.getPreferences().setLanguage(Language.TAMIL);
        Font tamilFont = UIUtil.getFont(Font.BOLD, 12);

        // 1. Birth and Input dialog strings
        assertEquals("பெயர்", DisplayStrings.NAME_STR.toString());
        assertEquals("தேதி", DisplayStrings.DATE_STR.toString());
        assertEquals("பிறந்த நேரம்", DisplayStrings.BIRTH_TIME_STR.toString());
        assertEquals("ராசி", DisplayStrings.RASI_STR.toString());
        assertEquals("நட்சத்திரம்", DisplayStrings.NAK_STR.toString());

        // 2. Compatibility and Shadbala strings
        assertEquals("பொருத்தம்", DisplayStrings.KUTA_STR.toString());
        assertEquals("தோஷம்", DisplayStrings.DOSHA_STR.toString());
        assertEquals("ஷட்பலம்", AstrosoftTableColumn.ShadBala.toString());
        assertEquals("பாவ பலம்", AstrosoftTableColumn.BhavaBala.toString());
        assertEquals("ஸ்தான பலம்", AstrosoftTableColumn.SthanaBala.toString());
        assertEquals("கால பலம்", AstrosoftTableColumn.KalaBala.toString());

        // 3. Tabbed pane font
        AstrosoftTabbedPane tp = new AstrosoftTabbedPane();
        assertEquals(-1, tp.getFont().canDisplayUpTo("ஷட்பலம்"));

        // 4. Titled border font
        javax.swing.border.Border b = UIConsts.getTitleBorder("சோதனை");
        assertTrue(b instanceof javax.swing.border.TitledBorder);
        Font borderFont = ((javax.swing.border.TitledBorder) b).getTitleFont();
        assertNotNull(borderFont);
        assertEquals(-1, borderFont.canDisplayUpTo("சோதனை"));
    }

    @Test
    public void testTransitChartDialogTamilAndEnglishLocalization() {
        AstroSoft frame = new AstroSoft();
        Place place = AstroSoft.getPreferences().getPlace();

        try {
            // 1. Tamil Mode
            AstroSoft.getPreferences().setLanguage(Language.TAMIL);
            TransitChartDialog dialogTa = new TransitChartDialog(frame, new Date(), place, new DefaultTransitChartComputation());
            assertEquals("மாற்று...", dialogTa.getEditButton().getText());
            assertEquals("விரிவாக்கு...", dialogTa.getMoreButton().getText());
            assertEquals("புதுப்பி", dialogTa.getUpdateButton().getText());
            assertEquals("கோசார ராசி", dialogTa.getChart().getChartData().getChartName());

            // Check glyph rendering capability
            Font font = dialogTa.getEditButton().getFont();
            assertEquals(-1, font.canDisplayUpTo(dialogTa.getEditButton().getText()));
            assertEquals(-1, font.canDisplayUpTo(dialogTa.getMoreButton().getText()));
            assertEquals(-1, font.canDisplayUpTo(dialogTa.getUpdateButton().getText()));
            assertEquals(-1, font.canDisplayUpTo(dialogTa.getChart().getChartData().getChartName()));

            dialogTa.toggleMore();
            assertEquals("சுருக்கு...", dialogTa.getMoreButton().getText());
            assertEquals(-1, font.canDisplayUpTo(dialogTa.getMoreButton().getText()));
            dialogTa.dispose();

            // 2. English Mode
            AstroSoft.getPreferences().setLanguage(Language.ENGLISH);
            TransitChartDialog dialogEn = new TransitChartDialog(frame, new Date(), place, new DefaultTransitChartComputation());
            assertEquals("Edit...", dialogEn.getEditButton().getText());
            assertEquals("More...", dialogEn.getMoreButton().getText());
            assertEquals("Update", dialogEn.getUpdateButton().getText());
            assertEquals("Transit (D1)", dialogEn.getChart().getChartData().getChartName());

            dialogEn.toggleMore();
            assertEquals("Less...", dialogEn.getMoreButton().getText());
            dialogEn.dispose();
        } finally {
            frame.dispose();
            AstroSoft.getPreferences().setLanguage(originalLanguage);
        }
    }

    @Test
    public void testMyMuhurtaViewTamilAndEnglishLocalization() {
        try {
            // 1. Tamil Mode
            AstroSoft.getPreferences().setLanguage(Language.TAMIL);
            MyMuhurtaView viewTa = new MyMuhurtaView(new Point(0, 0), (Horoscope) null);
            assertNotNull(viewTa.getPerson1Btn());
            assertNotNull(viewTa.getPerson2Btn());
            assertEquals("நபர் 1: தேர்வு செய்...", viewTa.getPerson1Btn().getText());
            assertEquals("நபர் 2: தேர்வு செய்...", viewTa.getPerson2Btn().getText());
            Font btnFont = viewTa.getPerson1Btn().getFont();
            assertEquals(-1, btnFont.canDisplayUpTo(viewTa.getPerson1Btn().getText()));
            assertEquals(-1, btnFont.canDisplayUpTo(viewTa.getPerson2Btn().getText()));

            // 2. English Mode
            AstroSoft.getPreferences().setLanguage(Language.ENGLISH);
            MyMuhurtaView viewEn = new MyMuhurtaView(new Point(0, 0), (Horoscope) null);
            assertEquals("Person 1: Select... ▾", viewEn.getPerson1Btn().getText());
            assertEquals("Person 2: Select... ▾", viewEn.getPerson2Btn().getText());
        } finally {
            AstroSoft.getPreferences().setLanguage(originalLanguage);
        }
    }

    @Test
    public void testModernBirthDataPanelTamilAndEnglishLocalization() {
        try {
            // 1. Tamil Mode
            AstroSoft.getPreferences().setLanguage(Language.TAMIL);
            ModernBirthDataPanel panelTa = new ModernBirthDataPanel();
            assertEquals("ஆண்", panelTa.getMaleRadio().getText());
            assertEquals("பெண்", panelTa.getFemaleRadio().getText());
            assertEquals("கைமுறை ஆயத்தொலைவுகள்", panelTa.getManualCoordCheck().getText());
            assertEquals("ஊரைத் தேடுக", panelTa.getSearchButton().getToolTipText());

            Font radioFont = panelTa.getMaleRadio().getFont();
            assertEquals(-1, radioFont.canDisplayUpTo(panelTa.getMaleRadio().getText()));
            assertEquals(-1, radioFont.canDisplayUpTo(panelTa.getFemaleRadio().getText()));
            assertEquals(-1, radioFont.canDisplayUpTo(panelTa.getManualCoordCheck().getText()));
            assertEquals(-1, radioFont.canDisplayUpTo(panelTa.getSearchButton().getToolTipText()));

            // 2. English Mode
            AstroSoft.getPreferences().setLanguage(Language.ENGLISH);
            ModernBirthDataPanel panelEn = new ModernBirthDataPanel();
            assertEquals("Male", panelEn.getMaleRadio().getText());
            assertEquals("Female", panelEn.getFemaleRadio().getText());
            assertEquals("Manual Coordinates", panelEn.getManualCoordCheck().getText());
            assertEquals("Search Place", panelEn.getSearchButton().getToolTipText());
        } finally {
            AstroSoft.getPreferences().setLanguage(originalLanguage);
        }
    }

    @Test
    public void testPanchangEnumsTamilAndEnglishLocalization() {
        Font tamilFont = UIUtil.getFont(Language.TAMIL, Font.PLAIN, 12);
        try {
            // 1. Tamil Mode
            AstroSoft.getPreferences().setLanguage(Language.TAMIL);
            for (Thithi t : Thithi.values()) {
                String ta = t.toString();
                assertNotNull("Tamil Thithi must not be null for " + t, ta);
                assertFalse("Tamil Thithi must not match enum name for " + t, ta.equals(t.name()));
                assertEquals("Glyphs in Thithi '" + ta + "' must display cleanly", -1, tamilFont.canDisplayUpTo(ta));
            }

            for (Yoga y : Yoga.values()) {
                String ta = y.toString();
                assertNotNull("Tamil Yoga must not be null for " + y, ta);
                assertFalse("Tamil Yoga must not match enum name for " + y, ta.equals(y.name()));
                assertEquals("Glyphs in Yoga '" + ta + "' must display cleanly", -1, tamilFont.canDisplayUpTo(ta));
            }

            for (AmirthathiYoga ay : AmirthathiYoga.values()) {
                String ta = ay.toString();
                assertNotNull("Tamil AmirthathiYoga must not be null for " + ay, ta);
                assertFalse("Tamil AmirthathiYoga must not match enum name for " + ay, ta.equals(ay.name()));
                assertEquals("Glyphs in AmirthathiYoga '" + ta + "' must display cleanly", -1, tamilFont.canDisplayUpTo(ta));
            }

            // 2. English Mode
            AstroSoft.getPreferences().setLanguage(Language.ENGLISH);
            assertEquals("Prathama", Thithi.Prathamai.toString());
            assertEquals("Amavasya", Thithi.Amavasya.toString());
            assertEquals("Vishakambha", Yoga.Vishakambha.toString());
            assertEquals("Amrita", AmirthathiYoga.Amirtha.toString());
            assertEquals("Prabalarishta", AmirthathiYoga.Prabalarshta.toString());
        } finally {
            AstroSoft.getPreferences().setLanguage(originalLanguage);
        }
    }

    @Test
    public void testMissingTranslationOfflineLogger() {
        String nonExistentKey = "NonExistentKey_XYZ_12345";
        String result = Internalization.getString(Language.TAMIL, nonExistentKey);
        assertEquals("Missing key must return key string fallback", nonExistentKey, result);

        java.io.File logFile = new java.io.File("logs/missing_translations.log");
        assertTrue("missing_translations.log must exist after missing key lookup", logFile.exists());
    }
}
