package app.astrosoft.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/** Bridge instant-based Muhurta context and legacy host-zone date/time choosers. */
public final class MuhurtaDateTime {
    private MuhurtaDateTime() {}

    public static Date toChooserDate(Date instant, TimeZone zone) {
        Calendar local = Calendar.getInstance(zone);
        local.setTime(instant);
        Calendar chooser = Calendar.getInstance();
        chooser.clear();
        chooser.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH),
            local.get(Calendar.HOUR_OF_DAY), local.get(Calendar.MINUTE), local.get(Calendar.SECOND));
        return chooser.getTime();
    }

    public static Date fromChooserValues(Date date, Date time, TimeZone zone) {
        Calendar day = Calendar.getInstance(); day.setTime(date);
        Calendar clock = Calendar.getInstance(); clock.setTime(time);
        Calendar local = Calendar.getInstance(zone);
        local.clear();
        local.setLenient(false);
        local.set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH),
            clock.get(Calendar.HOUR_OF_DAY), clock.get(Calendar.MINUTE), clock.get(Calendar.SECOND));
        return local.getTime();
    }
}
