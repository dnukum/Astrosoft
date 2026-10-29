/**
 * YogaFinder.java
 * Created On 2007, Nov 15, 2007 4:10:53 PM
 * Pure Java Yoga Evaluator replacing Drools 4
 */

package app.astrosoft.xps.yoga;

import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;

import app.astrosoft.beans.PlanetCharacter;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Varga;
import app.astrosoft.consts.YogaCombination;
import app.astrosoft.core.Horoscope;
import app.astrosoft.util.Mod;
import app.astrosoft.util.Timer;
import app.astrosoft.xps.beans.PlanetChart;
import app.astrosoft.xps.beans.YogaFacts;

public class YogaFinder {

	private static final Logger log = Logger.getLogger(YogaFinder.class.getName());

	private static YogaFinder instance = null;

	private YogaFinder() {
	}

	public static synchronized YogaFinder getInstance() {
		if (instance == null) {
			instance = new YogaFinder();
		}
		return instance;
	}

	public YogaResults findYogas(Horoscope h) {
		Timer t = new Timer();
		YogaFacts facts = new YogaFacts(h);
		PlanetChart chart = new PlanetChart(Varga.Rasi, h.getPlanetaryInfo());
		YogaResults yogas = findYogas(chart, facts);
		t.print("FindYoga Execution time for " + h.getPersonName());
		return yogas;
	}

	public YogaResults findYogas(PlanetChart chart, YogaFacts facts) {
		return findYogas(chart, facts, null);
	}

