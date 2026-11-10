/**
 * UIConsts.java
 * Created On 2006, Mar 15, 2006 3:14:06 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import app.astrosoft.ui.AstroSoft;

public class UIConsts {

	public static final Color LIGHT_YELLOW = new Color(255, 255, 235);
	public static final Color YELLOWISH_WHITE = new Color(255, 255, 252);
	
	public static final Color GRAY_WHITE = new Color(238,238,238);
	public static final Color LIGHT_BLUE = new Color(0, 0 , 204);
	public static final Color DARK_BLUE = new Color(0, 0 , 140);
	public static final Color GREENISH_BLUE = new Color( 0, 70, 140 );
	public static final Color SKY_BLUE = new Color( 210, 228, 238 );
	
	public static final Color DARK_GREEN = new Color(0, 90, 0);
	public static final Color LEAF_GREEN = new Color(75, 148,75);
	public static final Color PEARL_GREEN = new Color(236, 233,216);
	public static Color DARK_PEARL_GREEN = new Color(218,216,200);
	
	public static final Color DARK_RED = new Color(128, 0, 0);
	public static final Color MEROON_RED = new Color( 128, 0, 80 );
	public static final Color ROSE = new Color(200, 0, 130);
	
	public static final Color TABLE_HEADER_BACKGROUND = GRAY_WHITE;
	public static final Color TABLE_HEADER_FOREGROUND = GREENISH_BLUE;
	
	public static final Color CAL_COMBO_SEL_CLR = Color.RED;
	
	public static final Color CAL_COMBO_BACKGROUND = LIGHT_YELLOW;
	
	public static final Color THEME_CLR = PEARL_GREEN;
	//public static final Color THEME_CLR = PEARL_GREEN;
	
	public static final Color OPTIONPANE_BACKGROUND = new Color(222,222,222);
	
	
	public static final Dimension BUTTON_ICON_SIZE = new Dimension(20,20);
	
	public static String getLookAndFeel(){
		return "com.digitprop.tonic.TonicLookAndFeel";
	}
	
	public static Map getUIDefaults() {
		Properties UIProps = new Properties();
		
		
		//UIProps.put("Label.font", new Font("Arial", Font.PLAIN,12 ));
		
		UIProps.put("Button.focus", Color.BLACK);
		UIProps.put("Button.background", THEME_CLR);
		UIProps.put("Button.rollover", true);
		//UIProps.put("Button.border", new LineBorder(THEME_CLR));

		UIProps.put("Panel.background", Color.WHITE);
		//UIProps.put("Panel.background", YELLOWISH_WHITE);
		UIProps.put("CheckBox.background", Color.WHITE);
		UIProps.put("Viewport.background", Color.WHITE);
		UIProps.put("ComboBox.background", Color.WHITE);
		
		//Tabbed Pane Settings
		
		UIProps.put("TabbedPane.background", Color.WHITE);
		UIProps.put("TabbedPane.selected", THEME_CLR);
		UIProps.put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));
		UIProps.put("TabbedPane.tabRunOverlay", 5);
		
		
		//Menu Bar Settings
		UIProps.put("MenuBar.background", THEME_CLR);
		UIProps.put("MenuItem.background", THEME_CLR);
		UIProps.put("Menu.background", THEME_CLR);
		UIProps.put("Menu.selectionBackground", LEAF_GREEN);
		UIProps.put("MenuItem.selectionBackground", LEAF_GREEN);
		UIProps.put("ToolBar.background", THEME_CLR);
		UIProps.put("RadioButtonMenuItem.background", THEME_CLR);
		UIProps.put("RadioButtonMenuItem.selectionBackground", LEAF_GREEN);
		UIProps.put("PopupMenu.border", new LineBorder(LEAF_GREEN));
		
		//UIProps.put("ScrollBar.background", MENU_CLR);
		
		
		//Table Settings
		//UIProps.put("Table.background", Color.WHITE);
		UIProps.put("Table.foreground", TEXT_DARK_NAVY);
		UIProps.put("Table.selectionBackground", GRAY_WHITE);
		UIProps.put("Table.selectionForeground", GREENISH_BLUE);
		//UIProps.put("Table.gridColor", new Color( 0, 128, 192));
		UIProps.put("TableHeader.background", TABLE_HEADER_BACKGROUND);
		UIProps.put("TableHeader.foreground", TABLE_HEADER_FOREGROUND);
		
		//Tree Settings
		
		UIProps.put("Tree.selectionBackground", LEAF_GREEN);
		UIProps.put("Tree.selectionBorderColor", LEAF_GREEN);
		
		UIProps.put("SplitPane.background", Color.WHITE);
		UIProps.put("SplitPane.continuousLayout", Boolean.TRUE);
		UIProps.put("SplitPaneDivider.draggingColor", new Color(176, 196, 222));
		
		UIProps.put("List.selectionBackground", LEAF_GREEN);
		UIProps.put("List.selectionBorderColor", LEAF_GREEN);
		
		//JOptionPane Properties
		//UIProps.put("OptionPane.background", Color.WHITE);
		//UIProps.put("OptionPane.foreground", Color.BLACK);
		
		
		//UIProps.put("window", GREENISH_BLUE);
		return UIProps;
	}

	public static Border getTitleBorder(String title){
		TitledBorder border = new TitledBorder(new EtchedBorder(), title);
		border.setTitleColor(UIConsts.DARK_GREEN);
		return border;
	}
	
	public static final Color CANVAS_BACKGROUND = new Color(244, 245, 247); // #F4F5F7 Soft light gray
	public static final Color CARD_BACKGROUND = Color.WHITE;
	public static final Color LABEL_MUTED_COLOR = new Color(100, 116, 139); // #64748B Slate gray
	public static final Color TEXT_DARK_NAVY = new Color(30, 41, 59); // #1E293B Dark navy/charcoal
	public static final Color NAVY_BADGE_BG = new Color(15, 30, 60); // #0F1E3C Modern dark navy badge
	public static final Color ASCENDANT_COLOR = new Color(220, 38, 38); // #DC2626 Modern clean red
	public static final Color MOON_COLOR = new Color(2, 132, 199); // #0284C7 Sky / Cerulean Blue
	public static final Color ASHTAVARGA_WEAK_BG = new Color(255, 240, 240);    // #FFF0F0
	public static final Color ASHTAVARGA_STRONG_BG = new Color(234, 242, 235);  // #EAF2EB
	public static final Color ASHTAVARGA_NEUTRAL_BG = Color.WHITE;              // #FFFFFF
	public static final Color ASHTAVARGA_DIVIDER_CLR = Color.decode("#d0a65c"); // #d0a65c Desaturated gold-ochre

	public enum ChartTheme {
		WARM_IVORY("Warm Ivory", 
			new Color(250, 248, 242), // #FAF8F2
			new Color(208, 166, 92),  // #D0A65C
			new Color(30, 41, 59),    // #1E293B
			new Color(220, 38, 38),   // #DC2626
			new Color(2, 132, 199),   // #0284C7
			new Color(30, 41, 59),    // Score
			new Color(100, 116, 139), // Label (#64748B)
			new Color(194, 65, 12)    // Natal Highlight (#C2410C Warm Rust)
		),
		SOFT_ALABASTER("Soft Alabaster", 
			new Color(250, 250, 249), // #FAFAF9
			new Color(203, 213, 225), // #CBD5E1
			new Color(15, 23, 42),    // #0F172A
			new Color(225, 29, 72),   // #E11D48
			new Color(2, 132, 199),   // #0284C7
			new Color(15, 23, 42),
			new Color(100, 116, 139),
			new Color(190, 18, 60)    // Natal Highlight (#BE123C Ruby Crimson)
		),
		PALE_GOLD_MIST("Pale Gold Mist", 
			new Color(252, 249, 238), // #FCF9EE
			new Color(200, 155, 60),  // #C89B3C
			new Color(49, 46, 129),   // #312E81
			new Color(220, 38, 38),   // #DC2626
			new Color(2, 132, 199),   // #0284C7
			new Color(49, 46, 129),
			new Color(110, 105, 135),
			new Color(217, 119, 6)    // Natal Highlight (#D97706 Vibrant Amber)
		),
		CALM_SAGE("Calm Sage", 
			new Color(245, 246, 243), // #F5F6F3
			new Color(132, 150, 136), // #849688
			new Color(30, 41, 59),    // #1E293B
			new Color(220, 38, 38),   // #DC2626
			new Color(2, 132, 199),   // #0284C7
			new Color(30, 41, 59),
			new Color(100, 116, 110),
			new Color(154, 52, 18)    // Natal Highlight (#9A3412 Earthy Rust)
		),
		CLASSIC("Classic (Original)", 
			new Color(255, 255, 252), // #FFFFFC YELLOWISH_WHITE
			Color.ORANGE,             // Etched border highlight
			new Color(0, 0, 204),     // #0000CC LIGHT_BLUE
			Color.RED,                // Red Asc
			Color.MAGENTA,            // Magenta Moon
			new Color(0, 0, 140),     // #00008C DARK_BLUE score
			new Color(0, 70, 140),    // GREENISH_BLUE label
			new Color(225, 29, 72)    // Natal Highlight (#E11D48 Bright Coral Red)
		);

		private final String displayName;
		private final Color background;
		private final Color borderColor;
		private final Color planetForeground;
		private final Color ascendantColor;
		private final Color moonColor;
		private final Color scoreColor;
		private final Color labelColor;
		private final Color natalHighlightColor;

		ChartTheme(String displayName, Color background, Color borderColor, 
				   Color planetForeground, Color ascendantColor, Color moonColor, Color scoreColor, Color labelColor,
				   Color natalHighlightColor) {
			this.displayName = displayName;
			this.background = background;
			this.borderColor = borderColor;
			this.planetForeground = planetForeground;
			this.ascendantColor = ascendantColor;
			this.moonColor = moonColor;
			this.scoreColor = scoreColor;
			this.labelColor = labelColor;
			this.natalHighlightColor = natalHighlightColor;
		}

		public String displayName() { return displayName; }
		public Color getBackground() { return background; }
		public Color getBorderColor() { return borderColor; }
		public Color getPlanetForeground() { return planetForeground; }
		public Color getAscendantColor() { return ascendantColor; }
		public Color getMoonColor() { return moonColor; }
		public Color getScoreColor() { return scoreColor; }
		public Color getLabelColor() { return labelColor; }
		public Color getNatalHighlightColor() { return natalHighlightColor; }

		public Color getPrimaryAccentColor() {
			switch (this) {
				case WARM_IVORY: return new Color(180, 83, 9);       // #B45309 Rich Warm Amber
				case SOFT_ALABASTER: return new Color(37, 99, 235);    // #2563EB Clean Royal Blue
				case PALE_GOLD_MIST: return new Color(79, 70, 229);    // #4F46E5 Deep Indigo
				case CALM_SAGE: return new Color(46, 90, 68);          // #2E5A44 Deep Forest Sage
				case CLASSIC:
				default: return new Color(25, 118, 210);               // #1976D2 Material Blue
			}
		}

		public Border getBorder() {
			if (this == CLASSIC) {
				return BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.ORANGE, Color.WHITE);
			}
			return BorderFactory.createLineBorder(borderColor, 1);
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	private static ChartTheme currentChartTheme = ChartTheme.WARM_IVORY;

	public enum ChartStyle {
		SOUTH_INDIAN("South Indian"),
		NORTH_INDIAN("North Indian");

		private final String displayName;

		ChartStyle(String displayName) {
			this.displayName = displayName;
		}

		public String displayName() {
			return displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	private static ChartStyle currentChartStyle = ChartStyle.SOUTH_INDIAN;

	public static ChartStyle getChartStyle() {
		return currentChartStyle;
	}

	public static void setChartStyle(ChartStyle style) {
		if (style != null) {
			currentChartStyle = style;
		}
	}

	public static ChartTheme getChartTheme() {
		return currentChartTheme;
	}

	public static void setChartTheme(ChartTheme theme) {
		if (theme != null) {
			currentChartTheme = theme;
		}
	}

	public static Color getPrimaryAccentColor() {
		return currentChartTheme.getPrimaryAccentColor();
	}

	public static Border getTitleBorder(Enum title){
		return getTitleBorder(title.toString());
	}
	
	public static Color getChartBackground(){
		return currentChartTheme.getBackground();
	}
	
	public static Border getChartBorder(){
		return currentChartTheme.getBorder();
	}

	public static Color getChartBorderColor(){
		return currentChartTheme.getBorderColor();
	}

	public static Color getChartPlanetForeground(){
		return currentChartTheme.getPlanetForeground();
	}

	public static Color getChartAscendantColor(){
		return currentChartTheme.getAscendantColor();
	}

	public static Color getChartMoonColor(){
		return currentChartTheme.getMoonColor();
	}

	public static Color getChartScoreColor(){
		return currentChartTheme.getScoreColor();
	}

	public static Color getChartLabelColor(){
		return currentChartTheme.getLabelColor();
	}

	public static Color getChartNatalHighlightColor(){
		return currentChartTheme.getNatalHighlightColor();
	}

	public static Color getAshtavargaColor(app.astrosoft.consts.AshtavargaStrength strength) {
		if (strength == null) {
			return getChartBackground();
		}
		switch (strength) {
			case WEAK:
				return ASHTAVARGA_WEAK_BG;
			case STRONG:
				return ASHTAVARGA_STRONG_BG;
			case NEUTRAL:
			default:
				return getChartBackground();
		}
	}

	/**
	 * Per-planet foreground colors for North Indian charts, adjusted per theme to
	 * guarantee a WCAG AA contrast ratio of at least 4.5:1 against the chart background.
	 */
	public static Color getNorthIndianPlanetColor(app.astrosoft.consts.Planet planet) {
		Color base = baseNorthIndianPlanetColor(planet);
		return ensureContrast(base, getChartBackground(), 4.5);
	}

	/** Soft tint applied to the 4 angular diamond houses (1, 4, 7, 10) in North Indian charts. */
	public static Color getNorthIndianDiamondTint() {
		Color border = getChartBorderColor();
		return new Color(border.getRed(), border.getGreen(), border.getBlue(), 16);
	}

	private static Color baseNorthIndianPlanetColor(app.astrosoft.consts.Planet planet) {
		if (planet == null) {
			return getChartPlanetForeground();
		}
		switch (planet) {
			case Ascendant: return new Color(0xB9, 0x1C, 0x1C);
			case Sun:       return new Color(0xEA, 0x58, 0x0C);
			case Moon:      return new Color(0xC0, 0x26, 0xD3);
			case Mars:      return new Color(0xDC, 0x26, 0x26);
			case Mercury:   return new Color(0x15, 0x80, 0x3D);
			case Jupiter:   return new Color(0xB4, 0x53, 0x09);
			case Venus:     return new Color(0xDB, 0x27, 0x77);
			case Saturn:    return new Color(0x1D, 0x4E, 0xD8);
			case Rahu:      return new Color(0x43, 0x38, 0xCA);
			case Ketu:      return new Color(0x6B, 0x72, 0x80);
			default:        return getChartPlanetForeground();
		}
	}

	/** Adjusts {@code fg} toward darker (on light bg) or lighter (on dark bg) until contrast >= {@code minRatio}. */
	public static Color ensureContrast(Color fg, Color bg, double minRatio) {
		if (fg == null || bg == null) {
			return fg;
		}
		if (contrastRatio(fg, bg) >= minRatio) {
			return fg;
		}
		double bgLum = relativeLuminance(bg);
		boolean darken = bgLum > 0.179;
		float[] hsb = Color.RGBtoHSB(fg.getRed(), fg.getGreen(), fg.getBlue(), null);
		for (int i = 1; i <= 40; i++) {
			float t = i / 40.0f;
			float b = darken ? Math.max(0.0f, hsb[2] * (1.0f - t)) : Math.min(1.0f, hsb[2] + (1.0f - hsb[2]) * t);
			Color candidate = Color.getHSBColor(hsb[0], hsb[1], b);
			if (contrastRatio(candidate, bg) >= minRatio) {
				return candidate;
			}
		}
		return darken ? Color.BLACK : Color.WHITE;
	}

	public static double contrastRatio(Color a, Color b) {
		double la = relativeLuminance(a);
		double lb = relativeLuminance(b);
		double lighter = Math.max(la, lb);
		double darker = Math.min(la, lb);
		return (lighter + 0.05) / (darker + 0.05);
	}

	private static double relativeLuminance(Color c) {
		return 0.2126 * linearizeChannel(c.getRed() / 255.0)
			 + 0.7152 * linearizeChannel(c.getGreen() / 255.0)
			 + 0.0722 * linearizeChannel(c.getBlue() / 255.0);
	}

	private static double linearizeChannel(double c) {
		return (c <= 0.03928) ? (c / 12.92) : Math.pow((c + 0.055) / 1.055, 2.4);
	}
}


