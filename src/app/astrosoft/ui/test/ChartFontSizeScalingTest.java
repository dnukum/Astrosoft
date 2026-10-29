package app.astrosoft.ui.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.EnumMap;
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

    private static int getSunRenderedFontSize(JTable table) {
        for (int r = 0; r < table.getRowCount(); r++) {
            for (int col = 0; col < table.getColumnCount(); col++) {
                Object val = table.getValueAt(r, col);
                if (val == Planet.Sun) {
                    TableCellRenderer renderer = table.getCellRenderer(r, col);
                    Component comp = renderer.getTableCellRendererComponent(table, val, false, false, r, col);
                    if (comp instanceof JLabel) {
                        return ((JLabel) comp).getFont().getSize();
                    }
                }
            }
        }
        return -1;
    }

    @org.junit.Test
    public void testChartFontScaling() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                EnumMap<Planet, Integer> positions = new EnumMap<>(Planet.class);
                EnumMap<Planet, Boolean> directions = new EnumMap<>(Planet.class);
                for (Planet p : Planet.planetsAsc()) {
                    positions.put(p, 1);
                    directions.put(p, true);
                }
                EnumMap<Planet, Nakshathra> stars = new EnumMap<>(Planet.class);
                stars.put(Planet.Sun, Nakshathra.Magam);
                EnumMap<Planet, Double> longs = new EnumMap<>(Planet.class);
                longs.put(Planet.Sun, 123.75);

                Chart.resetFontOffset();
                Chart chart = new Chart(new PlanetChartData(Varga.Rasi, positions, directions, stars, longs), new Dimension(418, 365));

                JTable tableBase = findFirstHouseTable(chart);
                assert tableBase != null : "House table must exist";
                int baseFontSize = getSunRenderedFontSize(tableBase);
                assert baseFontSize > 0 : "Sun must be rendered";
                System.out.println("Base Sun font size: " + baseFontSize);

                // Now adjust font size by +2
                Chart.adjustFontOffset(2);
                chart.rebuild();

                JTable tableInc = findFirstHouseTable(chart);
                int incFontSize = getSunRenderedFontSize(tableInc);
                System.out.println("After +2 offset, Sun font size: " + incFontSize);
                assert incFontSize == baseFontSize + 2 : "Expected font size " + (baseFontSize + 2) + " but got " + incFontSize;

                // Now adjust font size by -1 (offset = +1)
                Chart.adjustFontOffset(-1);
                chart.rebuild();

                JTable tableDec = findFirstHouseTable(chart);
                int decFontSize = getSunRenderedFontSize(tableDec);
                System.out.println("After -1 offset, Sun font size: " + decFontSize);
                assert decFontSize == baseFontSize + 1 : "Expected font size " + (baseFontSize + 1) + " but got " + decFontSize;

                // Now reset font size (offset = 0)
                Chart.resetFontOffset();
                chart.rebuild();

                JTable tableReset = findFirstHouseTable(chart);
                int resetFontSize = getSunRenderedFontSize(tableReset);
                System.out.println("After reset, Sun font size: " + resetFontSize);
                assert resetFontSize == baseFontSize : "Expected font size " + baseFontSize + " but got " + resetFontSize;

                System.out.println("All ChartFontSizeScalingTest assertions PASSED successfully!");
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
            }
        });
    }
}
