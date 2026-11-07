package app.astrosoft.consts.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import app.astrosoft.beans.PlanetCharacter;
import app.astrosoft.consts.FunctionalRole;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;

/**
 * Unit tests verifying Dr. B.V. Raman's Functional Benefics, Functional Malefics,
 * Functional Neutrals, Yogakarakas, and Marakas across all 12 Lagnas.
 *
 * Authoritative Citations:
 * - Dr. B.V. Raman, "How to Judge a Horoscope", Vol. 1, Ch. 1:
 *   "Benefics and Malefics for Each Lagna" (pp. 7–9).
 * - Dr. B.V. Raman, "Astrology for Beginners", Ch. XV:
 *   "Hints on Judgment" (pp. 52–53).
 */
public class RamanFunctionalNatureTest {

	private static final Set<Planet> SEVEN_PLANETS = EnumSet.of(
			Planet.Sun, Planet.Moon, Planet.Mars, Planet.Mercury,
			Planet.Jupiter, Planet.Venus, Planet.Saturn
	);

	private void assertPartition(Rasi rasi) {
		Set<Planet> union = new HashSet<>();
		union.addAll(rasi.functionalBenefics());
		union.addAll(rasi.functionalMalefics());
		union.addAll(rasi.functionalNeutrals());

		assertEquals("Benefics, Malefics, and Neutrals must cover all 7 traditional planets for " + rasi,
				SEVEN_PLANETS, union);

		// Mutually exclusive
		for (Planet p : SEVEN_PLANETS) {
			int count = 0;
			if (rasi.isFunctionalBenefic(p)) count++;
			if (rasi.isFunctionalMalefic(p)) count++;
			if (rasi.isFunctionalNeutral(p)) count++;
			assertEquals("Planet " + p + " must belong to exactly one functional category for " + rasi, 1, count);
		}
	}

	@Test
	public void testAries() {
		Rasi lagna = Rasi.Mesha;
		assertEquals(EnumSet.of(Planet.Sun, Planet.Mars, Planet.Jupiter), lagna.functionalBenefics());
		assertEquals(EnumSet.of(Planet.Saturn, Planet.Mercury, Planet.Venus), lagna.functionalMalefics());
		assertEquals(EnumSet.of(Planet.Moon), lagna.functionalNeutrals());
		assertNull(lagna.yogakaraka());
		assertEquals(EnumSet.of(Planet.Mercury, Planet.Saturn, Planet.Venus), lagna.marakas());

		assertTrue(lagna.isFunctionalBenefic(Planet.Sun));
		assertTrue(lagna.isFunctionalBenefic(Planet.Mars));
		assertTrue(lagna.isFunctionalBenefic(Planet.Jupiter));
		assertTrue(lagna.isFunctionalMalefic(Planet.Saturn));
		assertTrue(lagna.isFunctionalMalefic(Planet.Mercury));
		assertTrue(lagna.isFunctionalMalefic(Planet.Venus));
		assertTrue(lagna.isFunctionalNeutral(Planet.Moon));
		assertTrue(lagna.isMaraka(Planet.Mercury));
		assertTrue(lagna.isMaraka(Planet.Saturn));
		assertTrue(lagna.isMaraka(Planet.Venus));
		assertFalse(lagna.isMaraka(Planet.Jupiter));

		assertPartition(lagna);
	}

