package app.astrosoft.i18n;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;

import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.DisplayFormat;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Thithi;
import app.astrosoft.consts.Varga;
import app.astrosoft.consts.WeekDay;
import app.astrosoft.consts.Yoga;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.util.Internalization;

/**
 * LocalizationAuditor crawls resource bundles, domain enums, and live UI
 * component trees to identify all missing translations, unlocalized strings,
 * and font glyph omissions ("tofu" boxes) across Tamil, English, and mixed-script text.
 */
public class LocalizationAuditor {

    public enum IssueType {
        MISSING_BUNDLE_TRANSLATION,
        UNLOCALIZED_ENUM_NAME,
        GLYPH_OMISSION_TAMIL,
        GLYPH_OMISSION_LATIN,
        RAW_S振りSERIF_ON_WINDOWS
    }

    public static class Issue {
        private final IssueType type;
        private final String context;
        private final String text;
        private final Font font;
        private final int failingIndex;
        private final Character failingChar;

        public Issue(IssueType type, String context, String text, Font font, int failingIndex, Character failingChar) {
            this.type = type;
            this.context = context;
            this.text = text;
            this.font = font;
            this.failingIndex = failingIndex;
            this.failingChar = failingChar;
        }

        public IssueType getType() { return type; }
        public String getContext() { return context; }
        public String getText() { return text; }
        public Font getFont() { return font; }
        public int getFailingIndex() { return failingIndex; }
        public Character getFailingChar() { return failingChar; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(type).append("] ");
            sb.append(context).append(" -> text: '").append(text).append("'");
            if (font != null) {
                sb.append(" (font: ").append(font.getName()).append(")");
            }
            if (failingIndex >= 0 && failingChar != null) {
                sb.append(" failed at index ").append(failingIndex)
                  .append(" char: '").append(failingChar)
                  .append("' (U+").append(Integer.toHexString(failingChar).toUpperCase()).append(")");
            }
            return sb.toString();
        }
    }

    public static class AuditReport {
        private final List<Issue> issues = new ArrayList<>();

        public void addIssue(Issue issue) {
            issues.add(issue);
        }

        public List<Issue> getIssues() {
            return Collections.unmodifiableList(issues);
        }

        public List<Issue> getIssuesByType(IssueType type) {
            List<Issue> res = new ArrayList<>();
            for (Issue i : issues) {
                if (i.getType() == type) res.add(i);
            }
            return res;
        }

        public int size() { return issues.size(); }

