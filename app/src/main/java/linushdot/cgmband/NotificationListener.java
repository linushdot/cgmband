package linushdot.cgmband;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;

import java.util.Collections;
import java.util.List;

import linushdot.cgmband.dexcomg6.DexcomValueExtractor;
import linushdot.cgmband.filters.TimeOfDayFilter;
import linushdot.cgmband.processors.RelativeDeltaProcessor;

/***
 * Service for receiving posted notifications, extracting and processing them.
 *
 * This service requires notification access. After being granted it, new notifications are received
 * and the service can be requested to read all existing notifications by sending an intent
 * with the "linushdot.cgmband.NOTIFICATION_REQUEST" action and a "command" extra with the value
 * "get".
 *
 * The notifications run through an extractor, then the processor and finally filters can keep
 * notifications from being displayed under certain conditions.
 *
 * The current configuration is:
 * - Extractor: {@link DexcomValueExtractor}
 * - Processor: {@link RelativeDeltaProcessor}
 * - Filter: {@link NotificationFilter}
 */
public class NotificationListener extends NotificationListenerService {

    public static final String ACTION_REQUEST = "linushdot.cgmband.NOTIFICATION_REQUEST";

    public static final String EXTRA_COMMAND = "command";
    public static final String COMMAND_GET = "get";

    public static final String ACTION_RESPONSE = "linushdot.cgmband.NOTIFICATION_RESPONSE";

    public static final String EXTRA_VALUE = "value";

    public static final int NOTIFICATION_ID = 10;
    public static final String NOTIFICATION_CHANNEL_ID = "values";

    private BroadcastReceiver receiver;

    private CgmValueExtractor extractor = new DexcomValueExtractor();

    private CgmValueProcessor processor = new RelativeDeltaProcessor();

    private List<NotificationFilter> filters = null;

    @Override
    public void onCreate() {
        super.onCreate();

        filters = Collections.singletonList((NotificationFilter)
                new TimeOfDayFilter(getApplicationContext().getSharedPreferences("prefs", 0)));

        createNotificationChannel();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getStringExtra(EXTRA_COMMAND).equals(COMMAND_GET)) {
                    for(StatusBarNotification sbn : NotificationListener.this.getActiveNotifications()) {
                        extractAndHandle(sbn);
                    }
                }
            }
        };
        registerReceiver(receiver, new IntentFilter(ACTION_REQUEST));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        extractAndHandle(sbn);
    }

    /**
     * Extracts value and broadcasts it, then displays notification if the processor generates one.
     *
     * Only processes matching notifications, nop for others.
     *
     * @param sbn status bar notification received
     */
    private void extractAndHandle(StatusBarNotification sbn) {
        if(extractor.match(sbn)) {
            final CgmValue value = extractor.extract(sbn);
            if(value == null) {
                return; // extraction failed
            }

            // broadcast value
            final Intent i = new Intent(ACTION_RESPONSE);
            i.putExtra(EXTRA_VALUE, value);
            sendBroadcast(i);

            // process value, notify if necessary
            final String notification = processor.process(value);
            if(notification != null) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancelAll();

                // check filters, display if no filter is positive
                for(NotificationFilter filter : filters) {
                    if(filter.filter(value, notification)) {
                        return;
                    }
                }

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(notification);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    private void createNotificationChannel() {
        final NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                "Values", NotificationManager.IMPORTANCE_DEFAULT);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

}
