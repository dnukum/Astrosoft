package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.Test;

import app.astrosoft.beans.ChartData;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.view.AshtavargaView;

public class AshtavargaMultiGridTest {

    @Test
    public void testAshtavargaMultiGridCreationAndRendering() throws Exception {
        Horoscope h = SampleHoroscopes.getHoroscope("samples/raja.ash");
        assertNotNull("Horoscope loaded", h);

        Rasi asc = h.getAscendant();
        Rasi moon = h.getRasi();
        assertNotNull("Ascendant found", asc);
        assertNotNull("Moon found", moon);

        JPanel panel = AshtavargaView.createAshtavargaGridPanel(h.getAshtaVarga(), asc, moon);
        assertNotNull("Grid panel created", panel);

        // Find all 8 mini Chart components
        List<Chart> charts = findComponents(panel, Chart.class);
        assertEquals("Must contain exactly 8 charts", 8, charts.size());

        String[] expectedCodes = {"SAV", "Su", "Mo", "Ma", "Me", "Ju", "Ve", "Sa"};
        String[] expectedTooltips = {
            "SAV (Sarva Ashtakavarga)",
            "Sun (Surya)",
            "Moon (Chandra)",
            "Mars (Mangala)",
            "Mercury (Budha)",
            "Jupiter (Guru)",
            "Venus (Shukra)",
            "Saturn (Shani)"
        };

        java.lang.reflect.Field field = Chart.class.getDeclaredField("chartData");
        field.setAccessible(true);

        java.lang.reflect.Field housePanelField = Chart.class.getDeclaredField("housePanel");
        housePanelField.setAccessible(true);

        for (int i = 0; i < 8; i++) {
            Chart c = charts.get(i);
            ChartData cd = (ChartData) field.get(c);
            assertEquals("Chart " + i + " code", expectedCodes[i], cd.getChartName());
            assertEquals("Chart " + i + " tooltip", expectedTooltips[i], c.getToolTipText());

            // Check house cells for Asc and Mo markers
            List<JPanel> houses = (List<JPanel>) housePanelField.get(c);
            assertEquals("Must have 12 houses", 12, houses.size());

            JPanel ascHouse = houses.get(asc.ordinal());
            JPanel moonHouse = houses.get(moon.ordinal());

            JLabel ascLabel = findLabelWithText(ascHouse, "Asc");
            assertNotNull("Asc label must be in Ascendant house of chart " + expectedCodes[i], ascLabel);

            JLabel moonLabel = findLabelWithText(moonHouse, "Mo");
            assertNotNull("Mo label must be in Moon house of chart " + expectedCodes[i], moonLabel);

            // Verify Ashtavarga domain strength and highlight coloring
            boolean isSav = expectedCodes[i].equals("SAV");
            app.astrosoft.beans.AshtaVargaChartData avData = (app.astrosoft.beans.AshtaVargaChartData) cd;
            assertEquals("isSarvaAshtavarga check", isSav, avData.isSarvaAshtavarga());

            for (Rasi r : Rasi.values()) {
                JPanel hp = houses.get(r.ordinal());
                Integer val = avData.getVarga().get(r);
                if (val != null) {
                    app.astrosoft.consts.AshtavargaStrength expectedStrength;
                    if (isSav) {
                        expectedStrength = (val <= 25) ? app.astrosoft.consts.AshtavargaStrength.WEAK
                                : (val >= 30) ? app.astrosoft.consts.AshtavargaStrength.STRONG
                                : app.astrosoft.consts.AshtavargaStrength.NEUTRAL;
                    } else {
                        expectedStrength = (val <= 2) ? app.astrosoft.consts.AshtavargaStrength.WEAK
                                : (val >= 5) ? app.astrosoft.consts.AshtavargaStrength.STRONG
                                : app.astrosoft.consts.AshtavargaStrength.NEUTRAL;
                    }
                    assertEquals("Domain strength for " + expectedCodes[i] + " " + r, expectedStrength, avData.getStrength(r));
                    assertEquals("House background for " + expectedCodes[i] + " " + r, app.astrosoft.ui.util.UIConsts.getAshtavargaColor(expectedStrength), hp.getBackground());
                }
            }
        }

        // Test pack and paint
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.pack();

        assertEquals("Panel width", AshtavargaView.gridPanelSize.width, panel.getWidth());
        assertEquals("Panel height", AshtavargaView.gridPanelSize.height, panel.getHeight());

        BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        panel.paint(g);
        g.dispose();
        frame.dispose();
    }

