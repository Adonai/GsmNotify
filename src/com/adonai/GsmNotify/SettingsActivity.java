package com.adonai.GsmNotify;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.adonai.GsmNotify.settings.SettingsFragment;
import com.adonai.GsmNotify.settings.SettingsPage1;
import com.adonai.GsmNotify.settings.SettingsPage2;
import com.adonai.GsmNotify.settings.SettingsPage3;
import com.adonai.GsmNotify.settings.SettingsPage4;
import com.adonai.GsmNotify.settings.SettingsPage5;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsActivity extends FragmentActivity implements View.OnClickListener, Handler.Callback
{
    final public static int HANDLE_STEP = 1;
    final public static int HANDLE_FINISH = 2;
    final public static int HANDLE_RESET = 3;

    final public static int SMS_DEFAULT_TIMEOUT = 20000;


    String SENT = "SMS_SENT_NOTIFY";
    String DELIVERED = "SMS_DELIVERED_NOTIFY";

    Boolean hasSomethingChanged = false;

    BroadcastReceiver sentReceiver, deliveryReceiver;
    SharedPreferences mPrefs;
    ProgressDialog pd;

    Button mApply, mEditDevice, mManageDevice, mApplyDevice;
    EditText mDeviceName, mDeviceNumber, mDevicePassword;
    Handler mHandler;

    FragmentManager mFragmentManager;
    SettingsFragment[] mSettingsPage = new SettingsFragment[5];
    ViewPager mPager;
    ViewFlipper mFlipper;
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
                    //Toast.makeText(SettingsActivity.this, getString(R.string.result_canceled), Toast.LENGTH_SHORT).show();
                    //mHandler.removeCallbacksAndMessages(null);
                    //pd.dismiss();
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
        mPager.setOffscreenPageLimit(5);
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

        mFlipper = (ViewFlipper) findViewById(R.id.settings_flipper);
        mApply = (Button) findViewById(R.id.device_apply);
        mApply.setOnClickListener(this);
        mEditDevice = (Button) findViewById(R.id.edit_device_button);
        mEditDevice.setOnClickListener(this);
        mManageDevice = (Button) findViewById(R.id.manage_device_button);
        mManageDevice.setOnClickListener(this);
        mApplyDevice = (Button) findViewById(R.id.device_apply_button);
        mApplyDevice.setOnClickListener(this);
        mDeviceName = (EditText) findViewById(R.id.device_name_text);
        mDeviceNumber = (EditText) findViewById(R.id.device_number_text);
        mDevicePassword = (EditText) findViewById(R.id.device_password_text);

        prepareUI(getIntent().getStringExtra("ID"));

        mHandler = new Handler(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.remove("currentEdit");
        edit.commit();
    }

    private void prepareUI(String id)
    {
        if(id != null)
        {
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putString("currentEdit", id);
            edit.commit();

            mSavedDevice = new Gson().fromJson(mPrefs.getString(id, ""), Device.class);
            mNewDevice = new Gson().fromJson(mPrefs.getString(id, ""), Device.class);

            if(mNewDevice.name != null)
                mDeviceName.setText(mSavedDevice.name);
            if(mNewDevice.number != null)
                mDeviceNumber.setText(mSavedDevice.number);
            if(mNewDevice.devicePassword != null)
                mDevicePassword.setText(mSavedDevice.devicePassword);

            mManageDevice.setVisibility(View.VISIBLE);
            mEditDevice.setVisibility(View.VISIBLE);
        }
        else
        {
            mSavedDevice = new Device();
            mNewDevice = new Device();
        }

        mSettingsPage[0] = new SettingsPage1(mNewDevice);
        mSettingsPage[1] = new SettingsPage2(mNewDevice);
        mSettingsPage[2] = new SettingsPage3(mNewDevice);
        mSettingsPage[3] = new SettingsPage4(mNewDevice);
        mSettingsPage[4] = new SettingsPage5(mNewDevice);
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
        switch (v.getId())
        {
            case R.id.manage_device_button:
            {
                Intent editNow = new Intent(this, MainActivity.class).putExtra("ID", mNewDevice.number);
                startActivity(editNow);
                finish();
                break;
            }
            case R.id.device_apply_button:
            {
                if(mDevicePassword.getText().toString().length() > 0 && mDeviceNumber.getText().toString().length() > 0 && mDeviceName.getText().toString().length() > 0) // all fields are filled in
                {
                    List<String> IDStrings = new ArrayList<>();
                    Collections.addAll(IDStrings, mPrefs.getString("IDs", "").split(";"));

                    if(IDStrings.contains(mDeviceNumber.getText().toString()) && !(mSavedDevice.number != null && mDeviceNumber.getText().toString().equals(mSavedDevice.number))) // we have already that number and that's not us
                    {
                        Toast.makeText(this, R.string.existing_device, Toast.LENGTH_SHORT).show();
                        break;
                    }

                    // we assume we have all the views created
                    mSavedDevice.devicePassword = mDevicePassword.getText().toString();
                    EditText password = (EditText) mSettingsPage[0].getView().findViewById(R.id.new_password_edit);
                    password.setText(mDevicePassword.getText().toString());

                    mNewDevice.name = mDeviceName.getText().toString();
                    mNewDevice.number = mDeviceNumber.getText().toString();

                    SharedPreferences.Editor edit = mPrefs.edit();
                    if(mSavedDevice.number != null) // if we have old device data
                    {
                        IDStrings.remove(mSavedDevice.number);
                        edit.remove(mSavedDevice.number); // delete old data about this device
                    }

                    IDStrings.add(mNewDevice.number);
                    edit.putString("IDs", Utils.join(IDStrings, ";"));
                    edit.putString(mNewDevice.number, new Gson().toJson(mNewDevice));
                    edit.commit();

                    Toast.makeText(this, R.string.settings_applied, Toast.LENGTH_SHORT).show();

                    mManageDevice.setVisibility(View.VISIBLE);
                    mEditDevice.setVisibility(View.VISIBLE);
                }
                else
                    new AlertDialog.Builder(this).setMessage(R.string.data_not_full).create().show();
                break;
            }
            case R.id.edit_device_button:
            {
                mFlipper.setDisplayedChild(1);
                break;
            }
            case R.id.device_apply:
                hasSomethingChanged = false;
                pd = ProgressDialog.show(this, getString(R.string.wait_please), getString(R.string.querying_device), true, false);
                mHandler.sendMessage(mHandler.obtainMessage(HANDLE_STEP, 1));
                break;
        }
    }


    private boolean shouldBeSent(Object old, Object current)
    {
        if(old == null && current != null)
            return true;

        return old != null && current != null && !old.equals(current);
    }

    // boolean - page changed, string - message to send
    private Pair<Boolean, String> compileDiff(Integer pageNumber)
    {
        assert pageNumber != null;
        String res = "*" + mSavedDevice.devicePassword + "#_sp_*" + pageNumber;
        Integer original_length = res.length();
        if(pageNumber == 1)
        {
            if(shouldBeSent(mSavedDevice.timeToArm, mNewDevice.timeToArm))
                res += "_1=" + String.format("%03d", mNewDevice.timeToArm);
            if(shouldBeSent(mSavedDevice.inputManager, mNewDevice.inputManager))
                res += "_2=" + String.format("%1d", mNewDevice.inputManager);
            if(shouldBeSent(mSavedDevice.sendSmsOnPowerLoss, mNewDevice.sendSmsOnPowerLoss))
                res += "_3=" + (mNewDevice.sendSmsOnPowerLoss ? "1" : "+");
            if(shouldBeSent(mSavedDevice.timeToWaitOnPowerLoss, mNewDevice.timeToWaitOnPowerLoss))
                res += "_4=" + String.format("%03d", mNewDevice.timeToWaitOnPowerLoss);
            if(shouldBeSent(mSavedDevice.smsAtArm, mNewDevice.smsAtArm))
                res += "_5=" + (mNewDevice.smsAtArm ? "1" : "+");
            if(shouldBeSent(mSavedDevice.smsAtWrongKey, mNewDevice.smsAtWrongKey))
                res += "_6=" + (mNewDevice.smsAtWrongKey ? "1" : "+");
            if(shouldBeSent(mSavedDevice.devicePassword, mNewDevice.devicePassword))
                res += "_7=" + "\"" + mNewDevice.devicePassword + "\"";
            if(shouldBeSent(mSavedDevice.smsAtDisarm, mNewDevice.smsAtDisarm))
                res += "_8=" + (mNewDevice.smsAtDisarm ? "1" : "+");
        }
        if(pageNumber == 2)
        {
            for(int i = 0; i < mNewDevice.phones.length; i++)
            {
                Device.PhoneSettings curr = mNewDevice.phones[i];
                Device.PhoneSettings old = mSavedDevice.phones[i];

                if(shouldBeSent(old.phoneNum, curr.phoneNum))
                    res += "_1." + String.valueOf(i + 1) + "=" + curr.phoneNum;
                if(shouldBeSent(old.info, curr.info))
                    res += "_2." + String.valueOf(i + 1) + "=" + (curr.info ? "1" : "+");
                if(shouldBeSent(old.manage, curr.manage))
                    res += "_3." + String.valueOf(i + 1) + "=" + (curr.manage ? "1" : "+");
                if(shouldBeSent(old.confirm, curr.confirm))
                    res += "_4." + String.valueOf(i + 1) + "=" + (curr.confirm ? "1" : "+");
            }
            if(shouldBeSent(mSavedDevice.recallCycles, mNewDevice.recallCycles))
                res += "_5=" + String.format("%03d", mNewDevice.recallCycles);
            if(shouldBeSent(mSavedDevice.recallWait, mNewDevice.recallWait))
                res += "_6=" + String.format("%03d", mNewDevice.recallWait);
            if(shouldBeSent(mSavedDevice.checkBalanceNum, mNewDevice.checkBalanceNum))
                res += "_7=" + "\"" + mNewDevice.checkBalanceNum + "\"";
        }
        if(pageNumber == 3)
        {
            for(int i = 0; i < mNewDevice.inputs.length; i++)
            {
                Device.InputSettings curr = mNewDevice.inputs[i];
                Device.InputSettings old = mSavedDevice.inputs[i];

                if(shouldBeSent(old.timeToRearm, curr.timeToRearm))
                    res += "_1." + String.valueOf(i + 1) + "=" + String.format("%03d", curr.timeToRearm);
                // TODO: here will some additional code sit
                if(shouldBeSent(old.timeToWaitBeforeCall, curr.timeToWaitBeforeCall))
                    res += "_3." + String.valueOf(i + 1) + "=" + String.format("%03d", curr.timeToWaitBeforeCall);
                if(shouldBeSent(old.smsText, curr.smsText))
                    res += "_5." + String.valueOf(i + 1) + "=" + "\"" + curr.smsText + "\"";
                if(shouldBeSent(old.constantControl, curr.constantControl))
                    res += "_6." + String.valueOf(i + 1) + "=" + (curr.constantControl ? "1" : "+");
                if(shouldBeSent(old.innerSound, curr.innerSound))
                    res += "_6." + String.valueOf(i + 1) + "=" + (curr.innerSound ? "1" : "+");
            }
        }
        if(pageNumber == 4)
        {
            for(int i = 0; i < mNewDevice.outputs.length; i++)
            {
                Device.OutputSettings curr = mNewDevice.outputs[i];
                Device.OutputSettings old = mSavedDevice.outputs[i];

                if(shouldBeSent(old.outputMode, curr.outputMode)) // always send timing when change mode
                {
                    res += "_1." + String.valueOf(i + 1) + "=" + String.format("%1d", curr.outputMode);
                    if(curr.outputMode == 3 && curr.timeToEnableOnDisarm != null)
                        res += "_2." + String.valueOf(i + 1) + "=" + String.format("%03d", curr.timeToEnableOnDisarm);
                    else if (curr.outputMode == 4 && curr.timeToEnableOnAlert != null)
                        res += "_2." + String.valueOf(i + 1) + "=" + String.format("%03d", curr.timeToEnableOnAlert);
                }
                else if(curr.outputMode != null) // if only timing changed
                {
                    if(curr.outputMode == 3 && shouldBeSent(old.timeToEnableOnDisarm, curr.timeToEnableOnDisarm))
                        res += "_2." + String.valueOf(i + 1) + "=" + String.format("%03d", curr.timeToEnableOnDisarm);
                    if(curr.outputMode == 4 && shouldBeSent(old.timeToEnableOnAlert, curr.timeToEnableOnAlert))
                        res += "_2." + String.valueOf(i + 1) + "=" + String.format("%03d", curr.timeToEnableOnAlert);
                }
            }
        }
        if(pageNumber == 5)
        {
            if(shouldBeSent(mSavedDevice.enableTC, mNewDevice.enableTC))
                res += "_1=" + (mNewDevice.enableTC ? "1" : "+");
            if(shouldBeSent(mSavedDevice.tempLimit, mNewDevice.tempLimit))
                res += "_2=" + String.format("%05.0f", mNewDevice.tempLimit * 1000).substring(0, 5);
            if(shouldBeSent(mSavedDevice.tcSendSms, mNewDevice.tcSendSms))
                res += "_3=" + (mNewDevice.tcSendSms ? "1" : "+");
            if(shouldBeSent(mSavedDevice.tcActivateAlert, mNewDevice.tcActivateAlert))
                res += "_4=" + (mNewDevice.tcActivateAlert ? "1" : "+");
            if(shouldBeSent(mSavedDevice.tcActivateInnerSound, mNewDevice.tcActivateInnerSound))
                res += "_5=" + (mNewDevice.tcActivateInnerSound ? "1" : "+");
            if(shouldBeSent(mSavedDevice.tMin, mNewDevice.tMin))
                res += "_6=" + String.format("%05.0f", mNewDevice.tMin * 1000).substring(0, 5);
            if(shouldBeSent(mSavedDevice.tMax, mNewDevice.tMax))
                res += "_7=" + String.format("%05.0f", mNewDevice.tMax * 1000).substring(0, 5);
        }

        return new Pair<>(res.length() > original_length, res + "#");
    }

    @Override
    public boolean handleMessage(Message msg)
    {
        switch (msg.what)
        {
            case HANDLE_STEP:
            {
                Integer step = (Integer) msg.obj;
                switch(step)
                {
                    case 1: // pages
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    {
                        pd.setMessage(getString(R.string.setting_mode) + " " + step);
                        Pair<Boolean, String> toSend = compileDiff(step);
                        if(toSend.first)
                        {
                            hasSomethingChanged = true;
                            PendingIntent sentPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(SENT), 0);
                            PendingIntent deliveredPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(DELIVERED), 0);
                            SmsManager sms = SmsManager.getDefault();
                            sms.sendTextMessage(mNewDevice.number, null, toSend.second, sentPI, deliveredPI);
                            mHandler.sendMessageDelayed(mHandler.obtainMessage(HANDLE_STEP, ++step), SMS_DEFAULT_TIMEOUT);
                        }
                        else
                            mHandler.sendMessage(mHandler.obtainMessage(HANDLE_STEP, ++step));
                        break;
                    }
                    case 6: // temp control
                    {
                        pd.setMessage(getString(R.string.setting_temp_mode));
                        if(shouldBeSent(mSavedDevice.tempMode, mNewDevice.tempMode))
                        {
                            String res = "*" + mSavedDevice.devicePassword + "#" + (mNewDevice.tempMode == 1 ? "_tb" : "_th") + "#";
                            PendingIntent sentPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(SENT), 0);
                            PendingIntent deliveredPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(DELIVERED), 0);
                            SmsManager sms = SmsManager.getDefault();
                            sms.sendTextMessage(mNewDevice.number, null, res, sentPI, deliveredPI);
                            mHandler.sendEmptyMessageDelayed(HANDLE_RESET, SMS_DEFAULT_TIMEOUT);
                        }
                        else
                            mHandler.sendEmptyMessage(HANDLE_RESET);
                        break;
                    }
                }
                break;
            }
            case HANDLE_RESET:
            {
                if(hasSomethingChanged)
                {
                    pd.setMessage(getString(R.string.resetting_device));

                    PendingIntent sentPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(SENT), 0);
                    PendingIntent deliveredPI = PendingIntent.getBroadcast(SettingsActivity.this, 0, new Intent(DELIVERED), 0);
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(mNewDevice.number, null, "*" + mSavedDevice.devicePassword + "#_fullrst#", sentPI, deliveredPI);
                    mHandler.sendEmptyMessageDelayed(HANDLE_FINISH, SMS_DEFAULT_TIMEOUT);
                }
                else
                    mHandler.sendEmptyMessage(HANDLE_FINISH);


                break;
            }
            case HANDLE_FINISH:
            {
                pd.dismiss();

                String IDStrings = mPrefs.getString("IDs", "");
                if(!IDStrings.contains(mNewDevice.number))
                    IDStrings = IDStrings + mNewDevice.number + ";";

                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putString("IDs", IDStrings);
                edit.putString(mNewDevice.number, new Gson().toJson(mNewDevice));
                edit.commit();

                // replace with finish
                // mSavedDevice = new Gson().fromJson(mPrefs.getString(mNewDevice.number, ""), Device.class);

                Intent editNow = new Intent(this, MainActivity.class).putExtra("ID", mNewDevice.number);
                startActivity(editNow);
                finish();
                break;
            }
        }
        return true;
    }
}
