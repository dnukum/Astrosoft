package app.astrosoft.test;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.Command;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Sex;
import app.astrosoft.core.Compatibility;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.MuhurtaDocument;
import app.astrosoft.core.Muhurtha;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.view.ViewManager.View;

public class DocumentDirtyAndPromptIntegrationTest {

    private AstroSoft app;
    private File fileA;
    private File fileB;
    private File fileMuhurta;
    private File fileMuhurta2;
    private File fileComp;

    @Before
    public void setUp() throws Exception {
        Place place = new Place("Chennai", 13.08, 80.27, 5.5);

        // File A
        BirthData bdA = new BirthData("PersonA", Sex.Male, 10, 5, 1985, 10, 0, 0, place);
        bdA.setNotes("Original Notes A");
        Horoscope hA = new Horoscope(bdA);
        fileA = File.createTempFile("test_personA_", ".ash");
        fileA.deleteOnExit();
        hA.saveToFile(fileA.getAbsolutePath());

        // File B
        BirthData bdB = new BirthData("PersonB", Sex.Female, 20, 8, 1990, 14, 30, 0, place);
        bdB.setNotes("Original Notes B");
        Horoscope hB = new Horoscope(bdB);
        fileB = File.createTempFile("test_personB_", ".ash");
        fileB.deleteOnExit();
        hB.saveToFile(fileB.getAbsolutePath());

        // File Muhurta
        MuhurtaDocument mDoc = new MuhurtaDocument("HouseWarming", new Date(), new Date(), place, null, null);
        mDoc.setNotes("Original Muhurta Notes");
        fileMuhurta = File.createTempFile("test_muhurta_", ".asm");
        fileMuhurta.deleteOnExit();
        mDoc.saveToFile(fileMuhurta.getAbsolutePath());

        // File Muhurta 2
        MuhurtaDocument mDoc2 = new MuhurtaDocument("TravelMuhurta", new Date(), new Date(), place, null, null);
        mDoc2.setNotes("Travel Notes");
        fileMuhurta2 = File.createTempFile("test_muhurta2_", ".asm");
        fileMuhurta2.deleteOnExit();
        mDoc2.saveToFile(fileMuhurta2.getAbsolutePath());

        // File Compatibility (.asc)
        Compatibility comp = new Compatibility(hA, hB);
        fileComp = File.createTempFile("test_comp_", ".asc");
        fileComp.deleteOnExit();
        comp.saveToXML(fileComp.getAbsolutePath());

        app = new AstroSoft();
        // Default headless delegate to prevent UI modal blocking during automated tests
        AstroSoft.setSavePromptDelegate((title, parent) -> 1);
    }

    @After
    public void tearDown() {
        AstroSoft.setSavePromptDelegate(null);
        AstroSoft.setExitHandler(null);
    }

    @Test
    public void testHoroscopeModifyNotesAndSaveOnOpenAnother() {
        app.openHoroscope(fileA.getAbsolutePath());
        Horoscope currentH = app.getHoroscope();
        Assert.assertNotNull(currentH);
        Assert.assertEquals("PersonA", currentH.getPersonName());
        Assert.assertFalse(currentH.isModified());

        // User edits notes in ChartNotesPanel editor
        app.getChartNotesPanel().getEditor().setText("<p>Updated Notes for Person A</p>");
        Assert.assertTrue(currentH.isModified());

        // User opens PersonB and chooses SAVE (choice 0)
        final AtomicBoolean promptCalled = new AtomicBoolean(false);
        AstroSoft.setSavePromptDelegate((title, parent) -> {
            promptCalled.set(true);
            Assert.assertEquals("PersonA", title);
            return 0; // Save
        });

        app.openHoroscope(fileB.getAbsolutePath());

        Assert.assertTrue("Save prompt must be displayed", promptCalled.get());

        // Verify PersonA on disk was saved with updated notes
        Horoscope reloadedA = Horoscope.createFromFile(fileA.getAbsolutePath());
        Assert.assertTrue(reloadedA.getNotes().contains("Updated Notes for Person A"));

        // Verify PersonB is now active
        Horoscope activeH = app.getHoroscope();
        Assert.assertNotNull(activeH);
        Assert.assertEquals("PersonB", activeH.getPersonName());
        Assert.assertTrue(activeH.getNotes().contains("Original Notes B"));
        Assert.assertFalse(activeH.isModified());
    }

