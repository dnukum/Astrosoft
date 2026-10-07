package app.astrosoft.core.test;
import java.util.*;
import app.astrosoft.core.*;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.*;
import app.astrosoft.ui.table.*;
public class PanchangTransitionDisplayTest {
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[] args) {
  Calendar c=Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));c.clear();c.set(2026,9,7,6,0,0);
  check(PanchangTransitionHelp.until("Magha",9+10.0/60,"Purva Phalguni",c).equals("Magha until 09:10 AM → Purva Phalguni"),"Until format");
  check(PanchangTransitionHelp.until("A",25.5,"B",c).contains("08 Oct, 01:30 AM"),"Next day date");
  check(PanchangTransitionHelp.until("A",48,"B",c).contains("09 Oct, 12:00 AM"),"Multiple days");
  c.set(2026,11,31);check(PanchangTransitionHelp.until("A",24,"B",c).contains("01 Jan 2027"),"Year rollover");
  check(PanchangTransitionHelp.forRow("Day").contains("Civil"),"Civil day help");
  check(PanchangTransitionHelp.forRow("Amirthathi Yoga").contains("previous weekday"),"Vara help");
  check(PanchangTransitionHelp.forRow(DisplayStrings.AUS_TIME_STR.toString()).contains("not the Nitya Yoga"),"Subha Hora distinction");
  check(PanchangTransitionHelp.forRow("Date")==null,"Unrelated row clears help");
  c.set(2026,9,7,6,0,0);Place place=new Place("Sammamish","Washington","US",47.6,-122.0333,"America/Los_Angeles");
  Panchang pan=new Panchang(c.getTime(),place);
  TableData<MapTableRow> oldRows=pan.getPanchangTableData(), rows=pan.getPanchangTableData(true);
  for(int i=0;i<rows.getRowCount();i++) {
   String key=rows.getRow(i).getColumnData(AstrosoftTableColumn.Key).toString();
   String value=rows.getRow(i).getColumnData(AstrosoftTableColumn.Value).toString();
   boolean transition=key.startsWith(DisplayStrings.NAK_STR.toString())||key.startsWith(DisplayStrings.THITHI_STR.toString())||key.startsWith(DisplayStrings.YOGA_STR.toString())||key.startsWith(DisplayStrings.KARANA_STR.toString());
   if(transition)check(value.contains(" until ")&&value.contains(" → "),"Named successor: "+key);
   else check(value.equals(oldRows.getRow(i).getColumnData(AstrosoftTableColumn.Value).toString()),"Other rows unchanged: "+key);
   if(key.startsWith(DisplayStrings.NAK_STR.toString()))check(value.contains("Purva Phalguni")||value.contains("Pooram"),"Next star");
   if(key.startsWith(DisplayStrings.YOGA_STR.toString()))check(value.endsWith("Sukla"),"Next Nitya Yoga");
   if(key.startsWith(DisplayStrings.KARANA_STR.toString()))check(value.endsWith(Karana.Vanija.toString()),"Third Karana");
  }
  double[] edges={0,6,342,348,354,359};
  Karana[] expected={Karana.Kimstughna,Karana.Bava,Karana.Sakuna,Karana.Chatushpada,Karana.Naga,Karana.Naga};
  for(int i=0;i<edges.length;i++)check(PanchangaWindowEngine.karanaAt(edges[i])==expected[i],"Karana special slots");
  System.out.println("PanchangTransitionDisplayTest: named successors, dates, tooltips and unchanged timing rows passed");
 }
}
