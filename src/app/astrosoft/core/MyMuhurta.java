package app.astrosoft.core;

import java.util.EnumSet;
import app.astrosoft.beans.BalamRowBean;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import app.astrosoft.beans.Place;
import app.astrosoft.ui.table.MapTableRow;
import app.astrosoft.ui.table.TableData;
import app.astrosoft.ui.table.TableDataFactory;
import app.astrosoft.ui.table.MapTableRowHelper;
import app.astrosoft.ui.table.DefaultColumnMetaData;
import app.astrosoft.consts.AstrosoftTableColumn;

public class MyMuhurta {

    private Date selectedDate;
    private Date selectedTime;
    private Place selectedPlace;
    private Horoscope userHoroscope;
    private Panchang pan;

    public MyMuhurta(Date date, Date time, Place place, Horoscope horoscope) {
        this.selectedDate = date;
        this.selectedTime = time;
        this.selectedPlace = place;
        this.userHoroscope = horoscope;
        
        java.util.TimeZone tz = java.util.TimeZone.getDefault();
        if (selectedPlace != null) {
            tz = selectedPlace.astrosoftTimeZone().getTimeZone();
        }
        
        Calendar cal = Calendar.getInstance(tz);
        Calendar systemCal = Calendar.getInstance();
        systemCal.setTime(selectedDate);
        cal.set(Calendar.YEAR, systemCal.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, systemCal.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, systemCal.get(Calendar.DAY_OF_MONTH));
        
        Calendar tCal = Calendar.getInstance();
        tCal.setTime(selectedTime);
        cal.set(Calendar.HOUR_OF_DAY, tCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, tCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, 0);
        
        if (selectedPlace != null) {
            pan = new Panchang(cal.getTime(), selectedPlace);
        } else {
            pan = new Panchang(cal.getTime());
        }
    }

    public TableData<MapTableRow> getTableData() {
        List<MapTableRow> rows = new ArrayList<MapTableRow>();
        DefaultColumnMetaData meta = new DefaultColumnMetaData(AstrosoftTableColumn.keyvalCols());
        meta.localizeColumns();
        MapTableRowHelper helper = new MapTableRowHelper(meta);
        
        if (userHoroscope != null && userHoroscope.getPersonName() != null && !userHoroscope.getPersonName().isEmpty()) {
            rows.add(helper.createRow(app.astrosoft.consts.DisplayStrings.NAME_STR, userHoroscope.getPersonName()));
        }
        
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("MMMM d, yyyy");
        java.text.SimpleDateFormat tf = new java.text.SimpleDateFormat("hh:mm:ss a");
        String dateStr = df.format(selectedDate);
        String timeStr = tf.format(selectedTime);
        
        String placeStr = "Default Place";
        String tzStr = "";
        if (selectedPlace != null) {
            placeStr = selectedPlace.city().trim();
            if (selectedPlace.state() != null && !selectedPlace.state().trim().isEmpty()) placeStr += ", " + selectedPlace.state().trim();
            if (selectedPlace.country() != null && !selectedPlace.country().trim().isEmpty()) placeStr += ", " + selectedPlace.country().trim();
            tzStr = selectedPlace.astrosoftTimeZone().toString();
        }
        
        rows.add(helper.createRow(app.astrosoft.consts.DisplayStrings.DATE_STR, dateStr));
        rows.add(helper.createRow("Time", timeStr + "," + tzStr));
        rows.add(helper.createRow("Location", placeStr));
        
        for (int i=0; i<pan.getPanchangTableData().getRowCount(); i++) {
            app.astrosoft.ui.table.MapTableRow row = pan.getPanchangTableData().getRow(i);
            if (row.getColumnData(app.astrosoft.consts.AstrosoftTableColumn.Key).toString().equals(app.astrosoft.consts.DisplayStrings.DATE_STR.toString())) continue;
            rows.add(row);
        }
        
        if (userHoroscope != null) {
            app.astrosoft.consts.Nakshathra birthNak = userHoroscope.getNakshathra().getNak();
            app.astrosoft.consts.Nakshathra transitNak = pan.getNakshathra();
            
            BalamRank taraBalam = evalTaraBalam(birthNak, transitNak);
            int taraIndex = calcTaraIndex(birthNak, transitNak);
            
            rows.add(helper.createRow("Tara Balam", taraIndex + " (" + taraBalam.name() + ")"));
            
            app.astrosoft.consts.Rasi birthRasi = userHoroscope.getRasi();
            app.astrosoft.consts.Rasi transitRasi = pan.getRasi();
            
            BalamRank chandraBalam = evalChandraBalam(birthRasi, transitRasi);
            int house = calcChandraHouse(birthRasi, transitRasi);
            
            rows.add(helper.createRow("Chandra Balam", house + " (" + chandraBalam.name() + ")"));
        }
        
        return TableDataFactory.getTableData(rows);
    }