    @Test
    public void testHoroscopeModifyNotesAndDontSaveOnOpenAnother() {
        app.openHoroscope(fileA.getAbsolutePath());
        Horoscope currentH = app.getHoroscope();
        app.getChartNotesPanel().getEditor().setText("<p>Edits to be discarded</p>");
        Assert.assertTrue(currentH.isModified());

        // User opens PersonB and chooses DON'T SAVE (choice 1)
        final AtomicBoolean promptCalled = new AtomicBoolean(false);
        AstroSoft.setSavePromptDelegate((title, parent) -> {
            promptCalled.set(true);
            return 1; // Don't Save
        });

        app.openHoroscope(fileB.getAbsolutePath());

        Assert.assertTrue("Save prompt must be displayed", promptCalled.get());

        // Verify PersonA on disk was NOT updated
        Horoscope reloadedA = Horoscope.createFromFile(fileA.getAbsolutePath());
        Assert.assertTrue(reloadedA.getNotes().contains("Original Notes A"));

        // Verify PersonB is loaded
        Assert.assertEquals("PersonB", app.getHoroscope().getPersonName());
    }

    @Test
    public void testHoroscopeModifyNotesAndCancelOnOpenAnother() {
        app.openHoroscope(fileA.getAbsolutePath());
        Horoscope currentH = app.getHoroscope();
        app.getChartNotesPanel().getEditor().setText("<p>Pending unsaved thoughts</p>");
        Assert.assertTrue(currentH.isModified());

        // User opens PersonB but clicks CANCEL (choice 2)
        final AtomicBoolean promptCalled = new AtomicBoolean(false);
        AstroSoft.setSavePromptDelegate((title, parent) -> {
            promptCalled.set(true);
            return 2; // Cancel
        });

        app.openHoroscope(fileB.getAbsolutePath());

        Assert.assertTrue("Save prompt must be displayed", promptCalled.get());

        // Verify open was ABORTED: PersonA is STILL active with unsaved notes
        Assert.assertEquals("PersonA", app.getHoroscope().getPersonName());
        Assert.assertTrue(app.getHoroscope().getNotes().contains("Pending unsaved thoughts"));
        Assert.assertTrue(app.getHoroscope().isModified());

        // Verify disk still has original
        Horoscope reloadedA = Horoscope.createFromFile(fileA.getAbsolutePath());
        Assert.assertTrue(reloadedA.getNotes().contains("Original Notes A"));
    }

    @Test
    public void testHoroscopeModifyNotesAndOpenMuhurtaProfile() {
        app.openHoroscope(fileA.getAbsolutePath());
        Horoscope currentH = app.getHoroscope();
        app.getChartNotesPanel().getEditor().setText("<p>Important consultation notes</p>");
        Assert.assertTrue(currentH.isModified());

        // User opens Muhurta profile and chooses SAVE (choice 0)
        final AtomicBoolean promptCalled = new AtomicBoolean(false);
        final AtomicBoolean exitCalled = new AtomicBoolean(false);

        AstroSoft.setSavePromptDelegate((title, parent) -> {
            promptCalled.set(true);
            Assert.assertEquals("PersonA", title);
            return 0; // Save
        });

        AstroSoft.setExitHandler(status -> {
            exitCalled.set(true);
        });

        // Trigger opening Muhurta document
        app.openDocument(fileMuhurta.getAbsolutePath());

        Assert.assertTrue("Prompt must appear when switching to Muhurta with modified chart", promptCalled.get());
        Assert.assertFalse("Application MUST NOT EXIT when opening Muhurta document", exitCalled.get());

        // Verify PersonA was saved to disk
        Horoscope reloadedA = Horoscope.createFromFile(fileA.getAbsolutePath());
        Assert.assertTrue(reloadedA.getNotes().contains("Important consultation notes"));

        // Verify MyMuhurta view loaded the document
        Assert.assertNotNull(app.getMyMuhurtaView());
        MuhurtaDocument doc = app.getMyMuhurtaView().getDocument();
        Assert.assertNotNull(doc);
        Assert.assertEquals("HouseWarming", doc.getName());
    }

