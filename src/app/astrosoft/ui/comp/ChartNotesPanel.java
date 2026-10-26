/**
 * ChartNotesPanel.java
 * Presentation Layer: Notes taking panel attached to Horoscope metadata.
 * Supports WYSIWYG rich text formatting (bold, italics, underline, timestamps),
 * clean typography, intelligent clipboard paste with proper paragraph formatting,
 * and dock/undock capabilities.
 */

package app.astrosoft.ui.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.util.UIConsts;

public class ChartNotesPanel extends JPanel {

    public interface NotesListener {
        void onFold();
        void onTogglePopOut(boolean isPopOut);
        void onNotesModified();
    }

    private Horoscope horoscope;
    private JTextPane editor;
    private JButton popOutBtn;
    private JButton foldBtn;
    private JButton boldBtn;
    private JButton italicBtn;
    private JButton underlineBtn;
    private JButton timeBtn;
    private JLabel titleLabel;
    private boolean isUpdating = false;
    private boolean isDocked = true;
    private NotesListener listener;

    public ChartNotesPanel(NotesListener listener) {
        this.listener = listener;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UIConsts.DARK_PEARL_GREEN));
        setPreferredSize(new Dimension(340, 400));
        setMinimumSize(new Dimension(200, 200));

        // 1. Top Header Bar - Clean "Notes" title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConsts.PEARL_GREEN);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConsts.DARK_PEARL_GREEN),
                BorderFactory.createEmptyBorder(5, 12, 5, 6)
        ));

        titleLabel = new JLabel("Notes");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13.0f));
        titleLabel.setForeground(UIConsts.DARK_BLUE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel headerControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        headerControls.setOpaque(false);

        popOutBtn = new JButton("↗");
        popOutBtn.setToolTipText("Pop out to floating window");
        popOutBtn.setMargin(new Insets(2, 6, 2, 6));
        popOutBtn.setFocusable(false);
        popOutBtn.addActionListener(e -> {
            if (listener != null) {
                listener.onTogglePopOut(isDocked);
            }
        });

        foldBtn = new JButton("▶");
        foldBtn.setToolTipText("Fold / Close Notes");
        foldBtn.setMargin(new Insets(2, 6, 2, 6));
        foldBtn.setFocusable(false);
        foldBtn.addActionListener(e -> {
            if (listener != null) {
                listener.onFold();
            }
        });

        headerControls.add(popOutBtn);
        headerControls.add(foldBtn);
        headerPanel.add(headerControls, BorderLayout.EAST);

        // 2. Rich Text Formatting Toolbar
        JToolBar formatToolbar = new JToolBar();
        formatToolbar.setFloatable(false);
        formatToolbar.setRollover(true);
        formatToolbar.setBackground(UIConsts.GRAY_WHITE);
        formatToolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        Action boldAction = new StyledEditorKit.BoldAction();
        boldBtn = new JButton(boldAction);
        boldBtn.setText("B");
        boldBtn.setFont(boldBtn.getFont().deriveFont(Font.BOLD, 12.0f));
        boldBtn.setToolTipText("Bold (Ctrl+B / Cmd+B)");
        boldBtn.setMargin(new Insets(2, 6, 2, 6));
        boldBtn.setFocusable(false);

        Action italicAction = new StyledEditorKit.ItalicAction();
        italicBtn = new JButton(italicAction);
        italicBtn.setText("I");
        italicBtn.setFont(italicBtn.getFont().deriveFont(Font.ITALIC, 12.0f));
        italicBtn.setToolTipText("Italic (Ctrl+I / Cmd+I)");
        italicBtn.setMargin(new Insets(2, 6, 2, 6));
        italicBtn.setFocusable(false);

        Action underlineAction = new StyledEditorKit.UnderlineAction();
        underlineBtn = new JButton(underlineAction);
        underlineBtn.setText("U");
        underlineBtn.setToolTipText("Underline (Ctrl+U / Cmd+U)");
        underlineBtn.setMargin(new Insets(2, 6, 2, 6));
        underlineBtn.setFocusable(false);

        timeBtn = new JButton("+ Time");
        timeBtn.setToolTipText("Insert Current Date/Time Stamp");
        timeBtn.setMargin(new Insets(2, 6, 2, 6));
        timeBtn.setFocusable(false);
        timeBtn.addActionListener(e -> insertTimestamp());

        formatToolbar.add(Box.createHorizontalStrut(6));
        formatToolbar.add(boldBtn);
        formatToolbar.add(italicBtn);
        formatToolbar.add(underlineBtn);
        formatToolbar.addSeparator(new Dimension(8, 16));
        formatToolbar.add(timeBtn);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(formatToolbar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // 3. Editor & Typography Document
        editor = new JTextPane();
        editor.setContentType("text/html");
        editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        Font baseFont = new Font("Arial", Font.PLAIN, 13);
        editor.setFont(baseFont);
        editor.setMargin(new Insets(10, 12, 10, 12));
        editor.setBackground(Color.WHITE);

        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: Arial, Helvetica, sans-serif; font-size: 13pt; line-height: 1.45; margin: 10px 12px; color: #1a1a1a; }");
        styleSheet.addRule("p { margin-top: 0px; margin-bottom: 8px; line-height: 1.45; }");
        styleSheet.addRule("b, strong { font-weight: bold; }");
        styleSheet.addRule("i, em { font-style: italic; }");
        styleSheet.addRule("u { text-decoration: underline; }");
        styleSheet.addRule("ul, ol { margin-top: 2px; margin-bottom: 8px; padding-left: 20px; }");
        styleSheet.addRule("li { margin-bottom: 4px; }");
        editor.setEditorKit(kit);

        // Smart Paste Action for proper plain-text / rich-text handling
        Action smartPasteAction = createSmartPasteAction();

        // Mac & Windows shortcut bindings
        setupKeyBindings(boldAction, italicAction, underlineAction, smartPasteAction);

        // Context Popup Menu (Cut, Copy, Paste, Select All)
        setupContextMenu(smartPasteAction);

        // Auto-save sync to Horoscope
        editor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { syncToHoroscope(); }
            public void removeUpdate(DocumentEvent e) { syncToHoroscope(); }
            public void changedUpdate(DocumentEvent e) { syncToHoroscope(); }
        });

        JScrollPane scrollPane = new JScrollPane(editor);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        setEnabledState(false);
    }

    public static String cleanAndSanitizeText(String raw) {
        if (raw == null) return "";

        String text = raw;

        // 1. If HTML tags are present (e.g. from web/browser copy, Word Mso, Google Docs), extract clean text via Jsoup
        if (text.contains("<") && text.contains(">")) {
            text = extractTextFromHtml(text);
        }

        // 2. Decode common HTML entities
        text = text.replace("&nbsp;", " ")
                   .replace("&amp;", "&")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&quot;", "\"")
                   .replace("&#39;", "'");

        // 3. Remove zero-width spaces, BOM, soft hyphens, WhatsApp/Unicode BiDi marks, and invisible control characters
        text = text.replaceAll("[\u200B\u200C\u200D\uFEFF\u00AD\u200E\u200F\u202A-\u202E\u0000-\u0008\u000B\u000C\u000E-\u001F]", "");

        // 4. Normalize non-breaking spaces and special spaces to standard space
        text = text.replaceAll("[\u00A0\u2000-\u200A\u202F\u205F\u3000]", " ");

        // 5. PDF ligatures unpacking
        text = text.replace("\uFB00", "ff")
                   .replace("\uFB01", "fi")
                   .replace("\uFB02", "fl")
                   .replace("\uFB03", "ffi")
                   .replace("\uFB04", "ffl");

        // 6. PDF line-wrap hyphen reassembly (e.g., "astro-\nsoft" -> "astrosoft")
        text = text.replaceAll("(?i)([a-z]+)-\\s*\\n\\s*([a-z]+)", "$1$2");

        // 7. Normalize smart/curly quotes, apostrophes, dashes, and ellipses
        text = text.replace('“', '"')
                   .replace('”', '"')
                   .replace('‘', '\'')
                   .replace('’', '\'')
                   .replace('\u201B', '\'')
                   .replace('`', '\'')
                   .replace('—', '-')      // em dash
                   .replace('–', '-')      // en dash
                   .replace('\u2015', '-') // horizontal bar
                   .replace("\u2026", "..."); // ellipsis

        // 8. Normalize bullet characters (Google Docs, Slides bullets, Word mid-dots)
        text = text.replaceAll("(?m)^[ \t]*[•▪▫►⁃·]+\\s*", "• ");

        // 9. Normalize line endings (\r\n -> \n, \r -> \n, Unicode line/para separators -> \n)
        text = text.replaceAll("\r\n|\r|\u2028|\u2029", "\n");

        // 10. Trim trailing whitespace on each line
        text = text.replaceAll("[ \t]+(?=\n)", "");

        // 11. Collapse 3+ consecutive newlines to double newline (max one empty line between paragraphs)
        text = text.replaceAll("\n{3,}", "\n\n");

        // 12. Trim leading and trailing newlines, but preserve leading/trailing inline spaces for cut/paste
        return text.replaceAll("^\n+", "").replaceAll("\n+$", "");
    }

    private static String extractTextFromHtml(String html) {
        try {
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html);
            StringBuilder sb = new StringBuilder();
            doc.body().traverse(new org.jsoup.select.NodeVisitor() {
                @Override
                public void head(org.jsoup.nodes.Node node, int depth) {
                    if (node instanceof org.jsoup.nodes.TextNode) {
                        sb.append(((org.jsoup.nodes.TextNode) node).getWholeText());
                    } else if (node instanceof org.jsoup.nodes.Element) {
                        org.jsoup.nodes.Element el = (org.jsoup.nodes.Element) node;
                        String tag = el.tagName().toLowerCase();
                        if (tag.equals("br") || tag.equals("tr")) {
                            sb.append("\n");
                        } else if (tag.equals("p") || tag.equals("div") || tag.equals("h1") || tag.equals("h2") || tag.equals("h3")) {
                            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
                                sb.append("\n\n");
                            }
                        } else if (tag.equals("li")) {
                            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
                                sb.append("\n");
                            }
                            sb.append("• ");
                        } else if (tag.equals("td") || tag.equals("th")) {
                            sb.append("\t");
                        } else if (tag.equals("img")) {
                            // Extract emoji or image alt text (e.g. WhatsApp Web emoji images)
                            String alt = el.attr("alt");
                            if (!alt.isEmpty()) {
                                sb.append(alt);
                            }
                        }
                    }
                }

                @Override
                public void tail(org.jsoup.nodes.Node node, int depth) {
                    if (node instanceof org.jsoup.nodes.Element) {
                        org.jsoup.nodes.Element el = (org.jsoup.nodes.Element) node;
                        String tag = el.tagName().toLowerCase();
                        if (tag.equals("p") || tag.equals("div")) {
                            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
                                sb.append("\n\n");
                            }
                        } else if (tag.equals("li")) {
                            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
                                sb.append("\n");
                            }
                        }
                    }
                }
            });
            return sb.toString();
        } catch (Throwable t) {
            return html.replaceAll("<[^>]+>", "");
        }
    }

    private Action createSmartPasteAction() {
        return new AbstractAction("Paste") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!editor.isEnabled() || !editor.isEditable()) return;
                try {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    String rawText = null;

                    if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                        rawText = (String) clipboard.getData(DataFlavor.stringFlavor);
                    } else {
                        for (DataFlavor df : clipboard.getAvailableDataFlavors()) {
                            if (df.getMimeType().toLowerCase().startsWith("text/html")) {
                                Object data = clipboard.getData(df);
                                if (data instanceof String) {
                                    rawText = (String) data;
                                } else if (data instanceof java.io.Reader) {
                                    java.io.Reader r = (java.io.Reader) data;
                                    StringBuilder sb = new StringBuilder();
                                    char[] buf = new char[1024];
                                    int n;
                                    while ((n = r.read(buf)) != -1) {
                                        sb.append(buf, 0, n);
                                    }
                                    rawText = sb.toString();
                                }
                                break;
                            }
                        }
                    }

                    if (rawText != null && !rawText.isEmpty()) {
                        String cleaned = cleanAndSanitizeText(rawText);
                        if (!cleaned.isEmpty()) {
                            editor.replaceSelection(cleaned);
                            return;
                        }
                    }
                } catch (Exception ex) {
                    // Fall back to editor.paste()
                }

                editor.paste();
            }
        };
    }

    private void setupKeyBindings(Action boldAction, Action italicAction, Action underlineAction, Action smartPasteAction) {
        InputMap im = editor.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = editor.getActionMap();

        int meta = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, meta), "font-bold");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, meta), "font-italic");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, meta), "font-underline");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, meta), "smart-paste");

        // Windows Ctrl fallback
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK), "font-bold");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK), "font-italic");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), "font-underline");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), "smart-paste");

        am.put("font-bold", boldAction);
        am.put("font-italic", italicAction);
        am.put("font-underline", underlineAction);
        am.put("smart-paste", smartPasteAction);
        am.put(DefaultEditorKit.pasteAction, smartPasteAction);
    }

    private void setupContextMenu(Action smartPasteAction) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem cutItem = new JMenuItem(new DefaultEditorKit.CutAction());
        cutItem.setText("Cut");
        JMenuItem copyItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        copyItem.setText("Copy");
        JMenuItem pasteItem = new JMenuItem(smartPasteAction);
        pasteItem.setText("Paste");
        JMenuItem selectAllItem = new JMenuItem("Select All");
        selectAllItem.addActionListener(e -> editor.selectAll());

        popup.add(cutItem);
        popup.add(copyItem);
        popup.add(pasteItem);
        popup.addSeparator();
        popup.add(selectAllItem);

        editor.setComponentPopupMenu(popup);
    }

    private void insertTimestamp() {
        if (!editor.isEnabled()) return;
        String stamp = "<b>[" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "]</b> ";
        try {
            HTMLEditorKit htmlKit = (HTMLEditorKit) editor.getEditorKit();
            HTMLDocument htmlDoc = (HTMLDocument) editor.getDocument();
            int pos = editor.getCaretPosition();
            htmlKit.insertHTML(htmlDoc, pos, stamp, 0, 0, HTML.Tag.B);
            editor.requestFocusInWindow();
        } catch (Exception ex) {
            editor.replaceSelection("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "] ");
        }
    }

    private void syncToHoroscope() {
        if (isUpdating || horoscope == null) return;

        String content = getNotesContent();
        horoscope.setNotes(content);

        if (listener != null) {
            listener.onNotesModified();
        }
    }

    public boolean isEmpty() {
        try {
            String text = editor.getDocument().getText(0, editor.getDocument().getLength()).trim();
            return text.isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    public String getNotesContent() {
        if (isEmpty()) {
            return "";
        }
        return editor.getText();
    }

    public void setHoroscope(Horoscope h) {
        this.horoscope = h;
        isUpdating = true;
        try {
            // Keep title concise as "Notes"
            titleLabel.setText("Notes");
            if (h != null) {
                titleLabel.setToolTipText("Notes for " + (h.getTitle() != null ? h.getTitle() : ""));
                setEnabledState(true);
                String notes = h.getNotes();
                setNotesContent(notes);
            } else {
                titleLabel.setToolTipText("Notes");
                editor.setText("");
                setEnabledState(false);
            }
        } finally {
            isUpdating = false;
        }
    }

    private void setNotesContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            editor.setText("");
            return;
        }

        String trimmed = content.trim();
        // If content is plain text (without <html>), format it into clean HTML paragraphs
        if (!trimmed.toLowerCase().startsWith("<html>")) {
            String cleaned = cleanAndSanitizeText(content);
            String escaped = cleaned.replace("&", "&amp;")
                                    .replace("<", "&lt;")
                                    .replace(">", "&gt;");
            String[] paragraphs = escaped.split("\n\n");
            StringBuilder sb = new StringBuilder("<html><head></head><body>");
            for (String para : paragraphs) {
                String trimmedPara = para.trim();
                if (!trimmedPara.isEmpty()) {
                    String p = trimmedPara.replace("\n", "<br>");
                    sb.append("<p>").append(p).append("</p>");
                }
            }
            sb.append("</body></html>");
            editor.setText(sb.toString());
        } else {
            editor.setText(content);
        }
    }

    public void setDocked(boolean docked) {
        this.isDocked = docked;
        if (docked) {
            popOutBtn.setText("↗");
            popOutBtn.setToolTipText("Pop out to floating window");
            foldBtn.setVisible(true);
        } else {
            popOutBtn.setText("↙");
            popOutBtn.setToolTipText("Dock back to sidebar");
            foldBtn.setVisible(false);
        }
    }

    public boolean isDocked() {
        return isDocked;
    }

    private void setEnabledState(boolean enabled) {
        editor.setEnabled(enabled);
        boldBtn.setEnabled(enabled);
        italicBtn.setEnabled(enabled);
        underlineBtn.setEnabled(enabled);
        timeBtn.setEnabled(enabled);
    }

    public JTextPane getEditor() {
        return editor;
    }
}
