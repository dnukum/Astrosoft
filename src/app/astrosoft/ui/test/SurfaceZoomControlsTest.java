package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.Varga;
import app.astrosoft.core.Horoscope;
import app.astrosoft.pref.AstrosoftPref;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIConsts.ChartStyle;
import app.astrosoft.ui.view.AshtavargaView;
import app.astrosoft.ui.view.HoroscopeDashboard;
import app.astrosoft.ui.view.RelativePlanetView;

/**
 * Verifies right-click zoom on fixed charts, the shared rounded Rasi/Navamsha card,
 * absence of header zoom pills, and proportional Ashtavarga resizing across both chart styles.
 */
public class SurfaceZoomControlsTest {

    private int origChartZoom;
    private ChartStyle origStyle;

    @Before
    public void setUp() {
        AstrosoftPref pref = new AstrosoftPref();
        origChartZoom = pref.getChartZoom();
        origStyle = UIConsts.getChartStyle();

        pref.setChartZoom(100);
        Chart.setChartZoomPercent(100);
    }

    @After
    public void tearDown() {
        AstrosoftPref pref = new AstrosoftPref();
        pref.setChartZoom(origChartZoom);
        Chart.setChartZoomPercent(origChartZoom);
        UIConsts.setChartStyle(origStyle);
    }

    @Test
    public void testChartZoomPreferenceClampsAndPersists() {
        AstrosoftPref pref = new AstrosoftPref();
        pref.setChartZoom(130);
        assertEquals(130, new AstrosoftPref().getChartZoom());

        pref.setChartZoom(40);
        assertEquals(70, new AstrosoftPref().getChartZoom());

        pref.setChartZoom(220);
        assertEquals(160, new AstrosoftPref().getChartZoom());
    }

    @Test
    public void testFixedChartRightClickZoomPopupAndRoundedCard() {
        Horoscope h = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_1.ash");
        for (ChartStyle style : ChartStyle.values()) {
            UIConsts.setChartStyle(style);
            Chart.setChartZoomPercent(100);

            Chart rasi = new Chart(new PlanetChartData(Varga.Rasi, h.getPlanetaryInfo()), new Dimension(418, 365));
            Chart navamsa = new Chart(new PlanetChartData(Varga.Navamsa, h.getPlanetaryInfo()), new Dimension(418, 365));
            JPanel card = Chart.createDualChartsCard(rasi, navamsa);

            assertEquals("ChartsCard", card.getName());
            assertEquals(858, card.getPreferredSize().width);
            assertNull("Chart outer border suppressed inside rounded card", rasi.getBorder());
            assertNull("Chart outer border suppressed inside rounded card", navamsa.getBorder());
            for (Component child : card.getComponents()) {
                if (child instanceof JComponent) {
                    assertTrue("ChartsCard child must inherit popup menu", ((JComponent) child).getInheritsPopupMenu());
                }
            }

            JPopupMenu popup = rasi.getComponentPopupMenu();
            assertNotNull("Fixed chart must expose right-click zoom menu in " + style, popup);
            if (style == ChartStyle.NORTH_INDIAN) {
                assertTrue(rasi.getNorthIndianCanvas().getInheritsPopupMenu());
            } else {
                for (JTable t : findComponents(rasi, JTable.class)) {
                    assertTrue(t.getInheritsPopupMenu());
                }
            }

            JMenuItem zoomIn = findMenuItem(popup, "Zoom In (+10%)");
            JMenuItem zoomOut = findMenuItem(popup, "Zoom Out (-10%)");
            JMenuItem reset = findMenuItem(popup, "Reset Zoom (100%)");
            assertNotNull(zoomIn);
            assertNotNull(zoomOut);
            assertNotNull(reset);

            zoomIn.doClick();
            assertEquals(110, Chart.getChartZoomPercent());
            assertEquals(1, Chart.getFontOffset());

            zoomOut.doClick();
            assertEquals(100, Chart.getChartZoomPercent());
            assertEquals(0, Chart.getFontOffset());

            zoomIn.doClick();
            reset.doClick();
            assertEquals(100, Chart.getChartZoomPercent());
            assertEquals(0, Chart.getFontOffset());

            HoroscopeDashboard dash = new HoroscopeDashboard(
                    new JPanel(), new JPanel(), new JPanel(), card, new JPanel(),
                    false, c -> {});
            renderOffscreen(dash, dash.getPreferredSize().width, dash.getPreferredSize().height);
        }
    }