    @Test
    public void testCanExitGuardedByDirtyCheck() {
        app.openHoroscope(fileA.getAbsolutePath());
        app.getChartNotesPanel().getEditor().setText("<p>Unsaved exit test</p>");
        Assert.assertTrue(app.getHoroscope().isModified());

        final AtomicInteger promptResponses = new AtomicInteger(2); // Start with Cancel
        AstroSoft.setSavePromptDelegate((title, parent) -> promptResponses.get());

        // Cancel exit
        boolean canExit = app.canExit();
        Assert.assertFalse("canExit must be false when user cancels", canExit);

        // Don't save
        promptResponses.set(1);
        canExit = app.canExit();
        Assert.assertTrue("canExit must be true when user chooses Don't Save", canExit);
        Assert.assertFalse(app.getHoroscope().isModified());

        // Save
        app.getChartNotesPanel().getEditor().setText("<p>Saved before exit</p>");
        promptResponses.set(0);
        canExit = app.canExit();
        Assert.assertTrue("canExit must be true when user saves", canExit);
        Horoscope reloaded = Horoscope.createFromFile(fileA.getAbsolutePath());
        Assert.assertTrue(reloaded.getNotes().contains("Saved before exit"));
    }

    @Test
    public void testMuhurtaModifyNotesAndSaveOnOpenAnother() {
        app.openDocument(fileMuhurta.getAbsolutePath());
        Assert.assertNotNull(app.getMyMuhurtaView());
        MuhurtaDocument doc = app.getMyMuhurtaView().getDocument();
        Assert.assertEquals("HouseWarming", doc.getName());
        Assert.assertFalse(doc.isModified());

        // Edit Muhurta notes
        app.getChartNotesPanel().getEditor().setText("<p>Updated HouseWarming Notes</p>");
        Assert.assertTrue(doc.isModified());

        // Open TravelMuhurta and choose SAVE (0)
        final AtomicBoolean promptCalled = new AtomicBoolean(false);
        AstroSoft.setSavePromptDelegate((title, parent) -> {
            promptCalled.set(true);
            return 0; // Save
        });

        app.openDocument(fileMuhurta2.getAbsolutePath());

        Assert.assertTrue("Prompt must appear for Muhurta document", promptCalled.get());

        // Verify fileMuhurta on disk has updated notes
        MuhurtaDocument reloaded = MuhurtaDocument.createFromFile(fileMuhurta.getAbsolutePath());
        Assert.assertTrue(reloaded.getNotes().contains("Updated HouseWarming Notes"));

        // Verify TravelMuhurta is loaded
        Assert.assertEquals("TravelMuhurta", app.getMyMuhurtaView().getDocument().getName());
    }

    @Test
    public void testMuhurtaModifyNotesAndCancelOnOpenAnother() {
        app.openDocument(fileMuhurta.getAbsolutePath());
        MuhurtaDocument doc = app.getMyMuhurtaView().getDocument();
        app.getChartNotesPanel().getEditor().setText("<p>Pending Muhurta thoughts</p>");
        Assert.assertTrue(doc.isModified());

        // Open TravelMuhurta and choose CANCEL (2)
        final AtomicBoolean promptCalled = new AtomicBoolean(false);
        AstroSoft.setSavePromptDelegate((title, parent) -> {
            promptCalled.set(true);
            return 2; // Cancel
        });

        app.openDocument(fileMuhurta2.getAbsolutePath());

        Assert.assertTrue("Prompt must appear for Muhurta document", promptCalled.get());

        // Verify HouseWarming is STILL active
        Assert.assertEquals("HouseWarming", app.getMyMuhurtaView().getDocument().getName());
        Assert.assertTrue(doc.isModified());
    }

    @Test
    public void testNewHoroscopeGuardedByDirtyCheck() {
        app.openHoroscope(fileA.getAbsolutePath());
        app.getChartNotesPanel().getEditor().setText("<p>Notes before new</p>");
        Assert.assertTrue(app.getHoroscope().isModified());

        // Setup delegate to Cancel (2)
        AstroSoft.setSavePromptDelegate((title, parent) -> 2);

        // newHoroscope() checks promptSaveIfModified(h)
        app.newHoroscope();

        // Still PersonA
        Assert.assertEquals("PersonA", app.getHoroscope().getPersonName());
        Assert.assertTrue(app.getHoroscope().isModified());
    }

