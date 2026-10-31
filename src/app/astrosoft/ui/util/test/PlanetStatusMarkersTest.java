package app.astrosoft.ui.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.consts.Dignity;
import app.astrosoft.ui.util.PlanetStatusMarkers;

public class PlanetStatusMarkersTest {

	private static final String EXALTED = "\u25B2";
	private static final String DEBILITATED = "\u25BC";
	private static final String MOOLATRIKONA = "\u25C6";
	private static final String OWN = "\u25A0";
	private static final String VARGOTTAMA = "\u2021";

	@Test
	public void emptyStatusHasEmptySuffix() {
		assertEquals("", PlanetStatusMarkers.suffix(new PlanetStatus(Dignity.NONE, false)));
	}

	@Test
	public void eachDignityHasItsGlyph() {
		assertEquals(EXALTED, PlanetStatusMarkers.suffix(new PlanetStatus(Dignity.EXALTED, false)));
		assertEquals(DEBILITATED, PlanetStatusMarkers.suffix(new PlanetStatus(Dignity.DEBILITATED, false)));
		assertEquals(MOOLATRIKONA, PlanetStatusMarkers.suffix(new PlanetStatus(Dignity.MOOLATRIKONA, false)));
		assertEquals(OWN, PlanetStatusMarkers.suffix(new PlanetStatus(Dignity.OWN, false)));
	}

	@Test
	public void vargottamaAloneAndAfterDignity() {
		assertEquals(VARGOTTAMA, PlanetStatusMarkers.suffix(new PlanetStatus(Dignity.NONE, true)));
		assertEquals(EXALTED + VARGOTTAMA, PlanetStatusMarkers.suffix(new PlanetStatus(Dignity.EXALTED, true)));
		assertEquals(OWN + VARGOTTAMA, PlanetStatusMarkers.suffix(new PlanetStatus(Dignity.OWN, true)));
	}

	@Test
	public void allTenCombinationsAreDistinct() {
		Set<String> seen = new HashSet<String>();
		for (Dignity d : Dignity.values()) {
			for (boolean v : new boolean[] {false, true}) {
				seen.add(PlanetStatusMarkers.suffix(new PlanetStatus(d, v)));
			}
		}
		assertEquals(10, seen.size());
		assertTrue(seen.contains(""));
	}

	@Test
	public void nullStatusIsEmptySuffix() {
		assertEquals("", PlanetStatusMarkers.suffix(null));
	}
}
