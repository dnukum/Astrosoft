package app.astrosoft.test;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.Sex;
import app.astrosoft.core.Horoscope;
import app.astrosoft.export.XMLHelper;

import java.io.File;

public class ChartNotesXmlTest {

    @Test
    public void testBirthDataWithNotesSerialization() {
        Place place = new Place("Chennai", 13.0827, 80.2707, 5.5);
        BirthData bd = new BirthData("Test User", Sex.Male, 10, 5, 1985, 14, 30, 0, place);
        
        String notesContent = "<html><body><p><b>Prediction:</b> Strong Jupiter transit.</p></body></html>";
        bd.setNotes(notesContent);

        // 1. Serialize to XML Document
        Document doc = XMLHelper.createDOM();
        Element root = bd.toXMLElement(doc);
        doc.appendChild(root);

        // 2. Deserialize from XML Document
        BirthData parsedBd = BirthData.valueOfXMLNode(root);

        // 3. Verify
        Assert.assertEquals("Name should match", "Test User", parsedBd.name());
        Assert.assertEquals("Notes content should match exactly", notesContent, parsedBd.getNotes());
    }

    @Test
    public void testBackwardCompatibilityWithOlderFilesWithoutNotes() {
        // Test loading sample file without notes tag
        File sampleFile = new File("samples/suba.ash");
        if (sampleFile.exists()) {
            Document doc = XMLHelper.parseXML("samples/suba.ash");
            Assert.assertNotNull("Document should parse", doc);
            BirthData bd = BirthData.valueOfXMLNode(doc.getChildNodes().item(0));
            Assert.assertNotNull("BirthData should not be null", bd);
            Assert.assertEquals("Name should be Suba", "Suba", bd.name());
            Assert.assertNull("Notes should be null when missing in older file", bd.getNotes());
        }
    }

    @Test
    public void testTextSanitizationAndReformatting() {
        String dirtyInput = "“Client Query”: ‘Native’ has job loss—needs guidance.\u200B\uFEFF\n" +
                            "• Point 1: Mars–Sun dasha\u00A0is good.\n\n\n\n" +
                            "• Point 2: Sade-Sati is active.";

        String cleaned = app.astrosoft.ui.comp.ChartNotesPanel.cleanAndSanitizeText(dirtyInput);

        // Verify smart quotes replaced
        Assert.assertFalse("Should not contain smart quotes", cleaned.contains("“") || cleaned.contains("”"));
        Assert.assertTrue("Should contain standard quote", cleaned.contains("\"Client Query\""));

        // Verify curly apostrophe replaced
        Assert.assertFalse("Should not contain curly apostrophes", cleaned.contains("‘") || cleaned.contains("’"));
        Assert.assertTrue("Should contain standard single quote", cleaned.contains("'Native'"));

        // Verify dashes normalized
        Assert.assertFalse("Should not contain em dash", cleaned.contains("—"));
        Assert.assertTrue("Should contain standard hyphen", cleaned.contains("loss-needs"));

        // Verify zero-width characters removed
        Assert.assertFalse("Should not contain zero-width space", cleaned.contains("\u200B"));
        Assert.assertFalse("Should not contain BOM", cleaned.contains("\uFEFF"));

        // Verify non-breaking space converted
        Assert.assertFalse("Should not contain non-breaking space", cleaned.contains("\u00A0"));

        // Verify excess newlines collapsed (at most 2 consecutive newlines)
        Assert.assertFalse("Should not contain 3+ consecutive newlines", cleaned.contains("\n\n\n"));
    }