    @Test
    public void testCleanDocumentNeverPromptsOnOpen() {
        app.openHoroscope(fileA.getAbsolutePath());
        Assert.assertFalse(app.getHoroscope().isModified());

        // Delegate throws if prompted!
        AstroSoft.setSavePromptDelegate((title, parent) -> {
            Assert.fail("Clean document must NEVER trigger save confirmation prompt!");
            return 2;
        });

        // Open PersonB directly
        app.openHoroscope(fileB.getAbsolutePath());
        Assert.assertEquals("PersonB", app.getHoroscope().getPersonName());
    }

    @Test
    public void testMuhurtaModifyNotesAndSaveOnOpenHoroscope() {
        app.openDocument(fileMuhurta.getAbsolutePath());
        Assert.assertNotNull(app.getMyMuhurtaView());
        MuhurtaDocument doc = app.getMyMuhurtaView().getDocument();
        app.getChartNotesPanel().getEditor().setText("<p>Muhurta notes before switching to horoscope</p>");
        Assert.assertTrue(doc.isModified());

        final AtomicBoolean promptCalled = new AtomicBoolean(false);
        AstroSoft.setSavePromptDelegate((title, parent) -> {
            promptCalled.set(true);
            return 0; // Save
        });

        // Open PersonA horoscope while on My Muhurta with unsaved edits
        app.openDocument(fileA.getAbsolutePath());

        Assert.assertTrue("Save prompt must appear for Muhurta document", promptCalled.get());

        // Verify Muhurta was saved to disk
        MuhurtaDocument reloaded = MuhurtaDocument.createFromFile(fileMuhurta.getAbsolutePath());
        Assert.assertTrue(reloaded.getNotes().contains("Muhurta notes before switching to horoscope"));

        // Verify Horoscope view is now active and PersonA loaded
        Assert.assertEquals("PersonA", app.getHoroscope().getPersonName());
    }

    @Test
    public void testMuhurtaModifyNotesAndCancelOnOpenHoroscope() {
        app.openDocument(fileMuhurta.getAbsolutePath());
        MuhurtaDocument doc = app.getMyMuhurtaView().getDocument();
        app.getChartNotesPanel().getEditor().setText("<p>Unsaved Muhurta thoughts</p>");
        Assert.assertTrue(doc.isModified());

        final AtomicBoolean promptCalled = new AtomicBoolean(false);
        AstroSoft.setSavePromptDelegate((title, parent) -> {
            promptCalled.set(true);
            return 2; // Cancel
        });

        // Attempt to open PersonA horoscope
        app.openDocument(fileA.getAbsolutePath());

        Assert.assertTrue("Save prompt must appear for Muhurta document", promptCalled.get());

        // Verify open aborted: still on Muhurta with unsaved thoughts
        Assert.assertEquals("HouseWarming", app.getMyMuhurtaView().getDocument().getName());
        Assert.assertTrue(doc.isModified());
    }

    @Test
    public void testBrandNewUnsavedHoroscopeDirtyAndSave() throws Exception {
        Place place = new Place("Delhi", 28.6, 77.2, 5.5);
        BirthData newBd = new BirthData("BrandNewPerson", Sex.Male, 1, 1, 2000, 12, 0, 0, place);

        // Compute brand new chart
        app.computeHoroscope(newBd, false);
        Horoscope h = app.getHoroscope();
        Assert.assertNotNull(h);
        Assert.assertEquals("BrandNewPerson", h.getPersonName());
        Assert.assertNull(h.getFilePath());
        Assert.assertTrue("Brand new horoscope must be marked modified", h.isModified());

        // Save it to a file
        File newFile = File.createTempFile("test_brand_new_", ".ash");
        newFile.deleteOnExit();
        h.saveToFile(newFile.getAbsolutePath());

        Assert.assertFalse("After saving, isModified must be false", h.isModified());
        Assert.assertEquals(newFile.getAbsolutePath(), h.getFilePath());

        // Clean re-load
        Horoscope reloaded = Horoscope.createFromFile(newFile.getAbsolutePath());
        Assert.assertEquals("BrandNewPerson", reloaded.getPersonName());
    }

