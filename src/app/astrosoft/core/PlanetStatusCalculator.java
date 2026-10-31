package app.astrosoft.core;

import java.util.EnumSet;
import java.util.Set;

import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.consts.Dignity;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;

/**
 * Pure planetary-status rules. No UI, no preferences, no I/O.
 */
public final class PlanetStatusCalculator {

	private static final Set<Planet> CLASSICAL = EnumSet.of(Planet.Sun, Planet.Moon, Planet.Mars,
			Planet.Mercury, Planet.Jupiter, Planet.Venus, Planet.Saturn);

	private PlanetStatusCalculator() {
	}

	/**
	 * Whole-sign dignity, highest only: Exalted, Moolatrikona, Own; Debilitated stands alone.
	 * Nodes, the Ascendant and the combined node get NONE.
	 */
	public static Dignity dignity(Planet planet, Rasi sign) {
		if (planet == null || sign == null) {
			throw new IllegalArgumentException("planet and sign must not be null");
		}
		if (!CLASSICAL.contains(planet)) {
			return Dignity.NONE;
		}
		if (planet.isExaltedRasi(sign)) {
			return Dignity.EXALTED;
		}
		if (planet.isDebilitatedRasi(sign)) {
			return Dignity.DEBILITATED;
		}
		if (planet.isMoolaTrikona(sign)) {
			return Dignity.MOOLATRIKONA;
		}
		if (planet.isOwnerOf(sign)) {
			return Dignity.OWN;
		}
		return Dignity.NONE;
	}

	/**
	 * Vargottama: same sign in Rasi (D1) and Navamsa (D9). Classical seven only.
	 */
	public static boolean isVargottama(Planet planet, Rasi d1, Rasi d9) {
		if (planet == null || d1 == null || d9 == null) {
			throw new IllegalArgumentException("planet, d1 and d9 must not be null");
		}
		return CLASSICAL.contains(planet) && d1 == d9;
	}

	/**
	 * Combined status: dignity from the D1 sign, Vargottama from D1 vs D9 sign.
	 */
	public static PlanetStatus statusOf(Planet planet, Rasi d1, Rasi d9) {
		return new PlanetStatus(dignity(planet, d1), isVargottama(planet, d1, d9));
	}
}
