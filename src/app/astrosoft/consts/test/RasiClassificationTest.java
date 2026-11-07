package app.astrosoft.consts.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Rasi.Cardinality;
import app.astrosoft.consts.Rasi.Element;
import app.astrosoft.consts.Rasi.Polarity;
import app.astrosoft.consts.Sex;

/**
 * Unit tests verifying Polarity (Odd/Even), Cardinality (Movable/Fixed/Common),
 * Elemental (Fire/Earth/Air/Water), and Lord classifications for all 12 Rasis.
 */
public class RasiClassificationTest {

	@Test
	public void testMesha() {
		assertEquals(Polarity.Odd, Rasi.Mesha.polarity());
		assertEquals("Odd", Rasi.Mesha.polarity().toString());
		assertEquals(Sex.Male, Rasi.Mesha.polarity().gender());
		assertEquals(Cardinality.Movable, Rasi.Mesha.cardinality());
		assertEquals("Movable", Rasi.Mesha.cardinality().toString());
		assertEquals("Chara", Rasi.Mesha.cardinality().sanskrit());
		assertEquals(Element.Fire, Rasi.Mesha.element());
		assertEquals("Fire", Rasi.Mesha.element().toString());
		assertEquals("Agni", Rasi.Mesha.element().sanskrit());
		assertEquals(Planet.Mars, Rasi.Mesha.owner());
		assertEquals(Planet.Mars, Rasi.Mesha.lord());

		assertTrue(Rasi.Mesha.isOddSign());
		assertFalse(Rasi.Mesha.isEvenSign());
		assertTrue(Rasi.Mesha.isMovableSign());
		assertFalse(Rasi.Mesha.isFixedSign());
		assertFalse(Rasi.Mesha.isCommonSign());
		assertTrue(Rasi.Mesha.isFireSign());
		assertEquals("Odd, Movable, Fire", Rasi.Mesha.classification());
	}

	@Test
	public void testVrishabha() {
		assertEquals(Polarity.Even, Rasi.Vrishabha.polarity());
		assertEquals(Cardinality.Fixed, Rasi.Vrishabha.cardinality());
		assertEquals(Element.Earth, Rasi.Vrishabha.element());
		assertEquals(Planet.Venus, Rasi.Vrishabha.lord());

		assertTrue(Rasi.Vrishabha.isEvenSign());
		assertTrue(Rasi.Vrishabha.isFixedSign());
		assertTrue(Rasi.Vrishabha.isEarthSign());
		assertEquals("Even, Fixed, Earth", Rasi.Vrishabha.classification());
	}

	@Test
	public void testMithuna() {
		assertEquals(Polarity.Odd, Rasi.Mithuna.polarity());
		assertEquals(Cardinality.Common, Rasi.Mithuna.cardinality());
		assertEquals(Element.Air, Rasi.Mithuna.element());
		assertEquals(Planet.Mercury, Rasi.Mithuna.lord());

		assertTrue(Rasi.Mithuna.isOddSign());
		assertTrue(Rasi.Mithuna.isCommonSign());
		assertTrue(Rasi.Mithuna.isDualSign());
		assertTrue(Rasi.Mithuna.isAirSign());
		assertEquals("Odd, Common, Air", Rasi.Mithuna.classification());
	}

	@Test
	public void testKataka() {
		assertEquals(Polarity.Even, Rasi.Kataka.polarity());
		assertEquals(Cardinality.Movable, Rasi.Kataka.cardinality());
		assertEquals(Element.Water, Rasi.Kataka.element());
		assertEquals(Planet.Moon, Rasi.Kataka.lord());

		assertTrue(Rasi.Kataka.isEvenSign());
		assertTrue(Rasi.Kataka.isMovableSign());
		assertTrue(Rasi.Kataka.isWaterSign());
		assertEquals("Even, Movable, Water", Rasi.Kataka.classification());
	}

	@Test
	public void testSimha() {
		assertEquals(Polarity.Odd, Rasi.Simha.polarity());
		assertEquals(Cardinality.Fixed, Rasi.Simha.cardinality());
		assertEquals(Element.Fire, Rasi.Simha.element());
		assertEquals(Planet.Sun, Rasi.Simha.lord());

		assertTrue(Rasi.Simha.isOddSign());
		assertTrue(Rasi.Simha.isFixedSign());
		assertTrue(Rasi.Simha.isFireSign());
		assertEquals("Odd, Fixed, Fire", Rasi.Simha.classification());
	}

	@Test
	public void testKanya() {
		assertEquals(Polarity.Even, Rasi.Kanya.polarity());
		assertEquals(Cardinality.Common, Rasi.Kanya.cardinality());
		assertEquals(Element.Earth, Rasi.Kanya.element());
		assertEquals(Planet.Mercury, Rasi.Kanya.lord());

		assertTrue(Rasi.Kanya.isEvenSign());
		assertTrue(Rasi.Kanya.isCommonSign());
		assertTrue(Rasi.Kanya.isDualSign());
		assertTrue(Rasi.Kanya.isEarthSign());
		assertEquals("Even, Common, Earth", Rasi.Kanya.classification());
	}

