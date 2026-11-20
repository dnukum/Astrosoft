package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.comp.ChartNotesPanel;
import app.astrosoft.ui.util.MarkdownParser;

public class MarkdownParserTest {

    @Test
    public void testIsMarkdownDetection() {
        // Plain text should NOT be detected as Markdown
        assertFalse(MarkdownParser.isMarkdown("Hello world, this is a plain note."));
        assertFalse(MarkdownParser.isMarkdown("Just some simple lines\nwith multiple sentences."));

        // Markdown tables should be detected
        assertTrue(MarkdownParser.isMarkdown("| Dasa | Start | End |\n| --- | --- | --- |\n| Sa/Sa | 2024 | 2027 |"));

        // Headings
        assertTrue(MarkdownParser.isMarkdown("### Saturn Dasa Analysis\nDetailed observations."));

        // Bold & lists
        assertTrue(MarkdownParser.isMarkdown("- **Saturn**: Strong placement\n- **Jupiter**: Well placed"));

        // Numbered list
        assertTrue(MarkdownParser.isMarkdown("1. First point\n2. Second point\n3. Third point"));

        // Blockquotes and dividers
        assertTrue(MarkdownParser.isMarkdown("> Important quote from Brihat Parasara\n---\nAdditional note"));
    }

    @Test
    public void testTableConversion() {
        String md = "| Dasa | Start | End |\n| :--- | :--- | :--- |\n| Sa/Sa | 17-Jan-2024 | 20-Jan-2027 |\n| Sa/Me | 20-Jan-2027 | 30-Sep-2029 |";
        String html = MarkdownParser.toHtml(md);
        assertNotNull(html);
        assertTrue("Must contain table tag", html.contains("<table"));
        assertTrue("Must contain th tags", html.contains(">Dasa</th>") && html.contains(">Start</th>"));
        assertTrue("Must contain td tags", html.contains(">Sa/Sa</td>") && html.contains(">17-Jan-2024</td>"));
    }

    @Test
    public void testHeadingsConversion() {
        String md = "# Title 1\n## Subtitle 2\n### Section 3";
        String html = MarkdownParser.toHtml(md);
        assertNotNull(html);
        assertTrue("Heading 1 check", html.contains("<h1") || html.contains("font-size: 18pt") || html.contains("<b>Title 1</b>"));
        assertTrue("Heading 2 check", html.contains("<h2") || html.contains("font-size: 15pt") || html.contains("<b>Subtitle 2</b>"));
        assertTrue("Heading 3 check", html.contains("<h3") || html.contains("<b>Section 3</b>"));
    }

    @Test
    public void testInlineFormatting() {
        String md = "This is **bold** and *italic* and `code` test.";
        String html = MarkdownParser.toHtml(md);
        assertNotNull(html);
        assertTrue("Bold conversion", html.contains("<b>bold</b>") || html.contains("<strong>bold</strong>"));
        assertTrue("Italic conversion", html.contains("<i>italic</i>") || html.contains("<em>italic</em>"));
        assertTrue("Code conversion", html.contains("<code>code</code>"));
    }

    @Test
    public void testListsConversion() {
        String md = "- First bullet\n- Second bullet with **bold**\n- Third bullet";
        String html = MarkdownParser.toHtml(md);
        assertNotNull(html);
        assertTrue("Unordered list check", html.contains("<ul>") && html.contains("</ul>"));
        assertTrue("List item check", html.contains("<li>First bullet</li>"));
        assertTrue("Bold inside list check", html.contains("<b>bold</b>") || html.contains("<strong>bold</strong>"));

        String numMd = "1. Item one\n2. Item two";
        String numHtml = MarkdownParser.toHtml(numMd);
        assertNotNull(numHtml);
        assertTrue("Ordered list check", numHtml.contains("<ol>") && numHtml.contains("</ol>"));
        assertTrue("Ordered item check", numHtml.contains("<li>Item one</li>"));
    }

    @Test
    public void testBlockquoteAndDivider() {
        String md = "> Auspicious Muhurta\n---\nObservations";
        String html = MarkdownParser.toHtml(md);
        assertNotNull(html);
        assertTrue("Blockquote check", html.contains("<blockquote>") && html.contains("Auspicious Muhurta"));
        assertTrue("Horizontal rule check", html.contains("<hr"));
    }

