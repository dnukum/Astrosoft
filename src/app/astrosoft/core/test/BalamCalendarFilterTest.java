package app.astrosoft.core.test;

import java.awt.Component;
import java.awt.Container;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.swing.JButton;
import javax.swing.JTable;

import junit.framework.TestCase;

import app.astrosoft.beans.BalamDateCell;
import app.astrosoft.beans.BalamRowBean;
import app.astrosoft.beans.ChandraBalamResult;
import app.astrosoft.beans.MuhurtaProfile;
import app.astrosoft.beans.Place;
import app.astrosoft.beans.TaraBalamResult;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.BalamCalendarEngine;
import app.astrosoft.core.BalamCalendarModel;
import app.astrosoft.core.MyMuhurta;
import app.astrosoft.ui.comp.BalamCalendarPanel;
import app.astrosoft.ui.table.MapTableRow;
import app.astrosoft.ui.table.TableData;

/**
 * Phase 1 TDD suite: Whole-segment filtering based on Tara Balam (7) and Chandra Balam (8).
 * Real-world cases grounded in Chennai, October 2026 (IST).
 */
public class BalamCalendarFilterTest extends TestCase {

    private static final Place CHENNAI = new Place(
        "Chennai", "Tamil Nadu", "India",
        new Place.Location(13, 4, Place.Direction.NORTH),
        new Place.Location(80, 17, Place.Direction.EAST),
        "IST"
    );
    private static final TimeZone IST = CHENNAI.astrosoftTimeZone().getTimeZone();

    private static final MuhurtaProfile PERSON_1 =
        new MuhurtaProfile("Person 1", Rasi.Mesha, Nakshathra.Ashwini);
    private static final MuhurtaProfile PERSON_2 =
        new MuhurtaProfile("Person 2", Rasi.Vrishabha, Nakshathra.Rohini);

    private static List<BalamRowBean> oct2026Rows;

    private static synchronized List<BalamRowBean> getOct2026Rows() {
        if (oct2026Rows == null) {
            oct2026Rows = BalamCalendarEngine.generateCalendar(2026, 10, IST);
        }
        return oct2026Rows;
    }

    private static BalamRowBean octRow(int index) {
        return getOct2026Rows().get(index);
    }

    private static Date istDate(int year, int month, int day, int hour, int minute) {
        Calendar c = Calendar.getInstance(IST);
        c.clear();
        c.set(year, month - 1, day, hour, minute, 0);
        return c.getTime();
    }

    private static String fmtIst(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setTimeZone(IST);
        return sdf.format(d);
    }

    /**
     * Test Case 1: Single Person — Tara Balam = 7 (Naidhana) is excluded.
     * Row 30: [2026-10-23 Fri 15:03 -> 2026-10-23 Fri 21:02 IST], Poorattadhi in Meena.
     * Person 1 (Mesha, Ashwini): Tara = 7, Chandra = 12.
     */
    public void testCase01_SinglePersonTaraBalam7Excluded() {
        BalamRowBean row30 = octRow(30);
        assertEquals(Nakshathra.Poorattadhi, row30.getTransitNakshathra());
        assertEquals(Rasi.Meena, row30.getTransitRasi());

        List<BalamRowBean> list = new ArrayList<>();
        list.add(row30);
        BalamCalendarModel model = new BalamCalendarModel(IST, PERSON_1, CHENNAI);
        List<BalamRowBean> filtered = model.filterAndTrimRows(list, null);
        assertTrue("Row 30 (Tara 7) must be excluded for Person 1", filtered.isEmpty());
    }

    /**
     * Test Case 2: Single Person — Chandra Balam = 8 (Ashtama) is excluded.
     * Row 19: [2026-10-14 Wed 01:42 -> 2026-10-15 Thu 04:03 IST], Anusham in Vrichika.
     * Person 1 (Mesha, Ashwini): Tara = 8, Chandra = 8.
     */
    public void testCase02_SinglePersonChandraBalam8Excluded() {
        BalamRowBean row19 = octRow(19);
        assertEquals(Nakshathra.Anusham, row19.getTransitNakshathra());
        assertEquals(Rasi.Vrichika, row19.getTransitRasi());

        List<BalamRowBean> list = new ArrayList<>();
        list.add(row19);
        BalamCalendarModel model = new BalamCalendarModel(IST, PERSON_1, CHENNAI);
        List<BalamRowBean> filtered = model.filterAndTrimRows(list, null);
        assertTrue("Row 19 (Chandra 8) must be excluded for Person 1", filtered.isEmpty());
    }

