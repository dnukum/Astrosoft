/**
 * AshtavargaStrength.java
 *
 * Represents astrological qualitative strength of an Ashtakavarga house
 * based on its bindu count for Sarva Ashtakavarga and Bhinna Ashtakavarga.
 */
package app.astrosoft.consts;

public enum AshtavargaStrength {

	WEAK,
	NEUTRAL,
	STRONG;

	/**
	 * Evaluates Ashtakavarga bindu strength based on classical thresholds:
	 * - For Sarva Ashtakavarga (SAV): <= 25 is WEAK, >= 30 is STRONG, 26-29 is NEUTRAL.
	 * - For Bhinna Ashtakavarga (planets): <= 2 is WEAK, >= 5 is STRONG, 3-4 is NEUTRAL.
	 */
	public static AshtavargaStrength of(boolean isSarvaAshtavarga, int binduCount) {
		if (isSarvaAshtavarga) {
			if (binduCount <= 25) {
				return WEAK;
			} else if (binduCount >= 30) {
				return STRONG;
			} else {
				return NEUTRAL;
			}
		} else {
			if (binduCount <= 2) {
				return WEAK;
			} else if (binduCount >= 5) {
				return STRONG;
			} else {
				return NEUTRAL;
			}
		}
	}

	public static AshtavargaStrength of(AshtavargaName name, int binduCount) {
		return of(name == AshtavargaName.SarvaAshtavarga, binduCount);
	}
}
