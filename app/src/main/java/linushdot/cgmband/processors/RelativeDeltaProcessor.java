package linushdot.cgmband.processors;

import java.util.Locale;

import linushdot.cgmband.CgmValue;
import linushdot.cgmband.CgmValueProcessor;

/***
 * Processes values by their relative delta. Notifications are skipped as long as the difference
 * between the new value and the last value displayed stays in a certain relative range (given in
 * percent).
 *
 * Also a maximum time difference can be set, forcing a notification at least every x minutes.
 */
public class RelativeDeltaProcessor implements CgmValueProcessor {

    private final double minDelta;

    private final double maxDelta;

    private final long maxTimeDiff;

    private CgmValue lastSentValue;

    /***
     * Creates a new relative delta processor with the parameters:
     * minDelta = 0.1
     * maxDelta = 0.1
     * maxtimeDiffMins = 119
     */
    public RelativeDeltaProcessor() {
        this.minDelta = 0.1;
        this.maxDelta = 0.1;
        this.maxTimeDiff = 119 * 60 * 1000;
    }

    /***
     * Creates new relative delta processor.
     * @param minDelta decrease in percent (0..0%, 1..100%)
     * @param maxDelta increase in percent (0..0%, 1..100%)
     * @param maxTimeDiffMins maximum time difference in minutes
     */
    public RelativeDeltaProcessor(double minDelta, double maxDelta, long maxTimeDiffMins) {
        this.minDelta = minDelta;
        this.maxDelta = maxDelta;
        this.maxTimeDiff = maxTimeDiffMins * 60 * 1000;
    }

    @Override
    public String process(CgmValue value) {
        if(lastSentValue == null) {
            lastSentValue = value;
            return getNotificationText(value);
        }

        final long timediff = value.getTime() - lastSentValue.getTime();
        if(timediff >= maxTimeDiff) {
            final String result = getNotificationText(lastSentValue, value);
            lastSentValue = value;
            return result;
        }

        final float valdiff = value.getValue() - lastSentValue.getValue();
        final double reldiff = valdiff / lastSentValue.getValue();

        if(reldiff <= -minDelta || reldiff >= maxDelta) {
            final String result = getNotificationText(lastSentValue, value);
            lastSentValue = value;
            return result;
        }

        return null;
    }

    private String getNotificationText(CgmValue first) {
        return String.format(Locale.getDefault(),
                "%s %s",
                formatFloat(first.getValue()), first.getUnit());
    }

    /***
     * Generates a message in the format "{value} {unit}\n{+-change}/{time} m"
     * @param previous last displayed value
     * @param next new value
     * @return notification message
     */
    private String getNotificationText(CgmValue previous, CgmValue next) {

        final long timediff = next.getTime() - previous.getTime();
        final long minutes = Math.round((double) timediff / 60000.0);

        final float valdiff = next.getValue() - previous.getValue();
        char sign = ' ';
        if(valdiff > 0) {
            sign = '+';
        } else if(valdiff < 0) {
            sign = '-';
        }

        return String.format(Locale.getDefault(),
                "%s %s\n%c%s/%dm",
                formatFloat(next.getValue()), next.getUnit(),
                sign, formatFloat(Math.abs(valdiff)), minutes);
    }

    /***
     * Formats float values to display either as integer (mg/dl) or as a float with 2 decimal places (mmol/l)
     * @param value value to format
     * @return formatted string
     */
    private String formatFloat(float value) {
        if(value % 1.0 == 0) {
            return Integer.toString((int) value);
        } else {
            return String.format(Locale.getDefault(), "%.2f", value);
        }
    }
}
