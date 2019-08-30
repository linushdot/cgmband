package linushdot.cgmband;

public interface NotificationFilter {

    /***
     * Filter notification.
     *
     * @param value CGM value for notification
     * @param notification Notification message
     * @return true if message should _not_ be displayed, false if it should
     */
    boolean filter(CgmValue value, String notification);

}
