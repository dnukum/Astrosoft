/**
 * HousePACCalculator.java
 *
 * Core computation engine for K.N. Rao PAC (Placement, Aspect, Conjunction)
 * analysis. Precomputes and evaluates house properties with O(1) static lookup
 * speed and zero Swing/UI dependencies.
 */
package app.astrosoft.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import app.astrosoft.beans.HousePACData;
import app.astrosoft.beans.HousePACData.AspectInfo;
import app.astrosoft.beans.HousePACData.LordPlacement;
import app.astrosoft.beans.HousePACData.OccupantInfo;
import app.astrosoft.beans.PlanetCharacter;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.AstroConsts;
import app.astrosoft.consts.FunctionalRole;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.SignRelationship;

public class HousePACCalculator {

	private HousePACCalculator() {
		// Utility class
	}

	/**
	 * Computes PAC analysis for all 12 houses from the chart Ascendant.
	 */
	public static List<HousePACData> calculateAll(Horoscope horoscope) {
		if (horoscope == null) return Collections.emptyList();
		return calculateAll(horoscope.getPlanetaryInfo(), horoscope.getAscendant());
	}

	/**
	 * Computes PAC analysis for all 12 houses given PlanetaryInfo and Ascendant.
	 */
	public static List<HousePACData> calculateAll(PlanetaryInfo pi, Rasi ascendant) {
		List<HousePACData> result = new ArrayList<>(12);
		for (int h = 1; h <= 12; h++) {
			result.add(calculatePAC(pi, ascendant, h));
		}
		return result;
	}

	/**
	 * Computes PAC analysis for a single house (1 to 12).
	 */
	public static HousePACData calculatePAC(Horoscope horoscope, int houseNum) {
		if (horoscope == null) return null;
		return calculatePAC(horoscope.getPlanetaryInfo(), horoscope.getAscendant(), houseNum);
	}

	/**
	 * Computes PAC analysis for a single house (1 to 12) given PlanetaryInfo and Ascendant.
	 */
	public static HousePACData calculatePAC(PlanetaryInfo pi, Rasi ascendant, int houseNum) {
		if (pi == null || ascendant == null || houseNum < 1 || houseNum > 12) {
			return null;
		}

		EnumMap<Planet, Rasi> planetRasi = pi.getPlanetRasi();
		if (planetRasi == null) {
			return null;
		}

		// 1. House sign and lord
		Rasi houseRasi = Rasi.values()[(ascendant.ordinal() + houseNum - 1) % 12];
		Planet lord = houseRasi.lord();

		// 2. Classifications
		List<String> classifications = new ArrayList<>();
		if (AstroConsts.IS_KENDRA[houseNum]) classifications.add("Kendra");
		if (AstroConsts.IS_TRIKONA[houseNum]) classifications.add("Trikona");
		if (AstroConsts.IS_PANAPHARA[houseNum]) classifications.add("Panaphara");
		if (AstroConsts.IS_APOKLIMA[houseNum]) classifications.add("Apoklima");
		if (AstroConsts.IS_UPACHAYA[houseNum]) classifications.add("Upachaya");
		if (AstroConsts.IS_DUSTHANA[houseNum]) classifications.add("Dusthana");
		if (AstroConsts.IS_MARAKA[houseNum]) classifications.add("Maraka");

		// 3. Lord Placement
		LordPlacement lordPlacement = null;
		if (lord != null) {
			Rasi lordR = planetRasi.get(lord);
			int lordHouse = (lordR != null) ? lordR.bhava(ascendant) : 1;
			SignRelationship dignity = (lordR != null) ? PlanetStatusCalculator.signRelationship(lord, lordR) : null;
			FunctionalRole role = PlanetCharacter.getFunctionalRole(ascendant, lord);
			boolean isYk = PlanetCharacter.isYogakaraka(ascendant, lord);
			boolean isOwn = (lordHouse == houseNum);
			boolean isCombust = isPlanetCombust(pi, lord);
			boolean isRetro = isPlanetRetrograde(pi, lord);
			lordPlacement = new LordPlacement(lord, houseNum, lordHouse, lordR, dignity, role, isYk, isOwn, isCombust, isRetro);
		}

		// 4. Occupants
		List<OccupantInfo> occupants = new ArrayList<>();
		for (Planet p : Planet.allPlanets()) {
			Rasi r = planetRasi.get(p);
			if (r != null && r.bhava(ascendant) == houseNum) {
				SignRelationship dignity = PlanetStatusCalculator.signRelationship(p, r);
				FunctionalRole role = PlanetCharacter.getFunctionalRole(ascendant, p);
				boolean isYk = PlanetCharacter.isYogakaraka(ascendant, p);
				boolean isCombust = isPlanetCombust(pi, p);
				boolean isRetro = isPlanetRetrograde(pi, p);
				occupants.add(new OccupantInfo(p, dignity, role, isYk, isCombust, isRetro));
			}
		}

		// 5. Aspects (O(1) table lookup)
		List<AspectInfo> aspects = new ArrayList<>();
		for (Planet p : Planet.allPlanets()) {
			Rasi r = planetRasi.get(p);
			if (r == null) continue;
			int fromHouse = r.bhava(ascendant);
			if (fromHouse == houseNum) continue; // occupants are conjunctions

			String aspectName = AstroConsts.getAspectName(p, fromHouse, houseNum);
			if (aspectName != null) {
				FunctionalRole role = PlanetCharacter.getFunctionalRole(ascendant, p);
				boolean isYk = PlanetCharacter.isYogakaraka(ascendant, p);
				boolean isOwnLord = (p == lord);
				aspects.add(new AspectInfo(p, fromHouse, r, aspectName, role, isYk, isOwnLord));
			}
		}
		Collections.sort(aspects);

		// 6. Conjunctions & Yogas strictly within this house
		List<String> conjunctions = new ArrayList<>();
		Set<String> yogas = new LinkedHashSet<>();

		if (occupants.size() >= 2) {
			String inHouseStr = " in " + ordinal(houseNum) + " house in " + houseRasi.englishName();
			if (occupants.size() == 2) {
				String p1Str = formatPlanetWithLordship(occupants.get(0).getPlanet(), ascendant);
				String p2Str = formatPlanetWithLordship(occupants.get(1).getPlanet(), ascendant);
				conjunctions.add(p1Str + " is conjoined with " + p2Str + inHouseStr);
			} else {
				List<String> pStrs = new ArrayList<>();
				for (OccupantInfo occ : occupants) {
					pStrs.add(formatPlanetWithLordship(occ.getPlanet(), ascendant));
				}
				int lastIdx = pStrs.size() - 1;
				String allExceptLast = String.join(", ", pStrs.subList(0, lastIdx));
				conjunctions.add(allExceptLast + " and " + pStrs.get(lastIdx) + " are conjoined" + inHouseStr);
			}

			// Check pairs for Raja Yoga and Dhana Yoga
			for (int i = 0; i < occupants.size(); i++) {
				for (int j = i + 1; j < occupants.size(); j++) {
					Planet p1 = occupants.get(i).getPlanet();
					Planet p2 = occupants.get(j).getPlanet();
					detectYogasBetweenPlanets(p1, p2, ascendant, houseNum, yogas);
				}
			}
		}

		return new HousePACData(houseNum, houseRasi, lord, classifications, lordPlacement,
				occupants, aspects, conjunctions, new ArrayList<>(yogas));
	}