    public Panchang getPanchang() {
        return pan;
    }

    /**
     * Calculates the Tara Balam (Star Strength) between a birth Nakshatra and transit Nakshatra.
     * Returns a 1-based index (1-9) representing the Tara.
     */
    public static int calcTaraIndex(Nakshathra birth, Nakshathra transit) {
        return (((transit.ordinal() - birth.ordinal()) + 27) % 9) + 1;
    }
    
    /**
     * Returns the Paryaya (row) of the transit star relative to the birth star.
     * 1 = 1st cycle (stars 1-9)
     * 2 = 2nd cycle (stars 10-18)
     * 3 = 3rd cycle (stars 19-27)
     */
    public static int calcParyaya(Nakshathra birth, Nakshathra transit) {
        int diff = ((transit.ordinal() - birth.ordinal()) + 27) % 27;
        return (diff / 9) + 1;
    }

    /**
     * Evaluates Tara Balam based on the strict hierarchy rules.
     */
    public static BalamRank evalTaraBalam(Nakshathra birth, Nakshathra transit) {
        int taraIndex = calcTaraIndex(birth, transit);
        int paryaya = calcParyaya(birth, transit);
        
        switch (taraIndex) {
            case 2: // Sampat
            case 6: // Sadhana
            case 9: // Parama Mitra
                return BalamRank.BEST;
                
            case 4: // Kshema
            case 8: // Mitra
                return BalamRank.SECOND_BEST;
                
            case 1: // Janma
            case 5: // Pratyak
                return BalamRank.ACCEPTABLE; // Good for women/marriage if nullified
                
            case 3: // Vipat
                if (paryaya == 3) {
                    return BalamRank.CONDITIONAL; // No evil results in 3rd Paryaya
                }
                return BalamRank.AVOID; // Otherwise bad
                
            case 7: // Naidhana
                return BalamRank.STRICTLY_REJECTED;
                
            default:
                return BalamRank.AVOID;
        }
    }

    /**
     * Calculates the inclusive house count from birth Rasi to transit Rasi.
     * 1-based index (1 to 12).
     */
    public static int calcChandraHouse(Rasi birth, Rasi transit) {
        return (((transit.ordinal() - birth.ordinal()) + 12) % 12) + 1;
    }

    /**
     * Evaluates Chandra Balam based on house positions from Natal Moon.
     */
    public static BalamRank evalChandraBalam(Rasi birth, Rasi transit) {
        int house = calcChandraHouse(birth, transit);
        
        switch (house) {
            case 3:
            case 10:
            case 11:
                return BalamRank.BEST; // Upachaya/growth
                
            case 1:
            case 7:
                return BalamRank.ACCEPTABLE;
                
            case 6:
                return BalamRank.MIXED; // Upachaya, but generally avoided
                
            case 12:
                return BalamRank.AVOID; // Loss
                
            case 8:
                return BalamRank.STRICTLY_REJECTED; // Chandra Ashtama
                
            default:
                // 2, 4, 5, 9 are generally neutral/contextual depending on other rules
                return BalamRank.MIXED; 
        }
    }

