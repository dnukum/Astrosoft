package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.junit.Assume;
import org.junit.Test;

import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.view.PlanetView;
import app.astrosoft.ui.view.RelativePlanetView;

/**
 * Builds the real Planetary Positions views for a real chart and reads the text the user sees.
 * Chart: WA-Query-Career-Health (Mercury 29:53 Meena, Navamsa Meena => Db,V; Jupiter Meena, combust => C,Own;
 * Saturn retrograde in Kanya).
 * Override the folder with -Dastrosoft.horoscopes.dir=...; the tests skip when the file is absent.
 * Run with a display (Planet.toString loads the main window class).
 */
public class PlanetaryPositionsViewTest {

	private static final String DEFAULT_DIR = "tests/charts";
	private static final String CHART = "WA-Query-Career-Health.ash";

	private static PlanetaryInfo info() {
		File f = new File(System.getProperty("astrosoft.horoscopes.dir", DEFAULT_DIR), CHART);
		Assume.assumeTrue(f.exists());
		return SampleHoroscopes.getHoroscope(f.getPath()).getPlanetaryInfo();
	}

	private static void collectTables(Container c, List<JTable> out) {
		for (Component child : c.getComponents()) {
			if (child instanceof JTable) {
				out.add((JTable) child);
			}
			if (child instanceof Container) {
				collectTables((Container) child, out);
			}
		}
	}

	/** Text of every label-rendered cell in every table of the view, exactly as rendered. */
	private static List<String> displayedTexts(Container view) {
		List<JTable> tables = new ArrayList<JTable>();
		collectTables(view, tables);
		assertTrue("view must contain a JTable", !tables.isEmpty());
		List<String> texts = new ArrayList<String>();
		for (JTable t : tables) {
			for (int r = 0; r < t.getRowCount(); r++) {
				for (int c = 0; c < t.getColumnCount(); c++) {
					Component comp = t.getCellRenderer(r, c).getTableCellRendererComponent(t, t.getValueAt(r, c), false, false, r, c);
					if (comp instanceof JLabel) {
						texts.add(((JLabel) comp).getText());
					}
				}
			}
		}
		return texts;
	}

	private static String startingWith(List<String> texts, String prefix) {
		for (String s : texts) {
			if (s != null && s.startsWith(prefix)) {
				return s;
			}
		}
		return null;
	}

	@Test
	public void compactPanelShowsMercuryDebilitatedAndVargottama() {
		List<String> texts = displayedTexts(new RelativePlanetView(info()));
		assertEquals("texts=" + texts, "Mercury (Db,V)", startingWith(texts, "Mercury"));
	}

	@Test
	public void compactPanelShowsJupiterCombustAndOwnSign() {
		List<String> texts = displayedTexts(new RelativePlanetView(info()));
		assertEquals("texts=" + texts, "Jupiter (C,Own)", startingWith(texts, "Jupiter"));
	}

	@Test
	public void compactPanelKeepsRetrogradeAndLeavesNeutralPlanetsPlain() {
		List<String> texts = displayedTexts(new RelativePlanetView(info()));
		assertEquals("texts=" + texts, "Saturn (R)", startingWith(texts, "Saturn"));
		assertEquals("texts=" + texts, "Venus", startingWith(texts, "Venus"));
		assertEquals("texts=" + texts, "Ascendant", startingWith(texts, "Ascendant"));
	}

	@Test
	public void planetTableShowsMercuryDebilitatedAndVargottama() {
		List<String> texts = displayedTexts(new PlanetView("Planetary Positions", info()));
		assertEquals("texts=" + texts, "Mercury (Db,V)", startingWith(texts, "Mercury"));
		assertEquals("texts=" + texts, "Venus", startingWith(texts, "Venus"));
	}
}
