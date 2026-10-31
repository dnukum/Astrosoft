package app.astrosoft.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;

import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.PlanetStatusCalculator;

/**
 * Vargottama: a planet occupying the same sign in Rasi (D1) and Navamsa (D9).
 * Only the seven classical planets qualify; nodes, Ascendant and combined node never do.
 * This test covers the sign-pair rule only; the degree-to-Navamsa mapping is verified
 * separately against VargaCharts.
 */
public class PlanetVargottamaTest {

	private static final Set<Planet> CLASSICAL = EnumSet.of(Planet.Sun, Planet.Moon, Planet.Mars,
			Planet.Mercury, Planet.Jupiter, Planet.Venus, Planet.Saturn);

	@Test
	public void sameSignInD1AndD9IsVargottamaForAllClassicalPlanets() {
		int hits = 0;
		for (Planet p : CLASSICAL) {
			for (Rasi r : Rasi.values()) {
				assertTrue(p.name() + " in " + r.name(), PlanetStatusCalculator.isVargottama(p, r, r));
				hits++;
			}
		}
		assertEquals(84, hits);
	}

	@Test
	public void differentSignsAreNeverVargottama() {
		int checked = 0;
		for (Planet p : CLASSICAL) {
			for (Rasi d1 : Rasi.values()) {
				for (Rasi d9 : Rasi.values()) {
					if (d1 == d9) continue;
					assertFalse(p.name() + " " + d1.name() + "/" + d9.name(),
							PlanetStatusCalculator.isVargottama(p, d1, d9));
					checked++;
				}
			}
		}
		assertEquals(7 * 132, checked);
	}

	@Test
	public void nodesAscendantAndCombinedNodeAreNeverVargottama() {
		Planet[] none = {Planet.Rahu, Planet.Ketu, Planet.Ascendant, Planet.Ra_Ke};
		for (Planet p : none) {
			for (Rasi r : Rasi.values()) {
				assertFalse(p.name() + " in " + r.name(), PlanetStatusCalculator.isVargottama(p, r, r));
			}
		}
	}

	@Test
	public void knownCases() {
		assertTrue(PlanetStatusCalculator.isVargottama(Planet.Sun, Rasi.Mesha, Rasi.Mesha));
		assertFalse(PlanetStatusCalculator.isVargottama(Planet.Sun, Rasi.Mesha, Rasi.Simha));
		assertTrue(PlanetStatusCalculator.isVargottama(Planet.Moon, Rasi.Kataka, Rasi.Kataka));
		assertFalse(PlanetStatusCalculator.isVargottama(Planet.Moon, Rasi.Kataka, Rasi.Vrichika));
	}

	@Test
	public void nullArgumentsAreRejected() {
		try {
			PlanetStatusCalculator.isVargottama(null, Rasi.Mesha, Rasi.Mesha);
			fail("null planet");
		} catch (IllegalArgumentException expected) {
			// ok
		}
		try {
			PlanetStatusCalculator.isVargottama(Planet.Sun, null, Rasi.Mesha);
			fail("null D1 sign");
		} catch (IllegalArgumentException expected) {
			// ok
		}
		try {
			PlanetStatusCalculator.isVargottama(Planet.Sun, Rasi.Mesha, null);
			fail("null D9 sign");
		} catch (IllegalArgumentException expected) {
			// ok
		}
	}
}
