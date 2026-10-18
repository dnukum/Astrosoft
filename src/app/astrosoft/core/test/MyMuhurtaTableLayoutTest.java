package app.astrosoft.core.test;
import java.util.*;
import app.astrosoft.core.*;
import app.astrosoft.consts.*;
import app.astrosoft.ui.table.*;
public class MyMuhurtaTableLayoutTest {
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 public static void main(String[] args){
  MyMuhurta m=new MyMuhurta(new Date(1781667652000L),new Date(1781667652000L),null,null);
  TableData<MapTableRow> data=m.getTableData();int location=-1,rise=-1,set=-1,amirth=-1,yoga=-1,day=-1,vara=-1;
  String varaVal="";
  for(int i=0;i<data.getRowCount();i++){
   String k=data.getRow(i).getColumnData(AstrosoftTableColumn.Key).toString();
   if(k.equals("Location"))location=i;
   if(k.equals(DisplayStrings.SUNRISE_STR.toString()))rise=i;
   if(k.equals(DisplayStrings.SUNSET_STR.toString()))set=i;
   if(k.equals(DisplayStrings.DAY_STR.toString()))day=i;
   if(k.equals("Vara (Solar Day)")){vara=i;varaVal=data.getRow(i).getColumnData(AstrosoftTableColumn.Value).toString();}
   if(k.startsWith(DisplayStrings.YOGA_STR.toString()))yoga=i;
   if(k.equals("Amirthathi Yoga")){amirth=i;String v=data.getRow(i).getColumnData(AstrosoftTableColumn.Value).toString();check(v.length()>0,"Yoga details populated");}
  }
  check(rise==location+1 && set==rise+1,"Sunrise/sunset directly after location");
  check(vara==day+1,"Vara immediately after Day");
  check(varaVal.contains(" -> ("),"Vara format has transition with sunrise time: "+varaVal);
  check(amirth==yoga+1,"Amirthathi immediately after Nitya Yoga");

  // Test multi-line formatting
  int width = 245;
  String formattedTime = app.astrosoft.ui.view.MyMuhurtaView.formatMultiLineValue("Time", "05:05:11 PM,Asia/Kolkata (GMT +05:30)", width);
  check(formattedTime.contains("05:05:11 PM<br>Asia/Kolkata (GMT +05:30)"), "Time breaks on comma");

  String formattedNak = app.astrosoft.ui.view.MyMuhurtaView.formatMultiLineValue("Nakshathra ( End )", "Purva Phalguni until 03:21 PM → Uttra Phalguni", width);
  check(formattedNak.contains("Purva Phalguni until 03:21 PM →<br>Uttra Phalguni"), "Nakshatra breaks on arrow");

  String formattedKarana = app.astrosoft.ui.view.MyMuhurtaView.formatMultiLineValue("Karana ( End )", "Taitula until 06:56 AM → Garija until 06:58 PM → Vanija", width);
  check(formattedKarana.contains("Taitula until 06:56 AM →<br>Garija until 06:58 PM →<br>Vanija"), "Karana breaks on multiple arrows");

  String formattedAbhijit = app.astrosoft.ui.view.MyMuhurtaView.formatMultiLineValue("Abhijit Muhurta", "31 Mar 2026, 12:00 PM IST – 31 Mar 2026, 12:48 PM IST", width);
  check(formattedAbhijit.contains("31 Mar 2026, 12:00 PM IST –<br>31 Mar 2026, 12:48 PM IST"), "Abhijit breaks on en-dash");

  String formattedLoc = app.astrosoft.ui.view.MyMuhurtaView.formatMultiLineValue("Location", "Delhi, National Capital Territory of Delhi, India", width);
  check(formattedLoc.contains("Delhi,<br>National Capital Territory of Delhi,<br>India"), "Location wraps long components");

  String formattedShortLoc = app.astrosoft.ui.view.MyMuhurtaView.formatMultiLineValue("Location", "London, United Kingdom", width);
  check(!formattedShortLoc.contains("<br>"), "Short location does not break");

  String formattedDay = app.astrosoft.ui.view.MyMuhurtaView.formatMultiLineValue("Day", "Tuesday", width);
  check(formattedDay.equals("Tuesday"), "Single line values left untouched");

  System.out.println("MyMuhurtaTableLayoutTest: actual table ordering, Yoga details, and multi-line formatting passed");
 }
}
