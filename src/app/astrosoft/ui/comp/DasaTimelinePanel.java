/**
 * DasaTimelinePanel.java
 * Reusable Swing presentation panel for the Superimposed Dasa & Bhukti Timeline.
 * Features:
 * - Scope filter (Overlapping & Chidra [Default], All Milestones, Major Dasa Only)
 * - Age range filter (From Age 20+ [Default], From Birth)
 * - Jump to Today button
 * - Scrollable JTable with resizable columns and custom cell rendering.
 */

package app.astrosoft.ui.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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
import javax.swing.table.TableColumn;

import app.astrosoft.beans.DasaTimelineRow;
import app.astrosoft.core.DasaTimelineEngine;
import app.astrosoft.core.DasaTimelineEngine.ScopeFilter;
import app.astrosoft.core.DasaTimelineEngine.WindowFilter;
import app.astrosoft.core.DasaTimelineEngine.WindowType;
import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.table.DasaTimelineCellRenderer;
import app.astrosoft.ui.table.DasaTimelineTableModel;

public class DasaTimelinePanel extends JPanel {

	private final DasaTimelineEngine engine;
	private final DasaTimelineTableModel tableModel;
	private final JTable table;
	private final JScrollPane scrollPane;

	private JComboBox<String> scopeCombo;
	private JComboBox<String> windowCombo;
	private JButton jumpTodayBtn;
	private JLabel statusLabel;

	private ScopeFilter currentScope = ScopeFilter.OVERLAPPING_AND_CHIDRA;
	private WindowFilter currentWindow = WindowFilter.NEXT_5_YEARS;
	private int customStartYear = -1;
	private int customEndYear = -1;

	public interface ResizeListener {
		void onResize(int newWidth, int newHeight);
	}

	private ResizeListener resizeListener;

	// Drag resize state
	private int dragStartY;
	private int initialHeight;
	private int dragStartX;
	private int initialWidth;

