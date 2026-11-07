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

	public String displayName() {
		switch (this) {
			case BENEFIC: return "Functional Benefic";
			case MALEFIC: return "Functional Malefic";
			case NEUTRAL: return "Functional Neutral";
			default: return name();
		}
	}
}
