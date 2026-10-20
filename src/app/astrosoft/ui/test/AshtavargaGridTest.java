package app.astrosoft.ui.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import app.astrosoft.beans.ChartData;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.view.AshtavargaView;

public class AshtavargaGridTest {

    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        Horoscope h = SampleHoroscopes.getHoroscope("samples/raja.ash");
        check(h != null, "Horoscope loaded");

        Rasi asc = h.getAscendant();
        Rasi moon = h.getRasi();
        check(asc != null, "Ascendant found: " + asc);
        check(moon != null, "Moon rasi found: " + moon);

        JPanel panel = AshtavargaView.createAshtavargaPanel(h.getAshtaVarga(), asc, moon);
        check(panel != null, "Ashtavarga panel created");

        JComboBox combo = findComponent(panel, JComboBox.class);
        Chart currentChart = findComponent(panel, Chart.class);

        check(combo != null, "Dropdown JComboBox found");
        check(combo.getItemCount() == 8, "Expected 8 items in dropdown, got " + combo.getItemCount());
        check(currentChart != null, "Initial Chart found");

        String[] expectedCodes = {"SAV", "Su", "Mo", "Ma", "Me", "Ju", "Ve", "Sa"};
        java.lang.reflect.Field field = Chart.class.getDeclaredField("chartData");
        field.setAccessible(true);

        java.lang.reflect.Field housePanelField = Chart.class.getDeclaredField("housePanel");
        housePanelField.setAccessible(true);

        // Verify Asc and Mo labels inside house cells
        java.util.List<JPanel> houses = (java.util.List<JPanel>) housePanelField.get(currentChart);
        JPanel ascHouse = houses.get(asc.ordinal());
        JPanel moonHouse = houses.get(moon.ordinal());

        JLabel ascLabel = findLabelWithText(ascHouse, "Asc");
        check(ascLabel != null, "Asc label must be found in Ascendant house");

        JLabel moonLabel = findLabelWithText(moonHouse, "Mo");
        check(moonLabel != null, "Mo label must be found in Moon house");

        // Test switching through each option in the dropdown
        for (int i = 0; i < expectedCodes.length; i++) {
            combo.setSelectedIndex(i);
            Chart activeChart = findComponent(panel, Chart.class);
            check(activeChart != null, "Active chart must exist at index " + i);
            ChartData cd = (ChartData) field.get(activeChart);
            check(expectedCodes[i].equals(cd.getChartName()), "Chart at index " + i + " must have code " + expectedCodes[i] + " but got " + cd.getChartName());
        }

        // Test pack and render
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.pack();
        check(panel.getWidth() >= 400 && panel.getWidth() <= 460, "Panel width must be around 440 (snug around 360 chart), got " + panel.getWidth());
        check(panel.getHeight() >= 350, "Panel height must be at least 350, got " + panel.getHeight());

        BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        panel.paint(g);
        g.dispose();
        frame.dispose();

        // Verify InfoView multi-line rendering and row heights
        app.astrosoft.ui.view.InfoView iv = new app.astrosoft.ui.view.InfoView(h.getHoroscopeInfo(), new java.awt.Point(0,0));
        JFrame ivFrame = new JFrame();
        ivFrame.getContentPane().add(iv);
        ivFrame.pack();

        java.util.List<app.astrosoft.ui.table.AstrosoftTable> tables = findComponents(iv, app.astrosoft.ui.table.AstrosoftTable.class);
        check(tables.size() == 2, "Expected 2 tables in InfoView, got " + tables.size());
        boolean hasMultiLine = false;
        for (app.astrosoft.ui.table.AstrosoftTable tbl : tables) {
            for (int r = 0; r < tbl.getRowCount(); r++) {
                Object val = tbl.getValueAt(r, 1);
                javax.swing.table.TableCellRenderer ren = tbl.getCellRenderer(r, 1);
                Component comp = ren.getTableCellRendererComponent(tbl, val, false, false, r, 1);
                String rendered = (comp instanceof JLabel) ? ((JLabel) comp).getText() : comp.toString();
                if (rendered.contains("<br>")) {
                    hasMultiLine = true;
                    check(tbl.getRowHeight(r) >= 34, "Multi-line row height must be >= 34, got " + tbl.getRowHeight(r));
                }
            }
        }
        check(hasMultiLine, "InfoView must contain multi-line rendered cells");
        ivFrame.dispose();

        // Verify RelativePlanetView bounding box and painting
        app.astrosoft.ui.view.RelativePlanetView rpv = new app.astrosoft.ui.view.RelativePlanetView(h.getPlanetaryInfo());
        JFrame rpvFrame = new JFrame();
        rpvFrame.getContentPane().add(rpv);
        rpvFrame.pack();
        check(rpv.getWidth() > 300, "RelativePlanetView width must be > 300, got " + rpv.getWidth());
        BufferedImage rpvImg = new BufferedImage(rpv.getWidth(), rpv.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D rpvG = rpvImg.createGraphics();
        rpv.paint(rpvG);
        rpvG.dispose();
        rpvFrame.dispose();

        System.out.println("AshtavargaGridTest: InfoView, RelativePlanetView, and Ashtakavarga verification PASSED!");
    }

    @SuppressWarnings("unchecked")
    static <T extends Component> java.util.List<T> findComponents(Container parent, Class<T> clazz) {
        java.util.List<T> list = new java.util.ArrayList<T>();
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

    @SuppressWarnings("unchecked")
    static <T extends Component> T findComponent(Container parent, Class<T> clazz) {
        for (Component c : parent.getComponents()) {
            if (clazz.isInstance(c)) {
                return (T) c;
            }
            if (c instanceof Container) {
                T found = findComponent((Container) c, clazz);
                if (found != null) return found;
            }
        }
        return null;
    }

    static JLabel findLabelWithText(Container parent, String text) {
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
