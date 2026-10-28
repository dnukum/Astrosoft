package app.astrosoft.test;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.MuhurtaProfile;
import app.astrosoft.beans.NotesOwner;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Sex;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.MuhurtaDocument;

public class MuhurtaDocumentXmlTest {

    @Test
    public void testMuhurtaDocumentSaveAndLoadRoundTrip() throws Exception {
        Place place = new Place("Ramachandrapuram", "Andhra Pradesh", "India", 16.85, 82.02, 5.5);
        Calendar cal = Calendar.getInstance(place.astrosoftTimeZone().getTimeZone());
        cal.set(2026, Calendar.OCTOBER, 27, 11, 17, 49);
        cal.set(Calendar.MILLISECOND, 0);
        Date eventDate = cal.getTime();
        Date eventTime = eventDate;

        // Person 1: Full Horoscope
        Place p1Place = new Place("Chennai", "Tamil Nadu", "India", 13.08, 80.27, 5.5);
        BirthData p1Bd = new BirthData("Husband-Horoscope", Sex.Male, 15, 8, 1985, 10, 30, 0, p1Place);
        Horoscope p1H = new Horoscope(p1Bd);
        MuhurtaProfile p1 = new MuhurtaProfile(p1H);

        // Person 2: Quick Entry
        MuhurtaProfile p2 = new MuhurtaProfile("Wife-QuickEntry", Rasi.Mesha, Nakshathra.Ashwini);

        // Notes
        String htmlNotes = "<html><body><p>Recommended Subha Hora window: 08:51 AM - 09:49 AM.</p><p>Abhijit Muhurta verified.</p></body></html>";

        // Create Document
        MuhurtaDocument doc = new MuhurtaDocument("House Warming", eventDate, eventTime, place, p1, p2);
        doc.setNotes(htmlNotes);

        // Save to temp file
        File tempFile = File.createTempFile("test_muhurta_", ".asm");
        tempFile.deleteOnExit();
        doc.saveToFile(tempFile.getAbsolutePath());

        Assert.assertTrue("Saved file must exist", tempFile.exists());
        Assert.assertTrue("Saved file size must be > 0", tempFile.length() > 0);

        // Load back
        MuhurtaDocument loaded = MuhurtaDocument.createFromFile(tempFile.getAbsolutePath());
        Assert.assertNotNull("Loaded MuhurtaDocument should not be null", loaded);

        // Verify Event
        Assert.assertEquals("House Warming", loaded.getName());
        Assert.assertNotNull("Place should be loaded", loaded.getPlace());
        Assert.assertEquals("Ramachandrapuram", loaded.getPlace().city());
        Assert.assertEquals(5.5, loaded.getPlace().timeZone(), 0.001);

        Calendar loadedCal = Calendar.getInstance(loaded.getPlace().astrosoftTimeZone().getTimeZone());
        loadedCal.setTime(loaded.getDate());
        Assert.assertEquals(2026, loadedCal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.OCTOBER, loadedCal.get(Calendar.MONTH));
        Assert.assertEquals(27, loadedCal.get(Calendar.DATE));

        Calendar loadedTimeCal = Calendar.getInstance(loaded.getPlace().astrosoftTimeZone().getTimeZone());
        loadedTimeCal.setTime(loaded.getTime());
        Assert.assertEquals(11, loadedTimeCal.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(17, loadedTimeCal.get(Calendar.MINUTE));

        // Verify Person 1 (Horoscope)
        Assert.assertNotNull("Person 1 should be loaded", loaded.getPerson1());
        Assert.assertTrue("Person 1 should have full Horoscope", loaded.getPerson1().hasHoroscope());
        Assert.assertEquals("Husband-Horoscope", loaded.getPerson1().getName());
        Assert.assertNotNull("Person 1 horoscope rasi chart should compute", loaded.getPerson1().getHoroscope().getRasi());

        // Verify Person 2 (Quick Entry)
        Assert.assertNotNull("Person 2 should be loaded", loaded.getPerson2());
        Assert.assertFalse("Person 2 should be quick entry without horoscope", loaded.getPerson2().hasHoroscope());
        Assert.assertEquals("Wife-QuickEntry", loaded.getPerson2().getName());
        Assert.assertEquals(Rasi.Mesha, loaded.getPerson2().getRasi());
        Assert.assertEquals(Nakshathra.Ashwini, loaded.getPerson2().getNakshathra());

        // Verify Notes
        Assert.assertEquals(htmlNotes, loaded.getNotes());

        // Verify NotesOwner contract
        NotesOwner notesOwner = loaded;
        Assert.assertTrue(notesOwner.getNotesOwnerTitle().contains("House Warming"));
        Assert.assertEquals(tempFile.getAbsolutePath(), notesOwner.getFilePath());
        Assert.assertFalse("Loaded document should initially not be marked modified", notesOwner.isModified());

        notesOwner.setModified(true);
        Assert.assertTrue(notesOwner.isModified());
    }

    @Test
    public void testHoroscopeImplementsNotesOwner() {
        Place place = new Place("Delhi", 28.6, 77.2, 5.5);
        BirthData bd = new BirthData("TestPerson", Sex.Female, 1, 1, 1990, 12, 0, 0, place);
        Horoscope h = new Horoscope(bd);

        Assert.assertTrue("Horoscope must implement NotesOwner", h instanceof NotesOwner);
        NotesOwner owner = h;
        Assert.assertEquals("TestPerson", owner.getNotesOwnerTitle());
        Assert.assertNull(owner.getNotes());

        owner.setNotes("Natal consultation notes");
        Assert.assertEquals("Natal consultation notes", owner.getNotes());
        Assert.assertEquals("Natal consultation notes", h.getNotes());
    }

    @Test
    public void testModificationTracking() throws Exception {
        Place place = new Place("Delhi", 28.6, 77.2, 5.5);
        BirthData bd = new BirthData("Priya", Sex.Female, 1, 1, 1995, 10, 0, 0, place);
        Horoscope h = new Horoscope(bd);
        h.setModified(false);

        Assert.assertFalse(h.isModified());
        h.setModified(true);
        Assert.assertTrue(h.isModified());

        File tempFile = File.createTempFile("test_h_mod_", ".ash");
        tempFile.deleteOnExit();
        h.saveToFile(tempFile.getAbsolutePath());
        Assert.assertFalse("Saving horoscope resets modified to false", h.isModified());

        // MuhurtaDocument
        MuhurtaDocument doc = new MuhurtaDocument("Event", new Date(), new Date(), place, null, null);
        doc.setModified(false);
        Assert.assertFalse(doc.isModified());

        doc.setModified(true);
        Assert.assertTrue(doc.isModified());

        File tempMFile = File.createTempFile("test_m_mod_", ".asm");
        tempMFile.deleteOnExit();
        doc.saveToFile(tempMFile.getAbsolutePath());
        Assert.assertFalse("Saving MuhurtaDocument resets modified to false", doc.isModified());
    }
}
