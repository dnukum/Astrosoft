package app.astrosoft.core.test;
import java.util.*;
import app.astrosoft.core.*;
import app.astrosoft.consts.*;
import app.astrosoft.ui.table.*;
public class MyMuhurtaTableLayoutTest {
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 public static void main(String[] args){
  MyMuhurta m=new MyMuhurta(new Date(1781667652000L),new Date(1781667652000L),null,null);
  TableData<MapTableRow> data=m.getTableData();int location=-1,rise=-1,set=-1,amirth=-1,yoga=-1;
  for(int i=0;i<data.getRowCount();i++){
   String k=data.getRow(i).getColumnData(AstrosoftTableColumn.Key).toString();
   if(k.equals("Location"))location=i;
   if(k.equals(DisplayStrings.SUNRISE_STR.toString()))rise=i;
   if(k.equals(DisplayStrings.SUNSET_STR.toString()))set=i;
   if(k.startsWith(DisplayStrings.YOGA_STR.toString()))yoga=i;
   if(k.equals("Amirthathi Yoga")){amirth=i;String v=data.getRow(i).getColumnData(AstrosoftTableColumn.Value).toString();check(v.length()>0,"Yoga details populated");}
  }
  check(rise==location+1 && set==rise+1,"Sunrise/sunset directly after location");
  check(amirth==yoga+1,"Amirthathi immediately after Nitya Yoga");
  System.out.println("MyMuhurtaTableLayoutTest: actual table ordering and Yoga details passed");
 }
}
