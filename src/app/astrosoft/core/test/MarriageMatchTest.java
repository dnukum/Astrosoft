package app.astrosoft.core.test;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import javax.swing.JRadioButton;

import junit.framework.TestCase;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Compactibility;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.ShadBala;
import app.astrosoft.ui.view.MarriageMatchView;
import app.astrosoft.ui.view.ShadBalaBarView;
import app.astrosoft.ui.view.ShadBalaBarView.PlanetBalaItem;

public class MarriageMatchTest extends TestCase {

	private Horoscope boyHoroscope;
	private Horoscope girlHoroscope;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// Test charts: Elango (Boy) & Mani (Girl) from Compactibility.main
		boyHoroscope = new Horoscope("Elango", 17, 4, 1957, 7, 10,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode");
		girlHoroscope = new Horoscope("Mani", 10, 8, 1960, 5, 30,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode");
	}

	public void testCompactibilityHoroscopeAccessors() {
		Compactibility c = new Compactibility(boyHoroscope, girlHoroscope);

		assertTrue("Should have horoscope flag set to true", c.hasHoroscope());
		assertNotNull("Boy horoscope must not be null", c.getBoyHoroscope());
		assertNotNull("Girl horoscope must not be null", c.getGirlHoroscope());
		assertEquals("Elango", c.getBoyName());
		assertEquals("Mani", c.getGirlName());

		assertTrue("Total Kutas gained must be positive", c.getTotalKutaGained() > 0);
		assertTrue("Total Kutas gained should not exceed 40", c.getTotalKutaGained() <= 40);

		// Verify dosha calculations
		assertTrue("Boy dosha total should be non-negative", c.getBoyDoshaTotal() >= 0.0);
		assertTrue("Girl dosha total should be non-negative", c.getGirlDoshaTotal() >= 0.0);
		assertNotNull("Boy dosha map must not be null", c.getBoyDosha());
		assertNotNull("Girl dosha map must not be null", c.getGirlDosha());

		// Verify current period & beeja/kshetra accessors
		assertNotNull("Boy current Dasa period should be present", c.getBoyCurPeriod());
		assertNotNull("Girl current Dasa period should be present", c.getGirlCurPeriod());
		assertNotNull("Beeja rasi should be present", c.getBeejaRasi());
		assertNotNull("Kshetra rasi should be present", c.getKshetraRasi());
	}

	public void testDualShadBalaExtraction() {
		Compactibility c = new Compactibility(boyHoroscope, girlHoroscope);

		ShadBala boyShadBala = c.getBoyHoroscope().getShadBala();
		ShadBala girlShadBala = c.getGirlHoroscope().getShadBala();

		assertNotNull("Boy Shadbala must not be null", boyShadBala);
		assertNotNull("Girl Shadbala must not be null", girlShadBala);

		List<PlanetBalaItem> boyItems = ShadBalaBarView.extractItems(boyShadBala);
		List<PlanetBalaItem> girlItems = ShadBalaBarView.extractItems(girlShadBala);

		assertEquals("Boy should have 7 major planets in Shadbala", 7, boyItems.size());
		assertEquals("Girl should have 7 major planets in Shadbala", 7, girlItems.size());

		for (PlanetBalaItem item : boyItems) {
			assertNotNull("Planet must not be null", item.planet);
			assertTrue("Bala percentage must be positive", item.balaPercent > 0.0);
			assertTrue("Virupas must be positive", item.virupas > 0.0);
			assertTrue("Required minimum must be positive", item.minVirupas > 0.0);
		}

		// Verify custom title & dimension support in ShadBalaBarView
		ShadBalaBarView boyView = new ShadBalaBarView(boyShadBala, "Boy: Elango", new Dimension(360, 218));
		assertEquals(360, boyView.getPreferredSize().width);
		assertEquals(218, boyView.getPreferredSize().height);

		ShadBalaBarView girlView = new ShadBalaBarView(girlShadBala, "Girl: Mani", new Dimension(360, 218));
		assertEquals(360, girlView.getPreferredSize().width);

		// Verify traditional astrological order (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn)
		Planet[] expectedOrder = { Planet.Sun, Planet.Moon, Planet.Mars, Planet.Mercury, Planet.Jupiter, Planet.Venus, Planet.Saturn };
		List<PlanetBalaItem> renderedItems = boyView.getItems();
		assertEquals(7, renderedItems.size());
		for (int i = 0; i < expectedOrder.length; i++) {
			assertEquals("Planet at row " + i + " must follow traditional order", expectedOrder[i], renderedItems.get(i).planet);
		}
	}

	public void testMarriageMatchViewCreationAndModeSwitching() {
		Compactibility c = new Compactibility(boyHoroscope, girlHoroscope);
		MarriageMatchView view = new MarriageMatchView(c, new Point(0, 0));

		assertNotNull("MarriageMatchView instance should be created", view);

		// Test default mode is Dashboard (2-Column)
		assertEquals(MarriageMatchView.ViewMode.DASHBOARD, view.getCurrentViewMode());

		// Test switching to Widescreen (3-Column)
		view.setViewMode(MarriageMatchView.ViewMode.WIDESCREEN);
		assertEquals(MarriageMatchView.ViewMode.WIDESCREEN, view.getCurrentViewMode());

		// Test switching back to Dashboard
		view.setViewMode(MarriageMatchView.ViewMode.DASHBOARD);
		assertEquals(MarriageMatchView.ViewMode.DASHBOARD, view.getCurrentViewMode());

		// Test switching chart varga
		view.setChartVarga(app.astrosoft.consts.Varga.Navamsa);
		assertEquals(app.astrosoft.consts.Varga.Navamsa, view.getCurrentChartVarga());

		view.setChartVarga(app.astrosoft.consts.Varga.Rasi);
		assertEquals(app.astrosoft.consts.Varga.Rasi, view.getCurrentChartVarga());

		// Test Shadbala optional visibility: default OFF
		assertNotNull("Shadbala radio must exist", view.getShadbalaRadio());
		assertNotNull("Timeline radio must exist", view.getTimelineRadio());
		assertFalse("Shadbala radio must be unchecked by default", view.getShadbalaRadio().isSelected());
		assertFalse("Timeline radio must be unchecked by default", view.getTimelineRadio().isSelected());
		assertFalse("Shadbala card must be hidden by default", view.isShadbalaVisible());
		assertFalse("Timeline card must be hidden by default", view.isTimelineVisible());

		// Test toggling Shadbala on
		view.setShadbalaVisible(true);
		assertTrue("Shadbala radio must be checked when visible", view.getShadbalaRadio().isSelected());
		assertFalse("Timeline radio must be unchecked when Shadbala is visible", view.getTimelineRadio().isSelected());
		assertTrue("Shadbala card must be visible", view.isShadbalaVisible());
		assertFalse("Timeline card must be hidden", view.isTimelineVisible());

		// Test switching to Dasa Timeline
		view.setTimelineVisible(true);
		assertFalse("Shadbala radio must be unchecked when Timeline is selected", view.getShadbalaRadio().isSelected());
		assertTrue("Timeline radio must be checked when Timeline is visible", view.getTimelineRadio().isSelected());
		assertFalse("Shadbala card must be hidden", view.isShadbalaVisible());
		assertTrue("Timeline card must be visible", view.isTimelineVisible());

		// Verify Dasa Timeline panel existence, data, and status text format
		assertNotNull("DasaTimelinePanel must exist in view", view.getDasaTimelinePanel());
		assertEquals("Current Period", view.getDasaTimelinePanel().getCurrentPeriodButton().getText());
		assertNotNull(view.getDasaTimelinePanel().getWindowCombo());
		assertEquals("Next 5 Years (Default)", view.getDasaTimelinePanel().getWindowCombo().getSelectedItem());
		int rowCount = view.getDasaTimelinePanel().getTableModel().getRowCount();
		assertTrue("DasaTimelinePanel must have populated table rows", rowCount > 0);
		assertEquals(rowCount + " intervals", view.getDasaTimelinePanel().getStatusText());

		// Test toggling back off programmatically
		view.setTimelineVisible(false);
		assertFalse("Timeline card must be hidden", view.isTimelineVisible());
		assertFalse("Timeline radio must be unselected", view.getTimelineRadio().isSelected());
		assertFalse("Shadbala radio must be unselected", view.getShadbalaRadio().isSelected());

		// Test interactive toggling via doClick(): deselect both when clicked again
		view.getTimelineRadio().doClick();
		assertTrue("Timeline radio should be selected after click", view.getTimelineRadio().isSelected());
		assertFalse("Shadbala radio should remain unselected", view.getShadbalaRadio().isSelected());
		assertTrue("Timeline card should be visible", view.isTimelineVisible());

		view.getTimelineRadio().doClick(); // Click again to deselect
		assertFalse("Timeline radio should be deselected after 2nd click", view.getTimelineRadio().isSelected());
		assertFalse("Shadbala radio should be deselected", view.getShadbalaRadio().isSelected());
		assertFalse("Timeline card should be hidden when deselected", view.isTimelineVisible());

		view.getShadbalaRadio().doClick();
		assertTrue("Shadbala radio should be selected after click", view.getShadbalaRadio().isSelected());
		assertFalse("Timeline radio should remain unselected", view.getTimelineRadio().isSelected());
		assertTrue("Shadbala card should be visible", view.isShadbalaVisible());

		view.getTimelineRadio().doClick(); // Click timeline to switch
		assertTrue("Timeline radio should become selected", view.getTimelineRadio().isSelected());
		assertFalse("Shadbala radio should become unselected", view.getShadbalaRadio().isSelected());
		assertTrue("Timeline card should become visible", view.isTimelineVisible());
		assertFalse("Shadbala card should be hidden", view.isShadbalaVisible());

		view.getTimelineRadio().doClick(); // Click timeline again to turn off both
		assertFalse("Timeline radio should be deselected", view.getTimelineRadio().isSelected());
		assertFalse("Shadbala radio should be deselected", view.getShadbalaRadio().isSelected());
		assertFalse("Timeline card should be hidden", view.isTimelineVisible());
		assertFalse("Shadbala card should be hidden", view.isShadbalaVisible());
	}

	public void testMarriageMatchViewWithQuickStarEntry() {
		Compactibility quickComp = new Compactibility("Arun", "Divya",
				Nakshathra.Ashwini, Nakshathra.Bharani, Rasi.Mesha, Rasi.Mesha);

		assertFalse("Quick entry should not have horoscope", quickComp.hasHoroscope());
		assertTrue("Kutas should be calculated for quick entry", quickComp.getTotalKutaGained() >= 0);

		// MarriageMatchView must instantiate gracefully without throwing NullPointerException
		MarriageMatchView view = new MarriageMatchView(quickComp, new Point(0, 0));
		assertNotNull("View must handle quick entry gracefully", view);
	}

	public void testDoshaComparison() {
		// 1. Equal dosha
		String c1 = MarriageMatchView.calcDoshaComparison(100.0, 100.0);
		assertEquals("Boy's Dosha is equal to Girl's Dosha", c1);

		// 2. Boy is 20% more than Girl
		String c2 = MarriageMatchView.calcDoshaComparison(120.0, 100.0);
		assertEquals("Boy's Dosha is 20.0% more than Girl's Dosha", c2);

		// 3. Boy is 20% less than Girl
		String c3 = MarriageMatchView.calcDoshaComparison(80.0, 100.0);
		assertEquals("Boy's Dosha is 20.0% less than Girl's Dosha", c3);

		// 4. Boy is 50% more than Girl
		String c4 = MarriageMatchView.calcDoshaComparison(150.0, 100.0);
		assertEquals("Boy's Dosha is 50.0% more than Girl's Dosha", c4);

		// 5. Boy is 50% less than Girl
		String c5 = MarriageMatchView.calcDoshaComparison(50.0, 100.0);
		assertEquals("Boy's Dosha is 50.0% less than Girl's Dosha", c5);

		// 6. User screenshot case: 115.00 vs 18.75
		String c6 = MarriageMatchView.calcDoshaComparison(115.0, 18.75);
		assertEquals("Boy's Dosha is 513.3% more than Girl's Dosha", c6);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(MarriageMatchTest.class);
	}
}
