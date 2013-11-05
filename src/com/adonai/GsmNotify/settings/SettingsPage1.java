package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
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


        return layout;
    }

    @Override
    public void resetUI(Device source)
    {

    }

    @Override
    public void compileDiff(final Device source)
    {
        source.timeToArm = getValue(mTimeToArm.getText().toString(), 0);

        switch (mInputManager.getCheckedRadioButtonId())
        {
            case -1:
            case R.id.manage_input_1: source.inputManager = 1; break;
            case R.id.manage_input_2: source.inputManager = 2; break;
            case R.id.manage_input_3: source.inputManager = 3; break;
        }

        source.sendSmsOnPowerLoss = mSendSMS.isChecked();
        source.timeToWaitOnPowerLoss = getValue(mTimeToWait.getText().toString(), 0);

        switch (mSmsSendSetting.getCheckedRadioButtonId())
        {
            case -1:
            case R.id.sms_at_1: source.smsSendSetting = 1; break;
            case R.id.sms_at_2: source.smsSendSetting = 2; break;
            case R.id.sms_at_3: source.smsSendSetting = 3; break;
        }

        source.devicePassword = mPassword.getText().toString();
    }
}