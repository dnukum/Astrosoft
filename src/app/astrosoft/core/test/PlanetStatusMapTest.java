package app.astrosoft.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.EnumMap;

import org.junit.Test;

import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.consts.Dignity;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.PlanetStatusCalculator;

public class PlanetStatusMapTest {

	@Test
	public void signsOfConvertsOneBasedNumbers() {
		EnumMap<Planet, Integer> nums = new EnumMap<Planet, Integer>(Planet.class);
		nums.put(Planet.Sun, 1);
		nums.put(Planet.Moon, 4);
		nums.put(Planet.Mars, 12);
		EnumMap<Planet, Rasi> signs = PlanetStatusCalculator.signsOf(nums);
		assertEquals(Rasi.Mesha, signs.get(Planet.Sun));
		assertEquals(Rasi.Kataka, signs.get(Planet.Moon));
		assertEquals(Rasi.Meena, signs.get(Planet.Mars));
		assertEquals(3, signs.size());
	}

	@Test
	public void signsOfRejectsOutOfRangeAndNull() {
		int[] bad = {0, 13, -1};
		for (int b : bad) {
			EnumMap<Planet, Integer> nums = new EnumMap<Planet, Integer>(Planet.class);
			nums.put(Planet.Sun, b);
			try {
				PlanetStatusCalculator.signsOf(nums);
				fail("expected IllegalArgumentException for " + b);
			} catch (IllegalArgumentException expected) {
				// ok
			}
		}
		try {
			PlanetStatusCalculator.signsOf(null);
			fail("null map");
		} catch (IllegalArgumentException expected) {
			// ok
		}
	}

	@Test
	public void statusMapCombinesD1AndD9() {
		EnumMap<Planet, Rasi> d1 = new EnumMap<Planet, Rasi>(Planet.class);
		EnumMap<Planet, Rasi> d9 = new EnumMap<Planet, Rasi>(Planet.class);
		d1.put(Planet.Sun, Rasi.Mesha);     d9.put(Planet.Sun, Rasi.Mesha);
		d1.put(Planet.Moon, Rasi.Kataka);   d9.put(Planet.Moon, Rasi.Vrichika);
		d1.put(Planet.Rahu, Rasi.Mesha);    d9.put(Planet.Rahu, Rasi.Mesha);
		d1.put(Planet.Ascendant, Rasi.Simha); d9.put(Planet.Ascendant, Rasi.Simha);
		EnumMap<Planet, PlanetStatus> m = PlanetStatusCalculator.statusMap(d1, d9);
		assertEquals(new PlanetStatus(Dignity.EXALTED, true), m.get(Planet.Sun));
		assertEquals(new PlanetStatus(Dignity.OWN, false), m.get(Planet.Moon));
		assertTrue(m.get(Planet.Rahu).isEmpty());
		assertTrue(m.get(Planet.Ascendant).isEmpty());
		assertEquals(4, m.size());
	}

	@Test
	public void planetMissingFromD9KeepsDignityButIsNotVargottama() {
		EnumMap<Planet, Rasi> d1 = new EnumMap<Planet, Rasi>(Planet.class);
		EnumMap<Planet, Rasi> d9 = new EnumMap<Planet, Rasi>(Planet.class);
		d1.put(Planet.Jupiter, Rasi.Kataka);
		PlanetStatus s = PlanetStatusCalculator.statusMap(d1, d9).get(Planet.Jupiter);
		assertEquals(Dignity.EXALTED, s.getDignity());
		assertFalse(s.isVargottama());
	}

	@Test
	public void nullMapsRejected() {
		try {
			PlanetStatusCalculator.statusMap(null, new EnumMap<Planet, Rasi>(Planet.class));
			fail("null d1");
		} catch (IllegalArgumentException expected) {
			// ok
		}
		try {
			PlanetStatusCalculator.statusMap(new EnumMap<Planet, Rasi>(Planet.class), null);
			fail("null d9");
		} catch (IllegalArgumentException expected) {
			// ok
		}
	}
}
