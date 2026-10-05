package app.astrosoft.core.test;

import java.lang.reflect.Constructor;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import junit.framework.TestCase;
import app.astrosoft.consts.Tara;
import app.astrosoft.beans.TaraBalamResult;

/** Verify the actual Swing renderer for every Tara, including reused selected cells. */
public class TaraStarDisplayTest extends TestCase {
    public void testAllTaraStarCountsAndColors() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    Class<?> type = Class.forName("app.astrosoft.ui.comp.BalamCalendarPanel$BalamStarRenderer");
                    Constructor<?> constructor = type.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    TableCellRenderer renderer = (TableCellRenderer) constructor.newInstance();
                    JTable table = new JTable(1, 1);
                    for (Tara tara : Tara.values()) {
                        int number = tara.getNumber();
                        int count = number == 2 || number == 6 || number == 9 ? 2 : 1;
                        String color = number == 7 ? "#D32F2F"
                            : number == 2 || number == 4 || number == 6 || number == 8 || number == 9 ? "#1E9E3A" : "#E0A800";
                        String stars = count == 2 ? "\u2605 \u2605" : "\u2605";
                        String expected = "<html>" + number + " (<font color='" + color + "'>" + stars + "</font>)</html>";
                        TaraBalamResult value = new TaraBalamResult(tara, 1, tara.getDefaultRank());
                        for (boolean selected : new boolean[]{false, true}) {
                            JLabel cell = (JLabel) renderer.getTableCellRendererComponent(table, value, selected, false, 0, 0);
                            assertEquals("Tara " + number, expected, cell.getText());
                        }
                    }
                } catch (Exception error) { throw new RuntimeException(error); }
            }
        });
    }
}
