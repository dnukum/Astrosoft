/**
 * HousePACCalculatorTest.java
 *
 * Headless unit test verifying HousePACCalculator and O(1) static lookup
 * tables in AstroConsts across sample horoscopes and test fixtures.
 */
package app.astrosoft.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import app.astrosoft.beans.HousePACData;
import app.astrosoft.beans.HousePACData.AspectInfo;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.AstroConsts;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.core.HousePACCalculator;
import app.astrosoft.test.SampleHoroscopes;

public class HousePACCalculatorTest {

	private Horoscope horoscope;

	@Before
	public void setUp() {
		horoscope = SampleHoroscopes.getMyHoroscope();
	}

	@Test
	public void testStaticAspectTables() {
		// Sun (ordinal 0): 7th aspect only
		assertEquals("7th aspect", AstroConsts.getAspectName(Planet.Sun, 1, 7));
		assertNull(AstroConsts.getAspectName(Planet.Sun, 1, 4));

		// Mars (ordinal 2): 4th, 7th, 8th aspects
		// Mars in H10 -> aspects H1 (4th), H4 (7th), H5 (8th)
		assertEquals("4th aspect", AstroConsts.getAspectName(Planet.Mars, 10, 1));
		assertEquals("7th aspect", AstroConsts.getAspectName(Planet.Mars, 10, 4));
		assertEquals("8th aspect", AstroConsts.getAspectName(Planet.Mars, 10, 5));
		assertNull(AstroConsts.getAspectName(Planet.Mars, 10, 2));

		// Jupiter (ordinal 4): 5th, 7th, 9th aspects
		// Jupiter in H2 -> aspects H6 (5th), H8 (7th), H10 (9th)
		assertEquals("5th aspect", AstroConsts.getAspectName(Planet.Jupiter, 2, 6));
		assertEquals("7th aspect", AstroConsts.getAspectName(Planet.Jupiter, 2, 8));
		assertEquals("9th aspect", AstroConsts.getAspectName(Planet.Jupiter, 2, 10));
		assertNull(AstroConsts.getAspectName(Planet.Jupiter, 2, 7));

		// Saturn (ordinal 6): 3rd, 7th, 10th aspects
		// Saturn in H1 -> aspects H3 (3rd), H7 (7th), H10 (10th)
		assertEquals("3rd aspect", AstroConsts.getAspectName(Planet.Saturn, 1, 3));
		assertEquals("7th aspect", AstroConsts.getAspectName(Planet.Saturn, 1, 7));
		assertEquals("10th aspect", AstroConsts.getAspectName(Planet.Saturn, 1, 10));
		assertNull(AstroConsts.getAspectName(Planet.Saturn, 1, 4));

		// Rahu & Ketu: 5th, 7th, 9th aspects
		assertEquals("5th aspect", AstroConsts.getAspectName(Planet.Rahu, 1, 5));
		assertEquals("7th aspect", AstroConsts.getAspectName(Planet.Rahu, 1, 7));
		assertEquals("9th aspect", AstroConsts.getAspectName(Planet.Rahu, 1, 9));
		assertEquals("5th aspect", AstroConsts.getAspectName(Planet.Ketu, 4, 8));
		assertEquals("7th aspect", AstroConsts.getAspectName(Planet.Ketu, 4, 10));
		assertEquals("9th aspect", AstroConsts.getAspectName(Planet.Ketu, 4, 12));
	}

	@Test
	public void testKendraTrikonaYogaMatrix() {
		// House 1 (Kendra & Trikona) with House 4 (Kendra) -> Raja Yoga (1 is Trikona, 4 is Kendra)
		assertTrue(AstroConsts.isKendraTrikonaPair(1, 4));
		assertTrue(AstroConsts.isKendraTrikonaPair(4, 1));

		// House 1 with House 5 -> Raja Yoga (1 is Kendra, 5 is Trikona)
		assertTrue(AstroConsts.isKendraTrikonaPair(1, 5));
		assertTrue(AstroConsts.isKendraTrikonaPair(5, 1));

		// House 4 (Kendra) with House 5 (Trikona) -> Raja Yoga
		assertTrue(AstroConsts.isKendraTrikonaPair(4, 5));
		assertTrue(AstroConsts.isKendraTrikonaPair(5, 4));

		// House 9 (Trikona) with House 10 (Kendra) -> Raja Yoga
		assertTrue(AstroConsts.isKendraTrikonaPair(9, 10));
		assertTrue(AstroConsts.isKendraTrikonaPair(10, 9));

		// House 4 with House 7 (both Kendras) -> not Kendra-Trikona
		assertFalse(AstroConsts.isKendraTrikonaPair(4, 7));

		// House 5 with House 9 (both Trikonas) -> not Kendra-Trikona
		assertFalse(AstroConsts.isKendraTrikonaPair(5, 9));

		// House with itself
		assertFalse(AstroConsts.isKendraTrikonaPair(1, 1));
	}

