package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.BeforeClass;
import org.junit.Test;

import app.astrosoft.beans.MuhurtaProfile;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.comp.AstrosoftTabbedPane;
import app.astrosoft.ui.dlg.MuhurtaProfileDialog;

/**
 * Headless regression and functional test suite for MuhurtaProfileDialog.
 * Validates AstrosoftTabbedPane integration, Horoscope & Star tabs,
 * two-way synchronization, profile generation, and offscreen painting.
 */
public class MuhurtaProfileDialogTest {

    private static Horoscope sampleHoroscope;

    @BeforeClass
    public static void setUp() {
        sampleHoroscope = SampleHoroscopes.getHoroscope("samples/raja.ash");
        assertNotNull("Sample horoscope must load", sampleHoroscope);
    }

    private static void renderOffscreen(Component comp, int width, int height) {
        comp.setSize(width, height);
        comp.doLayout();
        layoutDeep(comp);
        BufferedImage img = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        comp.paint(g);
        g.dispose();
    }

    private static void layoutDeep(Component comp) {
        if (comp instanceof Container) {
            Container container = (Container) comp;
            container.doLayout();
            for (Component child : container.getComponents()) {
                layoutDeep(child);
            }
        }
    }

    @Test
    public void testDefaultInitialization() {
        MuhurtaProfileDialog dlg = new MuhurtaProfileDialog(null, "Person 1", null);
        assertNotNull("Dialog must instantiate", dlg);

        AstrosoftTabbedPane tabs = dlg.getTabbedPane();
        assertNotNull("AstrosoftTabbedPane must exist", tabs);
        assertEquals("Must have 2 tabs", 2, tabs.getTabCount());
        assertEquals("Tab 0 title", "Horoscope", tabs.getTitleAt(0));
        assertEquals("Tab 1 title", "Star", tabs.getTitleAt(1));
        assertEquals("Default selected tab is Horoscope", 0, tabs.getSelectedIndex());

        assertEquals("Default name in Star tab", "Person 1", dlg.getNameField().getText());
        dlg.dispose();
    }

    @Test
    public void testPopulateWithHoroscopeProfile() {
        MuhurtaProfile current = new MuhurtaProfile(sampleHoroscope);
        MuhurtaProfileDialog dlg = new MuhurtaProfileDialog(null, "Person 1", current);

        assertEquals("Tab 0 (Horoscope) selected for horoscope profile", 0, dlg.getTabbedPane().getSelectedIndex());
        assertNotNull("Loaded horoscope must be set", dlg.getLoadedHoroscope());
        assertEquals("Name must match horoscope", sampleHoroscope.getPersonName(), dlg.getNameField().getText());
        assertEquals("Rasi must match horoscope", sampleHoroscope.getRasi(), dlg.getRasiCombo().getSelectedItem());

        // Apply from horoscope tab
        dlg.invokeApply();
        assertTrue("Dialog must be applied", dlg.isApplied());
        MuhurtaProfile result = dlg.getSelectedProfile();
        assertNotNull("Result profile must exist", result);
        assertTrue("Result must have horoscope", result.hasHoroscope());
        assertEquals("Result name", sampleHoroscope.getPersonName(), result.getName());
        assertEquals("Result rasi", sampleHoroscope.getRasi(), result.getRasi());
        dlg.dispose();
    }

    @Test
    public void testPopulateWithStarProfile() {
        MuhurtaProfile current = new MuhurtaProfile("Kiran", Rasi.Simha, Nakshathra.Magam);
        MuhurtaProfileDialog dlg = new MuhurtaProfileDialog(null, "Person 2", current);

        assertEquals("Tab 1 (Star) selected for star profile", 1, dlg.getTabbedPane().getSelectedIndex());
        assertEquals("Name must match star profile", "Kiran", dlg.getNameField().getText());
        assertEquals("Rasi must match", Rasi.Simha, dlg.getRasiCombo().getSelectedItem());
        assertEquals("Nakshatra must match", Nakshathra.Magam, dlg.getNakCombo().getSelectedItem());

        // Apply from star tab
        dlg.invokeApply();
        assertTrue("Dialog must be applied", dlg.isApplied());
        MuhurtaProfile result = dlg.getSelectedProfile();
        assertNotNull("Result profile must exist", result);
        assertFalse("Result must not have horoscope", result.hasHoroscope());
        assertEquals("Result name", "Kiran", result.getName());
        assertEquals("Result rasi", Rasi.Simha, result.getRasi());
        assertEquals("Result nak", Nakshathra.Magam, result.getNakshathra());
        dlg.dispose();
    }

    @Test
    public void testClearProfile() {
        MuhurtaProfile current = new MuhurtaProfile(sampleHoroscope);
        MuhurtaProfileDialog dlg = new MuhurtaProfileDialog(null, "Person 1", current);

        dlg.invokeClear();
        assertTrue("Dialog must be applied on clear", dlg.isApplied());
        assertNull("Result profile must be null after clear", dlg.getSelectedProfile());
        dlg.dispose();
    }

    @Test
    public void testHeadlessOffscreenRendering() {
        MuhurtaProfileDialog dlg = new MuhurtaProfileDialog(null, "Person 1", null);
        renderOffscreen(dlg, 430, 285);
        dlg.dispose();

        // Also test with loaded horoscope
        MuhurtaProfileDialog dlgWithHor = new MuhurtaProfileDialog(null, "Person 2", new MuhurtaProfile(sampleHoroscope));
        renderOffscreen(dlgWithHor, 430, 285);
        dlgWithHor.dispose();
    }
}
