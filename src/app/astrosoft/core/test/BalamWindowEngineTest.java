package app.astrosoft.core.test;

import java.util.*;
import app.astrosoft.core.BalamWindowEngine;
import app.astrosoft.core.BalamWindowEngine.Segment;
import app.astrosoft.consts.*;

/** Deterministic boundaries plus real-ephemeris continuity smoke test. */
public class BalamWindowEngineTest {
    static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        List<Segment> s = BalamWindowEngine.generate(new Date(0), new Date(7200000), utc, t -> 6);
        check(s.size() == 1 && s.get(0).thithi == Thithi.Prathamai && s.get(0).isWarning(), "Single tithi");
        s = BalamWindowEngine.generate(new Date(0), new Date(7200000), utc, t -> 348 + t / 300000.0);
        check(s.size() == 2 && s.get(0).thithi == Thithi.Amavasya && s.get(1).thithi == Thithi.Prathamai, "New moon wrap");
        check(s.get(0).end.getTime() == 3600000 && s.get(1).start.equals(s.get(0).end), "Exact boundary");
        s = BalamWindowEngine.generate(new Date(3600000), new Date(7200000), utc, t -> 348 + t / 300000.0);
        check(s.size() == 1 && s.get(0).paksha == Paksha.Shukla, "Transition at start");
        s = BalamWindowEngine.generate(new Date(0), new Date(3600000), utc, t -> 348 + t / 300000.0);
        check(s.size() == 1 && s.get(0).thithi == Thithi.Amavasya, "Transition at end excluded");
        s = BalamWindowEngine.generate(new Date(0), new Date(10800000), utc, t -> 168 + t / 300000.0);
        check(s.size() == 3 && s.get(0).thithi == Thithi.Pournami && s.get(1).paksha == Paksha.Krishna, "Full moon and multiple transitions");
        for (int i = 0; i < 30; i++) {
            final double phase = i * 12 + 1;
            s = BalamWindowEngine.generate(new Date(0), new Date(1), utc, t -> phase);
            check(s.get(0).isWarning() == (i % 15 == 0 || i % 15 == 7 || i % 15 == 8 || i == 29), "Warning index " + i);
        }
        TimeZone la = TimeZone.getTimeZone("America/Los_Angeles");
        Calendar c = Calendar.getInstance(la); c.clear(); c.set(2026, Calendar.NOVEMBER, 1, 0, 0);
        Date start = c.getTime(); c.add(Calendar.DAY_OF_MONTH, 1); Date midnight = c.getTime(); c.add(Calendar.HOUR_OF_DAY, 1);
        s = BalamWindowEngine.generate(start, c.getTime(), la, t -> 30);
        check(s.size() == 2 && s.get(0).end.equals(midnight) && s.get(0).weekday != s.get(1).weekday, "DST civil midnight");
        check(midnight.getTime() - start.getTime() == 25 * 3600000L, "25-hour DST day");
        check(BalamWindowEngine.generate(start, start, la, t -> 0).isEmpty(), "Empty window");
        boolean rejected = false;
        try { BalamWindowEngine.generate(midnight, start, la, t -> 0); } catch (IllegalArgumentException e) { rejected = true; }
        check(rejected, "Reversed window");
        s = BalamWindowEngine.generate(new Date(1791158400000L), new Date(1791417600000L), la);
        check(!s.isEmpty() && s.get(0).start.getTime() == 1791158400000L && s.get(s.size()-1).end.getTime() == 1791417600000L, "Ephemeris coverage");
        for (int i=1; i<s.size(); i++) check(s.get(i-1).end.equals(s.get(i).start), "No gaps");
        System.out.println("BalamWindowEngineTest: all checks passed");
    }
}
