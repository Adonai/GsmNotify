package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.adonai.GsmNotify.R;

public class SettingsPage4 extends Fragment
{

    RadioGroup mOutputNum;
    RadioGroup mOutputMode;
    EditText mEnableOnUnarm, mEnableOnAlarm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.settings_fragment_4, container, false);
        assert layout != null;

        mOutputNum = (RadioGroup) layout.findViewById(R.id.output_selector);
        mOutputMode = (RadioGroup) layout.findViewById(R.id.output_mode_radios);

        mEnableOnUnarm = (EditText) layout.findViewById(R.id.enable_on_unarm_edit);
        mEnableOnAlarm = (EditText) layout.findViewById(R.id.enable_on_alarm_edit);

        return layout;
    }
}