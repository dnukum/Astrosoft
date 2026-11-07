/**
 * HousePACViewTest.java
 *
 * Headless unit test verifying HousePACView rendering, house switching,
 * HTML PAC card formatting, and integration with RelativePlanetView tab 3.
 */
package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;

import org.junit.Before;
import org.junit.Test;

import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.ui.view.HousePACView;
import app.astrosoft.ui.view.RelativePlanetView;

public class HousePACViewTest {

	private Horoscope horoscope;

	@Before
	public void setUp() {
		horoscope = SampleHoroscopes.getMyHoroscope();
	}

	private static List<JButton> findButtons(Container c) {
		List<JButton> buttons = new ArrayList<>();
		for (Component comp : c.getComponents()) {
			if (comp instanceof JButton) {
				buttons.add((JButton) comp);
			} else if (comp instanceof Container) {
				buttons.addAll(findButtons((Container) comp));
			}
		}
		return buttons;
	}

	private static JEditorPane findEditorPane(Container c) {
		for (Component comp : c.getComponents()) {
			if (comp instanceof JEditorPane) {
				return (JEditorPane) comp;
			} else if (comp instanceof Container) {
				JEditorPane ep = findEditorPane((Container) comp);
				if (ep != null) return ep;
			}
		}
		return null;
	}

	@Test
	public void testHousePACViewRenderingAndHouseSwitching() {
		HousePACView view = new HousePACView(horoscope);
		assertNotNull(view);
		assertEquals(1, view.getSelectedHouse());

		JEditorPane editor = findEditorPane(view);
		assertNotNull("Editor pane for PAC card must exist", editor);
		String textH1 = editor.getText();
		assertNotNull(textH1);
		assertTrue(textH1.contains("HOUSE 1"));
		assertTrue(textH1.contains("[P] PLACEMENT"));
		assertTrue(textH1.contains("[A] ASPECTS"));
		assertTrue(textH1.contains("[C] CONJUNCTIONS"));

		// Switch to House 10
		view.selectHouse(10);
		assertEquals(10, view.getSelectedHouse());
		String textH10 = editor.getText();
		assertTrue(textH10.contains("HOUSE 10"));

		// Switch through all 12 houses
		for (int h = 1; h <= 12; h++) {
			view.selectHouse(h);
			assertEquals(h, view.getSelectedHouse());
			String html = editor.getText();
			assertTrue("HTML for house " + h + " must contain header", html.contains("HOUSE " + h));
		}
	}

	@Test
	public void testCopySummaries() {
		HousePACView view = new HousePACView(horoscope);
		view.selectHouse(1);
		// Should execute cleanly without throwing
		view.copyCurrentHouseSummary();
		view.copyAllHousesSummary();
	}

	@Test
	public void testRelativePlanetViewCleanTabs() {
		PlanetaryInfo pi = horoscope.getPlanetaryInfo();
		RelativePlanetView rpv = new RelativePlanetView(pi, horoscope);
		assertNotNull(rpv);
		rpv.setSize(530, 188);
		rpv.doLayout();

		// Verify 2 clean tabs exist: Overview and Shadbala (Roles & Karakas retired)
		List<JButton> allButtons = findButtons(rpv);
		List<JButton> tabButtons = new ArrayList<>();
		for (JButton b : allButtons) {
			if ("Overview".equals(b.getText()) || "Shadbala".equals(b.getText())) {
				tabButtons.add(b);
			}
		}
		assertEquals("Both tab buttons must exist", 2, tabButtons.size());

		// Verify none wrapped to row 2
		int firstY = tabButtons.get(0).getY();
		for (JButton tb : tabButtons) {
			assertEquals("Tab button " + tb.getText() + " must stay on single row without wrapping",
					firstY, tb.getY());
		}

		assertEquals(232, rpv.getCardHeight());
	}

	@Test
	public void testPlanetDetailsDialog() {
		app.astrosoft.ui.dlg.PlanetDetailsDialog dlg = new app.astrosoft.ui.dlg.PlanetDetailsDialog(
				null, horoscope, null);
		assertNotNull(dlg);
		assertTrue("Dialog must be resizable", dlg.isResizable());
		assertFalse("Dialog must be modeless", dlg.isModal());
		assertNotNull(dlg.getPlanetView());
		dlg.dispose();
	}

	@Test
	public void testOccupantFormatting() {
		HousePACView view = new HousePACView(horoscope);
		// House 4 has Mars & Sun in Sagittarius (for Virgo ascendant)
		view.selectHouse(4);
		JEditorPane editor = findEditorPane(view);
		assertNotNull(editor);
		String html = editor.getText();
		assertTrue(html.contains("Occupants"));
		assertTrue(html.contains("Mars"));
		assertTrue(html.contains("sign"));
	}

	@Test
	public void testHouseDetailsDialog() {
		app.astrosoft.ui.dlg.HouseDetailsDialog dlg = new app.astrosoft.ui.dlg.HouseDetailsDialog(
				null, horoscope, null);
		assertNotNull(dlg);
		assertTrue("Dialog must be resizable", dlg.isResizable());
		assertFalse("Dialog must be modeless", dlg.isModal());
		assertNotNull(dlg.getPACView());
		assertEquals(1, dlg.getPACView().getSelectedHouse());
		dlg.dispose();
	}

	@Test
	public void testOverviewKendraTrikonaAndFunctionalNatureSection() {
		PlanetaryInfo pi = horoscope.getPlanetaryInfo();
		RelativePlanetView rpv = new RelativePlanetView(pi, horoscope);
		assertNotNull(rpv);
		rpv.setSize(530, 232);
		rpv.doLayout();

		List<JLabel> labels = findLabels(rpv);
		boolean foundKendra = false;
		boolean foundTrikona = false;
		boolean foundYogakaraka = false;
		boolean foundBenefics = false;
		boolean foundNeutrals = false;
		boolean foundMalefics = false;

		for (JLabel l : labels) {
			String text = l.getText();
			if (text != null) {
				if (text.contains("Kendra (1,4,7,10):")) foundKendra = true;
				if (text.contains("Trikona (1,5,9):")) foundTrikona = true;
				if (text.contains("Yogakaraka:")) foundYogakaraka = true;
				if (text.contains("Functional Benefics:")) foundBenefics = true;
				if (text.contains("Functional Neutrals:")) foundNeutrals = true;
				if (text.contains("Functional Malefics:")) foundMalefics = true;
			}
		}

		assertTrue("Kendra lords label must exist in Overview", foundKendra);
		assertTrue("Trikona lords label must exist in Overview", foundTrikona);
		assertTrue("Yogakaraka label must exist in Overview", foundYogakaraka);
		assertTrue("Functional Benefics label must exist in Overview", foundBenefics);
		assertTrue("Functional Neutrals label must exist in Overview", foundNeutrals);
		assertTrue("Functional Malefics label must exist in Overview", foundMalefics);
	}

	private static List<JLabel> findLabels(Container c) {
		List<JLabel> list = new ArrayList<JLabel>();
		for (Component comp : c.getComponents()) {
			if (comp instanceof JLabel) {
				list.add((JLabel) comp);
			}
			if (comp instanceof Container) {
				list.addAll(findLabels((Container) comp));
			}
		}
		return list;
	}

	private static HousePACView findHousePACView(Container c) {
		for (Component comp : c.getComponents()) {
			if (comp instanceof HousePACView) {
				return (HousePACView) comp;
			} else if (comp instanceof Container) {
				HousePACView found = findHousePACView((Container) comp);
				if (found != null) return found;
			}
		}
		return null;
	}
}
