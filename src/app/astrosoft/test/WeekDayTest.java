package app.astrosoft.test;

import junit.framework.TestCase;
import app.astrosoft.consts.WeekDay;

public class WeekDayTest extends TestCase {

    public void testWinterSolsticeDec20() {
        // Sammamish, WA (Dec 20, 2026) - Sunday
        double sunrise = 8.00; // 08:00 AM
        double sunset = 16.2166; // 04:13 PM
        
        WeekDay sunday = WeekDay.Sunday;
        
        String rahuKala = sunday.rahuKala(sunrise, sunset);
        assertEquals("03:11 PM - 04:12 PM", rahuKala);
        
        String yamaKanda = sunday.yamaKanda(sunrise, sunset);
        assertEquals("00:06 PM - 01:08 PM", yamaKanda);
    }

    public void testSummerSolsticeJun21() {
        // Sammamish, WA (Jun 21, 2026) - Sunday
        double sunrise = 5.2833; // 05:17 AM
        double sunset = 21.0666; // 09:04 PM
        
        WeekDay sunday = WeekDay.Sunday;
        
        String rahuKala = sunday.rahuKala(sunrise, sunset);
        assertEquals("07:05 PM - 09:03 PM", rahuKala);
        
        String yamaKanda = sunday.yamaKanda(sunrise, sunset);
        assertEquals("01:10 PM - 03:08 PM", yamaKanda);
    }

    public void testVernalEquinoxMar20() {
        // Vernal Equinox (Mar 20, 2026) - Friday
        double sunrise = 6.2833; // 06:17 AM
        double sunset = 18.2666; // 06:16 PM
        
        WeekDay friday = WeekDay.Friday;
        
        String rahuKala = friday.rahuKala(sunrise, sunset);
        assertEquals("10:46 AM - 00:16 PM", rahuKala);
        
        String yamaKanda = friday.yamaKanda(sunrise, sunset);
        assertEquals("03:16 PM - 04:46 PM", yamaKanda);
    }
}
