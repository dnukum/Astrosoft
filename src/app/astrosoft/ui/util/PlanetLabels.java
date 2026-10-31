package app.astrosoft.ui.util;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Varga;
import app.astrosoft.core.PlanetStatusCalculator;

/**
 * Display text for planets in the Planetary Positions panels. One place for the suffix rules so
 * every panel agrees: retrograde, combust, then dignity and Vargottama, e.g. "Mercury (R,C,Db,V)".
 * No Swing and no preferences.
 */
public final class PlanetLabels {

	private static final Logger log = Logger.getLogger(PlanetLabels.class.getName());

	private PlanetLabels() {
	}

	/** The planet name plus its suffix. Pass a null status for the Ascendant. */
	public static String label(String name, boolean retro, boolean combust, PlanetStatus status) {
		String lead = (retro && combust) ? "R,C" : retro ? "R" : combust ? "C" : null;
		return name + PlanetStatusCodes.suffixText(lead, status);
	}

	/** Dignity and Vargottama for display only: Rasi from D1, Navamsa from the divisional chart (not getPlanetRasi(Varga)). */
	public static Map<Planet, PlanetStatus> statuses(PlanetaryInfo info) {
		EnumMap<Planet, Rasi> d9 = new EnumMap<Planet, Rasi>(Planet.class);
		EnumMap<Planet, Integer> d9Numbers = info.getDivChart(Varga.Navamsa);
		if (d9Numbers == null) {
			log.warning("Navamsa chart missing in PlanetaryInfo; falling back to dignity-only status suffixes.");
		} else {
			try {
				d9 = PlanetStatusCalculator.signsOf(d9Numbers);
			} catch (IllegalArgumentException e) {
				// bad Navamsa data: show dignity only, never break the view
				log.warning("Malformed Navamsa data in PlanetaryInfo (" + e.getMessage()
						+ "); falling back to dignity-only status suffixes.");
			}
		}
		return PlanetStatusCalculator.statusMap(info.getPlanetRasi(), d9);
	}
}