	@Test
	public void testTaurus() {
		Rasi lagna = Rasi.Vrishabha;
		// Dr. Raman: Saturn is best (Yogakaraka); Mercury, Mars, Sun also benefics.
		// Jupiter, Moon are malefics. Venus is neutral (6th lord neutralized by Lagna lordship).
		// Marakas: Jupiter, Venus, Moon.
		assertEquals(EnumSet.of(Planet.Saturn, Planet.Mercury, Planet.Mars, Planet.Sun), lagna.functionalBenefics());
		assertEquals(EnumSet.of(Planet.Jupiter, Planet.Moon), lagna.functionalMalefics());
		assertEquals(EnumSet.of(Planet.Venus), lagna.functionalNeutrals());
		assertEquals(Planet.Saturn, lagna.yogakaraka());
		assertTrue(lagna.isYogakaraka(Planet.Saturn));
		assertEquals(EnumSet.of(Planet.Jupiter, Planet.Venus, Planet.Moon), lagna.marakas());

		assertEquals(FunctionalRole.BENEFIC, lagna.functionalRole(Planet.Saturn));
		assertEquals(FunctionalRole.BENEFIC, lagna.functionalRole(Planet.Mercury));
		assertEquals(FunctionalRole.BENEFIC, lagna.functionalRole(Planet.Mars));
		assertEquals(FunctionalRole.BENEFIC, lagna.functionalRole(Planet.Sun));
		assertEquals(FunctionalRole.MALEFIC, lagna.functionalRole(Planet.Jupiter));
		assertEquals(FunctionalRole.MALEFIC, lagna.functionalRole(Planet.Moon));
		assertEquals(FunctionalRole.NEUTRAL, lagna.functionalRole(Planet.Venus));

		assertPartition(lagna);
	}

	@Test
	public void testGemini() {
		Rasi lagna = Rasi.Mithuna;
		assertEquals(EnumSet.of(Planet.Venus, Planet.Saturn), lagna.functionalBenefics());
		assertEquals(EnumSet.of(Planet.Mars, Planet.Jupiter, Planet.Sun), lagna.functionalMalefics());
		assertEquals(EnumSet.of(Planet.Moon, Planet.Mercury), lagna.functionalNeutrals());
		assertNull(lagna.yogakaraka());
		assertEquals(EnumSet.of(Planet.Mars, Planet.Jupiter), lagna.marakas());

		assertPartition(lagna);
	}

	@Test
	public void testCancer() {
		Rasi lagna = Rasi.Kataka;
		// Dr. Raman: Mars is Yogakaraka, Jupiter benefic. Venus, Mercury malefics.
		// Saturn, Moon, Sun are neutrals. Marakas: Mercury, Venus.
		assertEquals(EnumSet.of(Planet.Mars, Planet.Jupiter), lagna.functionalBenefics());
		assertEquals(EnumSet.of(Planet.Venus, Planet.Mercury), lagna.functionalMalefics());
		assertEquals(EnumSet.of(Planet.Saturn, Planet.Moon, Planet.Sun), lagna.functionalNeutrals());
		assertEquals(Planet.Mars, lagna.yogakaraka());
		assertEquals(EnumSet.of(Planet.Mercury, Planet.Venus), lagna.marakas());

		assertPartition(lagna);
	}

	@Test
	public void testLeo() {
		Rasi lagna = Rasi.Simha;
		assertEquals(EnumSet.of(Planet.Mars, Planet.Sun), lagna.functionalBenefics());
		assertEquals(EnumSet.of(Planet.Mercury, Planet.Venus), lagna.functionalMalefics());
		assertEquals(EnumSet.of(Planet.Jupiter, Planet.Moon, Planet.Saturn), lagna.functionalNeutrals());
		assertEquals(Planet.Mars, lagna.yogakaraka());
		assertEquals(EnumSet.of(Planet.Mercury, Planet.Venus), lagna.marakas());

		assertPartition(lagna);
	}

	@Test
	public void testVirgo() {
		Rasi lagna = Rasi.Kanya;
		assertEquals(EnumSet.of(Planet.Venus), lagna.functionalBenefics());
		assertEquals(EnumSet.of(Planet.Moon, Planet.Mars, Planet.Jupiter), lagna.functionalMalefics());
		assertEquals(EnumSet.of(Planet.Saturn, Planet.Sun, Planet.Mercury), lagna.functionalNeutrals());
		assertNull(lagna.yogakaraka());
		assertEquals(EnumSet.of(Planet.Mars, Planet.Jupiter, Planet.Venus), lagna.marakas());

		assertPartition(lagna);
	}

