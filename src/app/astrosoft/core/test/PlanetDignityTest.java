package app.astrosoft.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.util.EnumMap;

import org.junit.Test;

import app.astrosoft.consts.Dignity;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.PlanetStatusCalculator;

/**
 * Whole-sign dignity of the seven classical planets (B.V. Raman, Bhava and Graha Balas, Table II).
 * Highest dignity only: Exalted > Moolatrikona > Own. Debilitated stands alone.
 *
 * The expected table below is written by hand and is independent of production code.
 * Columns: Ari Tau Gem Can Leo Vir Lib Sco Sag Cap Aqu Pis
 * E=Exalted D=Debilitated M=Moolatrikona O=Own -=none
 */
public class PlanetDignityTest {

	private static final EnumMap<Planet, String> EXPECTED = new EnumMap<Planet, String>(Planet.class);
	static {
		//                          Ari Tau Gem Can Leo Vir Lib Sco Sag Cap Aqu Pis
		EXPECTED.put(Planet.Sun,     "E - - - M - D - - - - -");
		EXPECTED.put(Planet.Moon,    "- E - O - - - D - - - -");
		EXPECTED.put(Planet.Mars,    "M - - D - - - O - E - -");
		EXPECTED.put(Planet.Mercury, "- - O - - E - - - - - D");
		EXPECTED.put(Planet.Jupiter, "- - - E - - - - M D - O");
		EXPECTED.put(Planet.Venus,   "- O - - - D M - - - - E");
		EXPECTED.put(Planet.Saturn,  "D - - - - - E - - O M -");
	}

	private static Dignity decode(String code) {
		if (code.equals("E")) return Dignity.EXALTED;
		if (code.equals("D")) return Dignity.DEBILITATED;
		if (code.equals("M")) return Dignity.MOOLATRIKONA;
		if (code.equals("O")) return Dignity.OWN;
		if (code.equals("-")) return Dignity.NONE;
		throw new IllegalStateException("bad code " + code);
	}

	@Test
	public void fullSevenByTwelveTable() {
		for (Planet p : EXPECTED.keySet()) {
			String[] cells = EXPECTED.get(p).split(" ");
			assertEquals(p.name() + " row width", 12, cells.length);
			for (Rasi r : Rasi.values()) {
				assertEquals(p.name() + " in " + r.name(), decode(cells[r.ordinal()]),
						PlanetStatusCalculator.dignity(p, r));
			}
		}
	}

	@Test
	public void tableHasTwentyFiveNonNoneCells() {
		int count = 0;
		for (Planet p : EXPECTED.keySet()) {
			for (String c : EXPECTED.get(p).split(" ")) {
				if (!c.equals("-")) count++;
			}
		}
		assertEquals(25, count);
	}

	@Test
	public void mercuryInVirgoIsExaltedOnly() {
		assertEquals(Dignity.EXALTED, PlanetStatusCalculator.dignity(Planet.Mercury, Rasi.Kanya));
	}

	@Test
	public void moonInTaurusIsExaltedNotMoolaTrikona() {
		assertEquals(Dignity.EXALTED, PlanetStatusCalculator.dignity(Planet.Moon, Rasi.Vrishabha));
	}

	@Test
	public void sunInLeoAndMarsInAriesAreMoolaTrikonaNotOwn() {
		assertEquals(Dignity.MOOLATRIKONA, PlanetStatusCalculator.dignity(Planet.Sun, Rasi.Simha));
		assertEquals(Dignity.MOOLATRIKONA, PlanetStatusCalculator.dignity(Planet.Mars, Rasi.Mesha));
	}

	@Test
	public void sunInAriesIsExaltedAndInLibraDebilitated() {
		assertEquals(Dignity.EXALTED, PlanetStatusCalculator.dignity(Planet.Sun, Rasi.Mesha));
		assertEquals(Dignity.DEBILITATED, PlanetStatusCalculator.dignity(Planet.Sun, Rasi.Thula));
	}

	@Test
	public void exaltationAndDebilitationSignsAreOpposite() {
		for (Planet p : EXPECTED.keySet()) {
			int diff = ((p.exaltedRasi().ordinal() - p.debilitatedRasi().ordinal()) % 12 + 12) % 12;
			assertEquals(p.name(), 6, diff);
		}
	}

	@Test
	public void ownSignIsNeverNoneOrDebilitated() {
		for (Planet p : EXPECTED.keySet()) {
			for (Rasi r : Rasi.values()) {
				if (r.owner() == p) {
					Dignity d = PlanetStatusCalculator.dignity(p, r);
					assertNotSame(p.name() + " in own sign " + r.name(), Dignity.NONE, d);
					assertNotSame(p.name() + " in own sign " + r.name(), Dignity.DEBILITATED, d);
				}
			}
		}
	}

	@Test
	public void nodesAscendantAndCombinedNodeGetNoDignity() {
		Planet[] none = {Planet.Rahu, Planet.Ketu, Planet.Ascendant, Planet.Ra_Ke};
		for (Planet p : none) {
			for (Rasi r : Rasi.values()) {
				assertEquals(p.name() + " in " + r.name(), Dignity.NONE, PlanetStatusCalculator.dignity(p, r));
			}
		}
	}

	@Test
	public void nullArgumentsAreRejected() {
		try {
			PlanetStatusCalculator.dignity(null, Rasi.Mesha);
			fail("expected IllegalArgumentException for null planet");
		} catch (IllegalArgumentException expected) {
			// ok
		}
		try {
			PlanetStatusCalculator.dignity(Planet.Sun, null);
			fail("expected IllegalArgumentException for null sign");
		} catch (IllegalArgumentException expected) {
			// ok
		}
	}
}
