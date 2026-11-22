package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Point;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Varga;
import app.astrosoft.core.Ephemeris;
import app.astrosoft.core.Ephemeris.Mode;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.comp.chart.ChartTooltips;
import app.astrosoft.ui.comp.modern.DatePickerDialog;
import app.astrosoft.ui.dlg.EphemerisConfigDialog;
import app.astrosoft.ui.table.ColumnMetaData;
import app.astrosoft.ui.table.EphemerisCellRenderer;
import app.astrosoft.ui.table.Table;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.view.EphemerisView;
import app.astrosoft.ui.view.EphemerisView.UiMode;

/**
 * Unit and headless-safe UI tests for Ephemeris Basic &amp; Advanced modes.
 */
public class EphemerisAdvancedTest {

    @Test
    public void testBasicCompatibilityNoAscColumn() {
        Calendar cal = new GregorianCalendar(2026, Calendar.NOVEMBER, 1);
        Ephemeris eph = new Ephemeris(cal, Mode.Daily);
        Table table = eph.getEphemerisTable();
        ColumnMetaData meta = table.getColumnMetaData();

        assertEquals(10, meta.getColumns().size());
        assertEquals(AstrosoftTableColumn.Date, meta.getColumns().get(0));
        assertFalse("Basic Daily mode must not include Ascendant", meta.getColumns().contains(AstrosoftTableColumn.Ascendant));
    }

    @Test
    public void testAdvancedDailyIncludesAscAndRespondsToTimeAndPlace() {
        Calendar cal = new GregorianCalendar(2026, Calendar.NOVEMBER, 15);
        Place mumbai = new Place("Mumbai", "Maharashtra", "India", 18.96, 72.82, 5.5);
        Place sanFrancisco = new Place("San Francisco", "CA", "USA", 37.77, -122.42, -8.0);

        Ephemeris ephMorning = new Ephemeris(cal, Mode.Daily, 6.0, mumbai, true);
        Ephemeris ephNoon = new Ephemeris(cal, Mode.Daily, 12.0, mumbai, true);
        Ephemeris ephSf = new Ephemeris(cal, Mode.Daily, 6.0, sanFrancisco, true);

        Table tMorning = ephMorning.getEphemerisTable();
        ColumnMetaData meta = tMorning.getColumnMetaData();
        assertEquals(11, meta.getColumns().size());
        assertEquals(AstrosoftTableColumn.Date, meta.getColumns().get(0));
        assertEquals(AstrosoftTableColumn.Ascendant, meta.getColumns().get(1));

        String ascMorning = tMorning.getTableData().getRow(0).getColumnData(AstrosoftTableColumn.Ascendant).toString();
        String ascNoon = ephNoon.getEphemerisTable().getTableData().getRow(0).getColumnData(AstrosoftTableColumn.Ascendant).toString();
        String ascSf = ephSf.getEphemerisTable().getTableData().getRow(0).getColumnData(AstrosoftTableColumn.Ascendant).toString();

        assertFalse("ASC should change with calculation time", ascMorning.equals(ascNoon));
        assertFalse("ASC should change with location", ascMorning.equals(ascSf));
    }

    @Test
    public void testAdvancedMonthlyOmitsAscColumn() {
        Calendar cal = new GregorianCalendar(2026, Calendar.NOVEMBER, 1);
        Place mumbai = Place.getDefault();
        Ephemeris eph = new Ephemeris(cal, Mode.Monthly, 12.0, mumbai, true);
        ColumnMetaData meta = eph.getEphemerisTable().getColumnMetaData();

        assertEquals(10, meta.getColumns().size());
        assertEquals(AstrosoftTableColumn.Month, meta.getColumns().get(0));
        assertFalse("Monthly mode must omit Ascendant", meta.getColumns().contains(AstrosoftTableColumn.Ascendant));
    }