	private static void detectYogasBetweenPlanets(Planet p1, Planet p2, Rasi ascendant, int inHouse, Set<String> yogas) {
		if (p1 == null || p2 == null || p1 == p2 || p1.isNode() || p2.isNode() || ascendant == null) {
			return;
		}

		List<Integer> owned1 = getOwnedHouses(p1, ascendant);
		List<Integer> owned2 = getOwnedHouses(p2, ascendant);

		for (int h1 : owned1) {
			for (int h2 : owned2) {
				if (h1 != h2) {
					// 1. Kendra-Trikona Raja Yoga
					if (AstroConsts.isKendraTrikonaPair(h1, h2)) {
						yogas.add("[Kendra-Trikona Raja Yoga: " + ordinal(h1) + " Lord (" + p1.name() + ") & "
								+ ordinal(h2) + " Lord (" + p2.name() + ") in House " + inHouse + "]");
					}

					// 2. Dhana Yoga
					if (AstroConsts.isDhanaPair(h1, h2)) {
						yogas.add("[Dhana Yoga: " + ordinal(h1) + " Lord (" + p1.name() + ") & "
								+ ordinal(h2) + " Lord (" + p2.name() + ") in House " + inHouse + "]");
					}
				}
			}
		}
	}

	public static List<Integer> getOwnedHouses(Planet p, Rasi ascendant) {
		if (p == null || p.isAsc() || p.isNode() || ascendant == null) {
			return Collections.emptyList();
		}
		List<Integer> houses = new ArrayList<>(2);
		for (Rasi r : Rasi.values()) {
			if (p.isOwnerOf(r)) {
				houses.add(r.bhava(ascendant));
			}
		}
		Collections.sort(houses);
		return houses;
	}

	private static boolean isPlanetCombust(PlanetaryInfo pi, Planet p) {
		return pi.getPlanetCombustion() != null && Boolean.TRUE.equals(pi.getPlanetCombustion().get(p));
	}

	private static boolean isPlanetRetrograde(PlanetaryInfo pi, Planet p) {
		return pi.getPlanetDirection() != null && Boolean.TRUE.equals(pi.getPlanetDirection().get(p));
	}

	public static String formatPlanetWithLordship(Planet p, Rasi ascendant) {
		if (p == null || p.isNode() || p.isAsc()) {
			return p != null ? p.name() : "";
		}
		List<Integer> owned = getOwnedHouses(p, ascendant);
		if (owned.isEmpty()) {
			return p.name();
		}
		List<String> ordinals = new ArrayList<>(owned.size());
		for (int h : owned) {
			ordinals.add(ordinal(h));
		}
		String houseLabel = ordinals.size() > 1 ? " houses" : " house";
		return p.name() + " (Lord of " + String.join(", ", ordinals) + houseLabel + ")";
	}

	public static String ordinal(int n) {
		if (n >= 11 && n <= 13) return n + "th";
		switch (n % 10) {
			case 1: return n + "st";
			case 2: return n + "nd";
			case 3: return n + "rd";
			default: return n + "th";
		}
	}
}
