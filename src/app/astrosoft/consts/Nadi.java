/**
 * Nadi.java
 *
 * Represents the physiological constitution, pulse, and hereditary nervous energy
 * in Hindu astrology, corresponding directly to the three Ayurvedic humours (Tridoshas):
 * 1. Vata (Wind / Adi - 1st Nadi)
 * 2. Pitta (Bile / Madhya - 2nd Nadi)
 * 3. Sleshma (Phlegm / Antya - 3rd Nadi)
 *
 * In Nadi Kuta evaluation:
 * - Macro level: The 27 Nakshatras are divided evenly into the three Nadis in a
 *   serpentine counting pattern of threes:
 *     Row 0 (Forward ➞):  Aswini (Vata), Bharani (Pitta), Krittika (Sleshma)
 *     Row 1 (⇦ Backward): Rohini (Sleshma), Mrigasira (Pitta), Aridra (Vata)
 *     Row 2 (Forward ➞):  Punarvasu (Vata), Pushyami (Pitta), Aslesha (Sleshma)
 *     ... continuing across all 9 rows of 3 Nakshatras.
 *
 * - Micro level (108 Nakshatra Padas):
 *     Each of the 27 Nakshatras has 4 Padas (quarters), giving 108 Padas in total.
 *     Dr. B.V. Raman prescribes counting forwards and backwards in threes starting
 *     from Aswini 1 across all 36 rows of 3 Padas:
 *     Row 0 (Forward ➞):  Aswini~1 (Vata), Aswini~2 (Pitta), Aswini~3 (Sleshma)
 *     Row 1 (⇦ Backward): Aswini~4 (Sleshma), Bharani~1 (Pitta), Bharani~2 (Vata)
 *     Row 2 (Forward ➞):  Bharani~3 (Vata), Bharani~4 (Pitta), Krittika~1 (Sleshma)
 *     Row 3 (⇦ Backward): Krittika~2 (Sleshma), Krittika~3 (Pitta), Krittika~4 (Vata)
 *     ... continuing through Revati~4.
 *
 * Option 2 Algorithm:
 * Computed on the fly using Dr. Raman's serpentine forward/backward counting formula:
 *   padaIndex = (nakIndex * 4) + (pada - 1)  // 0 to 107
 *   row = padaIndex / 3                      // 0 to 35
 *   col = padaIndex % 3                      // 0, 1, or 2
 *   Even rows (0, 2, 4...) count Forward:  nadi = col       (0=Vata, 1=Pitta, 2=Sleshma)
 *   Odd rows (1, 3, 5...) count Backward: nadi = 2 - col   (0=Sleshma, 1=Pitta, 2=Vata)
 *
 * @author Astrosoft Team
 */

package app.astrosoft.consts;

public enum Nadi {

	Vata,    // Adi (1st Nadi - Wind constitution)
	Pitta,   // Madhya (2nd Nadi - Bile constitution)
	Sleshma; // Antya (3rd Nadi - Phlegm constitution)

	private static final Nadi[] VALS = values();

	/**
	 * Returns the macro-level Nadi for a given Nakshathra.
	 *
	 * @param nak the Nakshathra enum
	 * @return the macro Nadi (Vata, Pitta, or Sleshma)
	 */
	public static Nadi ofNakshathra(Nakshathra nak) {
		return ofNakshathra(nak.ordinal());
	}

	/**
	 * Returns the macro-level Nadi for a Nakshathra by 0-based index (0 to 26).
	 *
	 * Follows the 3-column serpentine pattern:
	 * - Row r = nakIndex / 3 (0 to 8)
	 * - Col c = nakIndex % 3 (0, 1, 2)
	 * - Even row: forward (c = 0->Vata, 1->Pitta, 2->Sleshma)
	 * - Odd row: backward (2 - c = 0->Sleshma, 1->Pitta, 2->Vata)
	 *
	 * @param nakIndex 0-based index of Nakshatra (0 = Ashwini, ..., 26 = Revathi)
	 * @return the macro Nadi (Vata, Pitta, or Sleshma)
	 */
	public static Nadi ofNakshathra(int nakIndex) {
		int row = nakIndex / 3;
		int col = nakIndex % 3;
		int nadiOrdinal = (row % 2 == 0) ? col : (2 - col);
		return VALS[nadiOrdinal];
	}

	/**
	 * Returns the Pada-level Nadi out of the 108 Nakshatra Padas using Option 2
	 * (on-the-fly serpentine formula).
	 *
	 * Mathematical Formulation:
	 * 1. Map to continuous 0-based pada index P in [0, 107]:
	 *      P = (nakIndex * 4) + (pada - 1)
	 * 2. Identify the 3-element row R in [0, 35]:
	 *      R = P / 3
	 * 3. Identify the column offset K in {0, 1, 2}:
	 *      K = P % 3
	 * 4. Apply serpentine alternating direction:
	 *      - Even rows (0, 2, ...): Forward (0=Vata, 1=Pitta, 2=Sleshma)
	 *      - Odd rows (1, 3, ...): Backward (0=Sleshma, 1=Pitta, 2=Vata) -> 2 - K
	 *
	 * @param nakIndex 0-based Nakshatra index (0 = Ashwini, ..., 26 = Revathi)
	 * @param pada 1-based Nakshatra Pada (1 to 4)
	 * @return the Pada Nadi (Vata, Pitta, or Sleshma)
	 */
	public static Nadi ofPada(int nakIndex, int pada) {
		if (pada < 1 || pada > 4) {
			throw new IllegalArgumentException("Pada must be between 1 and 4, got: " + pada);
		}
		int padaIndex = (nakIndex * 4) + (pada - 1);
		int row = padaIndex / 3;
		int col = padaIndex % 3;

		int nadiOrdinal = (row % 2 == 0) ? col : (2 - col);
		return VALS[nadiOrdinal];
	}

	/**
	 * Returns the Pada-level Nadi for a Nakshathra enum and 1-based pada.
	 *
	 * @param nak the Nakshathra enum
	 * @param pada 1-based Nakshatra Pada (1 to 4)
	 * @return the Pada Nadi (Vata, Pitta, or Sleshma)
	 */
	public static Nadi ofPada(Nakshathra nak, int pada) {
		return ofPada(nak.ordinal(), pada);
	}
}
