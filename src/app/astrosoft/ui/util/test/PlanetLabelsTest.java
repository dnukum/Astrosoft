package app.astrosoft.ui.util.test;

import static org.junit.Assert.assertEquals;

import java.util.EnumMap;
import java.util.Map;

import org.junit.Test;

import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.PlanetStatusCalculator;
import app.astrosoft.ui.util.PlanetLabels;

/**
 * Label text for the Planetary Positions panels. Chart values mirror the real chart where
 * Mercury (29:53 Meena, Navamsa Meena) must read Db,V and Jupiter (Meena) must read Own.
 */
public class PlanetLabelsTest {

	private static final Rasi ARIES = Rasi.values()[0];
	private static final Rasi CAPRICORN = Rasi.values()[9];
	private static final Rasi PISCES = Rasi.values()[11];

	private static Map<Planet, PlanetStatus> chart() {
		EnumMap<Planet, Rasi> d1 = new EnumMap<Planet, Rasi>(Planet.class);
		EnumMap<Planet, Rasi> d9 = new EnumMap<Planet, Rasi>(Planet.class);
		for (Planet p : Planet.values()) {
			d1.put(p, ARIES);
			d9.put(p, ARIES);
		}
		d1.put(Planet.Mercury, PISCES);
		d9.put(Planet.Mercury, PISCES);
		d1.put(Planet.Jupiter, PISCES);
		d9.put(Planet.Jupiter, CAPRICORN);
		d1.put(Planet.Sun, PISCES);
		d9.put(Planet.Sun, ARIES);
		return PlanetStatusCalculator.statusMap(d1, d9);
	}

	@Test
	public void mercuryIsDebilitatedAndVargottama() {
		assertEquals("Mercury (Db,V)", PlanetLabels.label("Mercury", false, false, chart().get(Planet.Mercury)));
	}

	@Test
	public void jupiterInOwnSign() {
		assertEquals("Jupiter (Own)", PlanetLabels.label("Jupiter", false, false, chart().get(Planet.Jupiter)));
	}

	@Test
	public void combustComesBeforeDignity() {
		assertEquals("Jupiter (C,Own)", PlanetLabels.label("Jupiter", false, true, chart().get(Planet.Jupiter)));
	}

	@Test
	public void retroThenCombustThenStatus() {
		assertEquals("Mercury (R,C,Db,V)", PlanetLabels.label("Mercury", true, true, chart().get(Planet.Mercury)));
	}

	@Test
	public void planetWithoutStatusKeepsRetroAndCombustOnly() {
		assertEquals("Sun", PlanetLabels.label("Sun", false, false, chart().get(Planet.Sun)));
		assertEquals("Sun (R)", PlanetLabels.label("Sun", true, false, chart().get(Planet.Sun)));
		assertEquals("Sun (C)", PlanetLabels.label("Sun", false, true, chart().get(Planet.Sun)));
	}

	@Test
	public void ascendantWithNoStatusHasNoSuffix() {
		assertEquals("Ascendant", PlanetLabels.label("Ascendant", false, false, null));
	}
}
