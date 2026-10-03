package app.astrosoft.core.test;

import junit.framework.TestCase;
import app.astrosoft.core.BalamCalendarEngine;
import app.astrosoft.beans.BalamRowBean;
import java.util.List;
import java.util.TimeZone;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BalamCalendarEngineTest extends TestCase {

    private static class ExpectedEvent {
        Date time;
        String event;
        double boundary;
    }

    private List<ExpectedEvent> loadGoldenData(String key, TimeZone tz) throws Exception {
        List<ExpectedEvent> events = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader("src/app/astrosoft/core/test/data/BalamCalendarGoldenData.json"));
        String line;
        boolean inBlock = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(tz);
        
        ExpectedEvent currentEvent = null;

        while ((line = reader.readLine()) != null) {
            if (line.contains("\"" + key + "\": [")) {
                inBlock = true;
                continue;
            }
            if (inBlock) {
                if (line.trim().startsWith("]")) break;
                if (line.contains("{")) {
                    currentEvent = new ExpectedEvent();
                }
                if (line.contains("\"time\"") && currentEvent != null) {
                    String timeStr = line.split("\"time\"\\s*:\\s*\"")[1].split("\"")[0];
                    currentEvent.time = sdf.parse(timeStr);
                }
                if (line.contains("\"event\"") && currentEvent != null) {
                    currentEvent.event = line.split("\"event\"\\s*:\\s*\"")[1].split("\"")[0];
                }
                if (line.contains("\"boundary\"") && currentEvent != null) {
                    String b = line.split("\"boundary\"\\s*:\\s*")[1].replaceAll("[^0-9.]", "");
                    currentEvent.boundary = Double.parseDouble(b);
                }
                if (line.contains("}") && currentEvent != null) {
                    events.add(currentEvent);
                    currentEvent = null;
                }
            }
        }
        reader.close();
        return events;
    }

    private void runValidation(int year, int month, String tzName) throws Exception {
        TimeZone tz = TimeZone.getTimeZone(tzName);
        String key = tzName + "_" + year + "_" + month;
        List<ExpectedEvent> expected = loadGoldenData(key, tz);
        
        List<BalamRowBean> actual = BalamCalendarEngine.generateCalendar(year, month, tz);
        
        assertTrue("Engine generated no rows for " + key, actual.size() > 0);
        assertEquals("Start time mismatch for " + key, expected.get(0).time.getTime(), actual.get(0).getStartTime().getTime());
        
        int actualIdx = 0;
        for (int i = 1; i < expected.size(); i++) {
            ExpectedEvent ev = expected.get(i);
            BalamRowBean row = actual.get(actualIdx);
            
            long diff = Math.abs(ev.time.getTime() - row.getEndTime().getTime());
            assertTrue("Timestamp mismatch for " + key + " at index " + i + ". Expected: " + ev.time + ", Actual: " + row.getEndTime(), diff <= 60000);
            actualIdx++;
        }
        
        assertEquals("Generated calendar size mismatch", expected.size() - 1, actual.size());
    }

    public void testBaselineBengaluru() throws Exception { runValidation(2026, 10, "Asia/Kolkata"); }
    public void testDaylightSavingSydney() throws Exception { runValidation(2026, 10, "Australia/Sydney"); }
    public void testLeapYearAnchorage() throws Exception { runValidation(2028, 2, "America/Anchorage"); }
}
