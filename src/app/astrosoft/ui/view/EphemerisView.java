/**
 * EphemerisView.java
 * Created On 2006, Apr 6, 2006 3:33:23 PM
 * @author E. Rajasekar
 */
package app.astrosoft.ui.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;

import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.TableStyle;
import app.astrosoft.core.Ephemeris;
import app.astrosoft.core.Ephemeris.Mode;
import app.astrosoft.pref.AstrosoftPref;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.CalendarChooser;
import app.astrosoft.ui.comp.CalendarSpinner;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.comp.DateListener;
import app.astrosoft.ui.comp.TitleLabel;
import app.astrosoft.ui.comp.TitledTable;
import app.astrosoft.ui.comp.modern.DatePickerDialog;
import app.astrosoft.ui.comp.modern.ModernButton;
import app.astrosoft.ui.comp.modern.ModernScrollBarUI;
import app.astrosoft.ui.dlg.EphemerisConfigDialog;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.EphemerisCellRenderer;
import app.astrosoft.ui.table.Table;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.util.AstroUtil;
import app.astrosoft.util.Internalization;

public class EphemerisView extends AstrosoftView {

	public enum UiMode {
		Basic,
		Advanced
	}

	private static final Logger log = Logger.getLogger(EphemerisView.class.getName());

	private static final Dimension viewSize = new Dimension(860, 500);
	private static final Dimension basicDailyTableSize = new Dimension(viewSize.width, 468);
	private static final Dimension monthyTableSize = new Dimension(viewSize.width, 330);
	private static final Dimension D1_CHART_SIZE = new Dimension(316, 316);
	private static final int CARD_ARC = 12;

	private static final Integer[] HOUR_STEPS = { 1, 2, 3, 4, 6, 12 };
	private static final Integer[] MINUTE_STEPS = { 1, 5, 10, 15, 30, 45 };
	private static final AstrosoftTableColumn[] PLANET_COLS = {
			AstrosoftTableColumn.Sun, AstrosoftTableColumn.Moon, AstrosoftTableColumn.Mars,
			AstrosoftTableColumn.Mercury, AstrosoftTableColumn.Jupiter, AstrosoftTableColumn.Venus,
			AstrosoftTableColumn.Saturn, AstrosoftTableColumn.Rahu, AstrosoftTableColumn.Ketu
	};

	// Shared model & dual tables (Basic vs Advanced)
	private AstrosoftTableModel tableModel;
	private AstrosoftTable ephTable;
	private AstrosoftTable advTable;
	private TitledTable tablePanel;

	// Basic mode controls
	private CalendarChooser monthChooser;
	private JRadioButton basicDailyRadio;
	private JRadioButton basicMonthlyRadio;

	// Advanced mode controls
	private ModernButton advDateBtn;
	private ModernButton advLocTimeBtn;
	private ModernButton hrBackBtn;
	private JComboBox<Integer> hrStepCombo;
	private ModernButton hrFwdBtn;
	private ModernButton minBackBtn;
	private JComboBox<Integer> minStepCombo;
	private ModernButton minFwdBtn;
	private ModernButton advDailyBtn;
	private ModernButton advMonthlyBtn;

	// Advanced D1 chart panel
	private JPanel chartCard;
	private JLabel chartSubtitleLabel;
	private JPanel chartContainer;
	private JSplitPane advSplitPane;

	// Mode switcher strip & body cards
	private ModernButton uiBasicBtn;
	private ModernButton uiAdvancedBtn;
	private CardLayout bodyCardLayout;
	private JPanel bodyCardPanel;

	// State
	private UiMode uiMode = UiMode.Basic;
	private Ephemeris currentEphemeris;
	private Calendar ephDate;
	private Calendar activeDateTime;
	private Place sessionPlace;
	private Mode ephMode;
	private int selectedMonthRow = 0;
	private boolean isUpdatingTable = false;

