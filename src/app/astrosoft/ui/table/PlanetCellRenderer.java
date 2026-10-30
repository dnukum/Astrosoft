/**
 * PlanetCellRenderer.java
 * Created On 2006, Apr 1, 2006 1:23:07 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.EnumMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import app.astrosoft.consts.DisplayFormat;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Planet;
import app.astrosoft.ui.util.UIConsts;

public class PlanetCellRenderer implements TableCellRenderer {

	private TableCellRenderer tableCellRenderer;
	private boolean highlightRetrogrades;
	private boolean expandRetrogrades;
	private DisplayFormat planetFormat;
	private EnumMap<Planet, Boolean> planetDir;
	
	public PlanetCellRenderer(TableCellRenderer tableCellRenderer, DisplayFormat planetFormat, boolean highlightRetrogrades, boolean expandRetrogrades, EnumMap<Planet, Boolean> planetDir) {
		
		this.tableCellRenderer = tableCellRenderer;
		this.highlightRetrogrades = highlightRetrogrades;
		this.expandRetrogrades = expandRetrogrades;
		this.planetFormat = planetFormat;
		this.planetDir = planetDir;
	}
	
	public PlanetCellRenderer(TableCellRenderer tableCellRenderer) {
		this(tableCellRenderer, DisplayFormat.SYMBOL, false, false, null);
	}

	public PlanetCellRenderer(TableCellRenderer tableCellRenderer, boolean highlightRetrogrades, boolean expandRetrogrades, EnumMap<Planet, Boolean> planetDir) {
		this(tableCellRenderer, DisplayFormat.SYMBOL, highlightRetrogrades, expandRetrogrades, planetDir);
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		Component c = tableCellRenderer.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);
		
		if (value != null && value instanceof Planet && c instanceof JLabel){
			
			JLabel l = new JLabel(((JLabel)c).getText());
			Planet p = (Planet)value;
			
			Font tblFont = (table != null) ? table.getFont() : null;
			int targetSize = (tblFont != null) ? tblFont.getSize() : c.getFont().getSize();
			l.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, targetSize));
			l.setText(p.toString(planetFormat));
			l.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

			if (table != null && column >= 0 && column < table.getColumnCount()) {
				int colW = table.getColumnModel().getColumn(column).getWidth();
				if (colW >= 40) {
					l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
				}
				if (colW > 10) {
					int availW = colW - 4;
					int textW = l.getFontMetrics(l.getFont()).stringWidth(l.getText());
					int fitSize = targetSize;
					while (textW > availW && fitSize > 8) {
						fitSize--;
						Font fitFont = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, fitSize);
						textW = l.getFontMetrics(fitFont).stringWidth(l.getText());
						l.setFont(fitFont);
					}
				}
			}

			Color fgClr = null;
			
			if (p.isAsc()){
				fgClr = UIConsts.getChartAscendantColor();
			} else if (p == Planet.Moon){
				fgClr = UIConsts.getChartMoonColor();
			} else if (highlightRetrogrades && planetDir != null && planetDir.containsKey(p) && planetDir.get(p)){
				fgClr = UIConsts.getChartPlanetForeground();
				if(expandRetrogrades){
					l.setText(p.toString(planetFormat) + " (" + DisplayStrings.RETRO_SYM + ")");
				} else {
					String text = p.toString(planetFormat);
					if (text.length() > 0) {
						l.setText("<html><u><b>" + text + "</b></u></html>");
					}
				}
			} else {
				fgClr = UIConsts.getChartPlanetForeground();
			}
			
			l.setForeground(fgClr);
			return l;
		}
		return c;
	}
	
}
