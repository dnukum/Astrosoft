/**
 * AstrosoftMarkdownExporter.java
 * Converts AstroSoft export XML (Horoscope, Compactibility, Panchang) into formatted Markdown.
 */

package app.astrosoft.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AstrosoftMarkdownExporter {

    private static final Logger log = Logger.getLogger(AstrosoftMarkdownExporter.class.getName());

    public static void exportToMarkdown(AstrosoftExporter.Type type, String xmlFile, String mdFile) {
        exportToMarkdown(xmlFile, mdFile);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java AstrosoftMarkdownExporter <input.xml> [output.md]");
            System.exit(1);
        }
        String inputXml = args[0];
        String outputMd = (args.length >= 2) ? args[1] : inputXml.replaceAll("(?i)\\.xml$", ".md");
        exportToMarkdown(inputXml, outputMd);
        System.out.println("Successfully converted '" + inputXml + "' to '" + outputMd + "'!");
    }

    public static void exportToMarkdown(String xmlFile, String mdFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(xmlFile));
            Element root = doc.getDocumentElement();

            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mdFile), StandardCharsets.UTF_8))) {
                Element horoscopeElem = findElement(root, "Horoscope");
                if (horoscopeElem != null) {
                    exportHoroscope(horoscopeElem, writer);
                    return;
                }

                Element compElem = findElement(root, "Compatibility");
                if (compElem == null) {
                    compElem = findElement(root, "Compactibility");
                }
                if (compElem != null) {
                    exportCompatibility(compElem, writer);
                    return;
                }

                Element panchangElem = findElement(root, "Panchang");
                if (panchangElem != null) {
                    exportPanchang(panchangElem, writer);
                    return;
                }

                log.warning("Unrecognized XML format in " + xmlFile);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error exporting XML to Markdown: " + xmlFile, e);
            throw new RuntimeException("Failed to export markdown: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // 1. HOROSCOPE EXPORT
    // ==========================================

    private static void exportHoroscope(Element horoscope, Writer w) throws Exception {
        String title = horoscope.getAttribute("Title");
        if (title == null || title.trim().isEmpty()) {
            title = "Vedic Astrology Horoscope";
        }
        w.write("# " + title.trim() + "\n\n");

        // 1. Horoscope Information
        Element infoContainer = findElement(horoscope, "Horoscope-Information");
        if (infoContainer != null) {
            List<Element> infos = getDirectChildren(infoContainer, "Information");
            if (!infos.isEmpty()) {
                w.write("## 1. Horoscope Information\n\n");
                w.write("| Property | Value |\n|---|---|\n");
                for (Element info : infos) {
                    String name = info.getAttribute("Name").trim();
                    String val = getText(info);
                    w.write("| " + name + " | " + val + " |\n");
                }
                w.write("\n");
            }
        }

        // 2. Planetary Positions
        Element planetContainer = findElement(horoscope, "Planetary-Position");
        if (planetContainer != null) {
            List<Element> planets = getDirectChildren(planetContainer, "Planet-Position");
            if (!planets.isEmpty()) {
                w.write("## 2. Planetary Positions\n\n");
                w.write("| Planet | Longitude | Rasi | NakshathraPada | JaiminiKaraka |\n|---|---|---|---|---|\n");
                for (Element p : planets) {
                    String planet = getTextChild(p, "Planet");
                    String longitude = getTextChild(p, "Longitude");
                    String rasi = getTextChild(p, "Rasi");
                    String nak = getTextChild(p, "NakshathraPada");
                    String jaimini = getTextChild(p, "JaiminiKaraka");
                    w.write("| " + planet + " | " + longitude + " | " + rasi + " | " + nak + " | " + jaimini + " |\n");
                }
                w.write("\n");
            }
        }

        // 3. Bhava Positions
        Element bhavaContainer = findElement(horoscope, "Bhava-Positions");
        if (bhavaContainer != null) {
            List<Element> bhavas = getDirectChildren(bhavaContainer, "Bhava-Position");
            if (!bhavas.isEmpty()) {
                w.write("## 3. Bhava Positions\n\n");
                w.write("| House | Bhava | Start | Mid | End | Length |\n|---|---|---|---|---|---|\n");
                for (Element b : bhavas) {
                    String house = getTextChild(b, "House");
                    String bhava = getTextChild(b, "Bhava");
                    String start = getTextChild(b, "Start");
                    String mid = getTextChild(b, "Mid");
                    String end = getTextChild(b, "End");
                    String length = getTextChild(b, "Length");
                    w.write("| " + house + " | " + bhava + " | " + start + " | " + mid + " | " + end + " | " + length + " |\n");
                }
                w.write("\n");
            }
        }

        // 4. Varga Charts
        Element vargaContainer = findElement(horoscope, "Varga-Chart");
        if (vargaContainer != null) {
            List<Element> charts = getDirectChildren(vargaContainer, "Chart");
            if (!charts.isEmpty()) {
                w.write("## 4. Varga Charts\n\n");
                for (Element chart : charts) {
                    String name = chart.getAttribute("Name");
                    w.write("### " + name + " Chart\n\n");
                    writeChartHorizontalTable(chart, w, "Planets");
                }
            }
        }

        // 5. Ashtavargas
        Element avContainer = findElement(horoscope, "Ashtavargas");
        if (avContainer != null) {
            List<Element> avs = getDirectChildren(avContainer, "Ashtavarga");
            if (!avs.isEmpty()) {
                w.write("## 5. Ashtavargas\n\n");
                for (Element av : avs) {
                    String avName = av.getAttribute("Name");
                    w.write("### " + avName + " Ashtavarga\n\n");

                    List<Element> charts = getDirectChildren(av, "Chart");
                    for (Element chart : charts) {
                        String chartName = chart.getAttribute("Name");
                        w.write("#### " + chartName + "\n\n");
                        writeAshtavargaChartHorizontalTable(chart, w);
                    }

                    Element gunaharas = findElement(av, "Gunaharas");
                    if (gunaharas != null) {
                        List<Element> gunas = getDirectChildren(gunaharas, "Gunahara");
                        if (!gunas.isEmpty()) {
                            w.write("#### Gunaharas\n\n");
                            w.write("| Name | Value |\n|---|---|\n");
                            for (Element guna : gunas) {
                                String gName = guna.getAttribute("Name").trim();
                                if (gName.endsWith(":")) {
                                    gName = gName.substring(0, gName.length() - 1).trim();
                                }
                                String gVal = getText(guna);
                                w.write("| " + gName + " | " + gVal + " |\n");
                            }
                            w.write("\n");
                        }
                    }
                }
            }
        }

        // 6. Shadbalas & Strengths
        Element shadbalas = findElement(horoscope, "Shadbalas");
        if (shadbalas != null) {
            w.write("## 6. Shadbalas & Strengths\n\n");
            writeBalaSection(shadbalas, "Shadbala", w);
            writeBalaSection(shadbalas, "Sthanabala", w);
            writeBalaSection(shadbalas, "Kalabala", w);
            writeBalaSection(shadbalas, "Bhavabala", w);
        }

        // 7. Vimshottari Dasas
        Element dasaContainer = findElement(horoscope, "Vimshottari-Dasas");
        if (dasaContainer != null) {
            List<Element> majorDasas = getDirectChildren(dasaContainer, "Major-Dasa");
            if (!majorDasas.isEmpty()) {
                w.write("## 7. Vimshottari Dasas\n\n");
                for (Element major : majorDasas) {
                    String mDasa = major.getAttribute("Dasa").trim();
                    String mPeriod = major.getAttribute("Period").trim();
                    w.write("### Major Dasa: " + mDasa + " " + mPeriod + "\n\n");

                    List<Element> subDasas = getDirectChildren(major, "Sub-Dasa");
                    for (Element sub : subDasas) {
                        String sDasa = sub.getAttribute("Dasa").trim();
                        w.write("#### Sub Dasa: " + sDasa + "\n\n");
                        w.write("| Anthara Dasa | Start | End |\n|---|---|---|\n");

                        List<Element> antharas = getDirectChildren(sub, "Anthara-Dasa");
                        for (Element a : antharas) {
                            String d = getTextChild(a, "Dasa");
                            String s = getTextChild(a, "Start");
                            String e = getTextChild(a, "End");
                            w.write("| " + d + " | " + s + " | " + e + " |\n");
                        }
                        w.write("\n");
                    }
                }
            }
        }
    }

    // ==========================================
    // 2. COMPATIBILITY EXPORT
    // ==========================================

    private static void exportCompatibility(Element comp, Writer w) throws Exception {
        String title = comp.getAttribute("Title");
        if (title == null || title.trim().isEmpty()) {
            title = "Compatibility Report";
        }
        w.write("# " + title.trim() + "\n\n");

        // Boy
        Element boy = findElement(comp, "Boy");
        if (boy != null) {
            w.write("## Boy Information\n\n");
            exportPersonInfoAndCharts(boy, w);
        }

        // Girl
        Element girl = findElement(comp, "Girl");
        if (girl != null) {
            w.write("## Girl Information\n\n");
            exportPersonInfoAndCharts(girl, w);
        }

        // Kuta Analysis
        Element kutaContainer = findElement(comp, "Kuta-Analysis");
        if (kutaContainer != null) {
            List<Element> kutas = getDirectChildren(kutaContainer, "Kuta-Match");
            if (!kutas.isEmpty()) {
                w.write("## Kuta Analysis\n\n");
                w.write("| Kuta | Kuta Gained | Max Kuta |\n|---|---|---|\n");
                for (Element k : kutas) {
                    String kuta = getTextChild(k, "Kuta");
                    String gained = getTextChild(k, "KutaGained");
                    String max = getTextChild(k, "MaxKuta");
                    w.write("| " + kuta + " | " + gained + " | " + max + " |\n");
                }
                w.write("\n");
            }
        }

        // Dosha Analysis
        Element doshaContainer = findElement(comp, "Dosha-Analysis");
        if (doshaContainer != null) {
            List<Element> doshas = getDirectChildren(doshaContainer, "Planet-Dosha");
            if (!doshas.isEmpty()) {
                w.write("## Dosha Analysis\n\n");
                w.write("| Dosha | Boy | Girl |\n|---|---|---|\n");
                for (Element d : doshas) {
                    String dosha = getTextChild(d, "Dosha");
                    String boyScore = getTextChild(d, "Boy");
                    String girlScore = getTextChild(d, "Girl");
                    w.write("| " + dosha + " | " + boyScore + " | " + girlScore + " |\n");
                }
                w.write("\n");
            }
        }

        Element notes = findElement(comp, "Notes");
        if (notes != null) {
            String noteText = getText(notes);
            if (noteText != null && !noteText.trim().isEmpty()) {
                w.write("## Notes\n\n");
                String plain = noteText.replaceAll("(?i)<br\\s*/?>", "\n")
                                       .replaceAll("(?i)</p>\\s*<p[^>]*>", "\n\n")
                                       .replaceAll("<[^>]+>", "")
                                       .trim();
                w.write(plain + "\n\n");
            }
        }
    }

    private static void exportPersonInfoAndCharts(Element person, Writer w) throws Exception {
        Element infoContainer = findElement(person, "Horoscope-Information");
        if (infoContainer != null) {
            List<Element> infos = getDirectChildren(infoContainer, "Information");
            if (!infos.isEmpty()) {
                w.write("| Property | Value |\n|---|---|\n");
                for (Element info : infos) {
                    String name = info.getAttribute("Name").trim();
                    String val = getText(info);
                    w.write("| " + name + " | " + val + " |\n");
                }
                w.write("\n");
            }
        }

        List<Element> charts = getDirectChildren(person, "Chart");
        for (Element chart : charts) {
            String name = chart.getAttribute("Name");
            w.write("### " + name + " Chart\n\n");
            writeChartHorizontalTable(chart, w, "Planets");
        }
    }

    // ==========================================
    // 3. PANCHANG EXPORT
    // ==========================================

    private static void exportPanchang(Element panchang, Writer w) throws Exception {
        String loc = panchang.getAttribute("Location");
        String locSuffix = (loc != null && !loc.trim().isEmpty()) ? " - " + loc.trim() : "";
        w.write("# Panchang" + locSuffix + "\n\n");

        List<Element> months = getDirectChildren(panchang, "Month");
        for (Element month : months) {
            String title = month.getAttribute("Title");
            if (title != null && !title.trim().isEmpty()) {
                w.write("## " + title.trim() + "\n\n");
            }

            List<Element> days = getDirectChildren(month, "Panchang-Information");
            if (!days.isEmpty()) {
                // Dynamically collect all property names from the days preserving order
                List<String> headers = new ArrayList<>();
                for (Element day : days) {
                    List<Element> infos = getDirectChildren(day, "Information");
                    for (Element info : infos) {
                        String name = info.getAttribute("Name").trim();
                        if (!headers.contains(name)) {
                            headers.add(name);
                        }
                    }
                }

                w.write("| " + String.join(" | ", headers) + " |\n");
                StringBuilder sep = new StringBuilder("|");
                for (int i = 0; i < headers.size(); i++) sep.append("---|");
                w.write(sep.toString() + "\n");

                for (Element day : days) {
                    Map<String, String> dayProps = new LinkedHashMap<>();
                    List<Element> infos = getDirectChildren(day, "Information");
                    for (Element info : infos) {
                        dayProps.put(info.getAttribute("Name").trim(), getText(info));
                    }

                    List<String> row = new ArrayList<>();
                    for (String header : headers) {
                        row.add(dayProps.getOrDefault(header, ""));
                    }
                    w.write("| " + String.join(" | ", row) + " |\n");
                }
                w.write("\n");
            }
        }
    }

    // ==========================================
    // TABLE RENDERING HELPERS
    // ==========================================

    private static void writeChartHorizontalTable(Element chart, Writer w, String rowLabel) throws Exception {
        List<Element> houses = getDirectChildren(chart, "House");
        List<String> headers = new ArrayList<>();
        List<String> rowValues = new ArrayList<>();

        for (Element h : houses) {
            headers.add(h.getAttribute("No"));
            List<Element> planets = getDirectChildren(h, "Planet");
            if (planets.isEmpty()) {
                rowValues.add("-");
            } else {
                List<String> pNames = new ArrayList<>();
                for (Element p : planets) {
                    String txt = getText(p);
                    if (!txt.isEmpty()) pNames.add(txt);
                }
                rowValues.add(pNames.isEmpty() ? "-" : String.join(", ", pNames));
            }
        }

        w.write("| House | " + String.join(" | ", headers) + " |\n");
        StringBuilder sep = new StringBuilder("|---|");
        for (int i = 0; i < headers.size(); i++) sep.append("---|");
        w.write(sep.toString() + "\n");
        w.write("| " + rowLabel + " | " + String.join(" | ", rowValues) + " |\n\n");
    }

    private static void writeAshtavargaChartHorizontalTable(Element chart, Writer w) throws Exception {
        List<Element> houses = getDirectChildren(chart, "House");
        List<String> headers = new ArrayList<>();
        List<String> rowValues = new ArrayList<>();

        for (Element h : houses) {
            headers.add(h.getAttribute("No"));
            String val = getText(h);
            rowValues.add(val.isEmpty() ? "0" : val);
        }

        w.write("| House | " + String.join(" | ", headers) + " |\n");
        StringBuilder sep = new StringBuilder("|---|");
        for (int i = 0; i < headers.size(); i++) sep.append("---|");
        w.write(sep.toString() + "\n");
        w.write("| Value | " + String.join(" | ", rowValues) + " |\n\n");
    }

    private static void writeBalaSection(Element shadbalasNode, String balaTag, Writer w) throws Exception {
        Element balaContainer = findElement(shadbalasNode, balaTag);
        if (balaContainer == null) return;

        List<Element> balas = getDirectChildren(balaContainer, "Bala");
        if (balas.isEmpty()) return;

        w.write("### " + balaTag + "\n\n");

        // Extract headers from child elements of first Bala
        Element first = balas.get(0);
        List<String> headers = new ArrayList<>();
        NodeList children = first.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                headers.add(getLocalName((Element) children.item(i)));
            }
        }

        w.write("| " + String.join(" | ", headers) + " |\n");
        StringBuilder sep = new StringBuilder("|");
        for (int i = 0; i < headers.size(); i++) sep.append("---|");
        w.write(sep.toString() + "\n");

        for (Element bala : balas) {
            List<String> row = new ArrayList<>();
            for (String h : headers) {
                row.add(getTextChild(bala, h));
            }
            w.write("| " + String.join(" | ", row) + " |\n");
        }
        w.write("\n");
    }

    // ==========================================
    // DOM UTILITY METHODS
    // ==========================================

    private static String getLocalName(Element e) {
        String tag = e.getNodeName();
        int idx = tag.indexOf(':');
        return idx >= 0 ? tag.substring(idx + 1) : tag;
    }

    private static String getText(Element e) {
        return e == null ? "" : e.getTextContent().trim();
    }

    private static String getTextChild(Element parent, String childTag) {
        Element child = findElement(parent, childTag);
        return child == null ? "" : getText(child);
    }

    private static Element findElement(Element parent, String tag) {
        if (parent == null) return null;
        NodeList list = parent.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                if (getLocalName(e).equalsIgnoreCase(tag)) {
                    return e;
                }
            }
        }
        return null;
    }

    private static List<Element> getDirectChildren(Element parent, String tag) {
        List<Element> result = new ArrayList<>();
        if (parent == null) return result;
        NodeList list = parent.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                if (getLocalName(e).equalsIgnoreCase(tag)) {
                    result.add(e);
                }
            }
        }
        return result;
    }
}
