package app.astrosoft.ui.test;
import java.util.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import app.astrosoft.beans.*;
import app.astrosoft.consts.*;
import app.astrosoft.ui.comp.Chart;
public class ChartLongitudeTooltipTest {
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static boolean hover(Component c,String wanted) {
  if(c instanceof JTable){JTable t=(JTable)c;for(int r=0;r<t.getRowCount();r++)for(int col=0;col<t.getColumnCount();col++)if(t.getValueAt(r,col)==Planet.Sun){Rectangle rect=t.getCellRect(r,col,true);String text=t.getToolTipText(new MouseEvent(t,MouseEvent.MOUSE_MOVED,0,0,rect.x+1,rect.y+1,0,false));check(wanted.equals(text),"Actual tooltip: "+text);return true;}}
  if(c instanceof Container)for(Component child:((Container)c).getComponents())if(hover(child,wanted))return true;
  return false;
 }
 public static void main(String[] args)throws Exception {
  SwingUtilities.invokeAndWait(()->{
   check("123:45".equals(Chart.formatAbsoluteLongitude(123.75)),"Absolute not sign-relative");
   check("03:45".equals(Chart.formatSignRelativeOrb(123.75)),"Sign-relative orb offset");
   check("11:19".equals(Chart.formatSignRelativeOrb(191.32)),"Sign-relative orb offset 191.32");
   check("000:00".equals(Chart.formatAbsoluteLongitude(360.0)),"Wrap");
   check("359:59".equals(Chart.formatAbsoluteLongitude(-0.001)),"Negative wrap and floor minutes");
   check("012:59".equals(Chart.formatAbsoluteLongitude(12.999)),"Truncate seconds, no carry");
   check(Chart.formatAbsoluteLongitude(Double.NaN)==null,"Nonfinite");
   EnumMap<Planet,Integer> positions=new EnumMap<>(Planet.class);EnumMap<Planet,Boolean> directions=new EnumMap<>(Planet.class);
   for(Planet p:Planet.planetsAsc()){positions.put(p,1);directions.put(p,true);}
   EnumMap<Planet,Nakshathra> stars=new EnumMap<>(Planet.class);stars.put(Planet.Sun,Nakshathra.Magam);
   EnumMap<Planet,Double> longs=new EnumMap<>(Planet.class);longs.put(Planet.Sun,123.75);
   PlanetChartData data=new PlanetChartData(Varga.Rasi,positions,directions,stars,longs);
   longs.put(Planet.Sun,0.0);check(data.getPlanetLongitude(Planet.Sun)==123.75,"Snapshot");
   check(hover(new Chart(data,new Dimension(360,320)),"03:45 (123:45), "+Nakshathra.Magam),"Sun hover found");
   check(hover(new Chart(new PlanetChartData(Varga.Rasi,positions,directions,stars),new Dimension(360,320)),Nakshathra.Magam.toString()),"Legacy star-only");
  });
  System.out.println("ChartLongitudeTooltipTest: real hover, absolute minutes, wrap, truncation, missing data and snapshot passed");
 }
}
