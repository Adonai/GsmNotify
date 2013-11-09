package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ViewFlipper;

import com.adonai.GsmNotify.Device;
import com.adonai.GsmNotify.R;

public class SettingsPage4 extends SettingsFragment
{

    RadioGroup mOutputNum;
    RadioGroup mOutputMode;
    EditText mEnableOnDisarm, mEnableOnAlert;
    ViewFlipper mFlipper;
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

        mFlipper = (ViewFlipper) layout.findViewById(R.id.output_flipper);

        mOutputNum = (RadioGroup) layout.findViewById(R.id.output_selector);
        mOutputNum.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.output_1_radio: mFlipper.setDisplayedChild(0); break;
                    case R.id.output_2_radio: mFlipper.setDisplayedChild(1); break;
                }
            }
        });

        for(int i = 0; i < mOutputNum.getChildCount(); ++i)
        {
            final Device.OutputSettings curr = mSource.outputs[i];

            View output = inflater.inflate(R.layout.output_setting, mFlipper, false);
            assert output != null;
            mFlipper.addView(output, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            mOutputMode = (RadioGroup) output.findViewById(R.id.output_mode_radios);
            mEnableOnDisarm = (EditText) output.findViewById(R.id.enable_on_disarm_edit);
            mEnableOnAlert = (EditText) output.findViewById(R.id.enable_on_alert_edit);


            // Initial layout
            if(curr.outputMode != null)
                switch (curr.outputMode)
                {
                    case 1: mOutputMode.check(R.id.output_1_mode_radio); break;
                    case 2: mOutputMode.check(R.id.output_2_mode_radio); break;
                    case 3: mOutputMode.check(R.id.output_3_mode_radio); break;
                    case 4: mOutputMode.check(R.id.output_4_mode_radio); break;
                }
            if(curr.timeToEnableOnAlert != null)
                mEnableOnAlert.setText(curr.timeToEnableOnAlert);
            if(curr.timeToEnableOnDisarm != null)
                mEnableOnDisarm.setText(curr.timeToEnableOnDisarm);

            // Handlers
            mOutputMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId)
                {
                    switch (checkedId)
                    {
                        case R.id.output_1_mode_radio: curr.outputMode = 1; break;
                        case R.id.output_2_mode_radio: curr.outputMode = 2; break;
                        case R.id.output_3_mode_radio: curr.outputMode = 3; break;
                        case R.id.output_4_mode_radio: curr.outputMode = 4; break;
                    }
                }
            });
            mEnableOnDisarm.addTextChangedListener(new TextWatcher()
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
                    curr.timeToEnableOnAlert = getValue(s.toString(), 0);
                }
            });
            mEnableOnAlert.addTextChangedListener(new TextWatcher()
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
                    curr.timeToEnableOnDisarm = getValue(s.toString(), 0);
                }
            });
        }

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

        curr.timeToEnableOnDisarm = getValue(mEnableOnDisarm.getText().toString(), 0);
    }
}