package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.adonai.GsmNotify.Device;
import com.adonai.GsmNotify.R;

public class SettingsPage5 extends SettingsFragment
{
    CheckBox mTcEnable;
    EditText mTempLimit;
    RadioGroup mTcModeGroup;
    RadioGroup mTcOnLimitGroup;
    EditText mTMin, mTMax;

    public SettingsPage5(Device source)
    {
        super(source);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.settings_fragment_5, container, false);
        assert layout != null;

        mTcEnable = (CheckBox) layout.findViewById(R.id.tc_checkbox);
        mTempLimit = (EditText) layout.findViewById(R.id.tc_limit_edit);
        mTcModeGroup = (RadioGroup) layout.findViewById(R.id.tc_mode_group);
        mTcOnLimitGroup = (RadioGroup) layout.findViewById(R.id.tc_on_limit_group);
        mTMin = (EditText) layout.findViewById(R.id.tc_min);
        mTMax = (EditText) layout.findViewById(R.id.tc_max);

        // Initial layout
        if(mSource.enableTC != null)
            mTcEnable.setChecked(mSource.enableTC);
        if(mSource.tempLimit != null)
            mTempLimit.setText(String.valueOf(mSource.tempLimit));
        if(mSource.tempMode != null)
            switch (mSource.tempMode)
            {
                case 0: mTcModeGroup.check(R.id.tc_mode_0); break;
                case 1: mTcModeGroup.check(R.id.tc_mode_1); break;
            }
        if(mSource.onLimitReach != null)
            switch (mSource.onLimitReach)
            {
                case 1: mTcOnLimitGroup.check(R.id.tc_on_limit_1); break;
                case 2: mTcOnLimitGroup.check(R.id.tc_on_limit_2); break;
                case 3: mTcOnLimitGroup.check(R.id.tc_on_limit_3); break;
            }
        if(mSource.tMin != null)
            mTMin.setText(String.valueOf(mSource.tMin));
        if(mSource.tMax != null)
            mTMax.setText(String.valueOf(mSource.tMax));

        // Handlers
        mTcEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                mSource.enableTC = isChecked;
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
                mSource.tempLimit = getValue(s.toString(), 20);
            }
        });
        mTcModeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.tc_mode_0: mSource.tempMode = 0; break;
                    case R.id.tc_mode_1: mSource.tempMode = 1; break;
                }
            }
        });
        mTcOnLimitGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.tc_on_limit_1: mSource.onLimitReach = 1; break;
                    case R.id.tc_on_limit_2: mSource.onLimitReach = 2; break;
                    case R.id.tc_on_limit_3: mSource.onLimitReach = 3; break;
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
                mSource.tMin = getValue(s.toString(), 15);
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
                mSource.tMax = getValue(s.toString(), 25);
            }
        });

        return layout;
    }
}