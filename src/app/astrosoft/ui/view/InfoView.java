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

public class InfoView extends AstrosoftView {

	private List<AstrosoftTable> createdTables = new ArrayList<AstrosoftTable>();
	private JLabel titleLabel;
	private JPanel infoPanel;

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
    				c.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
    				c.setForeground(UIConsts.getChartLabelColor());
    				if (c instanceof JLabel) {
    					((JLabel) c).setVerticalAlignment(SwingConstants.TOP);
    				}
    				setOpaque(false);
    				return c;
    			}
    		});

    		valCol.setCellRenderer(new DefaultTableCellRenderer() {
    			@Override
    			public Component getTableCellRendererComponent(JTable table, Object value,
    					boolean isSelected, boolean hasFocus, int row, int column) {
    				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    				c.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
    				java.awt.Color fg = UIConsts.getChartPlanetForeground();
    				c.setForeground(fg);
    				if (c instanceof JLabel) {
    					((JLabel) c).setVerticalAlignment(SwingConstants.TOP);
    				}
    				setOpaque(false);
    				if (value != null) {
    					Object keyVal = table.getValueAt(row, 0);
    					String key = (keyVal != null) ? keyVal.toString() : "";
    					String formatted = formatInfoValue(key, value.toString());
    					if (formatted.startsWith("<html>")) {
    						String hexColor = String.format("#%02x%02x%02x", fg.getRed(), fg.getGreen(), fg.getBlue());
    						setText("<html><span style='color:" + hexColor + ";'>" + formatted.substring(6, formatted.length() - 7) + "</span></html>");
    					} else {
    						setText(formatted);
    					}
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
    	
    	setLayout(null);
    	setOpaque(false);

    	titleLabel = new JLabel("Profile", SwingConstants.CENTER);
    	titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
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
