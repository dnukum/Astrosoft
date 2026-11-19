/**
 * PanchangView.java
 * Created On 2006, May 8, 2006 6:27:10 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.DisplayFormat;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.TableStyle;
import app.astrosoft.consts.WeekDay;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.Panchang;
import app.astrosoft.pref.AstrosoftPref;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.comp.modern.DatePickerDialog;
import app.astrosoft.ui.comp.modern.ModernButton;
import app.astrosoft.ui.comp.modern.TimePickerDialog;
import app.astrosoft.ui.dlg.OptionDialog;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.DefaultColumnMetaData;
import app.astrosoft.ui.table.TableDataFactory;
import app.astrosoft.ui.table.TableRowData;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.util.AstroUtil;
import app.astrosoft.util.AstrosoftFileFilter;
import app.astrosoft.util.FileOps;

public class PanchangView extends AstrosoftView {

	private static final Dimension viewSize = new Dimension(990, 600);
	private static final Dimension chartSize = new Dimension(326, 316);

	private static final int LEFT_COL_WIDTH = 588;
	private static final int RIGHT_COL_WIDTH = 356;
	private static final int COLUMN_GAP = 14;
	private static final int TOTAL_DASHBOARD_WIDTH = LEFT_COL_WIDTH + COLUMN_GAP + RIGHT_COL_WIDTH;
	private static final int CARD_ARC = 12;
	private static final int ROW_HEIGHT = 28;
	private static final int MULTILINE_ROW_HEIGHT = 44;

	private static final DateTimeFormatter DATE_BTN_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);
	private static final DateTimeFormatter TIME_BTN_FMT = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);

	private JButton dateButton;
	private JButton timeButton;
	private JButton todayButton;
	private JButton loadChartButton;
	private JLabel contextLabel;

	private Panchang pan;
	private BirthData loadedBirthData;

	private PanchangaTableModel panchangaTableModel;
	private AstrosoftTable panchangaTable;

	private JLabel dateTimeTitleLabel;
	private JLabel dateTimeVal = new JLabel();
	private JLabel timeZoneVal = new JLabel();
	private JLabel placeVal = new JLabel();
	private JLabel weekdayVal = new JLabel();
	private JLabel moonRasiVal = new JLabel();

	private JLabel sunriseVal = new JLabel();
	private JLabel sunsetVal = new JLabel();
	private JLabel amirthathiVal = new JLabel();
	private JLabel brahmaVal = new JLabel();
	private JLabel abhijitVal = new JLabel();

	private JTextArea subhaHoraVal;
	private JLabel rahuKalaVal = new JLabel();
	private JLabel yamaKandaVal = new JLabel();

	private JPanel chartContainer;

	public PanchangView(Panchang pan, Point loc) {
		super(viewSize, loc);
		this.pan = pan;

		UIConsts.ChartTheme theme = UIConsts.getChartTheme();
		setLayout(new BorderLayout(0, 10));
		setBackground(UIConsts.CANVAS_BACKGROUND);
		setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

		add(createToolbarWrap(theme), BorderLayout.NORTH);
		add(createDashboardBody(theme), BorderLayout.CENTER);

		refreshView();
	}

	private JPanel createToolbarWrap(UIConsts.ChartTheme theme) {
		JPanel bar = createToolbar(theme);
		JPanel wrap = new JPanel(new BorderLayout());
		wrap.setOpaque(false);
		wrap.add(bar, BorderLayout.WEST);
		return wrap;
	}

	private JPanel createToolbar(UIConsts.ChartTheme theme) {
		JPanel bar = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				return new Dimension(Math.max(TOTAL_DASHBOARD_WIDTH, d.width), d.height);
			}
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
		bar.setOpaque(false);
		bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
		bar.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

		JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2)) {
			@Override
			public Dimension getMinimumSize() {
				Dimension d = getPreferredSize();
				return d;
			}
			@Override
			public Dimension getMaximumSize() {
				Dimension d = getPreferredSize();
				return new Dimension(Integer.MAX_VALUE, d.height);
			}
		};
		controls.setOpaque(false);
		controls.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel dateLbl = new JLabel(DisplayStrings.DATE_STR.toString() + ":");
		dateLbl.setFont(UIUtil.getFont(Font.BOLD, 12));
		dateLbl.setForeground(theme.getPlanetForeground());
		controls.add(dateLbl);

		dateButton = new ModernButton("", ModernButton.Style.SECONDARY, theme.getPrimaryAccentColor());
		dateButton.setPreferredSize(new Dimension(122, 30));
		dateButton.setMinimumSize(new Dimension(122, 30));
		dateButton.setToolTipText("Select Date");
		dateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openDatePicker();
			}
		});
		controls.add(dateButton);

		timeButton = new ModernButton("", ModernButton.Style.SECONDARY, theme.getPrimaryAccentColor());
		timeButton.setPreferredSize(new Dimension(102, 30));
		timeButton.setMinimumSize(new Dimension(102, 30));
		timeButton.setToolTipText("Select Time");
		timeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openTimePicker();
			}
		});
		controls.add(timeButton);

		todayButton = new ModernButton("Today", ModernButton.Style.SECONDARY, theme.getPrimaryAccentColor());
		todayButton.setPreferredSize(new Dimension(80, 30));
		todayButton.setMinimumSize(new Dimension(80, 30));
		todayButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switchToToday();
			}
		});
		controls.add(todayButton);

		loadChartButton = new ModernButton("Load Chart (.ash)...", ModernButton.Style.SECONDARY, theme.getPrimaryAccentColor());
		loadChartButton.setPreferredSize(new Dimension(172, 30));
		loadChartButton.setMinimumSize(new Dimension(172, 30));
		loadChartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadBirthChart();
			}
		});
		controls.add(loadChartButton);

		contextLabel = new JLabel(" ");
		contextLabel.setFont(UIUtil.getFont(Font.PLAIN, 11));
		contextLabel.setForeground(theme.getLabelColor());
		contextLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		contextLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

		bar.add(controls);
		bar.add(Box.createVerticalStrut(6));
		bar.add(contextLabel);

		return bar;
	}

	private JScrollPane createDashboardBody(UIConsts.ChartTheme theme) {
		JPanel leftCol = createFixedWidthColumn(LEFT_COL_WIDTH);
		leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));

		leftCol.add(createDateTimeCard(theme));
		leftCol.add(Box.createVerticalStrut(10));
		leftCol.add(createPanchangaCard(theme));
		leftCol.add(Box.createVerticalStrut(10));

		JPanel bottomRow = new JPanel(new java.awt.GridLayout(1, 2, 10, 0)) {
			@Override
			public Dimension getMaximumSize() {
				Dimension pref = getPreferredSize();
				return new Dimension(Integer.MAX_VALUE, pref.height);
			}
		};
		bottomRow.setOpaque(false);
		bottomRow.add(createSolarMuhurthaCard(theme));
		bottomRow.add(createTimingsCard(theme));
		leftCol.add(bottomRow);
		leftCol.add(Box.createVerticalGlue());

		JPanel chartPanel = createFixedWidthColumn(RIGHT_COL_WIDTH);
		chartPanel.setLayout(new BorderLayout());
		chartPanel.add(createChartCard(theme), BorderLayout.NORTH);

		JPanel columns = new JPanel(new GridBagLayout());
		columns.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;

		gbc.gridx = 0;
		gbc.insets = new Insets(0, 0, 0, COLUMN_GAP);
		columns.add(leftCol, gbc);

		gbc.gridx = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		columns.add(chartPanel, gbc);

		JPanel topAnchor = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		topAnchor.setOpaque(false);
		topAnchor.add(columns);

		JScrollPane scroll = new JScrollPane(topAnchor);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(UIConsts.CANVAS_BACKGROUND);
		scroll.setOpaque(false);
		return scroll;
	}

	private static JPanel createFixedWidthColumn(final int width) {
		JPanel col = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				return new Dimension(width, d.height);
			}
			@Override
			public Dimension getMinimumSize() {
				return getPreferredSize();
			}
			@Override
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}
		};
		col.setOpaque(false);
		return col;
	}

	private static JPanel createRoundedPanel(final Color bg, final Color borderClr, final int arc) {
		JPanel panel = new JPanel() {
			@Override
			public Dimension getMaximumSize() {
				Dimension pref = getPreferredSize();
				return new Dimension(Integer.MAX_VALUE, pref.height);
			}
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int w = getWidth();
				int h = getHeight();
				g2.setColor(bg);
				g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
				g2.setColor(borderClr);
				g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
				g2.dispose();
			}
		};
		panel.setOpaque(false);
		return panel;
	}

	private static JLabel createCardHeaderLabel(String title, UIConsts.ChartTheme theme) {
		JLabel titleLbl = new JLabel(title);
		titleLbl.setFont(UIUtil.getFont(Font.BOLD, 12));
		titleLbl.setForeground(theme.getPrimaryAccentColor());
		titleLbl.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, theme.getBorderColor()),
				BorderFactory.createEmptyBorder(0, 0, 4, 0)));
		return titleLbl;
	}

	private JPanel createCard(String title, UIConsts.ChartTheme theme) {
		return createCard(createCardHeaderLabel(title, theme), theme);
	}

	private JPanel createCard(JLabel titleLbl, UIConsts.ChartTheme theme) {
		JPanel card = createRoundedPanel(UIConsts.CARD_BACKGROUND, theme.getBorderColor(), CARD_ARC);
		card.setLayout(new BorderLayout(0, 6));
		card.setBorder(BorderFactory.createEmptyBorder(10, 14, 12, 14));
		card.add(titleLbl, BorderLayout.NORTH);
		return card;
	}

	private JPanel createKeyValueGrid(UIConsts.ChartTheme theme, String[] labels, JComponent[] values) {
		JPanel grid = new JPanel(new GridBagLayout());
		grid.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 0, 3, 8);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		Font keyFont = UIUtil.getFont(Font.BOLD, 11);
		Font valFont = UIUtil.getFont(Font.PLAIN, 12);

		for (int i = 0; i < labels.length; i++) {
			gbc.gridx = 0;
			gbc.gridy = i;
			gbc.weightx = 0.0;
			gbc.fill = GridBagConstraints.NONE;
			JLabel k = new JLabel(labels[i]);
			k.setFont(keyFont);
			k.setForeground(theme.getLabelColor());
			grid.add(k, gbc);

			gbc.gridx = 1;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			values[i].setFont(valFont);
			values[i].setForeground(theme.getPlanetForeground());
			grid.add(values[i], gbc);
		}
		return grid;
	}

	private JPanel createDateTimeCard(UIConsts.ChartTheme theme) {
		dateTimeTitleLabel = createCardHeaderLabel("Current Date/Time", theme);
		JPanel card = createCard(dateTimeTitleLabel, theme);
		String[] labels = {
			DisplayStrings.DATE_STR.toString(),
			"Time Zone",
			"Place",
			DisplayStrings.DAY_STR.toString(),
			DisplayStrings.RASI_STR.toString().trim()
		};
		JComponent[] values = { dateTimeVal, timeZoneVal, placeVal, weekdayVal, moonRasiVal };
		card.add(createKeyValueGrid(theme, labels, values), BorderLayout.CENTER);
		return card;
	}

	private JPanel createPanchangaCard(UIConsts.ChartTheme theme) {
		JPanel card = createCard("Panchanga", theme);

		panchangaTableModel = new PanchangaTableModel();
		final Border cellPad = BorderFactory.createEmptyBorder(2, 8, 2, 8);
		panchangaTable = new AstrosoftTable(panchangaTableModel, TableStyle.GRID) {
			private final TableCellRenderer multiLineRenderer = new PaddedTextAreaRenderer(theme, cellPad);
			private final DefaultTableCellRenderer rightRenderer = createRightRenderer(theme, cellPad);
			private final DefaultTableCellRenderer leftRenderer = createLeftRenderer(theme, cellPad);

			@Override
			public TableCellRenderer getCellRenderer(int row, int col) {
				if (col == 1 && row == 1) {
					return multiLineRenderer;
				}
				if (col == 2) {
					return rightRenderer;
				}
				if (col == 0 || col == 1 || col == 3) {
					return leftRenderer;
				}
				return super.getCellRenderer(row, col);
			}
		};

		panchangaTable.setBorder(null);
		panchangaTable.getTableHeader().setBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, AstrosoftTable.tableBorderClr));
		panchangaTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		panchangaTable.getColumnModel().getColumn(0).setPreferredWidth(90);
		panchangaTable.getColumnModel().getColumn(1).setPreferredWidth(252);
		panchangaTable.getColumnModel().getColumn(2).setPreferredWidth(74);
		panchangaTable.getColumnModel().getColumn(3).setPreferredWidth(144);
		applyPanchangaRowHeights();

		JPanel tableWrap = new JPanel(new BorderLayout());
		tableWrap.setOpaque(false);
		tableWrap.setBorder(BorderFactory.createLineBorder(AstrosoftTable.tableBorderClr, 1));
		tableWrap.add(panchangaTable.getTableHeader(), BorderLayout.NORTH);
		tableWrap.add(panchangaTable, BorderLayout.CENTER);

		card.add(tableWrap, BorderLayout.CENTER);
		return card;
	}

	private static DefaultTableCellRenderer createLeftRenderer(UIConsts.ChartTheme theme, final Border pad) {
		return new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				lbl.setForeground(theme.getPlanetForeground());
				lbl.setFont(UIUtil.getFont(Font.PLAIN, 12));
				lbl.setBorder(pad);
				return lbl;
			}
		};
	}

	private static DefaultTableCellRenderer createRightRenderer(UIConsts.ChartTheme theme, final Border pad) {
		return new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				lbl.setHorizontalAlignment(SwingConstants.RIGHT);
				lbl.setForeground(theme.getPlanetForeground());
				lbl.setFont(UIUtil.getFont(Font.PLAIN, 12));
				lbl.setBorder(pad);
				return lbl;
			}
		};
	}

	private static class PaddedTextAreaRenderer implements TableCellRenderer {
		private final JTextArea area;
		private final Color fg;

		public PaddedTextAreaRenderer(UIConsts.ChartTheme theme, Border pad) {
			this.fg = theme.getPlanetForeground();
			this.area = UIUtil.createWrappingTextArea(UIUtil.getFont(Font.PLAIN, 12), fg);
			this.area.setLineWrap(false);
			this.area.setWrapStyleWord(false);
			this.area.setOpaque(true);
			this.area.setBackground(Color.WHITE);
			this.area.setBorder(pad);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			area.setFont(UIUtil.getFont(Font.PLAIN, 12));
			area.setForeground(fg);
			area.setBackground(table != null ? table.getBackground() : Color.WHITE);
			area.setText(value != null ? value.toString() : "");
			return area;
		}
	}

	private JPanel createSolarMuhurthaCard(UIConsts.ChartTheme theme) {
		JPanel card = createCard("Solar & Muhurtha", theme);
		String[] labels = {
			DisplayStrings.SUNRISE_STR.toString(),
			DisplayStrings.SUNSET_STR.toString(),
			"Amirthathi Yoga",
			"Brahma Muhurta",
			"Abhijit Muhurta"
		};
		JComponent[] values = { sunriseVal, sunsetVal, amirthathiVal, brahmaVal, abhijitVal };
		card.add(createKeyValueGrid(theme, labels, values), BorderLayout.CENTER);
		return card;
	}

	private JPanel createChartCard(UIConsts.ChartTheme theme) {
		JPanel card = createCard(DisplayStrings.RASI_STR.toString().trim(), theme);
		chartContainer = new JPanel(new BorderLayout());
		chartContainer.setOpaque(false);
		chartContainer.add(new Chart(pan.getPlanetChartData(), chartSize), BorderLayout.CENTER);
		card.add(chartContainer, BorderLayout.CENTER);
		return card;
	}

	private JPanel createTimingsCard(UIConsts.ChartTheme theme) {
		JPanel card = createCard("Timings", theme);
		subhaHoraVal = UIUtil.createWrappingTextArea(UIUtil.getFont(Font.PLAIN, 12), theme.getPlanetForeground());
		subhaHoraVal.setOpaque(false);

		String[] labels = {
			"Subha Hora",
			DisplayStrings.RAHU_KALA_STR.toString(),
			DisplayStrings.YAMA_KANDA_STR.toString()
		};
		JComponent[] values = { subhaHoraVal, rahuKalaVal, yamaKandaVal };
		card.add(createKeyValueGrid(theme, labels, values), BorderLayout.CENTER);
		return card;
	}

	private void switchToToday() {
		loadedBirthData = null;
		AstrosoftPref prefs = AstroSoft.getPreferences();
		Date now = new Date();
		pan = new Panchang(now, prefs.getPlace(), true);
		refreshView();
	}

	private void loadBirthChart() {
		String file = FileOps.openFileDialog(
				this, FileOps.FileDialogMode.OPEN, AstrosoftFileFilter.HOROSCOPE_EXTN);
		if (file == null) {
			return;
		}
		Horoscope h = Horoscope.createFromFile(file);
		if (h == null || h.getBirthData() == null) {
			OptionDialog.showMessageDialog(this, "Unable to load horoscope (.ash) file.",
					"Load Chart", JOptionPane.ERROR_MESSAGE);
			return;
		}
		applyBirthChart(h.getBirthData());
	}

	/** Updates PanchangView to compute Panchang at the given birth chart's instant and place. */
	public void applyBirthChart(BirthData birthData) {
		if (birthData == null) {
			return;
		}
		loadedBirthData = birthData;
		Date birthInstant = birthData.birthDay().getTime();
		pan = new Panchang(birthInstant, birthData.getBirthPlace(), true);
		refreshView();
	}

	public Panchang getPanchang() {
		return pan;
	}

	public BirthData getLoadedBirthData() {
		return loadedBirthData;
	}

	/** Restores loaded chart badge after AstroSoft recreates PanchangView on option changes. */
	public void restoreLoadedBirthData(BirthData birthData) {
		this.loadedBirthData = birthData;
		refreshView();
	}

	/** Recalculates using Preferences place/time when not viewing an .ash birth chart. */
	public void reloadDefaultPlace() {
		if (loadedBirthData != null) {
			return;
		}
		Place prefPlace = AstroSoft.getPreferences().getPlace();
		if (pan.isExactInstant()) {
			pan = new Panchang(new Date(), prefPlace, true);
		} else {
			pan = new Panchang(pan.getDate().getTime(), prefPlace, false);
		}
		refreshView();
	}

	private LocalDate getCurrentPlaceDate() {
		Calendar c = pan.getDate();
		return LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
	}

	private LocalTime getCurrentPlaceTime() {
		double h = pan.getLocalTimeHours();
		int totalSecs = (int) Math.round((((h % 24.0) + 24.0) % 24.0) * 3600.0);
		totalSecs = Math.min(totalSecs, 86399);
		return LocalTime.ofSecondOfDay(totalSecs);
	}

	private void openDatePicker() {
		LocalDate picked = DatePickerDialog.showDatePicker(this, getCurrentPlaceDate());
		if (picked != null) {
			applyDateAndTime(picked, getCurrentPlaceTime());
		}
	}

	private void openTimePicker() {
		LocalTime picked = TimePickerDialog.showTimePicker(this, getCurrentPlaceTime());
		if (picked != null) {
			applyDateAndTime(getCurrentPlaceDate(), picked);
		}
	}

	/** Updates Panchang to the exact picked date and time at the active place. */
	public void applyDateAndTime(LocalDate date, LocalTime time) {
		if (date == null || time == null) {
			return;
		}
		Place activePlace = (loadedBirthData != null && loadedBirthData.getBirthPlace() != null)
				? loadedBirthData.getBirthPlace()
				: AstroSoft.getPreferences().getPlace();
		Calendar cal = Calendar.getInstance(activePlace.astrosoftTimeZone().getTimeZone());
		cal.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth(),
				time.getHour(), time.getMinute(), time.getSecond());
		cal.set(Calendar.MILLISECOND, 0);
		pan = new Panchang(cal.getTime(), activePlace, true);
		refreshView();
	}

	private void syncPickerButtons() {
		dateButton.setText(getCurrentPlaceDate().format(DATE_BTN_FMT));
		timeButton.setText(getCurrentPlaceTime().format(TIME_BTN_FMT));
	}

	private void refreshView() {
		syncPickerButtons();

		String dateStr = getCurrentPlaceDate().format(DATE_BTN_FMT);
		String timeStr = AstroUtil.timeFormat(pan.getLocalTimeHours());
		dateTimeVal.setText(dateStr + " · " + timeStr);

		double tz = pan.getTimeZoneOffset();
		int tzH = (int) tz;
		int tzM = (int) Math.round(Math.abs(tz - tzH) * 60.0);
		String tzSign = tz >= 0 ? "+" : "-";
		String tzStr = String.format(Locale.US, "%s%02d:%02d (%s GMT)",
				tzSign, Math.abs(tzH), tzM, tz >= 0 ? "East of" : "West of");
		timeZoneVal.setText(tzStr);

		Place place = pan.getPlace();
		String placeText = formatPlace(place);
		placeVal.setText(placeText);
		weekdayVal.setText(pan.getWeekday().toString());
		moonRasiVal.setText(pan.getRasi().toString());

		if (loadedBirthData != null) {
			dateTimeTitleLabel.setText("Chart Details — " + loadedBirthData.name());
			contextLabel.setText("Active: Chart — " + loadedBirthData.name() + "  |  "
					+ dateStr + " " + timeStr + "  |  " + placeText);
		} else if (pan.isExactInstant()) {
			dateTimeTitleLabel.setText("Current Date/Time");
			contextLabel.setText("Active: Today (Live)  |  " + placeText);
		} else {
			dateTimeTitleLabel.setText("Current Date/Time");
			contextLabel.setText("Active: Daily Panchang  |  " + placeText);
		}

		panchangaTableModel.updateFrom(pan);
		applyPanchangaRowHeights();

		sunriseVal.setText(AstroUtil.timeFormat(pan.getSunrise()));
		sunsetVal.setText(AstroUtil.timeFormat(pan.getSunset()));
		amirthathiVal.setText(pan.getAmirthathiYoga().toString());
		brahmaVal.setText(pan.getBrahmaMuhurta());
		abhijitVal.setText(pan.getAbhijitMuhurta());

		String[] aus = pan.auspiciousTime();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < aus.length; i++) {
			if (i > 0) sb.append("\n");
			sb.append(aus[i]);
		}
		subhaHoraVal.setText(sb.toString());
		rahuKalaVal.setText(pan.rahuKala());
		yamaKandaVal.setText(pan.yamaKanda());

		chartContainer.removeAll();
		chartContainer.add(new Chart(pan.getPlanetChartData(), chartSize), BorderLayout.CENTER);
		chartContainer.revalidate();
		chartContainer.repaint();
	}

	private void applyPanchangaRowHeights() {
		if (panchangaTable == null) {
			return;
		}
		panchangaTable.setRowHeight(ROW_HEIGHT);
		panchangaTable.setRowHeight(1, MULTILINE_ROW_HEIGHT); // Tithi (with Nitya Devi)
	}

	private static String formatPlace(Place place) {
		if (place == null) {
			return "-";
		}
		String city = place.city() != null ? place.city() : "";
		String country = place.country() != null ? place.country() : "";
		String loc = city;
		if (!country.isEmpty()) {
			loc = loc.isEmpty() ? country : loc + ", " + country;
		}
		return loc.isEmpty() ? place.toString() : loc;
	}

	private static String formatPct(double pct) {
		return String.format(Locale.US, "%.2f%%", pct);
	}

	private static String formatEndTime(Panchang pan, double endHours) {
		int dayOffset = (int) Math.floor(endHours / 24.0);
		double wrapped = endHours - 24.0 * dayOffset;
		if (wrapped < 0) {
			wrapped += 24.0;
			dayOffset--;
		}
		String timeStr = AstroUtil.timeFormat(wrapped);
		if (dayOffset <= 0) {
			return timeStr;
		}
		WeekDay endDay = WeekDay.ofIndex(pan.getWeekday().ordinal() + dayOffset);
		String dayLabel = (AstroSoft.getPreferences().getLanguage() == Language.ENGLISH)
				? endDay.sym()
				: endDay.toString();
		return timeStr + " (" + dayLabel + ")";
	}

	private static class PanchangaTableModel extends AstrosoftTableModel {
		private static final String[] COLUMNS = { "Limb", "Value", "% Left", "End" };
		private final Object[][] rows = new Object[5][4];

		public PanchangaTableModel() {
			super(TableDataFactory.getTableData(Collections.<TableRowData>emptyList()),
					new DefaultColumnMetaData(
							AstrosoftTableColumn.Key,
							AstrosoftTableColumn.Value,
							AstrosoftTableColumn.Start,
							AstrosoftTableColumn.End));
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return String.class;
		}

		public void updateFrom(Panchang pan) {
			// 1. Vara (Solar Day)
			rows[0][0] = "Vara (Solar)";
			rows[0][1] = pan.getVedicVara() + " (" + pan.getVedicVara().getLord().toString(DisplayFormat.SYMBOL) + ")";
			rows[0][2] = "—";
			rows[0][3] = "Sunrise–Sunrise";

			// 2. Tithi
			rows[1][0] = DisplayStrings.THITHI_STR.toString();
			rows[1][1] = pan.getPaksha() + " " + pan.getThithiEnum()
					+ " (" + pan.getThithiEnum().getLord().toString(DisplayFormat.SYMBOL) + ")\n["
					+ pan.getThithiEnum().getNityaDevi() + "]";
			rows[1][2] = formatPct(pan.getThithiPercentLeft());
			rows[1][3] = formatEndTime(pan, pan.getThithiEnd());

			// 3. Nakshatra
			rows[2][0] = DisplayStrings.NAK_STR.toString();
			rows[2][1] = pan.getNakshathra() + " (" + pan.getNakshathra().getLord().toString(DisplayFormat.SYMBOL) + ")";
			rows[2][2] = formatPct(pan.getNakshathraPercentLeft());
			rows[2][3] = formatEndTime(pan, pan.getNakshathraEnd());

			// 4. Yoga
			rows[3][0] = DisplayStrings.YOGA_STR.toString();
			rows[3][1] = pan.getYogaEnum() + " (" + pan.getYogaEnum().getLord().toString(DisplayFormat.SYMBOL) + ")";
			rows[3][2] = formatPct(pan.getYogaPercentLeft());
			rows[3][3] = formatEndTime(pan, pan.getYogaEnd());

			// 5. Karana (active first half-Tithi Karana)
			rows[4][0] = DisplayStrings.KARANA_STR.toString();
			rows[4][1] = pan.getFirstKarana() + " (" + pan.getFirstKarana().getLord().toString(DisplayFormat.SYMBOL) + ")";
			rows[4][2] = formatPct(pan.getKaranaPercentLeft());
			rows[4][3] = formatEndTime(pan, pan.getFirstKaranaEnd());

			fireTableRowsUpdated(0, rows.length - 1);
		}

		@Override
		public int getRowCount() {
			return rows.length;
		}

		@Override
		public int getColumnCount() {
			return COLUMNS.length;
		}

		@Override
		public String getColumnName(int col) {
			return COLUMNS[col];
		}

		@Override
		public Object getValueAt(int row, int col) {
			return rows[row][col];
		}
	}
}
