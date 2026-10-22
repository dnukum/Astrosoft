/**
 * NadiKutaTest.java
 *
 * Exhaustive Unit & Integration Test Suite for Nadi Kuta Evaluation
 * according to Dr. B.V. Raman's Electional Astrology rules and Astrosoft 2.0
 * Functional Specification.
 *
 * Covers:
 * 1. Verification of all 108 Nakshatra Padas against the published table.
 * 2. Verification of all 27 Macro Nakshatras against the serpentine Tridosha table.
 * 3. Exhaustive test of all 11,664 combinations (108 Boy Padas x 108 Girl Padas).
 * 4. Exhaustive test of all 729 Quick Star combinations (27 x 27 without Padas - Option A).
 * 5. Strict rejection of Madhya Nadi Dosha (Both Pitta = 0 points, Padas never mitigate).
 * 6. Partial relief (4 points) for Adi (Vata) and Antya (Sleshma) when Padas differ.
 *
 * @author Astrosoft Team
 */

package app.astrosoft.core.test;

import junit.framework.TestCase;
import app.astrosoft.consts.Kuta;
import app.astrosoft.consts.Nadi;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import app.astrosoft.core.Compactibility;

public class NadiKutaTest extends TestCase {

	/**
	 * Test 1: Verify all 108 Nakshatra Padas against the exact 36 rows
	 * published in the Functional Specification and Dr. B.V. Raman's table.
	 */
	public void testAll108PadasAgainstPublishedSpecTable() {
		// Published table entries: [36 rows][3 columns]
		// Columns: 0 = Vata (Adi), 1 = Pitta (Madhya), 2 = Sleshma (Antya)
		// Format: {nakIndex, pada} for each column in row
		int[][][] specTable = {
			// Row 0 (Forward): Aswini~1, Aswini~2, Aswini~3
			{ {0, 1}, {0, 2}, {0, 3} },
			// Row 1 (Backward): Bharani~2, Bharani~1, Aswini~4
			{ {1, 2}, {1, 1}, {0, 4} },
			// Row 2 (Forward): Bharani~3, Bharani~4, Krittika~1
			{ {1, 3}, {1, 4}, {2, 1} },
			// Row 3 (Backward): Krittika~4, Krittika~3, Krittika~2
			{ {2, 4}, {2, 3}, {2, 2} },
			// Row 4 (Forward): Rohini~1, Rohini~2, Rohini~3
			{ {3, 1}, {3, 2}, {3, 3} },
			// Row 5 (Backward): Mrigasira~2, Mrigasira~1, Rohini~4
			{ {4, 2}, {4, 1}, {3, 4} },
			// Row 6 (Forward): Mrigasira~3, Mrigasira~4, Aridra~1
			{ {4, 3}, {4, 4}, {5, 1} },
			// Row 7 (Backward): Aridra~4, Aridra~3, Aridra~2
			{ {5, 4}, {5, 3}, {5, 2} },
			// Row 8 (Forward): Punarvasu~1, Punarvasu~2, Punarvasu~3
			{ {6, 1}, {6, 2}, {6, 3} },
			// Row 9 (Backward): Pushyami~2, Pushyami~1, Punarvasu~4
			{ {7, 2}, {7, 1}, {6, 4} },
			// Row 10 (Forward): Pushyami~3, Pushyami~4, Aslesha~1
			{ {7, 3}, {7, 4}, {8, 1} },
			// Row 11 (Backward): Aslesha~4, Aslesha~3, Aslesha~2
			{ {8, 4}, {8, 3}, {8, 2} },
			// Row 12 (Forward): Makha~1, Makha~2, Makha~3
			{ {9, 1}, {9, 2}, {9, 3} },
			// Row 13 (Backward): Pubba~2, Pubba~1, Makha~4
			{ {10, 2}, {10, 1}, {9, 4} },
			// Row 14 (Forward): Pubba~3, Pubba~4, Uttara~1
			{ {10, 3}, {10, 4}, {11, 1} },
			// Row 15 (Backward): Uttara~4, Uttara~3, Uttara~2
			{ {11, 4}, {11, 3}, {11, 2} },
			// Row 16 (Forward): Hasta~1, Hasta~2, Hasta~3
			{ {12, 1}, {12, 2}, {12, 3} },
			// Row 17 (Backward): Chitta~2, Chitta~1, Hasta~4
			{ {13, 2}, {13, 1}, {12, 4} },
			// Row 18 (Forward): Chitta~3, Chitta~4, Swati~1
			{ {13, 3}, {13, 4}, {14, 1} },
			// Row 19 (Backward): Swati~4, Swati~3, Swati~2
			{ {14, 4}, {14, 3}, {14, 2} },
			// Row 20 (Forward): Visakha~1, Visakha~2, Visakha~3
			{ {15, 1}, {15, 2}, {15, 3} },
			// Row 21 (Backward): Anuradha~2, Anuradha~1, Visakha~4
			{ {16, 2}, {16, 1}, {15, 4} },
			// Row 22 (Forward): Anuradha~3, Anuradha~4, Jyeshta~1
			{ {16, 3}, {16, 4}, {17, 1} },
			// Row 23 (Backward): Jyeshta~4, Jyeshta~3, Jyeshta~2
			{ {17, 4}, {17, 3}, {17, 2} },
			// Row 24 (Forward): Moola~1, Moola~2, Moola~3
			{ {18, 1}, {18, 2}, {18, 3} },
			// Row 25 (Backward): Poorvashadha~2, Poorvashadha~1, Moola~4
			{ {19, 2}, {19, 1}, {18, 4} },
			// Row 26 (Forward): Poorvashadha~3, Poorvashadha~4, Uttarashadha~1
			{ {19, 3}, {19, 4}, {20, 1} },
			// Row 27 (Backward): Uttarashadha~4, Uttarashadha~3, Uttarashadha~2
			{ {20, 4}, {20, 3}, {20, 2} },
			// Row 28 (Forward): Sravana~1, Sravana~2, Sravana~3
			{ {21, 1}, {21, 2}, {21, 3} },
			// Row 29 (Backward): Dhanishta~2, Dhanishta~1, Sravana~4
			{ {22, 2}, {22, 1}, {21, 4} },
			// Row 30 (Forward): Dhanishta~3, Dhanishta~4, Satabhisha~1
			{ {22, 3}, {22, 4}, {23, 1} },
			// Row 31 (Backward): Satabhisha~4, Satabhisha~3, Satabhisha~2
			{ {23, 4}, {23, 3}, {23, 2} },
			// Row 32 (Forward): Poorvabhadra~1, Poorvabhadra~2, Poorvabhadra~3
			{ {24, 1}, {24, 2}, {24, 3} },
			// Row 33 (Backward): Uttarabhadra~2, Uttarabhadra~1, Poorvabhadra~4
			{ {25, 2}, {25, 1}, {24, 4} },
			// Row 34 (Forward): Uttarabhadra~3, Uttarabhadra~4, Revati~1
			{ {25, 3}, {25, 4}, {26, 1} },
			// Row 35 (Backward): Revati~4, Revati~3, Revati~2
			{ {26, 4}, {26, 3}, {26, 2} }
		};

		assertEquals("Spec table must contain exactly 36 rows", 36, specTable.length);

		int verifiedCount = 0;
		for (int row = 0; row < 36; row++) {
			// Column 0 must be Vata
			int vataNak = specTable[row][0][0];
			int vataPada = specTable[row][0][1];
			assertEquals("Row " + row + " Col 0 must be Vata",
					Nadi.Vata, Nadi.ofPada(vataNak, vataPada));

			// Column 1 must be Pitta
			int pittaNak = specTable[row][1][0];
			int pittaPada = specTable[row][1][1];
			assertEquals("Row " + row + " Col 1 must be Pitta",
					Nadi.Pitta, Nadi.ofPada(pittaNak, pittaPada));

			// Column 2 must be Sleshma
			int sleshmaNak = specTable[row][2][0];
			int sleshmaPada = specTable[row][2][1];
			assertEquals("Row " + row + " Col 2 must be Sleshma",
					Nadi.Sleshma, Nadi.ofPada(sleshmaNak, sleshmaPada));

			verifiedCount += 3;
		}

		assertEquals("Must verify all 108 padas", 108, verifiedCount);
	}

