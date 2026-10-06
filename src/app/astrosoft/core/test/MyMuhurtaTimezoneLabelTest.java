package app.astrosoft.core.test;
import java.util.*;
import app.astrosoft.util.*;
import app.astrosoft.beans.Place;
import app.astrosoft.core.MyMuhurta;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.ui.table.*;
public class MyMuhurtaTimezoneLabelTest {
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static void test(String id,String instant,String offset) {
  Date d=Date.from(java.time.Instant.parse(instant));
  Place p=new Place("Test","Test","Test",0.0,0.0,id);
  TableData<MapTableRow> data=new MyMuhurta(d,d,p,null).getTableData();
  for(int i=0;i<data.getRowCount();i++)if("Time".equals(String.valueOf(data.getRow(i).getColumnData(AstrosoftTableColumn.Key)))) {
   String v=String.valueOf(data.getRow(i).getColumnData(AstrosoftTableColumn.Value));
   check(v.contains(id+" (GMT "+offset+")"),id+" "+instant+" actual="+v);
  }
 }
 public static void main(String[] args) {
  test("America/Los_Angeles","2026-01-15T12:00:00Z","-08:00");
  test("America/Los_Angeles","2026-07-15T12:00:00Z","-07:00");
  test("Europe/London","2026-01-15T12:00:00Z","+00:00");
  test("Europe/London","2026-07-15T12:00:00Z","+01:00");
  test("Australia/Sydney","2026-01-15T12:00:00Z","+11:00");
  test("Australia/Sydney","2026-07-15T12:00:00Z","+10:00");
  test("Asia/Kolkata","2026-07-15T12:00:00Z","+05:30");
  test("Asia/Kathmandu","2026-07-15T12:00:00Z","+05:45");
  test("America/Los_Angeles","2026-03-08T09:59:59Z","-08:00");
  test("America/Los_Angeles","2026-03-08T10:00:00Z","-07:00");
  test("America/Los_Angeles","2026-11-01T08:30:00Z","-08:00"); // Calendar resolves ambiguous local time to standard occurrence.
  test("America/Los_Angeles","2026-11-01T09:30:00Z","-08:00");
  System.out.println("MyMuhurtaTimezoneLabelTest passed");
 }
}
