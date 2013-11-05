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

public class SettingsPage3 extends SettingsFragment
{
    RadioGroup mInputNum;
    EditText mTimeToWait, mTimeToRearm;
    CheckBox mConstantControl, mInnerSound;
    EditText mSmsText;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.settings_fragment_3, container, false);
        assert layout != null;

        mInputNum = (RadioGroup) layout.findViewById(R.id.input_selector);

        mTimeToWait = (EditText) layout.findViewById(R.id.time_to_wait_edit);
        mTimeToRearm = (EditText) layout.findViewById(R.id.time_to_rearm_edit);

        mConstantControl = (CheckBox) layout.findViewById(R.id.constant_control_check);
        mInnerSound = (CheckBox) layout.findViewById(R.id.inner_sound_check);

        mSmsText = (EditText) layout.findViewById(R.id.sms_text_edit);

        return layout;
    }

    @Override
    public String compileDiff(Device source)
    {
        return null;
    }
}