	public YogaResults findYogas(PlanetChart chart, YogaFacts facts, String ruleFilter) {
		YogaResults yogas = new YogaResults();
		if (chart == null) {
			return yogas;
		}
		if (facts == null) {
			facts = new YogaFacts();
		}
		ensureCharacter(facts);

		// Rule 1: Adhi Yoga
		if (shouldRun(ruleFilter, "Adhi Yoga")) {
			Set<Planet> p = planetsAt(chart, Planet.Moon, 6, 7, 8);
			if (facts.character().isAllBenefics(p)) {
				int count = p.size();
				if (count >= 3) {
					yogas.addYoga(YogaCombination.AdhiYoga, "100%");
				} else if (count >= 2) {
					yogas.addYoga(YogaCombination.AdhiYoga, "75%");
				}
			}
		}

		// Rule 2: Amala Yoga
		if (shouldRun(ruleFilter, "Amala Yoga")) {
			Set<Planet> moon10 = planetsAt(chart, Planet.Moon, 10);
			Set<Planet> asc10 = planetsAt(chart, Planet.Ascendant, 10);
			if (facts.character().isAnyBenefics(moon10) || facts.character().isAnyBenefics(asc10)) {
				yogas.addYoga(YogaCombination.AmalaYoga, "100%");
			}
		}

		// Rule 3: Amsavatara Yoga
		if (shouldRun(ruleFilter, "Amsavatara Yoga")) {
			if (isPlanetInKendra(chart, Planet.Jupiter)
					&& isPlanetInKendra(chart, Planet.Venus)
					&& isPlanetInKendra(chart, Planet.Saturn)
					&& isPlanetExalted(chart, Planet.Saturn)
					&& houseOf(chart, 1) != null && houseOf(chart, 1).isMovableSign()) {
				yogas.addYoga(YogaCombination.AmsavataraYoga, "100%");
			}
		}

		// Rule 4: Anapha Yoga
		if (shouldRun(ruleFilter, "Anapha Yoga")) {
			Set<Planet> p = planetsAt(chart, Planet.Moon, 12);
			if (hasAnyOfPlanet(p, Planet.Mars, Planet.Mercury, Planet.Jupiter, Planet.Venus, Planet.Saturn)) {
				if (facts.character().isAnyMalefics(p)) {
					yogas.addYoga(YogaCombination.AnaphaYoga, "50%");
				} else {
					yogas.addYoga(YogaCombination.AnaphaYoga, "100%");
				}
			}
		}

		// Rule 5: Bhadra Yoga
		if (shouldRun(ruleFilter, "Bhadra Yoga")) {
			if (isPlanetInKendra(chart, Planet.Mercury)
					&& (isPlanetInOwnHouse(chart, Planet.Mercury) || isPlanetExalted(chart, Planet.Mercury))) {
				yogas.addYoga(YogaCombination.BhadraYoga, "100%");
			}
		}

		// Rule 6: Bheri Yoga
		if (shouldRun(ruleFilter, "Bheri Yoga")) {
			Planet lord1 = lordOf(chart, 1);
			Planet lord9 = lordOf(chart, 9);
			if (lord1 != null && lord9 != null
					&& isPlanetInKendraTo(chart, Planet.Venus, Planet.Jupiter)
					&& isPlanetInKendraTo(chart, Planet.Venus, lord1)
					&& isPlanetStronglyDisposed(chart, lord9)) {
				yogas.addYoga(YogaCombination.BheriYoga, "100%");
			}
		}

		// Rule 7: Budha-Aditya Yoga
		if (shouldRun(ruleFilter, "Budha-Aditya Yoga")) {
			if (isPlanetWith(chart, Planet.Mercury, Planet.Sun)) {
				yogas.addYoga(YogaCombination.BudhaAdityaYoga, "100%");
			}
		}

		// Rule 8: Chandra Mangla Yoga
		if (shouldRun(ruleFilter, "Chandra Mangla Yoga")) {
			if (isPlanetWith(chart, Planet.Moon, Planet.Mars)) {
				yogas.addYoga(YogaCombination.ChandraManglaYoga, "100%");
			}
		}

		// Rule 9: Chapa Yoga
		if (shouldRun(ruleFilter, "Chapa Yoga")) {
			Planet lord1 = lordOf(chart, 1);
			Planet lord4 = lordOf(chart, 4);
			Planet lord10 = lordOf(chart, 10);
			if (lord1 != null && lord4 != null && lord10 != null
					&& isPlanetExalted(chart, lord1)
					&& isPlanetAt(chart, lord4, 10)
					&& isPlanetAt(chart, lord10, 4)) {
				yogas.addYoga(YogaCombination.ChapaYoga, "100%");
			}
		}

		// Rule 10: Chatussagara Yoga
		if (shouldRun(ruleFilter, "Chatussagara Yoga")) {
			if (isPresent(chart, Planet.Ascendant)
					&& !removeNodes(planetsAt(chart, Planet.Ascendant, 1)).isEmpty()
					&& !removeNodes(planetsAt(chart, Planet.Ascendant, 4)).isEmpty()
					&& !removeNodes(planetsAt(chart, Planet.Ascendant, 7)).isEmpty()
					&& !removeNodes(planetsAt(chart, Planet.Ascendant, 10)).isEmpty()) {
				yogas.addYoga(YogaCombination.ChatussagaraYoga, "100%");
			}
		}

		// Rule 11: Devendra Yoga
		if (shouldRun(ruleFilter, "Devendra Yoga")) {
			Rasi h1 = houseOf(chart, 1);
			Planet lord1 = lordOf(chart, 1);
			Planet lord11 = lordOf(chart, 11);
			Planet lord2 = lordOf(chart, 2);
			Planet lord10 = lordOf(chart, 10);
			if (h1 != null && h1.isFixedSign()
					&& lord1 != null && lord11 != null && lord2 != null && lord10 != null
					&& isPlanetAt(chart, lord1, 11)
					&& isPlanetAt(chart, lord11, 1)
					&& isPlanetAt(chart, lord2, 10)
					&& isPlanetAt(chart, lord10, 2)) {
				yogas.addYoga(YogaCombination.DevendraYoga, "100%");
			}
		}

		// Rule 12: Dhurdhura Yoga
		if (shouldRun(ruleFilter, "Dhurdhura Yoga")) {
			Set<Planet> p2 = planetsAt(chart, Planet.Moon, 2);
			Set<Planet> p12 = planetsAt(chart, Planet.Moon, 12);
			if (hasAnyOfPlanet(p2, Planet.Mars, Planet.Mercury, Planet.Jupiter, Planet.Venus, Planet.Saturn)
					&& hasAnyOfPlanet(p12, Planet.Mars, Planet.Mercury, Planet.Jupiter, Planet.Venus, Planet.Saturn)) {
				yogas.addYoga(YogaCombination.DhurdhuraYoga, "100%");
			}
		}

		// Rule 13: Gaja Yoga
		if (shouldRun(ruleFilter, "Gaja Yoga")) {
			Planet lord7 = lordOf(chart, 7);
			Planet lord11 = lordOf(chart, 11);
			if (lord7 != null && lord11 != null
					&& isPlanetAt(chart, lord7, 11)
					&& isPlanetAt(chart, Planet.Moon, 11)
					&& isPlanetAspectedBy(chart, Planet.Moon, lord11)) {
				yogas.addYoga(YogaCombination.GajaYoga, "100%");
			}
		}

		// Rule 14: Gajakesari Yoga
		if (shouldRun(ruleFilter, "Gajakesari Yoga")) {
			if (isPlanetInKendraTo(chart, Planet.Jupiter, Planet.Moon)) {
				yogas.addYoga(YogaCombination.GajakesariYoga, "100%");
			}
		}

		// Rule 15: Go Yoga
		if (shouldRun(ruleFilter, "Go Yoga")) {
			Planet lord2 = lordOf(chart, 2);
			Planet lord1 = lordOf(chart, 1);
			if (lord2 != null && lord1 != null
					&& lord2 != Planet.Jupiter
					&& isPlanetWith(chart, lord2, Planet.Jupiter)
					&& isPlanetInMoolaTrikona(chart, Planet.Jupiter)
					&& isPlanetExalted(chart, lord1)) {
				yogas.addYoga(YogaCombination.GoYoga, "100%");
			}
		}

		// Rule 16: Jaya Yoga
		if (shouldRun(ruleFilter, "Jaya Yoga")) {
			Planet lord6 = lordOf(chart, 6);
			Planet lord10 = lordOf(chart, 10);
			if (lord6 != null && lord10 != null
					&& isPlanetDebilitated(chart, lord6)
					&& isPlanetExalted(chart, lord10)) {
				yogas.addYoga(YogaCombination.JayaYoga, "100%");
			}
		}

		// Rule 17: Hamsa Yoga
		if (shouldRun(ruleFilter, "Hamsa Yoga")) {
			if (isPlanetInKendra(chart, Planet.Jupiter)
					&& (isPlanetInOwnHouse(chart, Planet.Jupiter) || isPlanetExalted(chart, Planet.Jupiter))) {
				yogas.addYoga(YogaCombination.HamsaYoga, "100%");
			}
		}

		// Rule 18: Indra Yoga
		if (shouldRun(ruleFilter, "Indra Yoga")) {
			Planet lord5 = lordOf(chart, 5);
			Planet lord11 = lordOf(chart, 11);
			if (lord5 != null && lord11 != null
					&& isPlanetAt(chart, lord5, 11)
					&& isPlanetAt(chart, lord11, 5)
					&& isPlanetAt(chart, Planet.Moon, 5)) {
				yogas.addYoga(YogaCombination.IndraYoga, "100%");
			}
		}

		// Rule 19: Kusuma Yoga
		if (shouldRun(ruleFilter, "Kusuma Yoga")) {
			if (isPlanetAt(chart, Planet.Jupiter, 1)
					&& isPlanetAt(chart, Planet.Moon, 7)
					&& isPlanetInPositionTo(chart, Planet.Sun, Planet.Moon, 8)) {
				yogas.addYoga(YogaCombination.KusumaYoga, "100%");
			}
		}

		// Rule 20: Kahala Yoga
		if (shouldRun(ruleFilter, "Kahala Yoga")) {
			Planet lord1 = lordOf(chart, 1);
			Planet lord4 = lordOf(chart, 4);
			Planet lord9 = lordOf(chart, 9);
			if (lord1 != null && lord4 != null && lord9 != null
					&& isPlanetStronglyDisposed(chart, lord1)
					&& isPlanetInKendraTo(chart, lord4, lord9)) {
				yogas.addYoga(YogaCombination.KahalaYoga, "100%");
			}
		}

		// Rule 21: Kemadruma Yoga
		if (shouldRun(ruleFilter, "Kemadruma Yoga")) {
			if (isPresent(chart, Planet.Moon)
					&& planetsAt(chart, Planet.Moon, 2).isEmpty()
					&& planetsAt(chart, Planet.Moon, 12).isEmpty()) {
				yogas.addYoga(YogaCombination.KemadrumaYoga, "100%");
			}
		}

		// Rule 22: Lakshmi Yoga
		if (shouldRun(ruleFilter, "Lakshmi Yoga")) {
			Planet lord1 = lordOf(chart, 1);
			Planet lord9 = lordOf(chart, 9);
			if (lord1 != null && lord9 != null
					&& facts.isPlanetPowerful(lord1)
					&& isPlanetInOwnOrExaltedHouse(chart, lord9)
					&& isPlanetInKendraOrTrine(chart, lord9)) {
				yogas.addYoga(YogaCombination.LakshmiYoga, facts.getPlanetStrength(lord1));
			}
		}

		// Rule 23: Mahabhagya Yoga
		if (shouldRun(ruleFilter, "Mahabhagya Yoga")) {
			if (isPresent(chart, Planet.Sun) && isPresent(chart, Planet.Moon) && isPresent(chart, Planet.Ascendant)) {
				boolean maleDayOdd = facts.isMale()
						&& facts.isBirthAtDay()
						&& chart.getPlanetHouse(Planet.Sun).isOddSign()
						&& chart.getPlanetHouse(Planet.Moon).isOddSign()
						&& chart.getPlanetHouse(Planet.Ascendant).isOddSign();
				boolean femaleNightEven = facts.isFemale()
						&& !facts.isBirthAtDay()
						&& chart.getPlanetHouse(Planet.Sun).isEvenSign()
						&& chart.getPlanetHouse(Planet.Moon).isEvenSign()
						&& chart.getPlanetHouse(Planet.Ascendant).isEvenSign();
				if (maleDayOdd || femaleNightEven) {
					yogas.addYoga(YogaCombination.MahabhagyaYoga, "100%");
				}
			}
		}

		// Rule 24: Malavya Yoga
		if (shouldRun(ruleFilter, "Malavya Yoga")) {
			if (isPlanetInKendra(chart, Planet.Venus)
					&& (isPlanetInOwnHouse(chart, Planet.Venus) || isPlanetExalted(chart, Planet.Venus))) {
				yogas.addYoga(YogaCombination.MalavyaYoga, "100%");
			}
		}

		// Rule 25: Makuta Yoga
		if (shouldRun(ruleFilter, "Makuta Yoga")) {
			Planet lord9 = lordOf(chart, 9);
			if (lord9 != null
					&& isPlanetInPositionTo(chart, Planet.Jupiter, lord9, 9)
					&& facts.character().isAnyBenefics(planetsAt(chart, Planet.Jupiter, 9))
					&& isPlanetAt(chart, Planet.Saturn, 10)) {
				yogas.addYoga(YogaCombination.MakutaYoga, "100%");
			}
		}

		// Rule 26: Obhayachari Yoga
		if (shouldRun(ruleFilter, "Obhayachari Yoga")) {
			Set<Planet> p2 = planetsAt(chart, Planet.Sun, 2);
			Set<Planet> p12 = planetsAt(chart, Planet.Sun, 12);
			if (hasAnyOfPlanet(p2, Planet.Mars, Planet.Mercury, Planet.Jupiter, Planet.Venus, Planet.Saturn)
					&& hasAnyOfPlanet(p12, Planet.Mars, Planet.Mercury, Planet.Jupiter, Planet.Venus, Planet.Saturn)) {
				yogas.addYoga(YogaCombination.ObhayachariYoga, "100%");
			}
		}

		// Rule 27: Rajalakshana Yoga
		if (shouldRun(ruleFilter, "Rajalakshana Yoga")) {
			if (isPlanetInKendra(chart, Planet.Moon)
					&& isPlanetInKendra(chart, Planet.Mercury)
					&& isPlanetInKendra(chart, Planet.Jupiter)
					&& isPlanetInKendra(chart, Planet.Venus)) {
				yogas.addYoga(YogaCombination.RajalakshanaYoga, "100%");
			}
		}

		// Rule 28: Ravi Yoga
		if (shouldRun(ruleFilter, "Ravi Yoga")) {
			Planet lord10 = lordOf(chart, 10);
			if (lord10 != null
					&& lord10 != Planet.Saturn
					&& isPlanetAt(chart, Planet.Sun, 10)
					&& isPlanetAt(chart, Planet.Saturn, 3)
					&& isPlanetAt(chart, lord10, 3)) {
				yogas.addYoga(YogaCombination.RaviYoga, "100%");
			}
		}

		// Rule 29: Ruchaka Yoga
		if (shouldRun(ruleFilter, "Ruchaka Yoga")) {
			if (isPlanetInKendra(chart, Planet.Mars)
					&& (isPlanetInOwnHouse(chart, Planet.Mars) || isPlanetExalted(chart, Planet.Mars))) {
				yogas.addYoga(YogaCombination.RuchakaYoga, "100%");
			}
		}

		// Rule 30: Sakata Yoga
		if (shouldRun(ruleFilter, "Sakata Yoga")) {
			if (isPlanetInPositionTo(chart, Planet.Moon, Planet.Jupiter, 6, 8, 12)) {
				yogas.addYoga(YogaCombination.SakataYoga, "100%");
			}
		}

		// Rule 31: Sankha Yoga
		if (shouldRun(ruleFilter, "Sankha Yoga")) {
			Planet lord1 = lordOf(chart, 1);
			Planet lord5 = lordOf(chart, 5);
			Planet lord6 = lordOf(chart, 6);
			if (lord1 != null && lord5 != null && lord6 != null
					&& isPlanetStronglyDisposed(chart, lord1)
					&& isPlanetInKendraTo(chart, lord5, lord6)) {
				yogas.addYoga(YogaCombination.SankhaYoga, "100%");
			}
		}

		// Rule 32: Sreenatha Yoga
		if (shouldRun(ruleFilter, "Sreenatha Yoga")) {
			Planet lord7 = lordOf(chart, 7);
			Planet lord10 = lordOf(chart, 10);
			Planet lord9 = lordOf(chart, 9);
			if (lord7 != null && lord10 != null && lord9 != null
					&& isPlanetExalted(chart, lord7)
					&& isPlanetAt(chart, lord7, 10)
					&& isPlanetWith(chart, lord10, lord9)) {
				yogas.addYoga(YogaCombination.SreenathaYoga, "100%");
			}
		}

		// Rule 33: Sasa Yoga
		if (shouldRun(ruleFilter, "Sasa Yoga")) {
			if (isPlanetInKendra(chart, Planet.Saturn)
					&& (isPlanetInOwnHouse(chart, Planet.Saturn) || isPlanetExalted(chart, Planet.Saturn))) {
				yogas.addYoga(YogaCombination.SasaYoga, "100%");
			}
		}

		// Rule 34: Siva Yoga
		if (shouldRun(ruleFilter, "Siva Yoga")) {
			Planet lord5 = lordOf(chart, 5);
			Planet lord9 = lordOf(chart, 9);
			Planet lord10 = lordOf(chart, 10);
			if (lord5 != null && lord9 != null && lord10 != null
					&& isPlanetAt(chart, lord5, 9)
					&& isPlanetAt(chart, lord9, 10)
					&& isPlanetAt(chart, lord10, 5)) {
				yogas.addYoga(YogaCombination.SivaYoga, "100%");
			}
		}

		// Rule 35: Sunapha Yoga
		if (shouldRun(ruleFilter, "Sunapha Yoga")) {
			Set<Planet> p = planetsAt(chart, Planet.Moon, 2);
			if (hasAnyOfPlanet(p, Planet.Mars, Planet.Mercury, Planet.Jupiter, Planet.Venus, Planet.Saturn)) {
				if (facts.character().isAnyMalefics(p)) {
					yogas.addYoga(YogaCombination.SunaphaYoga, "50%");
				} else {
					yogas.addYoga(YogaCombination.SunaphaYoga, "100%");
				}
			}
		}

		// Rule 36: SubhaVasi Yoga
		if (shouldRun(ruleFilter, "SubhaVasi Yoga")) {
			Set<Planet> p = planetsAt(chart, Planet.Sun, 12);
			if (hasAnyOfPlanet(p, Planet.Mercury, Planet.Jupiter, Planet.Venus)) {
				yogas.addYoga(YogaCombination.SubhaVasiYoga, "100%");
			}
		}

		// Rule 37: PapaVasi Yoga
		if (shouldRun(ruleFilter, "PapaVasi Yoga")) {
			Set<Planet> p = planetsAt(chart, Planet.Sun, 12);
			if (hasAnyOfPlanet(p, Planet.Mars, Planet.Saturn)) {
				yogas.addYoga(YogaCombination.PapaVasiYoga, "100%");
			}
		}

		// Rule 38: SubhaVesi Yoga
		if (shouldRun(ruleFilter, "SubhaVesi Yoga")) {
			Set<Planet> p = planetsAt(chart, Planet.Sun, 2);
			if (hasAnyOfPlanet(p, Planet.Mercury, Planet.Jupiter, Planet.Venus)) {
				yogas.addYoga(YogaCombination.SubhaVesiYoga, "100%");
			}
		}

		// Rule 39: PapaVesi Yoga
		if (shouldRun(ruleFilter, "PapaVesi Yoga")) {
			Set<Planet> p = planetsAt(chart, Planet.Sun, 2);
			if (hasAnyOfPlanet(p, Planet.Mars, Planet.Saturn)) {
				yogas.addYoga(YogaCombination.PapaVesiYoga, "100%");
			}
		}

		// Rule 40: Thrilochana Yoga
		if (shouldRun(ruleFilter, "Thrilochana Yoga")) {
			if (isPlanetInTrineTo(chart, Planet.Sun, Planet.Moon)
					&& isPlanetInTrineTo(chart, Planet.Moon, Planet.Mars)) {
				yogas.addYoga(YogaCombination.ThrilochanaYoga, "100%");
			}
		}

		// Rule 41: Vasumathi Yoga
		if (shouldRun(ruleFilter, "Vasumathi Yoga")) {
			Set<Planet> moonP = planetsAt(chart, Planet.Moon, 3, 6, 10, 11);
			Set<Planet> ascP = planetsAt(chart, Planet.Ascendant, 3, 6, 10, 11);
			boolean moonBenefics = facts.character().isAllBenefics(moonP);
			boolean ascBenefics = facts.character().isAllBenefics(ascP);

			if (moonBenefics || ascBenefics) {
				int count = Math.max(moonP.size(), ascP.size());
				if (count >= 3) {
					yogas.addYoga(YogaCombination.VasumathiYoga, "100%");
				} else if (count >= 2) {
					yogas.addYoga(YogaCombination.VasumathiYoga, "75%");
				}
			}
		}

		// Rule 42: Vidyut Yoga
		if (shouldRun(ruleFilter, "Vidyut Yoga")) {
			Planet lord11 = lordOf(chart, 11);
			Planet lord1 = lordOf(chart, 1);
			if (lord11 != null && lord1 != null
					&& isPlanetExalted(chart, lord11)
					&& isPlanetWith(chart, lord11, Planet.Venus)
					&& isPlanetInKendraTo(chart, Planet.Venus, lord1)) {
				yogas.addYoga(YogaCombination.VidyutYoga, "100%");
			}
		}

		return yogas;
	}