    /**
     * Test Case 3: Two Persons — Person 1 is good, Person 2 has Tara Balam = 7. Excluded.
     * Row 9: [2026-10-06 Tue 22:17 -> 2026-10-07 Wed 21:40 IST], Magam in Simha.
     * Person 1: Tara = 1, Chandra = 5.
     * Person 2: Tara = 7 (Naidhana), Chandra = 4.
     */
    public void testCase03_TwoPersonsPartnerTara7Excluded() {
        BalamRowBean row9 = octRow(9);
        assertEquals(Nakshathra.Magam, row9.getTransitNakshathra());

        List<BalamRowBean> list = new ArrayList<>();
        list.add(row9);
        BalamCalendarModel model = new BalamCalendarModel(IST, PERSON_1, CHENNAI);

        // Survives for P1 alone
        assertEquals(1, model.filterAndTrimRows(list, null).size());
        // Excluded when P2 is added
        assertTrue("Row 9 must be excluded when Person 2 has Tara 7",
            model.filterAndTrimRows(list, PERSON_2).isEmpty());
    }

    /**
     * Test Case 4: Two Persons — Person 1 is good, Person 2 has Chandra Balam = 8. Excluded.
     * Row 22: [2026-10-17 Sat 09:46 -> 2026-10-18 Sun 12:48 IST], Pooradam in Dhanus.
     * Person 1: Tara = 2, Chandra = 9.
     * Person 2: Tara = 8, Chandra = 8 (Ashtama).
     */
    public void testCase04_TwoPersonsPartnerChandra8Excluded() {
        BalamRowBean row22 = octRow(22);
        assertEquals(Nakshathra.Pooradam, row22.getTransitNakshathra());

        List<BalamRowBean> list = new ArrayList<>();
        list.add(row22);
        BalamCalendarModel model = new BalamCalendarModel(IST, PERSON_1, CHENNAI);

        // Survives for P1 alone
        assertEquals(1, model.filterAndTrimRows(list, null).size());
        // Excluded when P2 is added
        assertTrue("Row 22 must be excluded when Person 2 has Chandra 8",
            model.filterAndTrimRows(list, PERSON_2).isEmpty());
    }

    /**
     * Test Case 5: Two Persons — Both pass Rules 1 & 2. Kept in full.
     * Row 27: [2026-10-21 Wed 07:00 -> 2026-10-21 Wed 19:47 IST], Avittam in Kumbha.
     * Person 1: Tara = 5, Chandra = 11.
     * Person 2: Tara = 2, Chandra = 10.
     */
    public void testCase05_TwoPersonsBothPassTaraAndChandra() {
        BalamRowBean row27 = octRow(27);
        assertEquals(Nakshathra.Avittam, row27.getTransitNakshathra());

        List<BalamRowBean> list = new ArrayList<>();
        list.add(row27);
        BalamCalendarModel model = new BalamCalendarModel(IST, PERSON_1, CHENNAI);

        List<BalamRowBean> filtered = model.filterAndTrimRows(list, PERSON_2);
        assertEquals(1, filtered.size());
        assertEquals("2026-10-21 07:00", fmtIst(filtered.get(0).getStartTime()));
        assertEquals("2026-10-21 19:47", fmtIst(filtered.get(0).getEndTime()));
    }

