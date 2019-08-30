package linushdot.cgmband.filters;

import android.content.SharedPreferences;

import java.time.LocalTime;

import linushdot.cgmband.CgmValue;
import linushdot.cgmband.NotificationFilter;

/***
 * Filter by time of day.
 *
 * Turns on/off notifications for a certain time of the day. Defined by start and end time.
 */
public class TimeOfDayFilter implements NotificationFilter {

    public static final String KEY_FILTER_START_HOUR   = "filter_start_hour";
    public static final String KEY_FILTER_START_MINUTE = "filter_start_minute";

    public static final String KEY_FILTER_END_HOUR     = "filter_end_hour";
    public static final String KEY_FILTER_END_MINUTE   = "filter_end_minute";

    private final SharedPreferences prefs;

    public TimeOfDayFilter(final SharedPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public boolean filter(CgmValue value, String notification) {
        final int startHour = prefs.getInt(KEY_FILTER_START_HOUR, 0);
        final int startMinute = prefs.getInt(KEY_FILTER_START_MINUTE, 0);
        final int endHour = prefs.getInt(KEY_FILTER_END_HOUR, 0);
        final int endMinute = prefs.getInt(KEY_FILTER_END_MINUTE, 0);

        if(startHour == 0 && startMinute == 0 && endHour == 0 && endMinute == 0) {
            return false;
        }

        final LocalTime start = LocalTime.of(startHour, startMinute);
        final LocalTime end = LocalTime.of(endHour, endMinute);
        final LocalTime now = LocalTime.now();

        return now.isBefore(start) || now.isAfter(end);
    }
}