	@Test
	public void testLibra() {
		Rasi lagna = Rasi.Thula;
		// Dr. Raman: Saturn (best/Yogakaraka), Mercury, Venus benefics. Mars feeble benefic.
		// Sun, Jupiter, Moon malefics. Neutral: none. Marakas: Jupiter, Sun, Moon.
		assertEquals(EnumSet.of(Planet.Saturn, Planet.Mercury, Planet.Venus, Planet.Mars), lagna.functionalBenefics());
		assertEquals(EnumSet.of(Planet.Sun, Planet.Jupiter, Planet.Moon), lagna.functionalMalefics());
		assertTrue(lagna.functionalNeutrals().isEmpty());
		assertEquals(Planet.Saturn, lagna.yogakaraka());
		assertEquals(EnumSet.of(Planet.Jupiter, Planet.Sun, Planet.Moon), lagna.marakas());

		assertTrue(lagna.isFunctionalBenefic(Planet.Mars));
		assertTrue(lagna.isFunctionalMalefic(Planet.Jupiter));
		assertTrue(lagna.isMaraka(Planet.Jupiter));

		assertPartition(lagna);
	}

	@Test
	public void testScorpio() {
		Rasi lagna = Rasi.Vrichika;
		assertEquals(EnumSet.of(Planet.Moon, Planet.Jupiter, Planet.Sun), lagna.functionalBenefics());
		assertEquals(EnumSet.of(Planet.Mercury, Planet.Venus), lagna.functionalMalefics());
		assertEquals(EnumSet.of(Planet.Mars, Planet.Saturn), lagna.functionalNeutrals());
		assertNull(lagna.yogakaraka());
		assertEquals(EnumSet.of(Planet.Mercury, Planet.Venus, Planet.Saturn), lagna.marakas());

		assertPartition(lagna);
	}

	@Test
	public void testSagittarius() {
		Rasi lagna = Rasi.Dhanus;
		assertEquals(EnumSet.of(Planet.Mars, Planet.Sun), lagna.functionalBenefics());
		assertEquals(EnumSet.of(Planet.Venus, Planet.Saturn, Planet.Mercury), lagna.functionalMalefics());
		assertEquals(EnumSet.of(Planet.Jupiter, Planet.Moon), lagna.functionalNeutrals());
		assertNull(lagna.yogakaraka());
		assertEquals(EnumSet.of(Planet.Venus, Planet.Saturn), lagna.marakas());

		assertPartition(lagna);
	}

	@Test
	public void testCapricorn() {
		Rasi lagna = Rasi.Makara;
		assertEquals(EnumSet.of(Planet.Venus, Planet.Mercury, Planet.Saturn), lagna.functionalBenefics());
		assertEquals(EnumSet.of(Planet.Mars, Planet.Jupiter, Planet.Moon), lagna.functionalMalefics());
		assertEquals(EnumSet.of(Planet.Sun), lagna.functionalNeutrals());
		assertEquals(Planet.Venus, lagna.yogakaraka());
		assertEquals(EnumSet.of(Planet.Mars, Planet.Jupiter), lagna.marakas());

		assertPartition(lagna);
	}

	@Test
	public void testAquarius() {
		Rasi lagna = Rasi.Kumbha;
		assertEquals(EnumSet.of(Planet.Venus, Planet.Sun, Planet.Mars, Planet.Saturn), lagna.functionalBenefics());
		assertEquals(EnumSet.of(Planet.Jupiter, Planet.Moon), lagna.functionalMalefics());
		assertEquals(EnumSet.of(Planet.Mercury), lagna.functionalNeutrals());
		assertEquals(Planet.Venus, lagna.yogakaraka());
		assertEquals(EnumSet.of(Planet.Jupiter, Planet.Moon), lagna.marakas());

		assertPartition(lagna);
	}

