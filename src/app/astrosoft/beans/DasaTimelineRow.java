/**
 * DasaTimelineRow.java
 * Immutable data transfer bean representing a single synchronized milestone interval
 * on the combined Boy & Girl Dasha-Bhukti synastry timeline.
 */

package app.astrosoft.beans;

import java.util.Date;
import app.astrosoft.consts.Planet;
import app.astrosoft.core.Vimshottari;

public class DasaTimelineRow {

	private final Date startDate;
	private final String startDateStr;
	private final double boyAge;
	private final String boyAgeStr;
	private final Planet boyMD;
	private final Planet boyAD;
	private final double girlAge;
	private final String girlAgeStr;
	private final Planet girlMD;
	private final Planet girlAD;
	private final String triggerEvent;
	private final boolean isBoyMDChange;
	private final boolean isGirlMDChange;
	private final boolean isBoyADChange;
	private final boolean isGirlADChange;
	private final boolean isSamaDasa;
	private final boolean isBoyChidra;
	private final boolean isGirlChidra;
	private final boolean isToday;

	public DasaTimelineRow(Date startDate, double boyAge, String boyAgeStr,
						   Planet boyMD, Planet boyAD,
						   double girlAge, String girlAgeStr,
						   Planet girlMD, Planet girlAD,
						   String triggerEvent,
						   boolean isBoyMDChange, boolean isGirlMDChange,
						   boolean isBoyADChange, boolean isGirlADChange,
						   boolean isSamaDasa, boolean isBoyChidra, boolean isGirlChidra, boolean isToday) {
		this.startDate = startDate;
		this.startDateStr = Vimshottari.dateFormat.format(startDate);
		this.boyAge = boyAge;
		this.boyAgeStr = boyAgeStr;
		this.boyMD = boyMD;
		this.boyAD = boyAD;
		this.girlAge = girlAge;
		this.girlAgeStr = girlAgeStr;
		this.girlMD = girlMD;
		this.girlAD = girlAD;
		this.triggerEvent = triggerEvent;
		this.isBoyMDChange = isBoyMDChange;
		this.isGirlMDChange = isGirlMDChange;
		this.isBoyADChange = isBoyADChange;
		this.isGirlADChange = isGirlADChange;
		this.isSamaDasa = isSamaDasa;
		this.isBoyChidra = isBoyChidra;
		this.isGirlChidra = isGirlChidra;
		this.isToday = isToday;
	}

	public Date getStartDate() {
		return startDate;
	}

	public String getStartDateStr() {
		return startDateStr;
	}

	public double getBoyAge() {
		return boyAge;
	}

	public String getBoyAgeStr() {
		return boyAgeStr;
	}

	public Planet getBoyMD() {
		return boyMD;
	}

	public Planet getBoyAD() {
		return boyAD;
	}

	public double getGirlAge() {
		return girlAge;
	}

	public String getGirlAgeStr() {
		return girlAgeStr;
	}

	public Planet getGirlMD() {
		return girlMD;
	}

	public Planet getGirlAD() {
		return girlAD;
	}

	public String getTriggerEvent() {
		return triggerEvent;
	}

	public boolean isBoyMDChange() {
		return isBoyMDChange;
	}

	public boolean isGirlMDChange() {
		return isGirlMDChange;
	}

	public boolean isBoyADChange() {
		return isBoyADChange;
	}

	public boolean isGirlADChange() {
		return isGirlADChange;
	}

	public boolean isSamaDasa() {
		return isSamaDasa;
	}

	public boolean isBoyChidra() {
		return isBoyChidra;
	}

	public boolean isGirlChidra() {
		return isGirlChidra;
	}

	public boolean isChidra() {
		return isBoyChidra || isGirlChidra;
	}

	public boolean isToday() {
		return isToday;
	}

	@Override
	public String toString() {
		return String.format("[%s] Boy(%s, %s/%s) | Girl(%s, %s/%s) -> %s",
				startDateStr, boyAgeStr, boyMD, boyAD, girlAgeStr, girlMD, girlAD, triggerEvent);
	}
}
