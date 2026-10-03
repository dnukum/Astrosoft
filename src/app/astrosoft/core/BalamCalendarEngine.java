package app.astrosoft.core;

import java.util.List;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;

import app.astrosoft.beans.BalamRowBean;
import app.astrosoft.util.SwissHelper;
import app.astrosoft.util.TransitHelper;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import app.astrosoft.util.AstroUtil;
import swisseph.SweDate;

public class BalamCalendarEngine {
    
    private static double getJD(Calendar cal) {
        long timeMs = cal.getTimeInMillis();
        return (timeMs / 86400000.0) + 2440587.5;
    }

    public static List<BalamRowBean> generateCalendar(int year, int month, TimeZone tz) {
        List<BalamRowBean> rows = new ArrayList<>();
        
        Calendar cal = Calendar.getInstance(tz);
        cal.set(year, month - 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        long startMs = cal.getTimeInMillis();
        double jdStart = getJD(cal);
        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        
        long endMs = cal.getTimeInMillis();
        double jdEnd = getJD(cal);
        
        SwissHelper sh = new SwissHelper();
        TransitHelper th = sh.getTransitHelper(Planet.Moon);
        
        double currentJd = jdStart;
        Date currentBoundaryTime = new Date(startMs);
        
        while (currentJd < jdEnd) {
            sh.setSweDate(new SweDate(currentJd));
            double pos = sh.getPlanetaryPosition().get(Planet.Moon);
            
            Nakshathra currentNak = Nakshathra.ofDeg(pos);
            Rasi currentRasi = Rasi.ofDeg(pos);
            
            double nextNakLong = AstroUtil.nakEndPosition(pos);
            double nextRasiLong = ((int)(pos / 30) + 1) * 30.0;
            
            double nextBoundary = Math.min(nextNakLong, nextRasiLong);
            if (Math.abs(nextNakLong - nextRasiLong) < 0.0001) {
                nextBoundary = nextNakLong;
            }
            if (nextBoundary >= 360.0) {
                nextBoundary = 0.0;
            }
            
            double nextJd = th.getTransit(nextBoundary, currentJd);
            
            if (nextJd <= currentJd) {
                // Emergency failsafe to prevent infinite loops (if precision jitters)
                nextJd = currentJd + (1.0 / (24.0 * 60.0)); 
            }
            
            Date rowEnd;
            if (nextJd >= jdEnd) {
                rowEnd = new Date(endMs);
                rows.add(new BalamRowBean(currentBoundaryTime, rowEnd, currentNak, currentRasi));
                break;
            } else {
                rowEnd = SweDate.getDate(nextJd);
                rows.add(new BalamRowBean(currentBoundaryTime, rowEnd, currentNak, currentRasi));
                currentBoundaryTime = rowEnd;
                // Add 1 minute to the JD to step safely into the next Nakshatra/Rasi zone
                currentJd = nextJd + (1.0 / (24.0 * 60.0)); 
            }
        }
        
        return rows;
    }
}
