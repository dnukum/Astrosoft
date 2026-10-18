package app.astrosoft.core.test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import app.astrosoft.beans.MuhurtaProfile;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.MyMuhurta;
import app.astrosoft.ui.dlg.MuhurtaSummaryDialog;

public class MuhurtaSummaryTest extends TestCase {

    private Place chennai;
    private Date testDate;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        chennai = new Place("Chennai", "Tamil Nadu", "India", 13.0827, 80.2707, "Asia/Calcutta");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
        cal.set(2026, Calendar.OCTOBER, 17, 10, 30, 0);
        cal.set(Calendar.MILLISECOND, 0);
        testDate = cal.getTime();
    }

    public void testSummaryFieldsNoPerson() {
        MyMuhurta muhurta = new MyMuhurta(testDate, testDate, chennai);
        String summary = MuhurtaSummaryDialog.buildSummaryText(muhurta, chennai, null, null);

        assertNotNull(summary);
        assertTrue("Contains Date", summary.contains("1) Muhurta Date: October 17, 2026"));
        assertTrue("Contains Time", summary.contains("2) Muhurta Time: 10:30:00 AM"));
        assertTrue("Contains Location", summary.contains("3) Location with timezone: Chennai, Tamil Nadu, India, Asia/Calcutta"));
        assertTrue("Contains Paksha and Thithi", summary.contains("4) Paksha and Thithi:"));
        assertTrue("Contains Vara", summary.contains("5) Vara: Saturday"));
        assertTrue("Contains Nakshatra", summary.contains("6) Nakshatra:"));
        assertTrue("Contains Amirthathi Yoga", summary.contains("7) Amirthathi Yoga:"));
        assertTrue("Contains Tara/Chandra Balam not evaluated", summary.contains("8) Tara and Chandra Balam: Not evaluated (No person selected)"));
        assertTrue("Contains Panchanga Yoga", summary.contains("9) Panchanga Yoga:"));
        assertTrue("Contains Karana", summary.contains("10) Karana:"));
        assertTrue("Contains Planetary Positions header", summary.contains("Planetary Positions:"));

        // Verify planetary lines
        String[] lines = summary.split("\n");
        int planetSectionIdx = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().equals("Planetary Positions:")) {
                planetSectionIdx = i + 1;
                break;
            }
        }
        assertTrue("Planetary section found", planetSectionIdx > 0);

        // Pattern for line: Planet, Sign, DD° MM' SS", Nakshatra (Pada X)
        Pattern planetPattern = Pattern.compile("^([A-Za-z/]+),\\s+([A-Za-z]+),\\s+(\\d{2}°\\s+\\d{2}'\\s+\\d{2}\"),\\s+([A-Za-z ]+?)\\s+\\(Pada\\s+([1-4])\\)$");

        int planetCount = 0;
        String firstPlanet = null;
        Rasi ascSign = null;
        int prevSignClockwiseOffset = 0;

        for (int i = planetSectionIdx; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            Matcher m = planetPattern.matcher(line);
            assertTrue("Line should match planet pattern: " + line, m.matches());
            planetCount++;
            String pName = m.group(1);
            String signName = m.group(2);
            Rasi rasi = Rasi.valueOf(signName);

            if (planetCount == 1) {
                firstPlanet = pName;
                ascSign = rasi;
            } else {
                // Verify clockwise zodiac order from Ascendant's sign
                int offset = (rasi.ordinal() - ascSign.ordinal() + 12) % 12;
                assertTrue("Sign order must move clockwise around zodiac: " + signName + " after offset " + prevSignClockwiseOffset,
                    offset >= prevSignClockwiseOffset);
                prevSignClockwiseOffset = offset;
            }
        }

        assertEquals("First planet must be ASC", "ASC", firstPlanet);
        assertEquals("Must contain all 10 astrological points", 10, planetCount);
    }

    public void testSummaryFieldsWithPersonProfiles() {
        MyMuhurta muhurta = new MyMuhurta(testDate, testDate, chennai);
        MuhurtaProfile p1 = new MuhurtaProfile("John", Rasi.Mesha, Nakshathra.Ashwini);
        MuhurtaProfile p2 = new MuhurtaProfile("Jane", Rasi.Vrishabha, Nakshathra.Rohini);

        String summary = MuhurtaSummaryDialog.buildSummaryText(muhurta, chennai, p1, p2);

        assertTrue("Contains Person 1", summary.contains("Person 1 (John): Tara Balam:"));
        assertTrue("Contains Person 1 Chandra", summary.contains("Chandra Balam:"));
        assertTrue("Contains Person 2", summary.contains("Person 2 (Jane): Tara Balam:"));
        assertTrue("Contains Person 2 Chandra", summary.contains("Chandra Balam:"));
    }

    public void testClockwiseZodiacSortingWithMultiplePlanetsInSameSign() {
        MyMuhurta muhurta = new MyMuhurta(testDate, testDate, chennai);
        String summary = MuhurtaSummaryDialog.buildSummaryText(muhurta, chennai, null, null);

        String[] lines = summary.split("\n");
        int start = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().equals("Planetary Positions:")) {
                start = i + 1;
                break;
            }
        }

        Rasi currentRasi = null;
        int prevDeg = -1;
        Pattern p = Pattern.compile("^([A-Za-z/]+),\\s+([A-Za-z]+),\\s+(\\d{2})°\\s+(\\d{2})'\\s+(\\d{2})\".*");

        for (int i = start; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            Matcher m = p.matcher(line);
            assertTrue(m.matches());
            String planet = m.group(1);
            Rasi rasi = Rasi.valueOf(m.group(2));
            int degInSign = Integer.parseInt(m.group(3)) * 3600 + Integer.parseInt(m.group(4)) * 60 + Integer.parseInt(m.group(5));

            if (rasi.equals(currentRasi)) {
                // Within the same sign, if neither is Ascendant, degree must be non-decreasing
                if (!planet.equals("ASC")) {
                    assertTrue("Multiple planets in sign " + rasi + " must be sorted by degree: " + line,
                        degInSign >= prevDeg);
                }
            } else {
                currentRasi = rasi;
            }
            prevDeg = degInSign;
        }
    }

    public void testBalamCalendarSelectionPersistence() {
        MyMuhurta muhurta = new MyMuhurta(testDate, testDate, chennai);
        MuhurtaProfile p1 = new MuhurtaProfile("John", Rasi.Mesha, Nakshathra.Ashwini);
        final java.util.concurrent.atomic.AtomicReference<Date> selectedStartRef = new java.util.concurrent.atomic.AtomicReference<>();

        app.astrosoft.ui.comp.BalamCalendarPanel panel = new app.astrosoft.ui.comp.BalamCalendarPanel(
            muhurta,
            (start, end) -> selectedStartRef.set(start),
            p1,
            null
        );

        javax.swing.JTable table = panel.getTable();
        assertNotNull(table);
        assertTrue("Table should have rows", table.getRowCount() > 0);

        // Select a row (e.g. row 1)
        int rowToSelect = Math.min(1, table.getRowCount() - 1);
        table.setRowSelectionInterval(rowToSelect, rowToSelect);
        assertEquals(rowToSelect, table.getSelectedRow());
        assertTrue("Update button should be enabled after selection", panel.getUpdateButton().isEnabled());

        // Refresh context (which calls updateTable internally)
        panel.setContext(muhurta);

        // Selection and update button state must persist!
        assertEquals("Selected row must persist after updateTable()", rowToSelect, table.getSelectedRow());
        assertTrue("Update button must remain enabled after updateTable()", panel.getUpdateButton().isEnabled());
    }

    public void testMarch5DelhiMuhurtaTimeVsPanchang() {
        Place delhi = new Place("Delhi", "Delhi", "India", 28.6139, 77.2090, "Asia/Calcutta");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
        cal.set(2026, Calendar.MARCH, 5, 10, 15, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date muhurtaInstant = cal.getTime();

        MyMuhurta muhurta = new MyMuhurta(muhurtaInstant, muhurtaInstant, delhi);

        Nakshathra panchangNak = muhurta.getPanchang().getNakshathra();
        Nakshathra muhurtaInstantNak = muhurta.getEventHoroscope().getPlanetaryInfo().getPlanetNakshathra().get(Planet.Moon).getNak();

        assertEquals(Nakshathra.Uththiram, panchangNak); // Morning ref time: Uttra Phalguni
        assertEquals(Nakshathra.Hastam, muhurtaInstantNak); // Muhurta instant: Hasta

        // With Person 1 having birth star Uttra Phalguni (Uththiram) and birth rasi Mesha:
        MuhurtaProfile p1 = new MuhurtaProfile("House_Shift_Muhurta", Rasi.Mesha, Nakshathra.Uththiram);
        MyMuhurta muhurtaWithProfile = new MyMuhurta(muhurtaInstant, muhurtaInstant, delhi, p1);
        app.astrosoft.ui.table.TableData<app.astrosoft.ui.table.MapTableRow> tableData = muhurtaWithProfile.getTableData();

        String taraValue = null;
        String chandraValue = null;
        for (int i = 0; i < tableData.getRowCount(); i++) {
            app.astrosoft.ui.table.MapTableRow row = tableData.getRow(i);
            Object key = row.getColumnData(app.astrosoft.consts.AstrosoftTableColumn.Key);
            if ("Tara Balam".equals(key)) {
                taraValue = String.valueOf(row.getColumnData(app.astrosoft.consts.AstrosoftTableColumn.Value));
            } else if ("Chandra Balam".equals(key)) {
                chandraValue = String.valueOf(row.getColumnData(app.astrosoft.consts.AstrosoftTableColumn.Value));
            }
        }

        // At 10:15 AM (after 08:18 AM), transit star is Hasta, so Tara to Uththiram is raw score 2 (Sampat), and Chandra is raw score 6!
        assertEquals("2 (Sampat)", taraValue);
        assertEquals("6", chandraValue);
    }
}
