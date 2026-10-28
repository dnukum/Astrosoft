package app.astrosoft.test;

import org.junit.Assert;
import org.junit.Test;
import java.io.File;
import java.io.FileWriter;

import app.astrosoft.consts.Command;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Compatibility;
import app.astrosoft.core.Horoscope;
import app.astrosoft.export.AstrosoftExporter;
import app.astrosoft.export.AstrosoftExporter.Type;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.view.ViewManager.View;
import java.util.concurrent.FutureTask;

public class MarriageMatchCompatibilityXmlTest {

    private Horoscope createBoyHoroscope() {
        return new Horoscope("Rohit", 15, 6, 1990, 10, 30, 77.20, 28.61, 5.5, "Delhi");
    }

    private Horoscope createGirlHoroscope() {
        return new Horoscope("Priya", 22, 9, 1993, 14, 15, 72.87, 19.07, 5.5, "Mumbai");
    }

    @Test
    public void testLegacyCompactibilityXmlDualRead() throws Exception {
        // XML with legacy typo tag <Compactibility>
        String legacyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                           "<Compactibility hasHoroscope=\"true\">\n" +
                           "  <BoyData>\n" +
                           "    <Name>Rohit</Name>\n" +
                           "    <DateTime>Jun 15 1990, 10:30:00 AM</DateTime>\n" +
                           "    <Place><City>Delhi</City><Longitude dir=\"E\">77.20</Longitude><Latitude dir=\"N\">28.61</Latitude><TimeZone>IST</TimeZone></Place>\n" +
                           "  </BoyData>\n" +
                           "  <GirlData>\n" +
                           "    <Name>Priya</Name>\n" +
                           "    <DateTime>Sep 22 1993, 02:15:00 PM</DateTime>\n" +
                           "    <Place><City>Mumbai</City><Longitude dir=\"E\">72.87</Longitude><Latitude dir=\"N\">19.07</Latitude><TimeZone>IST</TimeZone></Place>\n" +
                           "  </GirlData>\n" +
                           "</Compactibility>";

        File tempFile = File.createTempFile("legacy_comp_", ".asc");
        tempFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write(legacyXml);
        }