    @Test
    public void testSwitchToMarriageMatchFoldsNotesAndClearsContext() throws Exception {
        app.openDocument(fileA.getAbsolutePath());
        app.unfoldNotes();
        Assert.assertFalse("Notes should be unfolded", app.isNotesFolded());
        Assert.assertNotNull(app.getChartNotesPanel());
        Assert.assertTrue(app.getChartNotesPanel().getEditor().getText().contains("Original Notes A"));

        // Switch to Marriage Match
        app.displayMarriageMatch("Husband", "Wife", Rasi.Mesha, Rasi.Vrishabha,
                Nakshathra.Ashwini, Nakshathra.Rohini);

        // Notes command must be enabled in Marriage Match
        Assert.assertTrue("Notes action must be enabled in Marriage Match", app.getActionManager().getAction(Command.NOTES).isEnabled());
        // Editor must be cleared / rebound to Compatibility
        Assert.assertFalse("Notes editor must not retain stale horoscope notes in Marriage Match",
                app.getChartNotesPanel().getEditor().getText().contains("Original Notes A"));
        // Window title must reflect Marriage Match
        Assert.assertTrue("Title must show Marriage Match: " + app.getTitle(),
                app.getTitle().contains("Marriage Match (Husband & Wife)"));

        // Switch back to horoscope
        app.onSwitchToHoroscopeView();
        Assert.assertTrue("Title must restore to horoscope file: " + app.getTitle(),
                app.getTitle().contains(fileA.getName()));
        Assert.assertTrue("Notes must rebind to horoscope",
                app.getChartNotesPanel().getEditor().getText().contains("Original Notes A"));
    }

    @Test
    public void testDirtyHoroscopeOpenCompactibilityPromptCancel() throws Exception {
        app.openHoroscope(fileA.getAbsolutePath());
        Horoscope currentH = app.getHoroscope();
        app.getChartNotesPanel().getEditor().setText("<p>Pending changes</p>");
        Assert.assertTrue(currentH.isModified());

        final AtomicBoolean promptCalled = new AtomicBoolean(false);
        AstroSoft.setSavePromptDelegate((title, parent) -> {
            promptCalled.set(true);
            return 2; // Cancel
        });

        // Open .asc file
        app.openDocument(fileComp.getAbsolutePath());

        Assert.assertTrue("Save prompt must appear when opening .asc from dirty horoscope", promptCalled.get());
        Assert.assertEquals("PersonA", app.getHoroscope().getPersonName());
        Assert.assertTrue(app.getHoroscope().isModified());
    }

    @Test
    public void testDirtyHoroscopeOpenCompactibilityPromptSave() throws Exception {
        app.openHoroscope(fileA.getAbsolutePath());
        Horoscope currentH = app.getHoroscope();
        app.getChartNotesPanel().getEditor().setText("<p>Notes before opening comp</p>");
        Assert.assertTrue(currentH.isModified());

        final AtomicBoolean promptCalled = new AtomicBoolean(false);
        AstroSoft.setSavePromptDelegate((title, parent) -> {
            promptCalled.set(true);
            return 0; // Save
        });

        app.openDocument(fileComp.getAbsolutePath());

        Assert.assertTrue("Save prompt must appear when opening .asc from dirty horoscope", promptCalled.get());
        // Verify PersonA was saved
        Horoscope reloadedA = Horoscope.createFromFile(fileA.getAbsolutePath());
        Assert.assertTrue(reloadedA.getNotes().contains("Notes before opening comp"));
        // Notes must be folded
        Assert.assertTrue(app.isNotesFolded());
        // Window title must reflect .asc file
        Assert.assertTrue(app.getTitle().contains(fileComp.getName()));
    }

