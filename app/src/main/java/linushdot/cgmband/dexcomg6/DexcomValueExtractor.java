package linushdot.cgmband.dexcomg6;

import android.app.Notification;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.widget.RemoteViews;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import linushdot.cgmband.CgmValue;
import linushdot.cgmband.CgmValueExtractor;

/***
 * This extractor supports Dexcom G6 notifications.
 *
 * Currently it only matches one packages, needs to be expanded.
 * Also the value extraction through reflection was only tested on android 9 and could be unreliable
 * on other android versions and versions of the Dexcom app.
 */
public class DexcomValueExtractor implements CgmValueExtractor {

    // Only consider notifications from the following packages, needs expansion
    private final List<String> packageFilter =
            Collections.singletonList("com.dexcom.g6.region1.mmol");

    @Override
    public boolean match(StatusBarNotification sbn) {
        return (packageFilter.contains(sbn.getPackageName()) &&
                (sbn.getNotification().flags & Notification.FLAG_ONGOING_EVENT) != 0);
    }

    @Override
    public CgmValue extract(StatusBarNotification sbn) {

        final String str = getStringFromNotification(sbn.getNotification());
        if(str == null) {
            return null;
        }

        Matcher matcher = Pattern.compile("([0-9.]+) ([A-z/]+)").matcher(str);
        if(!matcher.matches()) {
            return null;
        }

        try {
            final float value = Float.parseFloat(matcher.group(1));
            final String unit = matcher.group(2);
            final long time = sbn.getPostTime();

            return new CgmValue(value, unit, time);

        } catch(NumberFormatException e) {
            return null;
        }
    }

    /**
     * Extracts CGM value from Dexcom glance notification through reflection
     *
     * Tested only on Android 9 (API level 28) !
     *
     * It concatenates the values of all setText actions found in the content view of the
     * notification separated by spaces.
     *
     * Inspired by code from:
     * https://stackoverflow.com/questions/9293617/retrieve-text-from-a-remoteviews-object
     *
     * @param notification Notification containing the CGM value
     * @return Concatenated strings from notification, should have the format "value unit"
     */
    private String getStringFromNotification(Notification notification) {
        final StringBuilder sb = new StringBuilder();

        RemoteViews views = notification.bigContentView;
        if (views == null) views = notification.contentView;
        if (views == null) return null;

        try {
            Field field = views.getClass().getDeclaredField("mActions");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);

            for (Parcelable p : actions)
            {
                Parcel parcel = Parcel.obtain();
                p.writeToParcel(parcel, 0);
                parcel.setDataPosition(0);

                // View ID, ignore
                parcel.readInt();

                String methodName = parcel.readString();
                if (methodName == null) continue;

                // Save strings
                else if (methodName.equals("setText"))
                {
                    // Parameter type (10 = Character Sequence)
                    parcel.readInt();

                    // Store the actual string
                    String t = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString().trim();
                    if(sb.length() > 0) {
                        sb.append(" ");
                    }
                    sb.append(t);
                }

                parcel.recycle();
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        return sb.toString().trim();
    }
}
