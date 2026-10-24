/**
 * DasaTimelineCellRenderer.java
 * Specialized cell renderer for the Dasa & Bhukti Synastry Timeline table.
 * Applies visual styling:
 * - Highlighted background for Today
 * - Subtle purple tint for Sama Dasa
 * - Warm tint for Major Dasa shifts & Dasa Chidra
 * - Boy text accents (navy) & Girl text accents (rose)
 */

package app.astrosoft.ui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import app.astrosoft.beans.DasaTimelineRow;

public class DasaTimelineCellRenderer extends DefaultTableCellRenderer {

	// Themes & Palette
	private static final Color TODAY_BG = new Color(225, 238, 255);
	private static final Color SAMA_DASA_BG = new Color(248, 240, 255);
	private static final Color CHIDRA_OR_MD_BG = new Color(255, 249, 238);
	private static final Color DEFAULT_EVEN_BG = Color.WHITE;
	private static final Color DEFAULT_ODD_BG = new Color(250, 250, 252);

	// High-contrast Selected Row Styling
	private static final Color SELECTED_BG = new Color(25, 95, 210); // Vibrant Royal Blue
	private static final Color SELECTED_FG = Color.WHITE;
	private static final Color SELECTED_BORDER = new Color(15, 65, 160);

	private static final Color BOY_FG = new Color(25, 65, 150);
	private static final Color GIRL_FG = new Color(160, 35, 95);
	private static final Color SAMA_DASA_FG = new Color(110, 30, 160);
	private static final Color CHIDRA_FG = new Color(175, 95, 10);

	private final Font regularFont;
	private final Font boldFont;

	public DasaTimelineCellRenderer() {
		this.regularFont = new Font("Verdana", Font.PLAIN, 11);
		this.boldFont = new Font("Verdana", Font.BOLD, 11);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
												   boolean isSelected, boolean hasFocus,
												   int row, int column) {
		JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		label.setFont(regularFont);
		label.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));

		if (table.getModel() instanceof DasaTimelineTableModel) {
			DasaTimelineTableModel model = (DasaTimelineTableModel) table.getModel();
			DasaTimelineRow timelineRow = model.getRow(row);

			if (timelineRow != null) {
				boolean rowSelected = isSelected || table.isRowSelected(row);

				// 1. High-Contrast Selection (Must standout immediately whether table has focus or not)
				if (rowSelected) {
					label.setOpaque(true);
					label.setBackground(SELECTED_BG);
					label.setForeground(SELECTED_FG);
					label.setFont(boldFont);
					label.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(1, 0, 1, 0, SELECTED_BORDER),
							BorderFactory.createEmptyBorder(2, 6, 2, 6)
					));
					if (column == 0) {
						label.setText("▶ " + value);
					}
					return label;
				}

				// 2. Unselected Background Coloring
				if (timelineRow.isToday()) {
					label.setBackground(TODAY_BG);
					label.setFont(boldFont);
				} else if (timelineRow.isSamaDasa()) {
					label.setBackground(SAMA_DASA_BG);
				} else if (timelineRow.isBoyMDChange() || timelineRow.isGirlMDChange() || timelineRow.isChidra()) {
					label.setBackground(CHIDRA_OR_MD_BG);
				} else {
					label.setBackground((row % 2 == 0) ? DEFAULT_EVEN_BG : DEFAULT_ODD_BG);
				}

				// 3. Foreground Styling per Column
				if (column == 0) { // Start Date
					label.setForeground(Color.DARK_GRAY);
					if (timelineRow.isToday()) {
						label.setText("● " + value);
						label.setForeground(BOY_FG);
					}
				} else if (column == 1 || column == 2) { // Boy Age & Dasa/Buk
					label.setForeground(BOY_FG);
					if (timelineRow.isBoyMDChange()) {
						label.setFont(boldFont);
					}
				} else if (column == 3 || column == 4) { // Girl Age & Dasa/Buk
					label.setForeground(GIRL_FG);
					if (timelineRow.isGirlMDChange()) {
						label.setFont(boldFont);
					}
				} else if (column == 5) { // Trigger Event
					if (timelineRow.isSamaDasa()) {
						label.setForeground(SAMA_DASA_FG);
						label.setFont(boldFont);
					} else if (timelineRow.isChidra()) {
						label.setForeground(CHIDRA_FG);
						label.setFont(boldFont);
					} else if (timelineRow.isBoyMDChange() || timelineRow.isGirlMDChange()) {
						label.setForeground(new Color(180, 50, 0));
						label.setFont(boldFont);
					} else {
						label.setForeground(Color.GRAY);
					}
				}
			}
		}

		return label;
	}
}
