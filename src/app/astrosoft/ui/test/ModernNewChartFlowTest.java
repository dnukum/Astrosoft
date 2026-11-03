package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.Test;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.Sex;
import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.comp.modern.ModernBirthDataPanel;

/**
 * End-to-end integration test for the New Chart creation flow.
 * Tests data entry, validation, and full horoscope computation headlessly.
 */
public class ModernNewChartFlowTest {

    @Test
    public void testNewChartCalculationFlow() {
        Place place = Place.getDefault();
        assertNotNull("Default place should exist", place);

        // 1. Create panel directly (headless-safe)
        ModernBirthDataPanel panel = new ModernBirthDataPanel();
        assertNotNull(panel);

        // 2. Populate birth details
        panel.setPersonName("Varahamihira");
        panel.setSelectedSex(Sex.Male);
        panel.setSelectedDate(LocalDate.of(1985, 7, 20));
        panel.setSelectedTime(LocalTime.of(10, 30, 15));
        panel.setSelectedPlace(place);

        // 3. Extract BirthData bean
        BirthData bd = panel.getBirthData();
        assertNotNull("BirthData must be created successfully", bd);
        assertEquals("Varahamihira", bd.name());
        assertEquals(Sex.Male, bd.sex());
        assertEquals(1985, bd.year());
        assertEquals(7, bd.month());
        assertEquals(20, bd.date());
        assertEquals(10, bd.hour());
        assertEquals(30, bd.minutes());
        assertEquals(15, bd.seconds());
        assertEquals(place, bd.getBirthPlace());

        // 4. Compute Horoscope directly from generated BirthData
        Horoscope h = new Horoscope(bd);
        assertNotNull(h);
        assertEquals("Varahamihira", h.getPersonName());
        assertNotNull("Ascendant must be computed", h.getAscendant());
        assertNotNull("Moon Rasi must be computed", h.getRasi());
        assertNotNull("Nakshathra must be computed", h.getNakshathra());
        assertNotNull("House position must be computed", h.getHousePosition());
        assertNotNull("PlanetaryInfo must be computed", h.getPlanetaryInfo());
    }

    @Test
    public void testNewChartValidationPreventsIncompleteCalculation() {
        ModernBirthDataPanel panel = new ModernBirthDataPanel();

        // Name is blank by default
        assertEquals("", panel.getPersonName());

        // Validation fails, getBirthData returns null
        assertNull("Blank name must prevent computation", panel.getBirthData());

        // Set valid name
        panel.setPersonName("Valid Astrologer");
        assertNotNull("Valid name allows computation", panel.getBirthData());
    }
}
