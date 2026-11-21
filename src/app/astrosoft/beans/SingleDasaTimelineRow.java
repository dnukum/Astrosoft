/**
 * SingleDasaTimelineRow.java
 * Hierarchical domain node representing a Mahadasha (level 0), Bhukti (level 1),
 * or Pratyantara Dasa (level 2) interval for a single horoscope's Dasa timeline.
 */

package app.astrosoft.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import app.astrosoft.consts.Planet;

public class SingleDasaTimelineRow {

	public static final int LEVEL_MD = 0;
	public static final int LEVEL_AD = 1;
	public static final int LEVEL_PD = 2;

	private final int level;
	private final Date startDate;
	private final Date endDate;
	private final String startDateStr;
	private final String endDateStr;
	private final String ageStr;
	private final String durationStr;

	private final Planet md;
	private final Planet ad;
	private final Planet pd;

	private final boolean isCurrent;
	private final boolean isMDChange;
	private final boolean isADChange;
	private final boolean isChidra;
	private final String milestoneStatus;

	private final List<SingleDasaTimelineRow> children = new ArrayList<SingleDasaTimelineRow>();
	private boolean expanded;

	public SingleDasaTimelineRow(
			int level,
			Date startDate, Date endDate,
			String startDateStr, String endDateStr,
			String ageStr, String durationStr,
			Planet md, Planet ad, Planet pd,
			boolean isCurrent, boolean isMDChange, boolean isADChange, boolean isChidra,
			String milestoneStatus) {
		this.level = level;
		this.startDate = startDate;
		this.endDate = endDate;
		this.startDateStr = startDateStr;
		this.endDateStr = endDateStr;
		this.ageStr = ageStr;
		this.durationStr = durationStr;
		this.md = md;
		this.ad = ad;
		this.pd = pd;
		this.isCurrent = isCurrent;
		this.isMDChange = isMDChange;
		this.isADChange = isADChange;
		this.isChidra = isChidra;
		this.milestoneStatus = milestoneStatus;
		this.expanded = isCurrent && level < LEVEL_PD;
	}

	public void addChild(SingleDasaTimelineRow child) {
		children.add(child);
	}

	public List<SingleDasaTimelineRow> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public int getLevel() {
		return level;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public String getStartDateStr() {
		return startDateStr;
	}

	public String getEndDateStr() {
		return endDateStr;
	}

	public String getAgeStr() {
		return ageStr;
	}

	public String getDurationStr() {
		return durationStr;
	}

	public Planet getMd() {
		return md;
	}

	public Planet getAd() {
		return ad;
	}

	public Planet getPd() {
		return pd;
	}

	public boolean isCurrent() {
		return isCurrent;
	}

	public boolean isMDChange() {
		return isMDChange;
	}

	public boolean isADChange() {
		return isADChange;
	}

	public boolean isChidra() {
		return isChidra;
	}

	public String getMilestoneStatus() {
		return milestoneStatus;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		if (hasChildren()) {
			this.expanded = expanded;
		}
	}

	public String getDisplayLabel() {
		if (level == LEVEL_MD) {
			return md.toString();
		} else if (level == LEVEL_AD) {
			return md.toString() + " / " + ad.toString();
		} else {
			return pd.toString();
		}
	}
}
