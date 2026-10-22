/**
 * MarriageMatchView.java
 *
 * Unified single-screen Marriage Match dashboard providing comprehensive
 * Kuta compatibility, Papa Samya dosha evaluation, dual divisional charts
 * (Rasi / Navamsha), and dual planetary strength (Shadbala) bar charts.
 *
 * Supports two responsive view modes:
 * 1. Dashboard (2-Column) [Default]
 * 2. Widescreen (3-Column)
 *
 * @author Astrosoft Team
 */

package app.astrosoft.ui.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.EnumMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Kuta;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.TableStyle;
import app.astrosoft.consts.Varga;
import app.astrosoft.core.Compactibility;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.comp.TitledTable;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.SortableTable;
import app.astrosoft.ui.table.SortableTableModel;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.util.AstroUtil;

public class MarriageMatchView extends AstrosoftView {

	public enum ViewMode {
		DASHBOARD("Dashboard (2-Column)"),
		WIDESCREEN("Widescreen (3-Column)");

		private final String label;
		ViewMode(String label) { this.label = label; }
		public String getLabel() { return label; }
	}

	private static final Dimension VIEW_SIZE = new Dimension(1450, 850);

	private static final Font TITLE_FONT = new Font("Verdana", Font.BOLD, 13);
	private static final Font LABEL_FONT = new Font("Verdana", Font.BOLD, 11);
	private static final Font VALUE_FONT = new Font("Verdana", Font.PLAIN, 11);
	private static final Font BADGE_FONT = new Font("Verdana", Font.BOLD, 12);
	private static final Font SECTION_FONT = new Font("Verdana", Font.BOLD, 12);

	private static final Color CARD_BORDER_COLOR = new Color(210, 180, 120);
	private static final Color CARD_BG = new Color(253, 251, 247);
	private static final Color PASS_COLOR = new Color(0, 120, 0);
	private static final Color FAIL_COLOR = new Color(180, 40, 40);

	private static final DecimalFormat DOSHA_FMT = new DecimalFormat("0.00");
	private static final DecimalFormat PCT_FMT = new DecimalFormat("0.0'%'");

	private final Compactibility c;
	private ViewMode currentMode = ViewMode.DASHBOARD;
	private Varga currentVarga = Varga.Rasi;

	private JCheckBox shadbalaCheck;
	private JPanel shadbalaWrapper;

	public MarriageMatchView(Compactibility c, Point loc) {
		super(VIEW_SIZE, loc);
		this.c = c;
		initUI();
	}

	private void initUI() {
		setLayout(new BorderLayout());

		JPanel dashboardPanel = createDashboardView();

		JScrollPane dashboardScroll = new JScrollPane(dashboardPanel);
		dashboardScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		dashboardScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		dashboardScroll.getHorizontalScrollBar().setUnitIncrement(24);
		dashboardScroll.getVerticalScrollBar().setUnitIncrement(24);
		dashboardScroll.setWheelScrollingEnabled(true);
		dashboardScroll.setBorder(null);

		add(dashboardScroll, BorderLayout.CENTER);
	}

