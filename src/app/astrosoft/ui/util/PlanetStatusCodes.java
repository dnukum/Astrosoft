package app.astrosoft.ui.util;

import java.util.ArrayList;
import java.util.List;

import app.astrosoft.beans.PlanetStatus;

/**
 * Short codes for the Planetary Positions planet cell: Ex, Db, Mt, Own, V.
 * Pure strings; no Swing, no preferences.
 */
public final class PlanetStatusCodes {

	private PlanetStatusCodes() {
	}

	public static String codes(PlanetStatus status) {
		return join(parts(null, status));
	}

	/** Returns "" or " (R,Ex,V)"; the retrograde symbol, when given, always comes first. */
	public static String suffixText(String retroSym, PlanetStatus status) {
		List<String> p = parts(retroSym, status);
		return p.isEmpty() ? "" : " (" + join(p) + ")";
	}

	private static List<String> parts(String retroSym, PlanetStatus status) {
		List<String> p = new ArrayList<String>();
		if (retroSym != null && !retroSym.isEmpty()) {
			p.add(retroSym);
		}
		if (status != null) {
			switch (status.getDignity()) {
			case EXALTED: p.add("Ex"); break;
			case DEBILITATED: p.add("Db"); break;
			case MOOLATRIKONA: p.add("Mt"); break;
			case OWN: p.add("Own"); break;
			default: break;
			}
			if (status.isVargottama()) {
				p.add("V");
			}
		}
		return p;
	}

	private static String join(List<String> p) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < p.size(); i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(p.get(i));
		}
		return sb.toString();
	}
}
