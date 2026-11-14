package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
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
	public void testUnifiedOverviewTableStructureAndTooltips() {
		Horoscope h = SampleHoroscopes.getMyHoroscope();
		assertNotNull(h);
		PlanetaryInfo pi = h.getPlanetaryInfo();
		assertNotNull(pi);

		RelativePlanetView view = new RelativePlanetView(pi, h);
		assertNotNull(view);

		List<AstrosoftTable> tables = findTables(view);
		assertTrue("Unified overview table found in view", tables.size() >= 1);

		AstrosoftTable t = tables.get(0);
		assertEquals("Overview table must have 10 planetary rows", 10, t.getRowCount());
		assertEquals("Overview table must have 5 columns", 5, t.getColumnCount());

		assertEquals("Planet", t.getColumnName(0));
		assertEquals("Sign & Degree", t.getColumnName(1));
		assertEquals("Absolute (360°)", t.getColumnName(2));
		assertEquals("Nakshatra & Pada", t.getColumnName(3));
		assertEquals("Jaimini Karaka", t.getColumnName(4));

		// In South Indian style, row 0 must strictly start with As (Ascendant)
		assertEquals("First planet in South Indian chart must be As", "As", t.getValueAt(0, 0).toString());

		boolean foundAnyPlanetWithTooltip = false;
		for (int row = 0; row < t.getRowCount(); row++) {
			Object pVal = t.getValueAt(row, 0);
			Object degVal = t.getValueAt(row, 1);
			Object absVal = t.getValueAt(row, 2);
			Object nakVal = t.getValueAt(row, 3);
			Object kVal = t.getValueAt(row, 4);

			assertNotNull("Planet must not be null at row " + row, pVal);
			assertNotNull("Sign & Degree must not be null at row " + row, degVal);
			assertNotNull("Absolute 360° must not be null at row " + row, absVal);
			assertNotNull("Nakshatra & Pada must not be null at row " + row, nakVal);
			assertNotNull("Jaimini Karaka must not be null at row " + row, kVal);

			String pName = pVal.toString();
			String degStr = degVal.toString();
			String absStr = absVal.toString();
			String nakStr = nakVal.toString();
			String kStr = kVal.toString();

			// Degree column must contain degree symbol
			assertTrue("Degree format check: " + degStr, degStr.contains("°"));
			// Absolute 360° format check: contains colon delimiter
			assertTrue("Absolute 360° format check: " + absStr, absStr.contains(":"));
			// Nakshatra pada format check: contains tilde
			assertTrue("Nakshatra Pada format check: " + nakStr, nakStr.contains("~"));

			Point pt = t.getCellRect(row, 0, true).getLocation();
			MouseEvent me = new MouseEvent(t, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, pt.x + 5, pt.y + 5, 0, false);
			String tt = t.getToolTipText(me);
			if (tt != null) {
				foundAnyPlanetWithTooltip = true;
				// Tooltips should contain Nakshatra, Pada, Star Lord
				assertTrue("Tooltip for " + pName + " should contain Nakshatra: " + tt, tt.contains("Nakshatra"));
				assertTrue("Tooltip for " + pName + " should contain Pada: " + tt, tt.contains("Pada"));
				assertTrue("Tooltip for " + pName + " should contain Star Lord: " + tt, tt.contains("Star Lord"));
				if (!"As".equals(pName) && !"Ascendant".equals(pName) && !pName.startsWith("Rahu") && !pName.startsWith("Ketu")) {
					assertTrue("Tooltip for " + pName + " should contain Jaimini Chara Karaka: " + tt, tt.contains("Jaimini Chara Karaka"));
				}
			}
		}

		assertTrue("Should have inspected at least one planet tooltip", foundAnyPlanetWithTooltip);
	}

	@Test
	public void testNorthIndianStandardOrderEndingWithAsc() {
		app.astrosoft.ui.util.UIConsts.ChartStyle originalStyle = app.astrosoft.ui.util.UIConsts.getChartStyle();
		try {
			app.astrosoft.ui.util.UIConsts.setChartStyle(app.astrosoft.ui.util.UIConsts.ChartStyle.NORTH_INDIAN);

			Horoscope h = SampleHoroscopes.getMyHoroscope();
			assertNotNull(h);
			PlanetaryInfo pi = h.getPlanetaryInfo();
			assertNotNull(pi);

			RelativePlanetView view = new RelativePlanetView(pi, h);
			List<AstrosoftTable> tables = findTables(view);
			assertTrue("Unified table found in North Indian view", tables.size() >= 1);

			AstrosoftTable t = tables.get(0);
			assertEquals(10, t.getRowCount());

			String[] expectedOrder = {
				"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu", "As"
			};

			for (int row = 0; row < 10; row++) {
				Object val = t.getValueAt(row, 0);
				assertNotNull("Row " + row + " should not be null", val);
				String pName = val.toString();
				assertTrue("Row " + row + " expected starting with " + expectedOrder[row] + ", but got: " + pName,
						pName.startsWith(expectedOrder[row]));
			}
		} finally {
			app.astrosoft.ui.util.UIConsts.setChartStyle(originalStyle);
		}
	}
}
