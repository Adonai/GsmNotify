package com.adonai.GsmNotify;

import android.app.Activity;
import android.util.DisplayMetrics;

import java.util.Collection;
import java.util.Iterator;

public class Utils {

    final static String SENT = "SMS_SENT_NOTIFY_MAIN";
    final static String DELIVERED = "SMS_DELIVERED_NOTIFY_MAIN";

    final static int SMS_DEFAULT_TIMEOUT = 20000;

    static String join(Collection<String> s, String delimiter) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = s.iterator();
        while (iter.hasNext()) {
            String current = iter.next();
            if (current.isEmpty()) {
                continue;
            }

            builder.append(current);

            if (!iter.hasNext()) {
                break;
            }
            builder.append(delimiter);
        }
        return builder.toString();
    }

    public static boolean isTablet(Activity context) {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;

        float widthDp = widthPixels / metrics.density;
        float heightDp = heightPixels / metrics.density;
        float smallestWidth = Math.min(widthDp, heightDp);

        return  smallestWidth > 600;
    }
}
