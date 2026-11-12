package app.astrosoft.ui.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
        Horoscope h = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_1.ash");
        check(h != null, "Horoscope loaded");

        Rasi asc = h.getAscendant();
        Rasi moon = h.getRasi();
        check(asc != null, "Ascendant found: " + asc);
        check(moon != null, "Moon rasi found: " + moon);

        AshtavargaView view = new AshtavargaView(h.getTitle(), h.getAshtaVarga(), asc, moon, h.getPlanetRasis(), new java.awt.Point(0,0));
        check(view != null, "AshtavargaView created");

        app.astrosoft.ui.comp.AstrosoftTabbedPane tabbedPane = findComponent(view, app.astrosoft.ui.comp.AstrosoftTabbedPane.class);
        check(tabbedPane != null, "AstrosoftTabbedPane found");
        check(tabbedPane.getTabCount() == 8, "Expected 8 tabs in AshtavargaView, got " + tabbedPane.getTabCount());

        String[] expectedTabTitles = {"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "SarvaAshtavarga"};
        for (int i = 0; i < expectedTabTitles.length; i++) {
            check(expectedTabTitles[i].equals(tabbedPane.getTitleAt(i)),
                "Tab " + i + " must have title " + expectedTabTitles[i] + " but got " + tabbedPane.getTitleAt(i));
        }

        // Test pack and render
        JFrame frame = new JFrame();
        frame.getContentPane().add(view);
        frame.pack();
        check(view.getWidth() >= 700, "View width must be >= 700, got " + view.getWidth());
        check(view.getHeight() >= 400, "View height must be >= 400, got " + view.getHeight());

        BufferedImage img = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        view.paint(g);
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
