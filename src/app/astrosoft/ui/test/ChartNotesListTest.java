package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.swing.JTextPane;
import javax.swing.text.Document;

import org.junit.Before;
import org.junit.Test;

import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.comp.ChartNotesPanel;

public class ChartNotesListTest {

    private ChartNotesPanel panel;
    private JTextPane editor;

    @Before
    public void setUp() throws Exception {
        panel = new ChartNotesPanel(null);
        Horoscope h = SampleHoroscopes.getHoroscope("samples/suba.ash");
        panel.bindNotesOwner(h);

        java.lang.reflect.Field f = ChartNotesPanel.class.getDeclaredField("editor");
        f.setAccessible(true);
        editor = (JTextPane) f.get(panel);
    }

    private String getDocText() throws Exception {
        Document doc = editor.getDocument();
        return doc.getText(0, doc.getLength());
    }

    @Test
    public void testBulletOnEmptyDoc() throws Exception {
        editor.setText("");
        editor.setCaretPosition(0);
        panel.toggleList(false);

        String text = getDocText().trim();
        assertEquals("•", text);

        // Toggle off
        panel.toggleList(false);
        assertEquals("", getDocText().trim());
    }

    @Test
    public void testNumberedOnEmptyDoc() throws Exception {
        editor.setText("");
        editor.setCaretPosition(0);
        panel.toggleList(true);

        String text = getDocText().trim();
        assertEquals("1.", text);

        // Toggle off
        panel.toggleList(true);
        assertEquals("", getDocText().trim());
    }

    @Test
    public void testBulletOnTextLine() throws Exception {
        editor.setText("Important observation");
        editor.setCaretPosition(0);
        panel.toggleList(false);

        assertTrue("Expected bullet prefix, got: " + getDocText(), getDocText().contains("• Important observation"));

        // Toggle off
        panel.toggleList(false);
        assertFalse("Expected bullet removed, got: " + getDocText(), getDocText().contains("•"));
        assertTrue(getDocText().contains("Important observation"));
    }

    @Test
    public void testNumberedOnTextLine() throws Exception {
        editor.setText("First observation");
        editor.setCaretPosition(0);
        panel.toggleList(true);

        assertTrue("Expected numbered prefix, got: " + getDocText(), getDocText().contains("1. First observation"));

        // Switch to bullet
        panel.toggleList(false);
        assertTrue("Expected bullet prefix, got: " + getDocText(), getDocText().contains("• First observation"));
        assertFalse(getDocText().contains("1."));

        // Toggle off
        panel.toggleList(false);
        assertFalse(getDocText().contains("•"));
        assertTrue(getDocText().contains("First observation"));
    }

    @Test
    public void testMultiLineSelectionFormatting() throws Exception {
        editor.setText("<p>Point A</p><p>Point B</p><p>Point C</p>");
        editor.selectAll();
        panel.toggleList(true);

        String text = getDocText();
        assertTrue(text.contains("1. Point A"));
        assertTrue(text.contains("2. Point B"));
        assertTrue(text.contains("3. Point C"));

        // Toggle off
        editor.selectAll();
        panel.toggleList(true);
        String clearedText = getDocText();
        assertFalse(clearedText.contains("1."));
        assertFalse(clearedText.contains("2."));
        assertFalse(clearedText.contains("3."));
        assertTrue(clearedText.contains("Point A"));
        assertTrue(clearedText.contains("Point B"));
        assertTrue(clearedText.contains("Point C"));
    }

    @Test
    public void testSmartEnterBulletContinuationAndExit() throws Exception {
        editor.setText("• First item");
        editor.setCaretPosition(editor.getDocument().getLength());

        boolean handled = panel.handleSmartEnter();
        assertTrue(handled);
        assertTrue("Doc should have second bullet, got: " + getDocText(), getDocText().contains("• First item\n• "));

        // Smart enter again on empty bullet line exits list
        boolean exitHandled = panel.handleSmartEnter();
        assertTrue(exitHandled);
        String finalDoc = getDocText().trim();
        assertEquals("• First item", finalDoc);
    }

    @Test
    public void testSmartEnterNumberedContinuationAndExit() throws Exception {
        editor.setText("1. Step one");
        editor.setCaretPosition(editor.getDocument().getLength());

        boolean handled = panel.handleSmartEnter();
        assertTrue(handled);
        assertTrue("Doc should have step two, got: " + getDocText(), getDocText().contains("1. Step one\n2. "));

        // Smart enter again on empty numbered line exits list
        boolean exitHandled = panel.handleSmartEnter();
        assertTrue(exitHandled);
        String finalDoc = getDocText().trim();
        assertEquals("1. Step one", finalDoc);
    }

