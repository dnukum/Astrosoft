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
        String html=(String)render.invoke(null,d,Nakshathra.values()[0],Rasi.values()[0]);
        check(!html.contains("<table") && count(html,"<b>Start:</b>")==1 && count(html,"<b>End:</b>")==1,"No table or repeated header");
        check(count(html,"Prathamai")==1 && count(html,"Dvithiyai")==1,"Tithi presentation deduplication");
        String[] days=new java.text.DateFormatSymbols().getWeekdays();
        check(count(html,days[Calendar.THURSDAY])==1 && count(html,days[Calendar.FRIDAY])==1,"Core weekday presentation");
        check(count(html,"<font color='red'>")==1 && html.indexOf("<b>Thithi:</b>")<html.indexOf("<b>Varam:</b>"),"Warning and order");
        Date now=new Date(1791158400000L);
        MyMuhurta core=new MyMuhurta(now,now,null,null);
        BalamWindowDetails computed=core.getBalamWindowDetails(now,new Date(now.getTime()+86400000L));
        check(computed.getStart().equals(now) && !computed.getEntries().isEmpty(),"Core orchestration");
        check(computed.getTimeZone().equals(TimeZone.getDefault()),"Core timezone fallback");
        check(core.getBalamWindowDetails(now,now).getEntries().isEmpty(),"Empty window");
        boolean reversed=false;try{core.getBalamWindowDetails(new Date(now.getTime()+1),now);}catch(IllegalArgumentException e){reversed=true;}
        check(reversed,"Reversed window");
        System.out.println("BalamWindowDetailsTest: bean immutability, real HTML renderer and Core orchestration passed");
    }
}
