/**
 * SingleDasaTimelineEngine.java
 * Core domain orchestrator for extracting and filtering hierarchical 3-level
 * Vimshottari Dasa / Bhukti / Pratyantara (MD / AD / PD) intervals for a single horoscope.
 * Adheres strictly to ARCHITECTURE.md (pure domain logic, zero Swing/UI imports).
 */

package app.astrosoft.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import app.astrosoft.beans.SingleDasaTimelineRow;
import app.astrosoft.consts.Planet;
import app.astrosoft.core.DasaTimelineEngine.WindowFilter;
import app.astrosoft.core.DasaTimelineEngine.WindowType;
import app.astrosoft.util.AstroUtil;

public class SingleDasaTimelineEngine {

	private final Horoscope horoscope;
	private final Calendar birthCal;
	private final Date birthDate;
	private final List<SingleDasaTimelineRow> allRoots;

	public SingleDasaTimelineEngine(Horoscope horoscope) {
		this.horoscope = horoscope;
		this.birthCal = horoscope.getBirthData().birthDay();
		this.birthDate = DasaTimelineEngine.truncateToDay(birthCal.getTime());
		this.allRoots = extractTree();
	}

	public int getMinBirthYear() {
		return birthCal.get(Calendar.YEAR);
	}

	public int getMax120Year() {
		return birthCal.get(Calendar.YEAR) + 120;
	}

	/**
	 * Returns the currently running 3-level Pratyantara node, or null if outside 120-year span.
	 */
	public SingleDasaTimelineRow getCurrentRunningNode() {
		for (SingleDasaTimelineRow md : allRoots) {
			if (md.isCurrent()) {
				for (SingleDasaTimelineRow ad : md.getChildren()) {
					if (ad.isCurrent()) {
						for (SingleDasaTimelineRow pd : ad.getChildren()) {
							if (pd.isCurrent()) {
								return pd;
							}
						}
						return ad;
					}
				}
				return md;
			}
		}
		return null;
	}

	/**
	 * Returns filtered Mahadasha root nodes (with filtered AD and PD children)
	 * for the requested window. Current-period nodes start expanded; others start collapsed.
	 */
	public List<SingleDasaTimelineRow> getTimelineTree(WindowFilter window) {
		Date[] bounds = resolveWindowBounds(window);
		Date rangeStart = bounds[0];
		Date rangeEnd = bounds[1];

		List<SingleDasaTimelineRow> filteredRoots = new ArrayList<SingleDasaTimelineRow>();
		for (SingleDasaTimelineRow md : allRoots) {
			if (md.getEndDate().before(birthDate) || !overlaps(md, rangeStart, rangeEnd)) {
				continue;
			}
			SingleDasaTimelineRow mdCopy = copyNode(md);
			for (SingleDasaTimelineRow ad : md.getChildren()) {
				if (ad.getEndDate().before(birthDate) || !overlaps(ad, rangeStart, rangeEnd)) {
					continue;
				}
				SingleDasaTimelineRow adCopy = copyNode(ad);
				for (SingleDasaTimelineRow pd : ad.getChildren()) {
					if (!pd.getEndDate().before(birthDate) && overlaps(pd, rangeStart, rangeEnd)) {
						adCopy.addChild(copyNode(pd));
					}
				}
				if (adCopy.hasChildren()) {
					mdCopy.addChild(adCopy);
				}
			}
			if (mdCopy.hasChildren()) {
				filteredRoots.add(mdCopy);
			}
		}
		return filteredRoots;
	}

	private static SingleDasaTimelineRow copyNode(SingleDasaTimelineRow src) {
		return new SingleDasaTimelineRow(
				src.getLevel(),
				src.getStartDate(), src.getEndDate(),
				src.getStartDateStr(), src.getEndDateStr(),
				src.getAgeStr(), src.getDurationStr(),
				src.getMd(), src.getAd(), src.getPd(),
				src.isCurrent(), src.isMDChange(), src.isADChange(), src.isChidra(),
				src.getMilestoneStatus());
	}

	private static boolean overlaps(SingleDasaTimelineRow row, Date rangeStart, Date rangeEnd) {
		if (rangeStart != null && row.getEndDate().before(rangeStart)) {
			return false;
		}
		if (rangeEnd != null && row.getStartDate().after(rangeEnd)) {
			return false;
		}
		return true;
	}

	private Date[] resolveWindowBounds(WindowFilter window) {
		if (window == null || window.getType() == WindowType.FULL_120_YEARS) {
			return new Date[] { birthDate, null };
		}
		if (window.getType() == WindowType.NEXT_5_YEARS) {
			Calendar cal = Calendar.getInstance();
			Date start = DasaTimelineEngine.truncateToDay(cal.getTime());
			cal.add(Calendar.YEAR, 5);
			Date end = DasaTimelineEngine.truncateToDay(cal.getTime());
			return new Date[] { start, end };
		}
		Calendar startCal = Calendar.getInstance();
		startCal.set(window.getCustomStartYear(), Calendar.JANUARY, 1, 0, 0, 0);
		startCal.set(Calendar.MILLISECOND, 0);

		Calendar endCal = Calendar.getInstance();
		endCal.set(window.getCustomEndYear(), Calendar.DECEMBER, 31, 0, 0, 0);
		endCal.set(Calendar.MILLISECOND, 0);
		return new Date[] { startCal.getTime(), endCal.getTime() };
	}

