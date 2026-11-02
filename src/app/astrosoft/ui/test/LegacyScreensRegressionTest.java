package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTree;

import org.junit.BeforeClass;
import org.junit.Test;

import app.astrosoft.beans.AshtaVargaChartData;
import app.astrosoft.consts.Ayanamsa;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Varga;
import app.astrosoft.core.Compatibility;
import app.astrosoft.core.Ephemeris;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.Muhurtha;
import app.astrosoft.core.Panchang;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.util.AstroUtil;
import app.astrosoft.ui.comp.AstrosoftTabbedPane;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.view.AshtavargaView;
import app.astrosoft.ui.view.BhavaView;
import app.astrosoft.ui.view.CompatibilityView;
import app.astrosoft.ui.view.EphemerisView;
import app.astrosoft.ui.view.FindNameView;
import app.astrosoft.ui.view.MuhurthaPanel;
import app.astrosoft.ui.view.PanchangView;
import app.astrosoft.ui.view.PlanetView;
import app.astrosoft.ui.view.ShadBalaBarView;
import app.astrosoft.ui.view.ShadBalaView;
import app.astrosoft.ui.view.VargaChartView;
import app.astrosoft.ui.view.VimDasaView;
import app.astrosoft.ui.view.YogaCombinationsView;

/**
 * Headless regression test suite covering all 13 original legacy Astrosoft screens.
 * Validates component instantiation, structural contract (tabs, tables, charts),
 * and headless rendering without a physical display.
 */
public class LegacyScreensRegressionTest {

    private static Horoscope h;
    private static Panchang pan;
    private static Ephemeris eph;
    private static Compatibility compatibility;
    private static Muhurtha muhurtha;

