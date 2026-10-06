package app.astrosoft.core.test;
import java.util.*;
import app.astrosoft.core.PanchangaWindowEngine;
import app.astrosoft.consts.*;
public class PanchangaWindowEngineTest {
 static void check(boolean b,String m) {if(!b)throw new AssertionError(m);}
 public static void main(String[] args) {
  TimeZone tz=TimeZone.getTimeZone("UTC");
  List<PanchangaWindowEngine.Segment> s=PanchangaWindowEngine.generate(new Date(0),new Date(2000),tz,t->new double[]{0,5+t/1000.0});
  check(s.size()==2 && s.get(0).karana==Karana.Kimstughna && s.get(1).karana==Karana.Bava,"Half tithi change");
  check(s.get(1).start.getTime()==1000,"Exact six-degree boundary");
  s=PanchangaWindowEngine.generate(new Date(0),new Date(2000),tz,t->new double[]{0,13+t/3000.0});
  check(s.size()==2 && s.get(1).yoga==Yoga.Priti,"Yoga transition");
  for(int i=0;i<60;i++) {
   Karana wanted=i==0?Karana.Kimstughna:i==57?Karana.Sakuna:i==58?Karana.Chatushpada:i==59?Karana.Naga:Karana.values()[(i-1)%7];
   check(PanchangaWindowEngine.karanaAt(i*6)==wanted,"Karana exact boundary "+i);
  }
  check(PanchangaWindowEngine.karanaAt(360)==Karana.Kimstughna,"Cycle wrap");
  check(PanchangaWindowEngine.generate(new Date(0),new Date(0),tz,t->new double[]{0,0}).isEmpty(),"Empty");
  try {PanchangaWindowEngine.generate(new Date(1),new Date(0),tz,t->new double[]{0,0});throw new AssertionError("Reversed");}catch(IllegalArgumentException expected){}
  try {PanchangaWindowEngine.karanaAt(Double.NaN);throw new AssertionError("NaN");}catch(IllegalArgumentException expected){}
  long midnight=86400000L;
  s=PanchangaWindowEngine.generate(new Date(midnight-1000),new Date(midnight+1000),tz,t->new double[]{0,0});
  check(s.size()==2 && s.get(1).start.getTime()==midnight,"Civil midnight");
  System.out.println("PanchangaWindowEngineTest: boundaries, all 60 Karana slots, wrap, midnight and invalid windows passed");
 }
}
