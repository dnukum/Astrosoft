package app.astrosoft.consts;

/**
 * Whole-sign dignity of a planet in a rasi. At most one value applies; where several
 * tests are true (e.g. Mercury in Virgo) the highest wins: EXALTED, then MOOLATRIKONA, then OWN.
 */
public enum Dignity {
	EXALTED, DEBILITATED, MOOLATRIKONA, OWN, NONE
}
