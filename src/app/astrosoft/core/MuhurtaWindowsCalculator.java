package app.astrosoft.core;
import java.util.*;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.WeekDay;
import com.web_tomorrow.utils.suntimes.SunTimes;
import com.web_tomorrow.utils.suntimes.Time;
/** Instant-based seasonal windows. Brahma is anchored before the requested civil date's sunrise. */
public final class MuhurtaWindowsCalculator {
 private static final Logger LOG=Logger.getLogger(MuhurtaWindowsCalculator.class.getName());
 private MuhurtaWindowsCalculator(){}
 public static final class Window {
  public final String kind;public final long start,end;public final boolean caution,fallback,partial;
  private Window(String k,long s,long e,boolean c,boolean f,boolean p){kind=k;start=s;end=e;caution=c;fallback=f;partial=p;}
  public Window clip(long s,long e){long a=Math.max(s,start),b=Math.min(e,end);return a<b?new Window(kind,a,b,caution,fallback,partial||a!=start||b!=end):null;}
 }
 private static void ordered(long a,long b){if(b<=a)throw new IllegalArgumentException("Solar events must be increasing");}
 public static Window abhijit(long rise,long set,boolean wed,boolean fallback){ordered(rise,set);long d=set-rise;return new Window("Abhijit Muhurta",rise+Math.round(d*7.0/15),rise+Math.round(d*8.0/15),wed,fallback,false);}
 public static Window brahma(long set,long rise,boolean fallback){ordered(set,rise);long r=rise-set;return new Window("Brahma Muhurta",rise-Math.round(r*2.0/15),rise-Math.round(r/15.0),false,fallback,false);}
 private static final class Event {final long time;final boolean fallback;Event(long t,boolean f){time=t;fallback=f;}}
 private static Calendar date(Calendar input){Calendar c=(Calendar)input.clone();c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);return c;}
 private static Event solar(Calendar day,Place place,boolean rise){
  try {
   if(place==null)throw new IllegalStateException("Location required");
   int y=day.get(Calendar.YEAR),m=day.get(Calendar.MONTH)+1,d=day.get(Calendar.DATE);
   Time utc=rise?SunTimes.getSunriseTimeUTC(y,m,d,place.longitude(),place.latitude(),SunTimes.ZENITH):SunTimes.getSunsetTimeUTC(y,m,d,place.longitude(),place.latitude(),SunTimes.ZENITH);
   // Resolve UTC clock to the instant that falls on the requested local date, including DST.
   Calendar u=Calendar.getInstance(TimeZone.getTimeZone("UTC"));u.clear();u.set(y,m-1,d,utc.getHour(),utc.getMinute(),utc.getSecond());
   for(int shift=-1;shift<=1;shift++){long instant=u.getTimeInMillis()+shift*86400000L;Calendar local=Calendar.getInstance(day.getTimeZone());local.setTimeInMillis(instant);if(local.get(Calendar.YEAR)==y&&local.get(Calendar.MONTH)==m-1&&local.get(Calendar.DATE)==d)return new Event(instant,false);}
   throw new IllegalStateException("Solar event outside local date");
  }catch(Exception error){Calendar c=(Calendar)day.clone();c.set(Calendar.HOUR_OF_DAY,rise?6:18);LOG.warning((rise?"Sunrise":"Sunset")+" fallback to local "+(rise?"06:00":"18:00")+" on "+day.getTime()+": "+error);return new Event(c.getTimeInMillis(),true);}
 }
 public static List<Window> forDay(Calendar input,Place place){
  Calendar day=date(input),previous=(Calendar)day.clone(),next=(Calendar)day.clone();previous.add(Calendar.DATE,-1);next.add(Calendar.DATE,1);
  Event rise=solar(day,place,true),set=solar(day,place,false),before=solar(previous,place,false),after=solar(next,place,true);
  if(set.time<=rise.time||rise.time<=before.time||after.time<=set.time){LOG.warning("Invalid solar ordering on "+day.getTime()+"; timing windows unavailable (check coordinates/timezone)");return Collections.emptyList();}
  List<Window> out=new ArrayList<>();WeekDay weekday=WeekDay.ofCalendar(day);
  out.add(abhijit(rise.time,set.time,weekday==WeekDay.Wednesday,rise.fallback||set.fallback));
  out.add(brahma(before.time,rise.time,before.fallback||rise.fallback));
  for(double[] block:weekday.auspiciousTimeBlocks()){
   boolean night=block[2]==1;long base=night?set.time:rise.time,len=night?after.time-set.time:set.time-rise.time;
   out.add(new Window("Subha Hora",base+Math.round(len*block[0]/12),base+Math.round(len*block[1]/12),false,night?set.fallback||after.fallback:rise.fallback||set.fallback,false));
  }
  return Collections.unmodifiableList(out);
 }
 public static List<Window> forWindow(long start,long end,TimeZone zone,Place place){
  if(start==end)return Collections.emptyList();
  ordered(start,end);Calendar day=Calendar.getInstance(zone);day.setTimeInMillis(start);day=date(day);day.add(Calendar.DATE,-1);
  Calendar last=Calendar.getInstance(zone);last.setTimeInMillis(end-1);last=date(last);last.add(Calendar.DATE,1);
  List<Window> out=new ArrayList<>();
  for(;!day.after(last);day.add(Calendar.DATE,1))for(Window w:forDay(day,place)){Window overlap=w.clip(start,end);if(overlap!=null)out.add(overlap);}
  Collections.sort(out,Comparator.comparingLong(w->w.start));return Collections.unmodifiableList(out);
 }
 public static String format(Window w,TimeZone zone){SimpleDateFormat f=new SimpleDateFormat("dd MMM yyyy, hh:mm a z",Locale.ENGLISH);f.setTimeZone(zone);return f.format(new Date(w.start))+" – "+f.format(new Date(w.end))+(w.partial?" (partial overlap)":"")+(w.caution?" [Wednesday: additional scrutiny]":"")+(w.fallback?" [fallback solar time]":"");}
 public static String help(String kind){return kind.equals("Abhijit Muhurta")?"Eighth of 15 daytime divisions; Wednesday requires additional scrutiny. Not an automatic override of other checks.":kind.equals("Brahma Muhurta")?"Fourteenth of 15 nighttime divisions, before this date's sunrise; intended for meditation and study.":"Existing weekday auspicious blocks scaled to actual day/night spans; not Nitya Yoga Subha. Calendar results show overlaps only.";}
}
