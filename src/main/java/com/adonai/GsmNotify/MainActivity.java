package com.adonai.GsmNotify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.adonai.GsmNotify.database.DbProvider;
import com.adonai.GsmNotify.entities.HistoryEntry;
import com.adonai.GsmNotify.misc.DeliveryConfirmReceiver;
import com.adonai.GsmNotify.misc.SentConfirmReceiver;
import com.google.gson.Gson;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressLint("CommitPrefEdits")
public class MainActivity extends Activity implements View.OnClickListener {
    MessageQueue incMessages;

    String mAddressID;
    SharedPreferences mPrefs;
    BroadcastReceiver sentReceiver, deliveryReceiver;
    Device mDevice;

    //ScrollView mScroll;
    Button mNotifyEnable, mNotifyDisable, mRelay1Enable, mRelay1Disable, mRelay2Enable, mRelay2Disable;
    Button mGetData, mGetTemperature;
    EditText mResultText;

    static boolean isRunning;

    // увеличиваем длительность нажатия до 500 мс
    private View.OnTouchListener pressHolder = new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    v.setPressed(true);
                    onClick(v);
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v.setPressed(false);
                        }
                    }, 500);
                    break;

            }
            return true;
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DbProvider.setHelper(this);
        setContentView(R.layout.main);

        //mScroll = (ScrollView) findViewById(R.id.scroll_bar);

        incMessages = new MessageQueue();
        sentReceiver = new SentConfirmReceiver(this);
        deliveryReceiver = new DeliveryConfirmReceiver(this);
        mPrefs = getSharedPreferences(SMSReceiveService.PREFERENCES, MODE_PRIVATE);

        mNotifyEnable = (Button) findViewById(R.id.signal_on_button);
        mNotifyDisable = (Button) findViewById(R.id.signal_off_button);
        mRelay1Enable = (Button) findViewById(R.id.relay1_on_button);
        mRelay1Disable = (Button) findViewById(R.id.relay1_off_button);
        mRelay2Enable = (Button) findViewById(R.id.relay2_on_button);
        mRelay2Disable = (Button) findViewById(R.id.relay2_off_button);
        mGetData = (Button) findViewById(R.id.get_data_button);
        mGetTemperature = (Button) findViewById(R.id.get_temperature_button);

        mNotifyEnable.setOnTouchListener(pressHolder);
        mNotifyDisable.setOnTouchListener(pressHolder);
        mRelay1Enable.setOnTouchListener(pressHolder);
        mRelay1Disable.setOnTouchListener(pressHolder);
        mRelay2Enable.setOnTouchListener(pressHolder);
        mRelay2Disable.setOnTouchListener(pressHolder);
        mGetData.setOnTouchListener(pressHolder);
        mGetTemperature.setOnTouchListener(pressHolder);

        mResultText = (EditText) findViewById(R.id.result_text);


        if (getIntent().hasExtra("ID")) // запускаем из настроек
        {
            mAddressID = getIntent().getStringExtra("ID");
            extractParams();
        } else if (getIntent().hasExtra("number")) // запускаем из ресивера
        {
            mAddressID = getIntent().getStringExtra("number");
            extractParams();

            mResultText.setText(getIntent().getStringExtra("text"));
        } else // запускаем сами
        {
            String[] IDs = mPrefs.getString("IDs", "").split(";");
            if (IDs.length > 1) {
                Intent selector = new Intent(this, SelectorActivity.class);
                startActivity(selector);
                finish();
            } else if (IDs[0].length() != 0) {
                mAddressID = IDs[0];
                extractParams();
            } else // first launch
            {
                Toast.makeText(getApplicationContext(), R.string.first_launch, Toast.LENGTH_LONG).show();
                Intent firstLaunch = new Intent(this, SettingsActivity.class);
                startActivity(firstLaunch);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DbProvider.releaseHelper();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //--- When the SMS has been sent ---
        registerReceiver(sentReceiver, new IntentFilter(Utils.SENT));
        //--- When the SMS has been delivered. ---
        registerReceiver(deliveryReceiver, new IntentFilter(Utils.DELIVERED));

        isRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliveryReceiver);

        isRunning = false;
    }

    private void extractParams() {
        String gson = mPrefs.getString(mAddressID, "");
        if (!gson.equals("")) {
            mDevice = new Device();
            mDevice.details = new Gson().fromJson(gson, Device.CommonSettings.class);
            setTitle(mDevice.details.name);

            invalidateOptionsMenu();
        } else {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //setIntent(intent);

        if (intent.hasExtra("number")) { // запущено из сервиса SMS
            String newMessage = intent.getStringExtra("text");
            if (intent.getStringExtra("number").equals(mAddressID)) {
                incMessages.add(newMessage);
                mResultText.setTextKeepState(incMessages.toString());
                //mScroll.fling(10000);
            } else {
                incMessages.clear();
                incMessages.add(newMessage);
                mAddressID = intent.getStringExtra("number");
                extractParams();
                mResultText.setTextKeepState(incMessages.toString());
            }
        } else {
            if (intent.hasExtra("ID")) { // запускаем из настроек
                mAddressID = intent.getStringExtra("ID");
                extractParams();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_menu: {
                AlertDialog.Builder settingsSelector = new AlertDialog.Builder(this);
                final String[] IDs = mPrefs.getString("IDs", "").split(";");

                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        switch (i) {
                            case 0: //  add_device
                                startActivity(intent);
                                break;
                            case 1: { // remove_device
                                List<String> IDStrings = new ArrayList<>();
                                Collections.addAll(IDStrings, mPrefs.getString("IDs", "").split(";"));
                                IDStrings.remove(mAddressID);

                                // deleting from database
                                try {
                                    RuntimeExceptionDao<HistoryEntry, Long> dao = DbProvider.getHelper().getHistoryDao();
                                    DeleteBuilder<HistoryEntry, Long> stmt = dao.deleteBuilder();
                                    stmt.where().eq("deviceName", mDevice.details.name);
                                    dao.delete(stmt.prepare());
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                SharedPreferences.Editor edit = mPrefs.edit();
                                edit.putString("IDs", Utils.join(IDStrings, ";"));
                                edit.remove(mAddressID);
                                edit.commit();

                                if (IDs.length != 1) { // если есть еще устройства, даем выбор
                                    Intent selector = new Intent(MainActivity.this, SelectorActivity.class);
                                    startActivity(selector);
                                } else { // если нет, надо добавить
                                    startActivity(intent);
                                }
                                //finish();
                                break;
                            }
                            case 2: // edit_device
                                startActivity(intent.putExtra("ID", mAddressID));
                                break;
                        }
                    }
                };

                if (IDs.length > 1 || IDs.length == 1 && IDs[0].length() != 0) {
                    settingsSelector.setItems(new CharSequence[]{getString(R.string.add_device), getString(R.string.remove_device), getString(R.string.edit_device)}, listener);
                } else {
                    settingsSelector.setItems(new CharSequence[]{getString(R.string.add_device), getString(R.string.remove_device)}, listener);
                }
                settingsSelector.create().show();
                return true;
            }
            case R.id.device_history:
                HistoryListFragment hlf = HistoryListFragment.newInstance(mDevice.details.name);
                hlf.show(getFragmentManager(), "HistoryDialog_" + mDevice.details.name);
                return true;
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(Utils.SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(Utils.DELIVERED), 0);
        SmsManager sms = SmsManager.getDefault();
        switch (view.getId()) {
            case R.id.relay1_on_button:
                sms.sendTextMessage(mDevice.details.number, null, "*" + mDevice.details.password + "#_set_1=1#", sentPI, deliveredPI);
                break;
            case R.id.relay1_off_button:
                sms.sendTextMessage(mDevice.details.number, null, "*" + mDevice.details.password + "#_set_1=0#", sentPI, deliveredPI);
                break;
            case R.id.relay2_on_button:
                sms.sendTextMessage(mDevice.details.number, null, "*" + mDevice.details.password + "#_set_2=1#", sentPI, deliveredPI);
                break;
            case R.id.relay2_off_button:
                sms.sendTextMessage(mDevice.details.number, null, "*" + mDevice.details.password + "#_set_2=0#", sentPI, deliveredPI);
                break;
            case R.id.get_data_button:
                sms.sendTextMessage(mDevice.details.number, null, "*" + mDevice.details.password + "#_info#", sentPI, deliveredPI);
                break;
            case R.id.get_temperature_button:
                sms.sendTextMessage(mDevice.details.number, null, "*" + mDevice.details.password + "#_temp#", sentPI, deliveredPI);
                break;
            case R.id.signal_on_button:
                sms.sendTextMessage(mDevice.details.number, null, "*" + mDevice.details.password + "#_on#", sentPI, deliveredPI);
                break;
            case R.id.signal_off_button:
                sms.sendTextMessage(mDevice.details.number, null, "*" + mDevice.details.password + "#_off#", sentPI, deliveredPI);
                break;
        }
    }
}
