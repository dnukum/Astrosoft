/**
 * InfoView.java
 * Created On 2006, Mar 18, 2006 3:39:42 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import app.astrosoft.consts.TableStyle;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.ColumnMetaData;
import app.astrosoft.ui.table.ListTableData;
import app.astrosoft.ui.table.MapTableRow;
import app.astrosoft.ui.table.Table;
import app.astrosoft.ui.table.TableDataFactory;

public class InfoView extends AstrosoftView {

	public static String formatInfoValue(String key, String value) {
		if (value == null || value.trim().isEmpty()) return "";
		String s = value.trim();
		if (s.startsWith("<html>")) return s;

		String lk = key.toLowerCase();

		// Name: break long names on underscore or space
		if (lk.contains("name")) {
			if (s.length() > 13) {
				if (s.contains("_")) {
					int lastUs = s.lastIndexOf('_', 14);
					if (lastUs > 3) {
						return "<html>" + s.substring(0, lastUs + 1) + "<br>" + s.substring(lastUs + 1) + "</html>";
					}
					int firstUs = s.indexOf('_');
					if (firstUs > 0) {
						return "<html>" + s.substring(0, firstUs + 1) + "<br>" + s.substring(firstUs + 1) + "</html>";
					}
				} else if (s.contains(" ")) {
					int sp = s.lastIndexOf(' ', 14);
					if (sp > 3) {
						return "<html>" + s.substring(0, sp) + "<br>" + s.substring(sp + 1) + "</html>";
					}
				}
			}
		}

		// Place: break after comma(s)
		if (lk.contains("place")) {
			if (s.contains(",")) {
				String[] parts = s.split(",");
				if (parts.length == 2) {
					return "<html>" + parts[0].trim() + ",<br>" + parts[1].trim() + "</html>";
				} else if (parts.length > 2) {
					return "<html>" + parts[0].trim() + ",<br>" + parts[1].trim() + ",<br>" + parts[2].trim() + "</html>";
				}
			} else if (s.length() > 14 && s.contains(" ")) {
				int sp = s.lastIndexOf(' ', 14);
				if (sp > 3) {
					return "<html>" + s.substring(0, sp) + "<br>" + s.substring(sp + 1) + "</html>";
				}
			}
		}

		// Sunrise/Sunset: break after '/'
		if (lk.contains("sunrise") || lk.contains("sunset")) {
			int slash = s.indexOf('/');
			if (slash > 0 && slash < s.length() - 1) {
				String p1 = s.substring(0, slash).trim();
				String p2 = s.substring(slash + 1).trim();
				return "<html>" + p1 + " /<br>" + p2 + "</html>";
			}
		}

		// Lat/Long: break between coordinates and (GMT...)
		if (lk.contains("lat") || lk.contains("long") || lk.contains("coord")) {
			int gmt = s.indexOf("(GMT");
			if (gmt > 0) {
				String coords = s.substring(0, gmt).replaceAll("\\s+", " ").trim();
				String tz = s.substring(gmt).trim();
				return "<html>" + coords + "<br>" + tz + "</html>";
			}
			int doubleSpace = s.indexOf("  ");
			if (doubleSpace > 0) {
				return "<html>" + s.substring(0, doubleSpace).trim() + "<br>" + s.substring(doubleSpace).trim() + "</html>";
			}
		}

		// Dasa / Bal of: break before balance duration if long
		if (lk.contains("dasa") || lk.contains("dasha")) {
			if (s.length() > 16) {
				int dIdx = s.indexOf("Dasa ");
				if (dIdx > 0) {
					return "<html>" + s.substring(0, dIdx + 4).trim() + "<br>" + s.substring(dIdx + 5).trim() + "</html>";
				}
				int dIdx2 = s.indexOf("Dasa");
				if (dIdx2 > 0) {
					return "<html>" + s.substring(0, dIdx2 + 4).trim() + "<br>" + s.substring(dIdx2 + 4).trim() + "</html>";
				}
			}
		}

		// Nakshatra: break if long with '~'
		if (lk.contains("nak") || lk.contains("star")) {
			if (s.length() > 15 && s.contains("~")) {
				int tilde = s.indexOf('~');
				return "<html>" + s.substring(0, tilde).trim() + "<br>" + s.substring(tilde).trim() + "</html>";
			}
		}

		// Fallback for any other long value with spaces
		if (s.length() > 18 && s.contains(" ")) {
			int mid = s.lastIndexOf(' ', 16);
			if (mid > 4) {
				return "<html>" + s.substring(0, mid) + "<br>" + s.substring(mid + 1).trim() + "</html>";
			}
		}
		return s;
	}

    public InfoView(Table horoscopeInfo, Point loc) {

    	super(new Dimension(480, 230), loc);
    	
    	ListTableData<MapTableRow> allData = (ListTableData<MapTableRow>) horoscopeInfo.getTableData();
    	ColumnMetaData infoColMetaData = horoscopeInfo.getColumnMetaData();
    	
    	JPanel infoPanel = new JPanel(new GridLayout(1, 2, 16, 0));
    	int splitSize = (allData.getRowCount() + 1) / 2;
    	List<ListTableData<MapTableRow>> splittedData = TableDataFactory.splitTableData(allData, splitSize);
    	
    	for(ListTableData<MapTableRow> data : splittedData){
    		final AstrosoftTable infoTable = new AstrosoftTable(new AstrosoftTableModel(data, infoColMetaData), TableStyle.NONE);
    		
    		javax.swing.table.TableColumn keyCol = infoTable.getColumnModel().getColumn(0);
    		javax.swing.table.TableColumn valCol = infoTable.getColumnModel().getColumn(1);
    		keyCol.setPreferredWidth(98);
    		keyCol.setMaxWidth(115);
    		valCol.setPreferredWidth(142);

    		valCol.setCellRenderer(new DefaultTableCellRenderer() {
    			@Override
    			public Component getTableCellRendererComponent(JTable table, Object value,
    					boolean isSelected, boolean hasFocus, int row, int column) {
    				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    				c.setFont(new java.awt.Font("Verdana", java.awt.Font.PLAIN, 11));
    				if (value != null) {
    					Object keyVal = table.getValueAt(row, 0);
    					String key = (keyVal != null) ? keyVal.toString() : "";
    					String formatted = formatInfoValue(key, value.toString());
    					setText(formatted);
    					setToolTipText(value.toString());
    				}
    				return c;
    			}
    		});

    		for (int r = 0; r < infoTable.getRowCount(); r++) {
    			Object keyVal = infoTable.getValueAt(r, 0);
    			Object valVal = infoTable.getValueAt(r, 1);
    			String key = (keyVal != null) ? keyVal.toString() : "";
    			String val = (valVal != null) ? valVal.toString() : "";
    			String formatted = formatInfoValue(key, val);
    			if (formatted.contains("<br>")) {
    				int brCount = (formatted.length() - formatted.replace("<br>", "").length()) / 4;
    				if (brCount >= 2) {
    					infoTable.setRowHeight(r, 46);
    				} else {
    					infoTable.setRowHeight(r, 34);
    				}
    			} else {
    				infoTable.setRowHeight(r, 22);
    			}
    		}

    		infoPanel.add(infoTable);
    	}
    	
    	add(infoPanel, BorderLayout.CENTER);
    }
}