	@Test
	public void testPisces() {
		Rasi lagna = Rasi.Meena;
		assertEquals(EnumSet.of(Planet.Moon, Planet.Mars), lagna.functionalBenefics());
		assertEquals(EnumSet.of(Planet.Saturn, Planet.Sun, Planet.Venus, Planet.Mercury), lagna.functionalMalefics());
		assertEquals(EnumSet.of(Planet.Jupiter), lagna.functionalNeutrals());
		assertNull(lagna.yogakaraka());
		assertEquals(EnumSet.of(Planet.Mercury, Planet.Saturn, Planet.Venus), lagna.marakas());

		assertPartition(lagna);
	}

	@Test
	public void testPlanetCharacterIntegration() {
		// Static accessors on PlanetCharacter
		assertEquals(EnumSet.of(Planet.Saturn, Planet.Mercury, Planet.Venus, Planet.Mars),
				PlanetCharacter.getFunctionalBenefics(Rasi.Thula));
		assertEquals(EnumSet.of(Planet.Sun, Planet.Jupiter, Planet.Moon),
				PlanetCharacter.getFunctionalMalefics(Rasi.Thula));
		assertTrue(PlanetCharacter.getFunctionalNeutrals(Rasi.Thula).isEmpty());
		assertEquals(Planet.Saturn, PlanetCharacter.getYogakaraka(Rasi.Thula));
		assertTrue(PlanetCharacter.isYogakaraka(Rasi.Thula, Planet.Saturn));
		assertFalse(PlanetCharacter.isYogakaraka(Rasi.Thula, Planet.Mars));
		assertTrue(PlanetCharacter.isFunctionalBenefic(Rasi.Thula, Planet.Mars));
		assertTrue(PlanetCharacter.isFunctionalMalefic(Rasi.Thula, Planet.Jupiter));
		assertTrue(PlanetCharacter.isMaraka(Rasi.Thula, Planet.Jupiter));
		assertEquals(FunctionalRole.BENEFIC, PlanetCharacter.getFunctionalRole(Rasi.Thula, Planet.Saturn));
		assertEquals(FunctionalRole.MALEFIC, PlanetCharacter.getFunctionalRole(Rasi.Thula, Planet.Jupiter));

		// Instance accessors on PlanetCharacter bound to Lagna
		PlanetCharacter pc = new PlanetCharacter(null, Rasi.Thula);
		assertEquals(Rasi.Thula, pc.getLagna());
		assertTrue(pc.isFunctionalBenefic(Planet.Saturn));
		assertTrue(pc.isFunctionalBenefic(Planet.Mars));
		assertTrue(pc.isFunctionalMalefic(Planet.Jupiter));
		assertTrue(pc.isYogakaraka(Planet.Saturn));
		assertFalse(pc.isYogakaraka(Planet.Mars));
		assertTrue(pc.isMaraka(Planet.Jupiter));
		assertEquals(FunctionalRole.BENEFIC, pc.getFunctionalRole(Planet.Saturn));
		assertEquals(FunctionalRole.MALEFIC, pc.getFunctionalRole(Planet.Jupiter));
	}

	@Test
	public void testHoroscopeIntegration() {
		Horoscope horoscope = SampleHoroscopes.getHoroscope("samples/raja.ash");
		Rasi asc = horoscope.getAscendant();

		PlanetCharacter pc = horoscope.getPlanetCharacter();
		assertEquals(asc, pc.getLagna());

		for (Planet p : SEVEN_PLANETS) {
			assertEquals(asc.functionalRole(p), horoscope.getFunctionalRole(p));
			assertEquals(asc.functionalRole(p), pc.getFunctionalRole(p));
		}
		assertEquals(asc.yogakaraka(), horoscope.getYogakaraka());
	}
}
