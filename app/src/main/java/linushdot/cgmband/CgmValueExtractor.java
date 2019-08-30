package linushdot.cgmband;

import android.service.notification.StatusBarNotification;

/***
 * Value extractors match certain notifications and extract CGM values (value, unit and time) from
 * notifications.
 */
public interface CgmValueExtractor {

    /***
     * Check if notification matches this extractor, i.e. is a notification from the CGM this
     * extractor supports.
     * @param sbn Status bar notification to check
     * @return true if the notification matches, false otherwise
     */
    boolean match(StatusBarNotification sbn);

    /***
     * Extracts a {@link CgmValue} from a notification.
     * @param sbn Status bar notification to extract from
     * @return the extracted {@link CgmValue}, null if not successful
     */
    CgmValue extract(StatusBarNotification sbn);

}
