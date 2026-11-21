/**
 * SingleDasaTimelinePanel.java
 * Presentation panel for the single-horoscope expandable 3-level
 * (Dasa / Bhukti / Pratyantara) floating timeline window.
 */

package app.astrosoft.ui.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import app.astrosoft.beans.SingleDasaTimelineRow;
import app.astrosoft.core.DasaTimelineEngine.WindowFilter;
import app.astrosoft.core.DasaTimelineEngine.WindowType;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.SingleDasaTimelineEngine;
import app.astrosoft.ui.dlg.OptionDialog;
import app.astrosoft.ui.table.SingleDasaTimelineCellRenderer;
import app.astrosoft.ui.table.SingleDasaTimelineTableModel;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;

public class SingleDasaTimelinePanel extends JPanel {

	private final SingleDasaTimelineEngine engine;
	private final SingleDasaTimelineTableModel tableModel;
	private final JTable table;
	private final JScrollPane scrollPane;

	private JComboBox<String> windowCombo;
	private JButton jumpTodayBtn;
	private JLabel selectedValueLabel;

	private WindowFilter currentWindow = WindowFilter.NEXT_5_YEARS;
	private int customStartYear = -1;
	private int customEndYear = -1;

	public SingleDasaTimelinePanel(Horoscope horoscope) {
		super(new BorderLayout(0, 6));
		setOpaque(false);
		setPreferredSize(new Dimension(680, 390));
		setMinimumSize(new Dimension(520, 240));

		this.engine = new SingleDasaTimelineEngine(horoscope);
		this.tableModel = new SingleDasaTimelineTableModel();
		this.table = new JTable(tableModel);

		initTable();
		this.scrollPane = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(BorderFactory.createLineBorder(UIConsts.getChartBorderColor()));
		scrollPane.getViewport().setBackground(UIConsts.getChartBackground());

		add(createToolbar(), BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(createFooter(), BorderLayout.SOUTH);

		refreshData();
	}

	private void initTable() {
		Color chartBg = UIConsts.getChartBackground();
		Color borderCol = UIConsts.getChartBorderColor();
		table.setDefaultRenderer(Object.class, new SingleDasaTimelineCellRenderer());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.setRowHeight(23);
		table.setFont(UIUtil.getFont("Verdana", Font.PLAIN, 11));
		table.setBackground(chartBg);
		table.setGridColor(UIUtil.blendColors(borderCol, chartBg, 0.45f));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(new Color(25, 95, 210));
		table.setSelectionForeground(Color.WHITE);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setFont(UIUtil.getFont("Verdana", Font.BOLD, 11));
		table.getTableHeader().setBackground(UIUtil.blendColors(borderCol, chartBg, 0.22f));
		table.getTableHeader().setForeground(UIConsts.getChartLabelColor());
		table.getTableHeader().setOpaque(true);

		int[] widths = { 185, 90, 90, 58, 68, 185 };
		for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
			TableColumn col = table.getColumnModel().getColumn(i);
			col.setPreferredWidth(widths[i]);
		}

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					updateSelectedPill();
				}
			}
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());
				if (row < 0) {
					return;
				}
				SingleDasaTimelineRow node = tableModel.getRow(row);
				if (node != null && node.hasChildren() && (col == 0 || e.getClickCount() == 2)) {
					tableModel.toggleExpand(row);
					if (row < table.getRowCount()) {
						table.setRowSelectionInterval(row, row);
					}
					updateSelectedPill();
				}
			}
		});
	}

	private JPanel createToolbar() {
		JPanel toolbar = new JPanel(new BorderLayout());
		toolbar.setOpaque(false);

		JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
		leftControls.setOpaque(false);

		JLabel windowLbl = new JLabel("Window:");
		windowLbl.setFont(UIUtil.getFont("Verdana", Font.PLAIN, 11));
		windowLbl.setForeground(UIConsts.getChartPlanetForeground());
		leftControls.add(windowLbl);

		windowCombo = new JComboBox<String>(new String[] {
				"Next 5 Years (Default)",
				"Full 120 Years",
				"Custom Range..."
		});
		windowCombo.setFont(UIUtil.getFont("Verdana", Font.PLAIN, 11));
		windowCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int idx = windowCombo.getSelectedIndex();
				if (idx == 0) {
					currentWindow = WindowFilter.NEXT_5_YEARS;
					refreshData();
				} else if (idx == 1) {
					currentWindow = WindowFilter.FULL_120_YEARS;
					refreshData();
				} else if (idx == 2) {
					promptCustomRangeDialog();
				}
			}
		});
		leftControls.add(windowCombo);

		leftControls.add(Box.createHorizontalStrut(6));

		jumpTodayBtn = new JButton("Current Period");
		jumpTodayBtn.setFont(UIUtil.getFont("Verdana", Font.BOLD, 10));
		jumpTodayBtn.setFocusable(false);
		jumpTodayBtn.setToolTipText("Expand and scroll to current running Dasa / Bhukti / Pratyantara period");
		jumpTodayBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jumpToCurrentPeriod();
			}
		});
		leftControls.add(jumpTodayBtn);

		String runningText = formatNodePath(engine.getCurrentRunningNode());
		if (!runningText.isEmpty()) {
			leftControls.add(Box.createHorizontalStrut(4));
			leftControls.add(createPillBadge("\u25CF Now:", runningText, null));
		}

		JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
		rightControls.setOpaque(false);

		JButton expandAllBtn = new JButton("Expand All");
		expandAllBtn.setFont(UIUtil.getFont("Verdana", Font.PLAIN, 10));
		expandAllBtn.setFocusable(false);
		expandAllBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tableModel.expandAll(true);
				updateSelectedPill();
			}
		});

		JButton collapseAllBtn = new JButton("Collapse All");
		collapseAllBtn.setFont(UIUtil.getFont("Verdana", Font.PLAIN, 10));
		collapseAllBtn.setFocusable(false);
		collapseAllBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tableModel.expandAll(false);
				updateSelectedPill();
			}
		});

		rightControls.add(expandAllBtn);
		rightControls.add(collapseAllBtn);

		toolbar.add(leftControls, BorderLayout.WEST);
		toolbar.add(rightControls, BorderLayout.EAST);

		return toolbar;
	}

	private String formatNodePath(SingleDasaTimelineRow node) {
		if (node == null) {
			return "";
		}
		if (node.getPd() != null) {
			return node.getMd() + " / " + node.getAd() + " / " + node.getPd();
		}
		if (node.getAd() != null) {
			return node.getMd() + " / " + node.getAd();
		}
		return node.getMd().toString();
	}

	private void promptCustomRangeDialog() {
		final int minYear = engine.getMinBirthYear();
		final int maxYear = engine.getMax120Year();

		int curYear = Calendar.getInstance().get(Calendar.YEAR);
		int defaultStart = (customStartYear > 0) ? customStartYear : Math.max(minYear, curYear);
		int defaultEnd = (customEndYear > 0) ? customEndYear : Math.min(maxYear, defaultStart + 10);

		JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
		panel.add(new JLabel("Start Year:"));

		final JComboBox<Integer> startCombo = new JComboBox<Integer>();
		for (int y = minYear; y <= maxYear; y++) {
			startCombo.addItem(y);
		}
		startCombo.setSelectedItem(defaultStart);
		panel.add(startCombo);

		panel.add(new JLabel("End Year:"));
		final JComboBox<Integer> endCombo = new JComboBox<Integer>();
		for (int y = defaultStart; y <= maxYear; y++) {
			endCombo.addItem(y);
		}
		endCombo.setSelectedItem(defaultEnd);
		panel.add(endCombo);

		startCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (startCombo.getSelectedItem() == null) return;
				int s = (Integer) startCombo.getSelectedItem();
				int curEnd = (endCombo.getSelectedItem() != null) ? (Integer) endCombo.getSelectedItem() : s;
				endCombo.removeAllItems();
				for (int y = s; y <= maxYear; y++) {
					endCombo.addItem(y);
				}
				endCombo.setSelectedItem(Math.max(s, curEnd));
			}
		});

		int res = OptionDialog.showConfirmDialog(this, panel,
				"Select Custom Year Range (" + minYear + " - " + maxYear + ")",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (res == JOptionPane.OK_OPTION && startCombo.getSelectedItem() != null && endCombo.getSelectedItem() != null) {
			customStartYear = (Integer) startCombo.getSelectedItem();
			customEndYear = (Integer) endCombo.getSelectedItem();
			currentWindow = WindowFilter.custom(customStartYear, customEndYear);

			windowCombo.removeItemAt(2);
			windowCombo.addItem("Custom (" + customStartYear + " - " + customEndYear + ")...");
			windowCombo.setSelectedIndex(2);
			refreshData();
		} else {
			if (currentWindow.getType() == WindowType.NEXT_5_YEARS) {
				windowCombo.setSelectedIndex(0);
			} else if (currentWindow.getType() == WindowType.FULL_120_YEARS) {
				windowCombo.setSelectedIndex(1);
			}
		}
	}

	private JPanel createFooter() {
		JPanel footer = new JPanel(new BorderLayout());
		footer.setOpaque(false);
		footer.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 2));

		selectedValueLabel = new JLabel();
		footer.add(createPillBadge("Selected:", "\u2014", selectedValueLabel), BorderLayout.WEST);

		JLabel legend = new JLabel("\u25CF Current   \u26A1 MD Change   \u2022 Bhukti Change   \u25B2 Dasa Chidra");
		legend.setFont(UIUtil.getFont("Verdana", Font.PLAIN, 10));
		legend.setForeground(UIConsts.getChartLabelColor());
		footer.add(legend, BorderLayout.EAST);

		return footer;
	}

	private JPanel createPillBadge(String prefix, String initialValue, JLabel externalValueLabel) {
		final Color chartBg = UIConsts.getChartBackground();
		final Color borderCol = UIConsts.getChartBorderColor();
		final Color fillCol = UIUtil.blendColors(borderCol, chartBg, 0.14f);

		JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2)) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int w = getWidth();
				int h = getHeight();
				g2.setColor(fillCol);
				g2.fillRoundRect(0, 0, w - 1, h - 1, h - 1, h - 1);
				g2.setColor(borderCol);
				g2.drawRoundRect(0, 0, w - 1, h - 1, h - 1, h - 1);
				g2.dispose();
				super.paintComponent(g);
			}
		};
		pill.setOpaque(false);
		pill.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

		JLabel prefixLbl = new JLabel(prefix);
		prefixLbl.setFont(UIUtil.getFont("Verdana", Font.PLAIN, 10));
		prefixLbl.setForeground(UIConsts.getChartLabelColor());

		JLabel valLbl = (externalValueLabel != null) ? externalValueLabel : new JLabel();
		valLbl.setText(initialValue);
		valLbl.setFont(UIUtil.getFont("Verdana", Font.BOLD, 11));
		valLbl.setForeground(UIConsts.getChartPlanetForeground());

		pill.add(prefixLbl);
		pill.add(valLbl);
		return pill;
	}

	public void refreshData() {
		List<SingleDasaTimelineRow> roots = engine.getTimelineTree(currentWindow);
		tableModel.setRoots(roots);
		jumpToCurrentPeriod();
	}

	public void jumpToCurrentPeriod() {
		tableModel.expandCurrentPath();
		int currentIdx = tableModel.getCurrentLeafRowIndex();
		if (currentIdx >= 0 && currentIdx < table.getRowCount()) {
			table.setRowSelectionInterval(currentIdx, currentIdx);
			Rectangle rect = table.getCellRect(currentIdx, 0, true);
			table.scrollRectToVisible(rect);
		} else if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
			table.scrollRectToVisible(table.getCellRect(0, 0, true));
		}
		updateSelectedPill();
	}

	private void updateSelectedPill() {
		if (selectedValueLabel == null) {
			return;
		}
		int row = table.getSelectedRow();
		SingleDasaTimelineRow node = tableModel.getRow(row);
		String path = formatNodePath(node);
		selectedValueLabel.setText(path.isEmpty() ? "\u2014" : path);
	}

	public WindowFilter getCurrentWindow() {
		return currentWindow;
	}

	public void setWindowFilter(WindowFilter filter) {
		if (filter == null) {
			return;
		}
		this.currentWindow = filter;
		if (filter.getType() == WindowType.NEXT_5_YEARS) {
			windowCombo.setSelectedIndex(0);
		} else if (filter.getType() == WindowType.FULL_120_YEARS) {
			windowCombo.setSelectedIndex(1);
		} else if (filter.getType() == WindowType.CUSTOM_RANGE) {
			this.customStartYear = filter.getCustomStartYear();
			this.customEndYear = filter.getCustomEndYear();
			windowCombo.removeItemAt(2);
			windowCombo.addItem("Custom (" + customStartYear + " - " + customEndYear + ")...");
			windowCombo.setSelectedIndex(2);
			refreshData();
		}
	}
}
