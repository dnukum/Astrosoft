package app.astrosoft.core.test;
import java.util.*;
import java.lang.reflect.*;
import app.astrosoft.consts.*;
import app.astrosoft.beans.*;
import app.astrosoft.core.*;

public class NakshatraEventSuitabilityTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        // Each row records the checked Excel columns, left to right, zero-based.
        String[] expected = {"347", "", "", "0134567", "0124567", "23", "1234567", "1234567", "",
            "0458", "", "01234567", "0123467", "2348", "03458", "", "034567", "", "07", "",
            "0124567", "0123567", "2347", "03567", "", "01234567", "01234567"};
        check(expected.length == Nakshathra.values().length, "All 27 Excel rows");
        check(MuhurtaEvent.values().length == 9, "All nine categories");
        for (Nakshathra star : Nakshathra.values()) {
            EnumSet<MuhurtaEvent> wanted = EnumSet.noneOf(MuhurtaEvent.class);
            for (char c : expected[star.ordinal()].toCharArray()) wanted.add(MuhurtaEvent.values()[c-'0']);
            check(NakshatraEventSuitability.eventsFor(star).equals(wanted), "Exact Excel row: " + star.name());
        }
        EnumSet<MuhurtaEvent> copy = NakshatraEventSuitability.eventsFor(Nakshathra.Magam);
        copy.clear();
        check(NakshatraEventSuitability.eventsFor(Nakshathra.Magam).contains(MuhurtaEvent.General), "Defensive catalog result");
        try { NakshatraEventSuitability.eventsFor(null); throw new AssertionError("Null star accepted"); }
        catch (IllegalArgumentException expectedException) { }
        Date now = new Date();
        MyMuhurta core = new MyMuhurta(now, now, null, null);
        Date start = new Date(1767225600000L), end = new Date(start.getTime()+3600000L);
        EnumSet<Nakshathra> matches = EnumSet.of(Nakshathra.Magam, Nakshathra.Ashwini, Nakshathra.Bharani);
        BalamWindowDetails details = core.getBalamWindowDetails(start, end, matches);
        matches.clear();
        check(details.getSuitabilities().size() == 3, "Core copies input matches");
        check(details.getSuitabilities().get(0).getStar() == Nakshathra.Ashwini, "Nakshatra ordering");
        try { details.getSuitabilities().clear(); throw new AssertionError("Mutable bean list"); }
        catch (UnsupportedOperationException expectedException) { }
        details.getSuitabilities().get(2).getEvents().clear();
        check(details.getSuitabilities().get(2).getEvents().contains(MuhurtaEvent.General), "Defensive bean event set");
        Class<?> panel = Class.forName("app.astrosoft.ui.comp.BalamCalendarPanel");
        Method render = panel.getDeclaredMethod("buildDetailsHtml", BalamWindowDetails.class, Nakshathra.class, Rasi.class);
        render.setAccessible(true);
        String html = (String)render.invoke(null, details, Nakshathra.Revathi, Rasi.Meena);
        check(html.contains("Education Commencement, Vehicle Buying, Travel"), "Ashwini category order");
        check(html.contains("<font color='red'>Not recommended</font>"), "Empty Excel row message in red");
        check(html.contains("<br><br><b>Transit Moon:</b>"), "Blank line after star entries");
        check(html.contains("Revathi in Meena  (Mercury)"), "Transit lord inline in parentheses");
        check(!html.contains("<b>Lord:</b>"), "No separate lord line");
        check(html.indexOf("General") < html.indexOf("Transit Moon"), "Suitability before transit line");
        check(core.getBalamWindowDetails(start,end).getSuitabilities().isEmpty(), "Legacy caller remains compatible");
        check(core.getBalamWindowDetails(start,end,EnumSet.noneOf(Nakshathra.class)).getSuitabilities().isEmpty(), "No fabricated fallback");
        BalamWindowDetails empty = core.getBalamWindowDetails(start,end,EnumSet.noneOf(Nakshathra.class));
        String cleared = (String)render.invoke(null, empty, Nakshathra.Revathi, Rasi.Meena);
        check(!cleared.contains("General") && !cleared.contains("Education Commencement"), "No stale rendered events");
        System.out.println("NakshatraEventSuitabilityTest: all 27 rows, bean, Core and renderer checks passed");
    }
}
