package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JTextPane;

import org.junit.Test;

import app.astrosoft.beans.BirthData;
import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.comp.ChartNotesPanel;

public class ChartNotesImagePasteTest {

    @Test
    public void testImageDownscalingAndCompression() throws Exception {
        // 1. Create a large simulated Retina screenshot (1000 x 600)
        BufferedImage largeImg = new BufferedImage(1000, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = largeImg.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 1000, 600);
        g.dispose();

        BufferedImage scaled = ChartNotesPanel.prepareImageForNotes(largeImg, 500);
        assertNotNull("Scaled image must not be null", scaled);
        assertEquals("Width must be scaled down to 500px", 500, scaled.getWidth());
        assertEquals("Height must maintain aspect ratio (300px)", 300, scaled.getHeight());

        // 2. Small image (300 x 200) should NOT be upscaled
        BufferedImage smallImg = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        BufferedImage notUpscaled = ChartNotesPanel.prepareImageForNotes(smallImg, 500);
        assertEquals("Width should remain 300px", 300, notUpscaled.getWidth());
        assertEquals("Height should remain 200px", 200, notUpscaled.getHeight());

        // 3. Convert to Base64 PNG and check compression
        String b64 = ChartNotesPanel.encodeImageToBase64Png(scaled);
        assertNotNull("Base64 string must not be null", b64);
        assertTrue("Base64 string should be non-empty", b64.length() > 100);

        // Decode back and verify validity
        byte[] bytes = Base64.getDecoder().decode(b64);
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull("Decoded image must be valid", decoded);
        assertEquals(500, decoded.getWidth());
        assertEquals(300, decoded.getHeight());
    }

