package app.astrosoft.util.test;

import junit.framework.TestCase;
import app.astrosoft.util.AstroUtil;

public class AstroUtilTest extends TestCase {
    
    public void testTimeFormatMidnight() {
        assertEquals("12:00 AM", AstroUtil.timeFormat(0.0, false));
        assertEquals("12:30 AM", AstroUtil.timeFormat(0.5, false));
    }
    
    public void testTimeFormatAM() {
        assertEquals("01:00 AM", AstroUtil.timeFormat(1.0, false));
        assertEquals("09:15 AM", AstroUtil.timeFormat(9.25, false));
        assertEquals("11:59 AM", AstroUtil.timeFormat(11.9833333333333, false));
    }
    
    public void testTimeFormatNoon() {
        assertEquals("12:00 PM", AstroUtil.timeFormat(12.0, false));
        assertEquals("12:30 PM", AstroUtil.timeFormat(12.5, false));
        assertEquals("12:58 PM", AstroUtil.timeFormat(12.9666666666667, false));
    }
    
    public void testTimeFormatPM() {
        assertEquals("01:00 PM", AstroUtil.timeFormat(13.0, false));
        assertEquals("03:45 PM", AstroUtil.timeFormat(15.75, false));
        assertEquals("11:59 PM", AstroUtil.timeFormat(23.9833333333333, false));
    }
}
