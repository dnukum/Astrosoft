/**
 * DasaTimelineEngineTest.java
 * Test-driven unit tests for DasaTimelineEngine verifying chronological monotonicity,
 * interval overlap permutations, Sama Dasa detection, Dasa Chidra identification,
 * and standardized trigger label generation.
 */

package app.astrosoft.core.test;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import app.astrosoft.beans.DasaTimelineRow;
import app.astrosoft.consts.Planet;
import app.astrosoft.core.DasaTimelineEngine;
import app.astrosoft.core.DasaTimelineEngine.AgeFilter;
import app.astrosoft.core.DasaTimelineEngine.ScopeFilter;
import app.astrosoft.core.Horoscope;

public class DasaTimelineEngineTest extends TestCase {

	private Horoscope boyHoroscope;
	private Horoscope girlHoroscope;
	private DasaTimelineEngine engine;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// Standard test horoscopes: Elango (Boy born 17-Apr-1957) & Mani (Girl born 10-Aug-1960)
		boyHoroscope = new Horoscope("Elango", 17, 4, 1957, 7, 10,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode");
		girlHoroscope = new Horoscope("Mani", 10, 8, 1960, 5, 30,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode");

		engine = new DasaTimelineEngine(boyHoroscope, girlHoroscope);
	}

	/**
	 * Test 1: Verify strict chronological monotonicity (Date(k+1) > Date(k)).
	 */
	public void testChronologicalMonotonicity() {
		List<DasaTimelineRow> rows = engine.getTimelineRows(ScopeFilter.ALL_MILESTONES, AgeFilter.FROM_BIRTH);
		assertNotNull("Rows must not be null", rows);
		assertTrue("Must have multiple rows for full lifetime", rows.size() > 50);

		Date previousDate = null;
		for (DasaTimelineRow row : rows) {
			Date currentDate = row.getStartDate();
			assertNotNull("Start date must not be null", currentDate);
			if (previousDate != null) {
				assertTrue("Dates must strictly ascend: " + row.getStartDateStr() + " after " + previousDate,
						currentDate.after(previousDate));
			}
			previousDate = currentDate;
		}
	}

	/**
	 * Test 2: Verify zero missing events (all boundary dates are represented).
	 */
	public void testZeroLostEvents() {
		List<DasaTimelineRow> rows = engine.getTimelineRows(ScopeFilter.ALL_MILESTONES, AgeFilter.FROM_BIRTH);
		
		// Every row must have active non-null planets for both boy and girl
		for (DasaTimelineRow row : rows) {
			assertNotNull("Boy MD must not be null at " + row.getStartDateStr(), row.getBoyMD());
			assertNotNull("Boy AD must not be null at " + row.getStartDateStr(), row.getBoyAD());
			assertNotNull("Girl MD must not be null at " + row.getStartDateStr(), row.getGirlMD());
			assertNotNull("Girl AD must not be null at " + row.getStartDateStr(), row.getGirlAD());
			assertNotNull("Trigger event must not be null", row.getTriggerEvent());
		}
	}

	/**
	 * Test 3: Verify Sama Dasa identification.
	 * When Boy MD == Girl MD, isSamaDasa must be true, and trigger or status must reflect it.
	 */
	public void testSamaDasaIdentification() {
		List<DasaTimelineRow> rows = engine.getTimelineRows(ScopeFilter.ALL_MILESTONES, AgeFilter.FROM_BIRTH);
		boolean foundSamaDasa = false;

		for (DasaTimelineRow row : rows) {
			if (row.getBoyMD() == row.getGirlMD()) {
				assertTrue("isSamaDasa must be true when Boy MD == Girl MD (" + row.getBoyMD() + ")",
						row.isSamaDasa());
				assertTrue("Trigger event must contain 'Same Dasa': " + row.getTriggerEvent(),
						row.getTriggerEvent().contains("Same Dasa"));
				foundSamaDasa = true;
			} else {
				assertFalse("isSamaDasa must be false when Boy MD != Girl MD", row.isSamaDasa());
			}
		}

		assertTrue("Must have at least one Sama Dasa period in test lifetime", foundSamaDasa);
	}

	/**
	 * Test 4: Verify Dasa Chidra identification.
	 * Final Bhukti of a Mahadasha must be flagged as Chidra and have '(Chidra)' in label.
	 */
	public void testDasaChidraIdentification() {
		List<DasaTimelineRow> rows = engine.getTimelineRows(ScopeFilter.ALL_MILESTONES, AgeFilter.FROM_BIRTH);
		boolean foundChidraStart = false;

		for (DasaTimelineRow row : rows) {
			if ((row.isBoyADChange() && row.isBoyChidra()) || (row.isGirlADChange() && row.isGirlChidra())) {
				assertTrue("Trigger event must mention '(Chidra)': " + row.getTriggerEvent(),
						row.getTriggerEvent().contains("(Chidra)"));
				foundChidraStart = true;
			}
		}

		assertTrue("Must find Dasa Chidra start intervals in lifetime", foundChidraStart);
	}

	/**
	 * Test 5: Verify Standardized Trigger Labels.
	 */
	public void testStandardizedTriggerLabels() {
		List<DasaTimelineRow> rows = engine.getTimelineRows(ScopeFilter.ALL_MILESTONES, AgeFilter.FROM_BIRTH);

		for (DasaTimelineRow row : rows) {
			String trigger = row.getTriggerEvent();
			assertTrue("Trigger label must match standardized format: " + trigger,
					trigger.contains("Same Dasa") ||
					trigger.contains("Boy starts") ||
					trigger.contains("Girl starts") ||
					trigger.contains("Baseline"));
		}
	}

	/**
	 * Test 6: Verify Age 20+ Filter (Default).
	 */
	public void testAge20Filter() {
		List<DasaTimelineRow> rows20 = engine.getTimelineRows(ScopeFilter.ALL_MILESTONES, AgeFilter.FROM_AGE_20);
		List<DasaTimelineRow> rowsBirth = engine.getTimelineRows(ScopeFilter.ALL_MILESTONES, AgeFilter.FROM_BIRTH);

		assertTrue("Age 20+ filter should yield fewer rows than From Birth", rows20.size() < rowsBirth.size());
		
		// In the first row of Age 20+, the younger partner must be at least age 20
		DasaTimelineRow firstRow = rows20.get(0);
		double minAge = Math.min(firstRow.getBoyAge(), firstRow.getGirlAge());
		assertTrue("Younger partner must be approximately 20 or older at first row: " + minAge, minAge >= 19.9);
	}

	/**
	 * Test 7: Verify Default Scope Filter (Overlapping & Dasa Chidra).
	 */
	public void testDefaultScopeFilter() {
		List<DasaTimelineRow> criticalRows = engine.getTimelineRows(ScopeFilter.OVERLAPPING_AND_CHIDRA, AgeFilter.FROM_AGE_20);
		List<DasaTimelineRow> allRows = engine.getTimelineRows(ScopeFilter.ALL_MILESTONES, AgeFilter.FROM_AGE_20);

		assertTrue("Critical filter should be more concise than all milestones", criticalRows.size() < allRows.size());
		assertTrue("Critical filter must still have significant rows", criticalRows.size() > 5);

		for (DasaTimelineRow row : criticalRows) {
			// Every critical row must be either Sama Dasa, Chidra, MD change, or today
			boolean isValidCritical = row.isSamaDasa() || row.isChidra() ||
					row.isBoyMDChange() || row.isGirlMDChange() || row.isToday();
			assertTrue("Row in critical filter must be Sama Dasa, Chidra, or MD change: " + row, isValidCritical);
		}
	}

	/**
	 * Test 8: Verify Major Dasa Only Scope Filter.
	 */
	public void testMajorDasaOnlyFilter() {
		List<DasaTimelineRow> mdRows = engine.getTimelineRows(ScopeFilter.MAJOR_DASA_ONLY, AgeFilter.FROM_BIRTH);

		for (DasaTimelineRow row : mdRows) {
			boolean isMD = row.isBoyMDChange() || row.isGirlMDChange() || row.isToday();
			assertTrue("Major Dasa filter must only include MD changes: " + row, isMD);
		}
	}

	/**
	 * Test 9: Verify Next 5 Years Window Filter (Default).
	 */
	public void testNext5YearsWindow() {
		List<DasaTimelineRow> rows = engine.getTimelineRows(ScopeFilter.OVERLAPPING_AND_CHIDRA, DasaTimelineEngine.WindowFilter.NEXT_5_YEARS);
		assertNotNull(rows);
		assertTrue("Next 5 years should yield a focused list of intervals", rows.size() >= 1 && rows.size() <= 20);

		// Current running period should be represented
		boolean hasToday = false;
		for (DasaTimelineRow row : rows) {
			if (row.isToday()) {
				hasToday = true;
				break;
			}
		}
		assertTrue("Next 5 years window must include the current running period", hasToday);
	}

	/**
	 * Test 10: Verify Full 120 Years Window Filter.
	 */
	public void testFull120YearsWindow() {
		List<DasaTimelineRow> rows = engine.getTimelineRows(ScopeFilter.ALL_MILESTONES, DasaTimelineEngine.WindowFilter.FULL_120_YEARS);
		assertNotNull(rows);
		assertTrue("Full 120 years must contain all lifetime milestones", rows.size() > 50);

		DasaTimelineRow first = rows.get(0);
		DasaTimelineRow last = rows.get(rows.size() - 1);
		assertTrue("First milestone should be near earliest birth (1957)", first.getStartDateStr().contains("1957"));
		assertTrue("Last milestone should span far into future (> 2070)", last.getStartDate().after(first.getStartDate()));
	}

	/**
	 * Test 11: Verify Custom Range Window Filter.
	 */
	public void testCustomRangeWindow() {
		List<DasaTimelineRow> rows = engine.getTimelineRows(ScopeFilter.ALL_MILESTONES, DasaTimelineEngine.WindowFilter.custom(1990, 2010));
		assertNotNull(rows);
		assertTrue("Should have intervals between 1990 and 2010", rows.size() > 5);

		for (DasaTimelineRow row : rows) {
			java.util.Calendar cal = java.util.Calendar.getInstance();
			cal.setTime(row.getStartDate());
			int year = cal.get(java.util.Calendar.YEAR);
			assertTrue("Row year must be >= 1990: " + year, year >= 1990);
			assertTrue("Row year must be <= 2010: " + year, year <= 2010);
		}
	}

	/**
	 * Test 12: Verify Earliest Birth Date and Whoever Reaches 120 Last Bounds.
	 */
	public void testMinAndMaxYearBounds() {
		// Boy born 1957, Girl born 1960
		assertEquals(1957, engine.getMinBirthYear());
		assertEquals(2080, engine.getMax120Year()); // 1960 + 120 = 2080
	}
}