	public EphemerisView(Ephemeris eph, Point loc) {
		super(viewSize, loc);

		this.currentEphemeris = eph;
		this.ephMode = eph.getMode();
		this.ephDate = (Calendar) eph.getDate().clone();
		initSessionStateFromPrefs();

		tableModel = new AstrosoftTableModel(eph.getEphemerisTable());

		// Basic table (legacy TitledTable)
		ephTable = new AstrosoftTable(tableModel, TableStyle.SCROLL_SINGLE_ROW_SELECT);
		setEphemerisColumnWidths(ephTable, false);

		add(createUiModeStrip(), BorderLayout.NORTH);

		bodyCardLayout = new CardLayout();
		bodyCardPanel = new JPanel(bodyCardLayout);
		bodyCardPanel.add(createBasicBodyCard(), UiMode.Basic.name());
		bodyCardLayout.show(bodyCardPanel, uiMode.name());
		add(bodyCardPanel, BorderLayout.CENTER);

		ephTable.getSelectionModel().addListSelectionListener(e -> onTableRowSelected(ephTable, e.getValueIsAdjusting()));
	}

	private void ensureAdvancedCardCreated() {
		if (advTable != null) {
			return;
		}
		UIConsts.ChartTheme theme = UIConsts.getChartTheme();
		advTable = new AstrosoftTable(tableModel, TableStyle.SCROLL_SINGLE_ROW_SELECT);
		advTable.setDefaultRenderer(Ephemeris.EphData.class, new EphemerisCellRenderer(true));
		advTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		applyThemeToAdvancedTable(theme);
		setEphemerisColumnWidths(advTable, true);
		advTable.getSelectionModel().addListSelectionListener(e -> onTableRowSelected(advTable, e.getValueIsAdjusting()));

		bodyCardPanel.add(createAdvancedBodyCard(theme), UiMode.Advanced.name());
	}

	private JPanel createBasicBodyCard() {
		monthChooser = CalendarChooser.getMonthChooser();
		tablePanel = new TitledTable(ephTable, ephMode.isMonthly() ? monthyTableSize : basicDailyTableSize);

		JTableHeader header = ephTable.getTableHeader();
		tablePanel.add(createBasicEphSelector(header.getBackground(), header.getForeground()), BorderLayout.PAGE_START);

		monthChooser.addDateListener(new DateListener() {
			public void dateChanged(Date date) {
				ephDate = AstroUtil.getCalendar(date);
				syncActiveDateFromBasic();
				updateEphTable();
			}
		});

		JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
		wrapper.add(tablePanel);
		return wrapper;
	}

