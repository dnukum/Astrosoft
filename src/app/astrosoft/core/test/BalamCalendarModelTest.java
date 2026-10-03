package app.astrosoft.core.test;

import java.util.TimeZone;

import junit.framework.TestCase;
import app.astrosoft.beans.BalamDateCell;
import app.astrosoft.beans.ChandraBalamResult;
import app.astrosoft.beans.TaraBalamResult;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.ui.table.MapTableRow;
import app.astrosoft.ui.table.TableData;
import app.astrosoft.core.BalamCalendarModel;

public class BalamCalendarModelTest extends TestCase {

    public void testBuildTableWithoutPartnerHasThreeColumns() {
        BalamCalendarModel model = new BalamCalendarModel(TimeZone.getTimeZone("Asia/Kolkata"), null);
        TableData<MapTableRow> data = model.buildTable(2026, 10, null);

        assertTrue(data.getRowCount() > 0);
        MapTableRow first = data.getRow(0);
        assertTrue(first.getColumnData(AstrosoftTableColumn.Date) instanceof BalamDateCell);
        assertEquals("-", first.getColumnData(AstrosoftTableColumn.TaraBalam));
        assertEquals("-", first.getColumnData(AstrosoftTableColumn.ChandraBalam));
    }

    public void testDetailsWithoutPartnerReturnsSelfOnly() {
        BalamCalendarModel model = new BalamCalendarModel(TimeZone.getTimeZone("Asia/Kolkata"), null);
        TableData<MapTableRow> data = model.buildTable(2026, 10, null);

        BalamCalendarModel.BalamDetails details = model.buildDetails(data, 0, false);
        assertNotNull(details.getDateCell());
        assertNull(details.getSelfTara());
        assertNull(details.getSelfChandra());
        assertNull(details.getPartnerTara());
        assertNull(details.getPartnerChandra());
    }

    public void testDetailsWithSyntheticPartnerColumns() {
        BalamCalendarModel model = new BalamCalendarModel(TimeZone.getTimeZone("Asia/Kolkata"), null);

        app.astrosoft.ui.table.DefaultColumnMetaData meta = new app.astrosoft.ui.table.DefaultColumnMetaData(
            AstrosoftTableColumn.Date,
            AstrosoftTableColumn.TaraBalam,
            AstrosoftTableColumn.ChandraBalam,
            AstrosoftTableColumn.SpouseTaraBalam,
            AstrosoftTableColumn.SpouseChandraBalam
        );
        app.astrosoft.ui.table.MapTableRowHelper helper = new app.astrosoft.ui.table.MapTableRowHelper(meta);

        java.util.Date start = new java.util.Date(0L);
        java.util.Date end = new java.util.Date(60L * 60L * 1000L);
        app.astrosoft.beans.BalamRowBean bean = new app.astrosoft.beans.BalamRowBean(
            start,
            end,
            app.astrosoft.consts.Nakshathra.Ashwini,
            app.astrosoft.consts.Rasi.Mesha
        );
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, hh:mm a");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

        MapTableRow row = helper.createRow(
            new BalamDateCell(bean, sdf),
            "-",
            "-",
            new TaraBalamResult(app.astrosoft.consts.Tara.SAMPAT, 1, app.astrosoft.core.BalamRank.BEST),
            new ChandraBalamResult(app.astrosoft.consts.ChandraHouse.HOUSE_3, app.astrosoft.core.BalamRank.BEST)
        );
        TableData<MapTableRow> data = app.astrosoft.ui.table.TableDataFactory.getTableData(java.util.Arrays.asList(row));

        BalamCalendarModel.BalamDetails details = model.buildDetails(data, 0, true);
        assertNotNull(details.getDateCell());
        assertNotNull(details.getPartnerTara());
        assertNotNull(details.getPartnerChandra());
        assertEquals(app.astrosoft.consts.Tara.SAMPAT, details.getPartnerTara().getTara());
        assertEquals(app.astrosoft.consts.ChandraHouse.HOUSE_3, details.getPartnerChandra().getHouse());
    }
}