    @Test
    public void testCutAndPasteWorkflow() throws Exception {
        Place place = new Place("Delhi", 28.61, 77.20, 5.5);
        BirthData bd = new BirthData("Test User", Sex.Male, 10, 5, 1985, 14, 30, 0, place);
        Horoscope h = new Horoscope(bd);

        app.astrosoft.ui.comp.ChartNotesPanel panel = new app.astrosoft.ui.comp.ChartNotesPanel(null);
        panel.setHoroscope(h);

        // Access internal JTextPane
        javax.swing.JTextPane editor = null;
        for (java.awt.Component c : panel.getComponents()) {
            if (c instanceof javax.swing.JScrollPane) {
                editor = (javax.swing.JTextPane) ((javax.swing.JScrollPane) c).getViewport().getView();
            }
        }
        Assert.assertNotNull("Editor JTextPane must be present", editor);

        // 1. Initial text
        editor.setText("<p>First sentence. Second sentence. Third sentence.</p>");
        String docText = editor.getDocument().getText(0, editor.getDocument().getLength());

        // 2. Select "Second sentence. "
        int s = docText.indexOf("Second sentence.");
        editor.select(s, s + "Second sentence. ".length());

        // 3. Cut action
        javax.swing.Action cutAction = editor.getActionMap().get(javax.swing.text.DefaultEditorKit.cutAction);
        Assert.assertNotNull("Cut action must be present", cutAction);
        cutAction.actionPerformed(new java.awt.event.ActionEvent(editor, java.awt.event.ActionEvent.ACTION_PERFORMED, "cut"));

        String textAfterCut = editor.getDocument().getText(0, editor.getDocument().getLength());
        Assert.assertFalse("Cut text should no longer be present", textAfterCut.contains("Second sentence."));

        // 4. Move caret to end and paste
        editor.setCaretPosition(editor.getDocument().getLength());
        javax.swing.Action pasteAction = editor.getActionMap().get(javax.swing.text.DefaultEditorKit.pasteAction);
        Assert.assertNotNull("Paste action must be present", pasteAction);
        pasteAction.actionPerformed(new java.awt.event.ActionEvent(editor, java.awt.event.ActionEvent.ACTION_PERFORMED, "paste"));

        String textAfterPaste = editor.getDocument().getText(0, editor.getDocument().getLength());
        Assert.assertTrue("Pasted text should be present at target location", textAfterPaste.contains("Second sentence."));
        Assert.assertTrue("First sentence should remain intact", textAfterPaste.contains("First sentence."));
        Assert.assertTrue("Third sentence should remain intact", textAfterPaste.contains("Third sentence."));

        // 5. Test replace selection via paste
        // Select "First sentence." and paste over it
        int firstIndex = textAfterPaste.indexOf("First sentence.");
        editor.select(firstIndex, firstIndex + "First sentence.".length());
        pasteAction.actionPerformed(new java.awt.event.ActionEvent(editor, java.awt.event.ActionEvent.ACTION_PERFORMED, "paste"));

        String textAfterReplace = editor.getDocument().getText(0, editor.getDocument().getLength());
        Assert.assertFalse("Replaced text 'First sentence.' should be replaced", textAfterReplace.contains("First sentence."));
    }

    @Test
    public void testCopyFromBrowsers() throws Exception {
        // Complex HTML5 snippet copied from Chrome/Safari with inline styles, classes, and entities
        String browserHtml = "<div class=\"content-box\" style=\"color: rgb(34, 34, 34); font-family: 'Times New Roman', serif; font-size: 24px; background-color: rgb(255, 255, 255);\">\n" +
                             "  <p style=\"margin: 0px 0px 10px;\">Client <a href=\"https://astrosoft.org\" style=\"color: blue;\">Profile Overview</a> &amp; Details:</p>\n" +
                             "  <p>Native has <b>strong</b> benefic transits.&nbsp;&nbsp;Jupiter in 9th house.<br>Next dasha: Mars&ndash;Sun.\u200B\uFEFF</p>\n" +
                             "</div>";

        String cleaned = app.astrosoft.ui.comp.ChartNotesPanel.cleanAndSanitizeText(browserHtml);

        // Verify HTML tags and inline styles are completely stripped
        Assert.assertFalse("Should strip style attributes", cleaned.contains("style="));
        Assert.assertFalse("Should strip div tags", cleaned.contains("<div"));
        Assert.assertFalse("Should strip span/anchor tags", cleaned.contains("<a href"));
        Assert.assertFalse("Should strip bold tags", cleaned.contains("<b>"));

        // Verify content integrity
        Assert.assertTrue("Should keep anchor text", cleaned.contains("Profile Overview"));
        Assert.assertTrue("Should keep bold text", cleaned.contains("strong"));
        Assert.assertTrue("Should decode &amp;", cleaned.contains("&"));
        Assert.assertTrue("Should decode &ndash;", cleaned.contains("-"));
        Assert.assertTrue("Should convert <br> to newline", cleaned.contains("\n"));

        // Verify zero-width characters removed
        Assert.assertFalse("Should remove zero-width space", cleaned.contains("\u200B"));
        Assert.assertFalse("Should remove BOM", cleaned.contains("\uFEFF"));

        // Test pasting browser text into JTextPane
        app.astrosoft.ui.comp.ChartNotesPanel panel = new app.astrosoft.ui.comp.ChartNotesPanel(null);
        panel.setHoroscope(new Horoscope(new BirthData("User", Sex.Male, 1, 1, 1990, 12, 0, 0, new Place("Delhi", 28.6, 77.2, 5.5))));
        javax.swing.JTextPane editor = getEditor(panel);

        editor.setText("<p>Existing Notes:</p>");
        editor.setCaretPosition(editor.getDocument().getLength());
        editor.replaceSelection("\n" + cleaned);

        String fullText = editor.getDocument().getText(0, editor.getDocument().getLength());
        Assert.assertTrue("Document should contain cleaned browser text", fullText.contains("Profile Overview & Details:"));
        Assert.assertTrue("Document should contain second paragraph", fullText.contains("Jupiter in 9th house."));
    }

