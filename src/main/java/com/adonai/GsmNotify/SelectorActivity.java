package com.adonai.GsmNotify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.adonai.GsmNotify.database.DbProvider;
import com.adonai.GsmNotify.database.PersistManager;
import com.adonai.GsmNotify.entities.HistoryEntry;
import com.adonai.GsmNotify.misc.AbstractAsyncLoader;
import com.adonai.GsmNotify.misc.DeliveryConfirmReceiver;
import com.adonai.GsmNotify.misc.SentConfirmReceiver;
import com.adonai.contrib.ButtonWithRedTriangle;
import com.adonai.views.ColumnLinearLayout;
import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.LinearLayout.LayoutParams;
import static com.adonai.GsmNotify.Utils.DELIVERED;
import static com.adonai.GsmNotify.Utils.DeviceStatus;
import static com.adonai.GsmNotify.Utils.SENT;
import static com.adonai.GsmNotify.Utils.SMS_ROUNDTRIP_TIMEOUT;
import static com.adonai.GsmNotify.Utils.getStatusBySms;
import static com.adonai.GsmNotify.Utils.isTablet;

@SuppressLint("CommitPrefEdits")
public class SelectorActivity extends Activity implements View.OnClickListener {
    private SharedPreferences mPrefs;

    public final static int STATUS_LOADER = 0;
    private StatusRetrieverCallback mLocalArchiveParseCallback = new StatusRetrieverCallback();

    private String[] mDeviceIds;
    Map<String, Device.CommonSettings> mDeviceSettingsMap;
    private ViewGroup mMainLayout;

    static boolean isRunning;
    static boolean isStatusChecking;

    private BroadcastReceiver sentReceiver, deliveryReceiver;
    private Handler mUiHandler;
    private Handler.Callback mStatusWalkCallback = new StatusWalkCallback();

    private static final int HANDLE_START         = 0;
    private static final int HANDLE_SEND          = 1;
    private static final int HANDLE_ACK           = 2;
    private static final int HANDLE_TIMEOUT       = 3;
    private static final int HANDLE_TERMINATE     = 4;
    private static final int HANDLE_FINISH        = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences(SMSReceiveService.PREFERENCES, MODE_PRIVATE);

        sentReceiver = new SentConfirmReceiver(this);
        deliveryReceiver = new DeliveryConfirmReceiver(this);

        getLoaderManager().initLoader(STATUS_LOADER, null, mLocalArchiveParseCallback);

        mUiHandler = new Handler(mStatusWalkCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        obtainAndSortDevices();

        if(isTablet(this)) {
            prepareTabletUI();
        } else {
            preparePhoneUI();
        }

        //--- When the SMS has been sent ---
        registerReceiver(sentReceiver, new IntentFilter(SENT));
        //--- When the SMS has been delivered. ---
        registerReceiver(deliveryReceiver, new IntentFilter(DELIVERED));

        isRunning = true;
    }

