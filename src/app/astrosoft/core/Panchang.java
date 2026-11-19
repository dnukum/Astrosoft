/**
*
*  Panchang.java
*
* Created on February 24, 2003, 11:54 AM
*
* @author      E. Rajasekar
*
*/
package app.astrosoft.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import app.astrosoft.beans.ChartData;
import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetChartData;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.consts.AmirthathiYoga;
import app.astrosoft.consts.AstroConsts;
import app.astrosoft.consts.AstrosoftTableColumn;
import app.astrosoft.consts.DisplayStrings;
import app.astrosoft.consts.Karana;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Paksha;
import app.astrosoft.consts.Planet;
import app.astrosoft.consts.Thithi;
import app.astrosoft.consts.Varga;
import app.astrosoft.consts.WeekDay;
import app.astrosoft.consts.Yoga;
import app.astrosoft.pref.AstrosoftPref;
import app.astrosoft.ui.AstroSoft;
import app.astrosoft.ui.table.DefaultColumnMetaData;
import app.astrosoft.ui.table.DefaultTable;
import app.astrosoft.ui.table.MapTableRow;
import app.astrosoft.ui.table.MapTableRowHelper;
import app.astrosoft.ui.table.Table;
import app.astrosoft.ui.table.TableData;
import app.astrosoft.ui.table.TableDataFactory;
import app.astrosoft.util.AstroUtil;
import app.astrosoft.util.SwissHelper;
import app.astrosoft.util.TransitHelper;

import swisseph.*;


public class Panchang {

    private static final AstrosoftPref preferences = AstroSoft.getPreferences();
	private static final double PAN_APPROXIMATION = 0.02;
    
	private SwissHelper swissHelper;
	private Place place;
	private double moon;
	private double sun;
	private double totalSpeed;
    private double diffSpeed;
	private Calendar cal;
	private WeekDay weekday;
	private double sunrise;
	private double sunset;
	private double panTime;
	private double timeZone;
	private boolean exactInstant;
	private double localTimeHours;
    private PanEvent nakshathra;
    private PanEvent thithi;
    private PanEvent yoga;
    private KaranaEvent karana;
    private Paksha pak;
    private Thithi thithiEnum;
    private Yoga yogaEnum;
    private Karana firstKarana;
    
    private EnumMap<Planet, Integer> rasi;
    private EnumMap<Planet, Boolean> dir;
    
    private static DefaultColumnMetaData panchangColumnMetaData;

    /** Creates a new instance of Panchang */
    public Panchang(Calendar cal ) {
        this.place = preferences.getPlace();
        this.cal = java.util.Calendar.getInstance(place.astrosoftTimeZone().getTimeZone());
        this.cal.setTime(cal.getTime());
        calcPanchang();
    }
    
    public Panchang(Date date) {
    	this(date, preferences.getPlace(), false);
	}

    public Panchang(Date date, Place place) {
    	this(date, place, false);
	}

    /**
     * Creates a Panchang for the given date and place. When {@code useExactInstant} is true,
     * calculations use the exact wall-clock time of {@code date} in {@code place}'s time zone
     * instead of {@code preferences.getPanCalcTime()}.
     */
    public Panchang(Date date, Place place, boolean useExactInstant) {
    	this.place = (place != null) ? place : preferences.getPlace();
    	this.exactInstant = useExactInstant;
    	cal = java.util.Calendar.getInstance(this.place.astrosoftTimeZone().getTimeZone());
    	cal.setTime(date);
    	calcPanchang();
	}

    /** Recomputes all Panchang values using the current preferences (Ayanamsa, RahuNode, Language, etc.). */
    public void recalculate() {
    	calcPanchang();
    }

    private final EnumMap<Planet, app.astrosoft.consts.Nakshathra> planetNakshathra =
        new EnumMap<Planet, app.astrosoft.consts.Nakshathra>(Planet.class);

    private final EnumMap<Planet, Double> planetLongitudes = new EnumMap<Planet, Double>(Planet.class);
    public EnumMap<Planet, Double> getPlanetLongitudes() {
        return new EnumMap<Planet, Double>(planetLongitudes);
    }

    public EnumMap<Planet, app.astrosoft.consts.Nakshathra> getPlanetNakshathra() {
        return new EnumMap<Planet, app.astrosoft.consts.Nakshathra>(planetNakshathra);
    }

