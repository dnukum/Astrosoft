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

		// Verify 3 tabs exist and are on the same single row (no wrapping)
		List<JButton> allButtons = findButtons(rpv);
		List<JButton> tabButtons = new ArrayList<>();
		for (JButton b : allButtons) {
			if ("Positions".equals(b.getText()) || "Roles & Karakas".equals(b.getText())
					|| "Shadbala".equals(b.getText())) {
				tabButtons.add(b);
			}
		}
		assertEquals("All 3 tab buttons must exist", 3, tabButtons.size());

		// Verify none wrapped to row 2
		int firstY = tabButtons.get(0).getY();
		for (JButton tb : tabButtons) {
			assertEquals("Tab button " + tb.getText() + " must stay on single row without wrapping",
					firstY, tb.getY());
		}

		assertEquals(188, rpv.getCardHeight());
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