        Compatibility comp = Compatibility.createFromXML(tempFile.getAbsolutePath());
        Assert.assertNotNull("Must parse legacy <Compactibility> XML", comp);
        Assert.assertTrue("Must have horoscope flag set to true", comp.hasHoroscope());
        Assert.assertEquals("Rohit", comp.getBoyName());
        Assert.assertEquals("Priya", comp.getGirlName());
        Assert.assertNotNull("Boy horoscope must be populated", comp.getBoyHoroscope());
        Assert.assertNotNull("Girl horoscope must be populated", comp.getGirlHoroscope());
        Assert.assertTrue("Kuta points must be computed", comp.getTotalKutaGained() > 0);
        Assert.assertFalse("Freshly loaded legacy file must not be modified", comp.isModified());
    }

    @Test
    public void testModernCompatibilityXmlWriteAndReadRoundTripWithNotes() throws Exception {
        Horoscope boy = createBoyHoroscope();
        Horoscope girl = createGirlHoroscope();
        Compatibility comp = new Compatibility(boy, girl);

        String sampleNotes = "<html><body><p><b>Match Analysis:</b> Guna Milan score is favorable.</p>" +
                             "<table border=\"1\"><tr><th>Category</th><th>Score</th></tr>" +
                             "<tr><td>Nadi</td><td>8/8</td></tr></table></body></html>";
        comp.setNotes(sampleNotes);
        Assert.assertTrue("Setting notes must mark document as modified", comp.isModified());

        File tempFile = File.createTempFile("modern_comp_", ".asc");
        tempFile.deleteOnExit();

        comp.saveToXML(tempFile.getAbsolutePath());
        Assert.assertFalse("Saving to disk must reset isModified flag to false", comp.isModified());

        // Verify written XML contains clean <Compatibility> root tag (not the old typo)
        String xmlContent = new String(java.nio.file.Files.readAllBytes(tempFile.toPath()), "UTF-8");
        Assert.assertTrue("Written XML must use clean <Compatibility> tag", xmlContent.contains("<Compatibility"));
        Assert.assertFalse("Written XML should not use legacy <Compactibility> tag", xmlContent.contains("<Compactibility"));
        Assert.assertTrue("Written XML must contain <Notes> tag", xmlContent.contains("<Notes>"));
        Assert.assertTrue("Notes must be enclosed in CDATA", xmlContent.contains("<![CDATA["));
        Assert.assertTrue("Notes content must be present in XML", xmlContent.contains("Guna Milan score is favorable"));

        // Reload and verify
        Compatibility reloaded = Compatibility.createFromXML(tempFile.getAbsolutePath());
        Assert.assertNotNull("Reloaded compatibility must not be null", reloaded);
        Assert.assertEquals("Rohit", reloaded.getBoyName());
        Assert.assertEquals("Priya", reloaded.getGirlName());
        Assert.assertEquals("Notes must match exactly after roundtrip", sampleNotes, reloaded.getNotes());
        Assert.assertFalse("Freshly reloaded document must not be modified", reloaded.isModified());
    }

    @Test
    public void testLegacyCompactibilityWithNotesLoadsCorrectly() throws Exception {
        String legacyWithNotesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                                    "<Compactibility hasHoroscope=\"true\">\n" +
                                    "  <BoyData>\n" +
                                    "    <Name>BoyName</Name>\n" +
                                    "    <DateTime>Jun 15 1990, 10:30:00 AM</DateTime>\n" +
                                    "    <Place><City>Delhi</City><Longitude dir=\"E\">77.20</Longitude><Latitude dir=\"N\">28.61</Latitude><TimeZone>IST</TimeZone></Place>\n" +
                                    "  </BoyData>\n" +
                                    "  <GirlData>\n" +
                                    "    <Name>GirlName</Name>\n" +
                                    "    <DateTime>Sep 22 1993, 02:15:00 PM</DateTime>\n" +
                                    "    <Place><City>Mumbai</City><Longitude dir=\"E\">72.87</Longitude><Latitude dir=\"N\">19.07</Latitude><TimeZone>IST</TimeZone></Place>\n" +
                                    "  </GirlData>\n" +
                                    "  <Notes><![CDATA[<p>Legacy note content</p>]]></Notes>\n" +
                                    "</Compactibility>";

        File tempFile = File.createTempFile("legacy_notes_", ".asc");
        tempFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write(legacyWithNotesXml);
        }

        Compatibility comp = Compatibility.createFromXML(tempFile.getAbsolutePath());
        Assert.assertNotNull(comp);
        Assert.assertEquals("<p>Legacy note content</p>", comp.getNotes());
    }

    @Test
    public void testStarOnlyCompatibilityDualReadAndWrite() throws Exception {
        // Star-only legacy XML
        String starLegacyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                               "<Compactibility hasHoroscope=\"false\">\n" +
                               "  <BoyData>\n" +
                               "    <Name>BoyStar</Name>\n" +
                               "    <Rasi>Mesha</Rasi>\n" +
                               "    <Nakshathra>Ashwini</Nakshathra>\n" +
                               "  </BoyData>\n" +
                               "  <GirlData>\n" +
                               "    <Name>GirlStar</Name>\n" +
                               "    <Rasi>Vrishabha</Rasi>\n" +
                               "    <Nakshathra>Rohini</Nakshathra>\n" +
                               "  </GirlData>\n" +
                               "</Compactibility>";

        File tempFile = File.createTempFile("star_legacy_", ".asc");
        tempFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write(starLegacyXml);
        }

        Compatibility comp = Compatibility.createFromXML(tempFile.getAbsolutePath());
        Assert.assertNotNull(comp);
        Assert.assertFalse("Star-only should have hasHoroscope == false", comp.hasHoroscope());
        Assert.assertEquals("BoyStar", comp.getBoyName());
        Assert.assertEquals("GirlStar", comp.getGirlName());
        Assert.assertEquals(Rasi.Mesha, comp.getBoyRasi());
        Assert.assertEquals(Nakshathra.Ashwini, comp.getBoyNak());

        // Now save as modern XML and verify it writes <Compatibility>
        File starModern = File.createTempFile("star_modern_", ".asc");
        starModern.deleteOnExit();
        comp.saveToXML(starModern.getAbsolutePath());

        String content = new String(java.nio.file.Files.readAllBytes(starModern.toPath()), "UTF-8");
        Assert.assertTrue("Must write clean <Compatibility>", content.contains("<Compatibility"));
        Assert.assertFalse("Must not write legacy <Compactibility>", content.contains("<Compactibility"));

        Compatibility reloaded = Compatibility.createFromXML(starModern.getAbsolutePath());
        Assert.assertNotNull(reloaded);
        Assert.assertEquals("BoyStar", reloaded.getBoyName());
    }

    @Test
    public void testSaveCreatesAutomaticBackupFile() throws Exception {
        Horoscope boy = createBoyHoroscope();
        Horoscope girl = createGirlHoroscope();
        Compatibility comp = new Compatibility(boy, girl);

        File targetFile = File.createTempFile("backup_test_", ".asc");
        targetFile.deleteOnExit();

        comp.setNotes("<p>Version 1</p>");
        comp.saveToXML(targetFile.getAbsolutePath());

        File backupFile = new File(targetFile.getAbsolutePath() + ".bak");
        if (backupFile.exists()) backupFile.delete();

        // Second save over existing file
        comp.setNotes("<p>Version 2</p>");
        comp.saveToXML(targetFile.getAbsolutePath());

        Assert.assertTrue("Saving over existing file must create .bak backup", backupFile.exists());
        String backupContent = new String(java.nio.file.Files.readAllBytes(backupFile.toPath()), "UTF-8");
        Assert.assertTrue("Backup must contain prior version content", backupContent.contains("Version 1"));
        backupFile.delete();
    }

    @Test
    public void testNotesOwnerContract() {
        Horoscope boy = createBoyHoroscope();
        Horoscope girl = createGirlHoroscope();
        Compatibility comp = new Compatibility(boy, girl);

        Assert.assertTrue("Notes owner title must contain both candidates",
                comp.getNotesOwnerTitle().contains("Rohit") && comp.getNotesOwnerTitle().contains("Priya"));
    }

    @Test
    public void testMarriageMatchToolbarActionStateParity() {
        AstroSoft app = new AstroSoft();
        Horoscope boy = createBoyHoroscope();
        Horoscope girl = createGirlHoroscope();

        app.displayMarriageMatch(boy, girl);

        Assert.assertEquals("Current view must be MARRIAGE_MATCH_VIEW", View.MARRIAGE_MATCH_VIEW, app.getCurrentView());
        Assert.assertTrue("Save action must be enabled on Marriage Match", app.getActionManager().getAction(Command.SAVE).isEnabled());
        Assert.assertTrue("Save As action must be enabled on Marriage Match", app.getActionManager().getAction(Command.SAVE_AS).isEnabled());
        Assert.assertTrue("Print action must be enabled on Marriage Match", app.getActionManager().getAction(Command.PRINT).isEnabled());
        Assert.assertTrue("Notes action must be enabled on Marriage Match", app.getActionManager().getAction(Command.NOTES).isEnabled());
        Assert.assertNotNull("Chart notes panel must be bound to Compatibility document", app.getChartNotesPanel().getNotesOwner());
    }

    @Test
    public void testAutoRoutingOnOpenDocument() throws Exception {
        AstroSoft app = new AstroSoft();
        Horoscope boy = createBoyHoroscope();
        Horoscope girl = createGirlHoroscope();
        Compatibility fullComp = new Compatibility(boy, girl);

        File fullFile = File.createTempFile("full_match_", ".asc");
        fullFile.deleteOnExit();
        fullComp.saveToXML(fullFile.getAbsolutePath());

        // Open full horoscope compatibility
        app.openDocument(fullFile.getAbsolutePath());
        Assert.assertEquals("Full birth data compatibility must route to MARRIAGE_MATCH_VIEW",
                View.MARRIAGE_MATCH_VIEW, app.getCurrentView());
        Assert.assertTrue("Notes must be enabled", app.getActionManager().getAction(Command.NOTES).isEnabled());

        // Open star-only compatibility
        Compatibility starComp = new Compatibility("BoyStar", "GirlStar", Nakshathra.Ashwini, Nakshathra.Rohini, Rasi.Mesha, Rasi.Vrishabha);
        File starFile = File.createTempFile("star_match_", ".asc");
        starFile.deleteOnExit();
        starComp.saveToXML(starFile.getAbsolutePath());

        app.openDocument(starFile.getAbsolutePath());
        Assert.assertEquals("Star-only compatibility must route to COMPATIBILITY_VIEW",
                View.COMPATIBILITY_VIEW, app.getCurrentView());
    }

    @Test
    public void testPrintDialogTypeSeparationAndNaming() {
        // MarriageMatch must be distinct and display "Marriage Match"
        Assert.assertEquals("Marriage Match", Type.MarriageMatch.toString());
        // Compatibility must display "Marriage Compatibility"
        Assert.assertEquals("Marriage Compatibility", Type.Compatibility.toString());
    }

    @Test
    public void testMarriageMatchPdfExportIncludesNotes() throws Exception {
        Horoscope boy = createBoyHoroscope();
        Horoscope girl = createGirlHoroscope();
        Compatibility comp = new Compatibility(boy, girl);
        comp.setNotes("Auspicious match. Excellent planetary alignment for prosperity and harmony.");

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "mm_export_test_" + System.currentTimeMillis());
        tempDir.mkdirs();
        try {
            String base = new File(tempDir, "Rohit_Priya__MarriageMatch").getAbsolutePath();
            FutureTask<Object> task = AstrosoftExporter.export(Type.MarriageMatch, comp, base, true, false, true);
            task.get();

            File xml = new File(base + ".xml");
            File pdf = new File(base + ".pdf");

            Assert.assertTrue("Exported XML must exist", xml.exists() && xml.length() > 0);
            String xmlContent = new String(java.nio.file.Files.readAllBytes(xml.toPath()), "UTF-8");
            Assert.assertTrue("XML must contain Notes CDATA", xmlContent.contains("<Notes><![CDATA["));
            Assert.assertTrue("XML must contain Note-Line text", xmlContent.contains("<Note-Line>"));

            Assert.assertTrue("PDF must exist", pdf.exists());
            Assert.assertTrue("PDF must be a complete generated document (> 10 KB), was " + pdf.length(), pdf.length() > 10000);
        } finally {
            for (File f : tempDir.listFiles()) {
                f.delete();
            }
            tempDir.delete();
        }
    }

    @Test
    public void testStarCompatibilityPdfExportWithoutNotes() throws Exception {
        Compatibility starComp = new Compatibility("BoyStar", "GirlStar", Nakshathra.Ashwini, Nakshathra.Rohini, Rasi.Mesha, Rasi.Vrishabha);

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "star_export_test_" + System.currentTimeMillis());
        tempDir.mkdirs();
        try {
            String base = new File(tempDir, "BoyStar_GirlStar__MarriageCompatibility").getAbsolutePath();
            FutureTask<Object> task = AstrosoftExporter.export(Type.Compatibility, starComp, base, true, false, true);
            task.get();

            File xml = new File(base + ".xml");
            File pdf = new File(base + ".pdf");

            Assert.assertTrue("Exported XML must exist", xml.exists() && xml.length() > 0);
            Assert.assertTrue("PDF must exist", pdf.exists());
            Assert.assertTrue("PDF must be a complete generated document (> 5 KB), was " + pdf.length(), pdf.length() > 5000);
        } finally {
            for (File f : tempDir.listFiles()) {
                f.delete();
            }
            tempDir.delete();
        }
    }
}
