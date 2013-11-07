package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.adonai.GsmNotify.Device;
import com.adonai.GsmNotify.R;

public class SettingsPage4 extends SettingsFragment
{

    RadioGroup mOutputNum;
    RadioGroup mOutputMode;
    EditText mEnableOnDisarm, mEnableOnAlert;

    Integer mCurrentOutput;

    public SettingsPage4(Device source)
    {
        super(source);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.settings_fragment_4, container, false);
        assert layout != null;

        mOutputNum = (RadioGroup) layout.findViewById(R.id.output_selector);
        mOutputNum.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.output_1_radio: resetOutput(0); break;
                    case R.id.output_2_radio: resetOutput(1); break;
                }
            }
        });
        mOutputMode = (RadioGroup) layout.findViewById(R.id.output_mode_radios);

        mEnableOnDisarm = (EditText) layout.findViewById(R.id.enable_on_disarm_edit);
        mEnableOnAlert = (EditText) layout.findViewById(R.id.enable_on_alert_edit);

        mOutputNum.check(R.id.output_1_radio);

        return layout;
    }

    public void resetOutput(int index)
    {
        if(mCurrentOutput != null) // not init start
            compileDiff();

        mCurrentOutput = index;

        Device.OutputSettings curr = mSource.outputs[mCurrentOutput];
        switch (curr.outputMode)
        {
            case 1: mOutputMode.check(R.id.output_1_mode_radio); break;
            case 2: mOutputMode.check(R.id.output_2_mode_radio); break;
            case 3: mOutputMode.check(R.id.output_3_mode_radio); break;
            case 4: mOutputMode.check(R.id.output_4_mode_radio); break;
        }

        mEnableOnAlert.setText(curr.timeToEnableOnAlert);
        mEnableOnDisarm.setText(curr.timeToEnableOnDisarm);
    }

    @Override
    public void compileDiff()
    {
        Device.OutputSettings curr = mSource.outputs[mCurrentOutput];

        switch (mOutputMode.getCheckedRadioButtonId())
        {
            case R.id.output_1_mode_radio: curr.outputMode = 1; break;
            case R.id.output_2_mode_radio: curr.outputMode = 2; break;
            case R.id.output_3_mode_radio: curr.outputMode = 3; break;
            case R.id.output_4_mode_radio: curr.outputMode = 4; break;
        }

        curr.timeToEnableOnAlert = getValue(mEnableOnAlert.getText().toString(), 0);
        curr.timeToEnableOnDisarm = getValue(mEnableOnDisarm.getText().toString(), 0);
    }
}