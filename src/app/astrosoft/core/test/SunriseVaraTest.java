package app.astrosoft.core.test;

import java.util.*;
import java.time.*;
import app.astrosoft.core.*;
import app.astrosoft.consts.*;

/** Controlled sunrise/longitude fixtures keep boundary expectations independent of ephemeris. */
public class SunriseVaraTest {
    private static long instant(String value, TimeZone tz) {
        return LocalDateTime.parse(value).atZone(tz.toZoneId()).toInstant().toEpochMilli();
    }
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        SunriseVara vara = new SunriseVara(tz, date -> {
            date.set(Calendar.HOUR_OF_DAY, 6); return date.getTimeInMillis();
        });
        check(vara.weekdayAt(instant("2026-10-07T05:59:59",tz)) == WeekDay.Tuesday, "Before sunrise");
        check(vara.weekdayAt(instant("2026-10-07T06:00:00",tz)) == WeekDay.Wednesday, "Exactly sunrise");
        check(vara.weekdayAt(instant("2026-10-07T08:00:00",tz)) == WeekDay.Wednesday, "After sunrise");
        check(vara.weekdayAt(instant("2026-10-04T05:00:00",tz)) == WeekDay.Saturday, "Week wrap");
        for (SunriseVara.SunriseSource source : new SunriseVara.SunriseSource[] {
                date -> { throw new IllegalStateException("Injected failure"); }, date -> Long.MIN_VALUE }) {
            SunriseVara fallback = new SunriseVara(tz, source);
            check(fallback.weekdayAt(instant("2026-10-07T05:59:59",tz)) == WeekDay.Tuesday, "Fallback before 06:00");
            check(fallback.weekdayAt(instant("2026-10-07T06:00:00",tz)) == WeekDay.Wednesday, "Fallback at 06:00");
        }
        PanchangaWindowEngine.LongitudeSource fixed = t -> new double[]{0, 1};
        long start=instant("2026-10-06T23:00:00",tz), end=instant("2026-10-07T08:00:00",tz);
        List<PanchangaWindowEngine.Segment> segments = PanchangaWindowEngine.generate(new Date(start),new Date(end),tz,fixed,vara);
        check(segments.size()==3,"Midnight and sunrise boundaries");
        check(segments.get(1).weekday==Calendar.WEDNESDAY && segments.get(1).vara==WeekDay.Tuesday,"Civil day preserved independently");
        check(segments.get(1).end.getTime()==instant("2026-10-07T06:00:00",tz),"Sunrise split");
        check(AmirthathiYoga.ofWeekdayAndNakshatra(segments.get(1).vara,segments.get(1).nak,false).getFirst()==AmirthathiYoga.Siddha,"Tuesday Ashwini");
        check(AmirthathiYoga.ofWeekdayAndNakshatra(segments.get(2).vara,segments.get(2).nak,false).getFirst()==AmirthathiYoga.Marana,"Wednesday Ashwini");
        long sunrise=instant("2026-10-07T06:00:00",tz);
        PanchangaWindowEngine.LongitudeSource simultaneous = t -> new double[]{0,t<sunrise?1:14};
        segments=PanchangaWindowEngine.generate(new Date(sunrise-1000),new Date(sunrise+1000),tz,simultaneous,vara);
        check(segments.size()==2 && segments.get(1).vara==WeekDay.Wednesday && segments.get(1).nak==Nakshathra.Bharani,"Simultaneous transition");
        long transit=instant("2026-10-07T10:15:00",tz);
        segments=PanchangaWindowEngine.generate(new Date(transit-1000),new Date(transit+1000),tz,t->new double[]{0,t<transit?359:1},vara);
        check(segments.size()==2 && segments.get(0).nak==Nakshathra.Revathi && segments.get(1).nak==Nakshathra.Ashwini,"Star wrap");
        TimeZone berlin=TimeZone.getTimeZone("Europe/Berlin");
        SunriseVara dst=new SunriseVara(berlin,date->{date.set(Calendar.HOUR_OF_DAY,6);return date.getTimeInMillis();});
        check(dst.weekdayAt(instant("2026-10-25T05:59:59",berlin))==WeekDay.Saturday,"DST date before sunrise");
        check(dst.weekdayAt(instant("2026-10-25T06:00:00",berlin))==WeekDay.Sunday,"DST date at sunrise");
        check(PanchangaWindowEngine.generate(new Date(start),new Date(start),tz,fixed,vara).isEmpty(),"Empty window");
        app.astrosoft.beans.Place place=new app.astrosoft.beans.Place("Sammamish","Washington","US",47.6163,-122.0356,"America/Los_Angeles");
        SunriseVara real=SunriseVara.forPlace(tz,place);
        long actualRise=real.sunriseAt(instant("2026-10-07T12:00:00",tz));
        check(real.weekdayAt(actualRise-1)==WeekDay.Tuesday && real.weekdayAt(actualRise)==WeekDay.Wednesday,"Real selected-place sunrise boundary");
        java.util.Date event=new java.util.Date(instant("2026-10-07T05:00:00",tz));
        MyMuhurta core=new MyMuhurta(event,event,place,null);
        app.astrosoft.beans.BalamWindowDetails details=core.getBalamWindowDetails(event,new java.util.Date(actualRise+1000));
        for(app.astrosoft.beans.BalamWindowDetails.Entry entry:details.getEntries()) {
            java.util.List<PanchangaWindowEngine.Segment> expected=PanchangaWindowEngine.generate(entry.getStart(),entry.getEnd(),tz,place);
            check(entry.getWeekday()==Calendar.WEDNESDAY,"Integration preserves civil Wednesday");
            check(entry.getAmirthathiYoga()==AmirthathiYoga.ofWeekdayAndNakshatra(real.weekdayAt(entry.getStart().getTime()),expected.get(0).nak,false).getFirst(),"Integration uses Vara and active star");
        }
        System.out.println("SunriseVaraTest: sunrise, fallback, civil-day separation, transitions, wrap and DST passed");
    }
}
