package app.astrosoft.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.junit.Test;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.Ayanamsa;
import app.astrosoft.consts.Sex;
import app.astrosoft.core.Horoscope;
import app.astrosoft.ui.view.PlanetView;
import app.astrosoft.ui.view.RelativePlanetView;

/**
 * Step 5 Data-Driven JUnit 4 verification of planetary status suffixes in both
 * RelativePlanetView (compact panel on Profile page) and PlanetView (full Planetary Positions table)
 * against PyJHora-generated synthetic cases in planet_status_cases.json.
 *
 * Run with a display (NOT headless).
 */
public class PlanetaryPositionsGeneratedCasesTest {

	private static final String JSON_PATH = "src/app/astrosoft/ui/test/resources/planet_status_cases.json";

	private static final String[] PLANET_KEYS = {
		"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu", "Ascendant"
	};

	private static class GeneratedCase {
		String id;
		String targetState;
		String focusPlanet;
		String name;
		int year, month, day;
		int hour, minute, second;
		double timezone;
		String placeName;
		double latitude;
		double longitude;
		String ayanamsa;
		Map<String, String> expectedSuffix = new LinkedHashMap<String, String>();
		Map<String, String> expectedAstroCompact = new LinkedHashMap<String, String>();
		Map<String, String> expectedAstroTable = new LinkedHashMap<String, String>();
	}

	private static List<GeneratedCase> loadCases() throws Exception {
		File f = new File(JSON_PATH);
		if (!f.exists()) {
			fail("Missing generated test cases file: " + f.getAbsolutePath()
					+ " — run tools/gen_status_cases.py first.");
		}
		String raw = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
		List<GeneratedCase> list = new ArrayList<GeneratedCase>();

		// Split on case blocks inside "cases": [ ... ]
		int casesIdx = raw.indexOf("\"cases\"");
		assertTrue("JSON must contain 'cases' array", casesIdx >= 0);
		String casesPart = raw.substring(casesIdx);
		String[] blocks = casesPart.split("\\{\\s*\"id\"\\s*:");
		for (int i = 1; i < blocks.length; i++) {
			String b = "{\"id\":" + blocks[i];
			GeneratedCase c = new GeneratedCase();
			c.id = extractString(b, "id");
			c.targetState = extractString(b, "target_state");
			c.focusPlanet = extractString(b, "focus_planet");
			c.name = extractString(b, "name");
			c.year = extractIntInBlock(b, "date", "year");
			c.month = extractIntInBlock(b, "date", "month");
			c.day = extractIntInBlock(b, "date", "day");
			c.hour = extractIntInBlock(b, "time", "hour");
			c.minute = extractIntInBlock(b, "time", "minute");
			c.second = extractIntInBlock(b, "time", "second");
			c.timezone = extractDouble(b, "timezone");
			c.placeName = extractString(b, "place_name");
			c.latitude = extractDouble(b, "latitude");
			c.longitude = extractDouble(b, "longitude");
			c.ayanamsa = extractString(b, "ayanamsa");

			int planetsIdx = b.indexOf("\"planets\"");
			String planetsBlock = b.substring(planetsIdx);
			for (String pKey : PLANET_KEYS) {
				String pSub = extractObjectBlock(planetsBlock, pKey);
				c.expectedSuffix.put(pKey, extractString(pSub, "expected_suffix"));
				c.expectedAstroCompact.put(pKey, extractString(pSub, "expected_suffix_astrosoft_compact"));
				c.expectedAstroTable.put(pKey, extractString(pSub, "expected_suffix_astrosoft_table"));
			}
			list.add(c);
		}
		assertTrue("Expected at least 50 generated cases, got " + list.size(), list.size() >= 50);
		return list;
	}

