package app.astrosoft.core;

import java.util.*;
import java.util.logging.Logger;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.WeekDay;
import com.web_tomorrow.utils.suntimes.SunTimes;
import com.web_tomorrow.utils.suntimes.Time;

/** Sunrise-based Vara, separate from civil weekday. Scoped cache per evaluation window. */
public final class SunriseVara {
    public interface SunriseSource { long at(Calendar localDate) throws Exception; }
    private static final Logger LOG = Logger.getLogger(SunriseVara.class.getName());
    private final TimeZone zone;
    private final SunriseSource source;
    private final Map<Long, Long> cache = new HashMap<>();
    public SunriseVara(TimeZone zone, SunriseSource source) {
        if (zone == null || source == null) throw new IllegalArgumentException("Timezone and sunrise source required");
        this.zone = (TimeZone) zone.clone(); this.source = source;
    }
    public static SunriseVara forPlace(TimeZone zone, Place place) {
        return new SunriseVara(zone, date -> {
            if (place == null) throw new IllegalStateException("Location required");
            Time utc = SunTimes.getSunriseTimeUTC(date.get(Calendar.YEAR), date.get(Calendar.MONTH)+1,
                date.get(Calendar.DATE), place.longitude(), place.latitude(), SunTimes.ZENITH);
            // Match the existing SunTimes convention, but preserve calculation failures.
            int seconds = utc.getHour()*3600 + utc.getMinute()*60 + utc.getSecond();
            Calendar noon = (Calendar) date.clone(); noon.set(Calendar.HOUR_OF_DAY,12);
            int localSeconds = Math.floorMod(seconds + zone.getOffset(noon.getTimeInMillis())/1000,86400);
            Calendar rise = (Calendar) date.clone();
            rise.set(Calendar.HOUR_OF_DAY,localSeconds/3600);
            rise.set(Calendar.MINUTE,localSeconds%3600/60); rise.set(Calendar.SECOND,localSeconds%60);
            return rise.getTimeInMillis();
        });
    }
    private Calendar dateAt(long instant) {
        Calendar date=Calendar.getInstance(zone); date.setTimeInMillis(instant);
        date.set(Calendar.HOUR_OF_DAY,0);date.set(Calendar.MINUTE,0);date.set(Calendar.SECOND,0);date.set(Calendar.MILLISECOND,0);
        return date;
    }
    public long sunriseAt(long instant) {
        Calendar date=dateAt(instant);long key=date.getTimeInMillis();
        Long saved=cache.get(key);if(saved!=null)return saved;
        long result;
        try {
            result=source.at((Calendar)date.clone());
            Calendar following=(Calendar)date.clone();following.add(Calendar.DATE,1);
            if(result<key || result>=following.getTimeInMillis())throw new IllegalStateException("Invalid sunrise instant");
        } catch(Exception error) {
            Calendar fallback=(Calendar)date.clone();fallback.set(Calendar.HOUR_OF_DAY,6);result=fallback.getTimeInMillis();
            LOG.warning("Sunrise calculation failed for "+date.getTime()+" in "+zone.getID()+"; using local 06:00: "+error);
        }
        cache.put(key,result);return result;
    }
    public WeekDay weekdayAt(long instant) {
        Calendar date=dateAt(instant);if(instant<sunriseAt(instant))date.add(Calendar.DATE,-1);
        return WeekDay.ofIndex(date.get(Calendar.DAY_OF_WEEK)-1);
    }
    public long nextSunrise(long instant) {
        long rise=sunriseAt(instant);if(rise>instant)return rise;
        Calendar tomorrow=dateAt(instant);tomorrow.add(Calendar.DATE,1);return sunriseAt(tomorrow.getTimeInMillis());
    }
}
