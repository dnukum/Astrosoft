/**
 * SingleDasaTimelineTableModel.java
 * Hierarchical tree-table model for displaying 3-level (Dasa / Bhukti / Pratyantara)
 * expandable intervals for a single loaded horoscope.
 */

package app.astrosoft.ui.table;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

import app.astrosoft.beans.SingleDasaTimelineRow;
import app.astrosoft.consts.Language;
import app.astrosoft.ui.AstroSoft;

public class SingleDasaTimelineTableModel extends AbstractTableModel {

	private static final String[] EN_COLUMNS = {
			"Dasa / Bhukti / Pratyantara",
			"Start Date",
			"End Date",
			"Age",
			"Duration",
			"Status"
	};

	private static final String[] TA_COLUMNS = {
			"தசை / புத்தி / அந்தரம்",
			"தொடக்க தேதி",
			"முடிவு தேதி",
			"வயது",
			"கால அளவு",
			"நிலை"
	};

	private List<SingleDasaTimelineRow> roots = new ArrayList<SingleDasaTimelineRow>();
	private final List<SingleDasaTimelineRow> visibleRows = new ArrayList<SingleDasaTimelineRow>();

	public void setRoots(List<SingleDasaTimelineRow> roots) {
		this.roots = (roots != null) ? roots : new ArrayList<SingleDasaTimelineRow>();
		rebuildVisibleRows();
	}

	public void toggleExpand(int rowIndex) {
		SingleDasaTimelineRow row = getRow(rowIndex);
		if (row != null && row.hasChildren()) {
			row.setExpanded(!row.isExpanded());
			rebuildVisibleRows();
		}
	}

	public void expandAll(boolean expanded) {
		for (SingleDasaTimelineRow md : roots) {
			md.setExpanded(expanded);
			for (SingleDasaTimelineRow ad : md.getChildren()) {
				ad.setExpanded(expanded);
			}
		}
		rebuildVisibleRows();
	}

	public void expandCurrentPath() {
		for (SingleDasaTimelineRow md : roots) {
			md.setExpanded(md.isCurrent());
			for (SingleDasaTimelineRow ad : md.getChildren()) {
				ad.setExpanded(md.isCurrent() && ad.isCurrent());
			}
		}
		rebuildVisibleRows();
	}

	private void rebuildVisibleRows() {
		visibleRows.clear();
		for (SingleDasaTimelineRow md : roots) {
			visibleRows.add(md);
			if (md.isExpanded()) {
				for (SingleDasaTimelineRow ad : md.getChildren()) {
					visibleRows.add(ad);
					if (ad.isExpanded()) {
						for (SingleDasaTimelineRow pd : ad.getChildren()) {
							visibleRows.add(pd);
						}
					}
				}
			}
		}
		fireTableDataChanged();
	}

	public SingleDasaTimelineRow getRow(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < visibleRows.size()) {
			return visibleRows.get(rowIndex);
		}
		return null;
	}

	public int getCurrentLeafRowIndex() {
		int deepestIdx = -1;
		int deepestLevel = -1;
		for (int i = 0; i < visibleRows.size(); i++) {
			SingleDasaTimelineRow r = visibleRows.get(i);
			if (r.isCurrent() && r.getLevel() > deepestLevel) {
				deepestLevel = r.getLevel();
				deepestIdx = i;
			}
		}
		return deepestIdx;
	}

	@Override
	public int getRowCount() {
		return visibleRows.size();
	}

	@Override
	public int getColumnCount() {
		return EN_COLUMNS.length;
	}

	@Override
	public String getColumnName(int column) {
		String[] cols = (AstroSoft.getPreferences().getLanguage() == Language.TAMIL) ? TA_COLUMNS : EN_COLUMNS;
		if (column >= 0 && column < cols.length) {
			return cols[column];
		}
		return "";
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		SingleDasaTimelineRow row = getRow(rowIndex);
		if (row == null) {
			return null;
		}

		switch (columnIndex) {
			case 0:
				if (row.getLevel() == SingleDasaTimelineRow.LEVEL_MD) {
					boolean isTamil = AstroSoft.getPreferences().getLanguage() == Language.TAMIL;
					return row.getDisplayLabel() + (isTamil ? " தசை" : " Dasa");
				}
				return row.getDisplayLabel();
			case 1:
				return row.getStartDateStr();
			case 2:
				return row.getEndDateStr();
			case 3:
				return row.getAgeStr();
			case 4:
				return row.getDurationStr();
			case 5:
				return row.getMilestoneStatus();
			default:
				return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
}