    private void obtainAndSortDevices() {
        long a = System.currentTimeMillis();
        mDeviceIds = mPrefs.getString("IDs", "").split(";");
        mDeviceSettingsMap = new HashMap<>(mDeviceIds.length);
        for(String devId : mDeviceIds) {
            String gson = mPrefs.getString(devId, "");
            Device.CommonSettings details = new Gson().fromJson(gson, Device.CommonSettings.class);
            mDeviceSettingsMap.put(devId, details);
        }

        Arrays.sort(mDeviceIds, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                Device.CommonSettings detailsLeft = mDeviceSettingsMap.get(lhs);
                Device.CommonSettings detailsRight = mDeviceSettingsMap.get(rhs);

                return  detailsLeft.name.compareTo(detailsRight.name);
            }
        });
        Log.e("timetrace", String.valueOf(System.currentTimeMillis() - a));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliveryReceiver);

        isRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // stop querying if we're exiting
        mUiHandler.removeMessages(HANDLE_TIMEOUT);
        mUiHandler.sendEmptyMessage(HANDLE_FINISH);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("number")) { // запущено из сервиса SMS
            getLoaderManager().getLoader(STATUS_LOADER).onContentChanged();
            if(isStatusChecking) {
                mUiHandler.sendMessage(mUiHandler.obtainMessage(HANDLE_ACK, intent.getStringExtra("number")));
            }
        }
    }

    private void prepareTabletUI() {
        ScrollView scrollView = new ScrollView(this);
        mMainLayout = new ColumnLinearLayout(this);
        scrollView.addView(mMainLayout);
        setContentView(scrollView);

        for (String devId : mDeviceIds) {
            Device.CommonSettings details = mDeviceSettingsMap.get(devId);
            ButtonWithRedTriangle openDevice = new ButtonWithRedTriangle(this);
            openDevice.setWidth(LayoutParams.MATCH_PARENT);
            openDevice.setText(details.name);
            openDevice.setUpperLeft(details.workOngoing);
            openDevice.setLowerRight(details.isGsmQaud);
            openDevice.setTag(devId);
            openDevice.setTag(R.integer.device_details, details);
            openDevice.setMaxLines(1);
            openDevice.setEllipsize(TextUtils.TruncateAt.END);
            openDevice.setOnClickListener(this);
            mMainLayout.addView(openDevice);
        }
    }

    private void preparePhoneUI() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout deviceList = new LinearLayout(this);
        deviceList.setOrientation(LinearLayout.VERTICAL);
        mMainLayout = deviceList;


        for (String ID : mDeviceIds) {
            Device.CommonSettings details = mDeviceSettingsMap.get(ID);
            ButtonWithRedTriangle viewer = new ButtonWithRedTriangle(this);
            viewer.setWidth(LayoutParams.MATCH_PARENT);
            viewer.setText(details.name);
            viewer.setUpperLeft(details.workOngoing);
            viewer.setLowerRight(details.isGsmQaud);
            viewer.setTag(ID);
            viewer.setTag(R.integer.device_details, details);
            viewer.setOnClickListener(this);
            deviceList.addView(viewer);
        }
        scrollView.addView(deviceList);

        setContentView(scrollView);
    }

    @Override
    public void onClick(View v) {
        Intent starter = new Intent(this, MainActivity.class).putExtra("ID", v.getTag().toString());
        startActivity(starter);
        //finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.selector_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem smsOption = menu.findItem(R.id.notify_on_sms);
        boolean shouldOpen = mPrefs.getBoolean(SMSReceiveService.OPEN_ON_SMS_KEY, true);
        smsOption.setChecked(shouldOpen);

        MenuItem soundOption = menu.findItem(R.id.notify_with_sound);
        boolean shouldRing = mPrefs.getBoolean(SMSReceiveService.RING_ON_SMS_KEY, false);
        soundOption.setChecked(shouldRing);

        MenuItem soundAlarmOption = menu.findItem(R.id.notify_alarm_with_sound);
        boolean shouldAlarmRing = mPrefs.getBoolean(SMSReceiveService.RING_ON_ALARM_SMS_KEY, false);
        soundAlarmOption.setChecked(shouldAlarmRing);

        MenuItem queryOption = menu.findItem(R.id.query_all_devices);
        queryOption.setVisible(isTablet(this) && !isStatusChecking);

        MenuItem stopQueryOption = menu.findItem(R.id.stop_query_devices);
        stopQueryOption.setVisible(isTablet(this) && isStatusChecking);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().getLoader(STATUS_LOADER).onContentChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notify_on_sms: {
                boolean shouldOpen = mPrefs.getBoolean(SMSReceiveService.OPEN_ON_SMS_KEY, true);
                shouldOpen = !shouldOpen;

                // write to prefs
                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putBoolean(SMSReceiveService.OPEN_ON_SMS_KEY, shouldOpen);
                edit.commit();

                // update menu checked state
                invalidateOptionsMenu();
                return true;
            }
            case R.id.notify_with_sound: {
                boolean shouldRing = mPrefs.getBoolean(SMSReceiveService.RING_ON_SMS_KEY, false);
                shouldRing = !shouldRing;

                // write to prefs
                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putBoolean(SMSReceiveService.RING_ON_SMS_KEY, shouldRing);
                if(shouldRing) {
                    edit.putBoolean(SMSReceiveService.RING_ON_ALARM_SMS_KEY, !shouldRing); // exclusive
                }
                edit.commit();

                // update menu checked state
                invalidateOptionsMenu();
                return true;
            }
            case R.id.notify_alarm_with_sound: {
                boolean shouldRing = mPrefs.getBoolean(SMSReceiveService.RING_ON_ALARM_SMS_KEY, false);
                shouldRing = !shouldRing;

                // write to prefs
                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putBoolean(SMSReceiveService.RING_ON_ALARM_SMS_KEY, shouldRing);
                if(shouldRing) {
                    edit.putBoolean(SMSReceiveService.RING_ON_SMS_KEY, !shouldRing); // exclusive
                }
                edit.commit();

                // update menu checked state
                invalidateOptionsMenu();
                return true;
            }
            case R.id.query_all_devices:
                mUiHandler.removeCallbacksAndMessages(null);
                mUiHandler.sendEmptyMessage(HANDLE_START);
                return true;
            case R.id.stop_query_devices:
                mUiHandler.removeCallbacksAndMessages(null);
                mUiHandler.sendEmptyMessage(HANDLE_TERMINATE);
                return true;
            case R.id.show_alarm_history:
                AlarmHistoryListFragment hlf = AlarmHistoryListFragment.newInstance();
                hlf.show(getFragmentManager(), "AlarmHistoryListDialog");
                return true;
            case R.id.clear_all_history:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.clear_all_history).setMessage(R.string.clear_all_history_confirm);
                builder.setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PersistManager manager = DbProvider.getTempHelper(SelectorActivity.this);
                                try {
                                    manager.getHistoryDao().deleteBuilder().delete();
                                    getLoaderManager().getLoader(STATUS_LOADER).onContentChanged();
                                } catch (SQLException e) {
                                    Toast.makeText(SelectorActivity.this, R.string.db_cant_delete_history, Toast.LENGTH_LONG).show();
                                }
                                DbProvider.releaseTempHelper(); // it's ref-counted thus will not close if activity uses it...
                            }
                        });
                builder.create().show();
                return true;
            case R.id.add_device:
                Intent intent = new Intent(SelectorActivity.this, SettingsActivity.class);
                startActivity(intent);
                //finish();
                return true;
        }

        return false;
    }

    private class StatusRetrieverCallback implements LoaderManager.LoaderCallbacks<List<DeviceStatus>> {

        @Override
        public Loader<List<DeviceStatus>> onCreateLoader(int i, Bundle bundle) {
            return new AbstractAsyncLoader<List<DeviceStatus>>(SelectorActivity.this) {

                @Override
                protected void onForceLoad() {
                    if(mDeviceIds != null) {
                        super.onForceLoad();
                    }
                }

                @NonNull
                @Override
                public List<DeviceStatus> loadInBackground() {
                    List<DeviceStatus> devStatuses = new ArrayList<>(mDeviceIds.length);

                    PersistManager manager = DbProvider.getTempHelper(SelectorActivity.this);
                    for (String deviceId : mDeviceIds) {
                        DeviceStatus currentStatus = DeviceStatus.UNKNOWN;
                        Device.CommonSettings details = mDeviceSettingsMap.get(deviceId);
                        try {
                            List<HistoryEntry> entriesForDevice = manager.getHistoryDao().queryBuilder()
                                    .orderBy("eventDate", false).where().eq("deviceName", details.name).query();
                            for(HistoryEntry he : entriesForDevice) {
                                String lowercaseSms = he.getSmsText().toLowerCase();
                                currentStatus = getStatusBySms(SelectorActivity.this, lowercaseSms);
                                if(currentStatus != DeviceStatus.UNKNOWN) { // stop if we've found status
                                    break;
                                }
                            }
                        } catch (SQLException sqle) {
                            currentStatus = DeviceStatus.UNKNOWN;
                        }
                        devStatuses.add(currentStatus);
                    }
                    DbProvider.releaseTempHelper(); // it's ref-counted thus will not close if activity uses it...
                    return devStatuses;
                }
            };
        }

        @Override
        @SuppressWarnings("deprecation")
        public void onLoadFinished(Loader<List<DeviceStatus>> loader, List<DeviceStatus> statusesRetrieved) {
            int currentButtonIndex = 0;
            for(DeviceStatus status : statusesRetrieved) {
                Button child = (Button) mMainLayout.getChildAt(currentButtonIndex++);

                Drawable newBackground = child.getBackground().mutate();
                switch (status) {
                    case ARMED:
                        newBackground.setColorFilter(getResources().getColor(R.color.dark_yellow), PorterDuff.Mode.ADD);
                        break;
                    case DISARMED:
                        newBackground.setColorFilter(getResources().getColor(R.color.dark_green), PorterDuff.Mode.ADD);
                        break;
                    case ALARM:
                        newBackground.setColorFilter(getResources().getColor(R.color.dark_red), PorterDuff.Mode.ADD);
                        break;
                    case UNKNOWN:
                        // leave the same
                        newBackground.clearColorFilter();
                        break;
                }
                child.setBackgroundDrawable(newBackground);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<DeviceStatus>> loader) {

        }
    }

    private class StatusWalkCallback implements Handler.Callback {
        private int currentQueried;

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_START:
                    currentQueried = 0;
                    mUiHandler.sendEmptyMessage(HANDLE_SEND);
                    isStatusChecking = true;
                    invalidateOptionsMenu();
                    return true;
                case HANDLE_SEND:
                    Button deviceOpenButton = (Button) mMainLayout.getChildAt(currentQueried);
                    Device.CommonSettings details = (Device.CommonSettings) deviceOpenButton.getTag(R.integer.device_details);
                    deviceOpenButton.setText("→ " + deviceOpenButton.getText() + " ←");
                    sendStatusQuerySms(details);
                    mUiHandler.sendEmptyMessageDelayed(HANDLE_TIMEOUT, SMS_ROUNDTRIP_TIMEOUT);
                    return true;
                case HANDLE_ACK:
                    String number = (String) msg.obj;
                    if(number.equals(mDeviceIds[currentQueried])) { // it's current queried device's status message!
                        mUiHandler.removeMessages(HANDLE_TIMEOUT);
                        continueQueryIfNeeded();
                    }
                    return true;
                case HANDLE_TIMEOUT: // device didn't answer
                    Toast.makeText(SelectorActivity.this, R.string.sms_wait_timeout, Toast.LENGTH_LONG).show();
                    continueQueryIfNeeded();
                    return true;
                case HANDLE_TERMINATE:
                    restoreOldName();
                    /* fall through */
                case HANDLE_FINISH:
                    isStatusChecking = false;
                    invalidateOptionsMenu();
                    return true;
            }
            return false;
        }

        private void continueQueryIfNeeded() {
            restoreOldName();
            if(mDeviceIds.length > ++currentQueried) { // query next
                mUiHandler.sendEmptyMessage(HANDLE_SEND);
            } else { // finish
                mUiHandler.sendEmptyMessage(HANDLE_FINISH);
            }
        }

        private void restoreOldName() {
            Button deviceOpenButton = (Button) mMainLayout.getChildAt(currentQueried);
            Device.CommonSettings details = (Device.CommonSettings) deviceOpenButton.getTag(R.integer.device_details);
            deviceOpenButton.setText(details.name);
        }
    }

    public void sendStatusQuerySms(Device.CommonSettings details) {
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(details.number, null, "*" + details.password + "#_info#", sentPI, deliveredPI);
    }
}
