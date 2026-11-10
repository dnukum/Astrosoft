package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.GraphicsEnvironment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Sex;
import app.astrosoft.ui.comp.BirthDataPanel;
import app.astrosoft.ui.dlg.CompatibilityInputDialog;
import app.astrosoft.ui.dlg.MuhurtaConfigDialog;
import app.astrosoft.ui.dlg.TimeInputDialog;

/**
 * Headless-safe unit and regression tests for modernized date and time input dialogs:
 * 1. TimeInputDialog (Ephemeris / Panchang calculation time)
 * 2. MuhurtaConfigDialog (Transit / Muhurta date, time, and place)
 * 3. BirthDataPanel & CompatibilityInputDialog (Marriage Compatibility / Match)
 */
public class ModernInputDialogsTest {

    @Test
    public void testTimeInputDialogInitialStateAndConfirm() {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.NOVEMBER, 9, 10, 45, 30);
        cal.set(Calendar.MILLISECOND, 0);
        Date initialDate = cal.getTime();

        AtomicReference<Date> resultHolder = new AtomicReference<>();
        TimeInputDialog dlg = new TimeInputDialog(
                DisplayStrings.EPH_TIME_STR,
                null,
                initialDate,
                date -> resultHolder.set(date),
                false
        );

        assertNotNull(dlg.getTimePickerPanel());
        assertEquals(LocalTime.of(10, 45, 30), dlg.getSelectedTime());

        // Update selected time
        LocalTime newTime = LocalTime.of(16, 20, 0);
        dlg.setSelectedTime(newTime);
        assertEquals(newTime, dlg.getSelectedTime());

        Date updatedDate = dlg.getSelectedDate();
        assertNotNull(updatedDate);
        Calendar outCal = Calendar.getInstance();
        outCal.setTime(updatedDate);
        assertEquals(16, outCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, outCal.get(Calendar.MINUTE));
        assertEquals(0, outCal.get(Calendar.SECOND));

        dlg.dispose();
    }

    @Test
    public void testMuhurtaConfigDialogModernButtons() {
        Calendar dCal = Calendar.getInstance();
        dCal.set(2026, Calendar.OCTOBER, 25, 14, 30, 0);
        Date initInstant = dCal.getTime();
        Place place = Place.getDefault();

        AtomicReference<Date> resultDate = new AtomicReference<>();
        MuhurtaConfigDialog dlg = new MuhurtaConfigDialog(
                null,
                initInstant,
                initInstant,
                place,
                (date, time, p) -> resultDate.set(date),
                false
        );

        assertNotNull("Date button must exist", dlg.getDateButton());
        assertNotNull("Time button must exist", dlg.getTimeButton());
        assertNotNull("Place chooser must exist", dlg.getPlaceChooser());

        // Verify button text displays formatted date and time
        String dateText = dlg.getDateButton().getText();
        String timeText = dlg.getTimeButton().getText();
        assertTrue("Date text should contain month/year: " + dateText, dateText.contains("2026"));
        assertTrue("Time text should contain AM or PM: " + timeText, timeText.contains("AM") || timeText.contains("PM"));

        // Programmatic date/time update without typing
        LocalDate newDate = LocalDate.of(2027, 1, 15);
        LocalTime newTime = LocalTime.of(9, 15, 0);
        dlg.setSelectedDate(newDate);
        dlg.setSelectedTime(newTime);

        assertEquals(newDate, dlg.getSelectedDate());
        assertEquals(newTime, dlg.getSelectedTime());
        assertTrue("Updated date text must contain 2027: " + dlg.getDateButton().getText(), dlg.getDateButton().getText().contains("2027"));
        assertTrue("Updated time text must contain AM: " + dlg.getTimeButton().getText(), dlg.getTimeButton().getText().contains("AM"));

        dlg.dispose();
    }

    @Test
    public void testBirthDataPanelVisualPickers() {
        BirthDataPanel panel = new BirthDataPanel(new java.awt.Dimension(360, 320), "Boy Data");

        assertNotNull("Date button must exist", panel.getDateButton());
        assertNotNull("Time button must exist", panel.getTimeButton());

        Place place = Place.getDefault();
        BirthData input = new BirthData(
                "Srinivasa",
                Sex.Male,
                22,
                12,
                1887,
                18,
                45,
                0,
                place
        );
        input.setNotes("Kumbakonam prodigy");

        panel.setBirthData(input);

        assertEquals("Srinivasa", panel.getPersonName());
        assertEquals(LocalDate.of(1887, 12, 22), panel.getSelectedDate());
        assertEquals(LocalTime.of(18, 45, 0), panel.getSelectedTime());

        String dateText = panel.getDateButton().getText();
        String timeText = panel.getTimeButton().getText();
        assertTrue("Date text should display Dec 22, 1887: " + dateText, dateText.contains("1887"));
        assertTrue("Time text should display 06:45 PM: " + timeText, timeText.contains("06") && timeText.contains("45"));

        // Verify round-trip getBirthData()
        BirthData retrieved = panel.getBirthData();
        assertNotNull(retrieved);
        assertEquals("Srinivasa", retrieved.name());
        assertEquals("Kumbakonam prodigy", retrieved.getNotes());
        assertEquals(1887, retrieved.year());
        assertEquals(12, retrieved.month());
        assertEquals(22, retrieved.date());
        assertEquals(18, retrieved.hour());
        assertEquals(45, retrieved.minutes());
    }

    @Test
    public void testCompatibilityInputDialogModernPanels() {
        CompatibilityInputDialog dlg = new CompatibilityInputDialog(null, false, false);

        assertNotNull("Dialog panel should not be null", dlg.getContentPane());
        assertNotNull("Boy data panel must exist", dlg.getBoyDataPanel());
        assertNotNull("Girl data panel must exist", dlg.getGirlDataPanel());

        // Verify boy and girl panels use modern picker buttons
        assertNotNull("Boy date button must exist", dlg.getBoyDataPanel().getDateButton());
        assertNotNull("Boy time button must exist", dlg.getBoyDataPanel().getTimeButton());
        assertNotNull("Girl date button must exist", dlg.getGirlDataPanel().getDateButton());
        assertNotNull("Girl time button must exist", dlg.getGirlDataPanel().getTimeButton());

        // Verify data setting
        Place place = Place.getDefault();
        BirthData boyBd = new BirthData("Groom", Sex.Male, 10, 5, 1992, 10, 15, 0, place);
        BirthData girlBd = new BirthData("Bride", Sex.Female, 14, 8, 1994, 15, 30, 0, place);

        dlg.getBoyDataPanel().setBirthData(boyBd);
        dlg.getGirlDataPanel().setBirthData(girlBd);

        assertEquals(LocalDate.of(1992, 5, 10), dlg.getBoyDataPanel().getSelectedDate());
        assertEquals(LocalDate.of(1994, 8, 14), dlg.getGirlDataPanel().getSelectedDate());

        assertEquals("Groom", dlg.getBoyDataPanel().getBirthData().name());
        assertEquals("Bride", dlg.getGirlDataPanel().getBirthData().name());

        dlg.dispose();
    }
}