    private void calcPlanetaryInfo(  ) {

    	if (this.place == null) {
    	    place = preferences.getPlace();
    	}
    	
    	timeZone = ((double)cal.getTimeZone().getRawOffset() / AstroConsts.MILLIS_IN_HR) + (cal.get(Calendar.DST_OFFSET) / AstroConsts.MILLIS_IN_HR) ;
    	place.setTimeZone(timeZone);
    	localTimeHours = exactInstant
    		? AstroUtil.decimal(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))
    		: preferences.getPanCalcTime();
    	panTime = localTimeHours - timeZone;
    	
    	//System.out.println("TZ " + timeZone);
    	    	
    	swissHelper = new SwissHelper(new SweDate( cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), panTime ));
    	
    	EnumMap<Planet, Double> planetPosition = swissHelper.getPlanetaryPosition();
        planetLongitudes.clear();
        planetLongitudes.putAll(planetPosition);
        planetNakshathra.clear();
        for (java.util.Map.Entry<Planet, Double> entry : planetPosition.entrySet()) {
            if (entry.getValue() != null) {
                planetNakshathra.put(entry.getKey(), app.astrosoft.consts.Nakshathra.ofDeg(entry.getValue()));
            }
        }
    	sun = planetPosition.get(Planet.Sun);
    	moon = planetPosition.get(Planet.Moon);
    	rasi = PlanetaryInfo.positionToRasiNum(planetPosition);
    	rasi.put(Planet.Ascendant, swissHelper.getAscendant(place.longitude(), place.latitude()).ordinal() + 1);
    	
    	dir = swissHelper.getPlanetDirection();
    	
    	
    	double moonSpeed = swissHelper.getPlanetSpeed(Planet.Moon);
    	double sunSpeed = swissHelper.getPlanetSpeed(Planet.Sun);
    	
    	totalSpeed = sunSpeed + moonSpeed;
    	diffSpeed = moonSpeed - sunSpeed;
    	
    }

    private void calcPanchang(  ) {

    	calcPlanetaryInfo(  );
    	
    	TransitHelper transitHelper = swissHelper.getTransitHelper(Planet.Moon);
    	
    	Date nakEndDate = transitHelper.getTransitDate(AstroUtil.nakEndPosition(moon), swissHelper.getSweDate().getJulDay());
    	
    	java.util.TimeZone placeTz = place.astrosoftTimeZone().getTimeZone();
    	double nakEndTime = AstroUtil.dateToTimeDouble(nakEndDate, placeTz);
    	
    	Calendar nakEndCal = Calendar.getInstance(placeTz);
    	nakEndCal.setTime(nakEndDate);
    	long dayDiff = LocalDate.of(
    			nakEndCal.get(Calendar.YEAR),
    			nakEndCal.get(Calendar.MONTH) + 1,
    			nakEndCal.get(Calendar.DAY_OF_MONTH)).toEpochDay()
    		- LocalDate.of(
    			cal.get(Calendar.YEAR),
    			cal.get(Calendar.MONTH) + 1,
    			cal.get(Calendar.DAY_OF_MONTH)).toEpochDay();
    	if (dayDiff > 0) {
    		nakEndTime = nakEndTime + 24.0 * dayDiff;
    	}

    	nakshathra = new PanEvent(Nakshathra.ofDeg(moon).toString(), nakEndTime);
    	
    	thithiEnum = Thithi.ofDeg( sun, moon );
    	thithi = new PanEvent(thithiEnum.toString(), findThithiEnd( sun, moon, diffSpeed ));
    	
    	yogaEnum = Yoga.ofDeg( sun , moon );
        yoga = new PanEvent(yogaEnum.toString(), findYogaEnd( ( sun + moon ) % 360, totalSpeed ));
        
        karana = calcKarana();

        pak = Paksha.ofDeg( sun, moon );       
        weekday = WeekDay.ofCalendar( cal );
        sunrise = AstroUtil.getSunRise( cal, place );
        sunset = AstroUtil.getSunSet( cal, place );
    }

    private KaranaEvent calcKarana() {
		
    	KaranaEvent karanaEvent;
    	
    	Karana[] karanas = Karana.ofDeg(sun, moon);
    	firstKarana = karanas[0];
		
		double position = getMoonSunDiff(sun, moon);
		double time = 0;
		double firstTime = 0;
		double secondTime = 0;
		
		double firstBal = AstroConsts.karanaLength - ( position % AstroConsts.karanaLength );
		double expected = (position + firstBal) % 180;
		
		if (expected % AstroConsts.thithiLength == 0){
			
			firstTime = thithi.getEnding();
			
			double secondBal = firstBal + AstroConsts.karanaLength;
	        time = AstroUtil.endTime( diffSpeed, secondBal );
	        secondTime = time + timeZone + computeCorrection(position + AstroConsts.karanaLength, expected + AstroConsts.karanaLength, time, AstroConsts.karanaLength);
	        
		}else {
			time = AstroUtil.endTime( diffSpeed, firstBal );
	        firstTime = time + timeZone + computeCorrection(position, expected, time, AstroConsts.karanaLength);
	        secondTime = thithi.getEnding();
		}
		
		karanaEvent = new KaranaEvent(karanas[0].toString(),firstTime, karanas[1].toString(), secondTime );
		
		return karanaEvent;
	}

	private double findYogaEnd( double position, double speed ) {

        double bal = AstroConsts.yogaLength - ( position % AstroConsts.yogaLength );
        double time = AstroUtil.endTime( speed, bal );
        return time + timeZone + yogaCorrection(position, position + bal, time);
    }
    
    private double yogaCorrection(double position, double expected, double caltime) {
		return yogaCorrection(position, expected, caltime, 0);
	}

	private double yogaCorrection(double position, double expected, double caltime, int depth){
    	
    	double correction = 0.0;
    	 
    	SwissHelper swissHelper = new SwissHelper(new SweDate( cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), caltime));
 	    
 	    EnumMap<Planet, Double> planetPosition = swissHelper.getPlanetaryPosition(EnumSet.of(Planet.Sun, Planet.Moon));
 		
 	    double newmoon = planetPosition.get(Planet.Moon);
 	    double newsun = planetPosition.get(Planet.Sun);
 	    
 	    double newposition = (newsun + newmoon) % 360;
    	
 	    double bal = expected - newposition;
 	    
 	    if (bal > AstroConsts.yogaLength){
 	    	bal = 360 - bal;
 	    }
 	   
 	    correction = AstroUtil.endTime(totalSpeed, bal);
 	    
 	    if (Math.abs(bal) > PAN_APPROXIMATION) {
 	    	if (depth > 20) return correction;
	    	correction = correction + yogaCorrection(newposition, expected, caltime + correction, depth + 1);
 	    }
    	return correction;
    	 
    }
    
	private double findThithiEnd( double sun, double moon, double speed ) {

        double diff = getMoonSunDiff(sun, moon);

        double bal = AstroConsts.thithiLength - ( diff % AstroConsts.thithiLength );
        double time = AstroUtil.endTime( diffSpeed, bal );
        return (timeZone + time + computeCorrection(getMoonSunDiff(sun, moon), diff + bal, time, AstroConsts.thithiLength));
    }
    
	private double computeCorrection(double position, double expected, double caltime, double length) {
		return computeCorrection(position, expected, caltime, length, 0);
	}

	private double computeCorrection(double position, double expected, double caltime, double length, int depth)  {
		
	    double correction = 0.0;
	    
	    SwissHelper swissHelper = new SwissHelper(new SweDate( cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), caltime));
	    
	    EnumMap<Planet, Double> planetPosition = swissHelper.getPlanetaryPosition(EnumSet.of(Planet.Sun, Planet.Moon));
		
	    double newmoon = planetPosition.get(Planet.Moon);
	    double newsun = planetPosition.get(Planet.Sun);
	    
	    double newdiff = getMoonSunDiff(newsun, newmoon);
	
	    double newbal = expected - newdiff;
	
	    if ( (expected % 180) == 0 && newbal > length  ) {
	
	        newbal = newbal - 180;
	    }
	
	    correction = AstroUtil.endTime( diffSpeed, newbal );
	
	    if (Math.abs(newbal) > PAN_APPROXIMATION){
	    	
	    	if (depth > 20) return correction; correction = correction + computeCorrection(getMoonSunDiff(newsun, newmoon), expected , caltime + correction, length, depth + 1);
	    }
	    return correction;
	
	}


    public String rahuKala(){
		return weekday.rahuKala(sunrise, sunset);
	}
	
	public String yamaKanda(){
		return weekday.yamaKanda(sunrise, sunset);
	}
	
	public String[] auspiciousTime(){
		return weekday.auspiciousTime(sunrise, sunset);
	}

	public Calendar getDate() {
		
		return cal;
	};
	
    private String nextThithiName() {
        int slot=(int)(((moon-sun+360)%360)/12);
        int next=(slot+1)%30;
        Thithi result=next==29?Thithi.Amavasya:Thithi.ofIndex(next%15);
        return result.toString();
    }
    private String nextKaranaDetails() {
        double phase=(moon-sun+360)%360;
        int slot=(int)(phase/6);
        String third=PanchangaWindowEngine.karanaAt(((slot+2)%60)*6).toString();
        return PanchangTransitionHelp.until(karana.first.event,karana.first.ending,karana.second.event,cal)
            + PanchangTransitionHelp.until("",karana.second.ending,third,cal);
    }

	private static DefaultColumnMetaData getPanchangColumnMetaData() {
		
		if (panchangColumnMetaData == null){
			panchangColumnMetaData = new DefaultColumnMetaData(AstrosoftTableColumn.keyvalCols());
			panchangColumnMetaData.localizeColumns();
		}
		return panchangColumnMetaData;
	}
	
	public TableData<MapTableRow> getPanchangTableData() {
        return getPanchangTableData(false);
    }

    /** Optional named successors for MyMuhurta; legacy table consumers unchanged. */
    public TableData<MapTableRow> getPanchangTableData(boolean showSuccessors) {
		
		List<MapTableRow> rows = new ArrayList<MapTableRow>();
		
		MapTableRowHelper helper = new MapTableRowHelper(getPanchangColumnMetaData());
		
		rows.add(helper.createRow(DisplayStrings.DATE_STR, AstroUtil.formatDate(cal.getTime())));
		rows.add(helper.createRow(DisplayStrings.DAY_STR, weekday));
		rows.add(helper.createRow(DisplayStrings.NAK_STR + " ( " + AstrosoftTableColumn.End.toString() + " ) ", showSuccessors ? PanchangTransitionHelp.until(nakshathra.event, nakshathra.ending, Nakshathra.ofDeg(moon).next().toString(), cal) : nakshathra));
		rows.add(helper.createRow(DisplayStrings.THITHI_STR + " ( " + AstrosoftTableColumn.End.toString() + " ) ", showSuccessors ? PanchangTransitionHelp.until(thithi.event, thithi.ending, nextThithiName(), cal) : thithi));
		rows.add(helper.createRow(DisplayStrings.PAKSHA_STR, pak));
		rows.add(helper.createRow(DisplayStrings.YOGA_STR + " ( " + AstrosoftTableColumn.End.toString() + " ) ", showSuccessors ? PanchangTransitionHelp.until(yoga.event, yoga.ending, Yoga.ofIndex(Yoga.ofDeg(sun, moon).ordinal()+1).toString(), cal) : yoga));
		rows.add(helper.createRow(DisplayStrings.KARANA_STR + " ( " + AstrosoftTableColumn.End.toString() + " ) ", showSuccessors ? nextKaranaDetails() : karana));
		rows.add(helper.createRow(DisplayStrings.SUNRISE_STR, AstroUtil.timeFormat(sunrise)));
		rows.add(helper.createRow(DisplayStrings.SUNSET_STR, AstroUtil.timeFormat(sunset)));
		String auspiciousTime = Arrays.toString(weekday.auspiciousTime(sunrise, sunset));
		rows.add(helper.createRow(DisplayStrings.AUS_TIME_STR, auspiciousTime.substring(1, auspiciousTime.length() - 1)));
		rows.add(helper.createRow(DisplayStrings.RAHU_KALA_STR, weekday.rahuKala(sunrise, sunset)));
		rows.add(helper.createRow(DisplayStrings.YAMA_KANDA_STR, weekday.yamaKanda(sunrise, sunset)));
		
		return TableDataFactory.getTableData(rows);
	}
	
	public Table getPanchangTable() {
		
		return new DefaultTable(getPanchangTableData(), getPanchangColumnMetaData());
		
	}
	
	public app.astrosoft.consts.Rasi getRasi() { return app.astrosoft.consts.Rasi.values()[rasi.get(app.astrosoft.consts.Planet.Moon) - 1]; } 
	public app.astrosoft.consts.Nakshathra getNakshathra() { return app.astrosoft.consts.Nakshathra.ofDeg(moon); }
	public java.util.EnumMap<app.astrosoft.consts.Planet, Integer> getPlanetPositions() { return rasi; }
	public java.util.EnumMap<app.astrosoft.consts.Planet, Boolean> getPlanetDirection() { return dir; }
	public ChartData getPlanetChartData(){
		
		return new PlanetChartData(Varga.Rasi, rasi, dir);
	}
	
	public static double getMoonSunDiff(double sun, double moon){
		
		double diff;
		if ( moon > sun ) {

            diff = moon - sun;

        } else {

            diff = ( moon + 360 ) - sun;

        }

        if ( diff > 180.0 ) {

            diff = diff - 180;

        }
        
        return diff;
	}
	
	public WeekDay getWeekday() {
		return weekday;
	}

	/** Vedic solar weekday (sunrise-to-sunrise). */
	public WeekDay getVedicVara() {
		return (localTimeHours < sunrise) ? weekday.prev() : weekday;
	}

	public Place getPlace() {
		return place;
	}

	public double getTimeZoneOffset() {
		return timeZone;
	}

	public double getLocalTimeHours() {
		return localTimeHours;
	}

	public boolean isExactInstant() {
		return exactInstant;
	}

	public double getSunrise() {
		return sunrise;
	}

	public double getSunset() {
		return sunset;
	}

	public Paksha getPaksha() {
		return pak;
	}

	public Thithi getThithiEnum() {
		return thithiEnum;
	}

	public Yoga getYogaEnum() {
		return yogaEnum;
	}

	public Karana getFirstKarana() {
		return firstKarana;
	}

	public double getNakshathraEnd() {
		return nakshathra.getEnding();
	}

	public double getThithiEnd() {
		return thithi.getEnding();
	}

	public double getYogaEnd() {
		return yoga.getEnding();
	}

	public double getFirstKaranaEnd() {
		return karana.first.getEnding();
	}

	private static double percentRemaining(double pos, double span) {
		double mod = ((pos % span) + span) % span;
		return ((span - mod) / span) * 100.0;
	}

	public double getNakshathraPercentLeft() {
		return percentRemaining(moon, AstroConsts.nakLength);
	}

	public double getThithiPercentLeft() {
		double diff = ((moon - sun) % 360.0 + 360.0) % 360.0;
		return percentRemaining(diff, AstroConsts.thithiLength);
	}

	public double getYogaPercentLeft() {
		double sum = ((sun + moon) % 360.0 + 360.0) % 360.0;
		return percentRemaining(sum, AstroConsts.yogaLength);
	}

	public double getKaranaPercentLeft() {
		double diff = ((moon - sun) % 360.0 + 360.0) % 360.0;
		return percentRemaining(diff, AstroConsts.karanaLength);
	}

	public AmirthathiYoga getAmirthathiYoga() {
		return AmirthathiYoga.ofWeekdayAndNakshatra(getVedicVara(), getNakshathra(), false).getFirst();
	}

	public String getAbhijitMuhurta() {
		double dayLen = sunset - sunrise;
		if (dayLen < 0) dayLen += 24.0;
		double start = (sunrise + (7.0 / 15.0) * dayLen) % 24.0;
		double end = (sunrise + (8.0 / 15.0) * dayLen) % 24.0;
		return AstroUtil.timeFormat(start) + " - " + AstroUtil.timeFormat(end);
	}

	public String getBrahmaMuhurta() {
		Calendar prevCal = (Calendar) cal.clone();
		prevCal.add(Calendar.DATE, -1);
		double prevSunset = AstroUtil.getSunSet(prevCal, place);
		double nightLen = (24.0 - prevSunset) + sunrise;
		if (nightLen <= 0 || nightLen > 24.0) nightLen = 12.0;
		double start = sunrise - (2.0 / 15.0) * nightLen;
		double end = sunrise - (1.0 / 15.0) * nightLen;
		if (start < 0) start += 24.0;
		if (end < 0) end += 24.0;
		return AstroUtil.timeFormat(start) + " - " + AstroUtil.timeFormat(end);
	}

    @Override
    public String toString() {
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append(this.cal.getTime() + "\n");
    	sb.append(this.weekday + "\n");
    	sb.append(this.nakshathra + "\n");
    	sb.append(this.thithi + "\n");
    	sb.append(this.pak + "\n");
    	sb.append(this.yoga + "\n");
    	sb.append(this.karana + "\n");
    	sb.append(AstroUtil.timeFormat(this.sunrise) + " - " + AstroUtil.timeFormat(this.sunset) + "\n");
    	return sb.toString();
    }
    
    private static class PanEvent{
    	
    	private String event;
    	private double ending;
    	
    	public PanEvent(String event, double ending){
    		this.event = event;
    		this.ending = ending;
    	}
    	
    	public double getEnding() {
			return ending;
		}
    	
    	public String toString(){
    		return event + " ( " + AstroUtil.todegmin(ending, ":") + " ) ";
    	}
    }

    private static class KaranaEvent {
    	
    	private PanEvent first;
    	private PanEvent second;
    	
    	public KaranaEvent(String first, double firstEnding, String second, double secondEnding){
    		this.first = new PanEvent(first, firstEnding);
    		this.second = new PanEvent(second, secondEnding);
    	}
    	
    	public String toString() {
    		return first + " , " + second;
    	}
    }

	
	
}
