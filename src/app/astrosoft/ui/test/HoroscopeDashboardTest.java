package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.Test;

import app.astrosoft.ui.view.HoroscopeDashboard;

/**
 * Layout of the Horoscope screen with the left column expanded and collapsed.
 * Uses plain panels of the real sizes, so no horoscope or preferences are needed.
 * Run with a display (NOT headless).
 */
public class HoroscopeDashboardTest {

	private static JPanel box(String name, int w, int h) {
		JPanel p = new JPanel();
		p.setName(name);
		p.setPreferredSize(new Dimension(w, h));
		p.setMaximumSize(new Dimension(w, h));
		return p;
	}

	private final JPanel info = box("info", 530, 300);
	private final JPanel positions = box("positions", 530, 184);
	private final JPanel shadbala = box("shadbala", 530, 218);
	private final JPanel charts = box("charts", 858, 365);
	private final JPanel ashtavarga = box("ashtavarga", 858, 405);
	private final List<Boolean> saved = new ArrayList<Boolean>();

	private HoroscopeDashboard dashboard(boolean collapsed) {
		return new HoroscopeDashboard(info, positions, shadbala, charts, ashtavarga, collapsed,
				c -> saved.add(c));
	}

	private static boolean contains(Container root, Component c) {
		return c.getParent() != null && SwingUtilities.isDescendingFrom(c, root);
	}

	/** Index of c among the components of its column, used to check vertical order. */
	private static int order(Component c) {
		Container parent = c.getParent();
		for (int i = 0; i < parent.getComponentCount(); i++) {
			if (parent.getComponent(i) == c) return i;
		}
		return -1;
	}

	@Test
	public void expandedKeepsTodaysLayout() {
		HoroscopeDashboard d = dashboard(false);
		assertFalse(d.isCollapsed());
		assertEquals(530 + 40 + 858, d.getPreferredSize().width);
		assertTrue(contains(d, info));
		assertTrue("Planetary Positions is checked by default", contains(d, positions));
		assertFalse("Shadbala is unchecked by default", contains(d, shadbala));
		assertSame("controls sit in the left column", info.getParent(), d.getControlsPanel().getParent());
		assertTrue("points left (collapse)", d.getToggleButton().isPointingLeft());
		assertEquals("icon only, no text", "", d.getToggleButton().getText());
	}

	@Test
	public void collapsedFreesTheLeftColumnWidth() {
		HoroscopeDashboard d = dashboard(true);
		assertTrue(d.isCollapsed());
		assertEquals(858, d.getPreferredSize().width);
		assertFalse("Profile card is hidden", contains(d, info));
		assertSame("controls move to the chart column", charts.getParent(), d.getControlsPanel().getParent());
		assertTrue("controls row is above the charts", order(d.getControlsPanel()) < order(charts));
		assertFalse("points right (expand)", d.getToggleButton().isPointingLeft());
	}

	@Test
	public void collapsedStacksAshtavargaThenShadbalaThenPositions() {
		HoroscopeDashboard d = dashboard(true);
		d.getAshtavargaCheck().doClick();
		d.getShadbalaCheck().doClick();
		assertTrue(contains(d, ashtavarga));
		assertTrue(contains(d, shadbala));
		assertTrue(contains(d, positions));
		assertSame(charts.getParent(), shadbala.getParent());
		assertSame(charts.getParent(), positions.getParent());
		assertTrue(order(charts) < order(ashtavarga));
		assertTrue(order(ashtavarga) < order(shadbala));
		assertTrue(order(shadbala) < order(positions));
		assertEquals("stacking never widens past the charts", 858, d.getPreferredSize().width);
	}

	@Test
	public void checkboxesStillWorkWhenCollapsed() {
		HoroscopeDashboard d = dashboard(true);
		d.getPositionsCheck().doClick();
		assertFalse(contains(d, positions));
		d.getShadbalaCheck().doClick();
		assertTrue(contains(d, shadbala));
	}

	@Test
	public void toggleButtonFlipsStateAndReportsItForSaving() {
		HoroscopeDashboard d = dashboard(false);
		d.getToggleButton().doClick();
		assertTrue(d.isCollapsed());
		assertEquals(858, d.getPreferredSize().width);
		d.getToggleButton().doClick();
		assertFalse(d.isCollapsed());
		assertTrue("Profile card comes back", contains(d, info));
		assertEquals(530 + 40 + 858, d.getPreferredSize().width);
		assertEquals(List.of(true, false), saved);
	}

	@Test
	public void toggleIsALargeButtonWithTooltip() {
		HoroscopeDashboard d = dashboard(false);
		Dimension size = d.getToggleButton().getPreferredSize();
		assertTrue("wide enough for a large icon: " + size, size.width >= 36 && size.height >= 24);
		assertTrue(d.getToggleButton().getToolTipText().contains("Notes"));
		d.getToggleButton().doClick();
		assertTrue(d.getToggleButton().getToolTipText().contains("Show"));
	}

	@Test
	public void expandingKeepsCardsChosenWhileCollapsed() {
		HoroscopeDashboard d = dashboard(true);
		d.getShadbalaCheck().doClick();
		d.setCollapsed(false);
		assertSame("Shadbala returns to the left column", info.getParent(), shadbala.getParent());
		assertSame(info.getParent(), positions.getParent());
	}
}
