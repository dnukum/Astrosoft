/**
 * PlanetCharacter.java
 * Created On 2007, Oct 22, 2007 1:23:45 PM
 * @author E. Rajasekar
 */

package app.astrosoft.beans;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import app.astrosoft.consts.FunctionalRole;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;

public class PlanetCharacter {

	private Map<Planet, Boolean> planetCharacter;
	private Rasi lagna;

	/**
	 * =========================================================================
	 * DR. B.V. RAMAN FUNCTIONAL PLANETARY CLASSIFICATION BY ASCENDANT (LAGNA)
	 * =========================================================================
	 * In Vedic Astrology, while natural benefics/malefics are fixed, functional
	 * nature is dynamic and determined strictly relative to the Ascendant (Lagna).
	 *
	 * Authoritative Baseline: Dr. B.V. Raman
	 * Primary Source:
	 *   - "How to Judge a Horoscope", Vol. 1, Chapter 1:
	 *     "Benefics and Malefics for Each Lagna" (pp. 7–9).
	 * Secondary Source:
	 *   - "Astrology for Beginners", Chapter XV: "Hints on Judgment" (pp. 52–53).
	 *
	 * Note on Dr. B.V. Raman's Empirical Framework:
	 * Where classical textbook Parashari definitions evaluate literal house
	 * ownerships (such as 2nd/7th as Marakas), Dr. Raman observed practical
	 * astrological realities:
	 * 1. For Libra (Thula): Mars rules the 2nd and 7th houses, but sheds its
	 *    natural maleficence through the 7th Kendra lordship and rules the 2nd
	 *    house of wealth (Dhana Bhava). Raman explicitly classifies Mars as a
	 *    "feeble benefic".
	 * 2. For Taurus (Vrishabha): Saturn is the supreme Yogakaraka (9th & 10th lord);
	 *    Mercury, Mars, and Sun are also functional benefics. Venus is inherently
	 *    evil as 6th lord, but elevated to Neutral by Lagna lordship.
	 * 3. For Aquarius (Kumbha): Venus is Yogakaraka (4th & 9th); Sun, Mars, and
	 *    Saturn (Lagna lord) are functional benefics.
	 * 4. Marakas: In practical prediction (Phalita), Dr. Raman noted that operational
	 *    malefics (Trishadaya lords: 3, 6, 11; and 8th lord) frequently act as the
	 *    active death-inflicting agents, as cataloged in this table.
	 * =========================================================================
	 */
	private static class FunctionalData {
		final Set<Planet> benefics;
		final Set<Planet> malefics;
		final Set<Planet> neutrals;
		final Planet yogakaraka;
		final Set<Planet> marakas;

		FunctionalData(EnumSet<Planet> benefics, EnumSet<Planet> malefics,
				EnumSet<Planet> neutrals, Planet yogakaraka, EnumSet<Planet> marakas) {
			this.benefics = Collections.unmodifiableSet(benefics);
			this.malefics = Collections.unmodifiableSet(malefics);
			this.neutrals = Collections.unmodifiableSet(neutrals);
			this.yogakaraka = yogakaraka;
			this.marakas = Collections.unmodifiableSet(marakas);
		}
	}

	private static final EnumMap<Rasi, FunctionalData> FUNCTIONAL_TABLE = new EnumMap<>(Rasi.class);

