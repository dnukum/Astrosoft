/**
 * YogaAnalysisTestHelper.java
 * Created On 2007, Oct 27, 2007 7:56:59 PM
 * Pure Java Test Helper replacing Drools session
 */
package app.astrosoft.xps.yoga.test;

import static app.astrosoft.util.CollectionUtil.newEnumMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import app.astrosoft.beans.PlanetCharacter;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Varga;
import app.astrosoft.consts.YogaCombination;
import app.astrosoft.core.Horoscope;
import app.astrosoft.test.SampleHoroscopes;
import app.astrosoft.xps.beans.PlanetChart;
import app.astrosoft.xps.beans.YogaFacts;
import app.astrosoft.xps.yoga.YogaFinder;
import app.astrosoft.xps.yoga.YogaResults;

public class YogaAnalysisTestHelper {

	public static enum SAMPLE_HOROSCOPE {RAJA, SUBA};

	private static PlanetCharacter character1;

	private static PlanetCharacter character2;

	public static final String YOGAS = "yogas";

	private static String activeRule;

	private static YogaSession session;

	private static Map<SAMPLE_HOROSCOPE, Horoscope> samples;

	public static class TestLogger {
		public void writeToDisk() {}
		public void setFileName(String fileName) {}
	}

	private static TestLogger logger = new TestLogger();

	public static class YogaSession {
		private String ruleName;
		private PlanetChart chart;
		private YogaFacts facts;
		private YogaResults yogas = new YogaResults();

		public YogaSession(String ruleName) {
			this.ruleName = ruleName;
		}

		public void fireAllRules() {
			yogas = YogaFinder.getInstance().findYogas(chart, facts, ruleName);
		}

		public Object getGlobal(String name) {
			if (YOGAS.equals(name)) {
				return yogas;
			}
			if ("$facts".equals(name)) {
				return facts;
			}
			return null;
		}

		public void setGlobal(String name, Object val) {
			if (YOGAS.equals(name)) {
				this.yogas = (YogaResults) val;
			} else if ("$facts".equals(name)) {
				this.facts = (YogaFacts) val;
			}
		}

		public void insert(Object obj) {
			if (obj instanceof PlanetChart) {
				this.chart = (PlanetChart) obj;
			}
		}

		public void dispose() {
		}

		public void addEventListener(Object listener) {
		}
	}

	public static void setUpBeforeClass() throws Exception {
		System.out.println("setUpBeforeClass()");
		setUpHoroscope();
		setUpCharacter();
	}

	private static void setUpCharacter() {
		Map<Planet, Boolean> c1 = new EnumMap<Planet, Boolean>(Planet.class);
		Map<Planet, Boolean> c2 = new EnumMap<Planet, Boolean>(Planet.class);

		for (Planet p : Planet.subaPlanets()) {
			c1.put(p, true);
			c2.put(p, true);
		}
		for (Planet p : Planet.papaPlanets()) {
			c1.put(p, false);
			c2.put(p, false);
		}

		c1.put(Planet.Moon, true);
		c2.put(Planet.Moon, false);

		character1 = new PlanetCharacter(c1);
		character2 = new PlanetCharacter(c2);
	}

	private static void setUpHoroscope() {
		samples = new EnumMap<SAMPLE_HOROSCOPE, Horoscope>(SAMPLE_HOROSCOPE.class);
		Horoscope h = SampleHoroscopes.getMyHoroscope();
		samples.put(SAMPLE_HOROSCOPE.RAJA, h);
		samples.put(SAMPLE_HOROSCOPE.SUBA, SampleHoroscopes.getSubaHoroscope());
	}

	public static void setUp(String ruleName) throws Exception {
		activeRule = ruleName;
		session = new YogaSession(ruleName);
		YogaResults yogas = new YogaResults();
		session.setGlobal(YOGAS, yogas);
	}

	public static void filterRule(String ruleName) throws Exception {
		activeRule = ruleName;
		if (session != null) {
			session.ruleName = ruleName;
		}
	}

	public static void testYogaPresent(YogaCombination yoga, String efficacy) {
		YogaResults yogas = (YogaResults) session.getGlobal(YOGAS);
		assertTrue(yogas.getYogas().contains(new YogaResults.Result(yoga, efficacy)));
	}

	public static void testYogaPresent(YogaCombination yoga) {
		testYogaPresent(yoga, "100%");
	}

	public static void testYogaNotPresent(YogaCombination yoga) {
		YogaResults yogas = (YogaResults) session.getGlobal(YOGAS);
		assertFalse(yogas.hasYogaCombination(yoga));
	}

	@SafeVarargs
	public static PlanetChart constructChart(Varga varga, Map.Entry<Planet, Rasi>... entries) {
		Map<Planet, Rasi> planetHouses = newEnumMap(Planet.class, entries);
		return new PlanetChart(varga, planetHouses);
	}

	@SafeVarargs
	public static PlanetChart constructChart(Map.Entry<Planet, Rasi>... entries) {
		return constructChart(Varga.Rasi, entries);
	}

	public static void loadFacts(SAMPLE_HOROSCOPE sample) {
		Horoscope h = samples.get(sample);
		YogaFacts facts = new YogaFacts(h);
		session.setGlobal("$facts", facts);
		PlanetChart chart = new PlanetChart(Varga.Rasi, h.getPlanetaryInfo());
		session.insert(chart);
	}

	public static void loadFacts(PlanetChart chart, PlanetCharacter character, Set<Planet> powerfulPlanets) {
		YogaFacts facts = new YogaFacts();
		facts.setCharacter(character);
		facts.setPowerfulPlanets(powerfulPlanets);
		session.setGlobal("$facts", facts);
		session.insert(chart);
	}

	public static void loadFacts(PlanetChart chart) {
		loadFacts(chart, character1, null);
	}

	public static void loadFacts(PlanetChart chart, PlanetCharacter character) {
		loadFacts(chart, character, null);
	}

	public static void loadFacts(PlanetChart chart, Set<Planet> powerfulPlanets) {
		loadFacts(chart, character1, powerfulPlanets);
	}

	public static void loadFacts(PlanetChart chart, YogaFacts facts) {
		session.setGlobal("$facts", facts);
		session.insert(chart);
	}

	public static void tearDown() throws Exception {
		logger.writeToDisk();
		System.out.println(getSession().getGlobal("yogas"));
		session.dispose();
	}

	public static void printRules() {
	}

	public static YogaSession getSession() {
		return session;
	}

	public static TestLogger getLogger() {
		return logger;
	}

	public static PlanetCharacter getCharacter1() {
		return character1;
	}

	public static PlanetCharacter getCharacter2() {
		return character2;
	}
}