	private JPanel createAdvancedBodyCard(UIConsts.ChartTheme theme) {
		JPanel advRoot = new JPanel(new BorderLayout(0, 12));
		advRoot.setBackground(UIConsts.CANVAS_BACKGROUND);
		advRoot.setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));

		advRoot.add(createAdvancedToolbarCard(theme), BorderLayout.NORTH);

		// Left: Expandable themed table card
		JPanel tableCard = createRoundedCard(theme);
		tableCard.setLayout(new BorderLayout(0, 6));
		tableCard.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

		JScrollPane tableScroll = new JScrollPane(advTable);
		tableScroll.setBorder(BorderFactory.createEmptyBorder());
		tableScroll.getViewport().setBackground(theme.getBackground());
		tableScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
		tableScroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
		tableCard.add(tableScroll, BorderLayout.CENTER);

		// Right: Live D1 Chart card (Daily mode only)
		chartCard = createRoundedCard(theme);
		chartCard.setLayout(new BorderLayout(0, 6));
		chartCard.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
		chartCard.setPreferredSize(new Dimension(356, 396));
		chartCard.setMinimumSize(new Dimension(336, 356));

		JPanel chartHeader = new JPanel(new BorderLayout());
		chartHeader.setOpaque(false);
		JLabel chartTitle = new JLabel("D1 Rasi Chart");
		chartTitle.setFont(UIUtil.getFont(Font.BOLD, 13));
		chartTitle.setForeground(theme.getLabelColor());

		chartSubtitleLabel = new JLabel("");
		chartSubtitleLabel.setFont(UIUtil.getFont(Font.PLAIN, 11));
		chartSubtitleLabel.setForeground(theme.getPlanetForeground());

		chartHeader.add(chartTitle, BorderLayout.WEST);
		chartHeader.add(chartSubtitleLabel, BorderLayout.EAST);
		chartCard.add(chartHeader, BorderLayout.NORTH);

		chartContainer = new JPanel(new GridBagLayout());
		chartContainer.setOpaque(false);
		chartCard.add(chartContainer, BorderLayout.CENTER);

		advSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableCard, chartCard);
		advSplitPane.setResizeWeight(1.0);
		advSplitPane.setContinuousLayout(true);
		advSplitPane.setBorder(null);
		advSplitPane.setOpaque(false);
		advSplitPane.setDividerSize(8);

		chartCard.setVisible(ephMode.isDaily());
		advSplitPane.setDividerSize(ephMode.isDaily() ? 8 : 0);

		advRoot.add(advSplitPane, BorderLayout.CENTER);
		return advRoot;
	}

	private JPanel createRoundedCard(final UIConsts.ChartTheme theme) {
		JPanel card = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int w = getWidth();
				int h = getHeight();
				g2.setColor(theme.getBackground());
				g2.fillRoundRect(0, 0, w - 1, h - 1, CARD_ARC, CARD_ARC);
				g2.setColor(theme.getBorderColor());
				g2.drawRoundRect(0, 0, w - 1, h - 1, CARD_ARC, CARD_ARC);
				g2.dispose();
			}
		};
		card.setOpaque(false);
		return card;
	}

	private void applyThemeToAdvancedTable(UIConsts.ChartTheme theme) {
		advTable.setBackground(theme.getBackground());
		advTable.setForeground(theme.getPlanetForeground());
		advTable.setGridColor(theme.getBorderColor());
		advTable.setShowVerticalLines(false);
		advTable.setShowHorizontalLines(true);
		advTable.setIntercellSpacing(new Dimension(0, 1));
		advTable.setSelectionBackground(theme.getPrimaryAccentColor());
		advTable.setSelectionForeground(Color.WHITE);

		JTableHeader th = advTable.getTableHeader();
		if (th != null) {
			th.setBackground(theme.getBackground());
			th.setForeground(theme.getLabelColor());
			th.setPreferredSize(new Dimension(th.getPreferredSize().width, 30));
			th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, theme.getBorderColor()));
		}
	}

	private void onTableRowSelected(AstrosoftTable sourceTable, boolean adjusting) {
		if (isUpdatingTable || adjusting) {
			return;
		}
		int row = sourceTable.getSelectedRow();
		if (row < 0) {
			return;
		}
		if (ephMode.isDaily()) {
			int maxDay = activeDateTime.getActualMaximum(Calendar.DAY_OF_MONTH);
			int day = Math.min(row + 1, maxDay);
			activeDateTime.set(Calendar.DAY_OF_MONTH, day);
			log.fine("Ephemeris active day selected: " + day);
		} else {
			selectedMonthRow = Math.min(row, 11);
		}
		if (uiMode == UiMode.Advanced) {
			updateD1Chart(row);
		}
	}

	private void initSessionStateFromPrefs() {
		AstrosoftPref prefs = AstroSoft.getPreferences();
		sessionPlace = prefs.getPlace();
		activeDateTime = (Calendar) ephDate.clone();
		double calcHours = prefs.getEphCalcTime();
		int hr = (int) Math.floor(calcHours);
		int min = (int) Math.round((calcHours - hr) * 60.0);
		if (min == 60) {
			hr = (hr + 1) % 24;
			min = 0;
		}
		activeDateTime.set(Calendar.HOUR_OF_DAY, hr);
		activeDateTime.set(Calendar.MINUTE, min);
		activeDateTime.set(Calendar.SECOND, 0);
		activeDateTime.set(Calendar.MILLISECOND, 0);
		selectedMonthRow = activeDateTime.get(Calendar.MONTH);
	}

	private void syncActiveDateFromBasic() {
		if (activeDateTime == null || ephDate == null) {
			return;
		}
		activeDateTime.set(Calendar.YEAR, ephDate.get(Calendar.YEAR));
		activeDateTime.set(Calendar.MONTH, ephDate.get(Calendar.MONTH));
		int maxDay = activeDateTime.getActualMaximum(Calendar.DAY_OF_MONTH);
		if (activeDateTime.get(Calendar.DAY_OF_MONTH) > maxDay) {
			activeDateTime.set(Calendar.DAY_OF_MONTH, maxDay);
		}
	}

	private JPanel createUiModeStrip() {
		UIConsts.ChartTheme theme = UIConsts.getChartTheme();
		JPanel strip = new JPanel(new BorderLayout());
		strip.setBackground(theme.getBackground());
		strip.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, theme.getBorderColor()),
				BorderFactory.createEmptyBorder(3, 10, 3, 10)));

		JLabel label = new JLabel("Ephemeris Mode");
		label.setFont(UIUtil.getFont(Font.PLAIN, 11));
		label.setForeground(theme.getLabelColor());
		strip.add(label, BorderLayout.WEST);

		JPanel toggleGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		toggleGroup.setOpaque(false);

		Color accent = theme.getPrimaryAccentColor();
		uiBasicBtn = new ModernButton("Basic", ModernButton.Style.PRIMARY, accent);
		uiBasicBtn.setFont(UIUtil.getFont(Font.BOLD, 11));
		uiBasicBtn.setPreferredSize(new Dimension(74, 22));
		uiBasicBtn.addActionListener(e -> setUiMode(UiMode.Basic));

		uiAdvancedBtn = new ModernButton("Advanced", ModernButton.Style.SECONDARY, accent);
		uiAdvancedBtn.setFont(UIUtil.getFont(Font.BOLD, 11));
		uiAdvancedBtn.setPreferredSize(new Dimension(88, 22));
		uiAdvancedBtn.addActionListener(e -> setUiMode(UiMode.Advanced));

		toggleGroup.add(uiBasicBtn);
		toggleGroup.add(uiAdvancedBtn);
		strip.add(toggleGroup, BorderLayout.EAST);
		return strip;
	}

	private JPanel createBasicEphSelector(Color bgClr, Color fgClr) {
		JPanel ephSelector = new JPanel(new BorderLayout());

		JPanel chooser = monthChooser.getChooser();
		monthChooser.setBackground(bgClr);
		monthChooser.setForeground(fgClr);

		ephSelector.setBackground(bgClr);
		ephSelector.add(chooser, BorderLayout.WEST);
		ephSelector.add(createEphTitle(bgClr, fgClr), BorderLayout.CENTER);
		ephSelector.add(createBasicModeSelector(bgClr, fgClr), BorderLayout.EAST);
		ephSelector.setBorder(BorderFactory.createEtchedBorder());
		return ephSelector;
	}

	private JPanel createBasicModeSelector(Color bgClr, Color fgClr) {
		JPanel panel = new JPanel();
		ActionListener modeListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchEphMode(Mode.valueOf(e.getActionCommand()));
			}
		};
		ButtonGroup bg = new ButtonGroup();

		for (Mode m : Mode.values()) {
			JRadioButton button = new JRadioButton(Internalization.getString(m.name()));
			button.setFont(UIUtil.getFont(Font.PLAIN, 12));
			bg.add(button);
			button.setActionCommand(m.name());
			button.addActionListener(modeListener);
			panel.add(button);
			button.setBackground(bgClr);
			button.setForeground(fgClr);

			if (m == ephMode) {
				button.setSelected(true);
			}
			if (m == Mode.Daily) {
				basicDailyRadio = button;
			} else {
				basicMonthlyRadio = button;
			}
		}

		panel.setBackground(bgClr);
		return panel;
	}

	private JPanel createAdvancedToolbarCard(UIConsts.ChartTheme theme) {
		JPanel bar = createRoundedCard(theme);
		bar.setLayout(new BorderLayout(8, 0));
		bar.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

		Color accent = theme.getPrimaryAccentColor();
		Color fgClr = theme.getLabelColor();

		// Left: Date button + Time & Location pill
		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		leftPanel.setOpaque(false);

		advDateBtn = new ModernButton("", ModernButton.Style.SECONDARY, accent);
		advDateBtn.setFont(UIUtil.getFont(Font.BOLD, 12));
		advDateBtn.setPreferredSize(new Dimension(98, 28));
		advDateBtn.addActionListener(e -> openAdvancedDatePicker());

		advLocTimeBtn = new ModernButton("", ModernButton.Style.SECONDARY, accent);
		advLocTimeBtn.setFont(UIUtil.getFont(Font.PLAIN, 12));
		advLocTimeBtn.setPreferredSize(new Dimension(210, 28));
		advLocTimeBtn.addActionListener(e -> openTimeAndLocationDialog());

		leftPanel.add(advDateBtn);
		leftPanel.add(advLocTimeBtn);

		// Center: Inline Hour & Minute Steppers
		JPanel steppersPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
		steppersPanel.setOpaque(false);

		JLabel hrLabel = new JLabel("Hr:");
		hrLabel.setFont(UIUtil.getFont(Font.BOLD, 11));
		hrLabel.setForeground(fgClr);
		hrBackBtn = createStepButton("<<", accent, e -> stepActiveMinutes(-getSelectedStep(hrStepCombo) * 60));
		hrStepCombo = createStepCombo(HOUR_STEPS, 1);
		hrFwdBtn = createStepButton(">>", accent, e -> stepActiveMinutes(getSelectedStep(hrStepCombo) * 60));

		JLabel minLabel = new JLabel("Min:");
		minLabel.setFont(UIUtil.getFont(Font.BOLD, 11));
		minLabel.setForeground(fgClr);
		minBackBtn = createStepButton("<<", accent, e -> stepActiveMinutes(-getSelectedStep(minStepCombo)));
		minStepCombo = createStepCombo(MINUTE_STEPS, 15);
		minFwdBtn = createStepButton(">>", accent, e -> stepActiveMinutes(getSelectedStep(minStepCombo)));

		steppersPanel.add(hrLabel);
		steppersPanel.add(hrBackBtn);
		steppersPanel.add(hrStepCombo);
		steppersPanel.add(hrFwdBtn);
		steppersPanel.add(minLabel);
		steppersPanel.add(minBackBtn);
		steppersPanel.add(minStepCombo);
		steppersPanel.add(minFwdBtn);

		// Right: Daily / Monthly toggle
		JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		modePanel.setOpaque(false);

		advDailyBtn = new ModernButton(Internalization.getString(Mode.Daily.name()), ModernButton.Style.PRIMARY, accent);
		advDailyBtn.setFont(UIUtil.getFont(Font.BOLD, 11));
		advDailyBtn.setPreferredSize(new Dimension(68, 28));
		advDailyBtn.addActionListener(e -> switchEphMode(Mode.Daily));

		advMonthlyBtn = new ModernButton(Internalization.getString(Mode.Monthly.name()), ModernButton.Style.SECONDARY, accent);
		advMonthlyBtn.setFont(UIUtil.getFont(Font.BOLD, 11));
		advMonthlyBtn.setPreferredSize(new Dimension(76, 28));
		advMonthlyBtn.addActionListener(e -> switchEphMode(Mode.Monthly));

		modePanel.add(advDailyBtn);
		modePanel.add(advMonthlyBtn);

		bar.add(leftPanel, BorderLayout.WEST);
		bar.add(steppersPanel, BorderLayout.CENTER);
		bar.add(modePanel, BorderLayout.EAST);

		syncAdvancedControls();
		return bar;
	}

	private ModernButton createStepButton(String text, Color accent, ActionListener listener) {
		ModernButton btn = new ModernButton(text, ModernButton.Style.SECONDARY, accent);
		btn.setFont(UIUtil.getFont(Font.BOLD, 11));
		btn.setPreferredSize(new Dimension(34, 28));
		btn.addActionListener(listener);
		return btn;
	}

	private JComboBox<Integer> createStepCombo(Integer[] values, int defaultVal) {
		JComboBox<Integer> combo = new JComboBox<>(values);
		combo.setSelectedItem(defaultVal);
		combo.setFont(UIUtil.getFont(Font.PLAIN, 11));
		combo.setPreferredSize(new Dimension(54, 28));
		return combo;
	}

	private int getSelectedStep(JComboBox<Integer> combo) {
		Object val = combo.getSelectedItem();
		return (val instanceof Integer) ? (Integer) val : 1;
	}

	private JPanel createEphTitle(Color bgClr, Color fgClr) {
		JPanel panel = new JPanel();
		AstrosoftPref preferences = AstroSoft.getPreferences();
		boolean isTamil = preferences.getLanguage() == Language.TAMIL;
		StringBuilder text = new StringBuilder(DisplayStrings.EPHEMERIS_STR.toString());
		if (isTamil) {
			text.append(" - ").append(preferences.getPlace().city()).append(" நேரம் ").append(AstroUtil.timeFormat(preferences.getEphCalcTime()));
		} else {
			text.append(" For ").append(preferences.getPlace().city()).append(" at ").append(AstroUtil.timeFormat(preferences.getEphCalcTime()));
		}

		JLabel title = new TitleLabel(text.toString());
		panel.add(title);

		title.setFont(UIUtil.getFont(Font.BOLD, 12));
		title.setForeground(fgClr);
		panel.setBackground(bgClr);
		return panel;
	}

	public void setUiMode(UiMode mode) {
		if (mode == null || this.uiMode == mode) {
			return;
		}
		this.uiMode = mode;
		log.fine("Switching Ephemeris UI mode to " + mode);

		if (mode == UiMode.Basic) {
			ephDate = (Calendar) activeDateTime.clone();
			monthChooser.setSelectedDate(ephDate.getTime());
			((CalendarSpinner) monthChooser).setDateFormat(
					ephMode.isMonthly() ? CalendarSpinner.FMT_YEAR : CalendarSpinner.FMT_MONTH_YEAR);
			if (basicDailyRadio != null && basicMonthlyRadio != null) {
				basicDailyRadio.setSelected(ephMode.isDaily());
				basicMonthlyRadio.setSelected(ephMode.isMonthly());
			}
		} else {
			ensureAdvancedCardCreated();
			syncActiveDateFromBasic();
			syncAdvancedControls();
		}

		updateUiModeButtons();
		bodyCardLayout.show(bodyCardPanel, uiMode.name());
		updateTableViewportSize();
		updateEphTable();
	}

	private void updateUiModeButtons() {
		boolean isAdv = (uiMode == UiMode.Advanced);
		uiBasicBtn.setButtonStyle(isAdv ? ModernButton.Style.SECONDARY : ModernButton.Style.PRIMARY);
		uiAdvancedBtn.setButtonStyle(isAdv ? ModernButton.Style.PRIMARY : ModernButton.Style.SECONDARY);
	}

	private void switchEphMode(Mode newMode) {
		if (newMode == null || newMode == ephMode) {
			return;
		}
		AstrosoftTable activeTable = (uiMode == UiMode.Advanced) ? advTable : ephTable;
		int selectedRow = activeTable.getSelectedRow();
		if (selectedRow == -1 && ephMode.isMonthly()) {
			selectedRow = selectedMonthRow;
		}
		ephMode = newMode;

		if (ephMode.isMonthly()) {
			selectedMonthRow = activeDateTime.get(Calendar.MONTH);
			((CalendarSpinner) monthChooser).setDateFormat(CalendarSpinner.FMT_YEAR);
		} else {
			if (selectedRow >= 0 && selectedRow < 12) {
				ephDate.set(Calendar.MONTH, selectedRow);
				activeDateTime.set(Calendar.MONTH, selectedRow);
				activeDateTime.set(Calendar.DAY_OF_MONTH, 1);
				selectedMonthRow = selectedRow;
			}
			monthChooser.setSelectedDate(ephDate.getTime());
			((CalendarSpinner) monthChooser).setDateFormat(CalendarSpinner.FMT_MONTH_YEAR);
		}

		updateTableViewportSize();
		syncAdvancedControls();
		updateEphTable();
	}

	private void updateTableViewportSize() {
		if (ephMode.isMonthly()) {
			tablePanel.setScrollPaneSize(monthyTableSize);
		} else {
			tablePanel.setScrollPaneSize(basicDailyTableSize);
		}
	}

	private void syncAdvancedControls() {
		if (advDateBtn == null) {
			return;
		}
		SimpleDateFormat fmt = new SimpleDateFormat(ephMode.isMonthly() ? "yyyy" : "MMM yyyy");
		advDateBtn.setText(fmt.format(activeDateTime.getTime()) + " \u25BE");

		String city = sessionPlace.city();
		String shortCity = (city.length() > 14) ? city.substring(0, 12) + "..." : city;
		String timeStr = AstroUtil.timeFormat(toCalcHours(activeDateTime));
		advLocTimeBtn.setText(shortCity + " \u2022 " + timeStr);
		advLocTimeBtn.setToolTipText(sessionPlace.toString() + " (" + timeStr + ")");

		boolean isDaily = ephMode.isDaily();
		hrBackBtn.setEnabled(isDaily);
		hrStepCombo.setEnabled(isDaily);
		hrFwdBtn.setEnabled(isDaily);
		minBackBtn.setEnabled(isDaily);
		minStepCombo.setEnabled(isDaily);
		minFwdBtn.setEnabled(isDaily);

		advDailyBtn.setButtonStyle(isDaily ? ModernButton.Style.PRIMARY : ModernButton.Style.SECONDARY);
		advMonthlyBtn.setButtonStyle(isDaily ? ModernButton.Style.SECONDARY : ModernButton.Style.PRIMARY);

		if (chartCard != null && advSplitPane != null) {
			chartCard.setVisible(isDaily);
			advSplitPane.setDividerSize(isDaily ? 8 : 0);
			advSplitPane.revalidate();
			advSplitPane.repaint();
		}
	}

	private void openAdvancedDatePicker() {
		int clampedYear = Math.max(DatePickerDialog.MIN_YEAR,
				Math.min(DatePickerDialog.MAX_YEAR, activeDateTime.get(Calendar.YEAR)));
		LocalDate initial = LocalDate.of(
				clampedYear,
				activeDateTime.get(Calendar.MONTH) + 1,
				activeDateTime.get(Calendar.DAY_OF_MONTH));

		LocalDate picked = DatePickerDialog.showDatePicker(this, initial);
		if (picked == null) {
			return;
		}
		activeDateTime.set(Calendar.YEAR, picked.getYear());
		if (ephMode.isDaily()) {
			activeDateTime.set(Calendar.MONTH, picked.getMonthValue() - 1);
			activeDateTime.set(Calendar.DAY_OF_MONTH, picked.getDayOfMonth());
			selectedMonthRow = picked.getMonthValue() - 1;
		}
		ephDate = (Calendar) activeDateTime.clone();
		syncAdvancedControls();
		updateEphTable();
	}

	private void openTimeAndLocationDialog() {
		LocalTime initialTime = LocalTime.of(
				activeDateTime.get(Calendar.HOUR_OF_DAY),
				activeDateTime.get(Calendar.MINUTE));
		EphemerisConfigDialog.Result res = EphemerisConfigDialog.showConfigDialog(this, initialTime, sessionPlace);
		if (res == null) {
			return;
		}
		activeDateTime.set(Calendar.HOUR_OF_DAY, res.getTime().getHour());
		activeDateTime.set(Calendar.MINUTE, res.getTime().getMinute());
		activeDateTime.set(Calendar.SECOND, 0);
		if (res.getPlace() != null) {
			sessionPlace = res.getPlace();
		}
		syncAdvancedControls();
		updateEphTable();
	}

	public void stepActiveMinutes(int deltaMinutes) {
		if (uiMode != UiMode.Advanced || !ephMode.isDaily()) {
			return;
		}
		activeDateTime.add(Calendar.MINUTE, deltaMinutes);
		int yr = activeDateTime.get(Calendar.YEAR);
		if (yr < DatePickerDialog.MIN_YEAR) {
			activeDateTime.set(DatePickerDialog.MIN_YEAR, Calendar.JANUARY, 1, 0, 0, 0);
		} else if (yr > DatePickerDialog.MAX_YEAR) {
			activeDateTime.set(DatePickerDialog.MAX_YEAR, Calendar.DECEMBER, 31, 23, 59, 0);
		}
		ephDate = (Calendar) activeDateTime.clone();
		selectedMonthRow = activeDateTime.get(Calendar.MONTH);
		log.fine("Stepped Ephemeris time by " + deltaMinutes + "m -> " + activeDateTime.getTime());
		syncAdvancedControls();
		updateEphTable();
	}

	private static double toCalcHours(Calendar cal) {
		return cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0;
	}

	private void setEphemerisColumnWidths(AstrosoftTable targetTable, boolean advancedMode) {
		targetTable.setRowHeight(26);
		boolean showAsc = advancedMode && ephMode.isDaily();

		if (ephMode != null && ephMode.isMonthly()) {
			targetTable.setColumnWidth(60, AstrosoftTableColumn.Month);
			targetTable.setColumnWidth(80, PLANET_COLS);
		} else if (showAsc) {
			targetTable.setColumnWidth(45, AstrosoftTableColumn.Date);
			targetTable.setColumnWidth(72, AstrosoftTableColumn.Ascendant);
			targetTable.setColumnWidth(72, PLANET_COLS);
		} else {
			targetTable.setColumnWidth(50, AstrosoftTableColumn.Date);
			targetTable.setColumnWidth(80, PLANET_COLS);
		}
	}

	private void updateEphTable() {
		if (uiMode == UiMode.Advanced) {
			currentEphemeris = new Ephemeris(activeDateTime, ephMode, toCalcHours(activeDateTime), sessionPlace, true);
		} else {
			currentEphemeris = new Ephemeris(ephDate, ephMode);
		}

		Table table = currentEphemeris.getEphemerisTable();

		isUpdatingTable = true;
		try {
			tableModel.updateColumns(table.getColumnMetaData());
			tableModel.updateData(table.getTableData());

			ephTable.localizeColumns();
			setEphemerisColumnWidths(ephTable, false);

			if (advTable != null) {
				advTable.localizeColumns();
				setEphemerisColumnWidths(advTable, true);
			}

			if (uiMode == UiMode.Advanced && advTable != null) {
				int targetRow = ephMode.isDaily()
						? (activeDateTime.get(Calendar.DAY_OF_MONTH) - 1)
						: selectedMonthRow;
				if (targetRow >= 0 && targetRow < advTable.getRowCount()) {
					advTable.setRowSelectionInterval(targetRow, targetRow);
					final int scrollRow = targetRow;
					SwingUtilities.invokeLater(() -> {
						Rectangle rect = advTable.getCellRect(scrollRow, 0, true);
						advTable.scrollRectToVisible(rect);
					});
					if (ephMode.isDaily()) {
						updateD1Chart(targetRow);
					}
				}
			}
		} finally {
			isUpdatingTable = false;
		}
	}

	private void updateD1Chart(int rowIndex) {
		if (chartContainer == null || currentEphemeris == null || !ephMode.isDaily()) {
			return;
		}
		PlanetChartData chartData = currentEphemeris.getPlanetChartData(rowIndex);
		if (chartData == null) {
			return;
		}

		Calendar displayCal = (Calendar) activeDateTime.clone();
		if (ephMode.isDaily()) {
			displayCal.set(Calendar.DAY_OF_MONTH, rowIndex + 1);
		} else {
			displayCal.set(Calendar.MONTH, rowIndex);
			displayCal.set(Calendar.DAY_OF_MONTH, 1);
		}
		String dateStr = new SimpleDateFormat("MMM d, yyyy").format(displayCal.getTime());
		chartSubtitleLabel.setText(dateStr + " \u2022 " + AstroUtil.timeFormat(toCalcHours(displayCal)));

		UIConsts.ChartTheme theme = UIConsts.getChartTheme();
		Chart d1Chart = new Chart(chartData, D1_CHART_SIZE);
		d1Chart.setGridLineThickness(2.0f);
		d1Chart.setTitleBadge(theme.getPrimaryAccentColor(), theme.getBorderColor(), Color.WHITE);

		chartContainer.removeAll();
		chartContainer.add(d1Chart);
		chartContainer.revalidate();
		chartContainer.repaint();
	}

	/** Restores Advanced/Basic session state including the selected Monthly row. */
	public void restoreSessionState(UiMode mode, Calendar dateTime, Place place, Mode ephMode, int monthRow) {
		if (place != null) {
			this.sessionPlace = place;
		}
		if (dateTime != null) {
			this.activeDateTime = (Calendar) dateTime.clone();
			this.ephDate = (Calendar) dateTime.clone();
		}
		if (ephMode != null) {
			this.ephMode = ephMode;
		}
		this.selectedMonthRow = Math.max(0, Math.min(monthRow, 11));
		this.uiMode = (mode != null) ? mode : UiMode.Basic;
		if (this.uiMode == UiMode.Advanced) {
			ensureAdvancedCardCreated();
		}

		monthChooser.setSelectedDate(this.ephDate.getTime());
		((CalendarSpinner) monthChooser).setDateFormat(
				this.ephMode.isMonthly() ? CalendarSpinner.FMT_YEAR : CalendarSpinner.FMT_MONTH_YEAR);
		if (basicDailyRadio != null && basicMonthlyRadio != null) {
			basicDailyRadio.setSelected(this.ephMode.isDaily());
			basicMonthlyRadio.setSelected(this.ephMode.isMonthly());
		}

		updateUiModeButtons();
		bodyCardLayout.show(bodyCardPanel, this.uiMode.name());
		updateTableViewportSize();
		syncAdvancedControls();
		updateEphTable();
	}

	public UiMode getUiMode() {
		return uiMode;
	}

	public Calendar getActiveDateTime() {
		return (Calendar) activeDateTime.clone();
	}

	public Place getSessionPlace() {
		return sessionPlace;
	}

	public Mode getEphMode() {
		return ephMode;
	}

	public int getSelectedMonthRow() {
		return selectedMonthRow;
	}

	public AstrosoftTable getEphTable() {
		return (uiMode == UiMode.Advanced) ? advTable : ephTable;
	}

	public AstrosoftTable getAdvTable() {
		return advTable;
	}

	public JPanel getChartContainer() {
		return chartContainer;
	}

	public JPanel getChartCard() {
		return chartCard;
	}

	public ModernButton getHrFwdBtn() {
		return hrFwdBtn;
	}

	public ModernButton getMinFwdBtn() {
		return minFwdBtn;
	}
}