    /**
     * Test Case 6: Full month invariant check for Chennai October 2026.
     * - Unfiltered: 42 rows
     * - Filtered for Person 1 alone: 33 rows (9 rows removed)
     * - Filtered for Person 1 + Person 2: 28 rows (14 rows removed)
     * - Every surviving row has Tara != 7 and Chandra != 8 for active profiles.
     */
    public void testCase06_RealWorldChennaiOctober2026Invariants() {
        Date refDate = istDate(2026, 10, 15, 10, 0);
        MyMuhurta muhurta = new MyMuhurta(refDate, refDate, CHENNAI, PERSON_1);

        TableData<MapTableRow> unfiltered = muhurta.getBalamCalendarTable(2026, 10, PERSON_2, false);
        TableData<MapTableRow> filteredP1Only = muhurta.getBalamCalendarTable(2026, 10, null, true);
        TableData<MapTableRow> filteredBoth = muhurta.getBalamCalendarTable(2026, 10, PERSON_2, true);

        assertEquals("Unfiltered Oct 2026 has 42 rows", 42, unfiltered.getRowCount());
        assertEquals("Filtered P1 alone has 33 rows", 33, filteredP1Only.getRowCount());
        assertEquals("Filtered P1 + P2 has 28 rows", 28, filteredBoth.getRowCount());

        for (int i = 0; i < filteredBoth.getRowCount(); i++) {
            MapTableRow row = filteredBoth.getRow(i);
            TaraBalamResult t1 = (TaraBalamResult) row.getColumnData(AstrosoftTableColumn.TaraBalam);
            ChandraBalamResult c1 = (ChandraBalamResult) row.getColumnData(AstrosoftTableColumn.ChandraBalam);
            TaraBalamResult t2 = (TaraBalamResult) row.getColumnData(AstrosoftTableColumn.SpouseTaraBalam);
            ChandraBalamResult c2 = (ChandraBalamResult) row.getColumnData(AstrosoftTableColumn.SpouseChandraBalam);

            assertTrue("P1 Tara != 7", t1.getTara().getNumber() != 7);
            assertTrue("P1 Chandra != 8", c1.getHouse().getHouseNumber() != 8);
            assertTrue("P2 Tara != 7", t2.getTara().getNumber() != 7);
            assertTrue("P2 Chandra != 8", c2.getHouse().getHouseNumber() != 8);
        }
    }

    /**
     * Test Case 7: UI Button Default ("Filter"), Toggle ("Show All" <-> "Filter"),
     * and Stickiness across month navigation.
     */
    public void testCase07_UiToggleAndStickinessAcrossMonths() {
        Date refDate = istDate(2026, 10, 15, 10, 0);
        MyMuhurta muhurta = new MyMuhurta(refDate, refDate, CHENNAI, PERSON_1);

        final List<Date[]> updates = new ArrayList<>();
        BalamCalendarPanel panel = new BalamCalendarPanel(
            muhurta, (s, e) -> updates.add(new Date[] { s, e }), PERSON_1, PERSON_2);
        try {
            JButton filterBtn = findButton(panel, "Filter");
            assertNotNull("Default button with label 'Filter' must be present", filterBtn);

            JTable table = findTable(panel);
            assertNotNull("Calendar table must be present", table);
            assertEquals("Default view shows all 42 rows for Oct 2026", 42, table.getRowCount());

            // Click "Filter" -> switches to Filtered mode (28 rows), label becomes "Show All"
            filterBtn.doClick();
            assertEquals("Button text must change to 'Show All' when filtered", "Show All", filterBtn.getText());
            table = findTable(panel);
            assertEquals("Filtered view shows 28 rows for Oct 2026", 28, table.getRowCount());

            // Navigate to Next Month (Nov 2026) -> Filtered mode and "Show All" label remain sticky
            JButton nextBtn = findButton(panel, "Next Month >");
            assertNotNull(nextBtn);
            nextBtn.doClick();
            assertEquals("Filter mode must remain sticky ('Show All' label) across month navigation", "Show All", filterBtn.getText());
            int filteredNovCount = findTable(panel).getRowCount();

            // Click "Show All" -> switches back to unfiltered mode, label becomes "Filter"
            filterBtn.doClick();
            assertEquals("Button text must return to 'Filter'", "Filter", filterBtn.getText());
            int unfilteredNovCount = findTable(panel).getRowCount();
            assertTrue("Unfiltered November has more rows than filtered November", unfilteredNovCount > filteredNovCount);
        } finally {
            panel.removeNotify();
        }
    }

    private static JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) {
                return (JButton) c;
            }
            if (c instanceof Container) {
                JButton found = findButton((Container) c, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static JTable findTable(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JTable) {
                return (JTable) c;
            }
            if (c instanceof Container) {
                JTable found = findTable((Container) c);
                if (found != null) return found;
            }
        }
        return null;
    }
}
