package app.astrosoft.core;

import java.util.*;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Paksha;
import app.astrosoft.consts.Thithi;
import app.astrosoft.util.SwissHelper;
import swisseph.SweDate;

/** Half-open selected-window segments, split at tithi transitions and civil midnight. */
public final class BalamWindowEngine {
    private BalamWindowEngine() { }
    public interface PhaseSource { double elongation(long instant); }
    public static final class Segment {
        public final Date start, end;
        public final Paksha paksha;
        public final Thithi thithi;
        public final int weekday;
        Segment(long start, long end, int index, TimeZone tz) {
            this.start = new Date(start); this.end = new Date(end);
            paksha = index < 15 ? Paksha.Shukla : Paksha.Krishna;
            thithi = index == 29 ? Thithi.Amavasya : Thithi.ofIndex(index % 15);
            Calendar c = Calendar.getInstance(tz); c.setTimeInMillis(start);
            weekday = c.get(Calendar.DAY_OF_WEEK);
        }
        public boolean isWarning() {
            return thithi == Thithi.Amavasya || thithi == Thithi.Ashtami
                || thithi == Thithi.Navami || thithi == Thithi.Prathamai;
        }
    }
    private static int index(double phase) {
        if (!Double.isFinite(phase)) throw new IllegalArgumentException("Invalid lunar phase");
        return (int)(((phase % 360 + 360) % 360) / 12);
    }
    public static List<Segment> generate(Date start, Date end, TimeZone tz) {
        final SwissHelper swiss = new SwissHelper();
        return generate(start, end, tz, instant -> {
            swiss.setSweDate(new SweDate(instant / 86400000.0 + 2440587.5));
            EnumMap<Planet, Double> positions = swiss.getPlanetaryPosition(EnumSet.of(Planet.Sun, Planet.Moon));
            return positions.get(Planet.Moon) - positions.get(Planet.Sun);
        });
    }
    /** Phase must be continuous and advance by less than one tithi per hour. */
    public static List<Segment> generate(Date start, Date end, TimeZone tz, PhaseSource source) {
        if (start == null || end == null || tz == null || source == null)
            throw new IllegalArgumentException("Window, timezone and phase source required");
        if (end.before(start)) throw new IllegalArgumentException("Reversed window");
        List<Segment> result = new ArrayList<>();
        long cursor = start.getTime(), limit = end.getTime();
        while (cursor < limit) {
            int current = index(source.elongation(cursor));
            long probe = cursor, boundary = limit;
            while (probe < limit) {
                long next = Math.min(limit, probe + 3600000L);
                if (index(source.elongation(next)) != current) {
                    long low = probe, high = next;
                    while (high - low > 1) {
                        long middle = low + (high - low) / 2;
                        if (index(source.elongation(middle)) == current) low = middle;
                        else high = middle;
                    }
                    boundary = high;
                    break;
                }
                probe = next;
            }
            Calendar midnight = Calendar.getInstance(tz);
            midnight.setTimeInMillis(cursor);
            midnight.add(Calendar.DAY_OF_MONTH, 1);
            midnight.set(Calendar.HOUR_OF_DAY, 0); midnight.set(Calendar.MINUTE, 0);
            midnight.set(Calendar.SECOND, 0); midnight.set(Calendar.MILLISECOND, 0);
            boundary = Math.min(boundary, midnight.getTimeInMillis());
            if (boundary <= cursor) throw new IllegalStateException("Non-progressing window");
            result.add(new Segment(cursor, boundary, current, tz));
            cursor = boundary;
        }
        return result;
    }
}
