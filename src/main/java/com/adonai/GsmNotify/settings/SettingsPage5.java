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

public class SettingsPage5 extends SettingsFragment
{
    ThreeStateCheckBox mTcEnable;
    EditText mTempLimit;
    RadioGroup mTcModeGroup;
    EditText mTMin, mTMax;
    ThreeStateCheckBox mSendSms, mActivateAlert, mActivateInnerSound;

    public static SettingsPage5 newInstance(Device source) {
        SettingsPage5 fragment = new SettingsPage5();
        fragment.mSource = source;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.settings_fragment_5, container, false);
        assert layout != null;

        mTcEnable = (ThreeStateCheckBox) layout.findViewById(R.id.tc_checkbox);
        mTempLimit = (EditText) layout.findViewById(R.id.tc_limit_edit);
        mSendSms = (ThreeStateCheckBox) layout.findViewById(R.id.tc_send_sms);
        mActivateAlert = (ThreeStateCheckBox) layout.findViewById(R.id.tc_activate_alert);
        mActivateInnerSound = (ThreeStateCheckBox) layout.findViewById(R.id.tc_activate_inner_sound);
        mTcModeGroup = (RadioGroup) layout.findViewById(R.id.tc_mode_group);
        mTMin = (EditText) layout.findViewById(R.id.tc_min);
        mTMax = (EditText) layout.findViewById(R.id.tc_max);

        if(mSource.tMin != null)
            mTMin.setText(String.valueOf(mSource.tMin));
        if(mSource.tMax != null)
            mTMax.setText(String.valueOf(mSource.tMax));

        // Handlers
        mTcEnable.setOnStateChangedListener(new ThreeStateButton.OnStateChangedListener()
        {
            @Override
            public void onStateChanged(View v, int newState)
            {
                switch (newState)
                {
                    case ThreeStateButton.STATE_UNKNOWN: mSource.enableTC = null; break;
                    case ThreeStateButton.STATE_NO: mSource.enableTC = false; break;
                    case ThreeStateButton.STATE_YES: mSource.enableTC = true; break;
                }
            }
        });
        mTempLimit.addTextChangedListener(new TextWatcher()
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
                mSource.tempLimit = getValue(s.toString(), 28.0);
            }
        });
        mTcModeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.tc_mode_0:
                        mSource.tempMode = 0;
                        break;
                    case R.id.tc_mode_1:
                        mSource.tempMode = 1;
                        break;
                }
            }
        });
        mSendSms.setOnStateChangedListener(new ThreeStateButton.OnStateChangedListener()
        {
            @Override
            public void onStateChanged(View v, int newState)
            {
                switch (newState)
                {
                    case ThreeStateButton.STATE_UNKNOWN: mSource.tcSendSms = null; break;
                    case ThreeStateButton.STATE_NO: mSource.tcSendSms = false; break;
                    case ThreeStateButton.STATE_YES: mSource.tcSendSms = true; break;
                }
            }
        });
        mActivateAlert.setOnStateChangedListener(new ThreeStateButton.OnStateChangedListener()
        {
            @Override
            public void onStateChanged(View v, int newState)
            {
                switch (newState)
                {
                    case ThreeStateButton.STATE_UNKNOWN: mSource.tcActivateAlert = null; break;
                    case ThreeStateButton.STATE_NO: mSource.tcActivateAlert = false; break;
                    case ThreeStateButton.STATE_YES: mSource.tcActivateAlert = true; break;
                }
            }
        });
        mActivateInnerSound.setOnStateChangedListener(new ThreeStateButton.OnStateChangedListener()
        {
            @Override
            public void onStateChanged(View v, int newState)
            {
                switch (newState)
                {
                    case ThreeStateButton.STATE_UNKNOWN: mSource.tcActivateInnerSound = null; break;
                    case ThreeStateButton.STATE_NO: mSource.tcActivateInnerSound = false; break;
                    case ThreeStateButton.STATE_YES: mSource.tcActivateInnerSound = true; break;
                }
            }
        });
        mTMin.addTextChangedListener(new TextWatcher()
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
                mSource.tMin = getValue(s.toString(), 15.0);
            }
        });
        mTMax.addTextChangedListener(new TextWatcher()
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
                mSource.tMax = getValue(s.toString(), 30.0);
            }
        });

        return layout;
    }
}