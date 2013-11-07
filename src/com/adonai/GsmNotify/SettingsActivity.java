package com.adonai.GsmNotify;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.adonai.GsmNotify.settings.SettingsFragment;
import com.adonai.GsmNotify.settings.SettingsPage1;
import com.adonai.GsmNotify.settings.SettingsPage2;
import com.adonai.GsmNotify.settings.SettingsPage3;
import com.adonai.GsmNotify.settings.SettingsPage4;
import com.adonai.GsmNotify.settings.SettingsPage5;
import com.google.gson.Gson;

public class SettingsActivity extends FragmentActivity implements View.OnClickListener, Handler.Callback
{
    final public static int HANDLE_INCOMING = 0;
    final public static int HANDLE_STEP_1 = 1;
    final public static int HANDLE_STEP_2 = 2;
    final public static int HANDLE_STEP_3 = 3;


    String SENT = "SMS_SENT_NOTIFY";
    String DELIVERED = "SMS_DELIVERED_NOTIFY";

    BroadcastReceiver sentReceiver, deliveryReceiver;
    SharedPreferences mPrefs;
    ProgressDialog pd;

    Button mApply;
    Handler mHandler;

    FragmentManager mFragmentManager;
    SettingsFragment[] mSettingsPage = new SettingsFragment[5];
    ViewPager mPager;
    FragmentPagerAdapter mPagerAdapter;

    Device mSavedDevice, mNewDevice;

    public  class sentConfirmReceiver extends BroadcastReceiver
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

