package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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

        return layout;
    }

    @Override
    public String compileDiff(Device source)
    {
        return null;
    }
}