	/**
	 * Test 2: Verify Macro-Level Serpentine Classification for all 27 stars.
	 */
	public void testMacroSerpentineNadiTable() {
		// Vata (Adi) stars per spec:
		// Aswini (0), Aridra (5), Punarvasu (6), Uttara (11), Hasta (12),
		// Jyeshta (17), Moola (18), Satabhisha (23), Poorvabhadra (24)
		int[] vataStars = {0, 5, 6, 11, 12, 17, 18, 23, 24};
		for (int nak : vataStars) {
			assertEquals("Nak " + nak + " must be Vata macro", Nadi.Vata, Nadi.ofNakshathra(nak));
		}

		// Pitta (Madhya) stars per spec:
		// Bharani (1), Mrigasira (4), Pushyami (7), Pubba (10), Chitta (13),
		// Anuradha (16), Poorvashadha (19), Dhanishta (22), Uttarabhadra (25)
		int[] pittaStars = {1, 4, 7, 10, 13, 16, 19, 22, 25};
		for (int nak : pittaStars) {
			assertEquals("Nak " + nak + " must be Pitta macro", Nadi.Pitta, Nadi.ofNakshathra(nak));
		}

		// Sleshma (Antya) stars per spec:
		// Krittika (2), Rohini (3), Aslesha (8), Makha (9), Swati (14),
		// Visakha (15), Uttarashadha (20), Sravana (21), Revati (26)
		int[] sleshmaStars = {2, 3, 8, 9, 14, 15, 20, 21, 26};
		for (int nak : sleshmaStars) {
			assertEquals("Nak " + nak + " must be Sleshma macro", Nadi.Sleshma, Nadi.ofNakshathra(nak));
		}
	}

