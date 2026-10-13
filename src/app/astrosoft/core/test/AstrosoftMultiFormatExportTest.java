package app.astrosoft.core.test;

import java.io.File;
import java.util.concurrent.FutureTask;

import junit.framework.TestCase;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.PanchangList;
import app.astrosoft.export.AstrosoftExporter;
import app.astrosoft.export.AstrosoftExporter.Type;

public class AstrosoftMultiFormatExportTest extends TestCase {

    private File tempDir;
    private Horoscope horoscope;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tempDir = new File(System.getProperty("java.io.tmpdir"), "astrosoft_multi_test_" + System.currentTimeMillis());
        tempDir.mkdirs();
        horoscope = Horoscope.createFromFile("samples/suba.ash");
        assertNotNull("Horoscope should be loaded from sample", horoscope);
    }

    @Override
    protected void tearDown() throws Exception {
        if (tempDir != null && tempDir.exists()) {
            for (File f : tempDir.listFiles()) {
                f.delete();
            }
            tempDir.delete();
        }
        super.tearDown();
    }

    public void testExportPdfOnly() throws Exception {
        String base = new File(tempDir, "test_pdf_only").getAbsolutePath();
        FutureTask<Object> task = AstrosoftExporter.export(Type.Horosocope, horoscope, base, true, false, false);
        task.get();

        File pdf = new File(base + ".pdf");
        File md = new File(base + ".md");
        File xml = new File(base + ".xml");

        assertTrue("PDF file should exist", pdf.exists() && pdf.length() > 0);
        assertFalse("MD file should not exist when not selected", md.exists());
        assertFalse("XML file should not exist when not selected", xml.exists());
    }

    public void testExportMarkdownOnly() throws Exception {
        String base = new File(tempDir, "test_md_only").getAbsolutePath();
        FutureTask<Object> task = AstrosoftExporter.export(Type.Horosocope, horoscope, base, false, true, false);
        task.get();

        File pdf = new File(base + ".pdf");
        File md = new File(base + ".md");
        File xml = new File(base + ".xml");

        assertTrue("MD file should exist", md.exists() && md.length() > 0);
        assertFalse("PDF file should not exist when not selected", pdf.exists());
        assertFalse("XML file should not exist when not selected", xml.exists());
    }

    public void testExportXmlOnly() throws Exception {
        String base = new File(tempDir, "test_xml_only").getAbsolutePath();
        FutureTask<Object> task = AstrosoftExporter.export(Type.Horosocope, horoscope, base, false, false, true);
        task.get();

        File pdf = new File(base + ".pdf");
        File md = new File(base + ".md");
        File xml = new File(base + ".xml");

        assertTrue("XML file should exist", xml.exists() && xml.length() > 0);
        assertFalse("PDF file should not exist when not selected", pdf.exists());
        assertFalse("MD file should not exist when not selected", md.exists());
    }

    public void testExportPdfAndMarkdown() throws Exception {
        String base = new File(tempDir, "test_pdf_md").getAbsolutePath();
        FutureTask<Object> task = AstrosoftExporter.export(Type.Horosocope, horoscope, base, true, true, false);
        task.get();

        File pdf = new File(base + ".pdf");
        File md = new File(base + ".md");
        File xml = new File(base + ".xml");

        assertTrue("PDF file should exist", pdf.exists() && pdf.length() > 0);
        assertTrue("MD file should exist", md.exists() && md.length() > 0);
        assertFalse("XML file should not exist when not selected", xml.exists());
    }

    public void testExportAllThreeFormats() throws Exception {
        String base = new File(tempDir, "test_all_three").getAbsolutePath();
        FutureTask<Object> task = AstrosoftExporter.export(Type.Horosocope, horoscope, base, true, true, true);
        task.get();

        File pdf = new File(base + ".pdf");
        File md = new File(base + ".md");
        File xml = new File(base + ".xml");

        assertTrue("PDF file should exist", pdf.exists() && pdf.length() > 0);
        assertTrue("MD file should exist", md.exists() && md.length() > 0);
        assertTrue("XML file should exist", xml.exists() && xml.length() > 0);
    }

    public void testPanchangMultiFormatExport() throws Exception {
        String base = new File(tempDir, "test_panchang_md_xml").getAbsolutePath();
        PanchangList panchang = new PanchangList(2026, 9);
        FutureTask<Object> task = AstrosoftExporter.export(Type.Panchang, panchang, base, false, true, true);
        task.get();

        File pdf = new File(base + ".pdf");
        File md = new File(base + ".md");
        File xml = new File(base + ".xml");

        assertFalse("PDF file should not exist when not selected", pdf.exists());
        assertTrue("MD file should exist", md.exists() && md.length() > 0);
        assertTrue("XML file should exist", xml.exists() && xml.length() > 0);
    }
}
