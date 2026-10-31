package app.astrosoft.ui.util;

import app.astrosoft.beans.PlanetStatus;

/**
 * Pure mapping from PlanetStatus to a human-readable label for the Planetary Positions view.
 * No Swing, no preferences. English strings; localisation is deferred.
 */
public final class PlanetStatusLabels {

	private PlanetStatusLabels() {
	}

	public static String label(PlanetStatus status) {
		if (status == null) {
			return "";
		}
		String d;
		switch (status.getDignity()) {
		case EXALTED: d = "Exalted"; break;
		case DEBILITATED: d = "Debilitated"; break;
		case MOOLATRIKONA: d = "Moolatrikona"; break;
		case OWN: d = "Own sign"; break;
		default: d = ""; break;
		}
		if (!status.isVargottama()) {
			return d;
		}
		return d.isEmpty() ? "Vargottama" : d + ", Vargottama";
	}
}
