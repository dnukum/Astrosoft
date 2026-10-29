package app.astrosoft.ui.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.EnumMap;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Varga;
import app.astrosoft.ui.comp.Chart;

public class ChartFontSizeScalingTest {

    private static JTable findFirstHouseTable(Component c) {
        if (c instanceof JTable) {
            return (JTable) c;
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                JTable found = findFirstHouseTable(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    @org.junit.Test
    public void testChartFontScaling() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                EnumMap<Planet, Integer> hwPos = new EnumMap<>(Planet.class);
                hwPos.put(Planet.Sun, 11);
                hwPos.put(Planet.Moon, 11);
                hwPos.put(Planet.Mars, 11);
                hwPos.put(Planet.Mercury, 10);
                hwPos.put(Planet.Jupiter, 6);
                hwPos.put(Planet.Venus, 11);
                hwPos.put(Planet.Saturn, 6);
                hwPos.put(Planet.Rahu, 4);
                hwPos.put(Planet.Ketu, 10);
                hwPos.put(Planet.Ascendant, 12); // Meena

                EnumMap<Planet, Boolean> directions = new EnumMap<>(Planet.class);
                for (Planet p : Planet.planetsAsc()) {
                    directions.put(p, true);
                }
                EnumMap<Planet, Nakshathra> stars = new EnumMap<>(Planet.class);
                EnumMap<Planet, Double> longs = new EnumMap<>(Planet.class);

                Chart.resetFontOffset();
                Chart singleChart = new Chart(new PlanetChartData(Varga.Rasi, hwPos, directions, stars, longs), new Dimension(418, 365));
                JFrame frame = new JFrame();
                frame.setContentPane(singleChart);
                frame.pack();

                JTable tableSingle = findFirstHouseTable(singleChart);
                Component compSingle = tableSingle.getCellRenderer(0, 0).getTableCellRendererComponent(tableSingle, Planet.Ascendant, false, false, 0, 0);
                assert compSingle instanceof JLabel : "compSingle must be JLabel";
                assert "Asc".equals(((JLabel) compSingle).getText()) : "Text must be Asc";
                assert compSingle.getFont().getSize() == 15 : "Base Asc font size must be 15";

                // +1 Font Offset
                Chart.adjustFontOffset(1);
                singleChart.rebuild();
                frame.pack();
                JTable tableSingleInc1 = findFirstHouseTable(singleChart);
                Component compSingleInc1 = tableSingleInc1.getCellRenderer(0, 0).getTableCellRendererComponent(tableSingleInc1, Planet.Ascendant, false, false, 0, 0);
                assert compSingleInc1.getFont().getSize() == 16 : "Asc font size after +1 must be 16";
                assert "Asc".equals(((JLabel) compSingleInc1).getText()) : "Text must not be truncated at +1";

                // +2 Font Offset
                Chart.adjustFontOffset(1);
                singleChart.rebuild();
                frame.pack();
                JTable tableSingleInc2 = findFirstHouseTable(singleChart);
                Component compSingleInc2 = tableSingleInc2.getCellRenderer(0, 0).getTableCellRendererComponent(tableSingleInc2, Planet.Ascendant, false, false, 0, 0);
                assert "Asc".equals(((JLabel) compSingleInc2).getText()) : "Text must not be truncated at +2";

                // Reset
                Chart.resetFontOffset();
                singleChart.rebuild();
                frame.pack();
                JTable tableSingleReset = findFirstHouseTable(singleChart);
                Component compSingleReset = tableSingleReset.getCellRenderer(0, 0).getTableCellRendererComponent(tableSingleReset, Planet.Ascendant, false, false, 0, 0);
                assert compSingleReset.getFont().getSize() == 15 : "Font size after reset must be 15";

                System.out.println("All ChartFontSizeScalingTest assertions PASSED successfully!");
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
            }
        });
    }
}