    public static BalamRank evalOverallBalam(BalamRank tara, BalamRank chandra) {
        if (tara == BalamRank.STRICTLY_REJECTED || chandra == BalamRank.STRICTLY_REJECTED) return BalamRank.STRICTLY_REJECTED;
        if (tara == BalamRank.AVOID || chandra == BalamRank.AVOID) return BalamRank.AVOID;
        if (tara == BalamRank.CONDITIONAL || chandra == BalamRank.CONDITIONAL) return BalamRank.CONDITIONAL;
        if (tara == BalamRank.MIXED || chandra == BalamRank.MIXED) return BalamRank.MIXED;
        if (tara == BalamRank.BEST && chandra == BalamRank.BEST) return BalamRank.BEST;
        if (tara == BalamRank.BEST || chandra == BalamRank.BEST) return BalamRank.SECOND_BEST;
        return BalamRank.ACCEPTABLE;
    }

        
    public static app.astrosoft.beans.TaraBalamResult getTaraBalamResult(Nakshathra birth, Nakshathra transit) {
        int index = calcTaraIndex(birth, transit);
        int paryaya = calcParyaya(birth, transit);
        BalamRank rank = evalTaraBalam(birth, transit);
        return new app.astrosoft.beans.TaraBalamResult(app.astrosoft.consts.Tara.of(index), paryaya, rank);
    }

    public static app.astrosoft.beans.ChandraBalamResult getChandraBalamResult(Rasi birth, Rasi transit) {
        int house = calcChandraHouse(birth, transit);
        BalamRank rank = evalChandraBalam(birth, transit);
        return new app.astrosoft.beans.ChandraBalamResult(app.astrosoft.consts.ChandraHouse.of(house), rank);
    }

    public app.astrosoft.ui.table.TableData<app.astrosoft.ui.table.MapTableRow> getBalamCalendarTable(int year, int month) {
        java.util.TimeZone tz = java.util.TimeZone.getDefault();
        if (selectedPlace != null) {
            tz = selectedPlace.astrosoftTimeZone().getTimeZone();
        }
        
        java.util.List<BalamRowBean> engineRows = BalamCalendarEngine.generateCalendar(year, month, tz);
        
        java.util.List<app.astrosoft.ui.table.MapTableRow> tableRows = new java.util.ArrayList<>();
        
        java.util.List<app.astrosoft.consts.AstrosoftTableColumn> cols = new java.util.ArrayList<>();
        cols.add(app.astrosoft.consts.AstrosoftTableColumn.StartDate);
        cols.add(app.astrosoft.consts.AstrosoftTableColumn.EndDate);
        cols.add(app.astrosoft.consts.AstrosoftTableColumn.Nakshathra);
        cols.add(app.astrosoft.consts.AstrosoftTableColumn.Rasi);
        cols.add(app.astrosoft.consts.AstrosoftTableColumn.TaraBalam);
        cols.add(app.astrosoft.consts.AstrosoftTableColumn.ChandraBalam);
        cols.add(app.astrosoft.consts.AstrosoftTableColumn.OverallScore);
        
        app.astrosoft.ui.table.DefaultColumnMetaData meta = new app.astrosoft.ui.table.DefaultColumnMetaData(cols);
        app.astrosoft.ui.table.MapTableRowHelper helper = new app.astrosoft.ui.table.MapTableRowHelper(meta);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, hh:mm a");
        sdf.setTimeZone(tz);
        
        app.astrosoft.consts.Nakshathra birthNak = null;
        app.astrosoft.consts.Rasi birthRasi = null;
        
        if (userHoroscope != null) {
            birthNak = userHoroscope.getNakshathra().getNak();
            birthRasi = userHoroscope.getRasi();
        }

        for (BalamRowBean row : engineRows) {
            Object taraObj = "-";
            Object chandraObj = "-";
            String overallName = "-";
            
            if (birthNak != null && birthRasi != null) {
                app.astrosoft.beans.TaraBalamResult taraResult = getTaraBalamResult(birthNak, row.getTransitNakshathra());
                app.astrosoft.beans.ChandraBalamResult chandraResult = getChandraBalamResult(birthRasi, row.getTransitRasi());
                BalamRank overall = evalOverallBalam(taraResult.getFinalRank(), chandraResult.getFinalRank());
                
                // Now we store the ACTUAL OBJECTS directly in the MapTableRow, not just Strings!
                taraObj = taraResult;
                chandraObj = chandraResult;
                overallName = overall.name();
            }
            
            app.astrosoft.ui.table.MapTableRow tRow = helper.createRow(
                sdf.format(row.getStartTime()),
                sdf.format(row.getEndTime()),
                row.getTransitNakshathra().name(),
                row.getTransitRasi().name(),
                taraObj,
                chandraObj,
                overallName
            );
            
            tableRows.add(tRow);
        }
        
        return app.astrosoft.ui.table.TableDataFactory.getTableData(tableRows);
    }
}