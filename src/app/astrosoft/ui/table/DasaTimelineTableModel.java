/**
 * DasaTimelineTableModel.java
 * Table model for displaying synchronized Dasa & Bhukti milestones.
 */

package app.astrosoft.ui.table;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import app.astrosoft.beans.DasaTimelineRow;

public class DasaTimelineTableModel extends AbstractTableModel {

	private static final String[] COLUMN_NAMES = {
			"Start Date",
			"Boy Age",
			"Boy Dasa / Buk",
			"Girl Age",
			"Girl Dasa / Buk",
			"Trigger Event & Status"
	};

	private List<DasaTimelineRow> rows = new ArrayList<DasaTimelineRow>();

	public DasaTimelineTableModel() {
	}

	public DasaTimelineTableModel(List<DasaTimelineRow> rows) {
		setRows(rows);
	}

	public void setRows(List<DasaTimelineRow> rows) {
		this.rows = (rows != null) ? rows : new ArrayList<DasaTimelineRow>();
		fireTableDataChanged();
	}

	public List<DasaTimelineRow> getRows() {
		return rows;
	}

	public DasaTimelineRow getRow(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < rows.size()) {
			return rows.get(rowIndex);
		}
		return null;
	}

	public int getTodayRowIndex() {
		for (int i = 0; i < rows.size(); i++) {
			if (rows.get(i).isToday()) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public String getColumnName(int column) {
		if (column >= 0 && column < COLUMN_NAMES.length) {
			return COLUMN_NAMES[column];
		}
		return "";
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		DasaTimelineRow row = getRow(rowIndex);
		if (row == null) {
			return null;
		}

		switch (columnIndex) {
			case 0:
				return row.getStartDateStr();
			case 1:
				return row.getBoyAgeStr();
			case 2:
				return row.getBoyMD() + " / " + row.getBoyAD();
			case 3:
				return row.getGirlAgeStr();
			case 4:
				return row.getGirlMD() + " / " + row.getGirlAD();
			case 5:
				return row.getTriggerEvent();
			default:
				return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
}
