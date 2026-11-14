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
        HTML_FONT_BINDING_MISSING,
        RAW_SANS_SERIF_ON_TAMIL
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
            checkHtmlFontBinding(report, context + " > JLabel", (JComponent) comp, text, font);
            checkComponentText(report, context + " > JLabel", text, font);
        } else if (comp instanceof javax.swing.text.JTextComponent) {
            String text = ((javax.swing.text.JTextComponent) comp).getText();
            if (comp instanceof JComponent) {
                checkHtmlFontBinding(report, context + " > " + comp.getClass().getSimpleName(), (JComponent) comp, text, font);
            }
            checkComponentText(report, context + " > " + comp.getClass().getSimpleName(), text, font);
        } else if (comp instanceof AbstractButton) {
            String text = ((AbstractButton) comp).getText();
            checkHtmlFontBinding(report, context + " > " + comp.getClass().getSimpleName(), (JComponent) comp, text, font);
            checkComponentText(report, context + " > " + comp.getClass().getSimpleName(), text, font);
        } else if (comp instanceof JTable) {
            auditTable(report, context + " > JTable", (JTable) comp);
        } else if (comp instanceof javax.swing.JTabbedPane) {
            javax.swing.JTabbedPane tabs = (javax.swing.JTabbedPane) comp;
            Font selFont = javax.swing.UIManager.getFont("TabbedPane.selectedFont");
            if (selFont == null) selFont = font;
            for (int i = 0; i < tabs.getTabCount(); i++) {
                String title = tabs.getTitleAt(i);
                checkComponentText(report, context + " > JTabbedPane(tab=" + i + " unselected)", title, font);
                checkComponentText(report, context + " > JTabbedPane(tab=" + i + " selected)", title, selFont);
            }
        } else if (comp instanceof javax.swing.JComboBox) {
            javax.swing.JComboBox<?> combo = (javax.swing.JComboBox<?>) comp;
            javax.swing.ComboBoxModel<?> model = combo.getModel();
            if (model != null) {
                for (int i = 0; i < model.getSize(); i++) {
                    Object item = model.getElementAt(i);
                    if (item != null) {
                        checkComponentText(report, context + " > JComboBox(item=" + i + ")", item.toString(), font);
                    }
                }
            }
        } else if (comp instanceof javax.swing.JTree) {
            javax.swing.JTree tree = (javax.swing.JTree) comp;
            javax.swing.tree.TreeModel model = tree.getModel();
            if (model != null && model.getRoot() != null) {
                Object root = model.getRoot();
                checkComponentText(report, context + " > JTree(root)", root.toString(), font);
                int childCount = model.getChildCount(root);
                for (int i = 0; i < childCount; i++) {
                    Object child = model.getChild(root, i);
                    if (child != null) {
                        checkComponentText(report, context + " > JTree(child=" + i + ")", child.toString(), font);
                    }
                }
            }
        }

        if (comp instanceof JComponent) {
            JComponent jc = (JComponent) comp;
            String tip = jc.getToolTipText();
            if (tip != null && !tip.isEmpty()) {
                Font tipFont = javax.swing.UIManager.getFont("ToolTip.font");
                if (tipFont == null) tipFont = font;
                checkComponentText(report, context + " > ToolTip", tip, tipFont);
            }
            Border b = jc.getBorder();
            if (b instanceof TitledBorder) {
                TitledBorder tb = (TitledBorder) b;
                String title = tb.getTitle();
                Font tbFont = tb.getTitleFont() != null
                        ? tb.getTitleFont()
                        : javax.swing.UIManager.getFont("TitledBorder.font");
                if (tbFont == null) tbFont = font;
                checkComponentText(report, context + " > TitledBorder", title, tbFont);
            }
            javax.swing.JPopupMenu popup = jc.getComponentPopupMenu();
            if (popup != null) {
                auditComponentTree(popup, context + " > JPopupMenu", report);
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
                    String renderedStr = str;
                    if (renComp instanceof JLabel && ((JLabel) renComp).getText() != null) {
                        renderedStr = ((JLabel) renComp).getText();
                    } else if (renComp instanceof javax.swing.text.JTextComponent
                            && ((javax.swing.text.JTextComponent) renComp).getText() != null) {
                        renderedStr = ((javax.swing.text.JTextComponent) renComp).getText();
                    }
                    if (renComp instanceof JComponent) {
                        checkHtmlFontBinding(report, context + "(row=" + r + ", col=" + c + ")", (JComponent) renComp, renderedStr, cellFont);
                    }
                    checkComponentText(report, context + "(row=" + r + ", col=" + c + ")", renderedStr, cellFont);
                }
            }
        }
    }

    private static void checkHtmlFontBinding(AuditReport report, String context, JComponent comp, String text, Font font) {
        if (text == null) return;
        String trimmed = text.trim();
        if (!trimmed.regionMatches(true, 0, "<html>", 0, 6)) return;

        String cleanText = stripHtml(trimmed);
        boolean hasTamil = false;
        for (int i = 0; i < cleanText.length(); i++) {
            char ch = cleanText.charAt(i);
            if (ch >= '\u0B80' && ch <= '\u0BFF') {
                hasTamil = true;
                break;
            }
        }
        if (!hasTamil) return;

        boolean honorsDisplayProps = comp != null
                && Boolean.TRUE.equals(comp.getClientProperty(javax.swing.JEditorPane.HONOR_DISPLAY_PROPERTIES));
        String htmlFont = UIUtil.getHtmlFontFamily();
        boolean hasExplicitHtmlFont = trimmed.contains(htmlFont)
                || trimmed.contains(UIUtil.getTamilFontName());

        if (!honorsDisplayProps && !hasExplicitHtmlFont) {
            report.addIssue(new Issue(
                IssueType.HTML_FONT_BINDING_MISSING,
                context + " (missing HONOR_DISPLAY_PROPERTIES or UIUtil.getHtmlFontFamily())",
                cleanText,
                font,
                -1,
                null
            ));
        }
    }

    private static void checkComponentText(AuditReport report, String context, String text, Font font) {
        if (text == null || text.trim().isEmpty()) return;
        // Strip HTML wrappers for glyph checking
        String cleanText = stripHtml(text);
        if (cleanText.trim().isEmpty()) return;

        if (font != null && containsTamil(cleanText)) {
            String resolvedTamil = UIUtil.getTamilFontName();
            String fam = font.getFamily();
            String name = font.getName();
            if (!"SansSerif".equalsIgnoreCase(resolvedTamil)
                    && !resolvedTamil.equalsIgnoreCase(fam)
                    && !resolvedTamil.equalsIgnoreCase(name)) {
                report.addIssue(new Issue(
                    IssueType.RAW_SANS_SERIF_ON_TAMIL,
                    context + " (expected Tamil font '" + resolvedTamil + "', got '" + fam + "')",
                    cleanText,
                    font,
                    -1,
                    null
                ));
            }
        }

        checkGlyphs(report, context, cleanText, font);
    }

    private static boolean containsTamil(String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= '\u0B80' && ch <= '\u0BFF') {
                return true;
            }
        }
        return false;
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