        public String formatSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Localization Audit Report ===\n");
            sb.append("Total issues found: ").append(issues.size()).append("\n");
            for (IssueType type : IssueType.values()) {
                List<Issue> sub = getIssuesByType(type);
                sb.append("  - ").append(type).append(": ").append(sub.size()).append("\n");
            }
            sb.append("\nDetailed Findings:\n");
            for (int i = 0; i < issues.size(); i++) {
                sb.append(String.format("%3d. %s\n", i + 1, issues.get(i).toString()));
            }
            return sb.toString();
        }
    }

    /**
     * Crawls living AstrosoftBundle_en.properties and AstrosoftBundle_ta.properties files.
     */
    public static void auditBundles(AuditReport report) {
        Properties enProps = loadProperties("/resources/AstrosoftBundle_en.properties");
        Properties taProps = loadProperties("/resources/AstrosoftBundle_ta.properties");
        Font tamilFont = UIUtil.getFont(Language.TAMIL, Font.PLAIN, 12);

        for (String key : enProps.stringPropertyNames()) {
            String taVal = taProps.getProperty(key);
            if (taVal == null || taVal.trim().isEmpty()) {
                report.addIssue(new Issue(
                    IssueType.MISSING_BUNDLE_TRANSLATION,
                    "Bundle Parity",
                    "Key: " + key + " (English: '" + enProps.getProperty(key) + "')",
                    tamilFont,
                    -1,
                    null
                ));
            } else {
                checkGlyphs(report, "Bundle key: " + key, taVal, tamilFont);
            }
        }
    }

    /**
     * Audits all core domain enums under Language.TAMIL.
     */
    public static void auditEnums(AuditReport report) {
        Font tamilFont = UIUtil.getFont(Language.TAMIL, Font.PLAIN, 12);

        // 1. Planets
        for (Planet p : Planet.planetsAsc()) {
            checkGlyphs(report, "Planet." + p.name(), p.toString(), tamilFont);
            checkGlyphs(report, "Planet symbol." + p.name(), p.toString(DisplayFormat.SYMBOL), tamilFont);
        }

        // 2. Rasis
        for (Rasi r : Rasi.values()) {
            checkGlyphs(report, "Rasi." + r.name(), r.toString(), tamilFont);
            checkGlyphs(report, "Rasi symbol." + r.name(), r.toString(DisplayFormat.SYMBOL), tamilFont);
        }

        // 3. Nakshatras
        for (Nakshathra n : Nakshathra.values()) {
            checkGlyphs(report, "Nakshathra." + n.name(), n.toString(), tamilFont);
        }

        // 4. Thithis
        for (Thithi t : Thithi.values()) {
            String str = Internalization.getString(Language.TAMIL, t.name());
            if (str.equals(t.name())) {
                report.addIssue(new Issue(
                    IssueType.UNLOCALIZED_ENUM_NAME,
                    "Thithi enum",
                    t.name(),
                    tamilFont,
                    -1,
                    null
                ));
            } else {
                checkGlyphs(report, "Thithi." + t.name(), str, tamilFont);
            }
        }

        // 5. Yogas
        for (Yoga y : Yoga.values()) {
            String str = Internalization.getString(Language.TAMIL, y.name());
            if (str.equals(y.name())) {
                report.addIssue(new Issue(
                    IssueType.UNLOCALIZED_ENUM_NAME,
                    "Yoga enum",
                    y.name(),
                    tamilFont,
                    -1,
                    null
                ));
            } else {
                checkGlyphs(report, "Yoga." + y.name(), str, tamilFont);
            }
        }

        // 6. DisplayStrings
        for (DisplayStrings ds : DisplayStrings.values()) {
            String str = ds.toString(Language.TAMIL);
            checkGlyphs(report, "DisplayStrings." + ds.name(), str, tamilFont);
        }

        // 7. Table Columns
        for (AstrosoftTableColumn col : AstrosoftTableColumn.values()) {
            String colStr = col.toString();
            checkGlyphs(report, "AstrosoftTableColumn." + col.name(), colStr, tamilFont);
        }
    }

    /**
     * Recursively traverses a Swing container tree and audits all rendered texts and fonts.
     */
    public static void auditComponentTree(Component comp, String context, AuditReport report) {
        if (comp == null) return;

        Font font = comp.getFont();
        if (font == null) {
            font = UIUtil.getFont();
        }

        if (comp instanceof JLabel) {
            String text = ((JLabel) comp).getText();
            checkComponentText(report, context + " > JLabel", text, font);
        } else if (comp instanceof AbstractButton) {
            String text = ((AbstractButton) comp).getText();
            checkComponentText(report, context + " > " + comp.getClass().getSimpleName(), text, font);
            String tip = ((AbstractButton) comp).getToolTipText();
            if (tip != null && !tip.isEmpty()) {
                checkComponentText(report, context + " > ToolTip", tip, font);
            }
        } else if (comp instanceof JTable) {
            auditTable(report, context + " > JTable", (JTable) comp);
        }

        if (comp instanceof JComponent) {
            Border b = ((JComponent) comp).getBorder();
            if (b instanceof TitledBorder) {
                TitledBorder tb = (TitledBorder) b;
                String title = tb.getTitle();
                Font tbFont = tb.getTitleFont() != null ? tb.getTitleFont() : font;
                checkComponentText(report, context + " > TitledBorder", title, tbFont);
            }
        }

        if (comp instanceof Container) {
            Component[] children = ((Container) comp).getComponents();
            for (int i = 0; i < children.length; i++) {
                auditComponentTree(children[i], context + "[" + i + "]", report);
            }
        }
    }

    private static void auditTable(AuditReport report, String context, JTable table) {
        Font tableFont = table.getFont();
        int rowCount = table.getRowCount();
        int colCount = table.getColumnCount();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                Object val = table.getValueAt(r, c);
                if (val != null) {
                    String str = val.toString();
                    TableCellRenderer renderer = table.getCellRenderer(r, c);
                    Component renComp = renderer != null 
                        ? renderer.getTableCellRendererComponent(table, val, false, false, r, c)
                        : null;
                    Font cellFont = (renComp != null && renComp.getFont() != null) ? renComp.getFont() : tableFont;
                    checkComponentText(report, context + "(row=" + r + ", col=" + c + ")", str, cellFont);
                }
            }
        }
    }

    private static void checkComponentText(AuditReport report, String context, String text, Font font) {
        if (text == null || text.trim().isEmpty()) return;
        // Strip HTML wrappers for glyph checking
        String cleanText = stripHtml(text);
        if (cleanText.trim().isEmpty()) return;

        checkGlyphs(report, context, cleanText, font);
    }

    private static void checkGlyphs(AuditReport report, String context, String text, Font font) {
        if (text == null || text.isEmpty() || font == null) return;

        int failingIdx = font.canDisplayUpTo(text);
        if (failingIdx >= 0) {
            char failChar = text.charAt(failingIdx);
            boolean isLatin = (failChar >= 'A' && failChar <= 'Z') || (failChar >= 'a' && failChar <= 'z');
            boolean isTamil = (failChar >= '\u0B80' && failChar <= '\u0BFF');

            IssueType type = isTamil 
                ? IssueType.GLYPH_OMISSION_TAMIL 
                : (isLatin ? IssueType.GLYPH_OMISSION_LATIN : IssueType.GLYPH_OMISSION_LATIN);

            report.addIssue(new Issue(type, context, text, font, failingIdx, failChar));
        }
    }

    private static String stripHtml(String s) {
        if (s == null) return "";
        if (s.startsWith("<html>") || s.startsWith("<HTML>")) {
            return s.replaceAll("<[^>]*>", "").replace("&nbsp;", " ").trim();
        }
        return s;
    }

    private static Properties loadProperties(String path) {
        Properties p = new Properties();
        try (InputStream in = LocalizationAuditor.class.getResourceAsStream(path)) {
            if (in != null) {
                p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load properties: " + path, e);
        }
        return p;
    }
}