    @Test
    public void testAshtavargaNatalRasiHighlighting() throws Exception {
        Horoscope h = SampleHoroscopes.getHoroscope("samples/raja.ash");
        assertNotNull("Horoscope loaded", h);

        Rasi asc = h.getAscendant();
        Rasi moon = h.getRasi();
        java.util.EnumMap<app.astrosoft.consts.Planet, Rasi> planetRasis = h.getPlanetRasis();

        JPanel panel = AshtavargaView.createAshtavargaGridPanel(h.getAshtaVarga(), asc, moon, planetRasis);
        assertNotNull("Grid panel created", panel);

        List<Chart> charts = findComponents(panel, Chart.class);
        assertEquals("Must contain exactly 8 charts", 8, charts.size());

        java.lang.reflect.Field field = Chart.class.getDeclaredField("chartData");
        field.setAccessible(true);
        java.lang.reflect.Field housePanelField = Chart.class.getDeclaredField("housePanel");
        housePanelField.setAccessible(true);

        // Chart 0 is SAV -> natalRasi must be null
        app.astrosoft.beans.AshtaVargaChartData savData = (app.astrosoft.beans.AshtaVargaChartData) field.get(charts.get(0));
        org.junit.Assert.assertNull("SAV must not have a natal Rasi", savData.getNatalRasi());

        // Charts 1..7 are planets Sun..Saturn
        app.astrosoft.consts.Planet[] planets = {
            app.astrosoft.consts.Planet.Sun,
            app.astrosoft.consts.Planet.Moon,
            app.astrosoft.consts.Planet.Mars,
            app.astrosoft.consts.Planet.Mercury,
            app.astrosoft.consts.Planet.Jupiter,
            app.astrosoft.consts.Planet.Venus,
            app.astrosoft.consts.Planet.Saturn
        };

        for (int i = 0; i < planets.length; i++) {
            Chart c = charts.get(i + 1);
            app.astrosoft.beans.AshtaVargaChartData bchData = (app.astrosoft.beans.AshtaVargaChartData) field.get(c);
            Rasi expectedNatalRasi = planetRasis.get(planets[i]);
            assertEquals("Natal Rasi for " + planets[i], expectedNatalRasi, bchData.getNatalRasi());

            // Check that the natal house score label exists and has natal tooltip
            List<JPanel> houses = (List<JPanel>) housePanelField.get(c);
            JPanel natalHouse = houses.get(expectedNatalRasi.ordinal());
            Integer val = bchData.getVarga().get(expectedNatalRasi);
            assertNotNull("Score in natal house", val);
            JLabel scoreLabel = findLabelWithText(natalHouse, String.valueOf(val));
            assertNotNull("Score label must be found in natal house for " + planets[i], scoreLabel);
            assertTrue("Score label tooltip should mention Natal placement",
                    scoreLabel.getToolTipText() != null && scoreLabel.getToolTipText().contains("Natal " + planets[i].name()));
        }

        // Test painting with highlight
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.pack();
        BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        panel.paint(g);
        g.dispose();
        frame.dispose();
    }

