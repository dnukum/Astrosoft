package app.astrosoft.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import app.astrosoft.consts.AshtavargaName;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Ashtavarga;

/**
 * Backend unit tests for Lagna (Ascendant) Bhinna Ashtakavarga (49 bindus),
 * 96-cell Prasthara Chakra matrices for all 7 planets + Ascendant,
 * B.V. Raman contributor ordering, and reductions (Trikona, Ekadhipatya, Gunakaras).
 *
 * Cross-verifies every cell of the 8 x 8 x 12 Prasthara tensor, 8 x 12 BAVs,
 * and 12 SAV values against PyJHora-generated golden test cases.
 */
public class PrastharaAndLagnaBavTest {

	private static final String GOLDEN_JSON_PATH = "src/app/astrosoft/core/test/data/pyjhora_prasthara_golden.json";

	private static final Planet[] PYJHORA_PLANET_ORDER = {
		Planet.Sun,
		Planet.Moon,
		Planet.Mars,
		Planet.Mercury,
		Planet.Jupiter,
		Planet.Venus,
		Planet.Saturn,
		Planet.Ascendant
	};

	private static final AshtavargaName[] PYJHORA_TARGET_ORDER = {
		AshtavargaName.Sun,
		AshtavargaName.Moon,
		AshtavargaName.Mars,
		AshtavargaName.Mercury,
		AshtavargaName.Jupiter,
		AshtavargaName.Venus,
		AshtavargaName.Saturn,
		AshtavargaName.Ascendant
	};

	private static final int[] EXPECTED_BAV_TOTALS = {
		48, // Sun
		49, // Moon
		39, // Mars
		54, // Mercury
		56, // Jupiter
		52, // Venus
		39, // Saturn
		49  // Ascendant (Lagna)
	};

	@Test
	public void testRamanPrastharaContributorOrder() {
		List<Planet> contributors = Ashtavarga.getPrastharaContributors();
		assertEquals("Must have 8 contributors", 8, contributors.size());
		List<Planet> expectedOrder = List.of(
			Planet.Saturn,
			Planet.Jupiter,
			Planet.Mars,
			Planet.Sun,
			Planet.Venus,
			Planet.Mercury,
			Planet.Moon,
			Planet.Ascendant
		);
		assertEquals("Contributors must follow B.V. Raman's descending orbital distance order",
			expectedOrder, contributors);
	}

	@Test
	public void testLagnaBavCountAndReductions() {
		EnumMap<Planet, Integer> pos = new EnumMap<Planet, Integer>(Planet.class);
		pos.put(Planet.Sun, 8);
		pos.put(Planet.Moon, 10);
		pos.put(Planet.Mars, 9);
		pos.put(Planet.Mercury, 8);
		pos.put(Planet.Jupiter, 6);
		pos.put(Planet.Venus, 7);
		pos.put(Planet.Saturn, 6);
		pos.put(Planet.Rahu, 4);
		pos.put(Planet.Ketu, 10);
		pos.put(Planet.Ascendant, 6);

		Ashtavarga av = new Ashtavarga(pos);

		assertEquals("Ascendant BAV count metadata must be 49",
			49, Ashtavarga.getCount(AshtavargaName.Ascendant));

		EnumMap<Rasi, Integer> ascBav = av.getAshtavarga(AshtavargaName.Ascendant);
		assertNotNull("Ascendant BAV map must not be null", ascBav);

		int sum = 0;
		for (Rasi r : Rasi.values()) {
			int val = ascBav.get(r);
			assertTrue("Bindu in rasi must be in 0..8", val >= 0 && val <= 8);
			sum += val;
		}
		assertEquals("Total bindus in Ascendant BAV must equal 49", 49, sum);

		// Verify SAV remains strictly 337 bindus (sum of 7 physical planets only)
		EnumMap<Rasi, Integer> sav = av.getAshtavarga(AshtavargaName.SarvaAshtavarga);
		int savSum = 0;
		for (Rasi r : Rasi.values()) {
			savSum += sav.get(r);
		}
		assertEquals("SAV total bindus must remain 337 (excluding Lagna BAV)", 337, savSum);

		// Verify Trikona, Ekadhipatya, and Gunakaras are computed for Ascendant
		EnumMap<Rasi, Integer> ascTrikona = av.getTrikona(AshtavargaName.Ascendant);
		EnumMap<Rasi, Integer> ascEka = av.getEkathipathiya(AshtavargaName.Ascendant);
		assertNotNull("Ascendant Trikona reduction must not be null", ascTrikona);
		assertNotNull("Ascendant Ekadhipatya reduction must not be null", ascEka);
		for (Rasi r : Rasi.values()) {
			assertTrue("Trikona reduced value must be <= BAV value", ascTrikona.get(r) <= ascBav.get(r));
			assertTrue("Ekadhipatya reduced value must be <= Trikona value", ascEka.get(r) <= ascTrikona.get(r));
		}
		assertTrue("Ascendant Rasi Guna must be >= 0", av.getRasiGuna(AshtavargaName.Ascendant) >= 0);
		assertTrue("Ascendant Graha Guna must be >= 0", av.getGrahaGuna(AshtavargaName.Ascendant) >= 0);
		assertNotNull("Ascendant Guna Table must not be null", av.getGunaTable(AshtavargaName.Ascendant));
	}

