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
        }

        // Test pack and paint
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.pack();

        assertTrue("Panel width should be around 740, got " + panel.getWidth(), panel.getWidth() >= 730 && panel.getWidth() <= 750);
        assertTrue("Panel height should be around 340, got " + panel.getHeight(), panel.getHeight() >= 330 && panel.getHeight() <= 350);

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
