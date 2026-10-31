package app.astrosoft.core;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
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

	/**
	 * Converts 1-based sign numbers (as stored in divisional charts) to Rasi values.
	 */
	public static EnumMap<Planet, Rasi> signsOf(Map<Planet, Integer> signNumbers) {
		if (signNumbers == null) {
			throw new IllegalArgumentException("signNumbers must not be null");
		}
		EnumMap<Planet, Rasi> result = new EnumMap<Planet, Rasi>(Planet.class);
		for (Map.Entry<Planet, Integer> e : signNumbers.entrySet()) {
			Integer n = e.getValue();
			if (n == null || n < 1 || n > 12) {
				throw new IllegalArgumentException("sign number out of range 1..12: " + n);
			}
			result.put(e.getKey(), Rasi.values()[n - 1]);
		}
		return result;
	}

	/**
	 * Status for every planet present in d1. A planet missing from d9 keeps its dignity and is not Vargottama.
	 */
	public static EnumMap<Planet, PlanetStatus> statusMap(Map<Planet, Rasi> d1, Map<Planet, Rasi> d9) {
		if (d1 == null || d9 == null) {
			throw new IllegalArgumentException("d1 and d9 must not be null");
		}
		EnumMap<Planet, PlanetStatus> result = new EnumMap<Planet, PlanetStatus>(Planet.class);
		for (Map.Entry<Planet, Rasi> e : d1.entrySet()) {
			Planet p = e.getKey();
			Rasi navamsa = d9.get(p);
			if (navamsa == null) {
				result.put(p, new PlanetStatus(dignity(p, e.getValue()), false));
			} else {
				result.put(p, statusOf(p, e.getValue(), navamsa));
			}
		}
		return result;
	}
}