	static {
		// 1. Aries (Mesha)
		FUNCTIONAL_TABLE.put(Rasi.Mesha, new FunctionalData(
			EnumSet.of(Planet.Sun, Planet.Mars, Planet.Jupiter),
			EnumSet.of(Planet.Saturn, Planet.Mercury, Planet.Venus),
			EnumSet.of(Planet.Moon),
			null,
			EnumSet.of(Planet.Mercury, Planet.Saturn, Planet.Venus)
		));

		// 2. Taurus (Vrishabha)
		FUNCTIONAL_TABLE.put(Rasi.Vrishabha, new FunctionalData(
			EnumSet.of(Planet.Saturn, Planet.Mercury, Planet.Mars, Planet.Sun),
			EnumSet.of(Planet.Jupiter, Planet.Moon),
			EnumSet.of(Planet.Venus),
			Planet.Saturn,
			EnumSet.of(Planet.Jupiter, Planet.Venus, Planet.Moon)
		));

		// 3. Gemini (Mithuna)
		FUNCTIONAL_TABLE.put(Rasi.Mithuna, new FunctionalData(
			EnumSet.of(Planet.Venus, Planet.Saturn),
			EnumSet.of(Planet.Mars, Planet.Jupiter, Planet.Sun),
			EnumSet.of(Planet.Moon, Planet.Mercury),
			null,
			EnumSet.of(Planet.Mars, Planet.Jupiter)
		));

		// 4. Cancer (Kataka)
		FUNCTIONAL_TABLE.put(Rasi.Kataka, new FunctionalData(
			EnumSet.of(Planet.Mars, Planet.Jupiter),
			EnumSet.of(Planet.Venus, Planet.Mercury),
			EnumSet.of(Planet.Saturn, Planet.Moon, Planet.Sun),
			Planet.Mars,
			EnumSet.of(Planet.Mercury, Planet.Venus)
		));

		// 5. Leo (Simha)
		FUNCTIONAL_TABLE.put(Rasi.Simha, new FunctionalData(
			EnumSet.of(Planet.Mars, Planet.Sun),
			EnumSet.of(Planet.Mercury, Planet.Venus),
			EnumSet.of(Planet.Jupiter, Planet.Moon, Planet.Saturn),
			Planet.Mars,
			EnumSet.of(Planet.Mercury, Planet.Venus)
		));

		// 6. Virgo (Kanya)
		FUNCTIONAL_TABLE.put(Rasi.Kanya, new FunctionalData(
			EnumSet.of(Planet.Venus),
			EnumSet.of(Planet.Moon, Planet.Mars, Planet.Jupiter),
			EnumSet.of(Planet.Saturn, Planet.Sun, Planet.Mercury),
			null,
			EnumSet.of(Planet.Mars, Planet.Jupiter, Planet.Venus)
		));

		// 7. Libra (Thula)
		FUNCTIONAL_TABLE.put(Rasi.Thula, new FunctionalData(
			EnumSet.of(Planet.Saturn, Planet.Mercury, Planet.Venus, Planet.Mars),
			EnumSet.of(Planet.Sun, Planet.Jupiter, Planet.Moon),
			EnumSet.noneOf(Planet.class),
			Planet.Saturn,
			EnumSet.of(Planet.Jupiter, Planet.Sun, Planet.Moon)
		));

		// 8. Scorpio (Vrichika)
		FUNCTIONAL_TABLE.put(Rasi.Vrichika, new FunctionalData(
			EnumSet.of(Planet.Moon, Planet.Jupiter, Planet.Sun),
			EnumSet.of(Planet.Mercury, Planet.Venus),
			EnumSet.of(Planet.Mars, Planet.Saturn),
			null,
			EnumSet.of(Planet.Mercury, Planet.Venus, Planet.Saturn)
		));

		// 9. Sagittarius (Dhanus)
		FUNCTIONAL_TABLE.put(Rasi.Dhanus, new FunctionalData(
			EnumSet.of(Planet.Mars, Planet.Sun),
			EnumSet.of(Planet.Venus, Planet.Saturn, Planet.Mercury),
			EnumSet.of(Planet.Jupiter, Planet.Moon),
			null,
			EnumSet.of(Planet.Venus, Planet.Saturn)
		));

		// 10. Capricorn (Makara)
		FUNCTIONAL_TABLE.put(Rasi.Makara, new FunctionalData(
			EnumSet.of(Planet.Venus, Planet.Mercury, Planet.Saturn),
			EnumSet.of(Planet.Mars, Planet.Jupiter, Planet.Moon),
			EnumSet.of(Planet.Sun),
			Planet.Venus,
			EnumSet.of(Planet.Mars, Planet.Jupiter)
		));

		// 11. Aquarius (Kumbha)
		FUNCTIONAL_TABLE.put(Rasi.Kumbha, new FunctionalData(
			EnumSet.of(Planet.Venus, Planet.Sun, Planet.Mars, Planet.Saturn),
			EnumSet.of(Planet.Jupiter, Planet.Moon),
			EnumSet.of(Planet.Mercury),
			Planet.Venus,
			EnumSet.of(Planet.Jupiter, Planet.Moon)
		));

		// 12. Pisces (Meena)
		FUNCTIONAL_TABLE.put(Rasi.Meena, new FunctionalData(
			EnumSet.of(Planet.Moon, Planet.Mars),
			EnumSet.of(Planet.Saturn, Planet.Sun, Planet.Venus, Planet.Mercury),
			EnumSet.of(Planet.Jupiter),
			null,
			EnumSet.of(Planet.Mercury, Planet.Saturn, Planet.Venus)
		));
	}

	public PlanetCharacter(Map<Planet, Boolean> planetCharacter) {
		this(planetCharacter, null);
	}

	public PlanetCharacter(Map<Planet, Boolean> planetCharacter, Rasi lagna) {
		this.planetCharacter = planetCharacter;
		this.lagna = lagna;
	}

	public Rasi getLagna() {
		return lagna;
	}

	public void setLagna(Rasi lagna) {
		this.lagna = lagna;
	}

	// -------------------------------------------------------------------------
	// Natural Benefic / Malefic methods (backward-compatible)
	// -------------------------------------------------------------------------
	public boolean isBenefic(Planet planet) {
		if (planetCharacter != null && planetCharacter.containsKey(planet)) {
			return planetCharacter.get(planet);
		}

		if (planet.isAsc()) {
			return true;
		}
		throw new IllegalArgumentException("isBenefic() is not applicable for " + planet.name());
	}

	public boolean isMalefic(Planet planet) {
		if (planetCharacter != null && planetCharacter.containsKey(planet)) {
			return (!planetCharacter.get(planet));
		}

		if (planet.isAsc()) {
			return true;
		}
		throw new IllegalArgumentException("isMalefic() is not applicable for " + planet.name());
	}

	public boolean isAllBenefics(Set<Planet> planets) {
		if (planets.isEmpty())
			return false;

		for (Planet p : planets) {
			if (!isBenefic(p)) {
				return false;
			}
		}
		return true;
	}

