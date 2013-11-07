package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.adonai.GsmNotify.Device;
import com.adonai.GsmNotify.R;

public class SettingsPage1 extends SettingsFragment
{
    EditText mTimeToArm;
    RadioGroup mInputManager;
    CheckBox mSendSMS;
    EditText mTimeToWait;
    RadioGroup mSmsSendSetting;
    EditText mPassword;

    public SettingsPage1(Device source)
    {
        super(source);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        View layout = inflater.inflate(R.layout.settings_fragment_1, container, false);
        assert layout != null;

        mTimeToArm = (EditText) layout.findViewById(R.id.time_to_arm_edit);
        mInputManager = (RadioGroup) layout.findViewById(R.id.manage_input_group);
        mSendSMS = (CheckBox) layout.findViewById(R.id.sms_send_check);
        mTimeToWait = (EditText) layout.findViewById(R.id.time_to_send_edit);
        mSmsSendSetting = (RadioGroup) layout.findViewById(R.id.sms_at_group);
        mPassword = (EditText) layout.findViewById(R.id.new_password_edit);

        mTimeToArm.setText(mSource.timeToArm);
        switch (mSource.inputManager)
        {
            case 1: mInputManager.check(R.id.manage_input_1); break;
            case 2: mInputManager.check(R.id.manage_input_2); break;
            case 3: mInputManager.check(R.id.manage_input_3); break;
        }
        mSendSMS.setChecked(mSource.sendSmsOnPowerLoss);
        mTimeToWait.setText(mSource.timeToWaitOnPowerLoss);
        switch (mSource.smsSendSetting)
        {
            case 1: mSmsSendSetting.check(R.id.sms_at_1); break;
            case 2: mSmsSendSetting.check(R.id.sms_at_2); break;
            case 3: mSmsSendSetting.check(R.id.sms_at_3); break;
        }
        mPassword.setText(mSource.devicePassword);

        return layout;
    }

    @Override
    public void compileDiff()
    {
        mSource.timeToArm = getValue(mTimeToArm.getText().toString(), 0);

        switch (mInputManager.getCheckedRadioButtonId())
        {
            case -1:
            case R.id.manage_input_1: mSource.inputManager = 1; break;
            case R.id.manage_input_2: mSource.inputManager = 2; break;
            case R.id.manage_input_3: mSource.inputManager = 3; break;
        }

        mSource.sendSmsOnPowerLoss = mSendSMS.isChecked();
        mSource.timeToWaitOnPowerLoss = getValue(mTimeToWait.getText().toString(), 0);

        switch (mSmsSendSetting.getCheckedRadioButtonId())
        {
            case -1:
            case R.id.sms_at_1: mSource.smsSendSetting = 1; break;
            case R.id.sms_at_2: mSource.smsSendSetting = 2; break;
            case R.id.sms_at_3: mSource.smsSendSetting = 3; break;
        }

        mSource.devicePassword = mPassword.getText().toString();
    }
}