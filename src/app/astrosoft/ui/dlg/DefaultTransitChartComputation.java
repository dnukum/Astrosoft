package app.astrosoft.ui.dlg;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.beans.PlanetaryInfo;
import app.astrosoft.core.Horoscope;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DefaultTransitChartComputation implements TransitChartComputation {
    @Override
    public PlanetaryInfo compute(Date instant, Place place) {
        if (instant == null) {
            throw new IllegalArgumentException("instant is required");
        }
        if (place == null) {
            throw new IllegalArgumentException("place is required");
        }
        Calendar cal = new GregorianCalendar(place.astrosoftTimeZone().getTimeZone());
        cal.setTime(instant);
        BirthData birthData = new BirthData("Transit", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), place);
        Horoscope horoscope = new Horoscope(birthData);
        return horoscope.getPlanetaryInfo();
    }
}