    @Test
    public void testDirtyMuhurtaOpenCompactibilityPromptCancel() throws Exception {
        app.openMuhurta(fileMuhurta.getAbsolutePath());
        MuhurtaDocument doc = app.getMyMuhurtaView().getDocument();
        app.getChartNotesPanel().getEditor().setText("<p>Muhurta thoughts</p>");
        Assert.assertTrue(doc.isModified());

        final AtomicBoolean promptCalled = new AtomicBoolean(false);
        AstroSoft.setSavePromptDelegate((title, parent) -> {
            promptCalled.set(true);
            return 2; // Cancel
        });

        app.openDocument(fileComp.getAbsolutePath());

        Assert.assertTrue("Save prompt must appear when opening .asc from dirty muhurta", promptCalled.get());
        Assert.assertEquals("HouseWarming", app.getMyMuhurtaView().getDocument().getName());
        Assert.assertTrue(doc.isModified());
    }

    @Test
    public void testCleanMuhurtaNeverPromptsOnOpen() throws Exception {
        app.openMuhurta(fileMuhurta.getAbsolutePath());
        Assert.assertFalse(app.getMyMuhurtaView().getDocument().isModified());

        final AtomicBoolean promptCalled = new AtomicBoolean(false);
        AstroSoft.setSavePromptDelegate((title, parent) -> {
            promptCalled.set(true);
            return 2;
        });

        app.openMuhurta(fileMuhurta2.getAbsolutePath());

        Assert.assertFalse("Clean muhurta must never prompt save on open", promptCalled.get());
        Assert.assertEquals("TravelMuhurta", app.getMyMuhurtaView().getDocument().getName());
    }

    @Test
    public void testAllUtilityScreensFoldNotesAndSetTitle() throws Exception {
        app.openHoroscope(fileA.getAbsolutePath());
        app.unfoldNotes();
        Assert.assertFalse(app.isNotesFolded());

        // 1. Panchang
        app.viewChanged(View.PANCHANG_VIEW);
        Assert.assertTrue("Panchang must fold notes", app.isNotesFolded());
        Assert.assertTrue(app.getChartNotesPanel().getNotesContent().trim().isEmpty());
        Assert.assertTrue(app.getTitle().contains("Panchang"));

        // 2. Ephemeris
        app.viewChanged(View.EPHEMERIS_VIEW);
        Assert.assertTrue("Ephemeris must fold notes", app.isNotesFolded());
        Assert.assertTrue(app.getChartNotesPanel().getNotesContent().trim().isEmpty());
        Assert.assertTrue(app.getTitle().contains("Ephemeris"));

        // 3. Find Name
        app.viewChanged(View.FIND_NAME_VIEW);
        Assert.assertTrue("Find Name must fold notes", app.isNotesFolded());
        Assert.assertTrue(app.getChartNotesPanel().getNotesContent().trim().isEmpty());
        Assert.assertTrue(app.getTitle().contains("Find Name"));

        // 4. Muhurtha utility
        app.viewChanged(View.MUHURTHA_VIEW);
        Assert.assertTrue("Muhurtha must fold notes", app.isNotesFolded());
        Assert.assertTrue(app.getChartNotesPanel().getNotesContent().trim().isEmpty());
        Assert.assertTrue(app.getTitle().contains("Muhurtha"));

        // 5. Marriage Compatibility (supports Notes)
        app.displayCompatibility(app.getHoroscope(), app.getHoroscope());
        Assert.assertTrue("Compatibility must enable notes", app.getActionManager().getAction(Command.NOTES).isEnabled());
        Assert.assertTrue(app.getChartNotesPanel().getNotesContent().trim().isEmpty());
        Assert.assertTrue(app.getTitle().contains("Marriage Compatibility"));

        // 6. Marriage Match (supports Notes)
        app.displayMarriageMatch(app.getHoroscope(), app.getHoroscope());
        Assert.assertTrue("Marriage Match must enable notes", app.getActionManager().getAction(Command.NOTES).isEnabled());
        Assert.assertTrue(app.getChartNotesPanel().getNotesContent().trim().isEmpty());
        Assert.assertTrue(app.getTitle().contains("Marriage Match"));

        // Return to Horoscope
        app.onSwitchToHoroscopeView();
        Assert.assertTrue(app.getTitle().contains(fileA.getName()));
        Assert.assertTrue(app.getChartNotesPanel().getEditor().getText().contains("Original Notes A"));
    }