            if(getResultCode() != Activity.RESULT_OK)
            {
                mHandler.removeCallbacksAndMessages(null);
                pd.dismiss();
            }
        }
    }

    public  class deliveryConfirmReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            switch (getResultCode())
            {
                case Activity.RESULT_OK: Toast.makeText(SettingsActivity.this, getString(R.string.sms_deliver_success), Toast.LENGTH_SHORT).show(); break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(SettingsActivity.this, getString(R.string.result_canceled), Toast.LENGTH_SHORT).show();
                    mHandler.removeCallbacksAndMessages(null);
                    pd.dismiss();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_settings);

        mPrefs = getSharedPreferences(SMSReceiveService.PREFERENCES, MODE_PRIVATE);
        sentReceiver = new sentConfirmReceiver();
        deliveryReceiver = new deliveryConfirmReceiver();
        mFragmentManager = getSupportFragmentManager();

        mPager = (ViewPager) findViewById(R.id.settings_page_holder);
        mPagerAdapter = new FragmentPagerAdapter(mFragmentManager)
        {
            @Override
            public Fragment getItem(int i)
            {
                assert i < mSettingsPage.length;
                return mSettingsPage[i];
            }

            @Override
            public int getCount()
            {
                return 5;
            }

            @Override
            public CharSequence getPageTitle(int position)
            {
                switch(position)
                {
                    case 0: return getString(R.string.common);
                    case 1: return getString(R.string.phones);
                    case 2: return getString(R.string.inputs);
                    case 3: return getString(R.string.outputs);
                    case 4: return getString(R.string.temperature);
                    default: return null;
                }
            }
        };

        mPager.setAdapter(mPagerAdapter);


        mApply = (Button) findViewById(R.id.device_apply);
        mApply.setOnClickListener(this);

        prepareUI(getIntent().getStringExtra("ID"));

        mHandler = new Handler(this);
    }

    private void prepareUI(String id)
    {
        if(id != null)
        {
            mSavedDevice = new Gson().fromJson(mPrefs.getString(id, ""), Device.class);
            mNewDevice = new Gson().fromJson(mPrefs.getString(id, ""), Device.class);
        }
        else
        {
            mSavedDevice = new Device();
            mNewDevice = new Device();
        }

        mSettingsPage[0] = new SettingsPage1(mSavedDevice);
        mSettingsPage[1] = new SettingsPage2(mSavedDevice);
        mSettingsPage[2] = new SettingsPage3(mSavedDevice);
        mSettingsPage[3] = new SettingsPage4(mSavedDevice);
        mSettingsPage[4] = new SettingsPage5(mSavedDevice);

        /*mNumber.setText(dev.number);
        mName.setText(dev.name);
        mPassword.setText(dev.password);
        mTempLimit.setText(dev.tempLimit);
        mtMin.setText(dev.tMin);
        mtMax.setText(dev.tMax);
        mSendSMS.setChecked(dev.sendSMS);*/
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //--- When the SMS has been sent ---
        registerReceiver(sentReceiver, new IntentFilter(SENT));
        //--- When the SMS has been delivered. ---
        registerReceiver(deliveryReceiver, new IntentFilter(DELIVERED));
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliveryReceiver);
    }

    @Override
    public void onClick(View v)
    {
        /*switch (v.getId())
        {
            case R.id.device_apply:
                try
                {
                    Double.parseDouble(mTempLimit.getText().toString());
                    Double.parseDouble(mtMin.getText().toString());
                    Double.parseDouble(mtMax.getText().toString());
                    Long.parseLong(mNumber.getText().toString());
                }
                catch (NumberFormatException nfe)
                {
                    Toast.makeText(SettingsActivity.this, getString(R.string.data_not_full), Toast.LENGTH_SHORT).show();
                    return;
                }

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
                toSave.tMin = mtMin.getText().toString();
                toSave.tMax = mtMax.getText().toString();
                toSave.sendSMS = mSendSMS.isChecked();

                String IDStrings = mPrefs.getString("IDs", "");
                if(!IDStrings.contains(mNumber.getText().toString()))
                    IDStrings = IDStrings + mNumber.getText().toString() + ";";

                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putString("IDs", IDStrings);
                edit.putString(mNumber.getText().toString(), new Gson().toJson(toSave));
                edit.commit();
                break;
        }*/
    }

    private String composeMessage()
    {
        // *1928#_sp_*1_5=1_8=1#*5_1=1_2=50000_3=1_5=1_6=25000_7=30000#
        String res = "*";
        /*res += mPassword.getText().toString();
        res += "#_sp_*1_5=" + (mSendSMS.isChecked() ? "1" : "+") + "_8=" + (mSendSMS.isChecked() ? "1" : "+") + "#*5_1=1_2=";
        String tmp = String.format("%05.0f", Double.parseDouble(mTempLimit.getText().toString()) * 1000).substring(0, 5);
        res += tmp + "_3=1_5=1_6=";
        tmp = String.format("%05.0f", Double.parseDouble(mtMin.getText().toString()) * 1000).substring(0, 5);
        res += tmp + "_7=";
        tmp = String.format("%05.0f", Double.parseDouble(mtMax.getText().toString()) * 1000).substring(0, 5);
        res += tmp + "#";*/
        return res;
    }

    @Override
    public boolean handleMessage(Message msg)
    {
        switch (msg.what)
        {
            case HANDLE_STEP_1:
            {
                pd.setMessage(getString(R.string.setting_mode));
                mHandler.sendEmptyMessageDelayed(HANDLE_STEP_2, 10000);

                PendingIntent sentPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(SENT), 0);
                PendingIntent deliveredPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(DELIVERED), 0);
                SmsManager sms = SmsManager.getDefault();
                //sms.sendTextMessage(mNumber.getText().toString(), null, "*" + mPassword.getText().toString() + "#" + (mMode.getCheckedRadioButtonId() == R.id.mode0_radio ? "_tb" : "_th") + "#", sentPI, deliveredPI);
                break;
            }
            case HANDLE_STEP_2:
            {
                pd.setMessage(getString(R.string.resetting_device));
                mHandler.sendEmptyMessageDelayed(HANDLE_STEP_3, 10000);

                PendingIntent sentPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(SENT), 0);
                PendingIntent deliveredPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(DELIVERED), 0);
                SmsManager sms = SmsManager.getDefault();
                //sms.sendTextMessage(mNumber.getText().toString(), null, "*" + mPassword.getText().toString() + "#_fullrst#", sentPI, deliveredPI);
                break;
            }
            case HANDLE_STEP_3:
                pd.dismiss();

                /*Intent editNow = new Intent(this, MainActivity.class).putExtra("ID", mNumber.getText().toString());
                startActivity(editNow);
                finish();
                break;*/
        }
        return true;
    }
}