    @Test
    public void testInsertImageAtCaretAndSyncToOwner() throws Exception {
        Horoscope h = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_1.ash");
        assertNotNull("Horoscope must load", h);

        ChartNotesPanel panel = new ChartNotesPanel();
        panel.setHoroscope(h);

        // Create sample image (80 x 60)
        BufferedImage testImg = new BufferedImage(80, 60, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = testImg.createGraphics();
        g.setColor(Color.MAGENTA);
        g.fillRect(0, 0, 80, 60);
        g.dispose();

        panel.insertImageAtCaret(testImg);

        String notes = h.getNotes();
        assertNotNull("Horoscope notes must be populated", notes);
        assertTrue("Notes must contain img tag with data:image/png;base64",
                notes.contains("<img") && notes.contains("data:image/png;base64,"));
        assertTrue("Notes must specify width and height", notes.contains("width=\"80\"") && notes.contains("height=\"60\""));
    }

    @Test
    public void testCustomImageViewRendersBase64Image() throws Exception {
        Horoscope h = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_1.ash");
        ChartNotesPanel panel = new ChartNotesPanel();
        panel.setHoroscope(h);

        // Create a 50x50 pure green image
        BufferedImage greenImg = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = greenImg.createGraphics();
        g.setColor(new Color(0, 200, 0));
        g.fillRect(0, 0, 50, 50);
        g.dispose();

        panel.insertImageAtCaret(greenImg);

        // Pack panel and paint onto a canvas
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.setSize(450, 400);
        frame.pack();

        BufferedImage canvas = new BufferedImage(450, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D gCanvas = canvas.createGraphics();
        panel.paint(gCanvas);
        gCanvas.dispose();
        frame.dispose();

        // Verify that green pixels from the image were rendered
        int greenPixels = 0;
        for (int y = 0; y < 400; y++) {
            for (int x = 0; x < 450; x++) {
                int rgb = canvas.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int gVal = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (gVal > 180 && r < 50 && b < 50) {
                    greenPixels++;
                }
            }
        }
        assertTrue("Pasted image should render green pixels on panel (found " + greenPixels + ")", greenPixels > 100);
    }

    @Test
    public void testNotesXmlPersistenceWithEmbeddedImage() throws Exception {
        // Create sample birth data with an embedded image in notes
        BufferedImage testImg = new BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB);
        String b64 = ChartNotesPanel.encodeImageToBase64Png(testImg);
        String imgHtml = "<p>Chart observation:</p><p><img src=\"data:image/png;base64," + b64 + "\" width=\"40\" height=\"30\"></p>";

        BirthData bd = new BirthData("Test Person", null, java.util.Calendar.getInstance(), app.astrosoft.beans.Place.getDefault());
        bd.setNotes(imgHtml);

        // Serialize to XML Element
        org.w3c.dom.Document xmlDoc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        org.w3c.dom.Element elem = bd.toXMLElement(xmlDoc, "BirthData");
        assertNotNull("BirthData XML element must be created", elem);

        // Deserialize back from XML node
        BirthData loadedBd = BirthData.valueOfXMLNode(elem);
        assertNotNull("Loaded BirthData must not be null", loadedBd);
        assertEquals("Loaded notes must preserve image HTML", imgHtml, loadedBd.getNotes());
    }

    @Test
    public void testPastingImageDoesNotClobberPrecedingLines() throws Exception {
        Horoscope h = SampleHoroscopes.getHoroscope("tests/charts/sample_chart_1.ash");
        ChartNotesPanel panel = new ChartNotesPanel();
        panel.setHoroscope(h);

        String initialHtml = "<p>Client Details</p>"
                + "<p>Line 1</p>"
                + "<p>Line 2</p>"
                + "<p>Line 7</p>"
                + "<p>Family Lineage</p>";
        panel.getEditor().setText(initialHtml);

        javax.swing.text.Document doc = panel.getEditor().getDocument();
        String fullText = doc.getText(0, doc.getLength());
        int pos7 = fullText.indexOf("Line 7");
        int posAfter7 = fullText.indexOf("\n", pos7);
        if (posAfter7 == -1) posAfter7 = pos7 + "Line 7".length();
        panel.getEditor().setCaretPosition(posAfter7);

        // Paste an image of 300x200
        BufferedImage dummy = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        panel.insertImageAtCaret(dummy);

        // Verify document structure and view hierarchy
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.setSize(500, 800);
        frame.pack();

        BufferedImage canvas = new BufferedImage(500, 800, BufferedImage.TYPE_INT_RGB);
        Graphics2D gCanvas = canvas.createGraphics();
        panel.paint(gCanvas);
        gCanvas.dispose();
        frame.dispose();

        // Crucial invariant: The image must not be placed at negative offset or above preceding line
        javax.swing.text.View rootView = panel.getEditor().getUI().getRootView(panel.getEditor());
        java.awt.Rectangle rootAlloc = new java.awt.Rectangle(0, 0, 500, 800);
        java.awt.Rectangle imgAlloc = findViewBounds(rootView, rootAlloc, ChartNotesPanel.Base64ImageView.class);
        assertNotNull("Base64ImageView must be present in view tree", imgAlloc);

        int line7Pos = panel.getEditor().getDocument().getText(0, panel.getEditor().getDocument().getLength()).indexOf("Line 7");
        java.awt.Shape line7Shape = panel.getEditor().modelToView2D(line7Pos);
        assertNotNull("Line 7 shape must exist", line7Shape);

        assertTrue("Image Y (" + imgAlloc.y + ") must be below Line 7 Y (" + line7Shape.getBounds().y + ")",
                imgAlloc.y >= line7Shape.getBounds().y);

        // Verify that all original text sections still exist in the document
        String resultText = panel.getEditor().getDocument().getText(0, panel.getEditor().getDocument().getLength());
        assertTrue(resultText.contains("Client Details"));
        assertTrue(resultText.contains("Line 1"));
        assertTrue(resultText.contains("Line 2"));
        assertTrue(resultText.contains("Line 7"));
        assertTrue(resultText.contains("Family Lineage"));
    }

    private java.awt.Rectangle findViewBounds(javax.swing.text.View v, java.awt.Shape alloc, Class<?> targetClass) {
        if (targetClass.isInstance(v)) {
            return (alloc != null) ? alloc.getBounds() : null;
        }
        for (int i = 0; i < v.getViewCount(); i++) {
            java.awt.Shape childAlloc = null;
            if (alloc != null) {
                try {
                    childAlloc = v.getChildAllocation(i, alloc);
                } catch (Exception ignored) {}
            }
            java.awt.Rectangle res = findViewBounds(v.getView(i), childAlloc, targetClass);
            if (res != null) return res;
        }
        return null;
    }
}
