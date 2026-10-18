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

import app.astrosoft.beans.MuhurtaProfile;
import app.astrosoft.beans.Place;

public class BalamCalendarModel {

    private final TimeZone timeZone;
    private final MuhurtaProfile primaryProfile;
    private final Place place;

    public BalamCalendarModel(TimeZone timeZone, MuhurtaProfile primaryProfile) {
        this(timeZone, primaryProfile, app.astrosoft.ui.AstroSoft.getPreferences().getPlace());
    }

    public BalamCalendarModel(TimeZone timeZone, MuhurtaProfile primaryProfile, Place place) {
        this.timeZone = timeZone;
        this.primaryProfile = primaryProfile;
        this.place = place;
    }

    public TableData<MapTableRow> buildTable(int year, int month, MuhurtaProfile partnerProfile) {
        return buildTable(year, month, partnerProfile, false);
    }

    public TableData<MapTableRow> buildTable(int year, int month, MuhurtaProfile partnerProfile, boolean filter) {
        List<BalamRowBean> engineRows = BalamCalendarEngine.generateCalendar(year, month, timeZone);
        if (filter) {
            engineRows = filterAndTrimRows(engineRows, partnerProfile);
        }
        List<MapTableRow> tableRows = new ArrayList<MapTableRow>();

        List<AstrosoftTableColumn> cols = new ArrayList<AstrosoftTableColumn>();
        cols.add(AstrosoftTableColumn.Date);
        cols.add(AstrosoftTableColumn.TaraBalam);
        cols.add(AstrosoftTableColumn.ChandraBalam);
        if (partnerProfile != null) {
            cols.add(AstrosoftTableColumn.SpouseTaraBalam);
            cols.add(AstrosoftTableColumn.SpouseChandraBalam);
        }

        cols.add(AstrosoftTableColumn.Match);

        DefaultColumnMetaData meta = new DefaultColumnMetaData(cols);
        MapTableRowHelper helper = new MapTableRowHelper(meta);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a");
        sdf.setTimeZone(timeZone);

        Nakshathra birthNak = null;
        Rasi birthRasi = null;
        Nakshathra partnerNak = null;
        Rasi partnerRasi = null;

        if (primaryProfile != null) {
            birthNak = primaryProfile.getNakshathra();
            birthRasi = primaryProfile.getRasi();
        }
        if (partnerProfile != null) {
            partnerNak = partnerProfile.getNakshathra();
            partnerRasi = partnerProfile.getRasi();
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

            java.util.EnumSet<Nakshathra> matches = null;
            if (taraObj instanceof TaraBalamResult && chandraObj instanceof ChandraBalamResult) {
                matches = MyMuhurta.find_tara_chandra_match(birthNak, birthRasi,
                    ((TaraBalamResult) taraObj).getTara().getNumber(),
                    ((ChandraBalamResult) chandraObj).getHouse().getHouseNumber()).getMatches();
            }
            if (pTaraObj instanceof TaraBalamResult && pChandraObj instanceof ChandraBalamResult) {
                java.util.EnumSet<Nakshathra> partnerMatches = MyMuhurta.find_tara_chandra_match(
                    partnerNak, partnerRasi, ((TaraBalamResult) pTaraObj).getTara().getNumber(),
                    ((ChandraBalamResult) pChandraObj).getHouse().getHouseNumber()).getMatches();
                if (matches == null) matches = partnerMatches;
                else matches.retainAll(partnerMatches);
            }
            Object matchCell = matches == null ? "-" : new app.astrosoft.beans.BalamMatchCell(matches);

            BalamDateCell dateCell = new BalamDateCell(row, sdf);
            MapTableRow tRow;

            if (partnerProfile == null) {
                tRow = helper.createRow(dateCell, taraObj, chandraObj, matchCell);
            } else {
                tRow = helper.createRow(dateCell, taraObj, chandraObj, pTaraObj, pChandraObj, matchCell);
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

    public List<BalamRowBean> filterAndTrimRows(List<BalamRowBean> rawRows, MuhurtaProfile partnerProfile) {
        if (rawRows == null || rawRows.isEmpty()) {
            return new ArrayList<BalamRowBean>();
        }
        List<BalamRowBean> filtered = new ArrayList<BalamRowBean>();
        for (BalamRowBean row : rawRows) {
            if (isBalamAccepted(row, primaryProfile) && (partnerProfile == null || isBalamAccepted(row, partnerProfile))) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static boolean isBalamAccepted(BalamRowBean row, MuhurtaProfile profile) {
        if (profile == null || profile.getNakshathra() == null || profile.getRasi() == null) {
            return true;
        }
        int taraNumber = MyMuhurta.getTaraBalamResult(profile.getNakshathra(), row.getTransitNakshathra()).getTara().getNumber();
        if (taraNumber == 7) {
            return false;
        }
        int chandraHouse = MyMuhurta.getChandraBalamResult(profile.getRasi(), row.getTransitRasi()).getHouse().getHouseNumber();
        if (chandraHouse == 8) {
            return false;
        }
        return true;
    }

    public static List<BalamRowBean> filterAndTrimRow(BalamRowBean row, MuhurtaProfile primaryProfile,
            MuhurtaProfile partnerProfile, List<PanchangaWindowEngine.Segment> segments) {
        List<BalamRowBean> out = new ArrayList<BalamRowBean>();
        if (isBalamAccepted(row, primaryProfile) && (partnerProfile == null || isBalamAccepted(row, partnerProfile))) {
            out.add(row);
        }
        return out;
    }
}
