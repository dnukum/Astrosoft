package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.JTableHeader;

import org.junit.Test;

import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.TableStyle;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.DefaultColumnMetaData;
import app.astrosoft.ui.table.ListTableData;
import app.astrosoft.ui.table.Table;
import app.astrosoft.ui.table.TableData;
import app.astrosoft.ui.table.TableRowData;

public class AstrosoftTableTest {

    private static class SampleRow implements TableRowData {
        private final String col1;
        private final String col2;

        public SampleRow(String c1, String c2) {
            this.col1 = c1;
            this.col2 = c2;
        }

        public Object getColumnData(AstrosoftTableColumn col) {
            if (col == AstrosoftTableColumn.Planet) {
                return col1;
            } else if (col == AstrosoftTableColumn.Rasi) {
                return col2;
            }
            return null;
        }
    }

    private AstrosoftTable createTestTable() {
        List<AstrosoftTableColumn> cols = new ArrayList<AstrosoftTableColumn>();
        cols.add(AstrosoftTableColumn.Planet);
        cols.add(AstrosoftTableColumn.Rasi);

        List<SampleRow> rows = new ArrayList<SampleRow>();
        rows.add(new SampleRow("சூரியன் (Sun)", "மேஷம் (Aries)"));
        rows.add(new SampleRow("சந்திரன் (Moon)", "ரிஷபம் (Taurus)"));

        final DefaultColumnMetaData colMeta = new DefaultColumnMetaData(cols);
        final TableData<SampleRow> data = new ListTableData<SampleRow>(rows);

        Table table = new Table() {
            public DefaultColumnMetaData getColumnMetaData() {
                return colMeta;
            }
            public TableData<? extends TableRowData> getTableData() {
                return data;
            }
        };

        AstrosoftTableModel model = new AstrosoftTableModel(table);
        AstrosoftTable astTable = new AstrosoftTable(model, TableStyle.SCROLL_SINGLE_ROW_SELECT);
        astTable.setSize(new Dimension(300, 100));
        astTable.doLayout();
        return astTable;
    }

    @Test
    public void testCellToolTipShowsFullText() {
        AstrosoftTable table = createTestTable();

        // Simulate hovering over row 0, col 0
        Point p00 = table.getCellRect(0, 0, true).getLocation();
        p00.x += 5;
        p00.y += 5;
        MouseEvent event00 = new MouseEvent(table, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, p00.x, p00.y, 0, false);
        String tip00 = table.getToolTipText(event00);
        assertNotNull("Tooltip for cell (0,0) should not be null", tip00);
        assertEquals("சூரியன் (Sun)", tip00);

        // Simulate hovering over row 1, col 1
        Point p11 = table.getCellRect(1, 1, true).getLocation();
        p11.x += 5;
        p11.y += 5;
        MouseEvent event11 = new MouseEvent(table, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, p11.x, p11.y, 0, false);
        String tip11 = table.getToolTipText(event11);
        assertNotNull("Tooltip for cell (1,1) should not be null", tip11);
        assertEquals("ரிஷபம் (Taurus)", tip11);
    }

    @Test
    public void testHeaderToolTipShowsColumnName() {
        AstrosoftTable table = createTestTable();
        JTableHeader header = table.getTableHeader();
        header.setSize(new Dimension(300, 24));
        header.doLayout();

        // Hover over column 0 in header
        MouseEvent headerEvent = new MouseEvent(header, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, 10, 5, 0, false);
        String headerTip = header.getToolTipText(headerEvent);
        assertNotNull("Header tooltip should not be null", headerTip);
        assertEquals(AstrosoftTableColumn.Planet.toString().trim(), headerTip);
    }

    @Test
    public void testSetColumnWidthHandlesMissingColumnWithoutException() {
        AstrosoftTable table = createTestTable();
        // AstrosoftTableColumn.Date is NOT in the test table model (only Planet and Rasi exist)
        // This must execute safely without throwing ArrayIndexOutOfBoundsException: Index -1
        table.setColumnWidth(50, AstrosoftTableColumn.Date);
        table.setColumnWidth(100, AstrosoftTableColumn.Planet);
        assertEquals(100, table.getColumnModel().getColumn(0).getPreferredWidth());
    }

    @Test
    public void testOutOfBoundsToolTipReturnsNull() {
        AstrosoftTable table = createTestTable();
        MouseEvent outOfBoundsEvent = new MouseEvent(table, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, -10, -10, 0, false);
        assertNull(table.getToolTipText(outOfBoundsEvent));
    }
}
