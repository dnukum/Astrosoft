package app.astrosoft.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import app.astrosoft.beans.BalamDateCell;
import app.astrosoft.beans.BalamRowBean;
import app.astrosoft.beans.ChandraBalamResult;
import app.astrosoft.beans.TaraBalamResult;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import app.astrosoft.ui.table.DefaultColumnMetaData;
import app.astrosoft.ui.table.MapTableRow;
import app.astrosoft.ui.table.MapTableRowHelper;
import app.astrosoft.ui.table.TableData;
import app.astrosoft.ui.table.TableDataFactory;

public class BalamCalendarModel {

    private final TimeZone timeZone;
    private final Horoscope primaryHoroscope;

    public BalamCalendarModel(TimeZone timeZone, Horoscope primaryHoroscope) {
        this.timeZone = timeZone;
        this.primaryHoroscope = primaryHoroscope;
    }

    public TableData<MapTableRow> buildTable(int year, int month, Horoscope partnerHoroscope) {
        List<BalamRowBean> engineRows = BalamCalendarEngine.generateCalendar(year, month, timeZone);
        List<MapTableRow> tableRows = new ArrayList<MapTableRow>();

        List<AstrosoftTableColumn> cols = new ArrayList<AstrosoftTableColumn>();
        cols.add(AstrosoftTableColumn.Date);
        cols.add(AstrosoftTableColumn.TaraBalam);
        cols.add(AstrosoftTableColumn.ChandraBalam);
        if (partnerHoroscope != null) {
            cols.add(AstrosoftTableColumn.SpouseTaraBalam);
            cols.add(AstrosoftTableColumn.SpouseChandraBalam);
        }

        DefaultColumnMetaData meta = new DefaultColumnMetaData(cols);
        MapTableRowHelper helper = new MapTableRowHelper(meta);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a");
        sdf.setTimeZone(timeZone);

        Nakshathra birthNak = null;
        Rasi birthRasi = null;
        Nakshathra partnerNak = null;
        Rasi partnerRasi = null;

        if (primaryHoroscope != null) {
            birthNak = primaryHoroscope.getNakshathra().getNak();
            birthRasi = primaryHoroscope.getRasi();
        }
        if (partnerHoroscope != null) {
            partnerNak = partnerHoroscope.getNakshathra().getNak();
            partnerRasi = partnerHoroscope.getRasi();
        }

        for (BalamRowBean row : engineRows) {
            Object taraObj = "-";
            Object chandraObj = "-";
            Object pTaraObj = "-";
            Object pChandraObj = "-";

            if (birthNak != null && birthRasi != null) {
                taraObj = MyMuhurta.getTaraBalamResult(birthNak, row.getTransitNakshathra());
                chandraObj = MyMuhurta.getChandraBalamResult(birthRasi, row.getTransitRasi());
            }

            if (partnerNak != null && partnerRasi != null) {
                pTaraObj = MyMuhurta.getTaraBalamResult(partnerNak, row.getTransitNakshathra());
                pChandraObj = MyMuhurta.getChandraBalamResult(partnerRasi, row.getTransitRasi());
            }

            BalamDateCell dateCell = new BalamDateCell(row, sdf);
            MapTableRow tRow;

            if (partnerHoroscope == null) {
                tRow = helper.createRow(dateCell, taraObj, chandraObj);
            } else {
                tRow = helper.createRow(dateCell, taraObj, chandraObj, pTaraObj, pChandraObj);
            }
            tableRows.add(tRow);
        }

        return TableDataFactory.getTableData(tableRows);
    }

    public BalamDetails buildDetails(TableData<MapTableRow> data, int rowIndex, boolean hasPartner) {
        MapTableRow row = data.getRow(rowIndex);
        BalamDateCell dateCell = (BalamDateCell) row.getColumnData(AstrosoftTableColumn.Date);
        TaraBalamResult selfTara = asTara(row.getColumnData(AstrosoftTableColumn.TaraBalam));
        ChandraBalamResult selfChandra = asChandra(row.getColumnData(AstrosoftTableColumn.ChandraBalam));
        TaraBalamResult partnerTara = hasPartner ? asTara(row.getColumnData(AstrosoftTableColumn.SpouseTaraBalam)) : null;
        ChandraBalamResult partnerChandra = hasPartner ? asChandra(row.getColumnData(AstrosoftTableColumn.SpouseChandraBalam)) : null;
        return new BalamDetails(dateCell, selfTara, selfChandra, partnerTara, partnerChandra);
    }

    private TaraBalamResult asTara(Object value) {
        return value instanceof TaraBalamResult ? (TaraBalamResult) value : null;
    }

    private ChandraBalamResult asChandra(Object value) {
        return value instanceof ChandraBalamResult ? (ChandraBalamResult) value : null;
    }

    public static class BalamDetails {
        private final BalamDateCell dateCell;
        private final TaraBalamResult selfTara;
        private final ChandraBalamResult selfChandra;
        private final TaraBalamResult partnerTara;
        private final ChandraBalamResult partnerChandra;

        public BalamDetails(BalamDateCell dateCell,
                            TaraBalamResult selfTara,
                            ChandraBalamResult selfChandra,
                            TaraBalamResult partnerTara,
                            ChandraBalamResult partnerChandra) {
            this.dateCell = dateCell;
            this.selfTara = selfTara;
            this.selfChandra = selfChandra;
            this.partnerTara = partnerTara;
            this.partnerChandra = partnerChandra;
        }

        public BalamDateCell getDateCell() {
            return dateCell;
        }

        public TaraBalamResult getSelfTara() {
            return selfTara;
        }

        public ChandraBalamResult getSelfChandra() {
            return selfChandra;
        }

        public TaraBalamResult getPartnerTara() {
            return partnerTara;
        }

        public ChandraBalamResult getPartnerChandra() {
            return partnerChandra;
        }
    }
}
