package app.astrosoft.core.test;
import java.util.*;
import java.lang.reflect.*;
import app.astrosoft.beans.*;
import app.astrosoft.consts.*;
import app.astrosoft.core.MyMuhurta;

public class BalamWindowDetailsTest {
    static void check(boolean v, String m) { if (!v) throw new AssertionError(m); }
    static int count(String s, String part) { return (s.length()-s.replace(part, "").length())/part.length(); }
    public static void main(String[] args) throws Exception {
        Date start=new Date(0), end=new Date(3600000);
        TimeZone tz=TimeZone.getTimeZone("UTC");
        List<BalamWindowDetails.Entry> entries=new ArrayList<>();
        entries.add(new BalamWindowDetails.Entry(start,end,Paksha.Shukla,Thithi.Prathamai,Calendar.THURSDAY,true));
        entries.add(new BalamWindowDetails.Entry(start,end,Paksha.Shukla,Thithi.Prathamai,Calendar.FRIDAY,true));
        entries.add(new BalamWindowDetails.Entry(start,end,Paksha.Shukla,Thithi.Dvithiyai,Calendar.FRIDAY,false));
        BalamWindowDetails d=new BalamWindowDetails(start,end,tz,entries);
        start.setTime(123); end.setTime(123); tz.setRawOffset(123); entries.clear();
        check(d.getStart().getTime()==0 && d.getEnd().getTime()==3600000 && d.getEntries().size()==3,"Defensive constructor copies");
        d.getStart().setTime(42); d.getTimeZone().setRawOffset(42); d.getEntries().get(0).getStart().setTime(42);
        check(d.getStart().getTime()==0 && d.getTimeZone().getRawOffset()==0 && d.getEntries().get(0).getStart().getTime()==0,"Defensive getter copies");
        boolean immutable=false; try {d.getEntries().clear();} catch(UnsupportedOperationException e){immutable=true;}
        check(immutable,"Unmodifiable entries");
        Method render=Class.forName("app.astrosoft.ui.comp.BalamCalendarPanel").getDeclaredMethod("buildDetailsHtml",BalamWindowDetails.class,Nakshathra.class,Rasi.class);
        render.setAccessible(true);
        List<BalamWindowDetails.Entry> yogaEntries = Arrays.asList(
            new BalamWindowDetails.Entry(new Date(0),new Date(1000),Paksha.Shukla,Thithi.Prathamai,Calendar.WEDNESDAY,false,AmirthathiYoga.Marana,Yoga.Vishakambha,Karana.Kimstughna),
            new BalamWindowDetails.Entry(new Date(1000),new Date(2000),Paksha.Shukla,Thithi.Prathamai,Calendar.WEDNESDAY,false,AmirthathiYoga.Marana,Yoga.Vishakambha,Karana.Kimstughna),
            new BalamWindowDetails.Entry(new Date(2000),new Date(3000),Paksha.Shukla,Thithi.Prathamai,Calendar.THURSDAY,false,AmirthathiYoga.Amirtha,Yoga.Priti,Karana.Bava));
        BalamWindowDetails yogaDetails = new BalamWindowDetails(new Date(0),new Date(3000),TimeZone.getTimeZone("UTC"),yogaEntries);
        String yogaHtml = (String)render.invoke(null,yogaDetails,Nakshathra.Ashwini,Rasi.values()[0]);
        check(yogaHtml.contains("<b>Amirthathi Yoga:</b> <font color='red'>Marana</font> &rarr; (01 Jan, 12:00 AM UTC) Amirtha") || yogaHtml.contains("<b>Amirthathi Yoga:</b> <font color='red'>Marana</font> &rarr; (01 Jan, 00:00 UTC) Amirtha"), "Core Yoga rendered with Marana in red and adjacent repeats suppressed");
        check(yogaHtml.contains("<b>Yoga (Nitya Yoga):</b> Vishakambha &rarr; (01 Jan, 12:00 AM UTC) Priti") || yogaHtml.contains("<b>Yoga (Nitya Yoga):</b> Vishakambha &rarr; (01 Jan, 00:00 UTC) Priti"), "Timed Nitya Yoga");
        check(yogaHtml.contains("<b>Karana:</b> "), "Karana rendered");
        check(yogaEntries.get(0).getAmirthathiYoga() == AmirthathiYoga.Marana, "Bean carries Core classification");

        // Verify Vara transitions: Friday -> Saturday (Saturday in red) and Tuesday -> Wednesday (Tuesday in red)
        List<BalamWindowDetails.Entry> varaEntries = Arrays.asList(
            new BalamWindowDetails.Entry(new Date(0), new Date(1000), Paksha.Shukla, Thithi.Dvithiyai, Calendar.FRIDAY, false),
            new BalamWindowDetails.Entry(new Date(1000), new Date(2000), Paksha.Shukla, Thithi.Dvithiyai, Calendar.SATURDAY, false)
        );
        BalamWindowDetails varaDetails = new BalamWindowDetails(new Date(0), new Date(2000), TimeZone.getTimeZone("UTC"), varaEntries);
        String varaHtml = (String)render.invoke(null, varaDetails, Nakshathra.Ashwini, Rasi.values()[0]);
        check(varaHtml.contains("<b>Varam:</b> Friday &rarr; <font color='red'>Saturday</font>"), "Friday -> Saturday transition with Saturday in red");

        List<BalamWindowDetails.Entry> tueEntries = Arrays.asList(
            new BalamWindowDetails.Entry(new Date(0), new Date(1000), Paksha.Shukla, Thithi.Dvithiyai, Calendar.TUESDAY, false),
            new BalamWindowDetails.Entry(new Date(1000), new Date(2000), Paksha.Shukla, Thithi.Dvithiyai, Calendar.WEDNESDAY, false)
        );
        BalamWindowDetails tueDetails = new BalamWindowDetails(new Date(0), new Date(2000), TimeZone.getTimeZone("UTC"), tueEntries);
        String tueHtml = (String)render.invoke(null, tueDetails, Nakshathra.Ashwini, Rasi.values()[0]);
        check(tueHtml.contains("<b>Varam:</b> <font color='red'>Tuesday</font> &rarr; Wednesday"), "Tuesday -> Wednesday transition with Tuesday in red");

        // Verify Thithi transitions: Sapthami -> Ashtami (Ashtami in red)
        List<BalamWindowDetails.Entry> thithiEntries = Arrays.asList(
            new BalamWindowDetails.Entry(new Date(0), new Date(1000), Paksha.Shukla, Thithi.Sapthami, Calendar.WEDNESDAY, false),
            new BalamWindowDetails.Entry(new Date(1000), new Date(2000), Paksha.Shukla, Thithi.Ashtami, Calendar.WEDNESDAY, true),
            new BalamWindowDetails.Entry(new Date(2000), new Date(3000), Paksha.Shukla, Thithi.Navami, Calendar.WEDNESDAY, true)
        );
        BalamWindowDetails thithiDetails = new BalamWindowDetails(new Date(0), new Date(3000), TimeZone.getTimeZone("UTC"), thithiEntries);
        String thithiHtml = (String)render.invoke(null, thithiDetails, Nakshathra.Ashwini, Rasi.values()[0]);
        check(thithiHtml.contains("Shukla Sapthami &rarr; (01 Jan, 12:00 AM UTC) <font color='red'>Shukla Ashtami</font> &rarr; (01 Jan, 12:00 AM UTC) <font color='red'>Shukla Navami</font>") || thithiHtml.contains("Shukla Sapthami &rarr; (01 Jan, 00:00 UTC) <font color='red'>Shukla Ashtami</font> &rarr; (01 Jan, 00:00 UTC) <font color='red'>Shukla Navami</font>"), "Thithi transitions correctly color coded in red");

        String html=(String)render.invoke(null,d,Nakshathra.values()[0],Rasi.values()[0]);
        check(count(html,"<table")==1 && html.contains("<b>Subha Hora:</b>") && count(html,"<b>Start:</b>")==1 && count(html,"<b>End:</b>")==1,"Two-column layout without repeated header");
        check(count(html,"Prathamai")==1 && count(html,"Dvithiyai")==1,"Tithi presentation deduplication");
        String[] days=new java.text.DateFormatSymbols().getWeekdays();
        check(count(html,days[Calendar.THURSDAY])==1 && count(html,days[Calendar.FRIDAY])==1,"Core weekday presentation");
        check(count(html,"<font color='red'>")==1 && html.indexOf("<b>Thithi:</b>")<html.indexOf("<b>Varam:</b>"),"Warning and order");
        Date now=new Date(1791158400000L);
        MyMuhurta core=new MyMuhurta(now,now,null,null);
        BalamWindowDetails classified = core.getBalamWindowDetails(new Date(0),new Date(3600000));
        for (BalamWindowDetails.Entry entry : classified.getEntries()) {
            app.astrosoft.core.Panchang panchang = new app.astrosoft.core.Panchang(entry.getStart());
            check(entry.getAmirthathiYoga() == AmirthathiYoga.ofWeekdayAndNakshatra(
                app.astrosoft.core.SunriseVara.forPlace(classified.getTimeZone(),
                    app.astrosoft.ui.AstroSoft.getPreferences().getPlace()).weekdayAt(entry.getStart().getTime()),
                panchang.getNakshathra(),false).getFirst(), "Core uses actual transit star and sunrise Vara");
        }

        BalamWindowDetails computed=core.getBalamWindowDetails(now,new Date(now.getTime()+86400000L));
        check(computed.getStart().equals(now) && !computed.getEntries().isEmpty(),"Core orchestration");
        check(computed.getTimeZone().equals(TimeZone.getDefault()),"Core timezone fallback");
        check(core.getBalamWindowDetails(now,now).getEntries().isEmpty(),"Empty window");
        boolean reversed=false;try{core.getBalamWindowDetails(new Date(now.getTime()+1),now);}catch(IllegalArgumentException e){reversed=true;}
        check(reversed,"Reversed window");
        System.out.println("BalamWindowDetailsTest: bean immutability, real HTML renderer and Core orchestration passed");
    }
}
