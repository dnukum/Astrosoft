package app.astrosoft.beans;

import java.util.*;
import app.astrosoft.consts.*;

/** Immutable Core result snapshot; contains no calculation or presentation logic. */
public final class BalamWindowDetails {
    public static final class Entry {
        private final long start, end;
        private final Paksha paksha;
        private final Thithi thithi;
        private final int weekday;
        private final boolean warning;
        private final AmirthathiYoga amirthathiYoga;
        private final Yoga yoga;
        private final Karana karana;
        public Entry(Date start, Date end, Paksha paksha, Thithi thithi, int weekday, boolean warning) {
            this(start, end, paksha, thithi, weekday, warning, null);
        }
        public Entry(Date start, Date end, Paksha paksha, Thithi thithi, int weekday, boolean warning,
                AmirthathiYoga amirthathiYoga) {
            this(start,end,paksha,thithi,weekday,warning,amirthathiYoga,null,null);
        }
        public Entry(Date start, Date end, Paksha paksha, Thithi thithi, int weekday, boolean warning,
                AmirthathiYoga amirthathiYoga, Yoga yoga, Karana karana) {
            this.yoga = yoga; this.karana = karana;
            this.amirthathiYoga = amirthathiYoga;
            this.start = start.getTime(); this.end = end.getTime();
            this.paksha = paksha; this.thithi = thithi;
            this.weekday = weekday; this.warning = warning;
        }
        public Date getStart() { return new Date(start); }
        public Date getEnd() { return new Date(end); }
        public Paksha getPaksha() { return paksha; }
        public Thithi getThithi() { return thithi; }
        public int getWeekday() { return weekday; }
        public boolean isWarning() { return warning; }
        public AmirthathiYoga getAmirthathiYoga() { return amirthathiYoga; }
        public Yoga getYoga() { return yoga; }
        public Karana getKarana() { return karana; }
    }
    public static final class Suitability {
        private final Nakshathra star;
        private final EnumSet<MuhurtaEvent> events;
        public Suitability(Nakshathra star, EnumSet<MuhurtaEvent> events) {
            if (star == null || events == null) throw new IllegalArgumentException("Star and events required");
            this.star = star;
            this.events = events.clone();
        }
        public Nakshathra getStar() { return star; }
        public EnumSet<MuhurtaEvent> getEvents() { return events.clone(); }
    }
    private final List<app.astrosoft.core.MuhurtaWindowsCalculator.Window> timingWindows;
    public List<app.astrosoft.core.MuhurtaWindowsCalculator.Window> getTimingWindows(){return timingWindows;}
    private final List<Suitability> suitabilities;
    private final long start, end;
    private final TimeZone timezone;
    private final List<Date> sunrises;
    private final List<Entry> entries;
    public BalamWindowDetails(Date start, Date end, TimeZone timezone, List<Entry> entries) {
        this(start, end, timezone, entries, Collections.<Suitability>emptyList());
    }
    public BalamWindowDetails(Date start, Date end, TimeZone timezone, List<Entry> entries,
            List<Suitability> suitabilities) {
        this(start,end,timezone,entries,suitabilities,Collections.<app.astrosoft.core.MuhurtaWindowsCalculator.Window>emptyList());
    }
    public BalamWindowDetails(Date start,Date end,TimeZone timezone,List<Entry> entries,List<Suitability> suitabilities,List<app.astrosoft.core.MuhurtaWindowsCalculator.Window> windows){
        this(start, end, timezone, entries, suitabilities, windows, Collections.<Date>emptyList());
    }
    public BalamWindowDetails(Date start, Date end, TimeZone timezone, List<Entry> entries,
            List<Suitability> suitabilities, List<app.astrosoft.core.MuhurtaWindowsCalculator.Window> windows,
            List<Date> sunrises) {
        this.timingWindows=Collections.unmodifiableList(new ArrayList<>(windows));
        this.suitabilities = Collections.unmodifiableList(new ArrayList<Suitability>(suitabilities));
        this.start = start.getTime(); this.end = end.getTime();
        this.timezone = (TimeZone) timezone.clone();
        this.entries = Collections.unmodifiableList(new ArrayList<Entry>(entries));
        List<Date> riseCopies = new ArrayList<>();
        if (sunrises != null) {
            for (Date d : sunrises) {
                if (d != null) riseCopies.add(new Date(d.getTime()));
            }
        }
        this.sunrises = Collections.unmodifiableList(riseCopies);
    }
    public Date getStart() { return new Date(start); }
    public Date getEnd() { return new Date(end); }
    public TimeZone getTimeZone() { return (TimeZone) timezone.clone(); }
    public List<Suitability> getSuitabilities() { return suitabilities; }
    public List<Entry> getEntries() { return entries; }
    public List<Date> getSunrises() {
        List<Date> result = new ArrayList<>();
        for (Date d : sunrises) result.add(new Date(d.getTime()));
        return Collections.unmodifiableList(result);
    }
}
