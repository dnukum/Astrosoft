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
                    java.awt.Color expectedBg = avData.hasStrengthShading()
                            ? app.astrosoft.ui.util.UIConsts.getAshtavargaColor(expectedStrength)
                            : app.astrosoft.ui.util.UIConsts.getChartBackground();
                    assertEquals("House background for " + expectedCodes[i] + " " + r, expectedBg, hp.getBackground());
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
