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

    Device.InputSettings[] mInnerDIS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.settings_fragment_3, container, false);
        assert layout != null;

        mInputNum = (RadioGroup) layout.findViewById(R.id.input_selector);
        mInputNum.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.input_1_radio: resetInput(0); break;
                    case R.id.input_2_radio: resetInput(1); break;
                    case R.id.input_3_radio: resetInput(2); break;
                    case R.id.input_4_radio: resetInput(3); break;
                }
            }
        });

        mTimeToWait = (EditText) layout.findViewById(R.id.time_to_wait_edit);
        mTimeToRearm = (EditText) layout.findViewById(R.id.time_to_rearm_edit);

        mConstantControl = (CheckBox) layout.findViewById(R.id.constant_control_check);
        mInnerSound = (CheckBox) layout.findViewById(R.id.inner_sound_check);

        mSmsText = (EditText) layout.findViewById(R.id.sms_text_edit);

        return layout;
    }

    @Override
    public void resetUI(Device source)
    {
        mInnerDIS = source.inputs.clone();
        mInputNum.check(R.id.input_1_radio);
    }

    public void resetInput(int index)
    {

    }

    @Override
    public void compileDiff(Device source)
    {
        source.inputs = mInnerDIS.clone();
    }
}