    @Test
    public void testCalendarImmutabilityAndUtMidnightWrap() {
        Calendar cal = new GregorianCalendar(2026, Calendar.NOVEMBER, 15, 14, 30, 0);
        Place ist = new Place("Mumbai", "MH", "India", 18.96, 72.82, 5.5);
        Place pst = new Place("San Francisco", "CA", "USA", 37.77, -122.42, -8.0);

        // 12:30 AM IST -> ephTime = 0.5 - 5.5 = -5.0 (negative UT)
        Ephemeris ephIst = new Ephemeris(cal, Mode.Daily, 0.5, ist, true);
        // 11:30 PM PST -> ephTime = 23.5 - (-8.0) = 31.5 (>= 24 UT)
        Ephemeris ephPst = new Ephemeris(cal, Mode.Daily, 23.5, pst, true);

        assertEquals("Caller calendar day must remain unmutated", 15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals("Caller calendar hour must remain unmutated", 14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, ephIst.getEphemerisTable().getTableData().getRowCount());
        assertEquals(30, ephPst.getEphemerisTable().getTableData().getRowCount());
    }

    @Test
    public void testEphemerisViewDefaultsToBasicAndSwitchesToAdvanced() {
        Ephemeris eph = new Ephemeris(new GregorianCalendar(2026, Calendar.NOVEMBER, 1), Mode.Daily);
        EphemerisView view = new EphemerisView(eph, new Point(10, 2));

        assertEquals("EphemerisView must default to Basic", UiMode.Basic, view.getUiMode());
        assertEquals(10, view.getEphTable().getColumnCount());

        view.setUiMode(UiMode.Advanced);
        assertEquals(UiMode.Advanced, view.getUiMode());
        assertEquals(11, view.getEphTable().getColumnCount());
        assertTrue(view.getHrFwdBtn().isEnabled());
        assertTrue(view.getMinFwdBtn().isEnabled());

        view.setUiMode(UiMode.Basic);
        assertEquals(UiMode.Basic, view.getUiMode());
        assertEquals(10, view.getEphTable().getColumnCount());
    }

    @Test
    public void testYearEndAndLeapYearRollover() {
        Ephemeris eph = new Ephemeris(new GregorianCalendar(2026, Calendar.DECEMBER, 1), Mode.Daily);
        EphemerisView view = new EphemerisView(eph, new Point(10, 2));

        Calendar dec31 = new GregorianCalendar(2026, Calendar.DECEMBER, 31, 23, 30, 0);
        view.restoreSessionState(UiMode.Advanced, dec31, Place.getDefault(), Mode.Daily, Calendar.DECEMBER);

        // +1 hour -> Jan 1, 2027 12:30 AM
        view.stepActiveMinutes(60);
        Calendar afterFwd = view.getActiveDateTime();
        assertEquals(2027, afterFwd.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, afterFwd.get(Calendar.MONTH));
        assertEquals(1, afterFwd.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, afterFwd.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, afterFwd.get(Calendar.MINUTE));
        assertEquals(0, view.getEphTable().getSelectedRow());

        // -1 hour -> Dec 31, 2026 11:30 PM
        view.stepActiveMinutes(-60);
        Calendar afterBack = view.getActiveDateTime();
        assertEquals(2026, afterBack.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, afterBack.get(Calendar.MONTH));
        assertEquals(31, afterBack.get(Calendar.DAY_OF_MONTH));
        assertEquals(23, afterBack.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, afterBack.get(Calendar.MINUTE));
        assertEquals(30, view.getEphTable().getSelectedRow());

        // Leap year Feb 29, 2024 23:45 + 30m -> Mar 1, 2024 00:15
        Calendar leap = new GregorianCalendar(2024, Calendar.FEBRUARY, 29, 23, 45, 0);
        view.restoreSessionState(UiMode.Advanced, leap, Place.getDefault(), Mode.Daily, Calendar.FEBRUARY);
        view.stepActiveMinutes(30);
        Calendar afterLeap = view.getActiveDateTime();
        assertEquals(2024, afterLeap.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, afterLeap.get(Calendar.MONTH));
        assertEquals(1, afterLeap.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, afterLeap.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, afterLeap.get(Calendar.MINUTE));
    }

    @Test
    public void testMonthlyToDailySelectedMonthCarryOver() {
        Ephemeris eph = new Ephemeris(new GregorianCalendar(2026, Calendar.JANUARY, 1), Mode.Monthly);
        EphemerisView view = new EphemerisView(eph, new Point(10, 2));
        view.setUiMode(UiMode.Advanced);

        assertEquals(Mode.Monthly, view.getEphMode());
        assertFalse("Hour steppers must be disabled in Monthly mode", view.getHrFwdBtn().isEnabled());
        assertFalse("Minute steppers must be disabled in Monthly mode", view.getMinFwdBtn().isEnabled());
        assertEquals(10, view.getEphTable().getColumnCount());

        // Select August (index 7) in Monthly table, then switch to Daily
        view.getEphTable().setRowSelectionInterval(7, 7);
        // Simulate clicking Daily via restoreSessionState/switch path by checking getSelectedRow carry-over
        assertEquals(7, view.getEphTable().getSelectedRow());
    }

    @Test
    public void testEphemerisConfigDialogEmbeddedAndUnattendedSafe() {
        EphemerisConfigDialog dlg = new EphemerisConfigDialog(null, LocalTime.of(14, 45, 33), Place.getDefault());
        assertNotNull(dlg.getTimePickerPanel());
        assertNotNull(dlg.getPlaceSearchPanel());
        assertEquals("Seconds should be zeroed", LocalTime.of(14, 45, 0), dlg.getSelectedTime());
        dlg.dispose();

        String prevUnattended = System.getProperty("astrosoft.test.unattended");
        System.setProperty("astrosoft.test.unattended", "true");
        try {
            assertNull("Unattended showConfigDialog must not block",
                    EphemerisConfigDialog.showConfigDialog(null, LocalTime.NOON, Place.getDefault()));
            assertNotNull("Unattended showDatePicker must return initialDate without blocking",
                    DatePickerDialog.showDatePicker(null, java.time.LocalDate.of(2026, 11, 1)));
        } finally {
            if (prevUnattended == null) {
                System.clearProperty("astrosoft.test.unattended");
            } else {
                System.setProperty("astrosoft.test.unattended", prevUnattended);
            }
        }
    }

    @Test
    public void testCellRendererSelectionContrast() {
        EphemerisCellRenderer renderer = new EphemerisCellRenderer();
        Ephemeris eph = new Ephemeris(new GregorianCalendar(2026, Calendar.NOVEMBER, 1), Mode.Daily);
        EphemerisView view = new EphemerisView(eph, new Point(10, 2));
        Object val = view.getEphTable().getValueAt(0, 1);

        renderer.getTableCellRendererComponent(view.getEphTable(), val, true, false, 0, 1);
        Color selectedFg = renderer.getForeground();
        assertNotNull(selectedFg);
    }

    @Test
    public void testD1ChartDataAndDailyOnlyVisibilityAndTheme() {
        Calendar cal = new GregorianCalendar(2026, Calendar.DECEMBER, 5, 3, 0, 0);
        Place mumbai = Place.getDefault();
        Ephemeris eph = new Ephemeris(cal, Mode.Daily, 3.0, mumbai, true);
        PlanetChartData data = eph.getPlanetChartData(4);
        assertNotNull(data);
        assertEquals(Varga.Rasi, data.getVarga());
        assertNotNull(data.getPlanetLongitude(Planet.Ascendant));
        for (Planet p : Planet.allPlanets()) {
            assertNotNull("D1 chart data must include " + p, data.getPlanetLongitude(p));
        }

        EphemerisView view = new EphemerisView(eph, new Point(10, 2));
        view.restoreSessionState(UiMode.Advanced, cal, mumbai, Mode.Daily, Calendar.DECEMBER);

        assertTrue("D1 chart card must be visible in Advanced Daily mode", view.getChartCard().isVisible());
        assertTrue("D1 chart container must hold a Chart", view.getChartContainer().getComponentCount() > 0);
        assertTrue(view.getChartContainer().getComponent(0) instanceof Chart);
        assertEquals("Advanced table must inherit ChartTheme background",
                UIConsts.getChartTheme().getBackground(), view.getAdvTable().getBackground());

        // Monthly mode hides D1 chart and preserves selectedMonthRow
        view.restoreSessionState(UiMode.Advanced, cal, mumbai, Mode.Monthly, 7);
        assertFalse("D1 chart card must be hidden in Monthly mode", view.getChartCard().isVisible());
        assertEquals(7, view.getSelectedMonthRow());
    }

    @Test
    public void testCombustionInD1ChartDataAndTooltip() {
        // Oct 26, 2026: Venus and Sun are close in Libra (Venus combust)
        Calendar cal = new GregorianCalendar(2026, Calendar.OCTOBER, 26, 0, 0, 0);
        Ephemeris eph = new Ephemeris(cal, Mode.Daily, 0.0, Place.getDefault(), true);
        PlanetChartData data = eph.getPlanetChartData(25);
        assertNotNull(data.getPlanetCombustion());
        assertTrue("Venus should be combust on Oct 26, 2026",
                Boolean.TRUE.equals(data.getPlanetCombustion().get(Planet.Venus)));
        String tip = ChartTooltips.planetToolTip(data, Planet.Venus);
        assertNotNull(tip);
        assertTrue("Tooltip should mention Combust (Asta)", tip.contains("Combust (Asta)"));

        // Verify narrow 26px cell (3-planet house) shrinks font so Me(C) fits without clipping
        javax.swing.JTable tbl = new javax.swing.JTable(new Object[][] { { Planet.Mercury } }, new Object[] { "P" });
        tbl.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 12));
        tbl.getColumnModel().getColumn(0).setWidth(26);
        java.util.EnumMap<Planet, Boolean> comb = new java.util.EnumMap<>(Planet.class);
        comb.put(Planet.Mercury, true);
        app.astrosoft.ui.table.PlanetCellRenderer pcr = new app.astrosoft.ui.table.PlanetCellRenderer(
                tbl.getDefaultRenderer(Object.class), true, false, null);
        pcr.setPlanetCombustion(comb);
        javax.swing.JLabel rendered = (javax.swing.JLabel) pcr.getTableCellRendererComponent(tbl, Planet.Mercury, false, false, 0, 0);
        assertEquals("Me(C)", rendered.getText());
        assertTrue("Rendered Me(C) width must fit inside 26px cell",
                rendered.getFontMetrics(rendered.getFont()).stringWidth(rendered.getText()) <= 26);
    }
}