	private List<SingleDasaTimelineRow> extractTree() {
		List<SingleDasaTimelineRow> roots = new ArrayList<SingleDasaTimelineRow>();
		Vimshottari v = horoscope.getVimshottariDasa();
		Date today = DasaTimelineEngine.truncateToDay(new Date());

		for (Dasa mdDasa : v.dasas()) {
			VimDasa vmd = (VimDasa) mdDasa;
			Planet mdLord = vmd.getDasa();
			Date mdStart = DasaTimelineEngine.truncateToDay(AstroUtil.decimalYearToDate(vmd.getStart()));
			Date mdEnd = DasaTimelineEngine.truncateToDay(AstroUtil.decimalYearToDate(vmd.getEnd()));
			boolean mdCurrent = isWithin(today, mdStart, mdEnd);

			SingleDasaTimelineRow mdRow = new SingleDasaTimelineRow(
					SingleDasaTimelineRow.LEVEL_MD,
					mdStart, mdEnd,
					Vimshottari.dateFormat.format(mdStart),
					Vimshottari.dateFormat.format(mdEnd),
					DasaTimelineEngine.formatAge(birthCal, mdStart),
					formatDuration(mdStart, mdEnd),
					mdLord, null, null,
					mdCurrent, true, false, false,
					buildStatus(mdCurrent, true, false, false));

			List<VimDasa> adList = new ArrayList<VimDasa>();
			for (Dasa ad : mdDasa.subDasas()) {
				adList.add((VimDasa) ad);
			}

			for (int adIdx = 0; adIdx < adList.size(); adIdx++) {
				VimDasa vad = adList.get(adIdx);
				Planet adLord = vad.getDasa();
				Date adStart = DasaTimelineEngine.truncateToDay(AstroUtil.decimalYearToDate(vad.getStart()));
				Date adEnd = DasaTimelineEngine.truncateToDay(AstroUtil.decimalYearToDate(vad.getEnd()));
				boolean adCurrent = isWithin(today, adStart, adEnd);
				boolean isChidra = (adIdx == adList.size() - 1);

				SingleDasaTimelineRow adRow = new SingleDasaTimelineRow(
						SingleDasaTimelineRow.LEVEL_AD,
						adStart, adEnd,
						Vimshottari.dateFormat.format(adStart),
						Vimshottari.dateFormat.format(adEnd),
						DasaTimelineEngine.formatAge(birthCal, adStart),
						formatDuration(adStart, adEnd),
						mdLord, adLord, null,
						adCurrent, false, true, isChidra,
						buildStatus(adCurrent, false, true, isChidra));

				for (Dasa pd : vad.subDasas()) {
					VimDasa vpd = (VimDasa) pd;
					Planet pdLord = vpd.getDasa();
					Date pdStart = DasaTimelineEngine.truncateToDay(AstroUtil.decimalYearToDate(vpd.getStart()));
					Date pdEnd = DasaTimelineEngine.truncateToDay(AstroUtil.decimalYearToDate(vpd.getEnd()));
					boolean pdCurrent = isWithin(today, pdStart, pdEnd);

					SingleDasaTimelineRow pdRow = new SingleDasaTimelineRow(
							SingleDasaTimelineRow.LEVEL_PD,
							pdStart, pdEnd,
							Vimshottari.dateFormat.format(pdStart),
							Vimshottari.dateFormat.format(pdEnd),
							DasaTimelineEngine.formatAge(birthCal, pdStart),
							formatDuration(pdStart, pdEnd),
							mdLord, adLord, pdLord,
							pdCurrent, false, false, false,
							pdCurrent ? "Current" : "");

					adRow.addChild(pdRow);
				}

				mdRow.addChild(adRow);
			}

			roots.add(mdRow);
		}

		return roots;
	}

	private static boolean isWithin(Date target, Date start, Date end) {
		return !target.before(start) && target.before(end);
	}

	private static String buildStatus(boolean isCurrent, boolean isMDChange, boolean isADChange, boolean isChidra) {
		List<String> tags = new ArrayList<String>();
		if (isCurrent) {
			tags.add("Current");
		}
		if (isMDChange) {
			tags.add("MD Change");
		} else if (isADChange) {
			tags.add("Bhukti Change");
		}
		if (isChidra) {
			tags.add("Dasa Chidra");
		}
		return String.join(" \u2022 ", tags);
	}

	public static String formatDuration(Date start, Date end) {
		Calendar s = Calendar.getInstance();
		s.setTime(start);
		Calendar e = Calendar.getInstance();
		e.setTime(end);

		int years = e.get(Calendar.YEAR) - s.get(Calendar.YEAR);
		int months = e.get(Calendar.MONTH) - s.get(Calendar.MONTH);
		int days = e.get(Calendar.DATE) - s.get(Calendar.DATE);

		if (days < 0) {
			months--;
			Calendar prevMonth = (Calendar) e.clone();
			prevMonth.add(Calendar.MONTH, -1);
			days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
		}
		if (months < 0) {
			years--;
			months += 12;
		}
		if (years < 0) {
			return "0d";
		}

		if (years > 0 && months == 0) {
			return years + "y";
		}
		if (years > 0) {
			return years + "y " + months + "m";
		}
		if (months > 0 && days == 0) {
			return months + "m";
		}
		if (months > 0) {
			return months + "m " + days + "d";
		}
		return days + "d";
	}
}
