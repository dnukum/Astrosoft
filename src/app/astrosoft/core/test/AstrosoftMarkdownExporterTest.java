package app.astrosoft.core.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import app.astrosoft.export.AstrosoftMarkdownExporter;

public class AstrosoftMarkdownExporterTest extends TestCase {

    private File tempDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tempDir = new File(System.getProperty("java.io.tmpdir"), "astrosoft_md_test_" + System.currentTimeMillis());
        tempDir.mkdirs();
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

    public void testHoroscopeExportAndXmlEquivalence() throws Exception {
        File xmlFile = new File("samples/Raja_Horoscope.xml");
        assertTrue("Sample XML must exist", xmlFile.exists());

        File mdFile = new File(tempDir, "Raja_Horoscope.md");
        AstrosoftMarkdownExporter.exportToMarkdown(xmlFile.getAbsolutePath(), mdFile.getAbsolutePath());
        assertTrue("Markdown file must be generated", mdFile.exists() && mdFile.length() > 0);

        String mdContent = readEntireFile(mdFile);

        // Verify XML to Markdown equivalence
        verifyXmlEquivalence(xmlFile, mdContent);
    }

    public void testAllSampleHoroscopesBatch() throws Exception {
        File samplesDir = new File("samples");
        File[] xmlFiles = samplesDir.listFiles((dir, name) -> name.endsWith("_Horoscope.xml"));
        assertNotNull("Sample files array must not be null", xmlFiles);
        assertTrue("Should have multiple sample horoscopes", xmlFiles.length > 5);

        for (File xml : xmlFiles) {
            File md = new File(tempDir, xml.getName() + ".md");
            try {
                AstrosoftMarkdownExporter.exportToMarkdown(xml.getAbsolutePath(), md.getAbsolutePath());
                assertTrue("Generated MD must exist and not be empty for " + xml.getName(), md.exists() && md.length() > 0);

                String mdContent = readEntireFile(md);
                verifyXmlEquivalence(xml, mdContent);
            } catch (Exception e) {
                fail("Failed to export and verify " + xml.getName() + ": " + e.getMessage());
            }
        }
    }

    public void testCompactibilityExportAndXmlEquivalence() throws Exception {
        File xmlFile = new File("resources/export/compactibility.xml");
        assertTrue("Compactibility XML must exist", xmlFile.exists());

        File mdFile = new File(tempDir, "compactibility.md");
        AstrosoftMarkdownExporter.exportToMarkdown(xmlFile.getAbsolutePath(), mdFile.getAbsolutePath());
        assertTrue("Markdown file must be generated", mdFile.exists() && mdFile.length() > 0);

        String mdContent = readEntireFile(mdFile);

        assertTrue("Must contain Title", mdContent.contains("# Compactibility Report"));
        assertTrue("Must contain Boy Information", mdContent.contains("## Boy Information"));
        assertTrue("Must contain Girl Information", mdContent.contains("## Girl Information"));
        assertTrue("Must contain Kuta Analysis", mdContent.contains("## Kuta Analysis"));
        assertTrue("Must contain Dosha Analysis", mdContent.contains("## Dosha Analysis"));

        verifyXmlEquivalence(xmlFile, mdContent);
    }

    public void testPanchangExportAndXmlEquivalence() throws Exception {
        File xmlFile = new File("resources/export/panchang.xml");
        assertTrue("Panchang XML must exist", xmlFile.exists());

        File mdFile = new File(tempDir, "panchang.md");
        AstrosoftMarkdownExporter.exportToMarkdown(xmlFile.getAbsolutePath(), mdFile.getAbsolutePath());
        assertTrue("Markdown file must be generated", mdFile.exists() && mdFile.length() > 0);

        String mdContent = readEntireFile(mdFile);

        assertTrue("Must contain Title", mdContent.contains("# Panchang"));
        assertTrue("Must contain Month section", mdContent.contains("## Panchang for"));
        assertTrue("Must contain Date column", mdContent.contains("| Date |"));

        verifyXmlEquivalence(xmlFile, mdContent);
    }

    private void verifyXmlEquivalence(File xmlFile, String mdContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);

        int leafValuesChecked = 0;
        NodeList allNodes = doc.getElementsByTagName("*");
        for (int i = 0; i < allNodes.getLength(); i++) {
            Element elem = (Element) allNodes.item(i);

            // Check non-empty Title attribute
            if (elem.hasAttribute("Title") && !elem.getAttribute("Title").trim().isEmpty()) {
                String title = elem.getAttribute("Title").trim();
                assertTrue("MD must contain Title attribute: " + title, mdContent.contains(title));
                leafValuesChecked++;
            }

            // Check non-empty Name attribute (e.g. Information, Chart, Gunahara)
            if (elem.hasAttribute("Name") && !elem.getAttribute("Name").trim().isEmpty()) {
                String name = elem.getAttribute("Name").trim();
                if (name.endsWith(":")) name = name.substring(0, name.length() - 1).trim();
                assertTrue("MD must contain Name: " + name, mdContent.contains(name));
                leafValuesChecked++;
            }

            // If it's a leaf node with text content
            if (isLeafElement(elem)) {
                String text = elem.getTextContent().trim();
                if (!text.isEmpty()) {
                    assertTrue("MD must contain leaf XML value '" + text + "' from tag <" + elem.getTagName() + ">",
                            mdContent.contains(text));
                    leafValuesChecked++;
                }
            }
        }

        assertTrue("At least 50 leaf values must have been verified", leafValuesChecked > 50);
    }

    private boolean isLeafElement(Element elem) {
        NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return false;
            }
        }
        return true;
    }

    private String readEntireFile(File file) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
