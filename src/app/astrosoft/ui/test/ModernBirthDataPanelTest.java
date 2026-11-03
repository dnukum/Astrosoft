package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.Test;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.Sex;
import app.astrosoft.ui.comp.modern.ModernBirthDataPanel;

/**
 * Headless-safe unit and integration tests for ModernBirthDataPanel.
 */
public class ModernBirthDataPanelTest {

    @Test
    public void testInitialDefaultValues() {
        ModernBirthDataPanel panel = new ModernBirthDataPanel();

        assertNotNull(panel);
        assertNotNull(panel.getSelectedDate());
        assertNotNull(panel.getSelectedTime());
        assertNotNull(panel.getSelectedPlace());
        assertEquals(Sex.Male, panel.getSelectedSex());
        assertEquals("", panel.getPersonName());
    }

    @Test
    public void testSetAndGetBirthDataRoundTrip() {
        ModernBirthDataPanel panel = new ModernBirthDataPanel();

        Place place = Place.getDefault();
        BirthData input = new BirthData(
                "Ramanujan",
                Sex.Male,
                22,
                12,
                1887,
                18,
                30,
                45,
                place
        );
        input.setNotes("Mathematical genius");

        panel.setBirthData(input);

        assertEquals("Ramanujan", panel.getPersonName());
        assertEquals(Sex.Male, panel.getSelectedSex());
        assertEquals(LocalDate.of(1887, 12, 22), panel.getSelectedDate());
        assertEquals(LocalTime.of(18, 30, 45), panel.getSelectedTime());
        assertEquals(place, panel.getSelectedPlace());

        BirthData output = panel.getBirthData();
        assertNotNull(output);
        assertEquals("Ramanujan", output.name());
        assertEquals(Sex.Male, output.sex());
        assertEquals(1887, output.year());
        assertEquals(12, output.month());
        assertEquals(22, output.date());
        assertEquals(18, output.hour());
        assertEquals(30, output.minutes());
        assertEquals(45, output.seconds());
        assertEquals(place, output.getBirthPlace());
        assertEquals("Mathematical genius", output.getNotes());
    }

    @Test
    public void testFemaleGenderSelection() {
        ModernBirthDataPanel panel = new ModernBirthDataPanel();
        panel.setSelectedSex(Sex.Female);
        assertEquals(Sex.Female, panel.getSelectedSex());

        Place place = Place.getDefault();
        BirthData input = new BirthData("Hypatia", Sex.Female, 15, 3, 1990, 9, 15, 0, place);
        panel.setBirthData(input);
        assertEquals(Sex.Female, panel.getSelectedSex());

        BirthData output = panel.getBirthData();
        assertNotNull(output);
        assertEquals(Sex.Female, output.sex());
    }

    @Test
    public void testValidationPreventsBlankName() {
        ModernBirthDataPanel panel = new ModernBirthDataPanel();
        panel.setPersonName("");

        // getBirthData() must return null when name is blank
        assertNull("Blank name should fail validation and return null BirthData", panel.getBirthData());

        panel.setPersonName("   ");
        assertNull("Whitespace-only name should fail validation", panel.getBirthData());

        panel.setPersonName("Valid Name");
        assertNotNull("Non-blank name should pass validation", panel.getBirthData());
    }

    @Test
    public void testAtomicStateUpdates() {
        ModernBirthDataPanel panel = new ModernBirthDataPanel();
        panel.setPersonName("Test User");

        LocalDate newDate = LocalDate.of(2000, 1, 1);
        panel.setSelectedDate(newDate);
        assertEquals(newDate, panel.getSelectedDate());

        LocalTime newTime = LocalTime.of(12, 0, 0);
        panel.setSelectedTime(newTime);
        assertEquals(newTime, panel.getSelectedTime());

        BirthData bd = panel.getBirthData();
        assertNotNull(bd);
        assertEquals(2000, bd.year());
        assertEquals(1, bd.month());
        assertEquals(1, bd.date());
        assertEquals(12, bd.hour());
        assertEquals(0, bd.minutes());
        assertEquals(0, bd.seconds());
    }

    @Test
    public void testModernBirthInputDialogConstruction() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }

        Place place = Place.getDefault();
        BirthData input = new BirthData("Aryabhata", Sex.Male, 1, 1, 1980, 5, 30, 0, place);
        app.astrosoft.ui.dlg.ModernBirthInputDialog dlg = new app.astrosoft.ui.dlg.ModernBirthInputDialog(null, input, false);

        assertNotNull(dlg);
        assertNotNull(dlg.getBirthDataPanel());
        assertNotNull(dlg.getCalculateButton());
        assertNotNull(dlg.getCancelButton());
        assertEquals("Aryabhata", dlg.getBirthDataPanel().getPersonName());

        dlg.closeDialog();
    }
}
