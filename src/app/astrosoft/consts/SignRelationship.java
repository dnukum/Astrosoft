package app.astrosoft.consts;

/**
 * Planetary relationship with a zodiac sign (Kshetra Sambandha).
 * Determines the comfort and natural dignity of a planet in a sign based on
 * exaltation, debilitation, ownership, and natural friendship with the sign's dispositor (lord).
 */
public enum SignRelationship {

	EXALTED("Exalted"),
	DEBILITATED("Debilitated"),
	OWN("Own"),
	FRIENDLY("Friendly"),
	NEUTRAL("Neutral"),
	ENEMY("Enemy");

	private final String label;

	SignRelationship(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	@Override
	public String toString() {
		return label;
	}
}
