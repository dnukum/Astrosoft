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

    @Test
    public void testManualModeToggle() {
        ModernBirthDataPanel panel = new ModernBirthDataPanel();
        assertNotNull(panel.getManualCoordCheck());
        assertFalse(panel.getManualCoordCheck().isSelected());
        assertFalse(panel.getLatDegField().isEditable());
        assertFalse(panel.getLonDegField().isEditable());
        assertFalse(panel.getLatDirCombo().isEnabled());
        assertFalse(panel.getLonDirCombo().isEnabled());

        panel.setManualCoordMode(true);
        assertTrue(panel.getManualCoordCheck().isSelected());
        assertTrue(panel.getLatDegField().isEditable());
        assertTrue(panel.getLonDegField().isEditable());
        assertTrue(panel.getLatDirCombo().isEnabled());
        assertTrue(panel.getLonDirCombo().isEnabled());
        assertTrue(panel.getTimeZoneCombo().isVisible());

        panel.setManualCoordMode(false);
        assertFalse(panel.getManualCoordCheck().isSelected());
        assertFalse(panel.getLatDegField().isEditable());
        assertFalse(panel.getLonDegField().isEditable());
        assertFalse(panel.getLatDirCombo().isEnabled());
        assertFalse(panel.getLonDirCombo().isEnabled());
        assertFalse(panel.getTimeZoneCombo().isVisible());
    }

    @Test
    public void testManualCoordinatesValidationRejection() {
        ModernBirthDataPanel panel = new ModernBirthDataPanel();
        panel.setPersonName("Validation Tester");
        panel.setManualCoordMode(true);

        // Blank latitude
        panel.getLatDegField().setText("");
        panel.getLonDegField().setText("80");
        assertNull("Blank latitude degree should fail validation", panel.getBirthData());

        // Non-numeric latitude
        panel.getLatDegField().setText("abc");
        assertNull("Non-numeric latitude should fail validation", panel.getBirthData());

        // Latitude > 90
        panel.getLatDegField().setText("95");
        panel.getLatMinField().setText("0");
        assertNull("Latitude > 90 should fail validation", panel.getBirthData());

        // Latitude = 90 with minutes > 0
        panel.getLatDegField().setText("90");
        panel.getLatMinField().setText("15");
        assertNull("Latitude 90° with >0 minutes should fail validation", panel.getBirthData());

        // Latitude minutes > 59
        panel.getLatDegField().setText("45");
        panel.getLatMinField().setText("65");
        assertNull("Latitude minutes > 59 should fail validation", panel.getBirthData());

        // Valid latitude, but invalid longitude
        panel.getLatDegField().setText("45");
        panel.getLatMinField().setText("30");

        // Blank longitude
        panel.getLonDegField().setText("");
        assertNull("Blank longitude degree should fail validation", panel.getBirthData());

        // Longitude > 180
        panel.getLonDegField().setText("185");
        panel.getLonMinField().setText("0");
        assertNull("Longitude > 180 should fail validation", panel.getBirthData());

        // Longitude = 180 with minutes > 0
        panel.getLonDegField().setText("180");
        panel.getLonMinField().setText("10");
        assertNull("Longitude 180° with >0 minutes should fail validation", panel.getBirthData());

        // Longitude minutes > 59
        panel.getLonDegField().setText("80");
        panel.getLonMinField().setText("60");
        assertNull("Longitude minutes > 59 should fail validation", panel.getBirthData());
    }

    @Test
    public void testManualCoordinatesSuccessfulCreation() {
        ModernBirthDataPanel panel = new ModernBirthDataPanel();
        panel.setPersonName("San Francisco Native");
        panel.setManualCoordMode(true);

        panel.getPlaceCombo().getEditor().setItem("San Francisco, California, USA");
        panel.getLatDegField().setText("37");
        panel.getLatMinField().setText("46");
        panel.getLatDirCombo().setSelectedItem(Place.Direction.NORTH);

        panel.getLonDegField().setText("122");
        panel.getLonMinField().setText("25");
        panel.getLonDirCombo().setSelectedItem(Place.Direction.WEST);

        app.astrosoft.util.AstrosoftTimeZone pacificTz = new app.astrosoft.util.AstrosoftTimeZone("America/Los_Angeles");
        panel.getTimeZoneCombo().setSelectedItem(pacificTz);

        BirthData bd = panel.getBirthData();
        assertNotNull("Valid manual coordinates must produce BirthData", bd);
        assertEquals("San Francisco Native", bd.name());

        Place p = bd.getBirthPlace();
        assertNotNull(p);
        assertEquals("San Francisco", p.city());
        assertEquals("California", p.state());
        assertEquals("USA", p.country());
        assertEquals(37, p.latitudeLocation().deg());
        assertEquals(46, p.latitudeLocation().min());
        assertEquals(Place.Direction.NORTH, p.latitudeLocation().dir());
        assertEquals(122, p.longitudeLocation().deg());
        assertEquals(25, p.longitudeLocation().min());
        assertEquals(Place.Direction.WEST, p.longitudeLocation().dir());
    }

    @Test
    public void testManualCoordinatesOptionalCityCountry() {
        ModernBirthDataPanel panel = new ModernBirthDataPanel();
        panel.setPersonName("Explorer");
        panel.setManualCoordMode(true);

        panel.getPlaceCombo().getEditor().setItem("");
        panel.getLatDegField().setText("13");
        panel.getLatMinField().setText("4");
        panel.getLatDirCombo().setSelectedItem(Place.Direction.NORTH);

        panel.getLonDegField().setText("80");
        panel.getLonMinField().setText("15");
        panel.getLonDirCombo().setSelectedItem(Place.Direction.EAST);

        BirthData bd = panel.getBirthData();
        assertNotNull("Empty city/country should default cleanly and pass", bd);
        Place p = bd.getBirthPlace();
        assertNotNull(p);
        assertEquals("Custom", p.city());
        assertEquals(13, p.latitudeLocation().deg());
        assertEquals(4, p.latitudeLocation().min());
    }
}
