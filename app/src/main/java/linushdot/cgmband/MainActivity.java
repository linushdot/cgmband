package linushdot.cgmband;

import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import linushdot.cgmband.filters.TimeOfDayFilter;

/***
 * Main activity with the following features:
 *
 * Granting notification access: a button is displayed and enabled if notification access needs to
 * be granted.
 *
 * Get value: a second button lets the user request the first value to be extracted and displayed
 *
 * Set time of day: set the start and end time of the {@link TimeOfDayFilter}
 */
public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver receiver;

    private TextView text;

    private TextView timeOfDay;

    private SharedPreferences prefs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getApplicationContext().getSharedPreferences("prefs", 0);

        setContentView(R.layout.activity_main);

        text = findViewById(R.id.text);
        timeOfDay = findViewById(R.id.timeOfDay);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final CgmValue value = (CgmValue) intent.getSerializableExtra(NotificationListener.EXTRA_VALUE);
                final Date postTime = new Date(value.getTime());
                text.setText(String.format(Locale.getDefault(), "%.2f %s @%s",
                        value.getValue(), value.getUnit(),
                        new SimpleDateFormat("HH:mm", Locale.getDefault()).format(postTime)));
            }
        };
        registerReceiver(receiver, new IntentFilter(NotificationListener.ACTION_RESPONSE));
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadPrefs();

        // disable button if notification access already given
        boolean notificationAccess = false;
        for(String pkg : NotificationManagerCompat.getEnabledListenerPackages(this)) {
            if(getApplication().getPackageName().equals(pkg)) {
                notificationAccess = true;
                break;
            }
        }
        findViewById(R.id.notification_access).setEnabled(!notificationAccess);
    }

    private void loadPrefs() {
        final int startHour = prefs.getInt(TimeOfDayFilter.KEY_FILTER_START_HOUR, 0);
        final int startMinute = prefs.getInt(TimeOfDayFilter.KEY_FILTER_START_MINUTE, 0);
        final int endHour = prefs.getInt(TimeOfDayFilter.KEY_FILTER_END_HOUR, 0);
        final int endMinute = prefs.getInt(TimeOfDayFilter.KEY_FILTER_END_MINUTE, 0);

        if(startHour == 0 && startMinute == 0 && endHour == 0 && endMinute == 0) {
            return;
        }

        timeOfDay.setText(String.format(Locale.getDefault(),
                "%02d:%02d - %02d:%02d",
                startHour, startMinute, endHour, endMinute));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    public void buttonClicked(View v) {
        switch(v.getId()) {
            case R.id.notification_access:
                // show activity for granting notification access
                final Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
                break;
            case R.id.get:
                // request notification listener to read first value from CGM notification
                final Intent i = new Intent(NotificationListener.ACTION_REQUEST);
                i.putExtra(NotificationListener.EXTRA_COMMAND, NotificationListener.COMMAND_GET);
                sendBroadcast(i);
                break;
            case R.id.setTimeOfDay:
                // set start/end time values for when notifications should be deactivated
                new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        final int startHourPicked = hourOfDay;
                        final int startMinutePicked = minute;
                        new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                prefs.edit()
                                        .putInt(TimeOfDayFilter.KEY_FILTER_START_HOUR, startHourPicked)
                                        .putInt(TimeOfDayFilter.KEY_FILTER_START_MINUTE, startMinutePicked)
                                        .putInt(TimeOfDayFilter.KEY_FILTER_END_HOUR, hourOfDay)
                                        .putInt(TimeOfDayFilter.KEY_FILTER_END_MINUTE, minute)
                                        .apply();
                                loadPrefs();
                            }
                        }, 0, 0, true).show();
                    }
                }, 0, 0, true).show();
                break;
        }
    }
}