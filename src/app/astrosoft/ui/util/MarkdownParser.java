package app.astrosoft.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MarkdownParser: Converts Markdown content (from ChatGPT, Claude, Obsidian, etc.)
 * into clean, styled HTML suitable for Swing's HTMLEditorKit.
 */
public class MarkdownParser {

    private static final Pattern TABLE_PATTERN = Pattern.compile("(?m)^\\s*\\|.+?\\|\\s*$\\n^\\s*\\|[-: ]+\\|\\s*$");
    private static final Pattern HEADING_PATTERN = Pattern.compile("(?m)^#{1,6}\\s+.+$");
    private static final Pattern UNORDERED_LIST_PATTERN = Pattern.compile("(?m)^\\s*[-*+]\\s+.+$");
    private static final Pattern ORDERED_LIST_PATTERN = Pattern.compile("(?m)^\\s*\\d+\\.\\s+.+$");
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*[^*]+\\*\\*|__[^_]+__");
    private static final Pattern BLOCKQUOTE_PATTERN = Pattern.compile("(?m)^\\s*>\\s+.+$");
    private static final Pattern HR_PATTERN = Pattern.compile("(?m)^\\s*(-{3,}|\\*{3,}|_{3,})\\s*$");
    private static final Pattern CODE_SPAN_PATTERN = Pattern.compile("`[^`]+`");

    /**
     * Determines whether the given text contains characteristic Markdown markup.
     */
    public static boolean isMarkdown(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        // Tables are unmistakable
        if (containsMarkdownTable(text)) {
            return true;
        }

        // Headings are unmistakable
        if (HEADING_PATTERN.matcher(text).find()) {
            return true;
        }

        // 2 or more list items are unmistakable
        int listCount = 0;
        Matcher mU = UNORDERED_LIST_PATTERN.matcher(text);
        while (mU.find()) listCount++;
        Matcher mO = ORDERED_LIST_PATTERN.matcher(text);
        while (mO.find()) listCount++;
        if (listCount >= 2) {
            return true;
        }

        int indicators = 0;
        if (listCount >= 1) indicators++;
        if (BOLD_PATTERN.matcher(text).find()) indicators++;
        if (BLOCKQUOTE_PATTERN.matcher(text).find()) indicators++;
        if (HR_PATTERN.matcher(text).find()) indicators++;
        if (CODE_SPAN_PATTERN.matcher(text).find()) indicators++;

        return indicators >= 2 || (indicators >= 1 && (text.contains("**") || text.contains("__")));
    }

