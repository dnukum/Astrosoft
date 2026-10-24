/**
 * DasaTimelineEngine.java
 * Core domain orchestrator for extracting, merging, and chronologically analyzing
 * Vimshottari Dasa & Bhukti milestones for Boy and Girl.
 * Adheres strictly to ARCHITECTURE.md (pure domain logic, zero Swing/UI imports).
 */

package app.astrosoft.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import app.astrosoft.beans.DasaTimelineRow;
import app.astrosoft.consts.Planet;
import app.astrosoft.util.AstroUtil;

public class DasaTimelineEngine {

	public enum ScopeFilter {
		OVERLAPPING_AND_CHIDRA, // Default: Shows Sama Dasa, Chidra, and MD shifts
		ALL_MILESTONES,         // Shows every MD and AD transition
		MAJOR_DASA_ONLY         // Shows only major Mahadasha changes
	}

	public enum WindowType {
		NEXT_5_YEARS,
		FULL_120_YEARS,
		CUSTOM_RANGE
	}

	public static class WindowFilter {
		private final WindowType type;
		private final int customStartYear;
		private final int customEndYear;

		public static final WindowFilter NEXT_5_YEARS = new WindowFilter(WindowType.NEXT_5_YEARS, 0, 0);
		public static final WindowFilter FULL_120_YEARS = new WindowFilter(WindowType.FULL_120_YEARS, 0, 0);

		public static WindowFilter custom(int startYear, int endYear) {
			return new WindowFilter(WindowType.CUSTOM_RANGE, startYear, endYear);
		}

		public WindowFilter(WindowType type, int customStartYear, int customEndYear) {
			this.type = type;
			this.customStartYear = customStartYear;
			this.customEndYear = customEndYear;
		}

		public WindowType getType() {
			return type;
		}

		public int getCustomStartYear() {
			return customStartYear;
		}

		public int getCustomEndYear() {
			return customEndYear;
		}
	}

	public enum AgeFilter {
		FROM_AGE_20,
		FROM_BIRTH
	}

	private final Horoscope boyHoroscope;
	private final Horoscope girlHoroscope;
	private final Calendar boyBirthCal;
	private final Calendar girlBirthCal;
	private final List<PersonInterval> boyIntervals;
	private final List<PersonInterval> girlIntervals;

	public DasaTimelineEngine(Horoscope boyHoroscope, Horoscope girlHoroscope) {
		this.boyHoroscope = boyHoroscope;
		this.girlHoroscope = girlHoroscope;
		this.boyBirthCal = boyHoroscope.getBirthData().birthDay();
		this.girlBirthCal = girlHoroscope.getBirthData().birthDay();

		this.boyIntervals = extractIntervals(boyHoroscope);
		this.girlIntervals = extractIntervals(girlHoroscope);
	}

	public int getMinBirthYear() {
		return Math.min(boyBirthCal.get(Calendar.YEAR), girlBirthCal.get(Calendar.YEAR));
	}

	public int getMax120Year() {
		return Math.max(boyBirthCal.get(Calendar.YEAR) + 120, girlBirthCal.get(Calendar.YEAR) + 120);
	}

	/**
	 * Main API to retrieve the timeline rows according to scope and window filters.
	 */
	public List<DasaTimelineRow> getTimelineRows(ScopeFilter scope, WindowFilter window) {
		List<DasaTimelineRow> allRows = buildMergedTimeline(window);

		if (scope == ScopeFilter.ALL_MILESTONES) {
			return allRows;
		}

		List<DasaTimelineRow> filtered = new ArrayList<DasaTimelineRow>();
		for (DasaTimelineRow row : allRows) {
			if (scope == ScopeFilter.OVERLAPPING_AND_CHIDRA) {
				if (row.isSamaDasa() || row.isChidra() || row.isBoyMDChange() || row.isGirlMDChange() || row.isToday()) {
					filtered.add(row);
				}
			} else if (scope == ScopeFilter.MAJOR_DASA_ONLY) {
				if (row.isBoyMDChange() || row.isGirlMDChange() || row.isToday()) {
					filtered.add(row);
				}
			}
		}

		return filtered;
	}

	public List<DasaTimelineRow> getTimelineRows(ScopeFilter scope, AgeFilter ageFilter) {
		return getTimelineRows(scope, (ageFilter == AgeFilter.FROM_BIRTH) ? WindowFilter.FULL_120_YEARS : WindowFilter.NEXT_5_YEARS);
	}

	public List<DasaTimelineRow> getTimelineRows(ScopeFilter scope) {
		return getTimelineRows(scope, WindowFilter.NEXT_5_YEARS);
	}

