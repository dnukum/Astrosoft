package app.astrosoft.consts.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;

/**
 * Moolatrikona signs per B.V. Raman (Bhava and Graha Balas, Table II).
 * Sun: Leo (Simha). Sun is exalted in Aries but its moolatrikona is Leo.
 */
public class PlanetMoolaTrikonaTest {

	@Test
	public void sunMoolaTrikonaIsLeo() {
		assertEquals(Rasi.Simha, Planet.Sun.moolaTrikonaRasi());
	}

	@Test
	public void moonMoolaTrikonaIsTaurus() {
		assertEquals(Rasi.Vrishabha, Planet.Moon.moolaTrikonaRasi());
	}

	@Test
	public void marsMoolaTrikonaIsAries() {
		assertEquals(Rasi.Mesha, Planet.Mars.moolaTrikonaRasi());
	}

	@Test
	public void mercuryMoolaTrikonaIsVirgo() {
		assertEquals(Rasi.Kanya, Planet.Mercury.moolaTrikonaRasi());
	}

	@Test
	public void jupiterMoolaTrikonaIsSagittarius() {
		assertEquals(Rasi.Dhanus, Planet.Jupiter.moolaTrikonaRasi());
	}

	@Test
	public void venusMoolaTrikonaIsLibra() {
		assertEquals(Rasi.Thula, Planet.Venus.moolaTrikonaRasi());
	}

	@Test
	public void saturnMoolaTrikonaIsAquarius() {
		assertEquals(Rasi.Kumbha, Planet.Saturn.moolaTrikonaRasi());
	}

	@Test
	public void sunIsMoolaTrikonaInLeoOnly() {
		for (Rasi r : Rasi.values()) {
			assertEquals("Sun in " + r.name(), r == Rasi.Simha, Planet.Sun.isMoolaTrikona(r));
		}
	}

	@Test
	public void sunIsNotMoolaTrikonaInAries() {
		assertFalse(Planet.Sun.isMoolaTrikona(Rasi.Mesha));
		assertTrue(Planet.Sun.isExaltedRasi(Rasi.Mesha));
	}

	@Test
	public void everyClassicalPlanetHasExactlyOneMoolaTrikonaSign() {
		Planet[] seven = {Planet.Sun, Planet.Moon, Planet.Mars, Planet.Mercury,
				Planet.Jupiter, Planet.Venus, Planet.Saturn};
		for (Planet p : seven) {
			int count = 0;
			for (Rasi r : Rasi.values()) {
				if (p.isMoolaTrikona(r)) count++;
			}
			assertEquals(p.name(), 1, count);
		}
	}

	@Test
	public void nodesAndAscendantAreInvalid() {
		Planet[] invalid = {Planet.Rahu, Planet.Ketu, Planet.Ascendant, Planet.Ra_Ke};
		for (Planet p : invalid) {
			try {
				p.moolaTrikonaRasi();
				fail("expected IllegalArgumentException for " + p);
			} catch (IllegalArgumentException expected) {
				// ok
			}
		}
	}
}
