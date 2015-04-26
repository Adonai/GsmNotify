package com.adonai.GsmNotify;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

import com.adonai.GsmNotify.Utils.DeviceStatus;
import com.adonai.GsmNotify.database.DbProvider;
import com.adonai.GsmNotify.database.PersistManager;
import com.adonai.GsmNotify.entities.HistoryEntry;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMSReceiveService extends Service implements Handler.Callback {
    final public static String PREFERENCES = "devicePrefs";

    final public static String OPEN_ON_SMS_KEY = "open.on.sms";
    final public static String RING_ON_SMS_KEY = "ring.on.sms";
    final public static String RING_ON_ALARM_SMS_KEY = "ring.on.alarm.sms";

    final private static int TICK_RING = 0;

    Activity boundListener;
    SharedPreferences preferences;
    BroadcastReceiver mScreenStateReceiver;
    ToneGenerator mToneGenerator;
    Bitmap largeNotifIcon;

    Handler mHandler;
    
    Map<String, String> contactsCache = new HashMap<>();
    boolean cacheFilled = false;

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

        mHandler = new Handler(this);
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
            String smsNumber = intent.getStringExtra("number");
            String smsText = intent.getStringExtra("text");

            boolean shouldPlaySound = preferences.getBoolean(RING_ON_SMS_KEY, false);
            boolean shouldPlayAlarmSound = preferences.getBoolean(RING_ON_ALARM_SMS_KEY, false);
            boolean shouldOpen = preferences.getBoolean(OPEN_ON_SMS_KEY, true);

            String[] IDs = preferences.getString("IDs", "").split(";");
            for (String deviceNumber : IDs) {
                if (deviceNumber.length() > 1 && smsNumber.endsWith(deviceNumber.substring(1))) { // +7 / 8 handling
                    // it's one of our devices

                    String gson = preferences.getString(deviceNumber, "");
                    Device.CommonSettings settings = new Gson().fromJson(gson, Device.CommonSettings.class);
                    addHistoryEntry(smsText, settings);

                    // stop searching if we're editing it now
                    if(SettingsActivity.isRunning) {
                        break;
                    }

                    // change cell color
                    if(SelectorActivity.isRunning) {
                        Intent starter = new Intent(this, SelectorActivity.class);
                        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra("number", deviceNumber)
                                .putExtra("text", smsText);
                        startActivity(starter);

                        // this is a status checker, don't notify anyone else
                        if(SelectorActivity.isStatusChecking) {
                            break;
                        }
                    }

                    // if we're here, we're not checking status

                    // main window characteristics
                    boolean isSameNow = TextUtils.equals(MainActivity.deviceNumber, deviceNumber);
                    boolean isOpen = MainActivity.isRunning;

                    // not a status but an actual answer, play sound if we should!
                    if(!smsText.contains(getString(R.string.status_matcher))) {
                        DeviceStatus currentStatus = Utils.getStatusBySms(this, smsText.toLowerCase());
                        // play sound if we opted either all sounds or we have alarm only and status resolves to alarm
                        if((shouldPlaySound || (shouldPlayAlarmSound && currentStatus == DeviceStatus.ALARM)) && 
                                !(isOpen && isSameNow && currentStatus == DeviceStatus.ALARM)) // don't play long alarm if we're now viewing same device
                        { 
                            playSound(currentStatus);
                        }
                    }

                    // should send to activity now?
                    // if we're viewing another device, or shouldn't open at all
                    if (isOpen && !isSameNow || !isOpen && !shouldOpen) {

                        /*
                        // just make a notification
                        Notification.Builder builder = new Notification.Builder(this);
                        builder.setSmallIcon(R.drawable.app_icon);
                        builder.setLargeIcon(largeNotifIcon);
                        builder.setAutoCancel(true);
                        builder.setContentTitle(getString(R.string.warning));
                        builder.setContentText(settings.name + ": " + smsText);

                        final Intent notificationClicker = new Intent(this, MainActivity.class);
                        notificationClicker.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra("number", deviceNumber)
                                .putExtra("text", smsText);
                        builder.setContentIntent(PendingIntent.getActivity(this, 0, notificationClicker, 0));

                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(100501, builder.getNotification());
                        */

                        break;
                    }

                    // if we're here, we're not checking status, not editing device and not in main window

                    // open activity
                    Intent starter = new Intent(this, MainActivity.class);
                    starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("number", deviceNumber)
                            .putExtra("text", smsText);
                    startActivity(starter);
                }
            }
        }

        // stop alarm tone if playing one
        if(intent != null && intent.hasExtra("stop_alarm")) {
            mHandler.removeMessages(TICK_RING);
            mToneGenerator.stopTone();
        }

        return START_STICKY;
    }

    private void retrieveContacts() {
        if (cacheFilled) {
            return;
        }
        
        Cursor phones = getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(Phone.NUMBER));
            contactsCache.put(phoneNumber, name);
        }
        phones.close();
        cacheFilled = true;
    }
    
    private void addHistoryEntry(String text, Device.CommonSettings deviceDetails) {
        // fill the cache if we hadn't done so
        retrieveContacts();
        
        // lookup number
        String effectiveText = text;
        if(contactsCache.containsKey(deviceDetails.name)) {
            // replace number with name
            effectiveText = text.replace(deviceDetails.name, contactsCache.get(deviceDetails.name));
        }
        
        // add to history
        PersistManager manager = DbProvider.getTempHelper(this);
        HistoryEntry he = new HistoryEntry();
        he.setDeviceName(deviceDetails.name);
        he.setEventDate(Calendar.getInstance().getTime());
        he.setSmsText(effectiveText);
        manager.getHistoryDao().create(he);
        DbProvider.releaseTempHelper(); // it's ref-counted thus will not close if activity uses it...
    }

    private void playSound(DeviceStatus status) {
        switch (status) {
            case ARMED:
                mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER, 250);
                return;
            case DISARMED:
                mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER, 1000);
                return;
            case ALARM:
                mHandler.sendEmptyMessage(TICK_RING);
                return;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        // don't switch, it's simple
        // loop alarm sound
        mToneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 10000);
        mHandler.sendEmptyMessageDelayed(TICK_RING, 1000);
        return true;
    }
}
