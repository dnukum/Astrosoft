package app.astrosoft.ui.util;

import app.astrosoft.beans.PlanetStatus;

/**
 * Pure mapping from PlanetStatus to a display suffix. No Swing, no preferences.
 * Glyphs are provisional (see spec/TRANSIT_CHART.md section 17.3).
 */
public final class PlanetStatusMarkers {

	private static final String EXALTED = "\u25B2";
	private static final String DEBILITATED = "\u25BC";
	private static final String MOOLATRIKONA = "\u25C6";
	private static final String OWN = "\u25A0";
	private static final String VARGOTTAMA = "\u2021";

	private PlanetStatusMarkers() {
	}

	public static String suffix(PlanetStatus status) {
		if (status == null) {
			return "";
		}
		String s;
		switch (status.getDignity()) {
		case EXALTED: s = EXALTED; break;
		case DEBILITATED: s = DEBILITATED; break;
		case MOOLATRIKONA: s = MOOLATRIKONA; break;
		case OWN: s = OWN; break;
		default: s = ""; break;
		}
		return status.isVargottama() ? s + VARGOTTAMA : s;
	}
}