	public DasaTimelinePanel(Horoscope boyHoroscope, Horoscope girlHoroscope) {
		super(new BorderLayout());
		setOpaque(false);
		setPreferredSize(new Dimension(660, 360));
		setMinimumSize(new Dimension(500, 220));

		this.engine = new DasaTimelineEngine(boyHoroscope, girlHoroscope);
		this.tableModel = new DasaTimelineTableModel();
		this.table = new JTable(tableModel);

		initTable();
		this.scrollPane = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 225)));
		scrollPane.getViewport().setBackground(Color.WHITE);

		JPanel centerWithVHandle = new JPanel(new BorderLayout());
		centerWithVHandle.setOpaque(false);
		centerWithVHandle.add(scrollPane, BorderLayout.CENTER);
		centerWithVHandle.add(createVerticalHandleBar(), BorderLayout.EAST);

		add(createToolbar(), BorderLayout.NORTH);
		add(centerWithVHandle, BorderLayout.CENTER);

		JPanel southContainer = new JPanel(new BorderLayout());
		southContainer.setOpaque(false);
		southContainer.add(createHorizontalHandleBar(), BorderLayout.NORTH);
		southContainer.add(createFooter(), BorderLayout.SOUTH);
		add(southContainer, BorderLayout.SOUTH);

		refreshData();
	}

	public void setResizeListener(ResizeListener listener) {
		this.resizeListener = listener;
	}

	private JPanel createVerticalHandleBar() {
		final JPanel vHandle = new JPanel();
		vHandle.setPreferredSize(new Dimension(8, 0));
		vHandle.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		vHandle.setBackground(new Color(235, 238, 245));
		vHandle.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(205, 210, 220)));
		vHandle.setToolTipText("Drag horizontally to stretch table width");

		vHandle.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				dragStartX = e.getXOnScreen();
				initialWidth = getWidth();
			}
		});

		vHandle.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				int delta = e.getXOnScreen() - dragStartX;
				int newWidth = Math.max(660, initialWidth + delta);
				if (resizeListener != null) {
					resizeListener.onResize(newWidth, -1);
				} else {
					setPreferredSize(new Dimension(newWidth, getPreferredSize().height));
					revalidate();
					if (getParent() != null) {
						getParent().revalidate();
						getParent().repaint();
					}
				}
			}
		});

		return vHandle;
	}

	private JPanel createHorizontalHandleBar() {
		final JPanel hHandle = new JPanel();
		hHandle.setPreferredSize(new Dimension(0, 8));
		hHandle.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
		hHandle.setBackground(new Color(235, 238, 245));
		hHandle.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(205, 210, 220)));
		hHandle.setToolTipText("Drag vertically to stretch table height");

		hHandle.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				dragStartY = e.getYOnScreen();
				initialHeight = getHeight();
			}
		});

		hHandle.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				int delta = e.getYOnScreen() - dragStartY;
				int newHeight = Math.max(220, initialHeight + delta);
				if (resizeListener != null) {
					resizeListener.onResize(-1, newHeight);
				} else {
					setPreferredSize(new Dimension(getPreferredSize().width, newHeight));
					revalidate();
					if (getParent() != null) {
						getParent().revalidate();
						getParent().repaint();
					}
				}
			}
		});

		return hHandle;
	}

	private void initTable() {
		table.setDefaultRenderer(Object.class, new DasaTimelineCellRenderer());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.setRowHeight(22);
		table.setFont(new Font("Verdana", Font.PLAIN, 11));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(new Color(25, 95, 210));
		table.setSelectionForeground(Color.WHITE);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 11));

		// Set default column widths fitting neatly within standard 640px card
		int[] widths = { 88, 65, 102, 65, 102, 218 };
		for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
			TableColumn col = table.getColumnModel().getColumn(i);
			col.setPreferredWidth(widths[i]);
		}
	}

	private JPanel createToolbar() {
		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
		toolbar.setOpaque(false);

		JLabel scopeLbl = new JLabel("Show:");
		scopeLbl.setFont(new Font("Verdana", Font.PLAIN, 11));
		toolbar.add(scopeLbl);

		scopeCombo = new JComboBox<String>(new String[] {
				"Dasa Overlaps & Dasa Chidra",
				"All Milestones (MD + Bukti)",
				"Major Dasa Only"
		});
		scopeCombo.setFont(new Font("Verdana", Font.PLAIN, 11));
		scopeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int idx = scopeCombo.getSelectedIndex();
				if (idx == 0) {
					currentScope = ScopeFilter.OVERLAPPING_AND_CHIDRA;
				} else if (idx == 1) {
					currentScope = ScopeFilter.ALL_MILESTONES;
				} else if (idx == 2) {
					currentScope = ScopeFilter.MAJOR_DASA_ONLY;
				}
				refreshData();
			}
		});
		toolbar.add(scopeCombo);

		toolbar.add(Box.createHorizontalStrut(6));

		JLabel windowLbl = new JLabel("Window:");
		windowLbl.setFont(new Font("Verdana", Font.PLAIN, 11));
		toolbar.add(windowLbl);

		windowCombo = new JComboBox<String>(new String[] {
				"Next 5 Years (Default)",
				"Full 120 Years",
				"Custom Range..."
		});
		windowCombo.setFont(new Font("Verdana", Font.PLAIN, 11));
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
		toolbar.add(windowCombo);

		toolbar.add(Box.createHorizontalStrut(6));

		jumpTodayBtn = new JButton("Current Period");
		jumpTodayBtn.setFont(new Font("Verdana", Font.BOLD, 10));
		jumpTodayBtn.setFocusable(false);
		jumpTodayBtn.setToolTipText("Scroll to current running Dasa & Bhukti period");
		jumpTodayBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jumpToToday();
			}
		});
		toolbar.add(jumpTodayBtn);

		return toolbar;
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

		int res = JOptionPane.showConfirmDialog(this, panel, "Select Custom Year Range (" + minYear + " - " + maxYear + ")",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (res == JOptionPane.OK_OPTION && startCombo.getSelectedItem() != null && endCombo.getSelectedItem() != null) {
			customStartYear = (Integer) startCombo.getSelectedItem();
			customEndYear = (Integer) endCombo.getSelectedItem();
			currentWindow = WindowFilter.custom(customStartYear, customEndYear);

			// Update 3rd item text
			windowCombo.removeItemAt(2);
			windowCombo.addItem("Custom (" + customStartYear + " - " + customEndYear + ")...");
			windowCombo.setSelectedIndex(2);
			refreshData();
		} else {
			// Revert back to previous selection if cancelled
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
		footer.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));

		statusLabel = new JLabel("0 intervals");
		statusLabel.setFont(new Font("Verdana", Font.PLAIN, 10));
		statusLabel.setForeground(Color.DARK_GRAY);
		footer.add(statusLabel, BorderLayout.WEST);

		JLabel legend = new JLabel("🟣 Same Dasa  ⚡ MD Change  ⚠️ Dasa Chidra");
		legend.setFont(new Font("Verdana", Font.PLAIN, 10));
		legend.setForeground(Color.GRAY);
		footer.add(legend, BorderLayout.EAST);

		return footer;
	}

	public void refreshData() {
		List<DasaTimelineRow> rows = engine.getTimelineRows(currentScope, currentWindow);
		tableModel.setRows(rows);
		statusLabel.setText(rows.size() + " intervals");

		// Auto-scroll to today / current period
		jumpToToday();
	}

	public void jumpToToday() {
		jumpToCurrentPeriod();
	}

	public void jumpToCurrentPeriod() {
		int todayIdx = tableModel.getTodayRowIndex();
		if (todayIdx >= 0 && todayIdx < table.getRowCount()) {
			table.setRowSelectionInterval(todayIdx, todayIdx);
			Rectangle rect = table.getCellRect(todayIdx, 0, true);
			table.scrollRectToVisible(rect);
		} else if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
			table.scrollRectToVisible(table.getCellRect(0, 0, true));
		}
	}

	public JTable getTable() {
		return table;
	}

	public JButton getCurrentPeriodButton() {
		return jumpTodayBtn;
	}

	public JComboBox<String> getWindowCombo() {
		return windowCombo;
	}

	public WindowFilter getCurrentWindow() {
		return currentWindow;
	}

	public DasaTimelineTableModel getTableModel() {
		return tableModel;
	}

	public String getStatusText() {
		return statusLabel != null ? statusLabel.getText() : "";
	}
}
