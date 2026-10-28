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

    @Test
    public void testCrossPlatformCommandShortcutsAndCollisions() {
        int menuMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        
        // 1. Verify Save accelerator matches platform menu mask (Cmd on Mac, Ctrl on Windows)
        javax.swing.KeyStroke saveKey = app.astrosoft.consts.Command.SAVE.key();
        Assert.assertNotNull(saveKey);
        Assert.assertEquals(java.awt.event.KeyEvent.VK_S, saveKey.getKeyCode());
        Assert.assertEquals(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, menuMask), saveKey);

        // 2. Verify Save As accelerator is Cmd/Ctrl + Shift + S
        javax.swing.KeyStroke saveAsKey = app.astrosoft.consts.Command.SAVE_AS.key();
        Assert.assertNotNull(saveAsKey);
        Assert.assertEquals(java.awt.event.KeyEvent.VK_S, saveAsKey.getKeyCode());
        Assert.assertEquals(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, menuMask | java.awt.event.InputEvent.SHIFT_DOWN_MASK), saveAsKey);

        // 3. Verify Save Compactibility does not collide with Save As
        javax.swing.KeyStroke saveCompKey = app.astrosoft.consts.Command.SAVE_COMPACTIBILITY.key();
        Assert.assertNotNull(saveCompKey);
        Assert.assertEquals(java.awt.event.KeyEvent.VK_C, saveCompKey.getKeyCode());
        Assert.assertFalse("Save As and Save Compactibility must not share the same shortcut", saveAsKey.equals(saveCompKey));

        // 4. Verify Exit does not collide with Cut (VK_X)
        javax.swing.KeyStroke exitKey = app.astrosoft.consts.Command.EXIT.key();
        Assert.assertNotNull(exitKey);
        Assert.assertEquals("Exit should be Q, not Cut (X)", java.awt.event.KeyEvent.VK_Q, exitKey.getKeyCode());
    }

    @Test
    public void testBulletedAndNumberedListsWithUndoAndSmartEnter() throws Exception {
        app.astrosoft.beans.BirthData bd = new app.astrosoft.beans.BirthData("ListTest", app.astrosoft.consts.Sex.Male, 1, 1, 1990, 12, 0, 0, new app.astrosoft.beans.Place("Delhi", 28.6, 77.2, 5.5));
        app.astrosoft.core.Horoscope h = new app.astrosoft.core.Horoscope(bd);
        app.astrosoft.ui.comp.ChartNotesPanel panel = new app.astrosoft.ui.comp.ChartNotesPanel(null);
        panel.setHoroscope(h);

        javax.swing.JTextPane editor = panel.getEditor();
        javax.swing.text.Document doc = editor.getDocument();
        
        // 1. Initial multi-line text
        doc.insertString(doc.getLength(), "Sun in Aries\nMoon in Taurus\nMars in Leo", null);
        editor.select(0, doc.getLength());

        // 2. Format as Bulleted List
        panel.toggleList(false);
        String bulleted = doc.getText(0, doc.getLength()).trim();
        Assert.assertTrue("Line 1 should have bullet", bulleted.contains("• Sun in Aries"));
        Assert.assertTrue("Line 2 should have bullet", bulleted.contains("• Moon in Taurus"));
        Assert.assertTrue("Line 3 should have bullet", bulleted.contains("• Mars in Leo"));

        // 3. Switch to Numbered List
        editor.select(0, doc.getLength());
        panel.toggleList(true);
        String numbered = doc.getText(0, doc.getLength()).trim();
        Assert.assertTrue("Line 1 should be 1.", numbered.contains("1. Sun in Aries"));
        Assert.assertTrue("Line 2 should be 2.", numbered.contains("2. Moon in Taurus"));
        Assert.assertTrue("Line 3 should be 3.", numbered.contains("3. Mars in Leo"));

        // 4. Test Undo (Cmd+Z / Ctrl+Z action)
        javax.swing.Action undoAction = editor.getActionMap().get("undo");
        Assert.assertNotNull("Undo action must be registered in editor action map", undoAction);
        undoAction.actionPerformed(new java.awt.event.ActionEvent(editor, java.awt.event.ActionEvent.ACTION_PERFORMED, "undo"));
        String reverted = doc.getText(0, doc.getLength()).trim();
        Assert.assertTrue("Undo should revert back to bullet list", reverted.contains("• Sun in Aries"));

        // 5. Test Smart Enter on Bullet List
        editor.setCaretPosition(doc.getLength());
        boolean handledBulletEnter = panel.handleSmartEnter();
        Assert.assertTrue("Smart enter should handle continuing bullet list", handledBulletEnter);
        Assert.assertTrue("Text should now end with bullet continuation", doc.getText(0, doc.getLength()).trim().endsWith("•"));

        // 6. Test Smart Enter on empty bullet line (exit list)
        boolean handledExitBullet = panel.handleSmartEnter();
        Assert.assertTrue("Smart enter should handle exiting list on empty bullet", handledExitBullet);
        Assert.assertFalse("Trailing empty bullet should be removed on exit", doc.getText(0, doc.getLength()).trim().endsWith("•"));

        // 7. Test Smart Enter on Numbered List
        doc.remove(0, doc.getLength());
        doc.insertString(0, "1. Jupiter in Cancer", null);
        editor.setCaretPosition(doc.getLength());
        boolean handledNumEnter = panel.handleSmartEnter();
        Assert.assertTrue("Smart enter should handle continuing numbered list", handledNumEnter);
        Assert.assertTrue("Text should now continue with 2.", doc.getText(0, doc.getLength()).trim().endsWith("2."));

        // 8. Test Smart Enter on empty number (exit list)
        boolean handledExitNum = panel.handleSmartEnter();
        Assert.assertTrue("Smart enter should handle exiting list on empty number", handledExitNum);
        Assert.assertFalse("Trailing 2. should be removed on exit", doc.getText(0, doc.getLength()).trim().endsWith("2."));
    }

    @Test
    public void testMultiSourcePasteAndRoundTripPreservesParagraphs() throws Exception {
        Place place = new Place("Delhi", 28.61, 77.20, 5.5);
        BirthData bd = new BirthData("Test User", Sex.Male, 10, 5, 1985, 14, 30, 0, place);
        Horoscope h = new Horoscope(bd);
        app.astrosoft.ui.comp.ChartNotesPanel panel = new app.astrosoft.ui.comp.ChartNotesPanel(null);
        panel.setHoroscope(h);

        // 1. WhatsApp Multi-Paragraph Message
        String waMessage = "[10:30 AM, 24/10/2026] +91 98400 12345: Query regarding job relocation abroad.\n\n" +
                           "D1 chart: 10th HL Mercury in 9th house.\n\n" +
                           "Mars-Moon dasa chidra turbulence.\n\n" +
                           "[Bangaru Venkatesh]\n" +
                           "Very strong chart overall.";

        String cleanedWa = app.astrosoft.ui.comp.ChartNotesPanel.cleanAndSanitizeText(waMessage);
        h.setNotes(cleanedWa);
        panel.setHoroscope(h);

        String editorHtmlWa = panel.getEditor().getText();
        int pCountWa = editorHtmlWa.split("<p").length - 1;
        Assert.assertTrue("WhatsApp notes must have multiple paragraphs in editor (found " + pCountWa + ")", pCountWa >= 4);

        // Save and reload round-trip
        File tempFile = File.createTempFile("test_wa_roundtrip_", ".ash");
        tempFile.deleteOnExit();
        h.saveToFile(tempFile.getAbsolutePath());

        Horoscope reloadedH = Horoscope.createFromFile(tempFile.getAbsolutePath());
        panel.setHoroscope(reloadedH);
        String reloadedHtmlWa = panel.getEditor().getText();
        int reloadedPCountWa = reloadedHtmlWa.split("<p").length - 1;
        Assert.assertTrue("WhatsApp notes must preserve multiple paragraphs after save/reload round-trip (found " + reloadedPCountWa + ")", reloadedPCountWa >= 4);

        // 2. Google Docs / Workspace Multi-Paragraph Content
        String gDocsContent = "“Analysis Summary”\n\n" +
                              "• Point 1: Career growth in Q3\n\n" +
                              "• Point 2: Saturn-Rahu conjunction";
        String cleanedGDocs = app.astrosoft.ui.comp.ChartNotesPanel.cleanAndSanitizeText(gDocsContent);
        h.setNotes(cleanedGDocs);
        panel.setHoroscope(h);
        int pCountDocs = panel.getEditor().getText().split("<p").length - 1;
        Assert.assertTrue("Google Docs notes must have multiple paragraphs (found " + pCountDocs + ")", pCountDocs >= 3);

        // 3. Microsoft Word Multi-Paragraph Content
        String wordContent = "'Case Study Notes': Native's career transition\n\n" +
                             "• Dasa analysis shows favorable period\n\n" +
                             "• Transit Saturn moving into 10th house";
        String cleanedWord = app.astrosoft.ui.comp.ChartNotesPanel.cleanAndSanitizeText(wordContent);
        h.setNotes(cleanedWord);
        panel.setHoroscope(h);
        int pCountWord = panel.getEditor().getText().split("<p").length - 1;
        Assert.assertTrue("Word notes must have multiple paragraphs (found " + pCountWord + ")", pCountWord >= 3);
    }

    @Test
    public void testExcelHtmlTablePasteAndXmlRoundTrip() throws Exception {
        Place place = new Place("Delhi", 28.61, 77.20, 5.5);
        BirthData bd = new BirthData("Excel Test", Sex.Male, 12, 6, 1990, 10, 15, 0, place);
        Horoscope h = new Horoscope(bd);
        app.astrosoft.ui.comp.ChartNotesPanel panel = new app.astrosoft.ui.comp.ChartNotesPanel(null);
        panel.setHoroscope(h);

        // Typical HTML clipboard snippet produced when copying from Microsoft Excel
        String excelHtml = "<html xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\">\n" +
                           "<head><style>.xl65{mso-number-format:General;text-align:left;}</style></head>\n" +
                           "<body>\n" +
                           "<!--StartFragment-->\n" +
                           "<table class=\"MsoNormalTable\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:100%\">\n" +
                           " <tr style=\"height:15pt\">\n" +
                           "  <td class=\"xl65\" style=\"font-weight:bold;\">Planet</td>\n" +
                           "  <td class=\"xl65\" style=\"font-weight:bold;\">Rasi</td>\n" +
                           "  <td class=\"xl65\" style=\"font-weight:bold;\">Degree</td>\n" +
                           " </tr>\n" +
                           " <tr style=\"height:15pt\">\n" +
                           "  <td class=\"xl65\">Jupiter</td>\n" +
                           "  <td class=\"xl65\">Cancer</td>\n" +
                           "  <td class=\"xl65\">15° 24'</td>\n" +
                           " </tr>\n" +
                           " <tr style=\"height:15pt\">\n" +
                           "  <td class=\"xl65\">Saturn</td>\n" +
                           "  <td class=\"xl65\">Aquarius</td>\n" +
                           "  <td class=\"xl65\">22° 10'</td>\n" +
                           " </tr>\n" +
                           "</table>\n" +
                           "<!--EndFragment-->\n" +
                           "</body></html>";

        // Sanitize and insert into panel
        String sanitizedTable = app.astrosoft.ui.comp.ChartNotesPanel.sanitizeHtmlContentWithTables(excelHtml);
        Assert.assertNotNull("Sanitized table should not be null", sanitizedTable);
        Assert.assertTrue("Sanitized table must contain table tag", sanitizedTable.contains("<table"));
        Assert.assertTrue("Sanitized table must have border attribute", sanitizedTable.contains("border=\"1\""));

        panel.insertHtmlTableAtCaret(sanitizedTable);
        String editorText = panel.getEditor().getText();
        Assert.assertTrue("Editor should contain table", editorText.contains("<table"));
        Assert.assertTrue("Editor should contain Jupiter cell", editorText.contains("Jupiter"));
        Assert.assertTrue("Editor should contain Saturn cell", editorText.contains("Saturn"));
        Assert.assertTrue("Editor should contain Cancer cell", editorText.contains("Cancer"));

        // Save to .ash XML file and reload
        File tempFile = File.createTempFile("test_excel_table_", ".ash");
        tempFile.deleteOnExit();
        h.setNotes(panel.getNotesContent());
        h.saveToFile(tempFile.getAbsolutePath());

        Horoscope reloaded = Horoscope.createFromFile(tempFile.getAbsolutePath());
        panel.setHoroscope(reloaded);
        String reloadedText = panel.getEditor().getText();

        Assert.assertTrue("Reloaded notes must preserve table structure", reloadedText.contains("<table"));
        Assert.assertTrue("Reloaded notes must preserve Jupiter", reloadedText.contains("Jupiter"));
        Assert.assertTrue("Reloaded notes must preserve Saturn", reloadedText.contains("Saturn"));
        Assert.assertTrue("Reloaded notes must preserve Degree", reloadedText.contains("15") && (reloadedText.contains("15°") || reloadedText.contains("15&#176;")));
        String docText = panel.getEditor().getDocument().getText(0, panel.getEditor().getDocument().getLength());
        Assert.assertTrue("Rendered document text must preserve 15° 24'", docText.contains("15° 24'"));
    }

    @Test
    public void testGoogleSheetsHtmlTablePasteAndXmlRoundTrip() throws Exception {
        Place place = new Place("Bangalore", 12.97, 77.59, 5.5);
        BirthData bd = new BirthData("Sheets Test", Sex.Female, 20, 8, 1995, 18, 45, 0, place);
        Horoscope h = new Horoscope(bd);
        app.astrosoft.ui.comp.ChartNotesPanel panel = new app.astrosoft.ui.comp.ChartNotesPanel(null);
        panel.setHoroscope(h);

        // Typical HTML clipboard snippet produced when copying from Google Sheets
        String googleSheetsHtml = "<google-sheets-html-origin><style type=\"text/css\">td {padding: 0px 3px;}</style>\n" +
                                  "<table xmlns=\"http://www.w3.org/1999/xhtml\" cellspacing=\"0\" cellpadding=\"0\" dir=\"ltr\" border=\"1\">\n" +
                                  " <tbody>\n" +
                                  "  <tr style=\"height: 21px;\">\n" +
                                  "   <td dir=\"ltr\" style=\"font-weight:bold; background-color:#f3f3f3;\">Dasa</td>\n" +
                                  "   <td dir=\"ltr\" style=\"font-weight:bold; background-color:#f3f3f3;\">Start Date</td>\n" +
                                  "   <td dir=\"ltr\" style=\"font-weight:bold; background-color:#f3f3f3;\">End Date</td>\n" +
                                  "  </tr>\n" +
                                  "  <tr style=\"height: 21px;\">\n" +
                                  "   <td dir=\"ltr\">Jupiter-Venus</td>\n" +
                                  "   <td dir=\"ltr\">2024-05-10</td>\n" +
                                  "   <td dir=\"ltr\">2027-01-09</td>\n" +
                                  "  </tr>\n" +
                                  "  <tr style=\"height: 21px;\">\n" +
                                  "   <td dir=\"ltr\">Jupiter-Sun</td>\n" +
                                  "   <td dir=\"ltr\">2027-01-09</td>\n" +
                                  "   <td dir=\"ltr\">2027-10-28</td>\n" +
                                  "  </tr>\n" +
                                  " </tbody>\n" +
                                  "</table>\n" +
                                  "</google-sheets-html-origin>";

        String sanitizedTable = app.astrosoft.ui.comp.ChartNotesPanel.sanitizeHtmlContentWithTables(googleSheetsHtml);
        Assert.assertNotNull("Sanitized Google Sheets table should not be null", sanitizedTable);
        Assert.assertTrue("Sanitized table must have table tag", sanitizedTable.contains("<table"));

        panel.insertHtmlTableAtCaret(sanitizedTable);
        String editorText = panel.getEditor().getText();
        Assert.assertTrue("Editor should contain table", editorText.contains("<table"));
        Assert.assertTrue("Editor should contain Jupiter-Venus", editorText.contains("Jupiter-Venus"));
        Assert.assertTrue("Editor should contain 2024-05-10", editorText.contains("2024-05-10"));

        // Save and reload round-trip
        File tempFile = File.createTempFile("test_sheets_table_", ".ash");
        tempFile.deleteOnExit();
        h.setNotes(panel.getNotesContent());
        h.saveToFile(tempFile.getAbsolutePath());

        Horoscope reloaded = Horoscope.createFromFile(tempFile.getAbsolutePath());
        panel.setHoroscope(reloaded);
        String reloadedText = panel.getEditor().getText();

        Assert.assertTrue("Reloaded notes must preserve table structure", reloadedText.contains("<table"));
        Assert.assertTrue("Reloaded notes must contain Jupiter-Venus", reloadedText.contains("Jupiter-Venus"));
        Assert.assertTrue("Reloaded notes must contain Jupiter-Sun", reloadedText.contains("Jupiter-Sun"));
    }

    @Test
    public void testTabDelimitedPlainTextSpreadsheetTableConversion() throws Exception {
        String tsvData = "House\tSign\tLord\n" +
                         "1st\tAries\tMars\n" +
                         "2nd\tTaurus\tVenus\n" +
                         "3rd\tGemini\tMercury";

        // Verify table detection
        Assert.assertTrue("TSV spreadsheet data must be recognized as table",
                app.astrosoft.ui.comp.ChartNotesPanel.isTabDelimitedTable(tsvData));

        // Verify non-table text is rejected
        String normalTextWithOneTab = "First line with no tab\n" +
                                      "\tIndented bullet line\n" +
                                      "Third line without tab";
        Assert.assertFalse("Normal indented text must not be recognized as table",
                app.astrosoft.ui.comp.ChartNotesPanel.isTabDelimitedTable(normalTextWithOneTab));

        // Convert TSV to HTML Table
        String tableHtml = app.astrosoft.ui.comp.ChartNotesPanel.convertTabDelimitedToHtmlTable(tsvData);
        Assert.assertTrue("Generated HTML must contain <table>", tableHtml.contains("<table"));
        Assert.assertTrue("Header row should use <th>", tableHtml.contains("<th"));
        Assert.assertTrue("Header should contain House", tableHtml.contains("House"));
        Assert.assertTrue("Data rows should use <td>", tableHtml.contains("<td>Mars</td>"));

        // Test insertion in ChartNotesPanel
        Place place = new Place("Mumbai", 19.07, 72.87, 5.5);
        BirthData bd = new BirthData("TSV Test", Sex.Male, 5, 3, 1988, 8, 30, 0, place);
        Horoscope h = new Horoscope(bd);
        app.astrosoft.ui.comp.ChartNotesPanel panel = new app.astrosoft.ui.comp.ChartNotesPanel(null);
        panel.setHoroscope(h);

        panel.insertHtmlTableAtCaret(tableHtml);
        String editorText = panel.getEditor().getText();
        Assert.assertTrue("Editor should render converted TSV table", editorText.contains("<table"));
        Assert.assertTrue("Editor should contain Aries", editorText.contains("Aries"));
        Assert.assertTrue("Editor should contain Mars", editorText.contains("Mars"));

        // Save and reload round-trip
        File tempFile = File.createTempFile("test_tsv_table_", ".ash");
        tempFile.deleteOnExit();
        h.setNotes(panel.getNotesContent());
        h.saveToFile(tempFile.getAbsolutePath());

        Horoscope reloaded = Horoscope.createFromFile(tempFile.getAbsolutePath());
        panel.setHoroscope(reloaded);
        String reloadedText = panel.getEditor().getText();
        Assert.assertTrue("Reloaded notes must preserve converted TSV table", reloadedText.contains("<table"));
        Assert.assertTrue("Reloaded notes must contain Taurus", reloadedText.contains("Taurus"));
    }

    @Test
    public void testMixedNotesWithParagraphsAndSpreadsheetTables() throws Exception {
        Place place = new Place("Chennai", 13.08, 80.27, 5.5);
        BirthData bd = new BirthData("Mixed Test", Sex.Female, 15, 11, 1992, 14, 20, 0, place);
        Horoscope h = new Horoscope(bd);
        app.astrosoft.ui.comp.ChartNotesPanel panel = new app.astrosoft.ui.comp.ChartNotesPanel(null);
        panel.setHoroscope(h);

        String mixedContent = "<html><body>\n" +
                              "<p>Initial consultation observation: Native's 10th lord is well placed in 9th house.</p>\n" +
                              "<table border=\"1\" cellpadding=\"4\" cellspacing=\"0\">\n" +
                              " <tr><th>Planet</th><th>Strength</th></tr>\n" +
                              " <tr><td>Sun</td><td>6.5 Rupas</td></tr>\n" +
                              " <tr><td>Moon</td><td>7.2 Rupas</td></tr>\n" +
                              "</table>\n" +
                              "<p>Remedial recommendation: Wear Yellow Sapphire during Jupiter Mahadasha.</p>\n" +
                              "</body></html>";

        panel.setHoroscope(h);
        h.setNotes(mixedContent);
        panel.setHoroscope(h);

        String editorText = panel.getEditor().getText();
        Assert.assertTrue("Editor must preserve leading paragraph", editorText.contains("Initial consultation observation"));
        Assert.assertTrue("Editor must preserve table", editorText.contains("<table"));
        Assert.assertTrue("Editor must preserve table data", editorText.contains("6.5 Rupas"));
        Assert.assertTrue("Editor must preserve trailing paragraph", editorText.contains("Remedial recommendation"));

        // Save to XML and reload
        File tempFile = File.createTempFile("test_mixed_table_", ".ash");
        tempFile.deleteOnExit();
        h.saveToFile(tempFile.getAbsolutePath());

        Horoscope reloaded = Horoscope.createFromFile(tempFile.getAbsolutePath());
        panel.setHoroscope(reloaded);
        String reloadedText = panel.getEditor().getText();

        Assert.assertTrue("Reloaded mixed notes must preserve paragraph 1", reloadedText.contains("Initial consultation observation"));
        Assert.assertTrue("Reloaded mixed notes must preserve table", reloadedText.contains("<table"));
        Assert.assertTrue("Reloaded mixed notes must preserve table contents", reloadedText.contains("7.2 Rupas"));
        Assert.assertTrue("Reloaded mixed notes must preserve paragraph 2", reloadedText.contains("Remedial recommendation"));
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