	private static String extractString(String src, String key) {
		Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"").matcher(src);
		assertTrue("Missing string key '" + key + "'", m.find());
		return m.group(1);
	}

	private static double extractDouble(String src, String key) {
		Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?[0-9]+(?:\\.[0-9]+)?)").matcher(src);
		assertTrue("Missing numeric key '" + key + "'", m.find());
		return Double.parseDouble(m.group(1));
	}

	private static int extractIntInBlock(String src, String blockName, String key) {
		String sub = extractObjectBlock(src, blockName);
		Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?[0-9]+)").matcher(sub);
		assertTrue("Missing int key '" + key + "' in '" + blockName + "'", m.find());
		return Integer.parseInt(m.group(1));
	}

	private static String extractObjectBlock(String src, String key) {
		int idx = src.indexOf("\"" + key + "\"");
		assertTrue("Missing object key '" + key + "'", idx >= 0);
		int open = src.indexOf('{', idx);
		int depth = 0;
		for (int i = open; i < src.length(); i++) {
			char ch = src.charAt(i);
			if (ch == '{') depth++;
			else if (ch == '}') {
				depth--;
				if (depth == 0) {
					return src.substring(open, i + 1);
				}
			}
		}
		throw new IllegalArgumentException("Unclosed object for " + key);
	}

	private static void collectTables(Container c, List<JTable> out) {
		for (Component child : c.getComponents()) {
			if (child instanceof JTable) {
				out.add((JTable) child);
			}
			if (child instanceof Container) {
				collectTables((Container) child, out);
			}
		}
	}

	private static List<String> displayedTexts(Container view) {
		List<JTable> tables = new ArrayList<JTable>();
		collectTables(view, tables);
		assertTrue("view must contain a JTable", !tables.isEmpty());
		List<String> texts = new ArrayList<String>();
		for (JTable t : tables) {
			for (int r = 0; r < t.getRowCount(); r++) {
				for (int c = 0; c < t.getColumnCount(); c++) {
					Component comp = t.getCellRenderer(r, c).getTableCellRendererComponent(
							t, t.getValueAt(r, c), false, false, r, c);
					if (comp instanceof JLabel) {
						texts.add(((JLabel) comp).getText());
					}
				}
			}
		}
		return texts;
	}

	private static String planetLabelFromTexts(List<String> texts, String planetName) {
		for (String s : texts) {
			if (s != null && (s.equals(planetName) || s.startsWith(planetName + " ("))) {
				return s;
			}
		}
		if ("Ascendant".equals(planetName)) {
			for (String s : texts) {
				if (s != null && (s.equals("As") || s.startsWith("As ("))) {
					return s.equals("As") ? "Ascendant" : s;
				}
			}
		}
		return null;
	}

	private static PlanetaryInfo buildPlanetaryInfo(GeneratedCase c) {
		Place place = new Place(c.placeName, c.latitude, c.longitude, c.timezone);
		BirthData bd = new BirthData(c.name, Sex.Male, c.day, c.month, c.year, c.hour, c.minute, c.second, place);
		Horoscope h = new Horoscope(bd);
		h.setAyanamsa(Ayanamsa.valueOf(c.ayanamsa));
		return h.getPlanetaryInfo();
	}

	@Test
	public void verifyAllGeneratedCasesAgainstSpecAndReportMismatches() throws Exception {
		List<GeneratedCase> cases = loadCases();
		List<String> compactVsRamanMismatches = new ArrayList<String>();
		List<String> tableVsRamanMismatches = new ArrayList<String>();
		List<String> compactVsCurrentModelMismatches = new ArrayList<String>();
		List<String> tableVsCurrentModelMismatches = new ArrayList<String>();

		for (GeneratedCase c : cases) {
			PlanetaryInfo info = buildPlanetaryInfo(c);
			List<String> compactTexts = displayedTexts(new RelativePlanetView(info));
			List<String> tableTexts = displayedTexts(new PlanetView("Planetary Positions", info));

			for (String p : PLANET_KEYS) {
				String actualCompact = planetLabelFromTexts(compactTexts, p);
				String actualTable = planetLabelFromTexts(tableTexts, p);
				String expRaman = c.expectedSuffix.get(p);
				String expCompactModel = c.expectedAstroCompact.get(p);
				String expTableModel = c.expectedAstroTable.get(p);

				if (!expRaman.equals(actualCompact)) {
					compactVsRamanMismatches.add(String.format(
							"%s (%s) [%s]: RelativePlanetView actual='%s' vs expectedRaman='%s'",
							c.id, c.targetState, p, actualCompact, expRaman));
				}
				if (!expRaman.equals(actualTable)) {
					tableVsRamanMismatches.add(String.format(
							"%s (%s) [%s]: PlanetView actual='%s' vs expectedRaman='%s'",
							c.id, c.targetState, p, actualTable, expRaman));
				}
				if (!expCompactModel.equals(actualCompact)) {
					compactVsCurrentModelMismatches.add(String.format(
							"%s (%s) [%s]: RelativePlanetView actual='%s' vs currentCodeModel='%s'",
							c.id, c.targetState, p, actualCompact, expCompactModel));
				}
				if (!expTableModel.equals(actualTable)) {
					tableVsCurrentModelMismatches.add(String.format(
							"%s (%s) [%s]: PlanetView actual='%s' vs currentCodeModel='%s'",
							c.id, c.targetState, p, actualTable, expTableModel));
				}
			}
		}

		System.out.println("=== DATA-DRIVEN VERIFICATION SUMMARY (" + cases.size() + " charts, "
				+ (cases.size() * PLANET_KEYS.length) + " planet labels per panel) ===");
		System.out.println("Generator vs current Astrosoft code model mismatches (RelativePlanetView): "
				+ compactVsCurrentModelMismatches.size());
		for (String m : compactVsCurrentModelMismatches) {
			System.out.println("  [GEN_DIFF_COMPACT] " + m);
		}
		System.out.println("Generator vs current Astrosoft code model mismatches (PlanetView): "
				+ tableVsCurrentModelMismatches.size());
		for (String m : tableVsCurrentModelMismatches) {
			System.out.println("  [GEN_DIFF_TABLE] " + m);
		}
		System.out.println("RelativePlanetView mismatches vs B.V. Raman spec: " + compactVsRamanMismatches.size());
		for (String m : compactVsRamanMismatches) {
			System.out.println("  [COMPACT_VS_RAMAN] " + m);
		}
		System.out.println("PlanetView mismatches vs B.V. Raman spec: " + tableVsRamanMismatches.size());
		for (String m : tableVsRamanMismatches) {
			System.out.println("  [TABLE_VS_RAMAN] " + m);
		}

		// First assert 0 generator errors (our Python model of current Astrosoft matches Java 100%)
		assertEquals("Generator model must match current RelativePlanetView 100%",
				0, compactVsCurrentModelMismatches.size());
		assertEquals("Generator model must match current PlanetView 100%",
				0, tableVsCurrentModelMismatches.size());

		// Now assert both views match the B.V. Raman spec (will fail until production bugs are fixed!)
		assertTrue("RelativePlanetView has " + compactVsRamanMismatches.size()
				+ " mismatches against B.V. Raman spec:\n" + String.join("\n", compactVsRamanMismatches),
				compactVsRamanMismatches.isEmpty());
		assertTrue("PlanetView has " + tableVsRamanMismatches.size()
				+ " mismatches against B.V. Raman spec:\n" + String.join("\n", tableVsRamanMismatches),
				tableVsRamanMismatches.isEmpty());
	}
}