    @Test
    public void testHoroscopeSubViewsPreserveHoroscopeAndNotes() throws Exception {
        app.openHoroscope(fileA.getAbsolutePath());
        Assert.assertEquals("PersonA", app.getHoroscope().getPersonName());

        View[] horoscopeViews = new View[] {
            View.DASAS_VIEW,
            View.PLANET_POS_VIEW,
            View.BHAVA_POS_VIEW,
            View.DIV_CHART_VIEW,
            View.ASHTAVARGA_VIEW,
            View.SHADBALA_VIEW,
            View.YOGA_COMBINATIONS_VIEW,
            View.CHART_VIEW
        };

        for (View v : horoscopeViews) {
            app.viewChanged(v);
            Assert.assertNotNull("Horoscope must be preserved in " + v, app.getHoroscope());
            Assert.assertEquals("PersonA", app.getHoroscope().getPersonName());
            Assert.assertTrue("Title must retain horoscope file in " + v, app.getTitle().contains(fileA.getName()));
            Assert.assertTrue("Notes must be preserved in " + v,
                    app.getChartNotesPanel().getEditor().getText().contains("Original Notes A"));
        }
    }

    @Test
    public void testMuhurtaToMarriageMatchAndBack() throws Exception {
        app.openMuhurta(fileMuhurta.getAbsolutePath());
        app.unfoldNotes();
        Assert.assertFalse(app.isNotesFolded());
        Assert.assertTrue(app.getChartNotesPanel().getEditor().getText().contains("Original Muhurta Notes"));

        // Switch to Marriage Match
        app.displayMarriageMatch("B", "G", Rasi.Mesha, Rasi.Mesha, Nakshathra.Ashwini, Nakshathra.Ashwini);
        Assert.assertTrue(app.getActionManager().getAction(Command.NOTES).isEnabled());
        Assert.assertTrue(app.getChartNotesPanel().getNotesContent().trim().isEmpty());

        // Switch back to Muhurta
        app.onSwitchToMuhurtaView();
        Assert.assertTrue(app.getTitle().contains(fileMuhurta.getName()));
        Assert.assertTrue(app.getChartNotesPanel().getEditor().getText().contains("Original Muhurta Notes"));
    }

    @Test
    public void testAutomaticBackupCreatedOnSave() throws Exception {
        // Test horoscope backup
        File testHFile = File.createTempFile("test_bak_h_", ".ash");
        testHFile.deleteOnExit();
        Horoscope h = new Horoscope(new BirthData("BackupTestH", Sex.Male, 1, 1, 1990, 10, 0, 0, new Place("Delhi", 28.6, 77.2, 5.5)));
        h.setNotes("First Version of Notes");
        h.saveToFile(testHFile.getAbsolutePath());

        File bakHFile = new File(testHFile.getAbsolutePath() + ".bak");
        bakHFile.deleteOnExit();

        // Update and save again
        h.setNotes("Second Version of Notes (Updated)");
        h.saveToFile(testHFile.getAbsolutePath());

        Assert.assertTrue("A .bak file must be created on overwriting save", bakHFile.exists());
        // Verify .bak has the FIRST version
        Horoscope fromBak = Horoscope.createFromFile(bakHFile.getAbsolutePath());
        Assert.assertTrue(fromBak.getNotes().contains("First Version of Notes"));
        // Verify active file has the SECOND version
        Horoscope fromActive = Horoscope.createFromFile(testHFile.getAbsolutePath());
        Assert.assertTrue(fromActive.getNotes().contains("Second Version of Notes (Updated)"));

        // Test muhurta backup
        File testMFile = File.createTempFile("test_bak_m_", ".asm");
        testMFile.deleteOnExit();
        MuhurtaDocument m = new MuhurtaDocument("BackupTestM", new Date(), new Date(), new Place("Delhi", 28.6, 77.2, 5.5), null, null);
        m.setNotes("Muhurta Version 1");
        m.saveToFile(testMFile.getAbsolutePath());

        File bakMFile = new File(testMFile.getAbsolutePath() + ".bak");
        bakMFile.deleteOnExit();

        m.setNotes("Muhurta Version 2 (Updated)");
        m.saveToFile(testMFile.getAbsolutePath());

        Assert.assertTrue("A .bak file must be created for Muhurta on save", bakMFile.exists());
        MuhurtaDocument mBak = MuhurtaDocument.createFromFile(bakMFile.getAbsolutePath());
        Assert.assertTrue(mBak.getNotes().contains("Muhurta Version 1"));
    }