	@Test
	public void testThula() {
		assertEquals(Polarity.Odd, Rasi.Thula.polarity());
		assertEquals(Cardinality.Movable, Rasi.Thula.cardinality());
		assertEquals(Element.Air, Rasi.Thula.element());
		assertEquals(Planet.Venus, Rasi.Thula.lord());

		assertTrue(Rasi.Thula.isOddSign());
		assertTrue(Rasi.Thula.isMovableSign());
		assertTrue(Rasi.Thula.isAirSign());
		assertEquals("Odd, Movable, Air", Rasi.Thula.classification());
	}

	@Test
	public void testVrichika() {
		assertEquals(Polarity.Even, Rasi.Vrichika.polarity());
		assertEquals(Cardinality.Fixed, Rasi.Vrichika.cardinality());
		assertEquals(Element.Water, Rasi.Vrichika.element());
		assertEquals(Planet.Mars, Rasi.Vrichika.lord());

		assertTrue(Rasi.Vrichika.isEvenSign());
		assertTrue(Rasi.Vrichika.isFixedSign());
		assertTrue(Rasi.Vrichika.isWaterSign());
		assertEquals("Even, Fixed, Water", Rasi.Vrichika.classification());
	}

	@Test
	public void testDhanus() {
		assertEquals(Polarity.Odd, Rasi.Dhanus.polarity());
		assertEquals(Cardinality.Common, Rasi.Dhanus.cardinality());
		assertEquals(Element.Fire, Rasi.Dhanus.element());
		assertEquals(Planet.Jupiter, Rasi.Dhanus.lord());

		assertTrue(Rasi.Dhanus.isOddSign());
		assertTrue(Rasi.Dhanus.isCommonSign());
		assertTrue(Rasi.Dhanus.isDualSign());
		assertTrue(Rasi.Dhanus.isFireSign());
		assertEquals("Odd, Common, Fire", Rasi.Dhanus.classification());
	}

	@Test
	public void testMakara() {
		assertEquals(Polarity.Even, Rasi.Makara.polarity());
		assertEquals(Cardinality.Movable, Rasi.Makara.cardinality());
		assertEquals(Element.Earth, Rasi.Makara.element());
		assertEquals(Planet.Saturn, Rasi.Makara.lord());

		assertTrue(Rasi.Makara.isEvenSign());
		assertTrue(Rasi.Makara.isMovableSign());
		assertTrue(Rasi.Makara.isEarthSign());
		assertEquals("Even, Movable, Earth", Rasi.Makara.classification());
	}

	@Test
	public void testKumbha() {
		assertEquals(Polarity.Odd, Rasi.Kumbha.polarity());
		assertEquals(Cardinality.Fixed, Rasi.Kumbha.cardinality());
		assertEquals(Element.Air, Rasi.Kumbha.element());
		assertEquals(Planet.Saturn, Rasi.Kumbha.lord());

		assertTrue(Rasi.Kumbha.isOddSign());
		assertTrue(Rasi.Kumbha.isFixedSign());
		assertTrue(Rasi.Kumbha.isAirSign());
		assertEquals("Odd, Fixed, Air", Rasi.Kumbha.classification());
	}

	@Test
	public void testMeena() {
		assertEquals(Polarity.Even, Rasi.Meena.polarity());
		assertEquals(Cardinality.Common, Rasi.Meena.cardinality());
		assertEquals(Element.Water, Rasi.Meena.element());
		assertEquals(Planet.Jupiter, Rasi.Meena.lord());

		assertTrue(Rasi.Meena.isEvenSign());
		assertFalse(Rasi.Meena.isMovableSign());
		assertFalse(Rasi.Meena.isFixedSign());
		assertTrue(Rasi.Meena.isCommonSign());
		assertTrue(Rasi.Meena.isDualSign());
		assertTrue(Rasi.Meena.isWaterSign());
		assertEquals("Even, Common, Water", Rasi.Meena.classification());
	}

	@Test
	public void testLordAccessor() {
		for (Rasi r : Rasi.values()) {
			assertEquals(r.owner(), r.lord());
		}
	}

	@Test
	public void testAllSignsCardinalityDistribution() {
		int movCount = 0;
		int fixCount = 0;
		int comCount = 0;
		for (Rasi r : Rasi.values()) {
			if (r.cardinality() == Cardinality.Movable) movCount++;
			if (r.cardinality() == Cardinality.Fixed) fixCount++;
			if (r.cardinality() == Cardinality.Common) comCount++;
		}
		assertEquals(4, movCount);
		assertEquals(4, fixCount);
		assertEquals(4, comCount);
	}

	@Test
	public void testAllSignsElementDistribution() {
		int fireCount = 0;
		int earthCount = 0;
		int airCount = 0;
		int waterCount = 0;
		for (Rasi r : Rasi.values()) {
			if (r.element() == Element.Fire) fireCount++;
			if (r.element() == Element.Earth) earthCount++;
			if (r.element() == Element.Air) airCount++;
			if (r.element() == Element.Water) waterCount++;
		}
		assertEquals(3, fireCount);
		assertEquals(3, earthCount);
		assertEquals(3, airCount);
		assertEquals(3, waterCount);
	}
}