	private static boolean shouldRun(String ruleFilter, String ruleName) {
		return ruleFilter == null || ruleFilter.equalsIgnoreCase(ruleName);
	}

	private static boolean isPresent(PlanetChart chart, Planet p) {
		return chart != null && p != null && chart.getPlanetHouse(p) != null;
	}

	private static Planet lordOf(PlanetChart chart, int loc) {
		if (!isPresent(chart, Planet.Ascendant)) {
			return null;
		}
		return chart.lordOf(loc);
	}

	private static Rasi houseOf(PlanetChart chart, int loc) {
		if (!isPresent(chart, Planet.Ascendant)) {
			return null;
		}
		return chart.houseOf(loc);
	}

	private static boolean isPlanetAt(PlanetChart chart, Planet p, int loc) {
		return isPresent(chart, p) && chart.isPlanetAt(p, loc);
	}

	private static boolean isPlanetWith(PlanetChart chart, Planet p1, Planet p2) {
		return isPresent(chart, p1) && isPresent(chart, p2) && chart.isPlanetWith(p1, p2);
	}

	private static boolean isPlanetInKendra(PlanetChart chart, Planet p) {
		return isPresent(chart, p) && chart.isPlanetInKendra(p);
	}

	private static boolean isPlanetInKendraTo(PlanetChart chart, Planet p1, Planet p2) {
		return isPresent(chart, p1) && isPresent(chart, p2) && chart.isPlanetInKendraTo(p1, p2);
	}

