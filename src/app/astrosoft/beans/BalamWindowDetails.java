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
        public Entry(Date start, Date end, Paksha paksha, Thithi thithi, int weekday, boolean warning) {
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
    }
    private final long start, end;
    private final TimeZone timezone;
    private final List<Entry> entries;
    public BalamWindowDetails(Date start, Date end, TimeZone timezone, List<Entry> entries) {
        this.start = start.getTime(); this.end = end.getTime();
        this.timezone = (TimeZone) timezone.clone();
        this.entries = Collections.unmodifiableList(new ArrayList<Entry>(entries));
    }
    public Date getStart() { return new Date(start); }
    public Date getEnd() { return new Date(end); }
    public TimeZone getTimeZone() { return (TimeZone) timezone.clone(); }
    public List<Entry> getEntries() { return entries; }
}