    @Test
    public void testPrastharaMultiTabSwitchingAndRendering() throws Exception {
        Horoscope h = SampleHoroscopes.getHoroscope("samples/raja.ash");
        assertNotNull("Horoscope loaded", h);

        Rasi asc = h.getAscendant();
        Rasi moon = h.getRasi();
        java.util.EnumMap<app.astrosoft.consts.Planet, Rasi> planetRasis = h.getPlanetRasis();

        JPanel panel = AshtavargaView.createAshtavargaGridPanel(h.getAshtaVarga(), asc, moon, planetRasis);
        assertNotNull("Grid panel created", panel);

        // Locate PillTabBar ("SAV/BAV" | "Prasthara") and PrastharaCardPanel
        List<AshtavargaView.PillTabBar> pillBars = findComponents(panel, AshtavargaView.PillTabBar.class);
        assertEquals("Must contain 1 PillTabBar", 1, pillBars.size());
        AshtavargaView.PillTabBar pillBar = pillBars.get(0);
        assertEquals("PillTabBar must have 2 tabs (SAV/BAV | Prasthara)", 2, pillBar.getButtons().size());
        assertEquals("SAV/BAV", pillBar.getButtons().get(0).getText());
        assertEquals("Prasthara", pillBar.getButtons().get(1).getText());
        assertEquals("Default tab must be SAV/BAV (0)", 0, pillBar.getSelectedTab());

        List<AshtavargaView.PrastharaCardPanel> prastharaPanels = findComponents(panel, AshtavargaView.PrastharaCardPanel.class);
        assertEquals("Must contain 1 PrastharaCardPanel", 1, prastharaPanels.size());
        AshtavargaView.PrastharaCardPanel prastharaCard = prastharaPanels.get(0);
        assertEquals("PrastharaCardPanel must have 8 target buttons (Su..Sa + Asc)", 8, prastharaCard.getTargetButtons().size());

        String[] expectedTargetCodes = {"Su", "Mo", "Ma", "Me", "Ju", "Ve", "Sa", "Asc"};
        app.astrosoft.consts.AshtavargaName[] expectedTargets = {
            app.astrosoft.consts.AshtavargaName.Sun,
            app.astrosoft.consts.AshtavargaName.Moon,
            app.astrosoft.consts.AshtavargaName.Mars,
            app.astrosoft.consts.AshtavargaName.Mercury,
            app.astrosoft.consts.AshtavargaName.Jupiter,
            app.astrosoft.consts.AshtavargaName.Venus,
            app.astrosoft.consts.AshtavargaName.Saturn,
            app.astrosoft.consts.AshtavargaName.Ascendant
        };
        int[] expectedCounts = {48, 49, 39, 54, 56, 52, 39, 49};

        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.pack();

        // Switch to Prasthara tab via pill bar button click
        pillBar.getButtons().get(1).doClick();
        assertEquals("Selected tab must be Prasthara (1)", 1, pillBar.getSelectedTab());

        // Cycle through all 8 targets (including Ascendant) and render offscreen
        for (int i = 0; i < expectedTargetCodes.length; i++) {
            javax.swing.JButton targetBtn = prastharaCard.getTargetButtons().get(i);
            assertEquals("Target button code", expectedTargetCodes[i], targetBtn.getText());
            targetBtn.doClick();
            assertEquals("Selected target must match", expectedTargets[i], prastharaCard.getSelectedTarget());

            String badgeText = prastharaCard.getSummaryBadgeLabel().getText();
            assertTrue("Summary badge must contain total bindus " + expectedCounts[i] + " for " + expectedTargets[i] + ", got: " + badgeText,
                    badgeText.contains("Total Bindus: " + expectedCounts[i]));

            BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            panel.paint(g);
            g.dispose();
        }

        // Switch back to SAV/BAV tab
        pillBar.getButtons().get(0).doClick();
        assertEquals("Selected tab must return to SAV/BAV (0)", 0, pillBar.getSelectedTab());

        // Verify that stretching the panel dynamically stretches the 8 mini-charts and their house cells
        panel.setBounds(0, 0, 1200, 600);
        panel.doLayout();
        List<Chart> charts = findComponents(panel, Chart.class);
        assertEquals("Must contain 8 mini-charts", 8, charts.size());
        for (Chart chart : charts) {
            chart.doLayout();
            assertTrue("Mini-chart width must stretch beyond base 196px when panel is stretched to 1200px, got: " + chart.getWidth(),
                    chart.getWidth() > 240);
            assertTrue("Mini-chart height must stretch beyond base 162px when panel is stretched to 600px, got: " + chart.getHeight(),
                    chart.getHeight() > 220);
            Component firstHouse = chart.getComponent(0);
            assertTrue("House cell width must stretch beyond base 49px when chart stretches, got: " + firstHouse.getWidth(),
                    firstHouse.getWidth() > 60);
            assertTrue("House cell height must stretch beyond base 40px when chart stretches, got: " + firstHouse.getHeight(),
                    firstHouse.getHeight() > 55);
        }
        frame.dispose();
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

    private static JLabel findLabelWithText(Container parent, String text) {
        for (Component c : parent.getComponents()) {
            if (c instanceof JLabel && text.equals(((JLabel) c).getText())) {
                return (JLabel) c;
            }
            if (c instanceof Container) {
                JLabel found = findLabelWithText((Container) c, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