	/**
	 * Dashboard (2-Column) Layout
	 * Left Column: Candidate Profiles, Kuta Score & Table, Dosha Comparison Table
	 * Right Column: 4 Horoscope Charts (Boy & Girl Rasi + Navamsha) & Dual Shadbala bar graphs
	 */
	private JPanel createDashboardView() {
		JPanel container = new JPanel(new GridBagLayout());
		container.setBackground(Color.WHITE);
		container.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 1.0;
		gbc.weightx = 0.0; // Fixed column widths - NEVER dynamically squashed or scaled!

		// --- Left Column (500px FIXED) ---
		gbc.gridx = 0;
		gbc.insets = new Insets(0, 0, 0, 23); // 23px horizontal separation to Right Column (+5px)
		JPanel leftCol = new JPanel();
		leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
		leftCol.setOpaque(false);
		leftCol.setPreferredSize(new Dimension(500, 810));
		leftCol.setMinimumSize(new Dimension(500, 810));
		leftCol.setMaximumSize(new Dimension(500, 810));

		leftCol.add(createCombinedProfileCard());
		leftCol.add(Box.createVerticalStrut(10));
		leftCol.add(createKutaScoreCard());
		leftCol.add(Box.createVerticalStrut(10));
		if (c.hasHoroscope()) {
			leftCol.add(createDoshaCard());
		}
		leftCol.add(Box.createVerticalGlue());
		container.add(leftCol, gbc);

		// --- Right Column (660px FIXED with 4 Charts + Shadbala) ---
		gbc.gridx = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		JPanel rightCol = new JPanel();
		rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
		rightCol.setOpaque(false);
		rightCol.setPreferredSize(new Dimension(660, 810));
		rightCol.setMinimumSize(new Dimension(660, 810));
		rightCol.setMaximumSize(new Dimension(660, 810));

		if (c.hasHoroscope()) {
			JPanel chartsCard = createChartsCard2Col();
			chartsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
			rightCol.add(chartsCard);
			rightCol.add(Box.createVerticalStrut(8)); // 8 pixels below lower edge of bounding box

			shadbalaCheck = new JCheckBox("Shadbala");
			shadbalaCheck.setSelected(false);
			shadbalaCheck.setOpaque(false);
			shadbalaCheck.setFocusable(false);
			shadbalaCheck.setFont(new Font("Verdana", Font.PLAIN, 11));
			shadbalaCheck.setForeground(UIConsts.DARK_BLUE);
			shadbalaCheck.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (shadbalaWrapper != null) {
						shadbalaWrapper.setVisible(shadbalaCheck.isSelected());
						MarriageMatchView.this.revalidate();
						MarriageMatchView.this.repaint();
					}
				}
			});

			JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			checkPanel.setOpaque(false);
			checkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			checkPanel.setMaximumSize(new Dimension(660, 24));
			checkPanel.add(shadbalaCheck);
			rightCol.add(checkPanel);

			shadbalaWrapper = new JPanel();
			shadbalaWrapper.setLayout(new BoxLayout(shadbalaWrapper, BoxLayout.Y_AXIS));
			shadbalaWrapper.setOpaque(false);
			shadbalaWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
			shadbalaWrapper.add(Box.createVerticalStrut(8));
			JPanel shadbalaCard = createShadbalaCard2Col();
			shadbalaCard.setAlignmentX(Component.LEFT_ALIGNMENT);
			shadbalaWrapper.add(shadbalaCard);
			shadbalaWrapper.setVisible(false); // Optional: default off

			rightCol.add(shadbalaWrapper);
		} else {
			rightCol.add(createQuickEntryNoticeCard());
		}
		rightCol.add(Box.createVerticalGlue());
		container.add(rightCol, gbc);

		container.setPreferredSize(new Dimension(1211, 845));
		container.setMinimumSize(new Dimension(1211, 845));

		return container;
	}

	/**
	 * Combined Profile Card for 2-Column Dashboard with smart wrapping.
	 */
	private JPanel createCombinedProfileCard() {
		JPanel card = new JPanel(new GridLayout(1, 2, 8, 0));
		card.setOpaque(false);
		card.setBorder(createCardBorder("Candidate Profiles"));

		card.add(createProfileContent(true));
		card.add(createProfileContent(false));
		card.setPreferredSize(new Dimension(500, 250));
		card.setMinimumSize(new Dimension(500, 250));
		card.setMaximumSize(new Dimension(500, 250));
		return card;
	}

	/**
	 * Internal profile content builder utilizing GridBagLayout with smart wrapping to prevent clipping.
	 */
	private JPanel createProfileContent(boolean isBoy) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(CARD_BG);
		panel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

		String name = isBoy ? c.getBoyName() : c.getGirlName();
		BirthData bd = isBoy ? c.getBoyBirthData() : c.getGirlBirthData();

		int row = 0;
		addProfileRow(panel, row++, "Name:", name);

		if (c.hasHoroscope() && bd != null) {
			addProfileRow(panel, row++, "DOB:", bd.birthDayString());
			String tob = AstroUtil.timeFormat(bd.birthTime(), true);
			if (bd.isDstActive()) tob += " (DST)";
			addProfileRow(panel, row++, "TOB:", tob);
			addProfileRow(panel, row++, "Place:", bd.place());
			addProfileRow(panel, row++, "Coords:", bd.getBirthPlace().coordinates());
		}

		String nakStr = isBoy ? c.getBoyNak().toString() : c.getGirlNak().toString();
		String rasiStr = isBoy ? c.getBoyRasi().toString() : c.getGirlRasi().toString();
		addProfileRow(panel, row++, "Nakshatra:", nakStr);
		addProfileRow(panel, row++, "Janma Rasi:", rasiStr);

		if (c.hasHoroscope()) {
			String dasa = isBoy ? c.getBoyCurPeriod() : c.getGirlCurPeriod();
			addProfileRow(panel, row++, "Current Dasa:", dasa != null ? dasa : "-");

			if (isBoy && c.getBeejaRasi() != null) {
				addProfileRow(panel, row++, "Beeja Rasi:", c.getBeejaRasi().toString());
				addProfileRow(panel, row++, "Beeja Longitude:", formatDms(c.getBeejaPos()));
			} else if (!isBoy && c.getKshetraRasi() != null) {
				addProfileRow(panel, row++, "Kshetra Rasi:", c.getKshetraRasi().toString());
				addProfileRow(panel, row++, "Kshetra Longitude:", formatDms(c.getKshetraPos()));
			}
		}

		// Vertical glue pushes rows to top if fewer rows are present
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.gridwidth = 2;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.VERTICAL;
		panel.add(Box.createVerticalGlue(), gbc);

		return panel;
	}

	/**
	 * Formats a celestial position into clean degree, minute, second string (e.g., 19° 56' 56").
	 */
	public static String formatDms(double val) {
		int[] dms = AstroUtil.int_dms(val);
		return String.format(java.util.Locale.ROOT, "%02d\u00b0 %02d' %02d\"", Math.abs(dms[0]), Math.abs(dms[1]), Math.abs(dms[2]));
	}

	/**
	 * Helper to create a smart-wrapping label/value row that never clips.
	 */
	private void addProfileRow(JPanel panel, int rowIdx, String label, String value) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = rowIdx;
		gbc.insets = new Insets(1, 0, 1, 4);

		// Label (Column 0): anchor NORTHWEST, natural width (weightx = 0.0) so it never truncates
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		JLabel lbl = new JLabel(label);
		lbl.setFont(LABEL_FONT);
		lbl.setForeground(new Color(80, 70, 60));
		panel.add(lbl, gbc);

		// Value (Column 1): anchor NORTHWEST, fills horizontally, wraps with HTML div width
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		String valText = (value != null && !value.trim().isEmpty()) ? value : "-";
		JLabel val = new JLabel("<html><div width=\"115\">" + valText + "</div></html>");
		val.setFont(VALUE_FONT);
		val.setForeground(UIConsts.DARK_BLUE);
		panel.add(val, gbc);
	}

	public static String calcDoshaComparison(double bTotal, double gTotal) {
		if (gTotal == 0.0) {
			if (bTotal == 0.0) {
				return "Boy's Dosha is equal to Girl's Dosha (0.00)";
			} else {
				return "Boy's Dosha is more than Girl's Dosha (Girl has 0.00)";
			}
		}

		double diff = bTotal - gTotal;
		double diffPercent = Math.abs(diff) / gTotal * 100.0;
		if (diff > 0.0001) {
			return String.format(java.util.Locale.ROOT, "Boy's Dosha is %.1f%% more than Girl's Dosha", diffPercent);
		} else if (diff < -0.0001) {
			return String.format(java.util.Locale.ROOT, "Boy's Dosha is %.1f%% less than Girl's Dosha", diffPercent);
		} else {
			return "Boy's Dosha is equal to Girl's Dosha";
		}
	}

	/**
	 * Kuta Compatibility Scorecard with Score Banner and 12 Kutas Table.
	 */
	private JPanel createKutaScoreCard() {
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setOpaque(false);
		card.setBorder(createCardBorder("Kuta Match"));

		// 1. Score Summary Banner (Neutral, no judgments)
		int totalGained = c.getTotalKutaGained();
		int maxTotal = Kuta.totalValue();
		double pct = (totalGained * 100.0) / maxTotal;

		JPanel banner = new JPanel(new BorderLayout());
		banner.setBackground(new Color(248, 245, 238));
		banner.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(6, 8, 6, 8)));

		String bannerText = "Total Points: " + totalGained + " / " + maxTotal + " (" + PCT_FMT.format(pct) + ")";
		JLabel scoreLbl = new JLabel(bannerText, SwingConstants.CENTER);
		scoreLbl.setFont(new Font("Verdana", Font.BOLD, 13));
		scoreLbl.setForeground(UIConsts.DARK_BLUE);
		banner.add(scoreLbl, BorderLayout.CENTER);
		banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

		card.add(banner);
		card.add(Box.createVerticalStrut(6));

		// 2. Kuta Table (12 Kutas + Total)
		SortableTableModel kutaModel = new SortableTableModel(c.getKutaTableData(), Compactibility.getKutaTableColumnMetaData());
		SortableTable kutaTable = new SortableTable(kutaModel, TableStyle.SCROLL);
		int gainedIdx = kutaModel.getColumnIndex(AstrosoftTableColumn.KutaGained);
		if (gainedIdx >= 0) {
			kutaTable.getColumnModel().getColumn(gainedIdx).setHeaderValue("Kuta");
		}
		kutaTable.setColumnWidth(140, AstrosoftTableColumn.Kuta);
		kutaTable.setRowColor(13, Color.RED);

		int kutaIdx = kutaModel.getColumnIndex(AstrosoftTableColumn.Kuta);
		if (kutaIdx >= 0) {
			TableColumn tc = kutaTable.getColumnModel().getColumn(kutaIdx);
			tc.setCellRenderer(new LeftPaddedCellRenderer(tc.getCellRenderer(), 6));
		}

		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(Color.WHITE);
		tablePanel.add(kutaTable.getTableHeader(), BorderLayout.NORTH);
		tablePanel.add(kutaTable, BorderLayout.CENTER);
		tablePanel.setBorder(BorderFactory.createLineBorder(AstrosoftTable.tableBorderClr, 1));

		int kutaH = kutaTable.getTableHeader().getPreferredSize().height + kutaTable.getPreferredSize().height + 2;
		tablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, kutaH));
		card.add(tablePanel);

		return card;
	}

	/**
	 * Papa Samya (Dosha Comparison) Card with +/- 30% balance criteria.
	 */
	private JPanel createDoshaCard() {
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setOpaque(false);
		card.setBorder(createCardBorder("Dosha"));

		final AstrosoftTable doshaTable = new AstrosoftTable(
				new AstrosoftTableModel(c.getDoshaTableData(), Compactibility.getDoshaTableColumnMetaData()),
				TableStyle.STANDARD);
		doshaTable.setRowColor(5, Color.RED);
		doshaTable.setColumnNumberFormat(AstrosoftTableColumn.Boy, new java.text.DecimalFormat("00.00"));
		doshaTable.setColumnNumberFormat(AstrosoftTableColumn.Girl, new java.text.DecimalFormat("00.00"));

		int doshaIdx = ((AstrosoftTableModel) doshaTable.getModel()).getColumnIndex(AstrosoftTableColumn.Dosha);
		if (doshaIdx >= 0) {
			TableColumn tc = doshaTable.getColumnModel().getColumn(doshaIdx);
			tc.setCellRenderer(new LeftPaddedCellRenderer(tc.getCellRenderer(), 6));
		}

		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(Color.WHITE);
		tablePanel.add(doshaTable.getTableHeader(), BorderLayout.NORTH);
		tablePanel.add(doshaTable, BorderLayout.CENTER);
		tablePanel.setBorder(BorderFactory.createLineBorder(AstrosoftTable.tableBorderClr, 1));

		int doshaH = doshaTable.getTableHeader().getPreferredSize().height + doshaTable.getPreferredSize().height + 2;
		tablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, doshaH));
		card.add(tablePanel);

		// Comparison Note Footer (Relative difference)
		double bTotal = c.getBoyDoshaTotal();
		double gTotal = c.getGirlDoshaTotal();
		String comparison = calcDoshaComparison(bTotal, gTotal);

		JPanel footer = new JPanel(new BorderLayout());
		footer.setOpaque(false);
		footer.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));

		JLabel noteLbl = new JLabel("<html><center>" + comparison + "</center></html>", SwingConstants.CENTER);
		noteLbl.setFont(new Font("Verdana", Font.BOLD, 10));
		noteLbl.setForeground(UIConsts.DARK_BLUE);
		footer.add(noteLbl, BorderLayout.CENTER);
		footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

		card.add(Box.createVerticalStrut(4));
		card.add(footer);

		return card;
	}

	/**
	 * 4-Chart Card for 2-Column Dashboard: Boy (top row) & Girl (2nd row) Rasi & Navamsha in a 2x2 grid.
	 * Separated by a clean horizontal divider line with 10px vertical clearance to chart edges,
	 * and generous 20px horizontal space between Rasi and Navamsha charts.
	 */
	private JPanel createChartsCard2Col() {
		JPanel card = new JPanel(new BorderLayout());
		card.setOpaque(false);
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.setBorder(createCardBorder("Rasi and Navamsha"));

		JPanel inner = new JPanel(new GridBagLayout());
		inner.setOpaque(false);
		// 6px bottom padding matches the top edge to provide balanced margins around charts
		inner.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

		Dimension chartSize = new Dimension(296, 220);

		// Top Row: Boy's Rasi and Navamsha with 20px horizontal space between charts
		JPanel boyRow = new JPanel(new GridLayout(1, 2, 20, 0));
		boyRow.setOpaque(false);
		boyRow.add(new Chart(new PlanetChartData(Varga.Rasi, c.getBoyPlanetaryInfo()), chartSize, "(Boy)"));
		boyRow.add(new Chart(new PlanetChartData(Varga.Navamsa, c.getBoyPlanetaryInfo()), chartSize, "(Boy)"));

		// Horizontal Divider Line separating Boy row and Girl row
		JPanel separator = new JPanel();
		separator.setOpaque(true);
		separator.setBackground(CARD_BORDER_COLOR);
		separator.setPreferredSize(new Dimension(1, 1));
		separator.setMinimumSize(new Dimension(1, 1));

		// 2nd Row: Girl's Rasi and Navamsha with 20px horizontal space between charts
		JPanel girlRow = new JPanel(new GridLayout(1, 2, 20, 0));
		girlRow.setOpaque(false);
		girlRow.add(new Chart(new PlanetChartData(Varga.Rasi, c.getGirlPlanetaryInfo()), chartSize, "(Girl)"));
		girlRow.add(new Chart(new PlanetChartData(Varga.Navamsa, c.getGirlPlanetaryInfo()), chartSize, "(Girl)"));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		// Boy row: 10px gap below to separator line
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 10, 0);
		inner.add(boyRow, gbc);

		// Separator line 1: 10px vertical clearance above and below, 10px lateral insets from chart edges
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 10, 10, 10);
		inner.add(separator, gbc);

		// Girl row: 0px bottom gap (inner padding handles bottom clearance)
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		inner.add(girlRow, gbc);

		card.add(inner, BorderLayout.CENTER);
		card.setPreferredSize(new Dimension(660, 505));
		card.setMinimumSize(new Dimension(660, 505));
		card.setMaximumSize(new Dimension(660, 505));
		return card;
	}

	/**
	 * Dual Shadbala Card for 2-Column Dashboard: Boy & Girl bar charts side-by-side.
	 */
	private JPanel createShadbalaCard2Col() {
		JPanel card = new JPanel(new BorderLayout());
		card.setOpaque(false);
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.setBorder(createCardBorder("Shadbala"));

		JPanel inner = new JPanel(new GridLayout(1, 2, 16, 0));
		inner.setOpaque(false);

		Dimension sbSize = new Dimension(305, 218);
		ShadBalaBarView boyBar = new ShadBalaBarView(c.getBoyHoroscope().getShadBala(), "Boy: " + c.getBoyName(), sbSize);
		ShadBalaBarView girlBar = new ShadBalaBarView(c.getGirlHoroscope().getShadBala(), "Girl: " + c.getGirlName(), sbSize);

		inner.add(boyBar);
		inner.add(girlBar);
		card.add(inner, BorderLayout.CENTER);
		card.setPreferredSize(new Dimension(660, 255));
		card.setMinimumSize(new Dimension(660, 255));
		card.setMaximumSize(new Dimension(660, 255));
		return card;
	}

	/**
	 * Placeholder card when birth date/time are missing (quick Star/Rasi entry mode).
	 */
	private JPanel createQuickEntryNoticeCard() {
		JPanel card = new JPanel(new BorderLayout());
		card.setOpaque(false);
		card.setBorder(createCardBorder("Astrological Charts & Planetary Strength"));

		JPanel msgPanel = new JPanel();
		msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
		msgPanel.setBackground(CARD_BG);
		msgPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

		JLabel msg1 = new JLabel("Quick Star / Rasi Entry Mode", SwingConstants.CENTER);
		msg1.setAlignmentX(Component.CENTER_ALIGNMENT);
		msg1.setFont(new Font("Verdana", Font.BOLD, 14));
		msg1.setForeground(UIConsts.DARK_BLUE);

		JLabel msg2 = new JLabel("Divisional Charts, Shadbala Strength, and Papa Samya Dosha", SwingConstants.CENTER);
		msg2.setAlignmentX(Component.CENTER_ALIGNMENT);
		msg2.setFont(LABEL_FONT);
		msg2.setForeground(new Color(90, 80, 70));

		JLabel msg3 = new JLabel("require exact birth date, time, and geographical coordinates.", SwingConstants.CENTER);
		msg3.setAlignmentX(Component.CENTER_ALIGNMENT);
		msg3.setFont(LABEL_FONT);
		msg3.setForeground(new Color(90, 80, 70));

		JLabel msg4 = new JLabel("To view full charts and strength bars, compute using 'Horoscope' tab.", SwingConstants.CENTER);
		msg4.setAlignmentX(Component.CENTER_ALIGNMENT);
		msg4.setFont(new Font("Verdana", Font.ITALIC, 11));
		msg4.setForeground(new Color(130, 110, 80));

		msgPanel.add(msg1);
		msgPanel.add(Box.createVerticalStrut(10));
		msgPanel.add(msg2);
		msgPanel.add(Box.createVerticalStrut(4));
		msgPanel.add(msg3);
		msgPanel.add(Box.createVerticalStrut(12));
		msgPanel.add(msg4);

		card.add(msgPanel, BorderLayout.CENTER);
		return card;
	}

	/**
	 * Updates the current view mode.
	 */
	public void setViewMode(ViewMode mode) {
		this.currentMode = mode;
	}

	public ViewMode getCurrentViewMode() {
		return currentMode;
	}

	/**
	 * Updates the chart varga.
	 */
	public void setChartVarga(Varga varga) {
		this.currentVarga = varga;
	}

	public Varga getCurrentChartVarga() {
		return currentVarga;
	}

	public JCheckBox getShadbalaCheck() {
		return shadbalaCheck;
	}

	public void setShadbalaVisible(boolean visible) {
		if (shadbalaCheck != null) {
			shadbalaCheck.setSelected(visible);
			if (shadbalaWrapper != null) {
				shadbalaWrapper.setVisible(visible);
				revalidate();
				repaint();
			}
		}
	}

	public boolean isShadbalaVisible() {
		return shadbalaWrapper != null && shadbalaWrapper.isVisible();
	}

	private Border createCardBorder(String title) {
		Border line = BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1);
		Border titled = BorderFactory.createTitledBorder(line, " " + title + " ", TitledBorder.LEFT, TitledBorder.TOP, TITLE_FONT, UIConsts.DARK_BLUE);
		Border padding = BorderFactory.createEmptyBorder(6, 6, 6, 6);
		return BorderFactory.createCompoundBorder(titled, padding);
	}

	/**
	 * Wrapper TableCellRenderer that adds left padding to text labels (e.g. Kuta and Dosha names),
	 * preventing text from hugging the left table/cell border.
	 */
	private static class LeftPaddedCellRenderer implements TableCellRenderer {
		private final TableCellRenderer delegate;
		private final int padding;

		public LeftPaddedCellRenderer(TableCellRenderer delegate, int padding) {
			this.delegate = delegate;
			this.padding = padding;
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			TableCellRenderer r = (delegate != null) ? delegate : table.getDefaultRenderer(table.getColumnClass(column));
			Component c = r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (c instanceof JComponent) {
				((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, padding, 0, 0));
			}
			return c;
		}
	}
}