	@Test
	public void testPyJHoraGoldenCasesEquivalence() throws Exception {
		File jsonFile = new File(GOLDEN_JSON_PATH);
		assertTrue("PyJHora golden JSON must exist at " + jsonFile.getAbsolutePath(), jsonFile.exists());

		String raw = new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8);
		String[] blocks = raw.split("\\{\\s*\"id\"\\s*:");
		assertTrue("Must contain at least 8 golden test cases", blocks.length > 8);

		for (int bIdx = 1; bIdx < blocks.length; bIdx++) {
			String block = "{\"id\":" + blocks[bIdx];
			String caseId = extractString(block, "id");

			EnumMap<Planet, Integer> pos = extractPositions(block);
			Ashtavarga av = new Ashtavarga(pos);

			int[][] expectedBav = extract2DIntArray(block, "bav", "sav", 8, 12);
			int[] expectedSav = extract1DIntArray(block, "sav", "pav", 12);
			int[][][] expectedPav = extract3DIntArray(block, "pav", "trikona_7", 8, 8, 12);
			int[][] expectedTrikona7 = extract2DIntArray(block, "trikona_7", "eka_7", 7, 12);
			int[][] expectedEka7 = extract2DIntArray(block, "eka_7", "rasi_pindas", 7, 12);
			int[] expectedRasiPindas = extract1DIntArray(block, "rasi_pindas", "graha_pindas", 7);
			int[] expectedGrahaPindas = extract1DIntArray(block, "graha_pindas", "sodhya_pindas", 7);
			int[] expectedSodhyaPindas = extract1DIntArray(block, "sodhya_pindas", null, 7);

			// 1. Verify SAV (12 houses, 337 total)
			EnumMap<Rasi, Integer> actualSav = av.getAshtavarga(AshtavargaName.SarvaAshtavarga);
			for (int r = 0; r < 12; r++) {
				Rasi rasi = Rasi.ofIndex(r);
				assertEquals("Case [" + caseId + "] SAV at " + rasi,
					expectedSav[r], (int) actualSav.get(rasi));
			}

			// 2. Verify all 8 BAVs (Sun..Saturn + Ascendant) and all 8 x 96-cell Prasthara grids
			for (int t = 0; t < 8; t++) {
				AshtavargaName target = PYJHORA_TARGET_ORDER[t];
				EnumMap<Rasi, Integer> actualBav = av.getAshtavarga(target);
				assertNotNull("Case [" + caseId + "] BAV for " + target, actualBav);

				int targetSum = 0;
				for (int r = 0; r < 12; r++) {
					Rasi rasi = Rasi.ofIndex(r);
					int bavVal = actualBav.get(rasi);
					targetSum += bavVal;
					assertEquals("Case [" + caseId + "] BAV for " + target + " at " + rasi,
						expectedBav[t][r], bavVal);

					// Verify Prasthara column sum across all 8 contributors equals BAV at this rasi
					int colSum = 0;
					for (int c = 0; c < 8; c++) {
						Planet contributor = PYJHORA_PLANET_ORDER[c];
						int actualBit = av.getPrastharaBindu(target, contributor, rasi);
						int expectedBit = expectedPav[t][c][r];
						assertEquals("Case [" + caseId + "] Prasthara[" + target + "][" + contributor + "][" + rasi + "]",
							expectedBit, actualBit);
						colSum += actualBit;
					}
					assertEquals("Case [" + caseId + "] Prasthara column sum must equal BAV for " + target + " at " + rasi,
						bavVal, colSum);
				}
				assertEquals("Case [" + caseId + "] total BAV bindus for " + target,
					EXPECTED_BAV_TOTALS[t], targetSum);

				// 3. Verify Trikona, Ekadhipatya, and Pindas against PyJHora for the 7 planets
				if (t < 7) {
					EnumMap<Rasi, Integer> actualTrikona = av.getTrikona(target);
					EnumMap<Rasi, Integer> actualEka = av.getEkathipathiya(target);
					for (int r = 0; r < 12; r++) {
						Rasi rasi = Rasi.ofIndex(r);
						assertEquals("Case [" + caseId + "] Trikona for " + target + " at " + rasi,
							expectedTrikona7[t][r], (int) actualTrikona.get(rasi));
						assertEquals("Case [" + caseId + "] Ekadhipatya for " + target + " at " + rasi,
							expectedEka7[t][r], (int) actualEka.get(rasi));
					}
					assertEquals("Case [" + caseId + "] Rasi Guna for " + target,
						expectedRasiPindas[t], av.getRasiGuna(target));
					assertEquals("Case [" + caseId + "] Graha Guna for " + target,
						expectedGrahaPindas[t], av.getGrahaGuna(target));
					assertEquals("Case [" + caseId + "] Sodhya Pinda for " + target,
						expectedSodhyaPindas[t], av.getRasiGuna(target) + av.getGrahaGuna(target));
				}
			}
		}
	}

	private static String extractString(String src, String key) {
		Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"").matcher(src);
		assertTrue("Key " + key + " must exist", m.find());
		return m.group(1);
	}

	private static EnumMap<Planet, Integer> extractPositions(String src) {
		int idx = src.indexOf("\"positions\"");
		int endIdx = src.indexOf("}", idx);
		String sub = src.substring(idx, endIdx + 1);
		EnumMap<Planet, Integer> pos = new EnumMap<Planet, Integer>(Planet.class);
		for (Planet p : new Planet[] {
			Planet.Sun, Planet.Moon, Planet.Mars, Planet.Mercury, Planet.Jupiter,
			Planet.Venus, Planet.Saturn, Planet.Rahu, Planet.Ketu, Planet.Ascendant
		}) {
			Matcher m = Pattern.compile("\"" + p.name() + "\"\\s*:\\s*(\\d+)").matcher(sub);
			assertTrue("Position for " + p + " must exist", m.find());
			pos.put(p, Integer.parseInt(m.group(1)));
		}
		return pos;
	}

	private static List<Integer> parseIntsFromSection(String src, String key, String nextKey) {
		int start = src.indexOf("\"" + key + "\"");
		int end = nextKey != null ? src.indexOf("\"" + nextKey + "\"", start) : src.length();
		String sub = src.substring(start, end);
		List<Integer> nums = new ArrayList<Integer>();
		Matcher m = Pattern.compile("\\b(\\d+)\\b").matcher(sub);
		while (m.find()) {
			nums.add(Integer.parseInt(m.group(1)));
		}
		return nums;
	}

	private static int[] extract1DIntArray(String src, String key, String nextKey, int len) {
		List<Integer> nums = parseIntsFromSection(src, key, nextKey);
		assertEquals("1D array length for " + key, len, nums.size());
		int[] arr = new int[len];
		for (int i = 0; i < len; i++) {
			arr[i] = nums.get(i);
		}
		return arr;
	}

	private static int[][] extract2DIntArray(String src, String key, String nextKey, int d1, int d2) {
		List<Integer> nums = parseIntsFromSection(src, key, nextKey);
		assertEquals("2D array count for " + key, d1 * d2, nums.size());
		int[][] arr = new int[d1][d2];
		int k = 0;
		for (int i = 0; i < d1; i++) {
			for (int j = 0; j < d2; j++) {
				arr[i][j] = nums.get(k++);
			}
		}
		return arr;
	}

	private static int[][][] extract3DIntArray(String src, String key, String nextKey, int d1, int d2, int d3) {
		List<Integer> nums = parseIntsFromSection(src, key, nextKey);
		assertEquals("3D array count for " + key, d1 * d2 * d3, nums.size());
		int[][][] arr = new int[d1][d2][d3];
		int k = 0;
		for (int i = 0; i < d1; i++) {
			for (int j = 0; j < d2; j++) {
				for (int r = 0; r < d3; r++) {
					arr[i][j][r] = nums.get(k++);
				}
			}
		}
		return arr;
	}
}
