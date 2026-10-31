package app.astrosoft.ui.table.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.EnumMap;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

import org.junit.Test;

import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.consts.Dignity;
import app.astrosoft.consts.DisplayFormat;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Planet;
import app.astrosoft.ui.table.PlanetCellRenderer;

public class PlanetCellRendererStatusTest {

	private static PlanetCellRenderer renderer(EnumMap<Planet, Boolean> dir, EnumMap<Planet, PlanetStatus> st) {
		return renderer(dir, null, st);
	}

	private static PlanetCellRenderer renderer(EnumMap<Planet, Boolean> dir, EnumMap<Planet, Boolean> combust, EnumMap<Planet, PlanetStatus> st) {
		PlanetCellRenderer r = new PlanetCellRenderer(new DefaultTableCellRenderer(), DisplayFormat.FULL_NAME, true, true, dir);
		if (combust != null) {
			r.setPlanetCombustion(combust);
		}
		if (st != null) {
			r.setPlanetStatus(st);
		}
		return r;
	}

	private static String text(PlanetCellRenderer r, Planet p) {
		return ((JLabel) r.getTableCellRendererComponent(null, p, false, false, 0, 0)).getText();
	}

	@Test
	public void exaltedVargottamaSunGetsSuffix() {
		EnumMap<Planet, PlanetStatus> st = new EnumMap<Planet, PlanetStatus>(Planet.class);
		st.put(Planet.Sun, new PlanetStatus(Dignity.EXALTED, true));
		String t = text(renderer(new EnumMap<Planet, Boolean>(Planet.class), st), Planet.Sun);
		assertEquals(Planet.Sun.toString(DisplayFormat.FULL_NAME) + " (Ex,V)", t);
	}

	@Test
	public void moonOwnSignGetsSuffix() {
		EnumMap<Planet, PlanetStatus> st = new EnumMap<Planet, PlanetStatus>(Planet.class);
		st.put(Planet.Moon, new PlanetStatus(Dignity.OWN, false));
		String t = text(renderer(new EnumMap<Planet, Boolean>(Planet.class), st), Planet.Moon);
		assertEquals(Planet.Moon.toString(DisplayFormat.FULL_NAME) + " (Own)", t);
	}

	@Test
	public void retrogradeComesFirstThenStatus() {
		EnumMap<Planet, Boolean> dir = new EnumMap<Planet, Boolean>(Planet.class);
		dir.put(Planet.Saturn, true);
		EnumMap<Planet, PlanetStatus> st = new EnumMap<Planet, PlanetStatus>(Planet.class);
		st.put(Planet.Saturn, new PlanetStatus(Dignity.DEBILITATED, false));
		String t = text(renderer(dir, st), Planet.Saturn);
		assertEquals(Planet.Saturn.toString(DisplayFormat.FULL_NAME) + " (" + DisplayStrings.RETRO_SYM + ",Db)", t);
	}

	@Test
	public void ascendantNeverGetsStatusSuffix() {
		EnumMap<Planet, PlanetStatus> st = new EnumMap<Planet, PlanetStatus>(Planet.class);
		st.put(Planet.Ascendant, new PlanetStatus(Dignity.EXALTED, true));
		String t = text(renderer(new EnumMap<Planet, Boolean>(Planet.class), st), Planet.Ascendant);
		assertEquals(Planet.Ascendant.toString(DisplayFormat.FULL_NAME), t);
	}

	@Test
	public void withoutStatusMapLegacyRetrogradeTextIsUnchanged() {
		EnumMap<Planet, Boolean> dir = new EnumMap<Planet, Boolean>(Planet.class);
		dir.put(Planet.Saturn, true);
		String t = text(renderer(dir, null), Planet.Saturn);
		assertEquals(Planet.Saturn.toString(DisplayFormat.FULL_NAME) + " (" + DisplayStrings.RETRO_SYM + ")", t);
	}

	@Test
	public void planetWithoutStatusEntryHasNoSuffix() {
		EnumMap<Planet, PlanetStatus> st = new EnumMap<Planet, PlanetStatus>(Planet.class);
		String t = text(renderer(new EnumMap<Planet, Boolean>(Planet.class), st), Planet.Mars);
		assertTrue(!t.contains("("));
	}

	@Test
	public void combustOnlyGetsCSuffix() {
		EnumMap<Planet, Boolean> combust = new EnumMap<Planet, Boolean>(Planet.class);
		combust.put(Planet.Jupiter, true);
		EnumMap<Planet, PlanetStatus> st = new EnumMap<Planet, PlanetStatus>(Planet.class);
		st.put(Planet.Jupiter, new PlanetStatus(Dignity.NONE, false));
		String t = text(renderer(new EnumMap<Planet, Boolean>(Planet.class), combust, st), Planet.Jupiter);
		assertEquals(Planet.Jupiter.toString(DisplayFormat.FULL_NAME) + " (C)", t);
	}

	@Test
	public void retrogradeAndCombustPrefixBothSymbols() {
		EnumMap<Planet, Boolean> dir = new EnumMap<Planet, Boolean>(Planet.class);
		dir.put(Planet.Mercury, true);
		EnumMap<Planet, Boolean> combust = new EnumMap<Planet, Boolean>(Planet.class);
		combust.put(Planet.Mercury, true);
		EnumMap<Planet, PlanetStatus> st = new EnumMap<Planet, PlanetStatus>(Planet.class);
		st.put(Planet.Mercury, new PlanetStatus(Dignity.DEBILITATED, true));
		String t = text(renderer(dir, combust, st), Planet.Mercury);
		assertEquals(Planet.Mercury.toString(DisplayFormat.FULL_NAME) + " (" + DisplayStrings.RETRO_SYM + ",C,Db,V)", t);
	}
}
