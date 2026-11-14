/**
 * InfoView.java
 * Created On 2006, Mar 18, 2006 3:39:42 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import app.astrosoft.consts.TableStyle;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.table.AstrosoftTableModel;
import app.astrosoft.ui.table.ColumnMetaData;
import app.astrosoft.ui.table.ListTableData;
import app.astrosoft.ui.table.MapTableRow;
import app.astrosoft.ui.table.Table;
import app.astrosoft.ui.table.TableDataFactory;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;

public class InfoView extends AstrosoftView {

	private List<AstrosoftTable> createdTables = new ArrayList<AstrosoftTable>();
	private JLabel titleLabel;
	private JPanel infoPanel;

	public static String formatInfoValue(String key, String value) {
		if (value == null || value.trim().isEmpty()) return "";
		String s = value.trim();

		String lk = key.toLowerCase();

		// Name: break long names on underscore or space
		if (lk.contains("name")) {
			if (s.length() > 13) {
				if (s.contains("_")) {
					int lastUs = s.lastIndexOf('_', 14);
					if (lastUs > 3) {
						return s.substring(0, lastUs + 1) + "\n" + s.substring(lastUs + 1);
					}
					int firstUs = s.indexOf('_');
					if (firstUs > 0) {
						return s.substring(0, firstUs + 1) + "\n" + s.substring(firstUs + 1);
					}
				} else if (s.contains(" ")) {
					int sp = s.lastIndexOf(' ', 14);
					if (sp > 3) {
						return s.substring(0, sp) + "\n" + s.substring(sp + 1);
					}
				}
			}
		}

		// Place: break after comma(s)
		if (lk.contains("place")) {
			if (s.contains(",")) {
				String[] parts = s.split(",");
				if (parts.length == 2) {
					return parts[0].trim() + ",\n" + parts[1].trim();
				} else if (parts.length > 2) {
					return parts[0].trim() + ",\n" + parts[1].trim() + ",\n" + parts[2].trim();
				}
			} else if (s.length() > 14 && s.contains(" ")) {
				int sp = s.lastIndexOf(' ', 14);
				if (sp > 3) {
					return s.substring(0, sp) + "\n" + s.substring(sp + 1);
				}
			}
		}

		// Sunrise/Sunset: break after '/'
		if (lk.contains("sunrise") || lk.contains("sunset")) {
			int slash = s.indexOf('/');
			if (slash > 0 && slash < s.length() - 1) {
				String p1 = s.substring(0, slash).trim();
				String p2 = s.substring(slash + 1).trim();
				return p1 + " /\n" + p2;
			}
		}

		// Lat/Long: break between coordinates and (GMT...)
		if (lk.contains("lat") || lk.contains("long") || lk.contains("coord")) {
			int gmt = s.indexOf("(GMT");
			if (gmt > 0) {
				String coords = s.substring(0, gmt).replaceAll("\\s+", " ").trim();
				String tz = s.substring(gmt).trim();
				return coords + "\n" + tz;
			}
			int doubleSpace = s.indexOf("  ");
			if (doubleSpace > 0) {
				return s.substring(0, doubleSpace).trim() + "\n" + s.substring(doubleSpace).trim();
			}
		}

		// Dasa / Bal of: break before balance duration if long
		if (lk.contains("dasa") || lk.contains("dasha")) {
			if (s.length() > 16) {
				int dIdx = s.indexOf("Dasa ");
				if (dIdx > 0) {
					return s.substring(0, dIdx + 4).trim() + "\n" + s.substring(dIdx + 5).trim();
				}
				int dIdx2 = s.indexOf("Dasa");
				if (dIdx2 > 0) {
					return s.substring(0, dIdx2 + 4).trim() + "\n" + s.substring(dIdx2 + 4).trim();
				}
			}
		}

		// Nakshatra: break if long with '~'
		if (lk.contains("nak") || lk.contains("star")) {
			if (s.length() > 15 && s.contains("~")) {
				int tilde = s.indexOf('~');
				return s.substring(0, tilde).trim() + "\n" + s.substring(tilde).trim();
			}
		}

		// Fallback for any other long value with spaces
		if (s.length() > 18 && s.contains(" ")) {
			int mid = s.lastIndexOf(' ', 16);
			if (mid > 4) {
				return s.substring(0, mid) + "\n" + s.substring(mid + 1).trim();
			}
		}
		return s;
	}

    public InfoView(Table horoscopeInfo, Point loc) {

    	super(new Dimension(530, 200), loc);
    	
    	ListTableData<MapTableRow> allData = (ListTableData<MapTableRow>) horoscopeInfo.getTableData();
    	ColumnMetaData infoColMetaData = horoscopeInfo.getColumnMetaData();
    	List<MapTableRow> allRows = allData.getAllRows();

    	List<ListTableData<MapTableRow>> splittedData = new ArrayList<ListTableData<MapTableRow>>();
    	if (allRows.size() == 15) {
    		splittedData.add(new ListTableData<MapTableRow>(allRows.subList(0, 7)));
    		splittedData.add(new ListTableData<MapTableRow>(allRows.subList(7, 15)));
    	} else {
    		int splitSize = (allData.getRowCount() + 1) / 2;
    		splittedData = TableDataFactory.splitTableData(allData, splitSize);
    	}
    	
    	infoPanel = new JPanel(new GridLayout(1, 2, 16, 0));
    	infoPanel.setOpaque(false);
    	createdTables.clear();

    	for(ListTableData<MapTableRow> data : splittedData){
    		final AstrosoftTable infoTable = new AstrosoftTable(new AstrosoftTableModel(data, infoColMetaData), TableStyle.NONE);
    		infoTable.setOpaque(false);
    		createdTables.add(infoTable);
    		
    		javax.swing.table.TableColumn keyCol = infoTable.getColumnModel().getColumn(0);
    		javax.swing.table.TableColumn valCol = infoTable.getColumnModel().getColumn(1);
    		keyCol.setPreferredWidth(98);
    		keyCol.setMaxWidth(115);
    		valCol.setPreferredWidth(142);

    		keyCol.setCellRenderer(new DefaultTableCellRenderer() {
    			@Override
    			public Component getTableCellRendererComponent(JTable table, Object value,
    					boolean isSelected, boolean hasFocus, int row, int column) {
    				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    				c.setFont(UIUtil.getFont(Font.PLAIN, 11));
    				c.setForeground(UIConsts.getChartLabelColor());
    				if (c instanceof JLabel) {
    					((JLabel) c).setVerticalAlignment(SwingConstants.TOP);
    				}
    				setOpaque(false);
    				return c;
    			}
    		});

    		valCol.setCellRenderer(new javax.swing.table.TableCellRenderer() {
    			final javax.swing.JTextArea valArea = UIUtil.createWrappingTextArea(
    					UIUtil.getFont(Font.BOLD, 11), UIConsts.getChartPlanetForeground());
    			{
    				valArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
    			}

    			@Override
    			public Component getTableCellRendererComponent(JTable table, Object value,
    					boolean isSelected, boolean hasFocus, int row, int column) {
    				valArea.setFont(UIUtil.getFont(Font.BOLD, 11));
    				valArea.setForeground(UIConsts.getChartPlanetForeground());
    				if (value != null) {
    					Object keyVal = table.getValueAt(row, 0);
    					String key = (keyVal != null) ? keyVal.toString() : "";
    					valArea.setText(formatInfoValue(key, value.toString()));
    					valArea.setToolTipText(value.toString());
    				} else {
    					valArea.setText("");
    					valArea.setToolTipText(null);
    				}
    				int colW = table.getColumnModel().getColumn(column).getWidth();
    				if (colW > 0) {
    					valArea.setSize(colW, Short.MAX_VALUE);
    				}
    				return valArea;
    			}
    		});

    		for (int r = 0; r < infoTable.getRowCount(); r++) {
    			Object keyVal = infoTable.getValueAt(r, 0);
    			Object valVal = infoTable.getValueAt(r, 1);
    			String key = (keyVal != null) ? keyVal.toString() : "";
    			String val = (valVal != null) ? valVal.toString() : "";
    			String formatted = formatInfoValue(key, val);
    			if (formatted.indexOf('\n') >= 0) {
    				int brCount = formatted.length() - formatted.replace("\n", "").length();
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
    	
    	setLayout(null);
    	setOpaque(false);

    	titleLabel = new JLabel("Profile", SwingConstants.CENTER);
    	titleLabel.setFont(UIUtil.getFont(Font.BOLD, 12));
    	titleLabel.setForeground(UIConsts.getChartPlanetForeground());
    	titleLabel.setBounds(0, 0, 530, 20);
    	add(titleLabel);

    	int maxTableH = 0;
    	for (AstrosoftTable tbl : createdTables) {
    		int h = 0;
    		for (int r = 0; r < tbl.getRowCount(); r++) {
    			h += tbl.getRowHeight(r);
    		}
    		if (h > maxTableH) maxTableH = h;
    	}

    	infoPanel.setBounds(18, 34, 494, maxTableH + 6);
    	add(infoPanel);

    	Dimension exactSize = new Dimension(530, maxTableH + 50);
    	setPreferredSize(exactSize);
    	setMinimumSize(exactSize);
    	setMaximumSize(exactSize);

    	updateTheme();
    }

    @Override
    public void doLayout() {
    	super.doLayout();
    	int currentW = getWidth();
    	if (currentW <= 0) currentW = 530;
    	int currentH = getHeight();
    	if (currentH <= 0) currentH = getPreferredSize() != null ? getPreferredSize().height : 200;

    	if (titleLabel != null) {
    		titleLabel.setBounds(0, 0, currentW, 20);
    	}
    	if (infoPanel != null) {
    		infoPanel.setBounds(18, 34, Math.max(100, currentW - 36), Math.max(50, currentH - 42));
    	}
    }

    @Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	Graphics2D g2 = (Graphics2D) g.create();
    	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    	int currentW = getWidth();
    	if (currentW <= 0) currentW = 530;
    	int currentH = getHeight();
    	if (currentH <= 0) currentH = getPreferredSize() != null ? getPreferredSize().height : 200;

    	int arc = 12;
    	int gap = 16;

    	int boxLeft = 2;
    	int boxRight = currentW - 4;
    	int boxTop = 10;
    	int boxBottom = currentH - 5;

    	// Theme card background
    	g2.setColor(UIConsts.getChartBackground());
    	g2.fillRoundRect(boxLeft, boxTop, boxRight - boxLeft, boxBottom - boxTop, arc, arc);

    	if (titleLabel != null) {
    		titleLabel.setBounds(0, 0, currentW, 20);
    		titleLabel.setForeground(UIConsts.getChartPlanetForeground());

    		FontMetrics fm = titleLabel.getFontMetrics(titleLabel.getFont());
    		int textW = fm.stringWidth(titleLabel.getText());
    		int textX = (currentW - textW) / 2;

    		Path2D p = new Path2D.Double();
    		// Top-left line to title cutout
    		p.moveTo(boxLeft + arc, boxTop);
    		p.lineTo(Math.max(boxLeft + arc, textX - gap), boxTop);

    		// Top-right line from title cutout to right corner
    		p.moveTo(Math.min(boxRight - arc, textX + textW + gap), boxTop);
    		p.lineTo(boxRight - arc, boxTop);

    		// Top-right rounded corner
    		p.quadTo(boxRight, boxTop, boxRight, boxTop + arc);

    		// Right vertical border
    		p.lineTo(boxRight, boxBottom - arc);

    		// Bottom-right rounded corner
    		p.quadTo(boxRight, boxBottom, boxRight - arc, boxBottom);

    		// Bottom horizontal border
    		p.lineTo(boxLeft + arc, boxBottom);

    		// Bottom-left rounded corner
    		p.quadTo(boxLeft, boxBottom, boxLeft, boxBottom - arc);

    		// Left vertical border
    		p.lineTo(boxLeft, boxTop + arc);

    		// Top-left rounded corner
    		p.quadTo(boxLeft, boxTop, boxLeft + arc, boxTop);

    		g2.setColor(UIConsts.getChartBorderColor());
    		g2.setStroke(new BasicStroke(1.5f));
    		g2.draw(p);
    	}
    	g2.dispose();
    }

    public void updateTheme() {
    	setBorder(null);
    	if (titleLabel != null) {
    		titleLabel.setForeground(UIConsts.getChartPlanetForeground());
    	}
    	if (createdTables != null) {
    		for (AstrosoftTable tbl : createdTables) {
    			tbl.repaint();
    		}
    	}
    	repaint();
    }
}
