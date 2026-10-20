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
	private static final Dimension CHART_SIZE_2COL = new Dimension(330, 260);
	private static final Dimension CHART_SIZE_3COL = new Dimension(340, 270);
	private static final Dimension SHADBALA_SIZE_2COL = new Dimension(345, 218);
	private static final Dimension SHADBALA_SIZE_3COL = new Dimension(350, 218);

	private static final Font TITLE_FONT = new Font("Verdana", Font.BOLD, 13);
	private static final Font LABEL_FONT = new Font("Verdana", Font.BOLD, 11);
	private static final Font VALUE_FONT = new Font("Verdana", Font.PLAIN, 11);
	private static final Font BADGE_FONT = new Font("Verdana", Font.BOLD, 12);
	private static final Font SECTION_FONT = new Font("Verdana", Font.BOLD, 12);

	private static final Color CARD_BORDER_COLOR = new Color(210, 180, 120);
	private static final Color CARD_BG = new Color(253, 251, 247);
	private static final Color HEADER_BG = new Color(245, 240, 230);
	private static final Color PASS_COLOR = new Color(0, 120, 0);
	private static final Color FAIL_COLOR = new Color(180, 40, 40);

	private static final DecimalFormat DOSHA_FMT = new DecimalFormat("0.00");
	private static final DecimalFormat PCT_FMT = new DecimalFormat("0.0'%'");

	private final Compactibility c;
	private ViewMode currentMode = ViewMode.DASHBOARD;
	private Varga currentVarga = Varga.Rasi;

	private JRadioButton rbDashboard;
	private JRadioButton rbWidescreen;
	private JRadioButton rbRasi;
	private JRadioButton rbNavamsha;

	private JPanel cardPanel;
	private CardLayout cardLayout;

	// Chart host panels for dynamic varga refresh
	private JPanel boyChartHost2Col;
	private JPanel girlChartHost2Col;
	private JPanel boyChartHost3Col;
	private JPanel girlChartHost3Col;

	public MarriageMatchView(Compactibility c, Point loc) {
		super(VIEW_SIZE, loc);
		this.c = c;
		initUI();
	}

	private void initUI() {
		setLayout(new BorderLayout());

		// 1. Top Control Strip
		add(createTopControlBar(), BorderLayout.NORTH);

		// 2. Central Card Panel hosting Dashboard vs Widescreen
		cardLayout = new CardLayout();
		cardPanel = new JPanel(cardLayout);

		JPanel dashboardPanel = createDashboardView();
		JPanel widescreenPanel = createWidescreenView();

		cardPanel.add(new JScrollPane(dashboardPanel), ViewMode.DASHBOARD.name());
		cardPanel.add(new JScrollPane(widescreenPanel), ViewMode.WIDESCREEN.name());

		add(cardPanel, BorderLayout.CENTER);
	}

	/**
	 * Top control bar with Title, View Mode switchers, Chart varga switchers, and Summary action.
	 */
	private JPanel createTopControlBar() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(HEADER_BG);
		bar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER_COLOR),
				BorderFactory.createEmptyBorder(6, 12, 6, 12)));

		// Left: Title
		String bName = c.getBoyName() != null ? c.getBoyName() : "Boy";
		String gName = c.getGirlName() != null ? c.getGirlName() : "Girl";
		JLabel titleLabel = new JLabel("Marriage Match: " + bName + " & " + gName);
		titleLabel.setFont(new Font("Verdana", Font.BOLD, 15));
		titleLabel.setForeground(UIConsts.DARK_BLUE);
		bar.add(titleLabel, BorderLayout.WEST);

		// Center: Mode Radio buttons
		JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
		modePanel.setOpaque(false);
		JLabel modeLabel = new JLabel("View Mode:");
		modeLabel.setFont(LABEL_FONT);
		modeLabel.setForeground(new Color(90, 80, 70));
		modePanel.add(modeLabel);

		rbDashboard = new JRadioButton(ViewMode.DASHBOARD.getLabel(), true);
		rbDashboard.setOpaque(false);
		rbDashboard.setFont(LABEL_FONT);
		rbDashboard.setForeground(UIConsts.DARK_BLUE);

		rbWidescreen = new JRadioButton(ViewMode.WIDESCREEN.getLabel(), false);
		rbWidescreen.setOpaque(false);
		rbWidescreen.setFont(LABEL_FONT);
		rbWidescreen.setForeground(UIConsts.DARK_BLUE);

		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(rbDashboard);
		modeGroup.add(rbWidescreen);

		rbDashboard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setViewMode(ViewMode.DASHBOARD);
			}
		});

		rbWidescreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setViewMode(ViewMode.WIDESCREEN);
			}
		});

		modePanel.add(rbDashboard);
		modePanel.add(rbWidescreen);
		bar.add(modePanel, BorderLayout.CENTER);

		// Right: Chart Varga switcher (Rasi / Navamsha)
		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		rightPanel.setOpaque(false);

		if (c.hasHoroscope()) {
			JLabel chartLabel = new JLabel("Chart:");
			chartLabel.setFont(LABEL_FONT);
			chartLabel.setForeground(new Color(90, 80, 70));
			rightPanel.add(chartLabel);

			rbRasi = new JRadioButton("Rasi", true);
			rbRasi.setOpaque(false);
			rbRasi.setFont(LABEL_FONT);
			rbRasi.setForeground(UIConsts.DARK_BLUE);

			rbNavamsha = new JRadioButton("Navamsha", false);
			rbNavamsha.setOpaque(false);
			rbNavamsha.setFont(LABEL_FONT);
			rbNavamsha.setForeground(UIConsts.DARK_BLUE);

			ButtonGroup chartGroup = new ButtonGroup();
			chartGroup.add(rbRasi);
			chartGroup.add(rbNavamsha);

			ActionListener chartVargaListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setChartVarga(rbRasi.isSelected() ? Varga.Rasi : Varga.Navamsa);
				}
			};
			rbRasi.addActionListener(chartVargaListener);
			rbNavamsha.addActionListener(chartVargaListener);

			rightPanel.add(rbRasi);
			rightPanel.add(rbNavamsha);
		}

		bar.add(rightPanel, BorderLayout.EAST);
		return bar;
	}

	/**
	 * Mode 1: Dashboard (2-Column) Layout (Default)
	 * Left Column: Profiles, Kuta Score & Table, Dosha Comparison Table
	 * Right Column: Charts (Boy & Girl) & Dual Shadbala bar graphs
	 */
	private JPanel createDashboardView() {
		JPanel container = new JPanel(new GridBagLayout());
		container.setBackground(Color.WHITE);
		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.weighty = 1.0;

		// --- Left Column ---
		gbc.gridx = 0;
		gbc.weightx = 0.46;
		JPanel leftCol = new JPanel();
		leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
		leftCol.setOpaque(false);

		leftCol.add(createCombinedProfileCard());
		leftCol.add(Box.createVerticalStrut(10));
		leftCol.add(createKutaScoreCard());
		leftCol.add(Box.createVerticalStrut(10));
		if (c.hasHoroscope()) {
			leftCol.add(createDoshaCard());
		}
		leftCol.add(Box.createVerticalGlue());
		container.add(leftCol, gbc);

		// --- Right Column ---
		gbc.gridx = 1;
		gbc.weightx = 0.54;
		JPanel rightCol = new JPanel();
		rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
		rightCol.setOpaque(false);

		if (c.hasHoroscope()) {
			rightCol.add(createChartsCard2Col());
			rightCol.add(Box.createVerticalStrut(10));
			rightCol.add(createShadbalaCard2Col());
		} else {
			rightCol.add(createQuickEntryNoticeCard());
		}
		rightCol.add(Box.createVerticalGlue());
		container.add(rightCol, gbc);

		return container;
	}

	/**
	 * Mode 2: Widescreen (3-Column) Layout
	 * Left Column: Boy Profile, Chart, Shadbala
	 * Center Column: Kuta Score & Table, Dosha Comparison Table
	 * Right Column: Girl Profile, Chart, Shadbala
	 */
	private JPanel createWidescreenView() {
		JPanel container = new JPanel(new GridLayout(1, 3, 10, 0));
		container.setBackground(Color.WHITE);
		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// 1. Left: Boy's Domain
		JPanel boyCol = new JPanel();
		boyCol.setLayout(new BoxLayout(boyCol, BoxLayout.Y_AXIS));
		boyCol.setOpaque(false);
		boyCol.add(createSingleProfileCard(true));
		boyCol.add(Box.createVerticalStrut(10));
		if (c.hasHoroscope()) {
			boyChartHost3Col = new JPanel(new BorderLayout());
			boyChartHost3Col.setOpaque(false);
			boyChartHost3Col.setBorder(createCardBorder("Boy Chart: " + c.getBoyName() + " (" + currentVarga + ")"));
			boyChartHost3Col.add(new Chart(new PlanetChartData(currentVarga, c.getBoyPlanetaryInfo()), CHART_SIZE_3COL), BorderLayout.CENTER);
			boyCol.add(boyChartHost3Col);
			boyCol.add(Box.createVerticalStrut(10));

			ShadBalaBarView boyShadbala = new ShadBalaBarView(c.getBoyHoroscope().getShadBala(), "Boy Shadbala: " + c.getBoyName(), SHADBALA_SIZE_3COL);
			boyCol.add(boyShadbala);
		}
		boyCol.add(Box.createVerticalGlue());
		container.add(boyCol);

		// 2. Center: Match Analysis
		JPanel matchCol = new JPanel();
		matchCol.setLayout(new BoxLayout(matchCol, BoxLayout.Y_AXIS));
		matchCol.setOpaque(false);
		matchCol.add(createKutaScoreCard());
		matchCol.add(Box.createVerticalStrut(10));
		if (c.hasHoroscope()) {
			matchCol.add(createDoshaCard());
		}
		matchCol.add(Box.createVerticalGlue());
		container.add(matchCol);

		// 3. Right: Girl's Domain
		JPanel girlCol = new JPanel();
		girlCol.setLayout(new BoxLayout(girlCol, BoxLayout.Y_AXIS));
		girlCol.setOpaque(false);
		girlCol.add(createSingleProfileCard(false));
		girlCol.add(Box.createVerticalStrut(10));
		if (c.hasHoroscope()) {
			girlChartHost3Col = new JPanel(new BorderLayout());
			girlChartHost3Col.setOpaque(false);
			girlChartHost3Col.setBorder(createCardBorder("Girl Chart: " + c.getGirlName() + " (" + currentVarga + ")"));
			girlChartHost3Col.add(new Chart(new PlanetChartData(currentVarga, c.getGirlPlanetaryInfo()), CHART_SIZE_3COL), BorderLayout.CENTER);
			girlCol.add(girlChartHost3Col);
			girlCol.add(Box.createVerticalStrut(10));

			ShadBalaBarView girlShadbala = new ShadBalaBarView(c.getGirlHoroscope().getShadBala(), "Girl Shadbala: " + c.getGirlName(), SHADBALA_SIZE_3COL);
			girlCol.add(girlShadbala);
		}
		girlCol.add(Box.createVerticalGlue());
		container.add(girlCol);

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
		return card;
	}

	/**
	 * Single Profile Card for 3-Column Widescreen with smart wrapping.
	 */
	private JPanel createSingleProfileCard(boolean isBoy) {
		String title = isBoy ? "Boy Profile: " + c.getBoyName() : "Girl Profile: " + c.getGirlName();
		JPanel card = new JPanel(new BorderLayout());
		card.setOpaque(false);
		card.setBorder(createCardBorder(title));
		card.add(createProfileContent(isBoy), BorderLayout.CENTER);
		return card;
	}

	/**
	 * Internal profile content builder utilizing smart-wrapping JTextAreas to prevent clipping.
	 */
	private JPanel createProfileContent(boolean isBoy) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(CARD_BG);
		panel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

		String name = isBoy ? c.getBoyName() : c.getGirlName();
		BirthData bd = isBoy ? c.getBoyBirthData() : c.getGirlBirthData();

		panel.add(createFieldRow("Name:", name));

		if (c.hasHoroscope() && bd != null) {
			panel.add(createFieldRow("DOB:", bd.birthDayString()));
			String tob = AstroUtil.timeFormat(bd.birthTime(), true);
			if (bd.isDstActive()) tob += " (DST)";
			panel.add(createFieldRow("TOB:", tob));
			panel.add(createFieldRow("Place:", bd.place()));
			panel.add(createFieldRow("Coords:", bd.getBirthPlace().coordinates()));
		}

		String nakStr = isBoy ? c.getBoyNak().toString() : c.getGirlNak().toString();
		String rasiStr = isBoy ? c.getBoyRasi().toString() : c.getGirlRasi().toString();
		panel.add(createFieldRow("Nakshatra:", nakStr));
		panel.add(createFieldRow("Janma Rasi:", rasiStr));

		if (c.hasHoroscope()) {
			String dasa = isBoy ? c.getBoyCurPeriod() : c.getGirlCurPeriod();
			panel.add(createFieldRow("Current Dasa:", dasa != null ? dasa : "-"));

			if (isBoy && c.getBeejaRasi() != null) {
				String beejaStr = c.getBeejaRasi().toString() + " " + AstroUtil.dms(c.getBeejaPos());
				panel.add(createFieldRow("Beeja Sphuta:", beejaStr));
			} else if (!isBoy && c.getKshetraRasi() != null) {
				String kshetraStr = c.getKshetraRasi().toString() + " " + AstroUtil.dms(c.getKshetraPos());
				panel.add(createFieldRow("Kshetra Sphuta:", kshetraStr));
			}
		}

		return panel;
	}

	/**
	 * Helper to create a smart-wrapping label/value row that never clips.
	 */
	private JPanel createFieldRow(String label, String value) {
		JPanel row = new JPanel(new BorderLayout(4, 0));
		row.setOpaque(false);

		JLabel lbl = new JLabel(label);
		lbl.setFont(LABEL_FONT);
		lbl.setForeground(new Color(80, 70, 60));
		lbl.setPreferredSize(new Dimension(110, 18));
		row.add(lbl, BorderLayout.WEST);

		JTextArea val = new JTextArea(value != null && !value.trim().isEmpty() ? value : "-");
		val.setFont(VALUE_FONT);
		val.setForeground(UIConsts.DARK_BLUE);
		val.setWrapStyleWord(true);
		val.setLineWrap(true);
		val.setOpaque(false);
		val.setEditable(false);
		val.setFocusable(false);
		row.add(val, BorderLayout.CENTER);

		return row;
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
		card.setBorder(createCardBorder("Kuta Compatibility Analysis"));

		// 1. Score Summary Banner (Neutral, no judgments)
		int totalGained = c.getTotalKutaGained();
		int maxTotal = Kuta.totalValue();
		double pct = (totalGained * 100.0) / maxTotal;

		JPanel banner = new JPanel(new BorderLayout());
		banner.setBackground(new Color(248, 245, 238));
		banner.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1),
				BorderFactory.createEmptyBorder(6, 8, 6, 8)));

		JLabel scoreLbl = new JLabel("Total Points: " + totalGained + " / " + maxTotal + " (" + PCT_FMT.format(pct) + ")", SwingConstants.CENTER);
		scoreLbl.setFont(new Font("Verdana", Font.BOLD, 13));
		scoreLbl.setForeground(UIConsts.DARK_BLUE);
		banner.add(scoreLbl, BorderLayout.CENTER);
		banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

		card.add(banner);
		card.add(Box.createVerticalStrut(6));

		// 2. Kuta Table (12 Kutas + Total)
		SortableTableModel kutaModel = new SortableTableModel(c.getKutaTableData(), Compactibility.getKutaTableColumnMetaData());
		SortableTable kutaTable = new SortableTable(kutaModel, TableStyle.SCROLL);
		kutaTable.setColumnWidth(140, AstrosoftTableColumn.Kuta);
		kutaTable.setRowColor(13, Color.RED);

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
		card.setBorder(createCardBorder("Dosha Comparison (Papa Samya)"));

		final AstrosoftTable doshaTable = new AstrosoftTable(
				new AstrosoftTableModel(c.getDoshaTableData(), Compactibility.getDoshaTableColumnMetaData()),
				TableStyle.STANDARD);
		doshaTable.setRowColor(5, Color.RED);
		doshaTable.setColumnNumberFormat(AstrosoftTableColumn.Boy, new java.text.DecimalFormat("00.00"));
		doshaTable.setColumnNumberFormat(AstrosoftTableColumn.Girl, new java.text.DecimalFormat("00.00"));

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
		footer.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));

		JLabel noteLbl = new JLabel(comparison, SwingConstants.CENTER);
		noteLbl.setFont(new Font("Verdana", Font.BOLD, 11));
		noteLbl.setForeground(UIConsts.DARK_BLUE);
		footer.add(noteLbl, BorderLayout.CENTER);
		footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

		card.add(Box.createVerticalStrut(4));
		card.add(footer);

		return card;
	}

	/**
	 * Charts Card for 2-Column Dashboard: Boy & Girl side-by-side.
	 */
	private JPanel createChartsCard2Col() {
		JPanel card = new JPanel(new BorderLayout());
		card.setOpaque(false);
		card.setBorder(createCardBorder("Horoscope Charts (" + currentVarga + ")"));

		JPanel inner = new JPanel(new GridLayout(1, 2, 8, 0));
		inner.setOpaque(false);

		boyChartHost2Col = new JPanel(new BorderLayout());
		boyChartHost2Col.setOpaque(false);
		boyChartHost2Col.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Boy: " + c.getBoyName(), TitledBorder.CENTER, TitledBorder.TOP, LABEL_FONT, UIConsts.DARK_BLUE));
		boyChartHost2Col.add(new Chart(new PlanetChartData(currentVarga, c.getBoyPlanetaryInfo()), CHART_SIZE_2COL), BorderLayout.CENTER);

		girlChartHost2Col = new JPanel(new BorderLayout());
		girlChartHost2Col.setOpaque(false);
		girlChartHost2Col.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Girl: " + c.getGirlName(), TitledBorder.CENTER, TitledBorder.TOP, LABEL_FONT, UIConsts.DARK_BLUE));
		girlChartHost2Col.add(new Chart(new PlanetChartData(currentVarga, c.getGirlPlanetaryInfo()), CHART_SIZE_2COL), BorderLayout.CENTER);

		inner.add(boyChartHost2Col);
		inner.add(girlChartHost2Col);
		card.add(inner, BorderLayout.CENTER);
		return card;
	}

	/**
	 * Dual Shadbala Card for 2-Column Dashboard: Boy & Girl bar charts side-by-side.
	 */
	private JPanel createShadbalaCard2Col() {
		JPanel card = new JPanel(new BorderLayout());
		card.setOpaque(false);
		card.setBorder(createCardBorder("Planetary Strength (Shadbala) Comparison"));

		JPanel inner = new JPanel(new GridLayout(1, 2, 8, 0));
		inner.setOpaque(false);

		ShadBalaBarView boyBar = new ShadBalaBarView(c.getBoyHoroscope().getShadBala(), "Boy: " + c.getBoyName(), SHADBALA_SIZE_2COL);
		ShadBalaBarView girlBar = new ShadBalaBarView(c.getGirlHoroscope().getShadBala(), "Girl: " + c.getGirlName(), SHADBALA_SIZE_2COL);

		inner.add(boyBar);
		inner.add(girlBar);
		card.add(inner, BorderLayout.CENTER);
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
	 * Updates the current view mode (Dashboard 2-Col vs Widescreen 3-Col).
	 */
	public void setViewMode(ViewMode mode) {
		this.currentMode = mode;
		if (cardLayout != null) {
			cardLayout.show(cardPanel, mode.name());
		}
		if (rbDashboard != null && rbWidescreen != null) {
			if (mode == ViewMode.DASHBOARD && !rbDashboard.isSelected()) {
				rbDashboard.setSelected(true);
			} else if (mode == ViewMode.WIDESCREEN && !rbWidescreen.isSelected()) {
				rbWidescreen.setSelected(true);
			}
		}
		revalidate();
		repaint();
	}

	public ViewMode getCurrentViewMode() {
		return currentMode;
	}

	/**
	 * Updates the chart varga (Rasi vs Navamsha).
	 */
	public void setChartVarga(Varga varga) {
		this.currentVarga = varga;
		if (!c.hasHoroscope()) return;

		if (boyChartHost2Col != null) {
			boyChartHost2Col.removeAll();
			boyChartHost2Col.add(new Chart(new PlanetChartData(varga, c.getBoyPlanetaryInfo()), CHART_SIZE_2COL), BorderLayout.CENTER);
			boyChartHost2Col.revalidate();
			boyChartHost2Col.repaint();
		}

		if (girlChartHost2Col != null) {
			girlChartHost2Col.removeAll();
			girlChartHost2Col.add(new Chart(new PlanetChartData(varga, c.getGirlPlanetaryInfo()), CHART_SIZE_2COL), BorderLayout.CENTER);
			girlChartHost2Col.revalidate();
			girlChartHost2Col.repaint();
		}

		if (boyChartHost3Col != null) {
			boyChartHost3Col.removeAll();
			boyChartHost3Col.setBorder(createCardBorder("Boy Chart: " + c.getBoyName() + " (" + currentVarga + ")"));
			boyChartHost3Col.add(new Chart(new PlanetChartData(varga, c.getBoyPlanetaryInfo()), CHART_SIZE_3COL), BorderLayout.CENTER);
			boyChartHost3Col.revalidate();
			boyChartHost3Col.repaint();
		}

		if (girlChartHost3Col != null) {
			girlChartHost3Col.removeAll();
			girlChartHost3Col.setBorder(createCardBorder("Girl Chart: " + c.getGirlName() + " (" + currentVarga + ")"));
			girlChartHost3Col.add(new Chart(new PlanetChartData(varga, c.getGirlPlanetaryInfo()), CHART_SIZE_3COL), BorderLayout.CENTER);
			girlChartHost3Col.revalidate();
			girlChartHost3Col.repaint();
		}

		if (rbRasi != null && rbNavamsha != null) {
			if (varga == Varga.Rasi && !rbRasi.isSelected()) {
				rbRasi.setSelected(true);
			} else if (varga == Varga.Navamsa && !rbNavamsha.isSelected()) {
				rbNavamsha.setSelected(true);
			}
		}
	}

	public Varga getCurrentChartVarga() {
		return currentVarga;
	}

	private Border createCardBorder(String title) {
		Border line = BorderFactory.createLineBorder(CARD_BORDER_COLOR, 1);
		Border titled = BorderFactory.createTitledBorder(line, " " + title + " ", TitledBorder.LEFT, TitledBorder.TOP, TITLE_FONT, UIConsts.DARK_BLUE);
		Border padding = BorderFactory.createEmptyBorder(6, 6, 6, 6);
		return BorderFactory.createCompoundBorder(titled, padding);
	}
}
