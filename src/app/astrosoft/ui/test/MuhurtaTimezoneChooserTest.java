package app.astrosoft.ui.test;
import java.util.*;
import javax.swing.*;
import app.astrosoft.ui.comp.PlaceChooser;
import app.astrosoft.util.*;
public class MuhurtaTimezoneChooserTest {
 public static void main(String[] args)throws Exception {
  Thread.sleep(200);
  SwingUtilities.invokeAndWait(()-> {try {
   PlaceChooser p=new PlaceChooser(new java.awt.Dimension(400,140),false);
   java.lang.reflect.Field f=PlaceChooser.class.getDeclaredField("timeZoneCombo");f.setAccessible(true);
   JComboBox combo=(JComboBox)f.get(p);
   String[][] cases={{"America/Los_Angeles","2026-01-15T12:00:00Z","-08:00"},{"America/Los_Angeles","2026-07-15T12:00:00Z","-07:00"},{"Australia/Sydney","2026-01-15T12:00:00Z","+11:00"},{"Asia/Kathmandu","2026-07-15T12:00:00Z","+05:45"},{"America/Los_Angeles","2026-03-08T02:30:00Z","invalid local time"}};
   for(String[] c:cases){
    Date wallDate=MuhurtaDateTime.toChooserDate(Date.from(java.time.Instant.parse(c[1])),TimeZone.getTimeZone("UTC"));
    Date wallTime=wallDate;
    if ("invalid local time".equals(c[2])) {
     Calendar dayCal=Calendar.getInstance(); dayCal.set(2026, Calendar.MARCH, 8);
     Calendar timeCal=Calendar.getInstance(); timeCal.set(2026, Calendar.JANUARY, 1, 2, 30, 0);
     wallDate=dayCal.getTime();
     wallTime=timeCal.getTime();
    }
    p.setLocalDateForTimezone(wallDate,wallTime);
    JLabel label=(JLabel)combo.getRenderer().getListCellRendererComponent(new JList(),new AstrosoftTimeZone(c[0]),-1,false,false);
    if(!label.getText().contains(c[2]))throw new AssertionError(label.getText());
   }
  }catch(Exception e){throw new RuntimeException(e);}});
  System.out.println("MuhurtaTimezoneChooserTest: real renderer refresh and DST gap passed");
 }
}
