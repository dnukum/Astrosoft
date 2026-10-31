package app.astrosoft.ui.util.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.consts.Dignity;
import app.astrosoft.ui.util.PlanetStatusCodes;

public class PlanetStatusCodesTest {

	@Test
	public void dignityCodes() {
		assertEquals("Ex", PlanetStatusCodes.codes(new PlanetStatus(Dignity.EXALTED, false)));
		assertEquals("Db", PlanetStatusCodes.codes(new PlanetStatus(Dignity.DEBILITATED, false)));
		assertEquals("Mt", PlanetStatusCodes.codes(new PlanetStatus(Dignity.MOOLATRIKONA, false)));
		assertEquals("Own", PlanetStatusCodes.codes(new PlanetStatus(Dignity.OWN, false)));
	}

	@Test
	public void vargottamaAloneAndCombined() {
		assertEquals("V", PlanetStatusCodes.codes(new PlanetStatus(Dignity.NONE, true)));
		assertEquals("Ex,V", PlanetStatusCodes.codes(new PlanetStatus(Dignity.EXALTED, true)));
		assertEquals("Own,V", PlanetStatusCodes.codes(new PlanetStatus(Dignity.OWN, true)));
	}

	@Test
	public void emptyAndNullGiveEmptyCodes() {
		assertEquals("", PlanetStatusCodes.codes(new PlanetStatus(Dignity.NONE, false)));
		assertEquals("", PlanetStatusCodes.codes(null));
	}

	@Test
	public void suffixTextCombinesRetroThenStatus() {
		assertEquals("", PlanetStatusCodes.suffixText(null, null));
		assertEquals("", PlanetStatusCodes.suffixText(null, new PlanetStatus(Dignity.NONE, false)));
		assertEquals(" (R)", PlanetStatusCodes.suffixText("R", null));
		assertEquals(" (R,Ex,V)", PlanetStatusCodes.suffixText("R", new PlanetStatus(Dignity.EXALTED, true)));
		assertEquals(" (Own)", PlanetStatusCodes.suffixText(null, new PlanetStatus(Dignity.OWN, false)));
		assertEquals(" (V)", PlanetStatusCodes.suffixText("", new PlanetStatus(Dignity.NONE, true)));
	}
}