    @Test
    public void testAshtavargaChartsHaveNoZoomPopupAndResizeProportionallyInBothStyles() {
        Horoscope h = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_1.ash");

        // 1. North Indian: scales with container size and keeps all markers inside bounds
        UIConsts.setChartStyle(ChartStyle.NORTH_INDIAN);
        JPanel niGrid = AshtavargaView.createAshtavargaGridPanel(
                h.getAshtaVarga(), h.getAscendant(), h.getRasi(), h.getPlanetRasis());
        renderOffscreen(niGrid, 858, 380);
        for (Chart c : findComponents(niGrid, Chart.class)) {
            assertNull("Ashtavarga charts should not expose zoom popup", c.getComponentPopupMenu());
            assertTrue("Compact Ashtavarga markers must stay inside bounds",
                    c.getNorthIndianCanvas().allAshtavargaMarkersFit());
            double niWidth = c.getNorthIndianCanvas().getGeometry().getWidth();
            assertTrue("North Indian chart should fill > 90% of cell width (got " + niWidth + " vs " + c.getWidth() + ")",
                    niWidth > c.getWidth() * 0.90);
        }
        renderOffscreen(niGrid, 1280, 760);
        for (Chart c : findComponents(niGrid, Chart.class)) {
            assertTrue("Enlarged Ashtavarga markers must stay inside bounds",
                    c.getNorthIndianCanvas().allAshtavargaMarkersFit());
        }

        // 2. South Indian: proportionally grows score label fonts when resized larger (e.g. AshtavargaDialog)
        UIConsts.setChartStyle(ChartStyle.SOUTH_INDIAN);
        JPanel siGrid = AshtavargaView.createAshtavargaGridPanel(
                h.getAshtaVarga(), h.getAscendant(), h.getRasi(), h.getPlanetRasis());
        renderOffscreen(siGrid, 858, 380);
        int compactScoreFont = maxScoreFontSize(findComponents(siGrid, Chart.class).get(0));

        renderOffscreen(siGrid, 1280, 760);
        Chart enlargedFirst = findComponents(siGrid, Chart.class).get(0);
        enlargedFirst.dispatchEvent(new java.awt.event.ComponentEvent(
                enlargedFirst, java.awt.event.ComponentEvent.COMPONENT_RESIZED));
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> {});
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        layoutDeep(siGrid);
        int enlargedScoreFont = maxScoreFontSize(enlargedFirst);

        assertTrue("South Indian Ashtavarga score font should grow when dialog is resized larger (was "
                        + compactScoreFont + ", got " + enlargedScoreFont + ")",
                enlargedScoreFont > compactScoreFont);
    }

    @Test
    public void testFloatingRelativePlanetViewReflowsCleanlyWithoutVerticalStretch() {
        Horoscope h = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_1.ash");
        RelativePlanetView view = new RelativePlanetView(h.getPlanetaryInfo(), h.getAscendant(), h.getShadBala());
        view.setFloatingMode(true);

        assertTrue("Floating mode flag must be active", view.isFloatingMode());
        for (JButton b : findComponents(view, JButton.class)) {
            if ("↕".equals(b.getText()) || "⧉".equals(b.getText())) {
                assertFalse("Dock-only header button '" + b.getText() + "' must be hidden in floating mode", b.isVisible());
            }
        }
        assertNull("RelativePlanetView must not install right-click chart zoom popup", view.getComponentPopupMenu());

        renderOffscreen(view, 500, 236);
        List<JTable> tables = findComponents(view, JTable.class);
        assertEquals(2, tables.size());
        int narrowTableW = tables.get(0).getWidth();
        int baselineRowH = tables.get(0).getRowHeight();
        int baselineCellFont = tables.get(0).getCellRenderer(0, 0)
                .getTableCellRendererComponent(tables.get(0), "Sun", false, false, 0, 0)
                .getFont().getSize();
        JLabel kendraLabel = findKendraLabel(view);
        assertNotNull(kendraLabel);
        int baselineNatureFont = kendraLabel.getFont().getSize();

        // Standard reflow on larger size widens tables and unwraps Functional Nature without inflating fonts
        renderOffscreen(view, 760, 380);
        assertTrue("Split table must expand horizontally on wider container (narrow=" + narrowTableW
                        + ", wide=" + tables.get(0).getWidth() + ")",
                tables.get(0).getWidth() > narrowTableW + 80);
        assertEquals("Row height must remain consistent under standard reflow", baselineRowH, tables.get(0).getRowHeight());
        assertEquals("Table font must remain consistent with rest of app", baselineCellFont,
                tables.get(0).getCellRenderer(0, 0)
                        .getTableCellRendererComponent(tables.get(0), "Sun", false, false, 0, 0)
                        .getFont().getSize());
        assertEquals("Functional Nature font must stay matched with table font", baselineNatureFont,
                kendraLabel.getFont().getSize());

        Container natureGroup = kendraLabel.getParent();
        assertTrue("Functional Nature lines must stay compact instead of stretching across extra dialog height (got h="
                        + natureGroup.getHeight() + ")",
                natureGroup.getHeight() > 20 && natureGroup.getHeight() < 100);

        view.switchToTab(1);
        renderOffscreen(view, 620, 240);
        renderOffscreen(view, 760, 380);
    }

    private static JLabel findKendraLabel(Container root) {
        for (JLabel l : findComponents(root, JLabel.class)) {
            if (l.getText() != null && l.getText().contains("Kendra (1,4,7,10):")) {
                return l;
            }
        }
        return null;
    }

    private static int maxScoreFontSize(Chart chart) {
        int max = 0;
        for (JLabel lbl : findComponents(chart, JLabel.class)) {
            String text = lbl.getText();
            if (text != null && text.matches("\\d+")) {
                max = Math.max(max, lbl.getFont().getSize());
            }
        }
        return max;
    }

    private static JMenuItem findMenuItem(JPopupMenu popup, String text) {
        for (Component c : popup.getComponents()) {
            if (c instanceof JMenuItem && text.equals(((JMenuItem) c).getText())) {
                return (JMenuItem) c;
            }
        }
        return null;
    }

    private static void renderOffscreen(Component comp, int w, int h) {
        comp.setSize(w, h);
        layoutDeep(comp);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        comp.printAll(g2);
        g2.dispose();
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

    private static <T extends JComponent> List<T> findComponents(Container parent, Class<T> clazz) {
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
