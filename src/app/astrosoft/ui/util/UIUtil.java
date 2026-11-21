/*
 * UIUtil.java
 *
 * Created on November 5, 2005, 7:34 PM
 *
 * @author E. Rajasekar.
 */

package app.astrosoft.ui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import app.astrosoft.consts.Language;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.Cell;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class UIUtil {

	// public static final String fontTag = " <font face='" +
	// getFont().getFamily() + "' color='#00468C'> ";
	public static final String closeFontTag = "</font> ";

	public static final String htmlTag = "<html> ";

	public static final String closeHtmlTag = "</html> ";

	public static final String brTag = "<br> ";

	public static final ClassLoader classLoader = UIUtil.class.getClassLoader();

	public static final String imagePath = "/resources/images/";

	/** Returns an ImageIcon, or null if the path was invalid. */
	public static ImageIcon createImageIcon(String imageName) {

		String image = imagePath + imageName.trim() + ".gif";

		URL imgURL = UIUtil.class.getResource(image);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		}
		return null;

	}

	/** Event source cell of JTable based on mouse position * */
	public static Cell getSourceTableCell(MouseEvent e) {

		JTable table = (JTable) e.getSource();
		AstrosoftTableModel model = (AstrosoftTableModel) table.getModel();

		int row = table.rowAtPoint(e.getPoint());
		int col = table.convertColumnIndexToModel(table.columnAtPoint(e
				.getPoint()));

		return new Cell(row, model.getColumn(col));
	}

	private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(UIUtil.class.getName());

	private static boolean customFontsRegistered = false;
	private static String resolvedTamilFontName = null;
	private static Language lastSyncedLanguage = null;

	public static synchronized void registerCustomFonts() {
		if (customFontsRegistered) {
			return;
		}
		customFontsRegistered = true;

		String[] fontFiles = {
			"NotoSansTamil-Regular.ttf",
			"NotoSansTamil-Bold.ttf",
			"tscarial.ttf"
		};

		java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();

		for (String fontFile : fontFiles) {
			try {
				java.io.InputStream in = UIUtil.class.getResourceAsStream("/resources/fonts/" + fontFile);
				if (in == null) {
					in = UIUtil.class.getResourceAsStream("/fonts/" + fontFile);
				}
				if (in == null && classLoader != null) {
					in = classLoader.getResourceAsStream("resources/fonts/" + fontFile);
				}
				if (in == null && classLoader != null) {
					in = classLoader.getResourceAsStream("fonts/" + fontFile);
				}
				if (in == null) {
					java.io.File f = new java.io.File("resources/fonts/" + fontFile);
					if (!f.exists()) {
						f = new java.io.File("fonts/" + fontFile);
					}
					if (f.exists()) {
						in = new java.io.FileInputStream(f);
					}
				}
				if (in != null) {
					try (java.io.InputStream fontStream = in) {
						Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
						ge.registerFont(customFont);
						log.fine("Successfully registered custom font: " + customFont.getFontName() + " (" + customFont.getFamily() + ")");
					}
				} else {
					log.fine("Font file not found: " + fontFile);
				}
			} catch (Exception e) {
				log.log(java.util.logging.Level.WARNING, "Failed to register custom font " + fontFile, e);
			}
		}
		syncHtmlDefaultStyleSheet();
	}

	public static synchronized String getTamilFontName() {
		if (resolvedTamilFontName != null) {
			return resolvedTamilFontName;
		}
		registerCustomFonts();

		java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontFamilies = ge.getAvailableFontFamilyNames();
		java.util.Set<String> families = new java.util.HashSet<String>(java.util.Arrays.asList(fontFamilies));

		String testWord = "சூரியன்"; // Sample Tamil word to verify glyph coverage

		String os = System.getProperty("os.name", "").toLowerCase();
		boolean isMac = os.contains("mac");
		boolean isWindows = os.contains("win");

		// Candidate list in priority order based on platform capability:
		// On macOS, native 'Tamil Sangam MN' provides dual-script coverage (Latin + Tamil) without tofu.
		// On Windows, 'Nirmala UI' (Win 8/10/11) provides dual-script, followed by bundled 'Noto Sans Tamil'.
		String[] candidates;
		if (isMac) {
			candidates = new String[] {
				"Tamil Sangam MN",
				"Tamil MN",
				"Noto Sans Tamil",
				"Arial Unicode MS"
			};
		} else if (isWindows) {
			candidates = new String[] {
				"Nirmala UI",
				"Noto Sans Tamil",
				"Latha",
				"Vijaya",
				"Arial Unicode MS"
			};
		} else {
			candidates = new String[] {
				"Noto Sans Tamil",
				"Lohit Tamil",
				"Nirmala UI",
				"Arial Unicode MS"
			};
		}

		for (String candidate : candidates) {
			if (families.contains(candidate)) {
				Font f = new Font(candidate, Font.PLAIN, 12);
				if (f.canDisplayUpTo(testWord) == -1) {
					resolvedTamilFontName = candidate;
					log.fine("Resolved Tamil font: " + resolvedTamilFontName + " on " + System.getProperty("os.name"));
					return resolvedTamilFontName;
				}
			}
		}

		// Fallback: search all registered font families for any that can display Tamil
		for (String family : fontFamilies) {
			Font f = new Font(family, Font.PLAIN, 12);
			if (f.canDisplayUpTo(testWord) == -1) {
				resolvedTamilFontName = family;
				log.fine("Resolved fallback Tamil font: " + resolvedTamilFontName + " on " + System.getProperty("os.name"));
				return resolvedTamilFontName;
			}
		}

		resolvedTamilFontName = "SansSerif";
		log.warning("No font with Tamil Unicode glyph coverage found; falling back to SansSerif");
		return resolvedTamilFontName;
	}

	/** Returns font depending on prefered language option. * */
	public static Font getFont(int style, int size) {
		Language language = AstroSoft.getPreferences().getLanguage();
		ensureLookAndFeelSynced(language);
		if (language == Language.TAMIL) {
			return new Font(getTamilFontName(), style, size);
		}
		return new Font(language.font(), style, size);
	}

	public static Font getFont() {
		return getFont(Font.BOLD, 12);
	}

	public static Font getFont(String name, int style, int size) {
		Language language = AstroSoft.getPreferences().getLanguage();
		ensureLookAndFeelSynced(language);
		if (language == Language.TAMIL) {
			return new Font(getTamilFontName(), style, size);
		}
		return new Font(name, style, size);
	}

	public static Font getFont(String name, int style, int en_size, int ta_size) {
		Language language = AstroSoft.getPreferences().getLanguage();
		ensureLookAndFeelSynced(language);
		if (language == Language.TAMIL) {
			return new Font(getTamilFontName(), style, ta_size);
		}
		return new Font(name, style, en_size);
	}

	public static Font getFont(int en_style, int en_size, int ta_style,
			int ta_size) {
		Language language = AstroSoft.getPreferences().getLanguage();
		ensureLookAndFeelSynced(language);
		if (language == Language.TAMIL) {
			return new Font(getTamilFontName(), ta_style, ta_size);
		}
		return new Font(language.font(), en_style, en_size);
	}

	public static Font getFont(Language lang, int style, int size) {
		if (lang == Language.TAMIL) {
			return new Font(getTamilFontName(), style, size);
		}
		return new Font(lang.font(), style, size);
	}

	/**
	 * Returns a CSS-quoted font-family list suitable for Swing BasicHTML / HTMLEditorKit
	 * so Tamil fonts with spaces (e.g. 'Nirmala UI', 'Noto Sans Tamil') are not dropped.
	 */
	public static String getHtmlFontFamily() {
		Language language = (AstroSoft.getPreferences() != null)
				? AstroSoft.getPreferences().getLanguage()
				: Language.ENGLISH;
		String family = (language == Language.TAMIL) ? getTamilFontName() : language.font();
		if (family == null || family.trim().isEmpty()) {
			return "sans-serif";
		}
		String safeFamily = family.replace("'", "").trim();
		return "'" + safeFamily + "', sans-serif";
	}

	/**
	 * Ensures Swing HTML components honor their assigned Font instead of falling back
	 * to Look & Feel default logical fonts that may lack Tamil glyphs on Windows.
	 */
	public static void enableHtmlDisplayProperties(JComponent comp) {
		if (comp != null) {
			comp.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		}
	}

	private static void ensureLookAndFeelSynced(Language language) {
		if (language == null) {
			return;
		}
		String expectedFamily = (language == Language.TAMIL) ? getTamilFontName() : language.font();
		Font currentTipFont = javax.swing.UIManager.getFont("ToolTip.font");
		boolean tipMatches = currentTipFont != null
				&& (expectedFamily.equalsIgnoreCase(currentTipFont.getFamily())
						|| expectedFamily.equalsIgnoreCase(currentTipFont.getName()));
		if (language != lastSyncedLanguage || !tipMatches) {
			syncLookAndFeelFonts(language);
		}
	}

	/**
	 * Synchronizes Swing's UIManager component font keys (including TonicLookAndFeel's
	 * TabbedPane.selectedFont and ToolTip.font) with the active language's font.
	 */
	public static void syncLookAndFeelFonts() {
		Language language = (AstroSoft.getPreferences() != null)
				? AstroSoft.getPreferences().getLanguage()
				: Language.ENGLISH;
		syncLookAndFeelFonts(language);
	}

	public static void syncLookAndFeelFonts(Language language) {
		if (language == null) {
			language = Language.ENGLISH;
		}
		lastSyncedLanguage = language;
		String family = (language == Language.TAMIL) ? getTamilFontName() : language.font();
		int plainSize = (language == Language.TAMIL) ? 11 : 11;
		int tabSize = (language == Language.TAMIL) ? 9 : 11;
		javax.swing.plaf.FontUIResource plainFont = new javax.swing.plaf.FontUIResource(family, Font.PLAIN, plainSize);
		javax.swing.plaf.FontUIResource boldFont = new javax.swing.plaf.FontUIResource(family, Font.BOLD, plainSize);
		javax.swing.plaf.FontUIResource tabFont = new javax.swing.plaf.FontUIResource(family, Font.BOLD, tabSize);
		javax.swing.plaf.FontUIResource menuFont = new javax.swing.plaf.FontUIResource(Language.ENGLISH.font(), Font.BOLD, 12);

		String[] plainKeys = {
			"ToolTip.font",
			"ComboBox.font",
			"List.font",
			"Label.font",
			"Table.font",
			"Tree.font",
			"TextField.font",
			"TextArea.font",
			"FormattedTextField.font",
			"CheckBox.font",
			"RadioButton.font",
			"OptionPane.font",
			"OptionPane.messageFont"
		};
		for (String key : plainKeys) {
			javax.swing.UIManager.put(key, plainFont);
		}

		String[] boldKeys = {
			"Button.font",
			"TableHeader.font",
			"TitledBorder.font",
			"OptionPane.buttonFont"
		};
		for (String key : boldKeys) {
			javax.swing.UIManager.put(key, boldFont);
		}

		String[] menuKeys = {
			"Menu.font",
			"MenuBar.font",
			"MenuItem.font",
			"RadioButtonMenuItem.font",
			"CheckBoxMenuItem.font",
			"PopupMenu.font"
		};
		for (String key : menuKeys) {
			javax.swing.UIManager.put(key, menuFont);
		}

		javax.swing.UIManager.put("TabbedPane.font", tabFont);
		javax.swing.UIManager.put("TabbedPane.selectedFont", tabFont);
		javax.swing.UIManager.put("TabbedPane.tabInsets", new java.awt.Insets(1, 6, 1, 6));
	}

	/**
	 * Synchronizes Swing's shared HTMLEditorKit default StyleSheet and UIManager
	 * default fonts with the active language's resolved font-family.
	 */
	public static void syncHtmlDefaultStyleSheet() {
		syncLookAndFeelFonts();
		try {
			StyleSheet ss = new HTMLEditorKit().getStyleSheet();
			if (ss != null) {
				ss.addRule("body, p, div, span, table, td, th, li, font { font-family: " + getHtmlFontFamily() + "; }");
			}
		} catch (Exception e) {
			log.fine("Could not update HTMLEditorKit default StyleSheet: " + e.getMessage());
		}
	}

	/**
	 * Creates a rich-HTML JLabel with HONOR_DISPLAY_PROPERTIES enabled and the given Font.
	 */
	public static JLabel createHtmlLabel(String html, Font font) {
		JLabel label = new JLabel(html);
		enableHtmlDisplayProperties(label);
		if (font != null) {
			label.setFont(font);
		}
		return label;
	}

	/**
	 * Creates a read-only rich-HTML JEditorPane with HONOR_DISPLAY_PROPERTIES enabled and the given Font.
	 */
	public static JEditorPane createHtmlPane(String html, Font font) {
		JEditorPane pane = new JEditorPane("text/html", html);
		pane.setEditable(false);
		pane.setOpaque(false);
		enableHtmlDisplayProperties(pane);
		if (font != null) {
			pane.setFont(font);
		}
		return pane;
	}

	/**
	 * Creates a non-editable, word-wrapping JTextArea configured with the given Font and
	 * foreground color for plain multi-line display without Swing HTML rendering overhead.
	 */
	public static JTextArea createWrappingTextArea(Font font, Color fg) {
		return createWrappingTextArea(font, fg, 0);
	}

	/**
	 * Creates a read-only, transparent, word-wrapping JTextArea.
	 * When fallbackWrapWidth > 0, constrains initial width during layout passes
	 * before the component has a real width so wrapped lines report true height.
	 */
	public static JTextArea createWrappingTextArea(Font font, Color fg, int fallbackWrapWidth) {
		JTextArea area = (fallbackWrapWidth > 0) ? new JTextArea() {
			@Override
			public Dimension getPreferredSize() {
				if (getWidth() <= 0) {
					setSize(fallbackWrapWidth, Short.MAX_VALUE);
				}
				return super.getPreferredSize();
			}
		} : new JTextArea();
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setEditable(false);
		area.setFocusable(false);
		area.setOpaque(false);
		area.setBorder(null);
		if (font != null) {
			area.setFont(font);
		}
		if (fg != null) {
			area.setForeground(fg);
		}
		return area;
	}

	public static void setPanelBackground(JComponent panel, Color color) {
		if (panel!=null){
			panel.setBackground(color);
			for (Component c : panel.getComponents()) {
				if (c instanceof JPanel) {
					setPanelBackground((JPanel) c, color);
				}
			}
		}	
	}

	public static String getFontTag() {
		return " <font face='" + getFont().getFamily() + "' color='#00468C'> ";
	}

	public static void applyOptionPaneBackground(JOptionPane optionPane,
			Color color) {
		optionPane.setBackground(color);
		for (Iterator i = getComponents(optionPane).iterator(); i.hasNext();) {
			Component comp = (Component) i.next();
			//if (comp instanceof JPanel) {
				comp.setBackground(color);
			//}
		}
	}

	public final static Collection getComponents(Container container) {
		Collection components = new Vector();
		Component[] comp = container.getComponents();
		for (int i = 0, n = comp.length; i < n; i++) {
			components.add(comp[i]);
			if (comp[i] instanceof Container) {
				components.addAll(getComponents((Container) comp[i]));
			}
		}
		return components;
	}
	
	public static void setWindowLocation(Window window, Component parentComponent) {
		
		Point comboLocation =
			parentComponent.getLocationOnScreen(  );
        Dimension size = parentComponent.getSize(  );
        Dimension windowSize = window.getSize(  );
        Dimension screenSize =
            Toolkit.getDefaultToolkit(  ).getScreenSize(  );

        if ( 
            ( ( comboLocation.x
                - ( windowSize.width - size.width ) ) <= 0 )
                && ( ( comboLocation.y + size.height
                + windowSize.height ) >= screenSize.height ) ) {
            window.setLocation( 
                0, comboLocation.y - windowSize.height );
        } else if ( 
            ( comboLocation.x
                - ( windowSize.width - size.width ) ) <= 0 ) {
            window.setLocation( 
                0, comboLocation.y + size.height );
        } else if ( 
            ( comboLocation.y + size.height
                + windowSize.height ) >= screenSize.height ) {
            window.setLocation( 
                comboLocation.x
                - ( windowSize.width - size.width ),
                comboLocation.y - windowSize.height );
        } else {
            window.setLocation( 
                comboLocation.x
                - ( windowSize.width - size.width ),
                comboLocation.y + size.height );
        }
	}

	/** Blends {@code c1} into {@code c2} by {@code ratio} in [0, 1]. */
	public static Color blendColors(Color c1, Color c2, float ratio) {
		float inv = 1.0f - ratio;
		int r = Math.round(c1.getRed() * ratio + c2.getRed() * inv);
		int g = Math.round(c1.getGreen() * ratio + c2.getGreen() * inv);
		int b = Math.round(c1.getBlue() * ratio + c2.getBlue() * inv);
		return new Color(Math.min(255, Math.max(0, r)),
				Math.min(255, Math.max(0, g)),
				Math.min(255, Math.max(0, b)));
	}
}