	private static boolean isPlanetInTrineTo(PlanetChart chart, Planet p1, Planet p2) {
		return isPresent(chart, p1) && isPresent(chart, p2) && chart.isPlanetInTrineTo(p1, p2);
	}

	private static boolean isPlanetInKendraOrTrine(PlanetChart chart, Planet p) {
		return isPresent(chart, p) && chart.isPlanetInKendraOrTrine(p);
	}

	private static boolean isPlanetExalted(PlanetChart chart, Planet p) {
		return isPresent(chart, p) && chart.isPlanetExalted(p);
	}

	private static boolean isPlanetDebilitated(PlanetChart chart, Planet p) {
		return isPresent(chart, p) && chart.isPlanetDebilitated(p);
	}

	private static boolean isPlanetInOwnHouse(PlanetChart chart, Planet p) {
		return isPresent(chart, p) && chart.isPlanetInOwnHouse(p);
	}

	private static boolean isPlanetInOwnOrExaltedHouse(PlanetChart chart, Planet p) {
		return isPresent(chart, p) && chart.isPlanetInOwnOrExaltedHouse(p);
	}

	private static boolean isPlanetInMoolaTrikona(PlanetChart chart, Planet p) {
		return isPresent(chart, p) && chart.isPlanetInMoolaTrikona(p);
	}

