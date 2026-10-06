package app.astrosoft.core;
import java.util.*;
import app.astrosoft.consts.*;
import app.astrosoft.util.SwissHelper;
import swisseph.SweDate;

/** Half-open intervals split at Moon-star, Tithi, Nitya Yoga, Karana and civil midnight boundaries. */
public final class PanchangaWindowEngine {
 private PanchangaWindowEngine() {}
 /** Sidereal Sun and Moon longitudes, respectively. Source must advance less than one sector/hour. */
 public interface LongitudeSource { double[] at(long instant); }
 private static double norm(double d) {
  if(!Double.isFinite(d))throw new IllegalArgumentException("Finite longitude required");
  return (d%360+360)%360;
 }
 public static Karana karanaAt(double elongation) {
  int slot=(int)(norm(elongation)/6);
  return slot==0?Karana.Kimstughna:slot==57?Karana.Sakuna:slot==58?Karana.Chatushpada:slot==59?Karana.Naga:Karana.values()[(slot-1)%7];
 }
 private static int[] state(LongitudeSource source,long time) {
  double[] p=source.at(time);
  if(p==null || p.length<2)throw new IllegalArgumentException("Sun and Moon required");
  double sun=norm(p[0]),moon=norm(p[1]),phase=norm(moon-sun);
  return new int[]{(int)(phase/12),(int)(phase/6),(int)(norm(sun+moon)/(360.0/27)),(int)(moon/(360.0/27))};
 }
 public static final class Segment {
  public final Date start,end;
  public final Paksha paksha;
  public final Thithi thithi;
  public final Yoga yoga;
  public final Karana karana;
  public final Nakshathra nak;
  public final int weekday;
  Segment(long a,long b,int[] s,TimeZone tz) {
   start=new Date(a);end=new Date(b);
   paksha=s[0]<15?Paksha.Shukla:Paksha.Krishna;
   thithi=s[0]==29?Thithi.Amavasya:Thithi.ofIndex(s[0]%15);
   yoga=Yoga.ofIndex(s[2]);nak=Nakshathra.ofIndex(s[3]);karana=karanaAt(s[1]*6);
   Calendar c=Calendar.getInstance(tz);c.setTimeInMillis(a);weekday=c.get(Calendar.DAY_OF_WEEK);
  }
  public boolean isWarning(){return thithi==Thithi.Amavasya || thithi==Thithi.Ashtami || thithi==Thithi.Navami || thithi==Thithi.Prathamai;}
 }
 public static List<Segment> generate(Date start,Date end,TimeZone tz) {
  final SwissHelper swiss=new SwissHelper();
  return generate(start,end,tz,t->{
   swiss.setSweDate(new SweDate(t/86400000.0+2440587.5));
   EnumMap<Planet,Double> p=swiss.getPlanetaryPosition(EnumSet.of(Planet.Sun,Planet.Moon));
   return new double[]{p.get(Planet.Sun),p.get(Planet.Moon)};
  });
 }
 public static List<Segment> generate(Date start,Date end,TimeZone tz,LongitudeSource source) {
  if(start==null || end==null || tz==null || source==null || end.before(start))throw new IllegalArgumentException("Valid window, timezone and source required");
  long cursor=start.getTime(),limit=end.getTime();List<Segment> result=new ArrayList<>();
  while(cursor<limit) {
   int[] current=state(source,cursor);long boundary=limit;
   Calendar midnight=Calendar.getInstance(tz);midnight.setTimeInMillis(cursor);midnight.add(Calendar.DAY_OF_MONTH,1);
   midnight.set(Calendar.HOUR_OF_DAY,0);midnight.set(Calendar.MINUTE,0);midnight.set(Calendar.SECOND,0);midnight.set(Calendar.MILLISECOND,0);
   boundary=Math.min(boundary,midnight.getTimeInMillis());
   long probe=cursor;
   while(probe<boundary) {
    long next=Math.min(boundary,probe+3600000L);
    if(!Arrays.equals(current,state(source,next))) {
     long low=probe,high=next;
     while(high-low>1){long mid=low+(high-low)/2;if(Arrays.equals(current,state(source,mid)))low=mid;else high=mid;}
     boundary=high;break;
    }
    probe=next;
   }
   if(boundary<=cursor)throw new IllegalStateException("Non-progressing window");
   result.add(new Segment(cursor,boundary,current,tz));cursor=boundary;
  }
  return result;
 }
}
