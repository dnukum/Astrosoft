/**
 * SingleDasaTimelineEngineTest.java
 * Unit tests for SingleDasaTimelineEngine verifying hierarchical 3-level (MD/AD/PD)
 * tree extraction, duration formatting, current-period auto-expansion, and window filtering.
 */

package app.astrosoft.core.test;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import app.astrosoft.beans.SingleDasaTimelineRow;
import app.astrosoft.core.DasaTimelineEngine.WindowFilter;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.SingleDasaTimelineEngine;

public class SingleDasaTimelineEngineTest extends TestCase {

	private Horoscope sampleChart;
	private SingleDasaTimelineEngine engine;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		sampleChart = new Horoscope("SampleChart1", 17, 4, 1985, 7, 10,
				77 + (44.00 / 60.00), 11 + (22.00 / 60.00), 5.5, "Erode");
		engine = new SingleDasaTimelineEngine(sampleChart);
	}

	public void testFull120YearTreeExtractionAndHierarchy() {
		List<SingleDasaTimelineRow> roots = engine.getTimelineTree(WindowFilter.FULL_120_YEARS);
		assertNotNull("Roots must not be null", roots);
		assertTrue("Full 120-year timeline should have at least 9 MD roots", roots.size() >= 9);

		int totalPDs = 0;
		int chidraCount = 0;
		Date prevPdStart = null;

		for (SingleDasaTimelineRow md : roots) {
			assertEquals(SingleDasaTimelineRow.LEVEL_MD, md.getLevel());
			assertTrue(md.isMDChange());
			assertTrue(md.hasChildren());
			assertFalse(md.getDurationStr().isEmpty());

			for (SingleDasaTimelineRow ad : md.getChildren()) {
				assertEquals(SingleDasaTimelineRow.LEVEL_AD, ad.getLevel());
				assertTrue(ad.isADChange());
				assertTrue(ad.hasChildren());
				if (ad.isChidra()) {
					chidraCount++;
					assertTrue(ad.getMilestoneStatus().contains("Dasa Chidra"));
				}

				for (SingleDasaTimelineRow pd : ad.getChildren()) {
					totalPDs++;
					assertEquals(SingleDasaTimelineRow.LEVEL_PD, pd.getLevel());
					assertFalse(pd.hasChildren());
					assertFalse("End date must not be before start date", pd.getEndDate().before(pd.getStartDate()));
					if (prevPdStart != null) {
						assertFalse("PD start dates must be non-decreasing", pd.getStartDate().before(prevPdStart));
					}
					prevPdStart = pd.getStartDate();
				}
			}
		}

		assertTrue("Full 120-year tree should contain > 500 PDs", totalPDs > 500);
		assertTrue("Should detect Dasa Chidra Bhuktis", chidraCount >= 8);
	}

	public void testNext5YearsWindowAutoExpandsOnlyCurrentPath() {
		List<SingleDasaTimelineRow> roots = engine.getTimelineTree(WindowFilter.NEXT_5_YEARS);
		assertNotNull(roots);
		assertFalse("Next 5 years window should not be empty", roots.isEmpty());

		int currentMDs = 0;
		int currentADs = 0;
		int currentPDs = 0;

		for (SingleDasaTimelineRow md : roots) {
			assertEquals("Only current MD should start expanded", md.isCurrent(), md.isExpanded());
			if (md.isCurrent()) {
				currentMDs++;
			}
			for (SingleDasaTimelineRow ad : md.getChildren()) {
				assertEquals("Only current AD should start expanded", ad.isCurrent(), ad.isExpanded());
				if (ad.isCurrent()) {
					currentADs++;
				}
				for (SingleDasaTimelineRow pd : ad.getChildren()) {
					if (pd.isCurrent()) {
						currentPDs++;
					}
				}
			}
		}

		assertEquals("Exactly one MD should be current", 1, currentMDs);
		assertEquals("Exactly one AD should be current", 1, currentADs);
		assertEquals("Exactly one PD should be current", 1, currentPDs);
	}

	public void testCustomRangeFiltering() {
		List<SingleDasaTimelineRow> roots = engine.getTimelineTree(WindowFilter.custom(2025, 2027));
		assertNotNull(roots);
		assertFalse("Custom range 2025-2027 should not be empty", roots.isEmpty());
		assertTrue("Custom 3-year range should have <= 2 MD roots", roots.size() <= 2);
	}
}