	private static boolean isPlanetStronglyDisposed(PlanetChart chart, Planet p) {
		return isPresent(chart, p) && chart.isPlanetStronglyDisposed(p);
	}

	private static boolean isPlanetAspectedBy(PlanetChart chart, Planet aspected, Planet aspecting) {
		return isPresent(chart, aspected) && isPresent(chart, aspecting) && chart.isPlanetAspectedBy(aspected, aspecting);
	}

	private static boolean isPlanetInPositionTo(PlanetChart chart, Planet p1, Planet p2, int... positions) {
		if (!isPresent(chart, p1) || !isPresent(chart, p2)) {
			return false;
		}
		Mod mod = new Mod(12);
		int distance = mod.sub(chart.getPlanetLocation(p1), chart.getPlanetLocation(p2)) + 1;
		for (int pos : positions) {
			if (distance == pos) {
				return true;
			}
		}
		return false;
	}

	private static Set<Planet> planetsAt(PlanetChart chart, Planet from, int... locations) {
		Set<Planet> planets = EnumSet.noneOf(Planet.class);
		if (!isPresent(chart, from)) {
			return planets;
		}
		for (int loc : locations) {
			planets.addAll(chart.planetsAt(from, loc));
		}
		planets.remove(Planet.Ascendant);
		return planets;
	}

	private static Set<Planet> removeNodes(Set<Planet> planets) {
		Set<Planet> copy = EnumSet.noneOf(Planet.class);
		copy.addAll(planets);
		copy.remove(Planet.Rahu);
		copy.remove(Planet.Ketu);
		return copy;
	}

	private static boolean hasAnyOfPlanet(Set<Planet> source, Planet... targets) {
		if (source == null || source.isEmpty()) {
			return false;
		}
		for (Planet t : targets) {
			if (source.contains(t)) {
				return true;
			}
		}
		return false;
	}

	private static void ensureCharacter(YogaFacts facts) {
		if (facts.character() == null) {
			java.util.Map<Planet, Boolean> c = new java.util.EnumMap<Planet, Boolean>(Planet.class);
			for (Planet p : Planet.subaPlanets()) {
				c.put(p, true);
			}
			for (Planet p : Planet.papaPlanets()) {
				c.put(p, false);
			}
			c.put(Planet.Moon, true);
			facts.setCharacter(new PlanetCharacter(c));
		}
	}
}