    @Test
    public void testCopyFromGoogleWorkspace() throws Exception {
        // 1. Google Docs snippet with docs-internal-guid, smart quotes, em dash, and list
        String gDocsHtml = "<b id=\"docs-internal-guid-abcdef-1234\" style=\"font-weight:normal;\">\n" +
                           "  <p dir=\"ltr\" style=\"line-height:1.38;margin-top:0pt;margin-bottom:0pt;\">\n" +
                           "    <span style=\"font-size:11pt;font-family:Arial;font-weight:700;\">“Kundali Analysis 2026”</span>\n" +
                           "  </p>\n" +
                           "  <ul style=\"margin-top:0;margin-bottom:0;padding-inline-start:48px;\">\n" +
                           "    <li dir=\"ltr\" style=\"list-style-type:disc;font-size:11pt;font-family:Arial;\">\n" +
                           "      <span>Career growth—expect promotion in Q3</span>\n" +
                           "    </li>\n" +
                           "    <li dir=\"ltr\" style=\"list-style-type:disc;font-size:11pt;font-family:Arial;\">\n" +
                           "      <span>Health: Saturn–Rahu conjunction needs attention</span>\n" +
                           "    </li>\n" +
                           "  </ul>\n" +
                           "</b>";

        String cleanedDocs = app.astrosoft.ui.comp.ChartNotesPanel.cleanAndSanitizeText(gDocsHtml);
        Assert.assertFalse("Should remove docs-internal-guid", cleanedDocs.contains("docs-internal-guid"));
        Assert.assertFalse("Should normalize smart quotes", cleanedDocs.contains("“") || cleanedDocs.contains("”"));
        Assert.assertTrue("Should have standard quotes", cleanedDocs.contains("\"Kundali Analysis 2026\""));
        Assert.assertFalse("Should normalize em dash", cleanedDocs.contains("—"));
        Assert.assertTrue("Should have standard hyphen", cleanedDocs.contains("growth-expect"));
        Assert.assertTrue("Should normalize list items to bullets", cleanedDocs.contains("• Career growth"));
        Assert.assertTrue("Should normalize second list item", cleanedDocs.contains("• Health:"));

        // 2. Google Sheets table
        String gSheetsHtml = "<table><tbody>" +
                             "  <tr><td>Planet</td><td>Longitude</td><td>Rasi</td></tr>" +
                             "  <tr><td>Sun</td><td>123.45</td><td>Leo</td></tr>" +
                             "  <tr><td>Moon</td><td>210.12</td><td>Scorpio</td></tr>" +
                             "</tbody></table>";
        String cleanedSheets = app.astrosoft.ui.comp.ChartNotesPanel.cleanAndSanitizeText(gSheetsHtml);
        Assert.assertTrue("Should preserve tab separation between table columns", cleanedSheets.contains("Planet\tLongitude\tRasi"));
        Assert.assertTrue("Should preserve row separation", cleanedSheets.contains("Sun\t123.45\tLeo"));
        Assert.assertTrue("Should preserve second row", cleanedSheets.contains("Moon\t210.12\tScorpio"));

        // 3. Google Slides bullets and soft breaks (\u2028)
        String gSlidesText = "▪ Raja Yoga Formations\u2028Jupiter in Kendra\n" +
                             "▪ Dhana Yoga Formations\u2028Venus in 11th house\n" +
                             "► Summary Notes";
        String cleanedSlides = app.astrosoft.ui.comp.ChartNotesPanel.cleanAndSanitizeText(gSlidesText);
        Assert.assertFalse("Should normalize square bullet", cleanedSlides.contains("▪"));
        Assert.assertFalse("Should normalize arrow bullet", cleanedSlides.contains("►"));
        Assert.assertTrue("Should convert square bullet to standard bullet", cleanedSlides.contains("• Raja Yoga"));
        Assert.assertTrue("Should convert arrow bullet to standard bullet", cleanedSlides.contains("• Summary Notes"));
        Assert.assertFalse("Should normalize Unicode line separator", cleanedSlides.contains("\u2028"));
    }