    @Test
    public void testNotesFontSizeIncreaseDecreaseReset() throws Exception {
        panel.resetFontSize();
        assertEquals(0, ChartNotesPanel.getFontOffset());
        assertEquals(13, ChartNotesPanel.getCurrentFontSize());
        assertEquals(13, editor.getFont().getSize());

        // Increase by 1
        panel.adjustFontSize(1);
        assertEquals(1, ChartNotesPanel.getFontOffset());
        assertEquals(14, ChartNotesPanel.getCurrentFontSize());
        assertEquals(14, editor.getFont().getSize());

        // Increase by 2 more
        panel.adjustFontSize(2);
        assertEquals(3, ChartNotesPanel.getFontOffset());
        assertEquals(16, ChartNotesPanel.getCurrentFontSize());
        assertEquals(16, editor.getFont().getSize());

        // Decrease by 5
        panel.adjustFontSize(-5);
        assertEquals(-2, ChartNotesPanel.getFontOffset());
        assertEquals(11, ChartNotesPanel.getCurrentFontSize());
        assertEquals(11, editor.getFont().getSize());

        // Reset
        panel.resetFontSize();
        assertEquals(0, ChartNotesPanel.getFontOffset());
        assertEquals(13, ChartNotesPanel.getCurrentFontSize());
        assertEquals(13, editor.getFont().getSize());
    }

    @Test
    public void testNotesFontSizePersistsAcrossNotesContent() throws Exception {
        panel.resetFontSize();
        panel.adjustFontSize(3); // 16pt

        editor.setText("<p>Testing content reload</p>");
        assertEquals(16, ChartNotesPanel.getCurrentFontSize());
        assertEquals(16, editor.getFont().getSize());

        panel.resetFontSize();
    }

    @Test
    public void testNotesFontSizeBounds() throws Exception {
        panel.resetFontSize();
        // Test lower bound (-4 -> 9pt)
        panel.adjustFontSize(-20);
        assertEquals(-4, ChartNotesPanel.getFontOffset());
        assertEquals(9, ChartNotesPanel.getCurrentFontSize());

        // Test upper bound (+11 -> 24pt)
        panel.adjustFontSize(30);
        assertEquals(11, ChartNotesPanel.getFontOffset());
        assertEquals(24, ChartNotesPanel.getCurrentFontSize());

        panel.resetFontSize();
    }

    @Test
    public void testTabAndDeTabSingleLine() throws Exception {
        editor.setText("<p>Normal note line</p>");
        editor.setCaretPosition(1);

        // Tab (indent)
        panel.handleTab(false);
        String text = getDocText();
        assertTrue("Expected indented line with nbsp, got: [" + text + "]", text.contains("\u00A0\u00A0\u00A0\u00A0Normal note line"));

        // De-tab (outdent)
        panel.handleTab(true);
        String outdented = getDocText();
        assertTrue("Expected outdented line, got: [" + outdented + "]", outdented.contains("Normal note line"));
        assertFalse(outdented.contains("\u00A0"));
    }

    @Test
    public void testTabAndDeTabMultiLine() throws Exception {
        editor.setText("<p>Alpha</p><p>Beta</p><p>Gamma</p>");
        editor.selectAll();

        // Indent all lines
        panel.handleTab(false);
        String text = getDocText();
        assertTrue(text.contains("\u00A0\u00A0\u00A0\u00A0Alpha"));
        assertTrue(text.contains("\u00A0\u00A0\u00A0\u00A0Beta"));
        assertTrue(text.contains("\u00A0\u00A0\u00A0\u00A0Gamma"));

        // De-tab all lines
        editor.selectAll();
        panel.handleTab(true);
        String outdented = getDocText();
        assertTrue(outdented.contains("Alpha"));
        assertTrue(outdented.contains("Beta"));
        assertTrue(outdented.contains("Gamma"));
        assertFalse(outdented.contains("\u00A0"));
    }

    @Test
    public void testTabAndDeTabBulletList() throws Exception {
        editor.setText("<p>• Sub-item</p>");
        editor.setCaretPosition(1);

        panel.handleTab(false);
        String text = getDocText();
        assertTrue("Expected indented bullet, got: [" + text + "]", text.contains("\u00A0\u00A0\u00A0\u00A0• Sub-item"));

        panel.handleTab(true);
        String outdented = getDocText();
        assertTrue("Expected outdented bullet, got: [" + outdented + "]", outdented.contains("• Sub-item"));
        assertFalse(outdented.contains("\u00A0"));
    }

    @Test
    public void testSmartEnterPreservesIndentation() throws Exception {
        editor.setText("<p>\u00A0\u00A0\u00A0\u00A0• Indented task</p>");
        editor.setCaretPosition(editor.getDocument().getLength());

        boolean handled = panel.handleSmartEnter();
        assertTrue(handled);
        String text = getDocText();
        assertTrue("Should preserve 4 nbsp on next bullet, got: [" + text + "]", text.contains("\u00A0\u00A0\u00A0\u00A0• Indented task\n\u00A0\u00A0\u00A0\u00A0• "));

        // Smart enter again exits indented list
        boolean exitHandled = panel.handleSmartEnter();
        assertTrue(exitHandled);
        String finalDoc = getDocText().trim();
        assertEquals("\u00A0\u00A0\u00A0\u00A0• Indented task", finalDoc);
    }
}
