package app.astrosoft.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.EnumMap;

import org.junit.Test;

import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.SignRelationship;
import app.astrosoft.core.PlanetStatusCalculator;

/**
 * Natural planetary relationship with each of the 12 zodiac signs (Kshetra Sambandha).
 * Derived from Exaltation, Debilitation, Ownership, and the classical permanent friendship
 * with the sign dispositor (B.V. Raman, Hindu Predictive Astrology / BPHS).
 *
 * Columns: Mesha, Vrishabha, Mithuna, Kataka, Simha, Kanya, Thula, Vrischika, Dhanus, Makara, Kumbha, Meena
 * Codes:
 * Ex = Exalted
 * D  = Debilitated
 * O  = Own
 * F  = Friendly
 * N  = Neutral
 * E  = Enemy
 */
public class PlanetSignRelationshipTest {

	private static final EnumMap<Planet, String> EXPECTED = new EnumMap<Planet, String>(Planet.class);
	static {
		//                            Mes  Vri  Mit  Kat  Sim  Kan  Thu  Vri  Dha  Mak  Kum  Mee
		EXPECTED.put(Planet.Sun,     "Ex   E    N    F    O    N    D    F    F    E    E    F");
		EXPECTED.put(Planet.Moon,    "N    Ex   F    O    F    F    N    D    N    N    N    N");
		EXPECTED.put(Planet.Mars,    "O    N    E    D    F    E    N    O    F    Ex   N    F");
		EXPECTED.put(Planet.Mercury, "N    F    O    E    F    Ex   F    N    N    N    N    D");
		EXPECTED.put(Planet.Jupiter, "F    E    E    Ex   F    E    E    F    O    D    N    O");
		EXPECTED.put(Planet.Venus,   "N    O    F    E    E    D    O    N    N    F    F    Ex");
		EXPECTED.put(Planet.Saturn,  "D    F    F    E    E    F    Ex   E    N    O    O    N");
	}

	private static SignRelationship decode(String code) {
		switch (code) {
			case "Ex": return SignRelationship.EXALTED;
			case "D":  return SignRelationship.DEBILITATED;
			case "O":  return SignRelationship.OWN;
			case "F":  return SignRelationship.FRIENDLY;
			case "N":  return SignRelationship.NEUTRAL;
			case "E":  return SignRelationship.ENEMY;
			default:   throw new IllegalArgumentException("Unknown code: " + code);
		}
	}

	@Test
	public void fullSevenByTwelveSignRelationshipTable() {
		for (Planet p : EXPECTED.keySet()) {
			String[] cells = EXPECTED.get(p).split("\\s+");
			assertEquals("Expected 12 signs for " + p, 12, cells.length);
			for (int i = 0; i < 12; i++) {
				Rasi r = Rasi.values()[i];
				SignRelationship expected = decode(cells[i]);
				SignRelationship actual = PlanetStatusCalculator.signRelationship(p, r);
				assertEquals(p + " in " + r, expected, actual);
			}
		}
	}

	@Test
	public void moonInPiscesIsNeutral() {
		// Specific test for user case: Moon in Meena (ruled by Jupiter, natural neutral of Moon)
		assertEquals(SignRelationship.NEUTRAL, PlanetStatusCalculator.signRelationship(Planet.Moon, Rasi.Meena));
		assertEquals("Neutral", PlanetStatusCalculator.signRelationship(Planet.Moon, Rasi.Meena).label());
	}

	@Test
	public void nodesAndAscendantReturnNull() {
		for (Rasi r : Rasi.values()) {
			assertNull(PlanetStatusCalculator.signRelationship(Planet.Rahu, r));
			assertNull(PlanetStatusCalculator.signRelationship(Planet.Ketu, r));
			assertNull(PlanetStatusCalculator.signRelationship(Planet.Ascendant, r));
		}
	}
}
