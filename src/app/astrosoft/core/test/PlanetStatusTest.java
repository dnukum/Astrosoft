package app.astrosoft.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.consts.Dignity;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.PlanetStatusCalculator;

public class PlanetStatusTest {

	@Test
	public void exaltedAndVargottama() {
		PlanetStatus s = PlanetStatusCalculator.statusOf(Planet.Sun, Rasi.Mesha, Rasi.Mesha);
		assertEquals(Dignity.EXALTED, s.getDignity());
		assertTrue(s.isVargottama());
	}

	@Test
	public void debilitatedNotVargottama() {
		PlanetStatus s = PlanetStatusCalculator.statusOf(Planet.Sun, Rasi.Thula, Rasi.Mesha);
		assertEquals(Dignity.DEBILITATED, s.getDignity());
		assertFalse(s.isVargottama());
	}

	@Test
	public void dignityUsesD1SignNotD9() {
		PlanetStatus s = PlanetStatusCalculator.statusOf(Planet.Moon, Rasi.Mithuna, Rasi.Vrishabha);
		assertEquals(Dignity.NONE, s.getDignity());
		assertFalse(s.isVargottama());
	}

	@Test
	public void nodeHasNoDignityAndIsNeverVargottama() {
		PlanetStatus s = PlanetStatusCalculator.statusOf(Planet.Rahu, Rasi.Mesha, Rasi.Mesha);
		assertEquals(Dignity.NONE, s.getDignity());
		assertFalse(s.isVargottama());
	}

	@Test
	public void valueEquality() {
		assertEquals(new PlanetStatus(Dignity.OWN, true), new PlanetStatus(Dignity.OWN, true));
		assertEquals(new PlanetStatus(Dignity.OWN, true).hashCode(), new PlanetStatus(Dignity.OWN, true).hashCode());
		assertFalse(new PlanetStatus(Dignity.OWN, true).equals(new PlanetStatus(Dignity.OWN, false)));
		assertFalse(new PlanetStatus(Dignity.OWN, true).equals(new PlanetStatus(Dignity.NONE, true)));
	}

	@Test
	public void noneStatusIsEmpty() {
		assertTrue(new PlanetStatus(Dignity.NONE, false).isEmpty());
		assertFalse(new PlanetStatus(Dignity.NONE, true).isEmpty());
		assertFalse(new PlanetStatus(Dignity.OWN, false).isEmpty());
	}

	@Test
	public void nullsRejected() {
		try {
			new PlanetStatus(null, false);
			fail("null dignity");
		} catch (IllegalArgumentException expected) {
			// ok
		}
		try {
			PlanetStatusCalculator.statusOf(null, Rasi.Mesha, Rasi.Mesha);
			fail("null planet");
		} catch (IllegalArgumentException expected) {
			// ok
		}
	}
}
