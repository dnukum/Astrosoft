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
			Font resolved = UIUtil.getFont(font.getFontName(), font.getStyle(), targetSize, Math.max(targetSize, 12));
			if (value != null) {
				String str = (c instanceof javax.swing.JLabel && ((javax.swing.JLabel) c).getText() != null
						&& !((javax.swing.JLabel) c).getText().isEmpty())
						? ((javax.swing.JLabel) c).getText()
						: value.toString();
				boolean hasTamil = false;
				for (int i = 0; i < str.length(); i++) {
					char ch = str.charAt(i);
					if (ch >= '\u0B80' && ch <= '\u0BFF') {
						hasTamil = true;
						break;
					}
				}
				if (!hasTamil && resolved.canDisplayUpTo(str) != -1) {
					resolved = new Font(Font.SANS_SERIF, resolved.getStyle(), resolved.getSize());
				}
			}
			c.setFont(resolved);
		}
		
		return c;
	}
}
