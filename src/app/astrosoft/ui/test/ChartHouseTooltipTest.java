package app.astrosoft.ui.test;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTable;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Varga;
import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.comp.Chart;

/**
 * Headless test verifying that Chart house tooltips display the sign's
 * Polarity (Odd/Even), Cardinality (Movable/Fixed/Common), Element (Fire/Earth/Air/Water),
 * and for D1/Rasi charts, the resident planets' functional roles (e.g. Sat [Yog], Jup [Mal], Sun [Ben])
 * with proper green/red color styling.
 */
public class ChartHouseTooltipTest {

	private static Horoscope horoscope;

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty("java.awt.headless", "true");
		horoscope = SampleHoroscopes.getHoroscope("samples/raja.ash");
	}

	@Test
	public void testHouseTooltipContainsPolarityCardinalityAndElement() {
		PlanetChartData data = new PlanetChartData(Varga.Rasi, horoscope.getPlanetaryInfo());
		Chart chart = new Chart(data, new Dimension(418, 365));
		assertNotNull(chart);

		boolean verifiedAtLeastOneHouse = false;

		for (Component comp : chart.getComponents()) {
			if (comp instanceof JPanel) {
				JPanel panel = (JPanel) comp;
				String tip = panel.getToolTipText();

				// If panel doesn't have it directly, check child JTable
				if (tip == null) {
					for (Component child : panel.getComponents()) {
						if (child instanceof JTable) {
							tip = ((JTable) child).getToolTipText();
							break;
						}
					}
				}

				if (tip != null && tip.contains("&deg;")) {
					verifiedAtLeastOneHouse = true;

					// Find which Rasi this panel corresponds to
					for (Rasi r : Rasi.values()) {
						if (tip.contains("<b>" + r.name() + "</b>")) {
							String expectedClassification = r.classification(); // e.g. "Odd, Movable, Fire"
							assertTrue("Tooltip for " + r.name() + " should contain classification '" 
									+ expectedClassification + "'. Actual: " + tip,
									tip.contains(expectedClassification));
							break;
						}
					}
				}
			}
		}

		assertTrue("Should have verified at least one house tooltip in the chart", verifiedAtLeastOneHouse);
	}

	@Test
	public void testD1ChartTooltipDisplaysFunctionalRolesWithColors() {
		PlanetChartData data = new PlanetChartData(Varga.Rasi, horoscope.getPlanetaryInfo());
		Chart chart = new Chart(data, new Dimension(418, 365));
		assertNotNull(chart);

		boolean foundPlanetsFunctionalRole = false;

		for (Component comp : chart.getComponents()) {
			if (comp instanceof JPanel) {
				JPanel panel = (JPanel) comp;
				String tip = panel.getToolTipText();

				if (tip != null && tip.contains("Planets:")) {
					foundPlanetsFunctionalRole = true;

					// Verify color tagging for Malefic / Benefic / Yogakaraka
					if (tip.contains("[Mal]")) {
						assertTrue("Malefics should be colored red in tooltip: " + tip,
								tip.contains("<font color=\"red\"><b>[Mal]</b></font>"));
					}
					if (tip.contains("[Ben]")) {
						assertTrue("Benefics should be colored green in tooltip: " + tip,
								tip.contains("<font color=\"green\"><b>[Ben]</b></font>"));
					}
					if (tip.contains("[Yog]")) {
						assertTrue("Yogakarakas should be colored green in tooltip: " + tip,
								tip.contains("<font color=\"green\"><b>[Yog]</b></font>"));
					}
				}
			}
		}

		assertTrue("D1/Rasi chart should display resident planets with functional roles in tooltip", foundPlanetsFunctionalRole);
	}

	@Test
	public void testNonD1ChartOmitsFunctionalRolesInTooltip() {
		PlanetChartData data = new PlanetChartData(Varga.Navamsa, horoscope.getPlanetaryInfo());
		Chart chart = new Chart(data, new Dimension(418, 365));
		assertNotNull(chart);

		for (Component comp : chart.getComponents()) {
			if (comp instanceof JPanel) {
				JPanel panel = (JPanel) comp;
				String tip = panel.getToolTipText();

				if (tip != null) {
					assertFalse("Non-D1 chart should not show [Mal] in tooltip: " + tip, tip.contains("[Mal]"));
					assertFalse("Non-D1 chart should not show [Ben] in tooltip: " + tip, tip.contains("[Ben]"));
					assertFalse("Non-D1 chart should not show [Yog] in tooltip: " + tip, tip.contains("[Yog]"));
				}
			}
		}
	}
}