    @BeforeClass
    public static void setUp() {
        h = SampleHoroscopes.getHoroscope("samples/raja.ash");
        assertNotNull("Sample horoscope must load", h);

        Calendar today = new GregorianCalendar();
        pan = new Panchang(today);
        eph = new Ephemeris(AstroUtil.getCalendar(), Ephemeris.Mode.Daily);
        compatibility = new Compatibility(h, h);
        muhurtha = new Muhurtha(today, h.getRasi(), h.getNakshathra().getNak());
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

    @SuppressWarnings("unchecked")
    private static <T extends Component> List<T> findComponents(Container parent, Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        for (Component c : parent.getComponents()) {
            if (clazz.isInstance(c)) {
                list.add((T) c);
            }
            if (c instanceof Container) {
                list.addAll(findComponents((Container) c, clazz));
            }
        }
        return list;
    }

    @Test
    public void testLegacyAshtavargaView() {
        AshtavargaView view = new AshtavargaView(h.getTitle(), h.getAshtaVarga(), h.getAscendant(), h.getRasi(), h.getPlanetRasis(), new Point(0, 0));
        assertNotNull("AshtavargaView created", view);

        AstrosoftTabbedPane tabbedPane = findComponents(view, AstrosoftTabbedPane.class).get(0);
        assertNotNull("Tabbed pane must exist", tabbedPane);
        assertEquals("Must contain 8 tabs", 8, tabbedPane.getTabCount());

        String[] expectedTabs = {"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "SarvaAshtavarga"};
        for (int i = 0; i < expectedTabs.length; i++) {
            assertEquals("Tab " + i + " title", expectedTabs[i], tabbedPane.getTitleAt(i));

            Container tabContent = (Container) tabbedPane.getComponentAt(i);
            assertNotNull("Tab content panel " + i, tabContent);
            List<Chart> tabCharts = findComponents(tabContent, Chart.class);
            List<AstrosoftTable> tabTables = findComponents(tabContent, AstrosoftTable.class);

            // Every tab (Sun through Saturn and SarvaAshtavarga) has exactly 3 charts: Main, Trikona, Ekathipathya
            assertEquals("Tab " + expectedTabs[i] + " must have 3 charts", 3, tabCharts.size());

            // Chart 1 is Main (BAV for planets, SAV for SarvaAshtavarga)
            Chart mainChart = tabCharts.get(0);
            AshtaVargaChartData mainData = (AshtaVargaChartData) mainChart.getChartData();
            assertTrue("Main chart must be marked BAV/SAV for coloring", mainData.isBAV());

            // Trikona chart must NOT be marked BAV (no background color)
            Chart trikonaChart = tabCharts.get(1);
            AshtaVargaChartData trikonaData = (AshtaVargaChartData) trikonaChart.getChartData();
            assertFalse("Trikona chart must not have background coloring", trikonaData.isBAV());

            // Ekathipathya chart must NOT be marked BAV (no background color)
            Chart ekathipathyaChart = tabCharts.get(2);
            AshtaVargaChartData ekathipathyaData = (AshtaVargaChartData) ekathipathyaChart.getChartData();
            assertFalse("Ekathipathya chart must not have background coloring", ekathipathyaData.isBAV());

            if (i < 7) {
                // Planetary BAV chart must have natal rasi set for circle marker
                assertNotNull("BAV chart must have natal rasi set for circle marker", mainData.getNatalRasi());

                // Each planet tab must have exactly 1 Guna table
                assertEquals("Tab " + expectedTabs[i] + " must have 1 Guna table", 1, tabTables.size());
            } else {
                // SarvaAshtavarga tab has no natal planet circle and no Guna table
                assertEquals("SarvaAshtavarga tab has no Guna table", 0, tabTables.size());
            }

            // Render each individual tab off-screen to ensure layout and drawing succeed
            renderOffscreen(tabContent, 750, 380);
        }

        // Verify total charts across the entire view (8 tabs * 3 charts = 24 charts)
        List<Chart> allCharts = findComponents(view, Chart.class);
        assertEquals("Total charts in AshtavargaView must be 24 (3 charts across 8 tabs)", 24, allCharts.size());

        renderOffscreen(view, 750, 430);
    }

    @Test
    public void testLegacyVimDasaView() {
        VimDasaView view = new VimDasaView(h.getTitle(), h.getVimshottariDasa());
        assertNotNull("VimDasaView created", view);

        List<JTree> trees = findComponents(view, JTree.class);
        assertEquals("Must contain Dasa tree", 1, trees.size());

        List<AstrosoftTable> tables = findComponents(view, AstrosoftTable.class);
        assertEquals("Must contain Dasa table", 1, tables.size());

        renderOffscreen(view, 580, 400);
    }

    @Test
    public void testLegacyBhavaPositionsView() {
        BhavaView view = new BhavaView(h.getTitle(), h.getHousePosition());
        assertNotNull("BhavaView created", view);

        List<AstrosoftTable> tables = findComponents(view, AstrosoftTable.class);
        assertEquals("Must contain Bhava table", 1, tables.size());
        assertEquals("Must have 12 houses", 12, tables.get(0).getRowCount());

        renderOffscreen(view, 600, 340);
    }

    @Test
    public void testLegacyPlanetaryPositionsView() {
        PlanetView view = new PlanetView(h.getTitle(), h.getPlanetaryInfo());
        assertNotNull("PlanetView created", view);

        List<AstrosoftTable> tables = findComponents(view, AstrosoftTable.class);
        assertEquals("Must contain Planetary Positions table", 1, tables.size());
        assertTrue("Must have rows for all planets", tables.get(0).getRowCount() >= 9);

        renderOffscreen(view, 720, 295);
    }

    @Test
    public void testLegacyDivisionalChartsView() {
        VargaChartView view = new VargaChartView(h.getTitle(), h.getPlanetaryInfo());
        assertNotNull("VargaChartView created", view);

        List<JList> lists = findComponents(view, JList.class);
        assertEquals("Must contain Varga selection list", 1, lists.size());
        assertEquals("Must list all 16 Varga divisional charts", Varga.values().length, lists.get(0).getModel().getSize());

        List<Chart> charts = findComponents(view, Chart.class);
        assertTrue("Must display at least one divisional chart", !charts.isEmpty());

        renderOffscreen(view, 650, 400);
    }

    @Test
    public void testLegacyShadbalaView() {
        ShadBalaView view = new ShadBalaView(h.getTitle(), h.getShadBala(), new Point(30, 20));
        assertNotNull("ShadBalaView created", view);

        List<AstrosoftTabbedPane> tabbedPanes = findComponents(view, AstrosoftTabbedPane.class);
        assertEquals("Must contain Shadbala tabbed pane", 1, tabbedPanes.size());
        assertEquals("Must contain 4 tabs: ShadBala, SthanaBala, KalaBala, and BhavaBala", 4, tabbedPanes.get(0).getTabCount());

        List<AstrosoftTable> tables = findComponents(view, AstrosoftTable.class);
        assertTrue("Must contain tables for Shadbala data", tables.size() >= 2);

        renderOffscreen(view, 750, 400);

        // Also test ShadBalaBarView
        ShadBalaBarView barView = new ShadBalaBarView(h.getShadBala());
        assertNotNull("ShadBalaBarView created", barView);
        renderOffscreen(barView, 530, 218);
    }

    @Test
    public void testLegacyYogaCombinationsView() {
        YogaCombinationsView view = new YogaCombinationsView(h.getTitle(), h.getYogaCombinations(), h.getPlanetaryInfo());
        assertNotNull("YogaCombinationsView created", view);

        List<JList> lists = findComponents(view, JList.class);
        assertEquals("Must contain Yoga combinations list", 1, lists.size());

        renderOffscreen(view, 790, 470);
    }

    @Test
    public void testLegacyPanchangView() {
        PanchangView view = new PanchangView(pan, new Point(30, 20));
        assertNotNull("PanchangView created", view);

        List<AstrosoftTable> tables = findComponents(view, AstrosoftTable.class);
        assertTrue("Must contain Panchang table", !tables.isEmpty());

        List<Chart> charts = findComponents(view, Chart.class);
        assertEquals("Must contain Panchang planetary chart", 1, charts.size());

        renderOffscreen(view, 750, 450);
    }

    @Test
    public void testLegacyEphemerisView() {
        EphemerisView view = new EphemerisView(eph, new Point(10, 2));
        assertNotNull("EphemerisView created", view);

        List<AstrosoftTable> tables = findComponents(view, AstrosoftTable.class);
        assertEquals("Must contain Ephemeris table", 1, tables.size());

        renderOffscreen(view, 750, 450);
    }

    @Test
    public void testLegacyMarriageCompatibilityView() {
        CompatibilityView view = new CompatibilityView(DisplayStrings.MRAG_COMP_STR.toString(Language.ENGLISH), compatibility, new Point(30, 20));
        assertNotNull("CompatibilityView created", view);

        List<AstrosoftTabbedPane> tabs = findComponents(view, AstrosoftTabbedPane.class);
        assertEquals("Must contain Compatibility tabbed pane", 1, tabs.size());

        List<AstrosoftTable> tables = findComponents(view, AstrosoftTable.class);
        assertTrue("Must contain Kuta/Porutham scoring table", !tables.isEmpty());

        renderOffscreen(view, 750, 400);
    }

    @Test
    public void testLegacyMuhurthaPanel() {
        MuhurthaPanel panel = new MuhurthaPanel(muhurtha, null);
        assertNotNull("MuhurthaPanel created", panel);

        List<AstrosoftTable> tables = findComponents(panel, AstrosoftTable.class);
        assertEquals("Must contain Muhurtha table", 1, tables.size());

        renderOffscreen(panel, 800, 500);
    }

    @Test
    public void testLegacyFindNameView() {
        FindNameView view = new FindNameView();
        assertNotNull("FindNameView created", view);

        List<JTabbedPane> tabs = findComponents(view, JTabbedPane.class);
        assertEquals("Must contain Numerology tabbed pane", 1, tabs.size());
        assertEquals("Must have Search and Add tabs", 2, tabs.get(0).getTabCount());

        renderOffscreen(view, 700, 436);
    }

    @Test
    public void testLegacyHoroscopeDashboard() {
        app.astrosoft.ui.view.InfoView infoView = new app.astrosoft.ui.view.InfoView(h.getHoroscopeInfo(), new Point(20, 20));
        app.astrosoft.ui.view.RelativePlanetView relativePlanets = new app.astrosoft.ui.view.RelativePlanetView(h.getPlanetaryInfo());
        ShadBalaBarView shadbalaBar = new ShadBalaBarView(h.getShadBala());

        Chart rasi = new Chart(new app.astrosoft.beans.PlanetChartData(Varga.Rasi, h.getPlanetaryInfo()), new Dimension(418, 365));
        Chart navamsa = new Chart(new app.astrosoft.beans.PlanetChartData(Varga.Navamsa, h.getPlanetaryInfo()), new Dimension(418, 365));
        javax.swing.JPanel charts = new javax.swing.JPanel(new java.awt.GridLayout(1, 2, 22, 0));
        charts.add(rasi);
        charts.add(navamsa);

        javax.swing.JComponent ashtavargaGrid = AshtavargaView.createAshtavargaGridPanel(h.getAshtaVarga(), h.getAscendant(), h.getRasi(), h.getPlanetRasis());

        app.astrosoft.ui.view.HoroscopeDashboard dashboard = new app.astrosoft.ui.view.HoroscopeDashboard(
                infoView, relativePlanets, shadbalaBar, charts, ashtavargaGrid, false, c -> {});
        assertNotNull("Dashboard created", dashboard);

        renderOffscreen(dashboard, 1428, 900);
    }
}
