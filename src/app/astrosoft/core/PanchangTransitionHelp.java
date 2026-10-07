package app.astrosoft.core;
import java.util.*;
import java.text.SimpleDateFormat;
import app.astrosoft.consts.DisplayStrings;
/** Presentation only: existing local end-hour values, explicit successor names and row help. */
public final class PanchangTransitionHelp {
 private PanchangTransitionHelp() {}
 public static String until(String current,double ending,String next,Calendar day) {
  long minutes=Math.round(ending*60);int days=(int)Math.floorDiv(minutes,1440);
  int clock=(int)Math.floorMod(minutes,1440);
  Calendar date=(Calendar)day.clone();date.add(Calendar.DATE,days);
  date.set(Calendar.HOUR_OF_DAY,clock/60);date.set(Calendar.MINUTE,clock%60);date.set(Calendar.SECOND,0);date.set(Calendar.MILLISECOND,0);
  String pattern=days==0?"hh:mm a":date.get(Calendar.YEAR)==day.get(Calendar.YEAR)?"dd MMM, hh:mm a":"dd MMM yyyy, hh:mm a";
  SimpleDateFormat format=new SimpleDateFormat(pattern,Locale.ENGLISH);format.setTimeZone(day.getTimeZone());
  return current+" until "+format.format(date.getTime())+" → "+next;
 }
 public static String forRow(String key) {
  if(key==null)return null;
  if(key.equals("Abhijit Muhurta")||key.equals("Brahma Muhurta"))return MuhurtaWindowsCalculator.help(key);
  if(key.equals("Amirthathi Yoga"))return "Calculated from the active nakshatra and sunrise-to-sunrise Vara. Before sunrise, the previous weekday's Vara applies, regardless of the displayed Day. Arrow times use the selected location's time zone.";
  if(key.equals(DisplayStrings.DAY_STR.toString()))return "Civil weekday for the displayed date. Amirthathi Yoga uses sunrise-based Vara separately.";
  if(key.startsWith(DisplayStrings.AUS_TIME_STR.toString()))return "These are auspicious time blocks, not the Nitya Yoga named Subha. Read the other Panchang rows at the time you intend to select.";
  if(key.startsWith(DisplayStrings.NAK_STR.toString())||key.startsWith(DisplayStrings.THITHI_STR.toString())||key.startsWith(DisplayStrings.YOGA_STR.toString())||key.startsWith(DisplayStrings.KARANA_STR.toString()))return "The classification before the arrow applies until the displayed time; the name after it applies from that instant. Times use the selected location's time zone. A date is shown for transitions after midnight. These rows retain the daily Panchang reference-time calculation.";
  return null;
 }
}
