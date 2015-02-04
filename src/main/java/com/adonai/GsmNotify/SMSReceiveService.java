package com.adonai.GsmNotify;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import com.adonai.GsmNotify.database.DbProvider;
import com.adonai.GsmNotify.database.PersistManager;
import com.adonai.GsmNotify.entities.HistoryEntry;
import com.google.gson.Gson;

import java.util.Calendar;

public class SMSReceiveService extends Service {
    final public static String PREFERENCES = "devicePrefs";

    final public static String OPEN_ON_SMS_KEY = "open.on.sms";

    Activity boundListener;
    SharedPreferences preferences;
    Bitmap largeNotifIcon;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        boundListener = null;
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        DbProvider.setHelper(this);

        Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon);
        Resources res = getResources();
        int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
        largeNotifIcon = Bitmap.createScaledBitmap(appIcon, width, height, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DbProvider.releaseHelper();
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("number")) {
            String[] IDs = preferences.getString("IDs", "").split(";");
            String current = preferences.getString("currentEdit", "");
            boolean shouldNotify = preferences.getBoolean(OPEN_ON_SMS_KEY, true);
            for (String deviceId : IDs) {
                if (deviceId.length() > 1 && intent.getStringExtra("number").endsWith(deviceId.substring(1)) && !deviceId.equals(current)) { // +7 / 8 handling
                    String text = intent.getStringExtra("text");

                    // check notify on sms is set
                    String gson = preferences.getString(deviceId, "");
                    if (!gson.equals("")) {
                        Device.CommonSettings settings = new Gson().fromJson(gson, Device.CommonSettings.class);

                        // add to history
                        PersistManager manager = DbProvider.getTempHelper(this);
                        HistoryEntry he = new HistoryEntry();
                        he.setDeviceName(settings.name);
                        he.setEventDate(Calendar.getInstance().getTime());
                        he.setSmsText(text);
                        manager.getHistoryDao().create(he);
                        DbProvider.releaseTempHelper(); // it's ref-counted thus will not close if activity uses it...

                        // should send to activity now?
                        if (!MainActivity.isRunning && !shouldNotify) { // if we have it and it's false

                            // just make a notification
                            Notification.Builder builder = new Notification.Builder(this);
                            builder.setSmallIcon(R.drawable.app_icon);
                            builder.setLargeIcon(largeNotifIcon);
                            builder.setAutoCancel(true);
                            builder.setContentTitle(getString(R.string.warning));
                            builder.setContentText(settings.name + ": " + text);

                            final Intent notificationClicker = new Intent(this, MainActivity.class);
                            notificationClicker.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .putExtra("number", deviceId)
                                    .putExtra("text", text);
                            builder.setContentIntent(PendingIntent.getActivity(this, 0, notificationClicker, 0));

                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(100501, builder.getNotification());
                            break;
                        }
                    }

                    Intent starter = new Intent(this, MainActivity.class);
                    starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("number", deviceId)
                            .putExtra("text", text);
                    startActivity(starter);
                }
            }
        }
        return START_STICKY;
    }
}
