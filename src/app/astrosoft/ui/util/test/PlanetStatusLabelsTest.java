package app.astrosoft.ui.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.consts.Dignity;
import app.astrosoft.ui.util.PlanetStatusLabels;

public class PlanetStatusLabelsTest {

	@Test
	public void emptyStatusHasEmptyLabel() {
		assertEquals("", PlanetStatusLabels.label(new PlanetStatus(Dignity.NONE, false)));
	}

	@Test
	public void eachDignityHasAClearWord() {
		assertEquals("Exalted", PlanetStatusLabels.label(new PlanetStatus(Dignity.EXALTED, false)));
		assertEquals("Debilitated", PlanetStatusLabels.label(new PlanetStatus(Dignity.DEBILITATED, false)));
		assertEquals("Moolatrikona", PlanetStatusLabels.label(new PlanetStatus(Dignity.MOOLATRIKONA, false)));
		assertEquals("Own sign", PlanetStatusLabels.label(new PlanetStatus(Dignity.OWN, false)));
	}

	@Test
	public void vargottamaAloneAndCombined() {
		assertEquals("Vargottama", PlanetStatusLabels.label(new PlanetStatus(Dignity.NONE, true)));
		assertEquals("Exalted, Vargottama", PlanetStatusLabels.label(new PlanetStatus(Dignity.EXALTED, true)));
		assertEquals("Own sign, Vargottama", PlanetStatusLabels.label(new PlanetStatus(Dignity.OWN, true)));
	}

	@Test
	public void allTenCombinationsAreDistinct() {
		Set<String> seen = new HashSet<String>();
		for (Dignity d : Dignity.values()) {
			for (boolean v : new boolean[] {false, true}) {
				seen.add(PlanetStatusLabels.label(new PlanetStatus(d, v)));
			}
		}
		assertEquals(10, seen.size());
		assertTrue(seen.contains(""));
	}

	@Test
	public void nullStatusIsEmptyLabel() {
		assertEquals("", PlanetStatusLabels.label(null));
	}
}