	/**
	 * Test 3: Exhaustive test of ALL 11,664 Boy x Girl (Nakshatra, Pada) combinations!
	 * Verifies that the evaluated Nadi points strictly follow the business rules:
	 * - Different macro Nadis -> exactly 8 points
	 * - Both Madhya (Pitta)   -> strictly 0 points (even if padas differ)
	 * - Both Adi / Antya with different Pada Nadis -> exactly 4 points
	 * - Both Adi / Antya with same Pada Nadi      -> strictly 0 points
	 */
	public void testExhaustiveHoroscopeCombinations() {
		int totalTested = 0;
		int count8 = 0;
		int count4 = 0;
		int count0 = 0;

		for (int bNak = 0; bNak < 27; bNak++) {
			for (int bPada = 1; bPada <= 4; bPada++) {
				for (int gNak = 0; gNak < 27; gNak++) {
					for (int gPada = 1; gPada <= 4; gPada++) {

						Nadi bMacro = Nadi.ofNakshathra(bNak);
						Nadi gMacro = Nadi.ofNakshathra(gNak);
						Nadi bPadaNadi = Nadi.ofPada(bNak, bPada);
						Nadi gPadaNadi = Nadi.ofPada(gNak, gPada);

						// Determine expected score from rules
						int expectedScore;
						if (bMacro != gMacro) {
							expectedScore = 8;
						} else if (bMacro == Nadi.Pitta) {
							// Madhya Nadi Dosha strictly rejected
							expectedScore = 0;
						} else {
							// Adi (Vata) or Antya (Sleshma)
							if (bPadaNadi != gPadaNadi) {
								expectedScore = 4; // Mitigated
							} else {
								expectedScore = 0; // Unmitigated
							}
						}

						// Calculate using Compactibility
						Compactibility comp = new Compactibility("Boy", "Girl",
								Nakshathra.ofIndex(bNak), bPada,
								Nakshathra.ofIndex(gNak), gPada,
								Rasi.Mesha, Rasi.Mesha);

						int actualScore = comp.getKuta(Kuta.Nadi);
						assertEquals("Mismatch for Boy(" + bNak + "~" + bPada + ") Girl(" + gNak + "~" + gPada + ")",
								expectedScore, actualScore);

						if (actualScore == 8) count8++;
						else if (actualScore == 4) count4++;
						else count0++;

						totalTested++;
					}
				}
			}
		}

		assertEquals("Must test all 11,664 combinations", 11664, totalTested);
		assertTrue("Must have combinations with 8 points", count8 > 0);
		assertTrue("Must have combinations with 4 points (mitigated)", count4 > 0);
		assertTrue("Must have combinations with 0 points", count0 > 0);
		System.out.println("Exhaustive test completed: 11,664 combinations tested (8pts=" + count8 + ", 4pts=" + count4 + ", 0pts=" + count0 + ")");
	}