    @Test
    public void testChartNotesPanelMarkdownPasteIntegration() throws Exception {
        Horoscope h = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_1.ash");
        ChartNotesPanel panel = new ChartNotesPanel();
        panel.setHoroscope(h);

        String sampleMd = "### Saturn Dasa\n| Dasa | Start | End |\n| --- | --- | --- |\n| Sa/Sa | 2024 | 2027 |\n\n- **Status**: Excellent";
        panel.insertHtmlAtCaret(MarkdownParser.toHtml(sampleMd));

        String notes = h.getNotes();
        assertNotNull("Notes must not be null", notes);
        assertTrue("Notes should contain parsed table", notes.contains("<table") && notes.contains("Sa/Sa"));
        assertTrue("Notes should contain bold status", notes.contains("<b>Status</b>") || notes.contains("<strong>Status</strong>"));
        assertFalse("Notes should NOT contain raw markdown pipes table syntax", notes.contains("| --- |"));
    }

    @Test
    public void testNestedListsAndContinuationParagraphs() {
        String geminiMd =
                "### **1\\. Career &amp; Professional Growth**\n\n" +
                "*   **Prediction:**\n" +
                "    Career dissatisfaction and feelings of stagnation will persist through early 2027\\.\n\n" +
                "*   **Astrological Reasoning:**\n" +
                "    *   **Current Restlessness:** Running **Moon/Rahu Dasa** with Rahu in Lagna.\n" +
                "    *   **Financial Fortification:** 11th Bhava holds 37 bindus in SAV.\n\n" +
                "---\n\n" +
                "### 2. Marital & Relationship Life\n\n" +
                "1. **Primary Factor:** Venus in 6th house.\n" +
                "   * **Mitigation:** Jupiter aspect stabilizes the bond.";

        assertTrue("Gemini markdown must be detected", MarkdownParser.isMarkdown(geminiMd));
        assertTrue("Gemini markdown must have explicit syntax", MarkdownParser.hasExplicitMarkdownSyntax(geminiMd));

        String html = MarkdownParser.toHtml(geminiMd);
        assertTrue("Must unescape 1\\. and avoid double-escaping &amp;", html.contains("<h3><b>1. Career &amp; Professional Growth</b></h3>"));
        assertFalse("Must not contain literal backslash-dot", html.contains("\\."));
        assertFalse("Must not double-escape &amp;", html.contains("&amp;amp;"));
        assertTrue("Must contain nested ul inside li", html.contains("<ul><li><b>Prediction:</b><br>Career dissatisfaction"));
        assertTrue("Must nest sub-bullets inside Astrological Reasoning li",
                html.contains("<li><b>Astrological Reasoning:</b><ul><li><b>Current Restlessness:</b>"));
        assertTrue("Must contain hr divider", html.contains("<hr>"));
        assertTrue("Must support ol with nested ul", html.contains("<ol><li><b>Primary Factor:</b> Venus in 6th house.<ul><li><b>Mitigation:</b>"));
    }

    @Test
    public void testInsertMultiBlockHtmlIntoExistingNotes() throws Exception {
        Horoscope h = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_1.ash");
        h.setNotes("<html><body><p>Existing consultation question.</p></body></html>");
        ChartNotesPanel panel = new ChartNotesPanel();
        panel.setHoroscope(h);

        String geminiMd = "### 1. Career Outlook\n\n* **Prediction:** Steady progress.\n  * **Trigger:** Jupiter transit.\n\n---\n\n### 2. Remedies\n\n* Chant daily.";
        panel.insertHtmlAtCaret(MarkdownParser.toHtml(geminiMd));

        String notes = h.getNotes();
        assertNotNull(notes);
        assertTrue("Existing note must remain", notes.contains("Existing consultation question."));
        assertTrue("Heading must be inserted as HTML", notes.contains("<h3>") && notes.contains("1. Career Outlook"));
        assertTrue("Nested list must be preserved", notes.contains("<ul>") && notes.contains("<li>"));
        assertTrue("Horizontal rule must be preserved", notes.contains("<hr"));
        assertFalse("Raw HTML tags must not be escaped as literal text", notes.contains("&lt;h3&gt;"));

        org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(notes);
        assertTrue("h3 must be direct child of body (not nested inside p or li)",
                doc.select("p h3, li h3, h3 ul, li hr").isEmpty());
    }
}
