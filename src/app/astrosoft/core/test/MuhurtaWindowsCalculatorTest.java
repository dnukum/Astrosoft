package app.astrosoft.core.test;
import java.util.*;
import app.astrosoft.core.MuhurtaWindowsCalculator;
public class MuhurtaWindowsCalculatorTest {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  long h=3600000L;
  MuhurtaWindowsCalculator.Window a=MuhurtaWindowsCalculator.abhijit(6*h,18*h,false,false);
  check(a.start==11*h+36*60000L&&a.end==12*h+24*60000L,"Equinox Abhijit");
  MuhurtaWindowsCalculator.Window b=MuhurtaWindowsCalculator.brahma(18*h,30*h,false);
  check(b.start==28*h+24*60000L&&b.end==29*h+12*60000L,"Equinox Brahma");
  check(MuhurtaWindowsCalculator.abhijit(6*h,21*h,false,false).start==13*h,"Season scaling");
  check(a.clip(a.end,a.end+h)==null,"Half open");
  check(a.clip(a.start+60000,a.end).partial,"Partial");
  check(!a.clip(a.start,a.end).partial,"Full");
  check(MuhurtaWindowsCalculator.abhijit(6*h,18*h,true,true).caution,"Wednesday");
  boolean bad=false;try{MuhurtaWindowsCalculator.brahma(2,1,false);}catch(IllegalArgumentException e){bad=true;}check(bad,"Ordering");
  TimeZone tz=TimeZone.getTimeZone("America/Los_Angeles");Calendar c=Calendar.getInstance(tz);c.clear();c.set(2026,9,7);
  java.util.List<MuhurtaWindowsCalculator.Window> day=MuhurtaWindowsCalculator.forDay(c,null);
  check(day.size()==7,"Wednesday two muhurta and five hora blocks");
  for(MuhurtaWindowsCalculator.Window w:day)check(w.fallback,"Fallback identified");
  java.util.List<MuhurtaWindowsCalculator.Window> overlaps=MuhurtaWindowsCalculator.forWindow(c.getTimeInMillis(),c.getTimeInMillis()+86400000,tz,null);
  check(!overlaps.isEmpty(),"Window results");for(MuhurtaWindowsCalculator.Window w:overlaps)check(w.start>=c.getTimeInMillis()&&w.end<=c.getTimeInMillis()+86400000,"Clipped");
  Calendar sunset=Calendar.getInstance(tz),sunrise=Calendar.getInstance(tz);sunset.clear();sunrise.clear();sunset.set(2026,9,31,18,0);sunrise.set(2026,10,1,6,0);
  MuhurtaWindowsCalculator.Window dst=MuhurtaWindowsCalculator.brahma(sunset.getTimeInMillis(),sunrise.getTimeInMillis(),false);
  check(dst.end-dst.start==52*60000L,"Fall DST: 13-hour night / 15 = 52 minutes");
  Calendar rollover=Calendar.getInstance(tz);rollover.clear();rollover.set(2026,11,31);
  check(MuhurtaWindowsCalculator.forWindow(rollover.getTimeInMillis(),rollover.getTimeInMillis()+2*86400000L,tz,null).size()>7,"Year/multi-day window");
  double[][] offsets=app.astrosoft.consts.WeekDay.Wednesday.auspiciousTimeBlocks();offsets[0][0]=99;
  check(app.astrosoft.consts.WeekDay.Wednesday.auspiciousTimeBlocks()[0][0]==3,"Defensive offset copy");
  app.astrosoft.core.MyMuhurta model=new app.astrosoft.core.MyMuhurta(c.getTime(),c.getTime(),null,null);
  app.astrosoft.ui.table.TableData<app.astrosoft.ui.table.MapTableRow> table=model.getTableData();int named=0;
  for(int i=0;i<table.getRowCount();i++){String key=table.getRow(i).getColumnData(app.astrosoft.consts.AstrosoftTableColumn.Key).toString();if(key.equals("Abhijit Muhurta")||key.equals("Brahma Muhurta"))named++;}check(named==2,"Main table two rows");
  app.astrosoft.beans.BalamWindowDetails details=model.getBalamWindowDetails(c.getTime(),new Date(c.getTimeInMillis()+86400000));check(!details.getTimingWindows().isEmpty(),"Background domain supplies windows");
  try{
   java.lang.reflect.Method html=app.astrosoft.ui.comp.BalamCalendarPanel.class.getDeclaredMethod("buildDetailsHtml",app.astrosoft.beans.BalamWindowDetails.class,app.astrosoft.consts.Nakshathra.class,app.astrosoft.consts.Rasi.class);html.setAccessible(true);
   String rendered=(String)html.invoke(null,details,app.astrosoft.consts.Nakshathra.Magam,app.astrosoft.consts.Rasi.Simha);
   check(rendered.contains("width='43%'")&&!rendered.contains("Available timing windows")&&rendered.contains("Brahma Muhurta"),"Right-side Balam HTML");
   check(!rendered.contains("Overlaps with the selected window")&&!rendered.contains("Existing weekday auspicious blocks")&&!rendered.contains("Eighth of 15 daytime divisions")&&!rendered.contains("Fourteenth of 15 nighttime divisions"),"No explanatory clutter in calendar panel");
   check(rendered.contains("Subha Hora")&&rendered.contains("Abhijit Muhurta")&&rendered.contains("Wednesday: additional scrutiny"),"Timing labels and actual Wednesday caution preserved");
   javax.swing.JEditorPane pane=new javax.swing.JEditorPane("text/html",rendered);check(pane.getDocument().getLength()>0,"Swing parses HTML");
   app.astrosoft.beans.BalamWindowDetails empty=new app.astrosoft.beans.BalamWindowDetails(c.getTime(),new Date(c.getTimeInMillis()+1),tz,Collections.emptyList());
   String cleared=(String)html.invoke(null,empty,app.astrosoft.consts.Nakshathra.Magam,app.astrosoft.consts.Rasi.Simha);check(cleared.contains("No overlap")&&!cleared.contains("fallback solar time"),"Empty selection clears timing results");
  }catch(Exception e){throw new AssertionError(e);}
  System.out.println("MuhurtaWindowsCalculatorTest passed: formulas, overlaps, DST, rollover, fallbacks, main table and Swing detail HTML");
 }
}
