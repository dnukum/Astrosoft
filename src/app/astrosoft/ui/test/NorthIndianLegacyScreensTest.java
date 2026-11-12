package app.astrosoft.ui.test;

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

import javax.swing.JPanel;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Varga;
import app.astrosoft.core.Compatibility;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.Panchang;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIConsts.ChartStyle;
import app.astrosoft.ui.view.AshtavargaView;
import app.astrosoft.ui.view.CompPanel;
import app.astrosoft.ui.view.CompatibilityView;
import app.astrosoft.ui.view.HoroscopeDashboard;
import app.astrosoft.ui.view.InfoView;
import app.astrosoft.ui.view.MarriageMatchView;
import app.astrosoft.ui.view.MyMuhurtaView;
import app.astrosoft.ui.view.PanchangView;
import app.astrosoft.ui.view.RelativePlanetView;
import app.astrosoft.ui.view.ShadBalaBarView;
import app.astrosoft.ui.view.VargaChartView;

/**
 * Headless pass verifying that all chart-bearing Astrosoft screens build and render
 * cleanly when {@link ChartStyle#NORTH_INDIAN} is active.
 */
public class NorthIndianLegacyScreensTest {

    private static ChartStyle originalStyle;
    private static Horoscope h;
    private static Panchang pan;
    private static Compatibility compatibility;

    @BeforeClass
    public static void setUpClass() {
        originalStyle = UIConsts.getChartStyle();
        UIConsts.setChartStyle(ChartStyle.NORTH_INDIAN);
        h = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_1.ash");
        assertNotNull("Sample horoscope must load", h);
        Calendar today = new GregorianCalendar();
        pan = new Panchang(today);
        compatibility = new Compatibility(h, h);
    }

    @AfterClass
    public static void tearDownClass() {
        UIConsts.setChartStyle(originalStyle);
    }

    @Test
    public void testAllChartBearingScreensRenderNorthIndian() {
        // 1. HoroscopeDashboard (main Rasi + Navamsa + 8-chart Ashtavarga mini-grid)
        InfoView infoView = new InfoView(h.getHoroscopeInfo(), new Point(20, 20));
        RelativePlanetView relView = new RelativePlanetView(h.getPlanetaryInfo());
        ShadBalaBarView shadbalaBar = new ShadBalaBarView(h.getShadBala());
        Chart rasi = new Chart(new PlanetChartData(Varga.Rasi, h.getPlanetaryInfo()), new Dimension(418, 365));
        Chart navamsa = new Chart(new PlanetChartData(Varga.Navamsa, h.getPlanetaryInfo()), new Dimension(418, 365));
        JPanel charts = new JPanel(new java.awt.GridLayout(1, 2, 22, 0));
        charts.add(rasi);
        charts.add(navamsa);
        HoroscopeDashboard dashboard = new HoroscopeDashboard(
                infoView, relView, shadbalaBar, charts,
                AshtavargaView.createAshtavargaGridPanel(h.getAshtaVarga(), h.getAscendant(), h.getRasi(), h.getPlanetRasis()),
                false, c -> {});
        verifyChartsAndRender(dashboard, 1428, 900, "HoroscopeDashboard");

        // 2. AshtavargaView (24 charts across 8 tabs)
        AshtavargaView avView = new AshtavargaView(h.getTitle(), h.getAshtaVarga(),
                h.getAscendant(), h.getRasi(), h.getPlanetRasis(), new Point(0, 0));
        verifyChartsAndRender(avView, 750, 430, "AshtavargaView");

        // 3. VargaChartView
        VargaChartView vargaView = new VargaChartView(h.getTitle(), h.getPlanetaryInfo());
        verifyChartsAndRender(vargaView, 650, 400, "VargaChartView");

        // 4. PanchangView
        PanchangView panchangView = new PanchangView(pan, new Point(30, 20));
        verifyChartsAndRender(panchangView, 750, 450, "PanchangView");

        // 5. CompatibilityView & CompPanel
        CompatibilityView compView = new CompatibilityView(
                DisplayStrings.MRAG_COMP_STR.toString(Language.ENGLISH), compatibility, new Point(30, 20));
        verifyChartsAndRender(compView, 750, 400, "CompatibilityView");

        CompPanel compPanel = new CompPanel(compatibility);
        verifyChartsAndRender(compPanel, 750, 400, "CompPanel");

        // 6. MarriageMatchView
        MarriageMatchView matchView = new MarriageMatchView(compatibility, new Point(30, 20));
        verifyChartsAndRender(matchView, 900, 700, "MarriageMatchView");

        // 7. MyMuhurtaView
        MyMuhurtaView muhurtaView = new MyMuhurtaView(new Point(30, 20), h);
        verifyChartsAndRender(muhurtaView, 900, 650, "MyMuhurtaView");
    }

    private static void verifyChartsAndRender(Container root, int w, int h, String label) {
        List<Chart> charts = findComponents(root, Chart.class);
        assertFalse(label + " should contain at least one Chart", charts.isEmpty());
        root.setSize(w, h);
        layoutDeep(root);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        root.printAll(g2);
        g2.dispose();
        for (Chart c : charts) {
            assertNotNull(label + " Chart must have NorthIndianChartCanvas", c.getNorthIndianCanvas());
            assertTrue(label + " planet labels must fit inside houses",
                    c.getNorthIndianCanvas().allItemsFitInsideHouses());
            assertTrue(label + " Ashtavarga markers must stay inside houses",
                    c.getNorthIndianCanvas().allAshtavargaMarkersFit());
        }
    }

    private static void layoutDeep(Component comp) {
        if (comp instanceof Container) {
            Container c = (Container) comp;
            c.doLayout();
            for (Component child : c.getComponents()) {
                layoutDeep(child);
            }
        }
    }

    private static <T extends Component> List<T> findComponents(Container parent, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        for (Component c : parent.getComponents()) {
            if (clazz.isInstance(c)) {
                list.add(clazz.cast(c));
            }
            if (c instanceof Container) {
                list.addAll(findComponents((Container) c, clazz));
            }
        }
        return list;
    }
}
