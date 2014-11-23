package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.adonai.GsmNotify.Device;
import com.adonai.GsmNotify.R;
import com.adonai.contrib.ThreeStateButton;
import com.adonai.contrib.ThreeStateCheckBox;

public class SettingsPage1 extends SettingsFragment
{
    EditText mTimeToArm;
    RadioGroup mInputManager;
    ThreeStateCheckBox mSendSMS;
    EditText mTimeToWait;
    ThreeStateCheckBox mSmsAtDisarm, mSmsAtWrongKey, mSmsAtArm;
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
        mSendSMS = (ThreeStateCheckBox) layout.findViewById(R.id.sms_send_check);
        mTimeToWait = (EditText) layout.findViewById(R.id.time_to_send_edit);
        mSmsAtArm = (ThreeStateCheckBox) layout.findViewById(R.id.sms_at_arm);
        mSmsAtDisarm = (ThreeStateCheckBox) layout.findViewById(R.id.sms_at_disarm);
        mSmsAtWrongKey = (ThreeStateCheckBox) layout.findViewById(R.id.sms_at_wrong_key);
        mPassword = (EditText) layout.findViewById(R.id.new_password_edit);

        // Handlers
        mTimeToArm.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                mSource.timeToArm = getValue(s.toString(), 0);
            }
        });
        mInputManager.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case -1:
                    case R.id.manage_input_1:
                        mSource.inputManager = 1;
                        break;
                    case R.id.manage_input_2:
                        mSource.inputManager = 2;
                        break;
                    case R.id.manage_input_3:
                        mSource.inputManager = 3;
                        break;
                }
            }
        });
        mSendSMS.setOnStateChangedListener(new ThreeStateButton.OnStateChangedListener()
        {
            @Override
            public void onStateChanged(View v, int newState)
            {
                switch (newState)
                {
                    case ThreeStateButton.STATE_UNKNOWN: mSource.sendSmsOnPowerLoss = null; break;
                    case ThreeStateButton.STATE_NO: mSource.sendSmsOnPowerLoss = false; break;
                    case ThreeStateButton.STATE_YES: mSource.sendSmsOnPowerLoss = true; break;
                }
            }
        });
        mTimeToWait.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                mSource.timeToWaitOnPowerLoss = getValue(s.toString(), 0);
            }
        });
        mSmsAtArm.setOnStateChangedListener(new ThreeStateButton.OnStateChangedListener()
        {
            @Override
            public void onStateChanged(View v, int newState)
            {
                switch (newState)
                {
                    case ThreeStateButton.STATE_UNKNOWN: mSource.smsAtArm = null; break;
                    case ThreeStateButton.STATE_NO: mSource.smsAtArm = false; break;
                    case ThreeStateButton.STATE_YES: mSource.smsAtArm = true; break;
                }
            }
        });
        mSmsAtDisarm.setOnStateChangedListener(new ThreeStateButton.OnStateChangedListener()
        {
            @Override
            public void onStateChanged(View v, int newState)
            {
                switch (newState)
                {
                    case ThreeStateButton.STATE_UNKNOWN: mSource.smsAtDisarm = null; break;
                    case ThreeStateButton.STATE_NO: mSource.smsAtDisarm = false; break;
                    case ThreeStateButton.STATE_YES: mSource.smsAtDisarm = true; break;
                }
            }
        });
        mSmsAtWrongKey.setOnStateChangedListener(new ThreeStateButton.OnStateChangedListener()
        {
            @Override
            public void onStateChanged(View v, int newState)
            {
                switch (newState)
                {
                    case ThreeStateButton.STATE_UNKNOWN: mSource.smsAtWrongKey = null; break;
                    case ThreeStateButton.STATE_NO: mSource.smsAtWrongKey = false; break;
                    case ThreeStateButton.STATE_YES: mSource.smsAtWrongKey = true; break;
                }
            }
        });
        mPassword.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                mSource.details.password = s.toString();
            }
        });


        return layout;
    }
}