	/**
	 * Builds the complete unified chronological timeline for the requested window.
	 */
	private List<DasaTimelineRow> buildMergedTimeline(WindowFilter window) {
		SortedSet<Date> boundaries = new TreeSet<Date>();

		for (PersonInterval bi : boyIntervals) {
			boundaries.add(bi.startDate);
		}
		for (PersonInterval gi : girlIntervals) {
			boundaries.add(gi.startDate);
		}

		Date today = truncateToDay(new Date());

		Date earliestBirth = boyBirthCal.getTime().before(girlBirthCal.getTime())
				? truncateToDay(boyBirthCal.getTime())
				: truncateToDay(girlBirthCal.getTime());

		Calendar maxCal = (Calendar) (boyBirthCal.getTime().after(girlBirthCal.getTime())
				? boyBirthCal.clone()
				: girlBirthCal.clone());
		maxCal.add(Calendar.YEAR, 120);
		Date latest120 = truncateToDay(maxCal.getTime());

		Date windowStart;
		Date windowEnd;

		WindowFilter effectiveWindow = (window != null) ? window : WindowFilter.NEXT_5_YEARS;

		if (effectiveWindow.getType() == WindowType.NEXT_5_YEARS) {
			PersonInterval curBoy = findActiveInterval(boyIntervals, today);
			PersonInterval curGirl = findActiveInterval(girlIntervals, today);

			Date curStart = today;
			if (curBoy != null && curGirl != null) {
				curStart = curBoy.startDate.after(curGirl.startDate) ? curBoy.startDate : curGirl.startDate;
			}
			windowStart = curStart;

			Calendar c5 = Calendar.getInstance();
			c5.setTime(today);
			c5.add(Calendar.YEAR, 5);
			windowEnd = truncateToDay(c5.getTime());

			boundaries.add(windowStart);
		} else if (effectiveWindow.getType() == WindowType.CUSTOM_RANGE) {
			Calendar cStart = Calendar.getInstance();
			cStart.clear();
			cStart.set(effectiveWindow.getCustomStartYear(), Calendar.JANUARY, 1, 0, 0, 0);
			cStart.set(Calendar.MILLISECOND, 0);
			windowStart = cStart.getTime();

			Calendar cEnd = Calendar.getInstance();
			cEnd.clear();
			cEnd.set(effectiveWindow.getCustomEndYear(), Calendar.DECEMBER, 31, 23, 59, 59);
			cEnd.set(Calendar.MILLISECOND, 999);
			windowEnd = cEnd.getTime();

			boundaries.add(windowStart);
		} else { // FULL_120_YEARS
			windowStart = earliestBirth;
			windowEnd = latest120;
		}

		// Filter boundaries to milestone dates within [windowStart, windowEnd]
		List<Date> milestoneDates = new ArrayList<Date>();
		for (Date d : boundaries) {
			if (!d.before(windowStart) && !d.after(windowEnd)) {
				milestoneDates.add(d);
			}
		}

		Collections.sort(milestoneDates);

		List<DasaTimelineRow> rows = new ArrayList<DasaTimelineRow>();

		for (int idx = 0; idx < milestoneDates.size(); idx++) {
			Date date = milestoneDates.get(idx);
			Date nextDate = (idx + 1 < milestoneDates.size()) ? milestoneDates.get(idx + 1) : null;

			PersonInterval bi = findActiveInterval(boyIntervals, date);
			PersonInterval gi = findActiveInterval(girlIntervals, date);

			if (bi == null || gi == null) {
				continue;
			}

			double boyAge = computeAgeDecimal(boyBirthCal, date);
			String boyAgeStr = formatAge(boyBirthCal, date);
			double girlAge = computeAgeDecimal(girlBirthCal, date);
			String girlAgeStr = formatAge(girlBirthCal, date);

			boolean isBoyTransition = bi.startDate.equals(date);
			boolean isGirlTransition = gi.startDate.equals(date);

			boolean isBoyMDChange = isBoyTransition && bi.isMDStart;
			boolean isGirlMDChange = isGirlTransition && gi.isMDStart;
			boolean isBoyADChange = isBoyTransition && !bi.isMDStart;
			boolean isGirlADChange = isGirlTransition && !gi.isMDStart;

			boolean isSamaDasa = (bi.md == gi.md);
			boolean isChidra = bi.isChidra || gi.isChidra;

			Date intervalEnd = (nextDate != null) ? nextDate : (bi.endDate.before(gi.endDate) ? bi.endDate : gi.endDate);
			boolean isToday = !date.after(today) && today.before(intervalEnd);

			// Standardized Trigger Label Construction
			String triggerEvent = buildTriggerLabel(isBoyMDChange, isBoyADChange, bi,
					isGirlMDChange, isGirlADChange, gi,
					isSamaDasa, isToday, idx == 0 && !isBoyTransition && !isGirlTransition);

			DasaTimelineRow row = new DasaTimelineRow(
					date, boyAge, boyAgeStr,
					bi.md, bi.ad,
					girlAge, girlAgeStr,
					gi.md, gi.ad,
					triggerEvent,
					isBoyMDChange, isGirlMDChange,
					isBoyADChange, isGirlADChange,
					isSamaDasa, bi.isChidra, gi.isChidra, isToday
			);

			rows.add(row);
		}

		return rows;
	}

