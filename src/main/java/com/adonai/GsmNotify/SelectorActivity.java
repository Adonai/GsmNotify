package com.adonai.GsmNotify;

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
import com.adonai.views.ColumnLinearLayout;
import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static android.widget.LinearLayout.LayoutParams;

public class SelectorActivity extends Activity implements View.OnClickListener {
    SharedPreferences mPrefs;

    public final static int STATUS_LOADER = 0;
    private StatusRetrieverCallback mLocalArchiveParseCallback = new StatusRetrieverCallback();

    private String[] mDeviceIds;
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
    private static final int HANDLE_FINISH        = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences(SMSReceiveService.PREFERENCES, MODE_PRIVATE);

        sentReceiver = new SentConfirmReceiver(this);
        deliveryReceiver = new DeliveryConfirmReceiver(this);

        if(Utils.isTablet(this)) {
            getLoaderManager().initLoader(STATUS_LOADER, null, mLocalArchiveParseCallback);
        }

        mUiHandler = new Handler(mStatusWalkCallback);
    }

    enum DeviceStatus {
        ARMED,
        DISARMED,
        ALARM,
        UNKNOWN
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDeviceIds = mPrefs.getString("IDs", "").split(";");
        //Arrays.sort(mDeviceIds);

        if(Utils.isTablet(this)) {
            prepareTabletUI();
        } else {
            preparePhoneUI();
        }

        //--- When the SMS has been sent ---
        registerReceiver(sentReceiver, new IntentFilter(Utils.SENT));
        //--- When the SMS has been delivered. ---
        registerReceiver(deliveryReceiver, new IntentFilter(Utils.DELIVERED));

        isRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("number")) { // запущено из сервиса SMS
            String message = intent.getStringExtra("text");
            if(message.toLowerCase().contains(getString(R.string.status_matcher))) { // it's a status-message
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
            String gson = mPrefs.getString(devId, "");

            if (gson.isEmpty()) {
                continue;
            }

            Device.CommonSettings details = new Gson().fromJson(gson, Device.CommonSettings.class);
            Button openDevice = new Button(this);
            openDevice.setWidth(LayoutParams.MATCH_PARENT);
            openDevice.setText(details.name);
            openDevice.setTag(devId);
            openDevice.setTag(R.integer.device_details, details);
            openDevice.setMaxLines(1);
            openDevice.setEllipsize(TextUtils.TruncateAt.END);
            openDevice.setOnClickListener(this);
            mMainLayout.addView(openDevice);
        }
        getLoaderManager().getLoader(STATUS_LOADER).onContentChanged();
    }

    private void preparePhoneUI() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout deviceList = new LinearLayout(this);
        deviceList.setOrientation(LinearLayout.VERTICAL);


        for (String ID : mDeviceIds) {
            String gson = mPrefs.getString(ID, "");
            if (gson.isEmpty()) {
                continue;
            }

            Device.CommonSettings details = new Gson().fromJson(gson, Device.CommonSettings.class);
            Button viewer = new Button(this);
            viewer.setWidth(LayoutParams.MATCH_PARENT);
            viewer.setText(details.name);
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

        MenuItem queryOption = menu.findItem(R.id.query_all_devices);
        queryOption.setVisible(Utils.isTablet(this));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Utils.isTablet(SelectorActivity.this)) {
            getLoaderManager().getLoader(STATUS_LOADER).onContentChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notify_on_sms:
                boolean shouldOpen = mPrefs.getBoolean(SMSReceiveService.OPEN_ON_SMS_KEY, true);
                shouldOpen = !shouldOpen;

                // write to prefs
                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putBoolean(SMSReceiveService.OPEN_ON_SMS_KEY, shouldOpen);
                edit.commit();

                // update menu checked state
                invalidateOptionsMenu();
                return true;
            case R.id.query_all_devices:
                mUiHandler.removeCallbacksAndMessages(null);
                mUiHandler.sendEmptyMessage(HANDLE_START);
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
                                    if(Utils.isTablet(SelectorActivity.this)) {
                                        getLoaderManager().getLoader(STATUS_LOADER).onContentChanged();
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
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

                        String gson = mPrefs.getString(deviceId, "");
                        if (gson.isEmpty()) {
                            devStatuses.add(currentStatus);
                            continue;
                        }

                        Device.CommonSettings details = new Gson().fromJson(gson, Device.CommonSettings.class);
                        try {
                            HistoryEntry he = manager.getHistoryDao().queryBuilder()
                                    .orderBy("eventDate", false).where().eq("deviceName", details.name).queryForFirst();
                            if(he != null) {
                                String lowercaseSms = he.getSmsText().toLowerCase();
                                if (lowercaseSms.contains(getString(R.string.armed_matcher))) { // armed
                                    currentStatus = DeviceStatus.ARMED;
                                } else if (lowercaseSms.contains(getString(R.string.disarmed_matcher))) { // disarmed
                                    currentStatus = DeviceStatus.DISARMED;
                                } else if (lowercaseSms.contains(getString(R.string.alarm_matcher))) {
                                    currentStatus = DeviceStatus.ALARM;
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
            int childCount = mMainLayout.getChildCount();
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
                    return true;
                case HANDLE_SEND: {
                    Button deviceOpenButton = (Button) mMainLayout.getChildAt(currentQueried);
                    Device.CommonSettings details = (Device.CommonSettings) deviceOpenButton.getTag(R.integer.device_details);
                    deviceOpenButton.setText("→ " + deviceOpenButton.getText() + " ←");
                    sendStatusQuerySms(details);
                    mUiHandler.sendEmptyMessageDelayed(HANDLE_TIMEOUT, Utils.SMS_ROUNDTRIP_TIMEOUT);
                    return true;
                }
                case HANDLE_ACK: {
                    String number = (String) msg.obj;
                    if(number.equals(mDeviceIds[currentQueried])) { // it's current queried device's status message!
                        mUiHandler.removeMessages(HANDLE_TIMEOUT);
                        getLoaderManager().getLoader(STATUS_LOADER).onContentChanged();
                        continueQueryIfNeeded();
                    }
                    return true;
                }
                case HANDLE_TIMEOUT: { // device didn't answer
                    Toast.makeText(SelectorActivity.this, R.string.sms_wait_timeout, Toast.LENGTH_LONG).show();
                    continueQueryIfNeeded();
                    return true;
                }
                case HANDLE_FINISH: {
                    isStatusChecking = false;
                    return true;
                }
            }
            return false;
        }

        private void continueQueryIfNeeded() {
            // restore old name
            Button deviceOpenButton = (Button) mMainLayout.getChildAt(currentQueried);
            Device.CommonSettings details = (Device.CommonSettings) deviceOpenButton.getTag(R.integer.device_details);
            deviceOpenButton.setText(details.name);

            if(mDeviceIds.length > ++currentQueried) { // query next
                mUiHandler.sendEmptyMessage(HANDLE_SEND);
            } else { // finish
                mUiHandler.sendEmptyMessage(HANDLE_FINISH);
            }
        }
    }

    public void sendStatusQuerySms(Device.CommonSettings details) {
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(Utils.SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(Utils.DELIVERED), 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(details.number, null, "*" + details.password + "#_info#", sentPI, deliveredPI);
    }
}
