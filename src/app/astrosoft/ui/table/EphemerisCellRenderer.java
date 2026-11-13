/**
 * EphemerisCellRenderer.java
 * Created On 2006, Apr 7, 2006 6:23:50 PM
 * @author E. Rajasekar
 */
package app.astrosoft.ui.table;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import app.astrosoft.consts.DisplayFormat;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.core.Panchang;
import app.astrosoft.core.Ephemeris.EphData;
import app.astrosoft.ui.util.UIConsts;
import app.astrosoft.ui.util.UIUtil;
import app.astrosoft.util.AstroUtil;

public class EphemerisCellRenderer extends DefaultTableCellRenderer {

	public EphemerisCellRenderer() {
		super();
		setHorizontalAlignment(JLabel.CENTER);
	}

	@Override
	protected void setValue(Object value) {

		if (value instanceof EphData) {
			EphData ephValue = (EphData) value;
			StringBuilder data = new StringBuilder("<html><nobr style='white-space:nowrap;'>");

			String sym = ephValue.getHouse().toString(DisplayFormat.SYMBOL);
			String house = "&nbsp;<font color='#00468C'><b>" + sym + "</b></font>&nbsp;";

			data.append(AstroUtil.todegmin(ephValue.getPosition(), house));

			if (ephValue.isReverse()){
				data.append("&nbsp;<font color='#C53030'><b>[").append(DisplayStrings.RETRO_SYM).append("]</b></font>");
			}
			data.append("</nobr></html>");
			super.setValue(data.toString());
		} else {
			super.setValue(value);
		}
	}
}