	public boolean isAnyBenefics(Set<Planet> planets) {
		if (planets.isEmpty())
			return false;

		for (Planet p : planets) {
			if (isBenefic(p)) {
				return true;
			}
		}
		return false;
	}

	public boolean isAllMalefics(Set<Planet> planets) {
		if (planets.isEmpty())
			return false;

		for (Planet p : planets) {
			if (!isMalefic(p)) {
				return false;
			}
		}
		return true;
	}

	public boolean isAnyMalefics(Set<Planet> planets) {
		if (planets.isEmpty())
			return false;

		for (Planet p : planets) {
			if (isMalefic(p)) {
				return true;
			}
		}
		return false;
	}

	// -------------------------------------------------------------------------
	// Static Functional Nature Accessors by Ascendant (Dr. B.V. Raman baseline)
	// -------------------------------------------------------------------------
	public static Set<Planet> getFunctionalBenefics(Rasi lagna) {
		if (lagna == null) return Collections.emptySet();
		FunctionalData data = FUNCTIONAL_TABLE.get(lagna);
		return data != null ? data.benefics : Collections.<Planet>emptySet();
	}

	public static Set<Planet> getFunctionalMalefics(Rasi lagna) {
		if (lagna == null) return Collections.emptySet();
		FunctionalData data = FUNCTIONAL_TABLE.get(lagna);
		return data != null ? data.malefics : Collections.<Planet>emptySet();
	}

	public static Set<Planet> getFunctionalNeutrals(Rasi lagna) {
		if (lagna == null) return Collections.emptySet();
		FunctionalData data = FUNCTIONAL_TABLE.get(lagna);
		return data != null ? data.neutrals : Collections.<Planet>emptySet();
	}

	public static Planet getYogakaraka(Rasi lagna) {
		if (lagna == null) return null;
		FunctionalData data = FUNCTIONAL_TABLE.get(lagna);
		return data != null ? data.yogakaraka : null;
	}

	public static Set<Planet> getMarakas(Rasi lagna) {
		if (lagna == null) return Collections.emptySet();
		FunctionalData data = FUNCTIONAL_TABLE.get(lagna);
		return data != null ? data.marakas : Collections.<Planet>emptySet();
	}

	public static FunctionalRole getFunctionalRole(Rasi lagna, Planet planet) {
		if (lagna == null || planet == null) return FunctionalRole.NEUTRAL;
		FunctionalData data = FUNCTIONAL_TABLE.get(lagna);
		if (data != null) {
			if (data.benefics.contains(planet)) return FunctionalRole.BENEFIC;
			if (data.malefics.contains(planet)) return FunctionalRole.MALEFIC;
		}
		return FunctionalRole.NEUTRAL;
	}

	public static boolean isFunctionalBenefic(Rasi lagna, Planet planet) {
		return lagna != null && getFunctionalBenefics(lagna).contains(planet);
	}

	public static boolean isFunctionalMalefic(Rasi lagna, Planet planet) {
		return lagna != null && getFunctionalMalefics(lagna).contains(planet);
	}

	public static boolean isFunctionalNeutral(Rasi lagna, Planet planet) {
		return lagna != null && getFunctionalNeutrals(lagna).contains(planet);
	}

	public static boolean isYogakaraka(Rasi lagna, Planet planet) {
		return lagna != null && planet != null && planet == getYogakaraka(lagna);
	}

	public static boolean isMaraka(Rasi lagna, Planet planet) {
		return lagna != null && getMarakas(lagna).contains(planet);
	}

	// -------------------------------------------------------------------------
	// Instance Functional Nature Accessors (bound to this.lagna)
	// -------------------------------------------------------------------------
	public FunctionalRole getFunctionalRole(Planet planet) {
		return getFunctionalRole(lagna, planet);
	}

	public boolean isFunctionalBenefic(Planet planet) {
		return isFunctionalBenefic(lagna, planet);
	}

	public boolean isFunctionalMalefic(Planet planet) {
		return isFunctionalMalefic(lagna, planet);
	}

	public boolean isFunctionalNeutral(Planet planet) {
		return isFunctionalNeutral(lagna, planet);
	}

	public boolean isYogakaraka(Planet planet) {
		return isYogakaraka(lagna, planet);
	}

	public boolean isMaraka(Planet planet) {
		return isMaraka(lagna, planet);
	}

	public Set<Planet> getFunctionalBenefics() {
		return getFunctionalBenefics(lagna);
	}

	public Set<Planet> getFunctionalMalefics() {
		return getFunctionalMalefics(lagna);
	}

	public Set<Planet> getFunctionalNeutrals() {
		return getFunctionalNeutrals(lagna);
	}

	public Planet getYogakaraka() {
		return getYogakaraka(lagna);
	}

	public Set<Planet> getMarakas() {
		return getMarakas(lagna);
	}

	@Override
	public String toString() {
		return (planetCharacter != null ? planetCharacter.toString() : "{}")
				+ (lagna != null ? " [Lagna=" + lagna.name() + "]" : "");
	}
}
