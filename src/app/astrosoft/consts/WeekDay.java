/**
 * WeekDay.java
 * Created On 2006, Jan 7, 2006 8:11:43 PM
 * @author E. Rajasekar
 */

package app.astrosoft.consts;

import java.util.Calendar;

import app.astrosoft.util.Internalization;

public enum WeekDay {

	Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday;
	
	private static final double[] rahukalaOffsets = { 10.5, 1.5, 9.0, 6.0, 7.5, 4.5, 3.0 };
	
	private static final double[] yamakandaOffsets = { 6.0, 4.5, 3.0, 1.5, 0.0, 9.0, 7.5 };
    
	private static final double[][][] auspiciousTimeOffsets = {
	    { {1.5, 4.0, 0}, {8.0, 10.5, 0}, {3.0, 6.0, 1} },
	    { {0.0, 1.0, 0}, {6.0, 8.0, 0}, {0.0, 3.0, 1}, {4.0, 5.0, 1} },
	    { {4.5, 5.0, 0}, {6.0, 7.0, 0}, {10.5, 12.0, 0}, {1.0, 2.0, 1} },
	    { {3.0, 4.0, 0}, {7.5, 9.0, 0}, {10.0, 11.0, 0}, {1.0, 4.0, 1}, {5.0, 6.0, 1} },
	    { {3.0, 4.5, 0}, {7.0, 7.5, 0}, {10.5, 12.0, 0}, {0.0, 1.0, 1}, {2.0, 3.0, 1} },
	    { {0.0, 3.0, 0}, {7.0, 7.5, 0}, {11.0, 12.0, 0}, {2.0, 3.0, 1}, {4.5, 5.0, 1} },
	    { {1.0, 1.5, 0}, {4.5, 6.0, 0}, {6.0, 7.0, 0}, {11.0, 12.0, 0}, {0.0, 1.5, 1}, {3.0, 4.0, 1} }
	};
	
	private static WeekDay vals[] = values();
	
	public static WeekDay ofIndex(int index) {
		return vals[index % vals.length];
	}
	
	public static WeekDay ofDay( int yr, int mon, int date ) {

        return ofCalendar(new java.util.GregorianCalendar( yr, mon - 1, date ));
    }
	
	public static WeekDay ofCalendar(Calendar cal){
		return WeekDay.ofIndex( cal.get( java.util.Calendar.DAY_OF_WEEK ) - 1 );
	}
	
	private String formatDynamicTime(double startOffset, double endOffset, boolean isNight, double sunrise, double sunset) {
		double dayLen = sunset - sunrise;
		if (dayLen < 0) dayLen += 24.0;
		double nightLen = 24.0 - dayLen;
		
		double dynStart, dynEnd;
		if (!isNight) {
			dynStart = sunrise + (startOffset / 12.0) * dayLen;
			dynEnd = sunrise + (endOffset / 12.0) * dayLen;
		} else {
			dynStart = sunset + (startOffset / 12.0) * nightLen;
			dynEnd = sunset + (endOffset / 12.0) * nightLen;
		}
		
		if (dynStart >= 24.0) dynStart -= 24.0;
		if (dynEnd >= 24.0) dynEnd -= 24.0;
		
		return app.astrosoft.util.AstroUtil.timeFormat(dynStart) + " - " + app.astrosoft.util.AstroUtil.timeFormat(dynEnd);
	}
	
	public String rahuKala(double sunrise, double sunset){
		double start = rahukalaOffsets[ordinal()];
		return formatDynamicTime(start, start + 1.5, false, sunrise, sunset);
	}
	
	public String yamaKanda(double sunrise, double sunset){
		double start = yamakandaOffsets[ordinal()];
		return formatDynamicTime(start, start + 1.5, false, sunrise, sunset);
	}
	
	public String[] auspiciousTime(double sunrise, double sunset){
		double[][] blocks = auspiciousTimeOffsets[ordinal()];
		java.util.List<String> list = new java.util.ArrayList<String>();
		for (double[] b : blocks) {
			list.add(formatDynamicTime(b[0], b[1], b[2] == 1.0, sunrise, sunset));
		}
		return list.toArray(new String[0]);
	}
	
	public String sym(){
		return this.name().substring(0,3);
	}
	
	public String toString() {

		return Internalization.getString(this.name());
	}
}

