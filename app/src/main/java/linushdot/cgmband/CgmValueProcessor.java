package linushdot.cgmband;

/***
 * Value processors are supplied with {@link CgmValue}s after they are extracted. The processor
 * returns a string with a message, if a notification should be displayed.
 */
public interface CgmValueProcessor {

    /**
     * Handles a series of CGM values, returns a notification for the band if necessary
     * @param value CGM value
     * @return Notification text to send, null otherwise
     */
    String process(CgmValue value);

}
