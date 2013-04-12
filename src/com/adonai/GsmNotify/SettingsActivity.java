package com.adonai.GsmNotify;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.gson.Gson;

public class SettingsActivity extends Activity implements View.OnClickListener, Handler.Callback
{
    final public static int HANDLE_INCOMING = 0;
    final public static int HANDLE_STEP_1 = 1;
    final public static int HANDLE_STEP_2 = 2;
    final public static int HANDLE_STEP_3 = 3;


    String SENT = "SMS_SENT_NOTIFY";
    String DELIVERED = "SMS_DELIVERED_NOTIFY";

    SharedPreferences mPrefs;
    ProgressDialog pd;
    EditText mName, mNumber, mPassword;
    EditText mTempLimit, mtMin, mtMax;
    RadioGroup mMode;
    Button mApply;
    Handler mHandler;

    BroadcastReceiver sentReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            switch (getResultCode())
            {
                case Activity.RESULT_OK: Toast.makeText(SettingsActivity.this, getString(R.string.sms_sent_success), Toast.LENGTH_SHORT).show(); break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE: Toast.makeText(SettingsActivity.this, getString(R.string.generic_failure), Toast.LENGTH_SHORT).show(); break;
                case SmsManager.RESULT_ERROR_NO_SERVICE: Toast.makeText(SettingsActivity.this, getString(R.string.no_service), Toast.LENGTH_SHORT).show(); break;
                case SmsManager.RESULT_ERROR_NULL_PDU: Toast.makeText(SettingsActivity.this, getString(R.string.null_message), Toast.LENGTH_SHORT).show(); break;
                case SmsManager.RESULT_ERROR_RADIO_OFF: Toast.makeText(getBaseContext(), getString(R.string.radio_off), Toast.LENGTH_SHORT).show(); break;
            }
            unregisterReceiver(this);
        }
    };

    BroadcastReceiver deliveryReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            switch (getResultCode())
            {
                case Activity.RESULT_OK: Toast.makeText(SettingsActivity.this, getString(R.string.sms_deliver_success), Toast.LENGTH_SHORT).show(); break;
                case Activity.RESULT_CANCELED: Toast.makeText(SettingsActivity.this, getString(R.string.result_canceled), Toast.LENGTH_SHORT).show(); break;
            }
            unregisterReceiver(this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_settings);

        mPrefs = getSharedPreferences(SMSReceiveService.PREFERENCES, MODE_PRIVATE);

        mName = (EditText) findViewById(R.id.device_name);
        mNumber = (EditText) findViewById(R.id.device_number);
        mPassword = (EditText) findViewById(R.id.device_password);

        mTempLimit = (EditText) findViewById(R.id.device_temp_limit);
        mtMin = (EditText) findViewById(R.id.device_tmin);
        mtMax = (EditText) findViewById(R.id.device_tmax);

        mMode = (RadioGroup) findViewById(R.id.device_mode);
        mApply = (Button) findViewById(R.id.device_apply);
        mApply.setOnClickListener(this);

        if(getIntent().hasExtra("ID"))
            prepareUI(getIntent().getStringExtra("ID"));

        mHandler = new Handler(this);
    }

    private void prepareUI(String id)
    {
        Device dev = new Gson().fromJson(mPrefs.getString(id, ""), Device.class);
        mNumber.setText(dev.number);
        mName.setText(dev.name);
        mPassword.setText(dev.password);
        mTempLimit.setText(dev.tempLimit);
        mMode.check(dev.mode.equals("1") ? R.id.mode1_radio : R.id.mode2_radio);
        mtMin.setText(dev.tMin);
        mtMax.setText(dev.tMax);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        sConn = new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service)
            {
                ((SMSReceiveService.LocalBinder) service).setListener(SettingsActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name)
            {
            }
        };
        bindService(new Intent(this, SMSReceiveService.class), sConn, 0);

        //--- When the SMS has been sent ---
        registerReceiver(sentReceiver, new IntentFilter(SENT));
        //--- When the SMS has been delivered. ---
        registerReceiver(deliveryReceiver, new IntentFilter(DELIVERED));
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unbindService(sConn);
        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliveryReceiver);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.device_apply:
                pd = ProgressDialog.show(this, getString(R.string.wait_please), getString(R.string.querying_device), true, false);

                PendingIntent sentPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(SENT), 0);
                PendingIntent deliveredPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(DELIVERED), 0);
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(mNumber.getText().toString(), null, composeMessage(), sentPI, deliveredPI);

                mHandler.sendEmptyMessageDelayed(HANDLE_STEP_1, 10000);

                Device toSave = new Device(mNumber.getText().toString());
                toSave.name = mName.getText().toString();
                toSave.password = mPassword.getText().toString();
                toSave.tempLimit = mTempLimit.getText().toString();
                toSave.mode = mMode.getCheckedRadioButtonId() == R.id.mode1_radio ? "1" : "2";
                toSave.tMin = mtMin.getText().toString();
                toSave.tMax = mtMax.getText().toString();

                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putString("IDs", mPrefs.getString("IDs", "") + mNumber.getText().toString() + ";");
                edit.putString(mNumber.getText().toString(), new Gson().toJson(toSave));
                edit.commit();
                break;
        }
    }

    private String composeMessage()
    {
        String res = "*";
        res += mPassword.getText().toString();
        res += "#_sp_*1_5=1_8=1#*5=";
        String tmp = String.format("%05.0f", Double.parseDouble(mTempLimit.getText().toString()) * 1000).substring(0, 5);
        res += tmp + "_6=";
        tmp = String.format("%05.0f", Double.parseDouble(mtMin.getText().toString()) * 1000).substring(0, 5);
        res += tmp + "_7=";
        tmp = String.format("%05.0f", Double.parseDouble(mtMax.getText().toString()) * 1000).substring(0, 5);
        res += tmp + "#";
        return res;
    }

    @Override
    public boolean handleMessage(Message msg)
    {
        switch (msg.what)
        {
            case HANDLE_STEP_1:
                pd.setMessage(getString(R.string.setting_mode));
                mHandler.sendEmptyMessageDelayed(HANDLE_STEP_2, 10000);

                PendingIntent sentPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(SENT), 0);
                PendingIntent deliveredPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(DELIVERED), 0);
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(mNumber.getText().toString(), null, "*" + mPassword.getText().toString() + "#" +  + "#", sentPI, deliveredPI);
            case HANDLE_INCOMING:
                pd.dismiss();

                Intent editNow = new Intent(this, MainActivity.class).putExtra("ID", mNumber.getText().toString());
                startActivity(editNow);
                break;
        }
        return true;
    }

    public void updateActivity(int msg, Object obj)
    {
        mHandler.sendMessage(mHandler.obtainMessage(msg, obj));
    }
}