    public static boolean containsMarkdownTable(String text) {
        if (text == null || !text.contains("|")) return false;
        String[] lines = text.replace("\r\n", "\n").replace("\r", "\n").split("\n");
        for (int i = 0; i < lines.length - 1; i++) {
            if (lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|") && isTableDelimiter(lines[i + 1])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts Markdown string to clean HTML formatted for Notes.
     */
    public static String toHtml(String markdown) {
        if (markdown == null) return "";
        String text = markdown.replace("\r\n", "\n").replace("\r", "\n");

        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\n");
        int i = 0;

        while (i < lines.length) {
            String line = lines[i];
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                i++;
                continue;
            }

            // 1. Table block detection
            if (trimmed.startsWith("|") && trimmed.endsWith("|") && (i + 1 < lines.length) && isTableDelimiter(lines[i + 1])) {
                List<String> tableLines = new ArrayList<String>();
                while (i < lines.length && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                    tableLines.add(lines[i].trim());
                    i++;
                }
                sb.append(parseMarkdownTable(tableLines));
                continue;
            }

            // 2. Headings
            if (trimmed.startsWith("#")) {
                int level = 0;
                while (level < trimmed.length() && trimmed.charAt(level) == '#') {
                    level++;
                }
                if (level <= 6 && level < trimmed.length() && trimmed.charAt(level) == ' ') {
                    String hText = trimmed.substring(level + 1).trim();
                    hText = parseInline(hText);
                    sb.append("<h").append(level).append(">").append(hText).append("</h").append(level).append(">");
                    i++;
                    continue;
                }
            }

            // 3. Horizontal Rule
            if (HR_PATTERN.matcher(trimmed).matches()) {
                sb.append("<hr />");
                i++;
                continue;
            }

            // 4. Blockquote
            if (trimmed.startsWith(">")) {
                StringBuilder bqText = new StringBuilder();
                while (i < lines.length && lines[i].trim().startsWith(">")) {
                    String bqLine = lines[i].trim().substring(1).trim();
                    if (bqText.length() > 0) bqText.append("<br>");
                    bqText.append(parseInline(bqLine));
                    i++;
                }
                sb.append("<blockquote>").append(bqText).append("</blockquote>");
                continue;
            }

            // 5. Unordered List
            if (isUnorderedListItem(trimmed)) {
                sb.append("<ul>");
                while (i < lines.length && isUnorderedListItem(lines[i].trim())) {
                    String item = lines[i].trim();
                    int spaceIdx = item.indexOf(' ');
                    String itemText = (spaceIdx != -1) ? item.substring(spaceIdx + 1).trim() : item.substring(1).trim();
                    sb.append("<li>").append(parseInline(itemText)).append("</li>");
                    i++;
                }
                sb.append("</ul>");
                continue;
            }

            // 6. Ordered List
            if (isOrderedListItem(trimmed)) {
                sb.append("<ol>");
                while (i < lines.length && isOrderedListItem(lines[i].trim())) {
                    String item = lines[i].trim();
                    int dotIdx = item.indexOf('.');
                    String itemText = (dotIdx != -1) ? item.substring(dotIdx + 1).trim() : item;
                    sb.append("<li>").append(parseInline(itemText)).append("</li>");
                    i++;
                }
                sb.append("</ol>");
                continue;
            }

            // 7. Regular paragraph
            StringBuilder pText = new StringBuilder();
            while (i < lines.length && !lines[i].trim().isEmpty() 
                    && !lines[i].trim().startsWith("#")
                    && !lines[i].trim().startsWith(">")
                    && !isUnorderedListItem(lines[i].trim())
                    && !isOrderedListItem(lines[i].trim())
                    && !HR_PATTERN.matcher(lines[i].trim()).matches()
                    && !(lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|"))) {
                if (pText.length() > 0) pText.append("<br>");
                pText.append(parseInline(lines[i].trim()));
                i++;
            }
            if (pText.length() > 0) {
                sb.append("<p>").append(pText).append("</p>");
            }
        }

        return sb.toString();
    }

    private static boolean isUnorderedListItem(String line) {
        return line.startsWith("- ") || line.startsWith("* ") || line.startsWith("+ ");
    }

    private static boolean isOrderedListItem(String line) {
        return line.matches("^\\d+\\.\\s+.+$");
    }

    private static boolean isTableDelimiter(String line) {
        String t = line.trim();
        return t.startsWith("|") && t.endsWith("|") && t.matches("^\\|[\\s:-]+\\|([\\s:-]+\\|)*$");
    }

    private static String parseMarkdownTable(List<String> lines) {
        if (lines.size() < 2) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("<table border=\"1\" style=\"border-collapse: collapse; margin-top: 4px; margin-bottom: 8px;\">");

        // Header row
        String headerLine = lines.get(0);
        String[] headers = splitTableRow(headerLine);
        sb.append("<tr>");
        for (String h : headers) {
            sb.append("<th style=\"background-color: #f0f4f8; font-weight: bold; padding: 4px 8px; text-align: left;\">")
              .append(parseInline(h))
              .append("</th>");
        }
        sb.append("</tr>");

        // Data rows (skip line 1 which is delimiter)
        for (int r = 2; r < lines.size(); r++) {
            String[] cells = splitTableRow(lines.get(r));
            sb.append("<tr>");
            for (int c = 0; c < headers.length; c++) {
                String val = (c < cells.length) ? cells[c] : "";
                sb.append("<td style=\"padding: 4px 8px; vertical-align: top;\">")
                  .append(parseInline(val))
                  .append("</td>");
            }
            sb.append("</tr>");
        }

        sb.append("</table>");
        return sb.toString();
    }

    private static String[] splitTableRow(String row) {
        String trimmed = row.trim();
        if (trimmed.startsWith("|")) trimmed = trimmed.substring(1);
        if (trimmed.endsWith("|")) trimmed = trimmed.substring(0, trimmed.length() - 1);
        String[] parts = trimmed.split("\\|");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }

    /**
     * Parses inline Markdown formatting: bold, italic, code, links.
     */
    public static String parseInline(String text) {
        if (text == null) return "";
        String s = text;

        // Escape any raw HTML angle brackets to prevent injection/broken markup
        s = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        // Code spans: `code`
        s = s.replaceAll("`([^`]+)`", "<code>$1</code>");

        // Bold + Italic: ***text***
        s = s.replaceAll("\\*\\*\\*([^*]+)\\*\\*\\*", "<b><i>$1</i></b>");
        s = s.replaceAll("___([^_]+)___", "<b><i>$1</i></b>");

        // Bold: **text** or __text__
        s = s.replaceAll("\\*\\*([^*]+)\\*\\*", "<b>$1</b>");
        s = s.replaceAll("__([^_]+)__", "<b>$1</b>");

        // Italic: *text* or _text_
        s = s.replaceAll("(?<![a-zA-Z0-9])\\*([^*]+)\\*(?![a-zA-Z0-9])", "<i>$1</i>");
        s = s.replaceAll("(?<![a-zA-Z0-9])_([^_]+)_(?![a-zA-Z0-9])", "<i>$1</i>");

        return s;
    }
}
