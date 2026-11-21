package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testDatePickerYearMonthAndNavSyncSelectedDate() {
        DatePickerPanel panel = new DatePickerPanel(LocalDate.of(2026, 11, 4));
        List<JComboBox> combos = findComponents(panel, JComboBox.class);
        assertEquals(2, combos.size());
        JComboBox<Month> monthCombo = combos.get(0);
        JComboBox<Integer> yearCombo = combos.get(1);

        // 1. Changing year alone updates selectedDate and header
        yearCombo.setSelectedItem(1998);
        assertEquals(LocalDate.of(1998, 11, 4), panel.getSelectedDate());
        assertTrue(hasLabelText(panel, "1998"));

        // 2. Changing day first, then changing year preserves new day with new year
        JButton day15 = findButtonByText(panel, "15");
        assertNotNull(day15);
        day15.doClick();
        assertEquals(LocalDate.of(1998, 11, 15), panel.getSelectedDate());

        yearCombo.setSelectedItem(2005);
        assertEquals(LocalDate.of(2005, 11, 15), panel.getSelectedDate());
        assertTrue(hasLabelText(panel, "2005"));

        // 3. Changing month to shorter month clamps day-of-month (Jan 31 -> Feb 28)
        panel.setSelectedDate(LocalDate.of(2026, 1, 31));
        monthCombo.setSelectedItem(Month.FEBRUARY);
        assertEquals(LocalDate.of(2026, 2, 28), panel.getSelectedDate());

        // 4. Changing year from leap day clamps Feb 29 -> Feb 28
        panel.setSelectedDate(LocalDate.of(2024, 2, 29));
        yearCombo.setSelectedItem(1998);
        assertEquals(LocalDate.of(1998, 2, 28), panel.getSelectedDate());

        // 5. Prev / Next month arrows update selectedDate
        JButton nextBtn = findButtonByText(panel, ">");
        JButton prevBtn = findButtonByText(panel, "<");
        assertNotNull(nextBtn);
        assertNotNull(prevBtn);
        nextBtn.doClick();
        assertEquals(LocalDate.of(1998, 3, 28), panel.getSelectedDate());
        prevBtn.doClick();
        assertEquals(LocalDate.of(1998, 2, 28), panel.getSelectedDate());
    }

    private static <T extends Component> List<T> findComponents(Container root, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) {
                result.add(type.cast(c));
            }
            if (c instanceof Container) {
                result.addAll(findComponents((Container) c, type));
            }
        }
        return result;
    }

    private static JButton findButtonByText(Container root, String text) {
        for (JButton btn : findComponents(root, JButton.class)) {
            if (text.equals(btn.getText())) {
                return btn;
            }
        }
        return null;
    }

    private static boolean hasLabelText(Container root, String text) {
        for (JLabel label : findComponents(root, JLabel.class)) {
            if (text.equals(label.getText())) {
                return true;
            }
        }
        return false;
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
