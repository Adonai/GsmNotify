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

        mTcEnable.setChecked(mSource.enableTC);
        mTempLimit.setText(mSource.tempLimit);
        switch (mSource.tempMode)
        {
            case 0: mTcModeGroup.check(R.id.tc_mode_0); break;
            case 1: mTcModeGroup.check(R.id.tc_mode_1); break;
        }
        switch (mSource.onLimitReach)
        {
            case 1: mTcOnLimitGroup.check(R.id.tc_on_limit_1); break;
            case 2: mTcOnLimitGroup.check(R.id.tc_on_limit_2); break;
            case 3: mTcOnLimitGroup.check(R.id.tc_on_limit_3); break;
        }
        mTMin.setText(mSource.tMin);
        mTMax.setText(mSource.tMax);

        return layout;
    }

    @Override
    public void compileDiff()
    {
        mSource.enableTC = mTcEnable.isChecked();
        mSource.tempLimit = getValue(mTempLimit.getText().toString(), 20);

        switch (mTcModeGroup.getCheckedRadioButtonId())
        {
            case R.id.tc_mode_0: mSource.tempMode = 0; break;
            case R.id.tc_mode_1: mSource.tempMode = 1; break;
        }

        switch (mTcOnLimitGroup.getCheckedRadioButtonId())
        {
            case R.id.tc_on_limit_1: mSource.onLimitReach = 1; break;
            case R.id.tc_on_limit_2: mSource.onLimitReach = 2; break;
            case R.id.tc_on_limit_3: mSource.onLimitReach = 3; break;
        }

        mSource.tMin = getValue(mTMin.getText().toString(), 15);
        mSource.tMax = getValue(mTMax.getText().toString(), 25);
    }
}