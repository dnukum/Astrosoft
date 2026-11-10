/**
 * ChartTooltips.java
 *
 * Shared tooltip text builders for chart renderers (South Indian and North Indian).
 * Extracted verbatim from {@link app.astrosoft.ui.comp.Chart} so both chart styles
 * produce identical planet and house tooltips.
 */

package app.astrosoft.ui.comp.chart;

import java.util.List;
import java.util.Locale;

import app.astrosoft.beans.ChartData;
import app.astrosoft.beans.PlanetCharacter;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.consts.Language;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Varga;

public final class ChartTooltips {

	private ChartTooltips() {
	}

	/** Display absolute sidereal degrees and whole arcminutes; omit seconds without rounding. */
	public static String formatAbsoluteLongitude(Double longitude) {
		if (longitude == null || !Double.isFinite(longitude)) return null;
		double normalized = (longitude % 360 + 360) % 360;
		int totalMinutes = (int) Math.floor(normalized * 60);
		return String.format(Locale.ROOT, "%03d:%02d", totalMinutes / 60, totalMinutes % 60);
	}

	/** Format sign-relative degrees and whole arcminutes (00:00 to 29:59) as offset from start of rasi. */
	public static String formatSignRelativeOrb(Double longitude) {
		if (longitude == null || !Double.isFinite(longitude)) return null;
		double signOffset = ((longitude % 30) + 30) % 30;
		int totalMinutes = (int) Math.floor(signOffset * 60);
		return String.format(Locale.ROOT, "%02d:%02d", totalMinutes / 60, totalMinutes % 60);
	}

	/**
	 * Tooltip for a single planet occupying a chart house: orb (longitude), nakshatra and,
	 * for D1 charts, the functional role tag. Returns null when nothing is known.
	 */
	public static String planetToolTip(ChartData chartData, Planet planet) {
		if (!(chartData instanceof PlanetChartData) || planet == null) {
			return null;
		}
		PlanetChartData planetData = (PlanetChartData) chartData;
		Nakshathra nak = planetData.getPlanetNakshathra(planet);
		Double rawLong = planetData.getPlanetLongitude(planet);
		Double rawOrb = planetData.getPlanetOrb(planet);
		if (rawOrb == null) {
			rawOrb = rawLong;
		}
		String orb = formatSignRelativeOrb(rawOrb);
		String longitude = formatAbsoluteLongitude(rawLong);
		StringBuilder text = new StringBuilder();
		if (orb != null && longitude != null) {
			text.append(orb).append(" (").append(longitude).append(")");
		} else if (orb != null) {
			text.append(orb);
		} else if (longitude != null) {
			text.append(longitude);
		}
		if (nak != null) {
			if (text.length() > 0) text.append(", ");
			text.append(nak.toString());
		}
		if (planetData.getVarga() == Varga.Rasi && chartData.getAscendant() != null && !planet.isAsc()) {
			String roleTag = formatPlanetFunctionalRoleTag(chartData.getAscendant(), planet);
			if (roleTag != null) {
				if (text.length() > 0) text.append(" ");
				text.append(roleTag);
				return "<html>" + text.toString() + "</html>";
			}
		}
		return text.length() == 0 ? null : text.toString();
	}

	/** Tooltip for a house (rasi) cell: sign, degree span, classification, bhava number and planets. */
	public static String houseToolTip(ChartData chartData, Rasi house, boolean showHouseNumber) {
		int startDeg = house.ordinal() * 30;
		int endDeg = startDeg + 30;
		Rasi ascendant = chartData.getAscendant();
		String houseNumStr = (showHouseNumber && ascendant != null) ? "<br>House: " + house.bhava(ascendant) : "";

		StringBuilder sb = new StringBuilder();
		sb.append("<html><b>").append(house.toString(Language.ENGLISH)).append("</b> (")
		  .append(startDeg).append("&deg; - ").append(endDeg).append("&deg;)<br>")
		  .append(house.classification()).append(houseNumStr);

		// Functional nature display: strictly for D1 / Rasi charts
		if (chartData instanceof PlanetChartData) {
			PlanetChartData pcd = (PlanetChartData) chartData;
			if (pcd.getVarga() == Varga.Rasi && ascendant != null) {
				List<Planet> planets = pcd.getPlanetsInRasi() != null ? pcd.getPlanetsInRasi().get(house) : null;
				if (planets != null && !planets.isEmpty()) {
					sb.append("<br>Planets: ");
					boolean first = true;
					for (Planet p : planets) {
						if (!first) {
							sb.append(", ");
						}
						first = false;
						sb.append(formatPlanetFunctional(ascendant, p));
					}
				}
			}
		}

		sb.append("</html>");
		return sb.toString();
	}

	public static String formatPlanetFunctional(Rasi ascendant, Planet p) {
		String name = formatPlanetShort(p);
		String roleTag = formatPlanetFunctionalRoleTag(ascendant, p);
		return roleTag != null ? (name + " " + roleTag) : name;
	}

	public static String formatPlanetFunctionalRoleTag(Rasi ascendant, Planet p) {
		if (p == null || p.isAsc() || ascendant == null) {
			return null;
		}
		if (PlanetCharacter.isYogakaraka(ascendant, p)) {
			return "<font color=\"green\"><b>[Yog]</b></font>";
		} else if (PlanetCharacter.isFunctionalBenefic(ascendant, p)) {
			return "<font color=\"green\"><b>[Ben]</b></font>";
		} else if (PlanetCharacter.isFunctionalMalefic(ascendant, p)) {
			return "<font color=\"red\"><b>[Mal]</b></font>";
		} else if (PlanetCharacter.isFunctionalNeutral(ascendant, p)) {
			return "<font color=\"#888888\">[Neu]</font>";
		}
		return null;
	}

	public static String formatPlanetShort(Planet p) {
		if (p == null) return "";
		switch (p) {
			case Sun: return "Sun";
			case Moon: return "Moon";
			case Mars: return "Mars";
			case Mercury: return "Mer";
			case Jupiter: return "Jup";
			case Venus: return "Ven";
			case Saturn: return "Sat";
			case Rahu: return "Rah";
			case Ketu: return "Ket";
			case Ascendant: return "Asc";
			default: return p.name().substring(0, Math.min(3, p.name().length()));
		}
	}
}
