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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;

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
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import app.astrosoft.beans.NotesOwner;
import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.util.UIConsts;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class ChartNotesPanel extends JPanel {

    public interface NotesListener {
        void onFold();
        void onTogglePopOut(boolean isPopOut);
        void onNotesModified();
    }

    private NotesOwner notesOwner;
    private Horoscope horoscope;
    private JTextPane editor;
    private JButton popOutBtn;
    private JButton foldBtn;
    private JButton boldBtn;
    private JButton italicBtn;
    private JButton underlineBtn;
    private JButton bulletBtn;
    private JButton numListBtn;
    private JButton deTabBtn;
    private JButton tabBtn;
    private JButton btnFontDec;
    private JButton btnFontInc;
    private JButton btnFontReset;

    private static int fontOffset = 0;
    private static final int BASE_FONT_SIZE = 13;
    private static final int MIN_FONT_OFFSET = -4; // 9pt
    private static final int MAX_FONT_OFFSET = 11; // 24pt
    private JLabel titleLabel;
    private boolean isUpdating = false;
    private boolean isDocked = true;
    private NotesListener listener;
    private NotesUndoManager undoManager;

    private static class NotesUndoManager extends UndoManager {
        private CompoundEdit compoundEdit;

        public void startCompoundEdit() {
            if (compoundEdit != null) {
                compoundEdit.end();
            }
            compoundEdit = new CompoundEdit();
        }

        public void endCompoundEdit() {
            if (compoundEdit != null) {
                compoundEdit.end();
                addEdit(compoundEdit);
                compoundEdit = null;
            }
        }

        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            if (compoundEdit != null) {
                compoundEdit.addEdit(e.getEdit());
            } else {
                super.undoableEditHappened(e);
            }
        }
    }

    private static class PopOutIcon implements javax.swing.Icon {
        public int getIconWidth() { return 11; }
        public int getIconHeight() { return 11; }
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c != null ? c.getForeground() : Color.DARK_GRAY);
            g2.setStroke(new java.awt.BasicStroke(1.2f));
            // Window box
            g2.drawRect(x + 1, y + 3, 6, 6);
            // Arrow pointing out (top-right)
            g2.drawLine(x + 5, y + 5, x + 10, y);
            g2.drawLine(x + 7, y, x + 10, y);
            g2.drawLine(x + 10, y, x + 10, y + 3);
            g2.dispose();
        }
    }

    private static class DockIcon implements javax.swing.Icon {
        public int getIconWidth() { return 11; }
        public int getIconHeight() { return 11; }
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c != null ? c.getForeground() : Color.DARK_GRAY);
            g2.setStroke(new java.awt.BasicStroke(1.2f));
            // Window box
            g2.drawRect(x + 1, y + 3, 6, 6);
            // Arrow pointing in (down-left)
            g2.drawLine(x + 10, y, x + 5, y + 5);
            g2.drawLine(x + 8, y + 5, x + 5, y + 5);
            g2.drawLine(x + 5, y + 2, x + 5, y + 5);
            g2.dispose();
        }
    }

    private static class FoldIcon implements javax.swing.Icon {
        public int getIconWidth() { return 9; }
        public int getIconHeight() { return 9; }
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c != null ? c.getForeground() : Color.DARK_GRAY);
            int[] xp = {x + 1, x + 7, x + 1};
            int[] yp = {y + 1, y + 5, y + 9};
            g2.fillPolygon(xp, yp, 3);
            g2.dispose();
        }
    }

    private static class DeTabIcon implements javax.swing.Icon {
        public int getIconWidth() { return 13; }
        public int getIconHeight() { return 12; }
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c != null ? c.getForeground() : Color.DARK_GRAY);
            g2.setStroke(new java.awt.BasicStroke(1.2f));
            // 3 text lines
            g2.drawLine(x + 5, y + 2, x + 12, y + 2);
            g2.drawLine(x + 5, y + 6, x + 12, y + 6);
            g2.drawLine(x + 5, y + 10, x + 12, y + 10);
            // Left-pointing arrow (outdent)
            g2.drawLine(x + 3, y + 6, x, y + 6);
            g2.drawLine(x, y + 6, x + 2, y + 4);
            g2.drawLine(x, y + 6, x + 2, y + 8);
            g2.dispose();
        }
    }

    private static class TabIcon implements javax.swing.Icon {
        public int getIconWidth() { return 13; }
        public int getIconHeight() { return 12; }
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c != null ? c.getForeground() : Color.DARK_GRAY);
            g2.setStroke(new java.awt.BasicStroke(1.2f));
            // 3 text lines
            g2.drawLine(x + 5, y + 2, x + 12, y + 2);
            g2.drawLine(x + 5, y + 6, x + 12, y + 6);
            g2.drawLine(x + 5, y + 10, x + 12, y + 10);
            // Right-pointing arrow (indent)
            g2.drawLine(x, y + 6, x + 3, y + 6);
            g2.drawLine(x + 3, y + 6, x + 1, y + 4);
            g2.drawLine(x + 3, y + 6, x + 1, y + 8);
            g2.dispose();
        }
    }

    public ChartNotesPanel(NotesListener listener) {
        this.listener = listener;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(165, 165, 165)));
        setPreferredSize(new Dimension(340, 400));
        setMinimumSize(new Dimension(200, 200));

        // 1. Top Header Bar - Clean "Notes" title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIConsts.PEARL_GREEN);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(165, 165, 165)),
                BorderFactory.createEmptyBorder(5, 12, 5, 6)
        ));

        titleLabel = new JLabel("Notes");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13.0f));
        titleLabel.setForeground(UIConsts.DARK_BLUE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel headerControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        headerControls.setOpaque(false);

        popOutBtn = new JButton(new PopOutIcon());
        popOutBtn.setToolTipText("Pop out to floating window");
        popOutBtn.setMargin(new Insets(2, 5, 2, 5));
        popOutBtn.setFocusable(false);
        popOutBtn.addActionListener(e -> {
            if (listener != null) {
                listener.onTogglePopOut(isDocked);
            }
        });

        foldBtn = new JButton(new FoldIcon());
        foldBtn.setToolTipText("Fold / Close Notes");
        foldBtn.setMargin(new Insets(2, 5, 2, 5));
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

        Action bulletAction = new AbstractAction("Bulleted List") {
            public void actionPerformed(ActionEvent e) {
                toggleList(false);
            }
        };
        bulletBtn = new JButton("•");
        bulletBtn.setFont(bulletBtn.getFont().deriveFont(Font.BOLD, 14.0f));
        bulletBtn.setToolTipText("Bulleted List (Cmd+Shift+8 / Ctrl+Shift+8)");
        bulletBtn.setMargin(new Insets(2, 6, 2, 6));
        bulletBtn.setFocusable(false);
        bulletBtn.addActionListener(bulletAction);

        Action numListAction = new AbstractAction("Numbered List") {
            public void actionPerformed(ActionEvent e) {
                toggleList(true);
            }
        };
        numListBtn = new JButton("1.");
        numListBtn.setFont(numListBtn.getFont().deriveFont(Font.BOLD, 11.5f));
        numListBtn.setToolTipText("Numbered List (Cmd+Shift+7 / Ctrl+Shift+7)");
        numListBtn.setMargin(new Insets(2, 5, 2, 5));
        numListBtn.setFocusable(false);
        numListBtn.addActionListener(numListAction);

        Action deTabAction = new AbstractAction("De-tab / Outdent") {
            public void actionPerformed(ActionEvent e) {
                handleTab(true);
            }
        };
        deTabBtn = new JButton(new DeTabIcon());
        deTabBtn.setToolTipText("De-tab / Outdent (Shift+Tab / Cmd+[)");
        deTabBtn.setMargin(new Insets(2, 5, 2, 5));
        deTabBtn.setFocusable(false);
        deTabBtn.addActionListener(deTabAction);

        Action tabAction = new AbstractAction("Tab / Indent") {
            public void actionPerformed(ActionEvent e) {
                handleTab(false);
            }
        };
        tabBtn = new JButton(new TabIcon());
        tabBtn.setToolTipText("Tab / Indent (Tab / Cmd+])");
        tabBtn.setMargin(new Insets(2, 5, 2, 5));
        tabBtn.setFocusable(false);
        tabBtn.addActionListener(tabAction);

        btnFontDec = new JButton("A-");
        btnFontDec.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        btnFontDec.setToolTipText("Decrease Notes Font Size (Ctrl+- / Cmd+-)");
        btnFontDec.setFocusable(false);
        btnFontDec.setMargin(new Insets(1, 3, 1, 3));
        btnFontDec.setPreferredSize(new Dimension(28, 22));
        btnFontDec.setForeground(UIConsts.TEXT_DARK_NAVY);
        btnFontDec.addActionListener(e -> adjustFontSize(-1));

        btnFontInc = new JButton("A+");
        btnFontInc.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        btnFontInc.setToolTipText("Increase Notes Font Size (Ctrl++ / Cmd++)");
        btnFontInc.setFocusable(false);
        btnFontInc.setMargin(new Insets(1, 3, 1, 3));
        btnFontInc.setPreferredSize(new Dimension(28, 22));
        btnFontInc.setForeground(UIConsts.TEXT_DARK_NAVY);
        btnFontInc.addActionListener(e -> adjustFontSize(1));

        int percent = (int) Math.round(((double) getCurrentFontSize() / BASE_FONT_SIZE) * 100);
        btnFontReset = new JButton(percent + "%");
        btnFontReset.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        btnFontReset.setToolTipText("Reset Notes Font Size to 100% (Ctrl+0 / Cmd+0)");
        btnFontReset.setFocusable(false);
        btnFontReset.setMargin(new Insets(1, 2, 1, 2));
        btnFontReset.setPreferredSize(new Dimension(42, 22));
        btnFontReset.setForeground(UIConsts.LABEL_MUTED_COLOR);
        btnFontReset.addActionListener(e -> resetFontSize());

        formatToolbar.add(Box.createHorizontalStrut(6));
        formatToolbar.add(boldBtn);
        formatToolbar.add(italicBtn);
        formatToolbar.add(underlineBtn);
        formatToolbar.addSeparator(new Dimension(8, 16));
        formatToolbar.add(bulletBtn);
        formatToolbar.add(numListBtn);
        formatToolbar.add(deTabBtn);
        formatToolbar.add(tabBtn);
        formatToolbar.addSeparator(new Dimension(8, 16));
        formatToolbar.add(btnFontDec);
        formatToolbar.add(btnFontInc);
        formatToolbar.add(btnFontReset);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(formatToolbar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // 3. Editor & Typography Document
        editor = new JTextPane();
        editor.setFocusTraversalKeysEnabled(false);
        editor.setContentType("text/html");
        editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        Font baseFont = new Font("Arial", Font.PLAIN, BASE_FONT_SIZE);
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
        styleSheet.addRule("table { border-collapse: collapse; margin-top: 4px; margin-bottom: 8px; }");
        styleSheet.addRule("th { background-color: #f0f4f8; font-weight: bold; padding: 4px 8px; text-align: left; }");
        styleSheet.addRule("td { padding: 4px 8px; vertical-align: top; }");
        editor.setEditorKit(kit);
        applyNotesFontSize();

        // Smart Paste Action for proper plain-text / rich-text handling
        Action smartPasteAction = createSmartPasteAction();

        this.undoManager = new NotesUndoManager();
        editor.getDocument().addUndoableEditListener(this.undoManager);

        Action undoAction = new AbstractAction("Undo") {
            public void actionPerformed(ActionEvent e) {
                if (undoManager != null && undoManager.canUndo()) undoManager.undo();
            }
        };
        Action redoAction = new AbstractAction("Redo") {
            public void actionPerformed(ActionEvent e) {
                if (undoManager != null && undoManager.canRedo()) undoManager.redo();
            }
        };

        // Mac & Windows shortcut bindings
        setupKeyBindings(boldAction, italicAction, underlineAction, bulletAction, numListAction, tabAction, deTabAction, smartPasteAction, undoAction, redoAction);

        // Context Popup Menu (Undo, Redo, Cut, Copy, Paste, Bullets, Numbers, Indent, Outdent, Select All)
        setupContextMenu(smartPasteAction, undoAction, redoAction, bulletAction, numListAction, tabAction, deTabAction);

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

    public static boolean isTabDelimitedTable(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        String[] lines = text.split("\r?\n");
        int rowsWithTabs = 0;
        int nonBlankRows = 0;
        int expectedTabs = -1;
        boolean consistentTabs = true;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            nonBlankRows++;
            int tabs = 0;
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == '\t') tabs++;
            }
            if (tabs > 0) {
                rowsWithTabs++;
                if (expectedTabs == -1) {
                    expectedTabs = tabs;
                } else if (Math.abs(tabs - expectedTabs) > 1) {
                    consistentTabs = false;
                }
            }
        }
        return nonBlankRows >= 2 && rowsWithTabs >= 2 && consistentTabs;
    }

    public static String convertTabDelimitedToHtmlTable(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        String[] lines = text.split("\r?\n");
        StringBuilder sb = new StringBuilder();
        sb.append("<table border=\"1\" cellpadding=\"4\" cellspacing=\"0\" style=\"border-collapse: collapse; margin-top: 4px; margin-bottom: 6px;\">");
        boolean isHeader = true;
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] cells = line.split("\t", -1);
            sb.append("<tr>");
            for (String cell : cells) {
                String cleanCell = cleanAndSanitizeText(cell).trim();
                String escaped = cleanCell.replace("&", "&amp;")
                                          .replace("<", "&lt;")
                                          .replace(">", "&gt;");
                if (escaped.isEmpty()) {
                    escaped = "&nbsp;";
                }
                if (isHeader) {
                    sb.append("<th bgcolor=\"#f2f2f2\" align=\"left\"><b>").append(escaped).append("</b></th>");
                } else {
                    sb.append("<td>").append(escaped).append("</td>");
                }
            }
            sb.append("</tr>");
            isHeader = false;
        }
        sb.append("</table>");
        return sb.toString();
    }

    public static String sanitizeHtmlContentWithTables(String html) {
        if (html == null || html.trim().isEmpty()) return "";
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            // Remove style, script, xml, meta, link tags
            doc.select("style, script, xml, meta, link, o\\:p").remove();

            org.jsoup.select.Elements tables = doc.select("table");
            if (tables.isEmpty()) {
                return null;
            }

            for (org.jsoup.nodes.Element table : tables) {
                table.removeAttr("class");
                table.removeAttr("id");
                table.removeAttr("style");
                table.attr("border", "1");
                table.attr("cellpadding", "4");
                table.attr("cellspacing", "0");
                table.attr("style", "border-collapse: collapse; margin-top: 4px; margin-bottom: 6px;");

                org.jsoup.select.Elements rows = table.select("tr");
                boolean firstRow = true;
                for (org.jsoup.nodes.Element tr : rows) {
                    tr.removeAttr("class");
                    tr.removeAttr("id");
                    tr.removeAttr("style");

                    org.jsoup.select.Elements cells = tr.children();
                    for (org.jsoup.nodes.Element cell : cells) {
                        String tagName = cell.tagName().toLowerCase();
                        if (!tagName.equals("td") && !tagName.equals("th")) {
                            continue;
                        }
                        cell.removeAttr("class");
                        cell.removeAttr("id");
                        cell.removeAttr("style");

                        // Unwrap non-formatting tags inside cells
                        org.jsoup.select.Elements nonFormatting = cell.select("*:not(td, th, b, strong, i, em, u, br)");
                        for (org.jsoup.nodes.Element el : nonFormatting) {
                            el.unwrap();
                        }

                        String cellText = cell.text().trim();
                        if (cellText.isEmpty()) {
                            cell.html("&nbsp;");
                        } else if (tagName.equals("th") || (firstRow && rows.size() > 1)) {
                            cell.attr("bgcolor", "#f2f2f2");
                            String inner = cell.html().trim();
                            if (!inner.startsWith("<b>") && !inner.startsWith("<strong>")) {
                                cell.html("<b>" + inner + "</b>");
                            }
                        }
                    }
                    firstRow = false;
                }
            }

            return doc.body().html();
        } catch (Exception e) {
            return null;
        }
    }

    public void insertHtmlTableAtCaret(String tableHtml) {
        if (!editor.isEnabled() || !editor.isEditable()) return;
        try {
            int pos = editor.getCaretPosition();
            editor.replaceSelection("");
            pos = editor.getCaretPosition();
            HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
            HTMLDocument doc = (HTMLDocument) editor.getDocument();
            if (doc.getLength() == 0 || editor.getText().replaceAll("<[^>]*>", "").trim().isEmpty()) {
                editor.setText("<html><head></head><body>" + tableHtml + "<p><br></p></body></html>");
            } else {
                try {
                    kit.insertHTML(doc, pos, tableHtml, 0, 0, HTML.Tag.TABLE);
                } catch (Exception ex) {
                    kit.insertHTML(doc, doc.getLength(), "<p><br></p>" + tableHtml + "<p><br></p>", 0, 0, HTML.Tag.TABLE);
                }
            }
        } catch (Exception ex) {
            editor.replaceSelection("\n" + tableHtml + "\n");
        }
    }

    private Action createSmartPasteAction() {
        return new AbstractAction("Paste") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!editor.isEnabled() || !editor.isEditable()) return;
                try {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    String htmlData = null;
                    String rawText = null;

                    // 1. Inspect text/html flavor first (for Excel, Google Sheets, rich web tables)
                    for (DataFlavor df : clipboard.getAvailableDataFlavors()) {
                        if (df.getMimeType().toLowerCase().startsWith("text/html")) {
                            try {
                                Object data = clipboard.getData(df);
                                if (data instanceof String) {
                                    htmlData = (String) data;
                                } else if (data instanceof java.io.Reader) {
                                    java.io.Reader r = (java.io.Reader) data;
                                    StringBuilder sb = new StringBuilder();
                                    char[] buf = new char[1024];
                                    int n;
                                    while ((n = r.read(buf)) != -1) {
                                        sb.append(buf, 0, n);
                                    }
                                    htmlData = sb.toString();
                                }
                            } catch (Throwable ignored) {}
                            if (htmlData != null) break;
                        }
                    }

                    // 2. Inspect plain text flavor
                    if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                        try {
                            rawText = (String) clipboard.getData(DataFlavor.stringFlavor);
                        } catch (Throwable ignored) {}
                    }

                    // 3. If HTML contains a table, sanitize and insert it
                    if (htmlData != null && htmlData.toLowerCase().contains("<table")) {
                        String sanitizedTable = sanitizeHtmlContentWithTables(htmlData);
                        if (sanitizedTable != null && !sanitizedTable.trim().isEmpty()) {
                            insertHtmlTableAtCaret(sanitizedTable);
                            return;
                        }
                    }

                    // 4. If plain text is tab-delimited tabular data (e.g. copied from Excel/Sheets as TSV)
                    if (rawText != null && isTabDelimitedTable(rawText)) {
                        String tableHtml = convertTabDelimitedToHtmlTable(rawText);
                        if (tableHtml != null && !tableHtml.trim().isEmpty()) {
                            insertHtmlTableAtCaret(tableHtml);
                            return;
                        }
                    }

                    // 5. Standard plain text / rich text paste
                    if (rawText != null && !rawText.isEmpty()) {
                        String cleaned = cleanAndSanitizeText(rawText);
                        if (!cleaned.isEmpty()) {
                            if (!cleaned.contains("\n")) {
                                editor.replaceSelection(cleaned);
                            } else {
                                insertFormattedTextAtCaret(cleaned);
                            }
                            return;
                        }
                    }

                    // 6. If plain text was empty but HTML is available (without table)
                    if (htmlData != null && !htmlData.trim().isEmpty()) {
                        String extracted = extractTextFromHtml(htmlData);
                        String cleaned = cleanAndSanitizeText(extracted);
                        if (!cleaned.isEmpty()) {
                            if (!cleaned.contains("\n")) {
                                editor.replaceSelection(cleaned);
                            } else {
                                insertFormattedTextAtCaret(cleaned);
                            }
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

    private void setupKeyBindings(Action boldAction, Action italicAction, Action underlineAction, Action bulletAction, Action numListAction, Action tabAction, Action deTabAction, Action smartPasteAction, Action undoAction, Action redoAction) {
        InputMap im = editor.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = editor.getActionMap();

        int meta = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, meta), "font-bold");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, meta), "font-italic");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, meta), "font-underline");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_8, meta | InputEvent.SHIFT_DOWN_MASK), "toggle-bullet");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_7, meta | InputEvent.SHIFT_DOWN_MASK), "toggle-numlist");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "indent-tab");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "outdent-tab");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, meta), "indent-tab");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, meta), "outdent-tab");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, meta), "smart-paste");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, meta), DefaultEditorKit.copyAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, meta), DefaultEditorKit.cutAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, meta), DefaultEditorKit.selectAllAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, meta), "undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, meta | InputEvent.SHIFT_DOWN_MASK), "redo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, meta), "redo");

        // Windows Ctrl fallback
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK), "font-bold");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK), "font-italic");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK), "font-underline");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_8, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "toggle-bullet");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_7, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "toggle-numlist");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.CTRL_DOWN_MASK), "indent-tab");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.CTRL_DOWN_MASK), "outdent-tab");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), "smart-paste");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.copyAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.cutAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.selectAllAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "redo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "redo");

        Action incFontAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) { adjustFontSize(1); }
        };
        Action decFontAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) { adjustFontSize(-1); }
        };
        Action resetFontAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) { resetFontSize(); }
        };

        // Mac Meta & Windows Ctrl shortcuts for font sizing
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, meta), "font-inc");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, meta | InputEvent.SHIFT_DOWN_MASK), "font-inc");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, meta), "font-inc");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, meta), "font-dec");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, meta), "font-dec");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, meta), "font-reset");

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK), "font-inc");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "font-inc");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK), "font-inc");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK), "font-dec");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK), "font-dec");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK), "font-reset");

        am.put("font-inc", incFontAction);
        am.put("font-dec", decFontAction);
        am.put("font-reset", resetFontAction);

        am.put("font-bold", boldAction);
        am.put("font-italic", italicAction);
        am.put("font-underline", underlineAction);
        am.put("toggle-bullet", bulletAction);
        am.put("toggle-numlist", numListAction);
        am.put("indent-tab", tabAction);
        am.put("outdent-tab", deTabAction);
        am.put("smart-paste", smartPasteAction);
        am.put(DefaultEditorKit.pasteAction, smartPasteAction);
        am.put("undo", undoAction);
        am.put("redo", redoAction);

        // Smart Enter handling for lists & Shift+Enter soft break
        final Action defaultEnter = am.get(DefaultEditorKit.insertBreakAction);
        Action smartEnterAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!editor.isEnabled() || !editor.isEditable()) return;
                if (!handleSmartEnter()) {
                    if (defaultEnter != null) {
                        defaultEnter.actionPerformed(e);
                    } else {
                        editor.replaceSelection("\n");
                    }
                }
            }
        };
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "smart-enter");
        am.put("smart-enter", smartEnterAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), DefaultEditorKit.insertBreakAction);
    }

    private void setupContextMenu(Action smartPasteAction, Action undoAction, Action redoAction, Action bulletAction, Action numListAction, Action tabAction, Action deTabAction) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem undoItem = new JMenuItem(undoAction);
        JMenuItem redoItem = new JMenuItem(redoAction);
        JMenuItem cutItem = new JMenuItem(new DefaultEditorKit.CutAction());
        cutItem.setText("Cut");
        JMenuItem copyItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        copyItem.setText("Copy");
        JMenuItem pasteItem = new JMenuItem(smartPasteAction);
        pasteItem.setText("Paste");
        JMenuItem bulletItem = new JMenuItem(bulletAction);
        JMenuItem numListItem = new JMenuItem(numListAction);
        JMenuItem deTabItem = new JMenuItem(deTabAction);
        JMenuItem tabItem = new JMenuItem(tabAction);

        JMenuItem incFontItem = new JMenuItem("Increase Font Size (A+)");
        incFontItem.addActionListener(e -> adjustFontSize(1));
        JMenuItem decFontItem = new JMenuItem("Decrease Font Size (A-)");
        decFontItem.addActionListener(e -> adjustFontSize(-1));
        JMenuItem resetFontItem = new JMenuItem("Reset Font Size (100%)");
        resetFontItem.addActionListener(e -> resetFontSize());

        JMenuItem selectAllItem = new JMenuItem("Select All");
        selectAllItem.addActionListener(e -> editor.selectAll());

        popup.add(undoItem);
        popup.add(redoItem);
        popup.addSeparator();
        popup.add(cutItem);
        popup.add(copyItem);
        popup.add(pasteItem);
        popup.addSeparator();
        popup.add(bulletItem);
        popup.add(numListItem);
        popup.add(deTabItem);
        popup.add(tabItem);
        popup.addSeparator();
        popup.add(incFontItem);
        popup.add(decFontItem);
        popup.add(resetFontItem);
        popup.addSeparator();
        popup.add(selectAllItem);

        editor.setComponentPopupMenu(popup);
    }

    public static int getFontOffset() {
        return fontOffset;
    }

    public static int getCurrentFontSize() {
        return BASE_FONT_SIZE + fontOffset;
    }

    public void adjustFontSize(int delta) {
        fontOffset = Math.max(MIN_FONT_OFFSET, Math.min(MAX_FONT_OFFSET, fontOffset + delta));
        applyNotesFontSize();
    }

    public void resetFontSize() {
        fontOffset = 0;
        applyNotesFontSize();
    }

    private void applyNotesFontSize() {
        if (editor == null) return;
        int currentSize = getCurrentFontSize();
        editor.setFont(new Font("Arial", Font.PLAIN, currentSize));
        if (editor.getEditorKit() instanceof HTMLEditorKit) {
            HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
            String rule = "body, p, td, th, li { font-size: " + currentSize + "pt; }";
            kit.getStyleSheet().addRule(rule);
            if (editor.getDocument() instanceof HTMLDocument) {
                ((HTMLDocument) editor.getDocument()).getStyleSheet().addRule(rule);
            }
        }
        int percent = (int) Math.round(((double) currentSize / BASE_FONT_SIZE) * 100);
        if (btnFontReset != null) {
            btnFontReset.setText(percent + "%");
            btnFontReset.setToolTipText("Reset Notes Font Size to 100% (currently " + percent + "% / " + currentSize + "pt)");
        }
        editor.revalidate();
        editor.repaint();
    }

    public void handleTab(boolean outdent) {
        if (!editor.isEnabled() || !editor.isEditable()) return;
        Document doc = editor.getDocument();
        int selStart = Math.min(editor.getSelectionStart(), editor.getSelectionEnd());
        int selEnd = Math.max(editor.getSelectionStart(), editor.getSelectionEnd());

        try {
            int docLen = doc.getLength();
            if (docLen == 0) {
                if (!outdent) {
                    if (undoManager != null) undoManager.startCompoundEdit();
                    try {
                        doc.insertString(0, "\u00A0\u00A0\u00A0\u00A0", null);
                        editor.setCaretPosition(4);
                    } finally {
                        if (undoManager != null) undoManager.endCompoundEdit();
                    }
                }
                editor.requestFocusInWindow();
                return;
            }

            String fullText = doc.getText(0, docLen);

            int lineStart = selStart;
            while (lineStart > 0 && fullText.charAt(lineStart - 1) != '\n') {
                lineStart--;
            }

            int lineEnd = selEnd;
            while (lineEnd < fullText.length() && fullText.charAt(lineEnd) != '\n') {
                lineEnd++;
            }

            // In HTMLDocument, index 0 can be an implied leading newline
            if (lineStart == 0 && lineEnd == 0 && fullText.length() > 0 && fullText.charAt(0) == '\n') {
                lineStart = 1;
                lineEnd = 1;
                while (lineEnd < fullText.length() && fullText.charAt(lineEnd) != '\n') {
                    lineEnd++;
                }
            }

            String targetText = fullText.substring(lineStart, lineEnd);
            String[] lines = targetText.split("\n", -1);
            StringBuilder sb = new StringBuilder();

            final String TAB_STR = "\u00A0\u00A0\u00A0\u00A0";
            boolean isSingleEmpty = (lines.length == 1 && lines[0].isEmpty());

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (outdent) {
                    if (line.startsWith("\t")) {
                        sb.append(line.substring(1));
                    } else {
                        int spacesToRemove = 0;
                        while (spacesToRemove < 4 && spacesToRemove < line.length()) {
                            char c = line.charAt(spacesToRemove);
                            if (c == ' ' || c == '\u00A0') {
                                spacesToRemove++;
                            } else {
                                break;
                            }
                        }
                        sb.append(line.substring(spacesToRemove));
                    }
                } else {
                    if (!line.isEmpty() || isSingleEmpty) {
                        sb.append(TAB_STR).append(line);
                    } else {
                        sb.append(line);
                    }
                }
                if (i < lines.length - 1) {
                    sb.append("\n");
                }
            }

            String replacement = sb.toString();
            if (undoManager != null) {
                undoManager.startCompoundEdit();
            }
            try {
                if (doc instanceof javax.swing.text.AbstractDocument) {
                    ((javax.swing.text.AbstractDocument) doc).replace(lineStart, lineEnd - lineStart, replacement, null);
                } else {
                    doc.remove(lineStart, lineEnd - lineStart);
                    doc.insertString(lineStart, replacement, null);
                }
            } finally {
                if (undoManager != null) {
                    undoManager.endCompoundEdit();
                }
            }

            if (selStart != selEnd) {
                editor.select(lineStart, lineStart + replacement.length());
            } else {
                int newCaret = Math.min(lineStart + replacement.length(), doc.getLength());
                editor.setCaretPosition(newCaret);
            }
            editor.requestFocusInWindow();
        } catch (Exception ex) {
            // Keep editor responsive
        }
    }

    public void toggleList(boolean isNumbered) {
        if (!editor.isEnabled() || !editor.isEditable()) return;
        Document doc = editor.getDocument();
        int selStart = Math.min(editor.getSelectionStart(), editor.getSelectionEnd());
        int selEnd = Math.max(editor.getSelectionStart(), editor.getSelectionEnd());
        try {
            int docLen = doc.getLength();
            if (docLen == 0) {
                String bullet = isNumbered ? "1. " : "• ";
                if (undoManager != null) undoManager.startCompoundEdit();
                try {
                    doc.insertString(0, bullet, null);
                    editor.setCaretPosition(bullet.length());
                } finally {
                    if (undoManager != null) undoManager.endCompoundEdit();
                }
                editor.requestFocusInWindow();
                return;
            }

            String fullText = doc.getText(0, docLen);

            int lineStart = selStart;
            while (lineStart > 0 && fullText.charAt(lineStart - 1) != '\n') {
                lineStart--;
            }

            int lineEnd = selEnd;
            while (lineEnd < fullText.length() && fullText.charAt(lineEnd) != '\n') {
                lineEnd++;
            }

            // In HTMLDocument, index 0 can be an implied leading newline
            if (lineStart == 0 && lineEnd == 0 && fullText.length() > 0 && fullText.charAt(0) == '\n') {
                lineStart = 1;
                lineEnd = 1;
                while (lineEnd < fullText.length() && fullText.charAt(lineEnd) != '\n') {
                    lineEnd++;
                }
            }

            String targetText = fullText.substring(lineStart, lineEnd);
            String[] lines = targetText.split("\n", -1);

            boolean allMatches = true;
            int nonEmptyCount = 0;
            for (String l : lines) {
                if (l.trim().isEmpty()) continue;
                nonEmptyCount++;
                if (isNumbered) {
                    if (!l.matches("^\\s*\\d+\\.\\s*.*")) {
                        allMatches = false;
                        break;
                    }
                } else {
                    if (!l.matches("^\\s*•\\s*.*")) {
                        allMatches = false;
                        break;
                    }
                }
            }

            if (nonEmptyCount == 0) {
                allMatches = false;
            }

            StringBuilder sb = new StringBuilder();
            int counter = 1;
            boolean isSingleEmptyLine = (lines.length == 1 && lines[0].trim().isEmpty());

            if (isSingleEmptyLine) {
                sb.append(isNumbered ? "1. " : "• ");
            } else {
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (line.trim().isEmpty()) {
                        sb.append(line);
                    } else if (allMatches) {
                        // Toggle off
                        if (isNumbered) {
                            sb.append(line.replaceFirst("^\\s*\\d+\\.\\s*", ""));
                        } else {
                            sb.append(line.replaceFirst("^\\s*•\\s*", ""));
                        }
                    } else {
                        // Format into list
                        String cleanLine = line.replaceFirst("^\\s*([•]|\\d+\\.)\\s*", "");
                        if (isNumbered) {
                            sb.append(counter++).append(". ").append(cleanLine);
                        } else {
                            sb.append("• ").append(cleanLine);
                        }
                    }
                    if (i < lines.length - 1) {
                        sb.append("\n");
                    }
                }
            }

            String replacement = sb.toString();
            if (undoManager != null) {
                undoManager.startCompoundEdit();
            }
            try {
                if (doc instanceof javax.swing.text.AbstractDocument) {
                    ((javax.swing.text.AbstractDocument) doc).replace(lineStart, lineEnd - lineStart, replacement, null);
                } else {
                    doc.remove(lineStart, lineEnd - lineStart);
                    doc.insertString(lineStart, replacement, null);
                }
            } finally {
                if (undoManager != null) {
                    undoManager.endCompoundEdit();
                }
            }
            if (selStart != selEnd) {
                editor.select(lineStart, lineStart + replacement.length());
            } else {
                editor.setCaretPosition(Math.min(lineStart + replacement.length(), doc.getLength()));
            }
            editor.requestFocusInWindow();
        } catch (Exception ex) {
            // Ignore / keep editor responsive
        }
    }

    public boolean handleSmartEnter() {
        if (!editor.isEnabled() || !editor.isEditable()) return false;
        if (editor.getSelectionStart() != editor.getSelectionEnd()) {
            return false;
        }
        Document doc = editor.getDocument();
        int caret = editor.getCaretPosition();
        try {
            String fullText = doc.getText(0, doc.getLength());
            int lineStart = fullText.lastIndexOf('\n', caret - 1);
            lineStart = (lineStart == -1) ? 0 : lineStart + 1;

            int lineEnd = fullText.indexOf('\n', caret);
            lineEnd = (lineEnd == -1) ? doc.getLength() : lineEnd;

            String beforeCaret = fullText.substring(lineStart, caret);
            String wholeLine = fullText.substring(lineStart, lineEnd);

            // 1. Bullet list
            java.util.regex.Matcher bm = java.util.regex.Pattern.compile("^([\\s\\u00A0]*)•\\s*(.*)").matcher(wholeLine);
            if (bm.matches()) {
                String indent = bm.group(1);
                String content = bm.group(2).trim();
                if (content.isEmpty()) {
                    // Empty bullet line -> Exit list
                    if (undoManager != null) undoManager.startCompoundEdit();
                    try {
                        doc.remove(lineStart, lineEnd - lineStart);
                    } finally {
                        if (undoManager != null) undoManager.endCompoundEdit();
                    }
                    return true;
                } else if (beforeCaret.matches("^[\\s\\u00A0]*•\\s*.*")) {
                    String next = "\n" + indent + "• ";
                    if (undoManager != null) undoManager.startCompoundEdit();
                    try {
                        doc.insertString(caret, next, null);
                        editor.setCaretPosition(caret + next.length());
                    } finally {
                        if (undoManager != null) undoManager.endCompoundEdit();
                    }
                    return true;
                }
            }

            // 2. Numbered list
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("^([\\s\\u00A0]*)(\\d+)\\.\\s*(.*)").matcher(wholeLine);
            if (m.matches()) {
                String indent = m.group(1);
                int num = Integer.parseInt(m.group(2));
                String content = m.group(3).trim();
                if (content.isEmpty()) {
                    // Empty numbered line -> Exit list
                    if (undoManager != null) undoManager.startCompoundEdit();
                    try {
                        doc.remove(lineStart, lineEnd - lineStart);
                    } finally {
                        if (undoManager != null) undoManager.endCompoundEdit();
                    }
                    return true;
                } else if (beforeCaret.matches("^[\\s\\u00A0]*\\d+\\.\\s*.*")) {
                    String next = "\n" + indent + (num + 1) + ". ";
                    if (undoManager != null) undoManager.startCompoundEdit();
                    try {
                        doc.insertString(caret, next, null);
                        editor.setCaretPosition(caret + next.length());
                    } finally {
                        if (undoManager != null) undoManager.endCompoundEdit();
                    }
                    return true;
                }
            }
        } catch (Exception ex) {
            // Fall back
        }
        return false;
    }

    public void flushToOwner() {
        if (notesOwner == null) return;
        boolean wasModified = notesOwner.isModified();
        String content = getNotesContent();
        notesOwner.setNotes(content);
        notesOwner.setModified(wasModified);
    }

    public void flushToHoroscope() {
        flushToOwner();
    }

    private void syncToHoroscope() {
        if (isUpdating || notesOwner == null) return;

        String content = getNotesContent();
        notesOwner.setNotes(content);
        notesOwner.setModified(true);

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
        bindNotesOwner(h);
    }

    public void bindNotesOwner(NotesOwner owner) {
        this.notesOwner = owner;
        this.horoscope = (owner instanceof Horoscope) ? (Horoscope) owner : null;
        isUpdating = true;
        try {
            titleLabel.setText("Notes");
            if (owner != null) {
                String ownerTitle = owner.getNotesOwnerTitle();
                titleLabel.setToolTipText("Notes for " + (ownerTitle != null ? ownerTitle : ""));
                setEnabledState(true);
                String notes = owner.getNotes();
                setNotesContent(notes);
            } else {
                titleLabel.setToolTipText("Notes");
                editor.setText("");
                setEnabledState(false);
                if (undoManager != null) {
                    undoManager.discardAllEdits();
                }
            }
        } finally {
            isUpdating = false;
        }
    }

    public NotesOwner getNotesOwner() {
        return notesOwner;
    }

    private void setNotesContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            editor.setText("");
            if (undoManager != null) {
                undoManager.discardAllEdits();
            }
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
            editor.setText(normalizeHtmlContent(content));
        }
        if (undoManager != null) {
            undoManager.discardAllEdits();
        }
        applyNotesFontSize();
    }

    private void insertFormattedTextAtCaret(String cleaned) {
        String escaped = cleaned.replace("&", "&amp;")
                                .replace("<", "&lt;")
                                .replace(">", "&gt;");
        String[] paras = escaped.split("\n\n+");
        StringBuilder html = new StringBuilder();
        for (String p : paras) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                String withBr = trimmed.replace("\n", "<br>");
                html.append("<p style=\"margin-top: 0\">").append(withBr).append("</p>");
            }
        }

        try {
            int pos = editor.getCaretPosition();
            editor.replaceSelection("");
            pos = editor.getCaretPosition();
            HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
            HTMLDocument doc = (HTMLDocument) editor.getDocument();
            if (doc.getLength() == 0 || editor.getText().replaceAll("<[^>]*>", "").trim().isEmpty()) {
                editor.setText("<html><head></head><body>" + html.toString() + "</body></html>");
            } else {
                kit.insertHTML(doc, pos, html.toString(), 0, 0, HTML.Tag.P);
            }
        } catch (Exception ex) {
            editor.replaceSelection(cleaned);
        }
    }

    public static String normalizeHtmlContent(String html) {
        if (html == null || !html.trim().toLowerCase().startsWith("<html>")) {
            return html;
        }
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            doc.outputSettings().prettyPrint(false);

            boolean hasUnwrappedText = false;
            for (Node child : doc.body().childNodes()) {
                if (child instanceof TextNode) {
                    if (!((TextNode) child).getWholeText().trim().isEmpty()) {
                        hasUnwrappedText = true;
                        break;
                    }
                }
            }

            if (!hasUnwrappedText) {
                return html;
            }

            StringBuilder out = new StringBuilder("<html><head></head><body>");
            for (Node child : doc.body().childNodes()) {
                if (child instanceof TextNode) {
                    String text = ((TextNode) child).getWholeText();
                    String[] paras = text.split("\n\n+");
                    for (String para : paras) {
                        String p = para.trim();
                        if (!p.isEmpty()) {
                            String withBr = p.replace("&", "&amp;")
                                             .replace("<", "&lt;")
                                             .replace(">", "&gt;")
                                             .replace("\n", "<br>");
                            out.append("<p style=\"margin-top: 0\">").append(withBr).append("</p>");
                        }
                    }
                } else if (child instanceof Element) {
                    out.append(((Element) child).outerHtml());
                }
            }
            out.append("</body></html>");
            return out.toString();
        } catch (Exception e) {
            return html;
        }
    }

    public void setDocked(boolean docked) {
        this.isDocked = docked;
        if (docked) {
            popOutBtn.setIcon(new PopOutIcon());
            popOutBtn.setToolTipText("Pop out to floating window");
            foldBtn.setVisible(true);
        } else {
            popOutBtn.setIcon(new DockIcon());
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
        bulletBtn.setEnabled(enabled);
        numListBtn.setEnabled(enabled);
        if (deTabBtn != null) deTabBtn.setEnabled(enabled);
        if (tabBtn != null) tabBtn.setEnabled(enabled);
        if (btnFontDec != null) btnFontDec.setEnabled(enabled);
        if (btnFontInc != null) btnFontInc.setEnabled(enabled);
        if (btnFontReset != null) btnFontReset.setEnabled(enabled);
    }

    public JTextPane getEditor() {
        return editor;
    }
}
