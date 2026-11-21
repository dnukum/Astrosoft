/**
 * SingleDasaTimelineCellRenderer.java
 * Cell renderer for the hierarchical 3-level Dasa / Bhukti / Pratyantara tree-table.
 * Dynamically adapts to the active ChartTheme via UIConsts.
 */

package app.astrosoft.ui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import app.astrosoft.beans.SingleDasaTimelineRow;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;

public class SingleDasaTimelineCellRenderer extends DefaultTableCellRenderer {

	private static final Color SELECTED_BG = new Color(25, 95, 210);
	private static final Color SELECTED_FG = Color.WHITE;
	private static final Color SELECTED_BORDER = new Color(15, 65, 160);
	private static final Color CHIDRA_ACCENT = new Color(175, 95, 10);

	private final Font regularFont;
	private final Font boldFont;

	public SingleDasaTimelineCellRenderer() {
		this.regularFont = UIUtil.getFont("Verdana", Font.PLAIN, 11);
		this.boldFont = UIUtil.getFont("Verdana", Font.BOLD, 11);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		label.setFont(regularFont);
		label.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

		if (table.getModel() instanceof SingleDasaTimelineTableModel) {
			SingleDasaTimelineTableModel model = (SingleDasaTimelineTableModel) table.getModel();
			SingleDasaTimelineRow timelineRow = model.getRow(row);

			if (timelineRow != null) {
				int level = timelineRow.getLevel();
				boolean rowSelected = isSelected || table.isRowSelected(row);
				String text = (value != null) ? value.toString() : "";

				if (column == 0) {
					label.setText(formatHierarchyLabel(timelineRow, text, rowSelected));
				}

				if (rowSelected) {
					label.setOpaque(true);
					label.setBackground(SELECTED_BG);
					label.setForeground(SELECTED_FG);
					label.setFont(boldFont);
					label.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(1, 0, 1, 0, SELECTED_BORDER),
							BorderFactory.createEmptyBorder(2, 6, 2, 6)));
					return label;
				}

				Color chartBg = UIConsts.getChartBackground();
				Color borderCol = UIConsts.getChartBorderColor();
				Color planetFg = UIConsts.getChartPlanetForeground();
				Color labelFg = UIConsts.getChartLabelColor();
				Color ascFg = UIConsts.getChartAscendantColor();
				Color natalHighlight = UIConsts.getChartNatalHighlightColor();

				Color mdBg = UIUtil.blendColors(borderCol, chartBg, 0.22f);
				Color pdBg = UIUtil.blendColors(borderCol, chartBg, 0.06f);
				Color todayBg = UIUtil.blendColors(natalHighlight, chartBg, 0.55f);
				Color chidraBg = UIUtil.blendColors(CHIDRA_ACCENT, chartBg, 0.12f);

				if (level == SingleDasaTimelineRow.LEVEL_MD) {
					label.setBackground(mdBg);
					label.setFont(boldFont);
				} else if (level == SingleDasaTimelineRow.LEVEL_AD) {
					label.setBackground(timelineRow.isChidra() ? chidraBg : chartBg);
					label.setFont(timelineRow.isCurrent() ? boldFont : regularFont);
				} else {
					label.setBackground(timelineRow.isCurrent() ? todayBg : pdBg);
					label.setFont(timelineRow.isCurrent() ? boldFont : regularFont);
				}

				if (column == 0) {
					label.setForeground((level == SingleDasaTimelineRow.LEVEL_MD || timelineRow.isCurrent()) ? planetFg : labelFg);
				} else if (column == 1 || column == 2 || column == 3) {
					label.setForeground(planetFg);
				} else if (column == 4) {
					label.setForeground(labelFg);
				} else if (column == 5) {
					if (timelineRow.isMDChange()) {
						label.setForeground(ascFg);
						label.setFont(boldFont);
					} else if (timelineRow.isChidra()) {
						label.setForeground(CHIDRA_ACCENT);
						label.setFont(boldFont);
					} else if (timelineRow.isCurrent()) {
						label.setForeground(planetFg);
						label.setFont(boldFont);
					} else {
						label.setForeground(labelFg);
					}
				}
			}
		}

		return label;
	}

	private static String formatHierarchyLabel(SingleDasaTimelineRow row, String text, boolean selected) {
		int level = row.getLevel();
		if (level == SingleDasaTimelineRow.LEVEL_MD) {
			String chevron = row.isExpanded() ? "\u25BE " : "\u25B8 ";
			return chevron + text;
		} else if (level == SingleDasaTimelineRow.LEVEL_AD) {
			String chevron = row.isExpanded() ? "\u25BE " : "\u25B8 ";
			return "    " + chevron + text;
		} else {
			String marker = selected ? "\u25B6 " : (row.isCurrent() ? "\u25CF " : "");
			return "         " + marker + text;
		}
	}
}
