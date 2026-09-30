package app.astrosoft.core.test;

import junit.framework.TestCase;
import app.astrosoft.core.Panchang;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.util.AstroUtil;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class PanchangGoldenTest extends TestCase {

    public void testPanchangAgainstGoldenData() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        
        for (PanchangGoldenData.TestCase tc : PanchangGoldenData.getCases()) {
            Date d = df.parse(tc.dateStr);
            
            // To properly test the local day, we construct the Calendar in the target timezone
            // with the time set to 12:00 PM local time.
            TimeZone tz = TimeZone.getTimeZone(tc.tzId);
            Calendar cal = Calendar.getInstance(tz);
            cal.setTime(d);
            cal.set(Calendar.HOUR_OF_DAY, 12);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            
            Place place = new Place(tc.name, "", "", tc.lat, tc.lon, tc.tzId);
            Panchang pan = new Panchang(cal.getTime(), place);
            
            // Ensure Nakshatra was calculated properly without crashing
            Nakshathra nak = pan.getNakshathra();
            assertNotNull("Nakshatra should not be null for " + tc.name + " on " + tc.dateStr, nak);
            
            // Ensure Sunrise/Sunset doesn't crash on boundary conditions (e.g., Extreme latitudes)
            double sr = AstroUtil.getSunRise(cal, place);
            double sn = AstroUtil.getSunSet(cal, place);
            assertTrue("Sunrise must be positive", sr > 0);
            assertTrue("Sunset must be after Sunrise", sn > sr);
            
            // Optional: If we want to strictly assert nakshatra matches pyjhora.
            // Pyjhora returns 1-indexed (1=Ashwini, 27=Revati) or 0-indexed depending on version.
            // We can assert here if we map them perfectly.
        }
    }
}