    @Test
    public void testCopyFromPdf() throws Exception {
        // PDF with ligatures, hyphenated line breaks, and soft hyphens
        String pdfText = "The na\u00ADtive has pro\uFB01table business ventures and strong bene\uFB01ts.\n" +
                         "Astrological con\uFB02icts make major decisions di\uFB03cult,\n" +
                         "causing di\uFB00erence in opinion. Natal astro-\n" +
                         "nomical charts show remedies for a\uFB04icted planets.";

        String cleaned = app.astrosoft.ui.comp.ChartNotesPanel.cleanAndSanitizeText(pdfText);

        // Verify ligatures unpacked
        Assert.assertFalse("Should unpack fi ligature", cleaned.contains("\uFB01"));
        Assert.assertTrue("Should contain 'profitable'", cleaned.contains("profitable"));
        Assert.assertTrue("Should contain 'benefits'", cleaned.contains("benefits"));
        Assert.assertFalse("Should unpack fl ligature", cleaned.contains("\uFB02"));
        Assert.assertTrue("Should contain 'conflicts'", cleaned.contains("conflicts"));
        Assert.assertFalse("Should unpack ff ligature", cleaned.contains("\uFB00"));
        Assert.assertTrue("Should contain 'difference'", cleaned.contains("difference"));
        Assert.assertFalse("Should unpack ffi ligature", cleaned.contains("\uFB03"));
        Assert.assertTrue("Should contain 'difficult'", cleaned.contains("difficult"));
        Assert.assertFalse("Should unpack ffl ligature", cleaned.contains("\uFB04"));
        Assert.assertTrue("Should contain 'afflicted'", cleaned.contains("afflicted"));

        // Verify soft hyphen removed
        Assert.assertFalse("Should remove soft hyphen", cleaned.contains("\u00AD"));
        Assert.assertTrue("Should contain 'native'", cleaned.contains("native"));

        // Verify line-wrap hyphen reassembled
        Assert.assertTrue("Should reassemble 'astronomical' across line break", cleaned.contains("astronomical"));
        Assert.assertFalse("Should not have broken 'astro- nomical'", cleaned.contains("astro-"));
    }

