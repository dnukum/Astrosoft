/**
 * FunctionalRole.java
 * Astrosoft Functional Planetary Character (Benefic, Malefic, Neutral)
 * based on Ascendant / Lagna lordship per Dr. B.V. Raman.
 */
package app.astrosoft.consts;

public enum FunctionalRole {

	BENEFIC,
	MALEFIC,
	NEUTRAL;

	public boolean isBenefic() {
		return this == BENEFIC;
	}

	public boolean isMalefic() {
		return this == MALEFIC;
	}

	public boolean isNeutral() {
		return this == NEUTRAL;
	}
}
