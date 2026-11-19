package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.junit.Test;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Karana;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Thithi;
import app.astrosoft.consts.WeekDay;
import app.astrosoft.consts.Yoga;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.Panchang;
import app.astrosoft.ui.comp.Chart;
import app.astrosoft.ui.table.AstrosoftTable;
import app.astrosoft.ui.view.PanchangView;

/**
 * Verifies Tier 1/2 Panchang fields, Muhurtha additions, exact-instant calculation,
 * .ash birth chart loading, and the Today button on PanchangView.
 */
public class PanchangChartAndTodayTest {

	@Test
	public void testConstsLordsAndNityaDevi() {
		assertEquals(Planet.Saturn, WeekDay.Saturday.getLord());
		assertEquals(Planet.Moon, Thithi.Dvithiyai.getLord());
		assertEquals("Bhaga Maalini", Thithi.Dvithiyai.getNityaDevi());
		assertEquals(Planet.Sun, Yoga.Subha.getLord());
		assertEquals(Planet.Mercury, Karana.Taitula.getLord());
	}

	@Test
	public void testBirthChartApplyAndTodayButton() {
		Horoscope h = Horoscope.createFromFile("tests/charts/sample_chart_1.ash");
		assertNotNull("Sample chart must load", h);
		BirthData bd = h.getBirthData();
		assertNotNull("BirthData must be present", bd);

		Panchang initial = new Panchang(new Date());
		PanchangView view = new PanchangView(initial, new Point(0, 0));

		// Apply birth chart
		view.applyBirthChart(bd);

		AstrosoftTable table = findFirst(view, AstrosoftTable.class);
		assertNotNull("Panchanga table must exist", table);
		assertEquals(5, table.getRowCount());
		assertEquals(4, table.getColumnCount());
		assertEquals("Vara (Solar)", table.getValueAt(0, 0));
		assertEquals(DisplayStrings.THITHI_STR.toString(), table.getValueAt(1, 0));
		assertTrue("Tithi % left formatted", table.getValueAt(1, 2).toString().endsWith("%"));
		assertTrue("Context label shows Chart", hasLabelContaining(view, "Active: Chart — " + bd.name()));
		assertTrue("Top card title shows Chart Details", hasLabelContaining(view, "Chart Details — " + bd.name()));

		// Verify exact-instant Panchang properties directly
		Panchang birthPan = new Panchang(bd.birthDay().getTime(), bd.getBirthPlace(), true);
		assertTrue(birthPan.isExactInstant());
		assertTrue(birthPan.getThithiPercentLeft() > 0 && birthPan.getThithiPercentLeft() <= 100.0);
		assertTrue(birthPan.getNakshathraPercentLeft() > 0 && birthPan.getNakshathraPercentLeft() <= 100.0);
		assertTrue(birthPan.getYogaPercentLeft() > 0 && birthPan.getYogaPercentLeft() <= 100.0);
		assertTrue(birthPan.getKaranaPercentLeft() > 0 && birthPan.getKaranaPercentLeft() <= 100.0);
		assertNotNull(birthPan.getAmirthathiYoga());
		assertFalse(birthPan.getAbhijitMuhurta().isEmpty());
		assertFalse(birthPan.getBrahmaMuhurta().isEmpty());

		// Verify modern date/time picker buttons update cleanly
		view.applyDateAndTime(LocalDate.of(2026, 11, 19), LocalTime.of(14, 30));
		assertNotNull("Date picker button must show Nov 19, 2026", findButton(view, "Nov 19, 2026"));
		assertNotNull("Time picker button must show 02:30 PM", findButton(view, "02:30 PM"));

		// Click Today button
		JButton todayBtn = findButton(view, "Today");
		assertNotNull("Today button must exist", todayBtn);
		todayBtn.doClick();
		assertTrue("Context label switches to Today (Live)", hasLabelContaining(view, "Active: Today (Live)"));
		assertTrue("Top card title switches to Current Date/Time", hasLabelContaining(view, "Current Date/Time"));

		// Verify chart presence, unclipped toolbar controls, exact table row heights/zero gap, and offscreen rendering
		assertNotNull("Rasi chart must exist", findFirst(view, Chart.class));
		view.setSize(990, 600);
		layoutTree(view);
		assertEquals("Tithi row height must remain 44px after refresh", 44, table.getRowHeight(1));
		int sumRowHeights = 0;
		for (int r = 0; r < table.getRowCount(); r++) {
			sumRowHeights += table.getRowHeight(r);
		}
		assertEquals("Table height must equal sum of row heights (0px bottom gap)", sumRowHeights, table.getHeight());
		assertTrue("Today button must not be clipped vertically (h=" + todayBtn.getHeight() + ")", todayBtn.getHeight() >= 28);
		JButton loadBtn = findButton(view, "Load Chart (.ash)...");
		assertNotNull("Load Chart button must exist", loadBtn);
		assertTrue("Load Chart button must not be clipped vertically (h=" + loadBtn.getHeight() + ")", loadBtn.getHeight() >= 28);
		BufferedImage img = new BufferedImage(990, 600, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();
		try {
			view.paint(g2);
		} finally {
			g2.dispose();
		}
	}

	private static void layoutTree(Container root) {
		root.doLayout();
		for (Component c : root.getComponents()) {
			if (c instanceof Container) {
				layoutTree((Container) c);
			}
		}
	}

	@Test
	public void testNextDayNakshatraEndUsesTraditional24PlusHours() {
		// Nov 19, 2026 06:57 AM IST in Mumbai: Purva Bhadrapada ends Nov 20 ~06:56 AM (<24h elapsed, next calendar day)
		TimeZone ist = TimeZone.getTimeZone("Asia/Kolkata");
		Calendar cal = new GregorianCalendar(ist);
		cal.set(2026, Calendar.NOVEMBER, 19, 6, 57, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Place mumbai = new Place("Mumbai", "", "India", 19.0760, 72.8777, "Asia/Kolkata");
		Panchang pan = new Panchang(cal.getTime(), mumbai, true);
		assertTrue("Next-day nakshatra end must stay >= 24.0 in traditional style (was " + pan.getNakshathraEnd() + ")",
				pan.getNakshathraEnd() >= 24.0);

		PanchangView mumbaiView = new PanchangView(pan, new Point(0, 0));
		AstrosoftTable mumbaiTable = findFirst(mumbaiView, AstrosoftTable.class);
		assertNotNull(mumbaiTable);
		String nakEndCell = mumbaiTable.getValueAt(2, 3).toString();
		assertTrue("Next-day nakshatra End must format as AM/PM + (Fri), got: " + nakEndCell,
				nakEndCell.contains("AM (Fri)"));
		String yogaEndCell = mumbaiTable.getValueAt(3, 3).toString();
		assertTrue("Same-day Yoga End must format as AM/PM, got: " + yogaEndCell,
				yogaEndCell.endsWith("AM") || yogaEndCell.endsWith("PM"));
	}

	private static JButton findButton(Container root, String text) {
		for (Component c : root.getComponents()) {
			if (c instanceof JButton && text.equals(((JButton) c).getText())) {
				return (JButton) c;
			}
			if (c instanceof Container) {
				JButton found = findButton((Container) c, text);
				if (found != null) return found;
			}
		}
		return null;
	}

	private static boolean hasLabelContaining(Container root, String needle) {
		for (Component c : root.getComponents()) {
			if (c instanceof JLabel) {
				String t = ((JLabel) c).getText();
				if (t != null && t.contains(needle)) {
					return true;
				}
			}
			if (c instanceof Container && hasLabelContaining((Container) c, needle)) {
				return true;
			}
		}
		return false;
	}

	private static <T extends Component> T findFirst(Container root, Class<T> cls) {
		for (Component c : root.getComponents()) {
			if (cls.isInstance(c)) {
				return cls.cast(c);
			}
			if (c instanceof Container) {
				T found = findFirst((Container) c, cls);
				if (found != null) return found;
			}
		}
		return null;
	}
}