	private String buildTriggerLabel(boolean isBoyMDChange, boolean isBoyADChange, PersonInterval bi,
									 boolean isGirlMDChange, boolean isGirlADChange, PersonInterval gi,
									 boolean isSamaDasa, boolean isToday, boolean isBaseline) {
		List<String> events = new ArrayList<String>();

		if (isBoyMDChange) {
			events.add("Boy starts " + bi.md + " Dasha");
		} else if (isBoyADChange) {
			String label = "Boy starts " + bi.ad + " Bukti";
			if (bi.isChidra) {
				label += " (Chidra)";
			}
			events.add(label);
		}

		if (isGirlMDChange) {
			events.add("Girl starts " + gi.md + " Dasha");
		} else if (isGirlADChange) {
			String label = "Girl starts " + gi.ad + " Bukti";
			if (gi.isChidra) {
				label += " (Chidra)";
			}
			events.add(label);
		}

		if (events.isEmpty()) {
			if (isBaseline) {
				events.add("Baseline (Age 20+ Start)");
			} else if (isToday) {
				events.add("Current Running Period (Today)");
			} else if (isSamaDasa) {
				events.add("Same Dasa (" + bi.md + ")");
			} else {
				events.add("Ongoing Period");
			}
		}

		StringBuilder sb = new StringBuilder();
		if (isSamaDasa) {
			sb.append("Same Dasa • ");
		}

		for (int i = 0; i < events.size(); i++) {
			if (i > 0) {
				sb.append(" • ");
			}
			sb.append(events.get(i));
		}

		return sb.toString();
	}

	private PersonInterval findActiveInterval(List<PersonInterval> intervals, Date date) {
		for (PersonInterval interval : intervals) {
			if (!date.before(interval.startDate) && date.before(interval.endDate)) {
				return interval;
			}
		}
		// If exactly on or after the last interval end, return the last one
		if (!intervals.isEmpty()) {
			PersonInterval last = intervals.get(intervals.size() - 1);
			if (!date.before(last.startDate)) {
				return last;
			}
		}
		return null;
	}

	private List<PersonInterval> extractIntervals(Horoscope h) {
		List<PersonInterval> list = new ArrayList<PersonInterval>();
		Vimshottari v = h.getVimshottariDasa();

		for (Dasa mdDasa : v.dasas()) {
			VimDasa vmd = (VimDasa) mdDasa;
			Planet mdLord = vmd.getDasa();

			List<VimDasa> adList = new ArrayList<VimDasa>();
			for (Dasa ad : mdDasa.subDasas()) {
				adList.add((VimDasa) ad);
			}

			for (int i = 0; i < adList.size(); i++) {
				VimDasa vad = adList.get(i);
				Planet adLord = vad.getDasa();
				Date start = truncateToDay(AstroUtil.decimalYearToDate(vad.getStart()));
				Date end = truncateToDay(AstroUtil.decimalYearToDate(vad.getEnd()));

				boolean isMDStart = (i == 0);
				boolean isChidra = (i == adList.size() - 1);

				list.add(new PersonInterval(start, end, mdLord, adLord, isMDStart, isChidra));
			}
		}

		return list;
	}

	public static Date truncateToDay(Date date) {
		Calendar cal = AstroUtil.getCalendar(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static String formatAge(Calendar birthCal, Date milestoneDate) {
		Calendar targetCal = Calendar.getInstance();
		targetCal.setTime(milestoneDate);

		int years = targetCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
		int months = targetCal.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH);
		if (targetCal.get(Calendar.DATE) < birthCal.get(Calendar.DATE)) {
			months--;
		}
		if (months < 0) {
			years--;
			months += 12;
		}
		if (years < 0) {
			return "0y 0m";
		}
		return years + "y " + months + "m";
	}

	public static double computeAgeDecimal(Calendar birthCal, Date milestoneDate) {
		Calendar targetCal = Calendar.getInstance();
		targetCal.setTime(milestoneDate);

		int years = targetCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
		int months = targetCal.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH);
		if (targetCal.get(Calendar.DATE) < birthCal.get(Calendar.DATE)) {
			months--;
		}
		if (months < 0) {
			years--;
			months += 12;
		}
		return Math.max(0.0, years + (months / 12.0));
	}

	private static class PersonInterval {
		final Date startDate;
		final Date endDate;
		final Planet md;
		final Planet ad;
		final boolean isMDStart;
		final boolean isChidra;

		PersonInterval(Date startDate, Date endDate, Planet md, Planet ad, boolean isMDStart, boolean isChidra) {
			this.startDate = startDate;
			this.endDate = endDate;
			this.md = md;
			this.ad = ad;
			this.isMDStart = isMDStart;
			this.isChidra = isChidra;
		}
	}
}