	@Test
	public void testCalculateAllHouses() {
		List<HousePACData> allHouses = HousePACCalculator.calculateAll(horoscope);
		assertNotNull(allHouses);
		assertEquals(12, allHouses.size());

		for (int i = 0; i < 12; i++) {
			HousePACData data = allHouses.get(i);
			assertEquals(i + 1, data.getHouseNum());
			assertNotNull(data.getRasi());
			assertNotNull(data.getLord());
			assertNotNull(data.getClassifications());
			assertNotNull(data.getLordPlacement());
			assertNotNull(data.getOccupants());
			assertNotNull(data.getAspects());

			String plainText = data.toPlainText();
			assertNotNull(plainText);
			assertTrue(plainText.contains("HOUSE " + (i + 1)));
			assertTrue(plainText.contains("[P] PLACEMENT"));
			assertTrue(plainText.contains("[A] ASPECTS"));
			assertTrue(plainText.contains("[C] CONJUNCTIONS & YOGAS"));
		}
	}

	@Test
	public void testAspectSortingOrder() {
		// Verify aspects are sorted: Benefics/Yogakaraka -> Neutrals -> Malefics
		List<HousePACData> allHouses = HousePACCalculator.calculateAll(horoscope);
		for (HousePACData data : allHouses) {
			int lastPriority = -1;
			for (AspectInfo asp : data.getAspects()) {
				assertTrue("Aspects must be sorted by benefic/neutral/malefic priority",
						asp.getSortPriority() >= lastPriority);
				lastPriority = asp.getSortPriority();
			}
		}
	}

	@Test
	public void testPerformanceO1Lookups() {
		// Benchmark 10,000 house PAC calculations to verify high throughput O(1) performance
		long start = System.nanoTime();
		for (int i = 0; i < 10000; i++) {
			int h = (i % 12) + 1;
			HousePACCalculator.calculatePAC(horoscope, h);
		}
		long elapsedNs = System.nanoTime() - start;
		double elapsedMs = elapsedNs / 1_000_000.0;
		System.out.println("10,000 HousePAC calculations elapsed: " + elapsedMs + " ms (" + (elapsedMs / 10000.0) + " ms per call)");
		assertTrue("10,000 calculations must run in under 500ms on local JVM", elapsedMs < 500);
	}

	@Test
	public void testConjunctionsAndYogasStrictlyScopedToHouse() {
		List<HousePACData> allHouses = HousePACCalculator.calculateAll(horoscope);
		for (HousePACData data : allHouses) {
			if (data.getOccupants().size() < 2) {
				assertTrue("House " + data.getHouseNum() + " has " + data.getOccupants().size()
						+ " occupants, so conjunctions must be empty", data.getConjunctions().isEmpty());
				assertTrue("House " + data.getHouseNum() + " has " + data.getOccupants().size()
						+ " occupants, so yogas must be empty", data.getYogas().isEmpty());
			} else {
				assertFalse("House " + data.getHouseNum() + " has >= 2 occupants, so conjunctions must be listed",
						data.getConjunctions().isEmpty());
				for (String c : data.getConjunctions()) {
					assertTrue("Conjunction must describe conjoined planets", c.contains("conjoined"));
					assertTrue("Conjunction must mention English Rasi " + data.getRasi().englishName(),
							c.contains(data.getRasi().englishName()));
				}
				for (String y : data.getYogas()) {
					assertTrue("Yoga must occur in House " + data.getHouseNum(),
							y.contains("in House " + data.getHouseNum() + "]"));
				}
			}
		}
	}
}
