package app.astrosoft.core.test;
import java.util.*;
import app.astrosoft.core.MyMuhurta;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.ui.table.*;
public class MyMuhurtaEventTimeTest {
    static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
    static String tableTime(MyMuhurta m){
        TableData<MapTableRow> data=m.getTableData();
        for(int i=0;i<data.getRowCount();i++) {
            MapTableRow row=data.getRow(i);
            if("Time".equals(String.valueOf(row.getColumnData(AstrosoftTableColumn.Key))))
                return String.valueOf(row.getColumnData(AstrosoftTableColumn.Value));
        }
        throw new AssertionError("Time row missing");
    }
    public static void main(String[] args){
        TimeZone original=TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            Calendar c=Calendar.getInstance();c.clear();c.set(2026,9,5,9,15,0);
            Date date=c.getTime(); MyMuhurta first=new MyMuhurta(date,date,null,null);
            c.set(Calendar.HOUR_OF_DAY,16);c.set(Calendar.MINUTE,42);
            MyMuhurta edited=new MyMuhurta(date,c.getTime(),null,null);
            check(!tableTime(first).equals(tableTime(edited)),"Edit changes table event time");
            check(tableTime(edited).startsWith("04:42:00 PM"),"Edit exact time");
            c.set(Calendar.DAY_OF_MONTH,6);c.set(Calendar.HOUR_OF_DAY,7);c.set(Calendar.MINUTE,23);
            Date windowStart=c.getTime();MyMuhurta updated=new MyMuhurta(windowStart,windowStart,null,null);
            check(tableTime(updated).startsWith("07:23:00 AM"),"Calendar Update exact time");
            check(updated.getEventInstant().equals(windowStart),"Calendar event instant");
            updated.getEventInstant().setTime(0);
            check(updated.getEventInstant().equals(windowStart),"Event instant defensive copy");
            System.out.println("MyMuhurtaEventTimeTest: Edit, calendar Update, event table and defensive copy passed");
        } finally {TimeZone.setDefault(original);}
    }
}