    @Test
    public void testCopyFromMicrosoftWord() throws Exception {
        // Microsoft Word snippet with Mso comments, MsoNormal classes, Symbol mid-dots, and smart punctuation
        String wordHtml = "<!--[if gte mso 9]><xml>\n" +
                          " <w:WordDocument>\n" +
                          "  <w:View>Normal</w:View>\n" +
                          " </w:WordDocument>\n" +
                          "</xml><![endif]-->\n" +
                          "<p class=\"MsoNormal\" style=\"margin-bottom:0in;\">\n" +
                          "  <span style=\"font-size:12.0pt;font-family:&quot;Calibri&quot;,sans-serif;\">\n" +
                          "    ‘Client Case Study’: Native’s career—transition…\n" +
                          "  </span>\n" +
                          "</p>\n" +
                          "<p class=\"MsoListParagraph\" style=\"text-indent:-.25in;margin-left:.5in;\">\n" +
                          "  <span style=\"font-family:Symbol;\">·<span style=\"font:7.0pt &quot;Times New Roman&quot;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span>\n" +
                          "  <span>Dasa analysis shows favorable Jupiter period.</span>\n" +
                          "</p>\n" +
                          "<p class=\"MsoListParagraph\" style=\"text-indent:-.25in;margin-left:.5in;\">\n" +
                          "  <span style=\"font-family:Symbol;\">·<span style=\"font:7.0pt &quot;Times New Roman&quot;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span></span>\n" +
                          "  <span>Transit Saturn moving into 10th house.</span>\n" +
                          "</p>";

        String cleaned = app.astrosoft.ui.comp.ChartNotesPanel.cleanAndSanitizeText(wordHtml);

        // Verify Microsoft Word XML and Mso tags are completely removed
        Assert.assertFalse("Should remove Word conditional comment", cleaned.contains("<!--[if"));
        Assert.assertFalse("Should remove WordDocument tag", cleaned.contains("<w:WordDocument>"));
        Assert.assertFalse("Should remove MsoNormal tag", cleaned.contains("MsoNormal"));

        // Verify smart typography normalized
        Assert.assertFalse("Should normalize curly single quote", cleaned.contains("‘") || cleaned.contains("’"));
        Assert.assertTrue("Should have standard single quote", cleaned.contains("'Client Case Study'"));
        Assert.assertTrue("Should have standard apostrophe in Native's", cleaned.contains("Native's"));
        Assert.assertFalse("Should normalize em dash", cleaned.contains("—"));
        Assert.assertTrue("Should have standard hyphen", cleaned.contains("career-transition"));
        Assert.assertFalse("Should normalize ellipsis", cleaned.contains("…"));
        Assert.assertTrue("Should have three dots for ellipsis", cleaned.contains("..."));

        // Verify Word mid-dots converted to bullets
        Assert.assertFalse("Should normalize Symbol mid-dot", cleaned.contains("·"));
        Assert.assertTrue("Should convert first Word bullet to standard bullet", cleaned.contains("• Dasa analysis"));
        Assert.assertTrue("Should convert second Word bullet to standard bullet", cleaned.contains("• Transit Saturn"));

        // Test pasting Word text into JTextPane
        app.astrosoft.ui.comp.ChartNotesPanel panel = new app.astrosoft.ui.comp.ChartNotesPanel(null);
        panel.setHoroscope(new Horoscope(new BirthData("User", Sex.Male, 1, 1, 1990, 12, 0, 0, new Place("Delhi", 28.6, 77.2, 5.5))));
        javax.swing.JTextPane editor = getEditor(panel);

        editor.setText("");
        editor.replaceSelection(cleaned);

        String docText = editor.getDocument().getText(0, editor.getDocument().getLength());
        Assert.assertTrue("Document should contain Word note title", docText.contains("'Client Case Study': Native's career-transition..."));
        Assert.assertTrue("Document should contain first bullet", docText.contains("• Dasa analysis"));
        Assert.assertTrue("Document should contain second bullet", docText.contains("• Transit Saturn"));
    }

    @Test
    public void testCopyFromWhatsApp() throws Exception {
        // 1. WhatsApp Web HTML copy (contains selectable-text wrapper, emoji images with alt text, internal newlines)
        String waWebHtml = "<div class=\"_21AhH\"><span class=\"selectable-text copyable-text\"><span>\u200ENamaste Guruji,\u200E\n" +
                           "Birth details for chart analysis:\n" +
                           "• Name: Rajesh K\n" +
                           "• DOB: 15-Oct-1992, 06:45 AM, Madurai\n" +
                           "Kindly check career and marriage prospects. </span><img class=\"emoji\" alt=\"🙏\"><img class=\"emoji\" alt=\"✨\"></span></div>";

        String cleanedWeb = app.astrosoft.ui.comp.ChartNotesPanel.cleanAndSanitizeText(waWebHtml);

        // Verify BiDi mark stripped
        Assert.assertFalse("Should strip LTR mark", cleanedWeb.contains("\u200E"));
        // Verify HTML tags stripped
        Assert.assertFalse("Should strip span tags", cleanedWeb.contains("<span"));
        Assert.assertFalse("Should strip img tags", cleanedWeb.contains("<img"));
        // Verify emojis from img alt preserved
        Assert.assertTrue("Should preserve folded emoji 🙏", cleanedWeb.contains("🙏"));
        Assert.assertTrue("Should preserve folded emoji ✨", cleanedWeb.contains("✨"));
        // Verify line breaks preserved
        Assert.assertTrue("Should keep birth details line", cleanedWeb.contains("Birth details for chart analysis:"));
        Assert.assertTrue("Should keep bullet list items", cleanedWeb.contains("• Name: Rajesh K"));

        // 2. WhatsApp Desktop / Mobile forwarded chat copy with timestamp and BiDi markers
        String waForwarded = "\u200E[10:30 AM, 24/10/2026] \u200E+91 98400 12345: \u200EQuery regarding job relocation abroad.\n" +
                             "*Urgent Query*: Visa interview scheduled next week.\n" +
                             "Will Rahu–Jupiter period support foreign travel?";

        String cleanedForwarded = app.astrosoft.ui.comp.ChartNotesPanel.cleanAndSanitizeText(waForwarded);
        Assert.assertFalse("Should strip BiDi markers from timestamp", cleanedForwarded.contains("\u200E"));
        Assert.assertTrue("Should contain clean timestamp and sender", cleanedForwarded.contains("[10:30 AM, 24/10/2026] +91 98400 12345: Query"));
        Assert.assertTrue("Should contain query body", cleanedForwarded.contains("Visa interview scheduled next week."));

        // 3. Paste into JTextPane
        app.astrosoft.ui.comp.ChartNotesPanel panel = new app.astrosoft.ui.comp.ChartNotesPanel(null);
        panel.setHoroscope(new Horoscope(new BirthData("User", Sex.Male, 1, 1, 1990, 12, 0, 0, new Place("Delhi", 28.6, 77.2, 5.5))));
        javax.swing.JTextPane editor = getEditor(panel);

        editor.setText("");
        editor.replaceSelection(cleanedWeb);

        String docText = editor.getDocument().getText(0, editor.getDocument().getLength());
        Assert.assertTrue("Editor should contain WhatsApp text with emoji", docText.contains("Namaste Guruji"));
        Assert.assertTrue("Editor should preserve emoji", docText.contains("🙏"));
        Assert.assertTrue("Editor should contain details", docText.contains("15-Oct-1992"));
    }

