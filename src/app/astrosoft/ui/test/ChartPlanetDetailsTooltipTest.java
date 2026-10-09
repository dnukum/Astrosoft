package app.astrosoft.ui.test;
import java.util.*;import java.awt.*;import java.awt.event.MouseEvent;import javax.swing.*;
import app.astrosoft.beans.*;import app.astrosoft.consts.*;import app.astrosoft.ui.comp.Chart;
public class ChartPlanetDetailsTooltipTest {
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static String hoverText(Component c){
  if(c instanceof JTable){JTable t=(JTable)c;for(int r=0;r<t.getRowCount();r++)for(int col=0;col<t.getColumnCount();col++)if(t.getValueAt(r,col)==Planet.Sun){Rectangle rect=t.getCellRect(r,col,true);return t.getToolTipText(new MouseEvent(t,MouseEvent.MOUSE_MOVED,0,0,rect.x+1,rect.y+1,0,false));}}
  if(c instanceof Container)for(Component child:((Container)c).getComponents()){String s=hoverText(child);if(s!=null)return s;}return null; }
 public static void main(String[] args)throws Exception {
  SwingUtilities.invokeAndWait(()->{
   EnumMap<Planet,Integer> positions=new EnumMap<>(Planet.class);EnumMap<Planet,Boolean> directions=new EnumMap<>(Planet.class);
   for(Planet p:Planet.planetsAsc()){positions.put(p,1);directions.put(p,true);}EnumMap<Planet,Nakshathra> stars=new EnumMap<>(Planet.class);stars.put(Planet.Sun,Nakshathra.Magam);
   EnumMap<Planet,Double> longs=new EnumMap<>(Planet.class);longs.put(Planet.Sun,123.75);
   String text=hoverText(new Chart(new PlanetChartData(Varga.Rasi,positions,directions,stars,longs),new Dimension(360,320)));
   check(text!=null,"Sun hover exists");
   check(text.contains(Nakshathra.Magam.toString()),"Nakshathra present: "+text);
   check(text.contains("123:45"),"Absolute longitude present: "+text);
   check(text.toLowerCase(Locale.ENGLISH).contains("orb"),"Orb present: "+text);
   check(text.toLowerCase(Locale.ENGLISH).contains("orb"),"Orb present: "+text);
  });
  System.out.println("ChartPlanetDetailsTooltipTest passed");
 }
}
