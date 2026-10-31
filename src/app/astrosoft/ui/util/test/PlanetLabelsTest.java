package app.astrosoft.ui.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Test;

import app.astrosoft.beans.HousePosition;
import app.astrosoft.beans.PlanetStatus;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.Dignity;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.Varga;
import app.astrosoft.core.PlanetStatusCalculator;
import app.astrosoft.ui.util.PlanetLabels;

/**
 * Label text for the Planetary Positions panels. Chart values mirror the real chart where
 * Mercury (29:53 Meena, Navamsa Meena) must read Db,V and Jupiter (Meena) must read Own.
 */
public class PlanetLabelsTest {

	private static final Rasi ARIES = Rasi.values()[0];
	private static final Rasi CAPRICORN = Rasi.values()[9];
	private static final Rasi PISCES = Rasi.values()[11];

	private static Map<Planet, PlanetStatus> chart() {
		EnumMap<Planet, Rasi> d1 = new EnumMap<Planet, Rasi>(Planet.class);
		EnumMap<Planet, Rasi> d9 = new EnumMap<Planet, Rasi>(Planet.class);
		for (Planet p : Planet.values()) {
			d1.put(p, ARIES);
			d9.put(p, ARIES);
		}
		d1.put(Planet.Mercury, PISCES);
		d9.put(Planet.Mercury, PISCES);
		d1.put(Planet.Jupiter, PISCES);
		d9.put(Planet.Jupiter, CAPRICORN);
		d1.put(Planet.Sun, PISCES);
		d9.put(Planet.Sun, ARIES);
		return PlanetStatusCalculator.statusMap(d1, d9);
	}

	@Test
	public void mercuryIsDebilitatedAndVargottama() {
		assertEquals("Mercury (Db,V)", PlanetLabels.label("Mercury", false, false, chart().get(Planet.Mercury)));
	}

	@Test
	public void jupiterInOwnSign() {
		assertEquals("Jupiter (Own)", PlanetLabels.label("Jupiter", false, false, chart().get(Planet.Jupiter)));
	}

	@Test
	public void combustComesBeforeDignity() {
		assertEquals("Jupiter (C,Own)", PlanetLabels.label("Jupiter", false, true, chart().get(Planet.Jupiter)));
	}

	@Test
	public void retroThenCombustThenStatus() {
		assertEquals("Mercury (R,C,Db,V)", PlanetLabels.label("Mercury", true, true, chart().get(Planet.Mercury)));
	}

	@Test
	public void planetWithoutStatusKeepsRetroAndCombustOnly() {
		assertEquals("Sun", PlanetLabels.label("Sun", false, false, chart().get(Planet.Sun)));
		assertEquals("Sun (R)", PlanetLabels.label("Sun", true, false, chart().get(Planet.Sun)));
		assertEquals("Sun (C)", PlanetLabels.label("Sun", false, true, chart().get(Planet.Sun)));
	}

	@Test
	public void ascendantWithNoStatusHasNoSuffix() {
		assertEquals("Ascendant", PlanetLabels.label("Ascendant", false, false, null));
	}

	private static PlanetaryInfo syntheticPlanetaryInfo() {
		EnumMap<Planet, Double> pos = new EnumMap<Planet, Double>(Planet.class);
		EnumMap<Planet, Boolean> dir = new EnumMap<Planet, Boolean>(Planet.class);
		for (Planet p : Planet.allPlanets()) {
			pos.put(p, 1.0); // 1 deg Aries (Mesha) -> Vargottama in D9 if Navamsa is intact
			dir.put(p, false);
		}
		double[] houses = new double[13];
		for (int i = 1; i <= 12; i++) {
			houses[i] = (i - 1) * 30.0 + 1.0;
		}
		HousePosition hp = new HousePosition(houses);
		hp.setSiderealTime(0.0);
		return new PlanetaryInfo(pos, dir, hp);
	}

	@Test
	public void missingNavamsaFallsBackToDignityOnlyAndLogsReason() {
		PlanetaryInfo info = syntheticPlanetaryInfo();
		info.getDivChart().put(Varga.Navamsa, null);

		final List<String> messages = new ArrayList<String>();
		Logger logger = Logger.getLogger(PlanetLabels.class.getName());
		Handler handler = new Handler() {
			@Override
			public void publish(LogRecord record) {
				if (record != null && record.getMessage() != null) {
					messages.add(record.getMessage());
				}
			}
			@Override
			public void flush() {}
			@Override
			public void close() {}
		};
		logger.addHandler(handler);
		try {
			Map<Planet, PlanetStatus> statuses = PlanetLabels.statuses(info);
			PlanetStatus sunStatus = statuses.get(Planet.Sun);
			assertEquals(Dignity.EXALTED, sunStatus.getDignity());
			assertFalse("Sun should not be Vargottama when Navamsa is missing", sunStatus.isVargottama());
			assertEquals("Sun (Ex)", PlanetLabels.label("Sun", false, false, sunStatus));
			assertTrue("Expected warning log when Navamsa map is missing, got: " + messages,
					!messages.isEmpty() && messages.get(0).contains("Navamsa"));
		} finally {
			logger.removeHandler(handler);
		}
	}

	@Test
	public void malformedNavamsaSignNumberFallsBackToDignityOnlyAndLogsReason() {
		PlanetaryInfo info = syntheticPlanetaryInfo();
		info.getDivChart(Varga.Navamsa).put(Planet.Sun, 0); // invalid sign number (must be 1..12)

		final List<String> messages = new ArrayList<String>();
		Logger logger = Logger.getLogger(PlanetLabels.class.getName());
		Handler handler = new Handler() {
			@Override
			public void publish(LogRecord record) {
				if (record != null && record.getMessage() != null) {
					messages.add(record.getMessage());
				}
			}
			@Override
			public void flush() {}
			@Override
			public void close() {}
		};
		logger.addHandler(handler);
		try {
			Map<Planet, PlanetStatus> statuses = PlanetLabels.statuses(info);
			PlanetStatus sunStatus = statuses.get(Planet.Sun);
			assertEquals(Dignity.EXALTED, sunStatus.getDignity());
			assertFalse("Sun should not be Vargottama when Navamsa sign number is invalid", sunStatus.isVargottama());
			assertEquals("Sun (Ex)", PlanetLabels.label("Sun", false, false, sunStatus));
			assertTrue("Expected warning log when Navamsa data is malformed, got: " + messages,
					!messages.isEmpty() && messages.get(0).contains("Navamsa"));
		} finally {
			logger.removeHandler(handler);
		}
	}
}
