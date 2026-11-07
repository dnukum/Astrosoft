package app.astrosoft.ui.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

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
	public void testPositionsTooltipContainsNakshatraAndJaiminiKaraka() {
		Horoscope h = SampleHoroscopes.getMyHoroscope();
		assertNotNull(h);
		PlanetaryInfo pi = h.getPlanetaryInfo();
		assertNotNull(pi);

		RelativePlanetView view = new RelativePlanetView(pi, h);
		assertNotNull(view);

		List<AstrosoftTable> tables = findTables(view);
		assertTrue("At least two tables found in view", tables.size() >= 2);

		boolean foundAnyPlanetWithTooltip = false;
		for (AstrosoftTable t : tables) {
			if (t.getColumnModel().getColumnCount() > 1 &&
				"Longitude".equals(t.getColumnModel().getColumn(1).getHeaderValue().toString())) {
				for (int row = 0; row < t.getRowCount(); row++) {
					Point pt = t.getCellRect(row, 0, true).getLocation();
					MouseEvent me = new MouseEvent(t, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, pt.x + 5, pt.y + 5, 0, false);
					String tt = t.getToolTipText(me);
					if (tt != null) {
						foundAnyPlanetWithTooltip = true;
						Object pVal = t.getValueAt(row, 0);
						String pName = (pVal != null) ? pVal.toString() : "";
						// Tooltips should contain Nakshatra, Pada, Star Lord
						assertTrue("Tooltip for " + pName + " should contain Nakshatra: " + tt, tt.contains("Nakshatra"));
						assertTrue("Tooltip for " + pName + " should contain Pada: " + tt, tt.contains("Pada"));
						assertTrue("Tooltip for " + pName + " should contain Star Lord: " + tt, tt.contains("Star Lord"));
						if (!"Ascendant".equals(pName) && !"Rahu".equals(pName) && !"Ketu".equals(pName)) {
							assertTrue("Tooltip for " + pName + " should contain Jaimini Chara Karaka: " + tt, tt.contains("Jaimini Chara Karaka"));
						}
					}
				}
			}
		}

		assertTrue("Should have inspected at least one planet tooltip", foundAnyPlanetWithTooltip);
	}
}
