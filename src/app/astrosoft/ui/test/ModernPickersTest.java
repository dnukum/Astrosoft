package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.GraphicsEnvironment;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.Test;

import app.astrosoft.beans.Place;
import app.astrosoft.ui.comp.modern.DatePickerDialog;
import app.astrosoft.ui.comp.modern.DatePickerPanel;
import app.astrosoft.ui.comp.modern.PlaceSearchDialog;
import app.astrosoft.ui.comp.modern.PlaceSearchPanel;
import app.astrosoft.ui.comp.modern.TimePickerDialog;
import app.astrosoft.ui.comp.modern.TimePickerPanel;

/**
 * Headless-safe unit and logic tests for Phase 1 Modern Pickers.
 */
public class ModernPickersTest {

    @Test
    public void testDatePickerPanelStateAndNavigation() {
        LocalDate initial = LocalDate.of(2026, 11, 2);
        DatePickerPanel panel = new DatePickerPanel(initial);

        assertNotNull(panel);
        assertEquals(initial, panel.getSelectedDate());
        assertFalse(panel.isConfirmed());

        // Test leap year date
        LocalDate leapDate = LocalDate.of(2024, 2, 29);
        panel.setSelectedDate(leapDate);
        assertEquals(leapDate, panel.getSelectedDate());

        // Test boundary year 1800
        LocalDate minBoundary = LocalDate.of(DatePickerPanel.MIN_YEAR, 1, 1);
        panel.setSelectedDate(minBoundary);
        assertEquals(minBoundary, panel.getSelectedDate());

        // Test boundary year 2100
        LocalDate maxBoundary = LocalDate.of(DatePickerPanel.MAX_YEAR, 12, 31);
        panel.setSelectedDate(maxBoundary);
        assertEquals(maxBoundary, panel.getSelectedDate());

        // Confirmation flag
        panel.setConfirmed(true);
        assertTrue(panel.isConfirmed());
    }

    @Test
    public void testTimePickerPanel12And24HourConversion() {
        // Morning time (08:06:14 AM)
        LocalTime morning = LocalTime.of(8, 6, 14);
        TimePickerPanel morningPanel = new TimePickerPanel(morning);
        assertNotNull(morningPanel);
        assertEquals(morning, morningPanel.getSelectedTime());
        assertFalse(morningPanel.isConfirmed());

        // Evening time (08:06:14 PM = 20:06:14)
        LocalTime evening = LocalTime.of(20, 6, 14);
        TimePickerPanel eveningPanel = new TimePickerPanel(evening);
        assertEquals(evening, eveningPanel.getSelectedTime());

        // Midnight edge case (00:00:00 -> 12:00:00 AM)
        LocalTime midnight = LocalTime.of(0, 0, 0);
        TimePickerPanel midnightPanel = new TimePickerPanel(midnight);
        assertEquals(midnight, midnightPanel.getSelectedTime());

        // Noon edge case (12:00:00 -> 12:00:00 PM)
        LocalTime noon = LocalTime.of(12, 0, 0);
        TimePickerPanel noonPanel = new TimePickerPanel(noon);
        assertEquals(noon, noonPanel.getSelectedTime());

        // Last second of day (23:59:59)
        LocalTime lastSecond = LocalTime.of(23, 59, 59);
        TimePickerPanel lastSecondPanel = new TimePickerPanel(lastSecond);
        assertEquals(lastSecond, lastSecondPanel.getSelectedTime());
    }

    @Test
    public void testPlaceSearchPanelInitialization() {
        Place defaultPlace = Place.getDefault();
        assertNotNull("Default place should exist", defaultPlace);

        PlaceSearchPanel panel = new PlaceSearchPanel(defaultPlace);
        assertNotNull(panel);
        assertEquals(defaultPlace, panel.getSelectedPlace());
        assertFalse(panel.isConfirmed());

        panel.setConfirmed(true);
        assertTrue(panel.isConfirmed());
    }

    @Test
    public void testDialogWrappingWhenNotHeadless() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // Skip dialog heavyweight test in pure headless mode
        }

        LocalDate date = LocalDate.of(2026, 11, 2);
        DatePickerDialog dateDlg = new DatePickerDialog(null, date);
        assertEquals(date, dateDlg.getSelectedDate());
        dateDlg.dispose();

        LocalTime time = LocalTime.of(8, 6, 14);
        TimePickerDialog timeDlg = new TimePickerDialog(null, time);
        assertEquals(time, timeDlg.getSelectedTime());
        timeDlg.dispose();

        Place place = Place.getDefault();
        PlaceSearchDialog placeDlg = new PlaceSearchDialog(null, place);
        assertEquals(place, placeDlg.getSelectedPlace());
        placeDlg.dispose();
    }
}
