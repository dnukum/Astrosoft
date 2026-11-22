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
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import app.astrosoft.consts.DisplayFormat;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.consts.Planet;
import app.astrosoft.ui.util.PlanetStatusCodes;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;

public class PlanetCellRenderer implements TableCellRenderer {

	private TableCellRenderer tableCellRenderer;
	private boolean highlightRetrogrades;
	private boolean expandRetrogrades;
	private DisplayFormat planetFormat;
	private EnumMap<Planet, Boolean> planetDir;
	private EnumMap<Planet, Boolean> planetCombustion;

	private Map<Planet, PlanetStatus> planetStatus;

	/** Optional dignity/vargottama status; when set, the planet cell shows e.g. "Sun (R,Ex,V)". */
	public void setPlanetStatus(Map<Planet, PlanetStatus> planetStatus) {
		this.planetStatus = planetStatus;
	}

	/** Optional combustion map; when set, combust planets show "C" e.g. "Mercury (R,C,Ex,V)". */
	public void setPlanetCombustion(EnumMap<Planet, Boolean> planetCombustion) {
		this.planetCombustion = planetCombustion;
	}
	
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
			Font baseFont = UIUtil.getFont(Font.BOLD, targetSize);
			l.setFont(baseFont);

			int colW = (table != null && column >= 0 && column < table.getColumnCount())
					? table.getColumnModel().getColumn(column).getWidth()
					: 0;

			// In narrow columns (< 32px, e.g. 3-planet houses), zero out horizontal padding
			// to avoid clipping wide planet labels like "Mo", "Asc", or "Me(C)".
			int pad = (colW > 0 && colW < 32) ? 0 : ((colW > 0 && colW < 38) ? 1 : 2);
			l.setBorder(BorderFactory.createEmptyBorder(0, pad, 0, pad));

			if (colW >= 40 || column == 1) {
				l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			}

			String labelText = p.toString(planetFormat);
			Color fgClr = UIConsts.getChartPlanetForeground();
			boolean applyRetroUnderline = false;

			if (p.isAsc()) {
				fgClr = UIConsts.getChartAscendantColor();
			} else if (p == Planet.Moon) {
				fgClr = UIConsts.getChartMoonColor();
			} else if (highlightRetrogrades && planetDir != null && planetDir.containsKey(p) && Boolean.TRUE.equals(planetDir.get(p))) {
				if (expandRetrogrades) {
					labelText = labelText + " (" + DisplayStrings.RETRO_SYM + ")";
				} else if (labelText.length() > 0) {
					applyRetroUnderline = true;
				}
			}

			if (!p.isAsc() && (planetStatus != null || planetCombustion != null)) {
				boolean retro = highlightRetrogrades && planetDir != null && planetDir.containsKey(p) && Boolean.TRUE.equals(planetDir.get(p)) && p != Planet.Moon;
				boolean combust = planetCombustion != null && Boolean.TRUE.equals(planetCombustion.get(p));
				if (planetStatus != null && (expandRetrogrades || !retro)) {
					String retroSym = (expandRetrogrades && retro) ? DisplayStrings.RETRO_SYM.toString() : null;
					String lead = null;
					if (retroSym != null && combust) {
						lead = retroSym + ",C";
					} else if (retroSym != null) {
						lead = retroSym;
					} else if (combust) {
						lead = "C";
					}
					labelText = p.toString(planetFormat) + PlanetStatusCodes.suffixText(lead, planetStatus.get(p));
				} else if (planetStatus == null && combust) {
					labelText = labelText + ((colW > 0 && colW < 36) ? "(C)" : " (C)");
				}
			}

			l.setText(labelText);

			if (colW > 10) {
				int availW = colW - (pad * 2) - 1;
				int textW = l.getFontMetrics(l.getFont()).stringWidth(labelText);
				int fitSize = targetSize;
				while (textW > availW && fitSize > 7) {
					fitSize--;
					Font fitFont = UIUtil.getFont(Font.BOLD, fitSize);
					textW = l.getFontMetrics(fitFont).stringWidth(labelText);
					l.setFont(fitFont);
				}
			}

			if (applyRetroUnderline) {
				java.util.Map<java.awt.font.TextAttribute, Object> attrs = new java.util.HashMap<>(l.getFont().getAttributes());
				attrs.put(java.awt.font.TextAttribute.UNDERLINE, java.awt.font.TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
				l.setFont(l.getFont().deriveFont(attrs));
			}

			l.setForeground(fgClr);
			return l;
		}
		return c;
	}
	
}
