import app.astrosoft.beans.Place;
import app.astrosoft.beans.Place.Direction;
import app.astrosoft.beans.Place.Location;
import app.astrosoft.core.Panchang;
import java.util.Calendar;
import java.util.TimeZone;

public class test_muhurta {
    public static void main(String[] args) {
        Location houstonLat = new Location("29", "45", Direction.NORTH);
        Location houstonLon = new Location("95", "21", Direction.WEST);
        Place houston = new Place("Houston", "TX", "USA", houstonLat, houstonLon, "America/Chicago");
        
        TimeZone tz = houston.astrosoftTimeZone().getTimeZone();
        Calendar cal = Calendar.getInstance(tz);
        
        Calendar systemCal = Calendar.getInstance();
        systemCal.set(2026, 8, 29); // Sep 29, 2026
        
        cal.set(Calendar.YEAR, systemCal.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, systemCal.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, systemCal.get(Calendar.DAY_OF_MONTH));
        
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 45);
        cal.set(Calendar.SECOND, 0);
        
        Panchang pan = new Panchang(cal.getTime(), houston);
        
        System.out.println("Sunrise from Panchang Table: " + pan.getPanchangTableData().getRow(7).getColumnData(app.astrosoft.consts.AstrosoftTableColumn.Value));
        System.out.println("Sunset from Panchang Table: " + pan.getPanchangTableData().getRow(8).getColumnData(app.astrosoft.consts.AstrosoftTableColumn.Value));
    }
}
