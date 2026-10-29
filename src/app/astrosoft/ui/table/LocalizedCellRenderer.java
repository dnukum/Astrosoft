/**
 * LocalizedCellRenderer.java
 * Created On 2006, Feb 17, 2006 8:49:28 PM
 * @author E. Rajasekar
 */

package app.astrosoft.ui.table;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import app.astrosoft.consts.Roman;
import app.astrosoft.ui.util.UIUtil;

public class LocalizedCellRenderer implements TableCellRenderer {

	private TableCellRenderer tableCellRenderer;
	private Font font;
	
	public LocalizedCellRenderer(TableCellRenderer tableCellRenderer, Font font) {
		this.tableCellRenderer = tableCellRenderer;
		this.font = font;
	}
	
	public LocalizedCellRenderer(TableCellRenderer tableCellRenderer) {
		this.tableCellRenderer = tableCellRenderer;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		Component c = tableCellRenderer.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);
		
		if (! (value instanceof Number || value instanceof Roman) ){
			Font tblFont = (table != null) ? table.getFont() : null;
			int targetSize = (tblFont != null) ? tblFont.getSize() : font.getSize();
			Font resolved = UIUtil.getFont(font.getFontName(), font.getStyle(), targetSize, 14);
			if (value != null && resolved.canDisplayUpTo(value.toString()) != -1) {
				resolved = new Font(Font.SANS_SERIF, resolved.getStyle(), resolved.getSize());
			}
			c.setFont(resolved);
		}
		
		return c;
	}
}
