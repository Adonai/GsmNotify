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
import android.widget.ViewFlipper;

import com.adonai.GsmNotify.Device;
import com.adonai.GsmNotify.R;

public class SettingsPage3 extends SettingsFragment
{
    RadioGroup mInputNum;
    ViewFlipper mFlipper;
    EditText mTimeToWait, mTimeToRearm;
    CheckBox mConstantControl, mInnerSound;
    EditText mSmsText;

    Integer mCurrentInput;

    public SettingsPage3(Device source)
    {
        super(source);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.settings_fragment_3, container, false);
        assert layout != null;

        mFlipper = (ViewFlipper) layout.findViewById(R.id.input_flipper);

        mInputNum = (RadioGroup) layout.findViewById(R.id.input_selector);
        mInputNum.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.input_1_radio: mFlipper.setDisplayedChild(0); break;
                    case R.id.input_2_radio: mFlipper.setDisplayedChild(1); break;
                    case R.id.input_3_radio: mFlipper.setDisplayedChild(2); break;
                    case R.id.input_4_radio: mFlipper.setDisplayedChild(3); break;
                }
            }
        });

        for(int i = 0; i < mInputNum.getChildCount(); ++i)
        {
            final Device.InputSettings curr = mSource.inputs[i];

            View input = inflater.inflate(R.layout.input_setting, mFlipper, false);
            assert input != null;
            mFlipper.addView(input, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            mTimeToWait = (EditText) input.findViewById(R.id.time_to_wait_edit);
            mTimeToRearm = (EditText) input.findViewById(R.id.time_to_rearm_edit);
            mConstantControl = (CheckBox) input.findViewById(R.id.constant_control_check);
            mInnerSound = (CheckBox) input.findViewById(R.id.inner_sound_check);
            mSmsText = (EditText) input.findViewById(R.id.sms_text_edit);

            // Initial layout
            if(curr.timeToWaitBeforeCall != null)
                mTimeToWait.setText(String.valueOf(curr.timeToWaitBeforeCall));
            if(curr.timeToRearm != null)
                mTimeToRearm.setText(String.valueOf(curr.timeToRearm));
            if(curr.constantControl != null)
                mConstantControl.setChecked(curr.constantControl);
            if(curr.innerSound != null)
                mInnerSound.setChecked(curr.innerSound);
            if(curr.smsText != null)
                mSmsText.setText(curr.smsText);

            // Handlers
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
                    curr.timeToWaitBeforeCall = getValue(s.toString(), 0);
                }
            });
            mTimeToRearm.addTextChangedListener(new TextWatcher()
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
                    curr.timeToRearm = getValue(s.toString(), 0);
                }
            });
            mConstantControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    curr.constantControl = isChecked;
                }
            });
            mInnerSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    curr.innerSound = isChecked;
                }
            });
            mSmsText.addTextChangedListener(new TextWatcher()
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
                    curr.smsText = s.toString();
                }
            });
        }

        mInputNum.check(R.id.input_1_radio);

        return layout;
    }
}