	/**
	 * Test 4: Exhaustive test of all 729 Quick Star combinations (Option A: without Padas).
	 * If Pada is not known (pada = 0):
	 * - Different macro Nadis -> exactly 8 points
	 * - Same macro Nadi       -> strictly 0 points
	 */
	public void testExhaustiveStarQuickEntryCombinations() {
		int totalTested = 0;
		for (int bNak = 0; bNak < 27; bNak++) {
			for (int gNak = 0; gNak < 27; gNak++) {
				Nadi bMacro = Nadi.ofNakshathra(bNak);
				Nadi gMacro = Nadi.ofNakshathra(gNak);

				int expectedScore = (bMacro != gMacro) ? 8 : 0;

				Compactibility comp = new Compactibility("Boy", "Girl",
						Nakshathra.ofIndex(bNak), Nakshathra.ofIndex(gNak),
						Rasi.Mesha, Rasi.Mesha);

				int actualScore = comp.getKuta(Kuta.Nadi);
				assertEquals("Quick Star mismatch for Boy(" + bNak + ") Girl(" + gNak + ")",
						expectedScore, actualScore);

				totalTested++;
			}
		}
		assertEquals("Must test all 729 star combinations", 729, totalTested);
	}

	/**
	 * Test 5: Verify strict rejection of Madhya Nadi Dosha even when Padas map
	 * to different Pada Nadis.
	 */
	public void testMadhyaNadiStrictRejectionEdgeCases() {
		// Bharani (1) is Madhya (Pitta). Bharani~1 is Pitta.
		// Mrigasira (4) is Madhya (Pitta). Mrigasira~2 is Vata!
		// Even though Bharani~1 (Pitta) and Mrigasira~2 (Vata) have DIFFERENT Pada Nadis,
		// because both stars are Madhya Nadi, score MUST be 0.
		Compactibility comp = new Compactibility("Boy", "Girl",
				Nakshathra.Bharani, 1,
				Nakshathra.Mrigasira, 2,
				Rasi.Mesha, Rasi.Vrishabha);

		assertEquals("Madhya Nadi Dosha must be strictly rejected (0 points)", 0, comp.getKuta(Kuta.Nadi));
	}

	/**
	 * Test 6: Verify Adi Nadi mitigation (4 points).
	 */
	public void testAdiNadiMitigation() {
		// Both Aswini (0) and Punarvasu (6) are Vata (Adi).
		// Aswini~1 is Vata. Punarvasu~2 is Pitta.
		// Different Pada Nadis -> 4 points!
		Compactibility comp = new Compactibility("Boy", "Girl",
				Nakshathra.Ashwini, 1,
				Nakshathra.Punarpoosam, 2,
				Rasi.Mesha, Rasi.Mithuna);

		assertEquals("Adi Nadi with different padas must award 4 points", 4, comp.getKuta(Kuta.Nadi));
	}

	/**
	 * Test 7: Verify Antya Nadi mitigation (4 points).
	 */
	public void testAntyaNadiMitigation() {
		// Both Krittika (2) and Rohini (3) are Sleshma (Antya).
		// Krittika~1 is Sleshma. Rohini~2 is Pitta.
		// Different Pada Nadis -> 4 points!
		Compactibility comp = new Compactibility("Boy", "Girl",
				Nakshathra.Krithika, 1,
				Nakshathra.Rohini, 2,
				Rasi.Mesha, Rasi.Vrishabha);

		assertEquals("Antya Nadi with different padas must award 4 points", 4, comp.getKuta(Kuta.Nadi));
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(NadiKutaTest.class);
	}
}
