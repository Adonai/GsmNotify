package com.adonai.GsmNotify;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.IBinder;
import android.text.TextUtils;

import com.adonai.GsmNotify.database.DbProvider;
import com.adonai.GsmNotify.database.PersistManager;
import com.adonai.GsmNotify.entities.HistoryEntry;
import com.google.gson.Gson;

import java.util.Calendar;

public class SMSReceiveService extends Service {
    final public static String PREFERENCES = "devicePrefs";

    final public static String OPEN_ON_SMS_KEY = "open.on.sms";
    final public static String RING_ON_SMS_KEY = "ring.on.sms";

    Activity boundListener;
    SharedPreferences preferences;
    BroadcastReceiver mScreenStateReceiver;
    ToneGenerator mToneGenerator;
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
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon);
        Resources res = getResources();
        int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
        largeNotifIcon = Bitmap.createScaledBitmap(appIcon, width, height, false);

        mScreenStateReceiver = new ScreenOnReceiver();
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStateReceiver, screenStateFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mToneGenerator.release();
        unregisterReceiver(mScreenStateReceiver);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("number")) {
            String[] IDs = preferences.getString("IDs", "").split(";");
            String current = preferences.getString("currentEdit", "");
            boolean shouldPlaySound = preferences.getBoolean(RING_ON_SMS_KEY, false);
            boolean shouldOpen = preferences.getBoolean(OPEN_ON_SMS_KEY, true);
            for (String deviceId : IDs) {
                if (deviceId.length() > 1 && intent.getStringExtra("number").endsWith(deviceId.substring(1))) { // +7 / 8 handling

                    // stop searching if we know we're editing it now
                    if(deviceId.equals(current)) {
                        break;
                    }

                    String text = intent.getStringExtra("text");
                    String gson = preferences.getString(deviceId, "");
                    Device.CommonSettings settings = new Gson().fromJson(gson, Device.CommonSettings.class);
                    addHistoryEntry(text, settings);

                    // this is a status checker, don't notify anyone else
                    if(SelectorActivity.isRunning && SelectorActivity.isStatusChecking) {
                        Intent starter = new Intent(this, SelectorActivity.class);
                        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra("number", deviceId)
                                .putExtra("text", text);
                        startActivity(starter);
                        break;
                    }

                    // if we're here, we're not checking status

                    // main window characteristics
                    boolean isSameNow = TextUtils.equals(MainActivity.deviceNumber, deviceId);
                    boolean isOpen = MainActivity.isRunning;

                    // not a status but an actual answer, play sound!
                    if(shouldPlaySound && !text.contains(getString(R.string.status_matcher))) {
                        Utils.DeviceStatus currentStatus = Utils.getStatusBySms(this, text.toLowerCase());
                        // don't play alarm if we're now viewing same device
                        //if(!(isOpen && isSameNow && currentStatus == Utils.DeviceStatus.ALARM)) {
                            playSound(currentStatus);
                        //}
                    }

                    // should send to activity now?
                    // if we're viewing another device, or shouldn't open at all
                    if (isOpen && !isSameNow || !shouldOpen) {

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

                    // if we're here, we're not checking status, not editing device and not in main window

                    // open activity
                    Intent starter = new Intent(this, MainActivity.class);
                    starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("number", deviceId)
                            .putExtra("text", text);
                    startActivity(starter);
                }
            }
        }

        // stop alarm tone if playing one
        if(intent != null && intent.hasExtra("stop_alarm")) {
            mToneGenerator.stopTone();
        }

        return START_STICKY;
    }

    private void addHistoryEntry(String text, Device.CommonSettings deviceDetails) {
        // add to history
        PersistManager manager = DbProvider.getTempHelper(this);
        HistoryEntry he = new HistoryEntry();
        he.setDeviceName(deviceDetails.name);
        he.setEventDate(Calendar.getInstance().getTime());
        he.setSmsText(text);
        manager.getHistoryDao().create(he);
        DbProvider.releaseTempHelper(); // it's ref-counted thus will not close if activity uses it...
    }

    private void playSound(Utils.DeviceStatus status) {
        switch (status) {
            case ARMED:
                mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER, 250);
                return;
            case DISARMED:
                mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER, 1000);
                return;
            case ALARM:
                mToneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 10000);
                return;
        }
    }
}