    @Test
    public void testHoroscopeFilePathTrackingAndDirectSave() throws Exception {
        // 1. Newly created horoscope has null filePath
        Place place = new Place("Chennai", 13.0827, 80.2707, 5.5);
        BirthData bd = new BirthData("Fresh User", Sex.Male, 1, 1, 2000, 10, 0, 0, place);
        Horoscope h = new Horoscope(bd);
        Assert.assertNull("New horoscope should not have an associated file path yet", h.getFilePath());

        // 2. Horoscope loaded from file tracks its file path
        File sampleFile = new File("samples/suba.ash");
        if (sampleFile.exists()) {
            Horoscope loaded = Horoscope.createFromFile("samples/suba.ash");
            Assert.assertNotNull("Loaded horoscope should have file path populated", loaded.getFilePath());
            Assert.assertTrue("File path should point to suba.ash", loaded.getFilePath().endsWith("samples/suba.ash"));
        }

        // 3. Saving to file updates getFilePath()
        File tempFile = File.createTempFile("astro_test_", ".ash");
        tempFile.deleteOnExit();

        h.setNotes("Notes version 1");
        h.saveToFile(tempFile.getAbsolutePath());
        Assert.assertEquals("Horoscope filePath should update to saved path", tempFile.getAbsolutePath(), h.getFilePath());

        // 4. Directly re-saving to existing file overwrites without needing dialog
        h.setNotes("Notes version 2 with updates");
        h.saveToFile(h.getFilePath());

        Horoscope reloaded = Horoscope.createFromFile(tempFile.getAbsolutePath());
        Assert.assertNotNull(reloaded);
        Assert.assertEquals("Notes version 2 with updates", reloaded.getNotes());
        Assert.assertEquals("Reloaded filePath should match tempFile", tempFile.getAbsolutePath(), reloaded.getFilePath());
    }

    @Test
    public void testBirthDataPanelPreservesNotes() {
        Place place = new Place("Bangalore", 12.9716, 77.5946, 5.5);
        BirthData originalBd = new BirthData("Raj", Sex.Male, 15, 8, 1995, 9, 30, 0, place);
        originalBd.setNotes("Important consultation notes");

        app.astrosoft.ui.comp.BirthDataPanel panel = new app.astrosoft.ui.comp.BirthDataPanel(new java.awt.Dimension(350, 300), "Birth Data");
        panel.setBirthData(originalBd);

        BirthData editedBd = panel.getBirthData();
        Assert.assertNotNull("Edited BirthData must not be null", editedBd);
        Assert.assertEquals("Name must match", "Raj", editedBd.name());
        Assert.assertEquals("Notes must be preserved across BirthDataPanel get/set", "Important consultation notes", editedBd.getNotes());
    }

    private javax.swing.JTextPane getEditor(app.astrosoft.ui.comp.ChartNotesPanel panel) {
        for (java.awt.Component c : panel.getComponents()) {
            if (c instanceof javax.swing.JScrollPane) {
                return (javax.swing.JTextPane) ((javax.swing.JScrollPane) c).getViewport().getView();
            }
        }
        return null;
    }
}
