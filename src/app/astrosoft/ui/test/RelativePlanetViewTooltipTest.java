package app.astrosoft.ui.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import org.junit.Test;

import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.view.RelativePlanetView;

public class RelativePlanetViewTooltipTest {

	private static List<AstrosoftTable> findTables(Container c) {
		List<AstrosoftTable> tables = new ArrayList<>();
		for (Component comp : c.getComponents()) {
			if (comp instanceof AstrosoftTable) {
				tables.add((AstrosoftTable) comp);
			} else if (comp instanceof Container) {
				tables.addAll(findTables((Container) comp));
			}
		}
		return tables;
	}

	@Test
	public void testRolesAndKarakasTooltipLayout1() {
		Horoscope h = SampleHoroscopes.getMyHoroscope();
		assertNotNull(h);
		PlanetaryInfo pi = h.getPlanetaryInfo();
		assertNotNull(pi);

		RelativePlanetView view = new RelativePlanetView(pi, h);
		assertNotNull(view);

		// Switch to Roles & Karakas tab (index 1)
		view.switchToTab(1);

		List<AstrosoftTable> tables = findTables(view);
		assertTrue("At least two tables found in view", tables.size() >= 2);

		// Check tooltips for planets in Roles & Karakas tab (where column 1 is Bhava)
		boolean foundAnyPlanetWithTooltip = false;
		for (AstrosoftTable t : tables) {
			if (t.getColumnModel().getColumnCount() > 1 &&
				"Bhava".equals(t.getColumnModel().getColumn(1).getHeaderValue().toString())) {
				for (int row = 0; row < t.getRowCount(); row++) {
					Point pt = t.getCellRect(row, 0, true).getLocation();
					MouseEvent me = new MouseEvent(t, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, pt.x + 5, pt.y + 5, 0, false);
					String tt = t.getToolTipText(me);
					if (tt != null) {
						foundAnyPlanetWithTooltip = true;
						Object pVal = t.getValueAt(row, 0);
						String pName = (pVal != null) ? pVal.toString() : "";
						if (!"Ascendant".equals(pName) && !"Rahu".equals(pName) && !"Ketu".equals(pName)) {
							// Should contain Layout 1 elements: Lord of, Occupies House, sign relationship
							assertTrue("Tooltip for " + pName + " should contain Lord of: " + tt, tt.contains("Lord of"));
							assertTrue("Tooltip for " + pName + " should contain Occupies House: " + tt, tt.contains("Occupies <b>House"));
							assertTrue("Tooltip for " + pName + " should contain sign): " + tt, tt.contains("sign)"));
						}
					}
				}
			}
		}

		assertTrue("Should have inspected at least one planet tooltip", foundAnyPlanetWithTooltip);
	}
}
