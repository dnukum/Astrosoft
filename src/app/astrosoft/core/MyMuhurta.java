package app.astrosoft.core;

import java.util.EnumSet;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Tara;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;
import app.astrosoft.beans.Place;
import app.astrosoft.beans.ChandraBalamResult;
import app.astrosoft.beans.MuhurtaProfile;
import app.astrosoft.beans.NakshathraPada;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.beans.TaraBalamResult;
import app.astrosoft.consts.AmirthathiYoga;
import app.astrosoft.util.AstroUtil;
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
    private app.astrosoft.beans.MuhurtaProfile userProfile;
    private Horoscope userHoroscope;
    private Panchang pan;
    private final Date eventInstant;
    private Horoscope eventHoroscope;

    public MyMuhurta(Date date, Date time, Place place, app.astrosoft.beans.MuhurtaProfile profile) {
        this.selectedDate = date;
        this.selectedTime = time;
        this.selectedPlace = place;
        this.userProfile = profile;
        this.userHoroscope = (profile != null) ? profile.getHoroscope() : null;
        
        java.util.TimeZone tz = java.util.TimeZone.getDefault();
        if (selectedPlace != null) {
            tz = selectedPlace.astrosoftTimeZone().getTimeZone();
        }
        
        Calendar cal = Calendar.getInstance(tz);
        Calendar systemCal = Calendar.getInstance(tz);
        systemCal.setTime(selectedDate);
        cal.set(Calendar.YEAR, systemCal.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, systemCal.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, systemCal.get(Calendar.DAY_OF_MONTH));
        
        Calendar tCal = Calendar.getInstance(tz);
        tCal.setTime(selectedTime);
        cal.set(Calendar.HOUR_OF_DAY, tCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, tCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, tCal.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, 0);
        
        eventInstant = new Date(cal.getTimeInMillis());
        if (selectedPlace != null) {
            pan = new Panchang(cal.getTime(), selectedPlace);
        } else {
            pan = new Panchang(cal.getTime());
        }
        
        Place placeToUse = selectedPlace != null ? selectedPlace : app.astrosoft.ui.AstroSoft.getPreferences().getPlace();
        app.astrosoft.beans.BirthData eventBD = new app.astrosoft.beans.BirthData("Muhurta", app.astrosoft.consts.Sex.Male, cal.get(Calendar.DATE), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), placeToUse);
        eventHoroscope = new Horoscope(eventBD);
    }

    public MyMuhurta(Date date, Date time, Place place) {
        this(date, time, place, (app.astrosoft.beans.MuhurtaProfile) null);
    }

    public Horoscope getEventHoroscope() {
        return eventHoroscope;
    }

    public Date getEventInstant() { return new Date(eventInstant.getTime()); }

    /** Domain orchestration; call outside the Swing event-dispatch thread. */
    public app.astrosoft.beans.BalamWindowDetails getBalamWindowDetails(Date start, Date end) {
        return getBalamWindowDetails(start, end, java.util.EnumSet.noneOf(Nakshathra.class));
    }

    public app.astrosoft.beans.BalamWindowDetails getBalamWindowDetails(Date start, Date end,
            java.util.EnumSet<Nakshathra> matches) {
        if (matches == null) throw new IllegalArgumentException("Matching stars required");
        java.util.EnumSet<Nakshathra> selectedMatches = matches.clone();
        if (start == null || end == null) throw new IllegalArgumentException("Window required");
        Date windowStart = new Date(start.getTime());
        Date windowEnd = new Date(end.getTime());
        java.util.TimeZone tz = selectedPlace == null ? java.util.TimeZone.getDefault()
            : selectedPlace.astrosoftTimeZone().getTimeZone();
        List<app.astrosoft.beans.BalamWindowDetails.Entry> entries = new ArrayList<>();
        for (PanchangaWindowEngine.Segment segment : PanchangaWindowEngine.generate(windowStart, windowEnd, tz, amirthathiPlace())) {
            app.astrosoft.consts.AmirthathiYoga yoga = app.astrosoft.consts.AmirthathiYoga
                .ofWeekdayAndNakshatra(segment.vara,
                    segment.nak, false).getFirst();
            entries.add(new app.astrosoft.beans.BalamWindowDetails.Entry(segment.start, segment.end,
                segment.paksha, segment.thithi, segment.weekday, segment.isWarning(), yoga, segment.yoga, segment.karana));
        }
        List<app.astrosoft.beans.BalamWindowDetails.Suitability> suitability = new ArrayList<>();
        for (Nakshathra star : selectedMatches) {
            suitability.add(new app.astrosoft.beans.BalamWindowDetails.Suitability(star,
                NakshatraEventSuitability.eventsFor(star)));
        }
        return new app.astrosoft.beans.BalamWindowDetails(windowStart, windowEnd, tz, entries, suitability, MuhurtaWindowsCalculator.forWindow(windowStart.getTime(),windowEnd.getTime(),tz,amirthathiPlace()));
    }

    public TableData<MapTableRow> getTableData() {
        List<MapTableRow> rows = new ArrayList<MapTableRow>();
        DefaultColumnMetaData meta = new DefaultColumnMetaData(AstrosoftTableColumn.keyvalCols());
        meta.localizeColumns();
        MapTableRowHelper helper = new MapTableRowHelper(meta);
        
        if (userProfile != null && userProfile.getName() != null && !userProfile.getName().isEmpty()) {
            rows.add(helper.createRow(app.astrosoft.consts.DisplayStrings.NAME_STR, userProfile.getName()));
        }
        
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("MMMM d, yyyy");
        java.text.SimpleDateFormat tf = new java.text.SimpleDateFormat("hh:mm:ss a");
        java.util.TimeZone tz = selectedPlace == null ? java.util.TimeZone.getDefault()
            : selectedPlace.astrosoftTimeZone().getTimeZone();
        df.setTimeZone(tz);
        tf.setTimeZone(tz);
        String dateStr = df.format(eventInstant);
        String timeStr = tf.format(eventInstant);
        
        String placeStr = "Default Place";
        String tzStr = "";
        if (selectedPlace != null) {
            placeStr = selectedPlace.city().trim();
            if (selectedPlace.state() != null && !selectedPlace.state().trim().isEmpty()) placeStr += ", " + selectedPlace.state().trim();
            if (selectedPlace.country() != null && !selectedPlace.country().trim().isEmpty()) placeStr += ", " + selectedPlace.country().trim();
            tzStr = selectedPlace.astrosoftTimeZone().toString(eventInstant);
        }
        
        rows.add(helper.createRow(app.astrosoft.consts.DisplayStrings.DATE_STR, dateStr));
        rows.add(helper.createRow("Time", timeStr + "," + tzStr));
        rows.add(helper.createRow("Location", placeStr));
        
        TableData<MapTableRow> panchangRows = pan.getPanchangTableData(true);
        for (String sunLabel : new String[] {app.astrosoft.consts.DisplayStrings.SUNRISE_STR.toString(),
                app.astrosoft.consts.DisplayStrings.SUNSET_STR.toString()}) {
            for (int i = 0; i < panchangRows.getRowCount(); i++) {
                MapTableRow row = panchangRows.getRow(i);
                if (sunLabel.equals(row.getColumnData(AstrosoftTableColumn.Key).toString())) rows.add(row);
            }
        }
        java.util.Calendar timingDate=java.util.Calendar.getInstance(tz);timingDate.setTime(eventInstant);
        java.util.List<MuhurtaWindowsCalculator.Window> dailyTimings=MuhurtaWindowsCalculator.forDay(timingDate,amirthathiPlace());
        String amirthathiDetails = getAmirthathiDayDetails(tz);
        for (int i = 0; i < panchangRows.getRowCount(); i++) {
            MapTableRow row = panchangRows.getRow(i);
            String key = row.getColumnData(AstrosoftTableColumn.Key).toString();
            if (key.equals(app.astrosoft.consts.DisplayStrings.DATE_STR.toString())
                    || key.equals(app.astrosoft.consts.DisplayStrings.SUNRISE_STR.toString())
                    || key.equals(app.astrosoft.consts.DisplayStrings.SUNSET_STR.toString())) continue;
            rows.add(row);
            if(key.startsWith(app.astrosoft.consts.DisplayStrings.AUS_TIME_STR.toString()) && dailyTimings.isEmpty()){
                rows.add(helper.createRow("Abhijit Muhurta","Unavailable: invalid solar event ordering"));
                rows.add(helper.createRow("Brahma Muhurta","Unavailable: invalid solar event ordering"));
            }
            if(key.startsWith(app.astrosoft.consts.DisplayStrings.AUS_TIME_STR.toString()))
                for(MuhurtaWindowsCalculator.Window timing:dailyTimings)
                    if(!timing.kind.equals("Subha Hora"))rows.add(helper.createRow(timing.kind,MuhurtaWindowsCalculator.format(timing,tz)));
            if (key.startsWith(app.astrosoft.consts.DisplayStrings.YOGA_STR.toString()))
                rows.add(helper.createRow("Amirthathi Yoga", amirthathiDetails));
        }

        if (userProfile != null && userProfile.getNakshathra() != null && userProfile.getRasi() != null) {
            app.astrosoft.consts.Nakshathra birthNak = userProfile.getNakshathra();
            app.astrosoft.beans.PlanetaryInfo pi = (eventHoroscope != null) ? eventHoroscope.getPlanetaryInfo() : null;
            app.astrosoft.beans.NakshathraPada transitNakPada = (pi != null && pi.getPlanetNakshathra() != null) ? pi.getPlanetNakshathra().get(Planet.Moon) : null;
            app.astrosoft.consts.Nakshathra transitNak = (transitNakPada != null) ? transitNakPada.getNak() : pan.getNakshathra();
            
            int taraIndex = calcTaraIndex(birthNak, transitNak);
            Tara tara = Tara.of(taraIndex);
            rows.add(helper.createRow("Tara Balam", taraIndex + " (" + tara.getName() + ")"));
            
            app.astrosoft.consts.Rasi birthRasi = userProfile.getRasi();
            app.astrosoft.consts.Rasi transitRasi = (pi != null && pi.getPlanetRasi() != null) ? pi.getPlanetRasi().get(Planet.Moon) : pan.getRasi();
            
            int house = calcChandraHouse(birthRasi, transitRasi);
            rows.add(helper.createRow("Chandra Balam", String.valueOf(house)));
        }
        
        return TableDataFactory.getTableData(rows);
    }

    private Place amirthathiPlace() {
        return selectedPlace == null ? app.astrosoft.ui.AstroSoft.getPreferences().getPlace() : selectedPlace;
    }

    /** Current instant through next civil midnight; only real classification changes shown. */
    private String getAmirthathiDayDetails(java.util.TimeZone tz) {
        Calendar nextDay = Calendar.getInstance(tz);
        nextDay.setTime(eventInstant);
        nextDay.add(Calendar.DAY_OF_MONTH, 1);
        nextDay.set(Calendar.HOUR_OF_DAY, 0); nextDay.set(Calendar.MINUTE, 0);
        nextDay.set(Calendar.SECOND, 0); nextDay.set(Calendar.MILLISECOND, 0);
        java.text.SimpleDateFormat time = new java.text.SimpleDateFormat("dd MMM, HH:mm z");
        time.setTimeZone(tz);
        StringBuilder text = new StringBuilder();
        app.astrosoft.consts.AmirthathiYoga previous = null;
        for (PanchangaWindowEngine.Segment segment : PanchangaWindowEngine.generate(
                new Date(eventInstant.getTime()), new Date(nextDay.getTimeInMillis()), tz, amirthathiPlace())) {
            app.astrosoft.consts.AmirthathiYoga current = app.astrosoft.consts.AmirthathiYoga
                .ofWeekdayAndNakshatra(segment.vara, segment.nak, false).getFirst();
            if (current != previous) {
                if (text.length() > 0) text.append(" → (").append(time.format(segment.start)).append(") ");
                text.append(current.name()); previous = current;
            }
        }
        return text.toString();
    }

    public Panchang getPanchang() {
        return pan;
    }

    /** Inverse candidates using the fixed three-column Navatara table, not date searching. */
    public static app.astrosoft.beans.TaraChandraMatchResult find_tara_chandra_match(
            Nakshathra birthNak, Rasi birthRasi, int targetTara, int targetChandra) {
        if (birthNak == null || birthRasi == null || targetTara < 1 || targetTara > 9
                || targetChandra < 1 || targetChandra > 12) {
            throw new IllegalArgumentException("Birth star/sign required; Tara 1-9 and Chandra 1-12");
        }
        // Step 1: inclusive Chandra count, then sign-overlapping stars.
        Rasi targetRasi = Rasi.ofIndex((birthRasi.ordinal() + targetChandra - 1) % 12);
        EnumSet<Nakshathra> chandraSet = Nakshathra.ofRasi(targetRasi);
        // Step 2: inclusive Tara count, then the fixed table row across its three cycles.
        int primary = (birthNak.ordinal() + targetTara - 1) % 27;
        int row = primary % 9;
        EnumSet<Nakshathra> taraSet = EnumSet.of(Nakshathra.ofIndex(row),
            Nakshathra.ofIndex(row + 9), Nakshathra.ofIndex(row + 18));
        // Step 3: all matching logic belongs in Core; the bean only carries results.
        EnumSet<Nakshathra> matches = taraSet.clone();
        matches.retainAll(chandraSet);
        return new app.astrosoft.beans.TaraChandraMatchResult(taraSet, chandraSet, matches);
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

    public app.astrosoft.ui.table.TableData<app.astrosoft.ui.table.MapTableRow> getBalamCalendarTable(int year, int month, app.astrosoft.beans.MuhurtaProfile partner) {
        java.util.TimeZone tz = java.util.TimeZone.getDefault();
        if (selectedPlace != null) {
            tz = selectedPlace.astrosoftTimeZone().getTimeZone();
        }
        return new BalamCalendarModel(tz, userProfile).buildTable(year, month, partner);
    }

    public app.astrosoft.ui.table.TableData<app.astrosoft.ui.table.MapTableRow> getBalamCalendarTable(int year, int month, Horoscope partner) {
        return getBalamCalendarTable(year, month, partner != null ? new app.astrosoft.beans.MuhurtaProfile(partner) : null);
    }

    public String getSummaryText(Place place, MuhurtaProfile person1, MuhurtaProfile person2) {
        Place placeToUse = place != null ? place : amirthathiPlace();
        TimeZone tz = (placeToUse != null && placeToUse.astrosoftTimeZone() != null)
            ? placeToUse.astrosoftTimeZone().getTimeZone()
            : TimeZone.getDefault();

        SimpleDateFormat df = new SimpleDateFormat("MMMM d, yyyy");
        SimpleDateFormat tf = new SimpleDateFormat("hh:mm:ss a");
        df.setTimeZone(tz);
        tf.setTimeZone(tz);
        String dateStr = df.format(eventInstant);
        String timeStr = tf.format(eventInstant);

        String placeStr = "";
        if (placeToUse != null) {
            if (placeToUse.city() != null && !placeToUse.city().trim().isEmpty()) {
                placeStr = placeToUse.city().trim();
            }
            if (placeToUse.state() != null && !placeToUse.state().trim().isEmpty()) {
                placeStr += (placeStr.isEmpty() ? "" : ", ") + placeToUse.state().trim();
            }
            if (placeToUse.country() != null && !placeToUse.country().trim().isEmpty()) {
                placeStr += (placeStr.isEmpty() ? "" : ", ") + placeToUse.country().trim();
            }
        }
        if (placeStr.isEmpty()) {
            placeStr = "Default Place";
        }
        String tzStr = (placeToUse != null && placeToUse.astrosoftTimeZone() != null)
            ? placeToUse.astrosoftTimeZone().toString(eventInstant)
            : tz.getID();
        String locationWithTz = placeStr + ", " + tzStr;

        List<PanchangaWindowEngine.Segment> segments = (placeToUse != null)
            ? PanchangaWindowEngine.generate(eventInstant, new Date(eventInstant.getTime() + 60000), tz, placeToUse)
            : PanchangaWindowEngine.generate(eventInstant, new Date(eventInstant.getTime() + 60000), tz);
        PanchangaWindowEngine.Segment segment = segments.get(0);

        Horoscope horoscope = getEventHoroscope();
        PlanetaryInfo pi = horoscope.getPlanetaryInfo();

        String pakshaThithi = segment.paksha + " Paksha, " + segment.thithi;
        String vara = segment.vara.toString();

        NakshathraPada moonNakPada = pi.getPlanetNakshathra().get(Planet.Moon);
        String moonNakStr = moonNakPada != null
            ? (moonNakPada.getNak().toString() + " (Pada " + moonNakPada.getPada() + ")")
            : segment.nak.toString();

        AmirthathiYoga amirthathi = AmirthathiYoga.ofWeekdayAndNakshatra(segment.vara, segment.nak, false).getFirst();
        String amirthathiStr = amirthathi != null ? amirthathi.toString() : "";

        String yogaStr = segment.yoga.toString();
        String karanaStr = segment.karana.toString();

        Nakshathra transitNak = (moonNakPada != null) ? moonNakPada.getNak() : segment.nak;
        Rasi transitRasi = pi.getPlanetRasi().get(Planet.Moon);

        boolean hasP1 = person1 != null && person1.getNakshathra() != null && person1.getRasi() != null;
        boolean hasP2 = person2 != null && person2.getNakshathra() != null && person2.getRasi() != null;
        StringBuilder balamSb = new StringBuilder();

        if (hasP1 || hasP2) {
            if (hasP1) {
                TaraBalamResult tr1 = getTaraBalamResult(person1.getNakshathra(), transitNak);
                ChandraBalamResult cr1 = getChandraBalamResult(person1.getRasi(), transitRasi);
                String name1 = (person1.getName() != null && !person1.getName().trim().isEmpty()) ? person1.getName() : "Person 1";
                balamSb.append("Person 1 (").append(name1).append("): Tara Balam: ")
                       .append(tr1.getTara().getNumber()).append(" (").append(tr1.getTara().getName()).append(", ").append(tr1.getFinalRank()).append("), ")
                       .append("Chandra Balam: ").append(cr1.getHouse().getHouseNumber()).append(" (").append(cr1.getFinalRank()).append(")");
            }
            if (hasP2) {
                if (balamSb.length() > 0) {
                    balamSb.append("\n                          ");
                }
                TaraBalamResult tr2 = getTaraBalamResult(person2.getNakshathra(), transitNak);
                ChandraBalamResult cr2 = getChandraBalamResult(person2.getRasi(), transitRasi);
                String name2 = (person2.getName() != null && !person2.getName().trim().isEmpty()) ? person2.getName() : "Person 2";
                balamSb.append("Person 2 (").append(name2).append("): Tara Balam: ")
                       .append(tr2.getTara().getNumber()).append(" (").append(tr2.getTara().getName()).append(", ").append(tr2.getFinalRank()).append("), ")
                       .append("Chandra Balam: ").append(cr2.getHouse().getHouseNumber()).append(" (").append(cr2.getFinalRank()).append(")");
            }
        } else {
            balamSb.append("Not evaluated (No person selected)");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("1) Muhurta Date: ").append(dateStr).append("\n");
        sb.append("2) Muhurta Time: ").append(timeStr).append("\n");
        sb.append("3) Location with timezone: ").append(locationWithTz).append("\n");
        sb.append("4) Paksha and Thithi: ").append(pakshaThithi).append("\n");
        sb.append("5) Vara: ").append(vara).append("\n");
        sb.append("6) Nakshatra: ").append(moonNakStr).append("\n");
        sb.append("7) Amirthathi Yoga: ").append(amirthathiStr).append("\n");
        sb.append("8) Tara and Chandra Balam: ").append(balamSb.toString()).append("\n");
        sb.append("9) Panchanga Yoga: ").append(yogaStr).append("\n");
        sb.append("10) Karana: ").append(karanaStr).append("\n\n");

        sb.append("Planetary Positions:\n");

        Rasi ascRasi = pi.getPlanetRasi().get(Planet.Ascendant);
        List<Planet> planetList = Arrays.asList(
            Planet.Ascendant, Planet.Sun, Planet.Moon, Planet.Mars,
            Planet.Mercury, Planet.Jupiter, Planet.Venus, Planet.Saturn,
            Planet.Rahu, Planet.Ketu
        );

        for (int step = 0; step < 12; step++) {
            Rasi currentSign = Rasi.ofIndex((ascRasi.ordinal() + step) % 12);
            List<Planet> inSign = new ArrayList<>();
            for (Planet p : planetList) {
                if (pi.getPlanetRasi().get(p) == currentSign) {
                    inSign.add(p);
                }
            }
            if (inSign.isEmpty()) continue;

            inSign.sort((p1, p2) -> {
                if (p1 == Planet.Ascendant) return -1;
                if (p2 == Planet.Ascendant) return 1;
                Double pos1 = pi.getPlanetRasiPosition().get(p1);
                Double pos2 = pi.getPlanetRasiPosition().get(p2);
                return Double.compare(pos1 != null ? pos1 : 0.0, pos2 != null ? pos2 : 0.0);
            });

            for (Planet p : inSign) {
                Double rasiPos = pi.getPlanetRasiPosition().get(p);
                int[] dms = AstroUtil.int_dms(rasiPos != null ? rasiPos : 0.0);
                String dmsStr = String.format("%02d° %02d' %02d\"", Math.abs(dms[0]), Math.abs(dms[1]), Math.abs(dms[2]));
                NakshathraPada np = pi.getPlanetNakshathra().get(p);
                String nakWithPada = np != null ? (np.getNak().toString() + " (Pada " + np.getPada() + ")") : "";
                String pName = (p == Planet.Ascendant) ? "ASC" : p.toString();
                sb.append(pName).append(", ")
                  .append(currentSign.toString()).append(", ")
                  .append(dmsStr).append(", ")
                  .append(nakWithPada).append("\n");
            }
        }

        return sb.toString();
    }

    public String getSummaryText(MuhurtaProfile person1, MuhurtaProfile person2) {
        return getSummaryText(selectedPlace, person1, person2);
    }
}