    private void switchToScreen(AstroSoft app, View v) {
        if (v == View.MARRIAGE_MATCH_VIEW) {
            app.displayMarriageMatch("Boy", "Girl", Rasi.Mesha, Rasi.Mesha, Nakshathra.Ashwini, Nakshathra.Ashwini);
        } else if (v == View.COMPATIBILITY_VIEW || v == View.COMPACTIBILITY_VIEW) {
            app.displayCompatibility("Boy", "Girl", Rasi.Mesha, Rasi.Mesha, Nakshathra.Ashwini, Nakshathra.Ashwini);
        } else if (v == View.MUHURTHA_VIEW) {
            Calendar cal = Calendar.getInstance();
            app.displayMuhurtha(new Muhurtha(cal, Rasi.Mesha, Nakshathra.Ashwini, false, false, 1));
        } else {
            app.viewChanged(v);
        }
    }

    private boolean isHoroscopeView(View v) {
        return v == View.CHART_VIEW || v == View.DASAS_VIEW || v == View.PLANET_POS_VIEW
                || v == View.BHAVA_POS_VIEW || v == View.DIV_CHART_VIEW || v == View.ASHTAVARGA_VIEW
                || v == View.SHADBALA_VIEW || v == View.YOGA_COMBINATIONS_VIEW;
    }

    private boolean isUtilityView(View v) {
        return v == View.PANCHANG_VIEW || v == View.EPHEMERIS_VIEW || v == View.FIND_NAME_VIEW
                || v == View.MUHURTHA_VIEW;
    }

    private boolean isCompatibilityView(View v) {
        return v == View.COMPATIBILITY_VIEW || v == View.MARRIAGE_MATCH_VIEW;
    }

    @Test
    public void testExhaustiveAllScreensPairwiseSwitchMatrix() throws Exception {
        app.openHoroscope(fileA.getAbsolutePath());
        app.openMuhurta(fileMuhurta.getAbsolutePath());
        app.openHoroscope(fileA.getAbsolutePath());

        View[] allViews = View.values();
        int switchCount = 0;

        for (View source : allViews) {
            for (View target : allViews) {
                // Switch to source
                switchToScreen(app, source);

                // Switch from source -> target
                switchToScreen(app, target);
                switchCount++;

                // Validation on target screen
                if (isUtilityView(target)) {
                    Assert.assertTrue("Notes must be folded on " + target + " when coming from " + source,
                            app.isNotesFolded());
                    Assert.assertTrue("Notes editor must be empty on " + target + " when coming from " + source,
                            app.getChartNotesPanel().getNotesContent().trim().isEmpty());
                } else if (isCompatibilityView(target)) {
                    Assert.assertTrue("Notes must be enabled on " + target + " when coming from " + source,
                            app.getActionManager().getAction(Command.NOTES).isEnabled());
                    Assert.assertTrue("Notes editor must be empty on newly displayed " + target + " when coming from " + source,
                            app.getChartNotesPanel().getNotesContent().trim().isEmpty());
                } else if (isHoroscopeView(target)) {
                    Assert.assertNotNull("Horoscope must not be null on " + target + " when coming from " + source,
                            app.getHoroscope());
                    Assert.assertTrue("Title must contain horoscope file on " + target,
                            app.getTitle().contains(fileA.getName()));
                    Assert.assertTrue("Notes must be rebound to horoscope on " + target,
                            app.getChartNotesPanel().getEditor().getText().contains("Original Notes A"));
                } else if (target == View.MY_MUHURTA_VIEW) {
                    Assert.assertNotNull("Muhurta doc must not be null on MY_MUHURTA_VIEW when coming from " + source,
                            app.getMyMuhurtaView().getDocument());
                    Assert.assertTrue("Notes must be rebound to Muhurta doc on MY_MUHURTA_VIEW",
                            app.getChartNotesPanel().getEditor().getText().contains("Original Muhurta Notes"));
                }
            }
        }

        Assert.assertEquals("Must test all 15x15 = 225 pairwise view transitions", 15 * 15, switchCount);
    }
}
