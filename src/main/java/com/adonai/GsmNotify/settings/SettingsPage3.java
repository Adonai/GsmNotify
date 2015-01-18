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
import com.adonai.contrib.ThreeStateButton;
import com.adonai.contrib.ThreeStateCheckBox;

public class SettingsPage3 extends SettingsFragment {
    RadioGroup mInputNum;
    ViewFlipper mFlipper;
    EditText mTimeToWait, mTimeToRearm;
    ThreeStateCheckBox mConstantControl, mInnerSound;
    EditText mSmsText;

    Integer mCurrentInput;

    public static SettingsPage3 newInstance(Device source) {
        SettingsPage3 fragment = new SettingsPage3();
        fragment.mSource = source;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.settings_fragment_3, container, false);
        assert layout != null;

        mFlipper = (ViewFlipper) layout.findViewById(R.id.input_flipper);

        mInputNum = (RadioGroup) layout.findViewById(R.id.input_selector);
        mInputNum.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.input_1_radio:
                        mFlipper.setDisplayedChild(0);
                        break;
                    case R.id.input_2_radio:
                        mFlipper.setDisplayedChild(1);
                        break;
                    case R.id.input_3_radio:
                        mFlipper.setDisplayedChild(2);
                        break;
                    case R.id.input_4_radio:
                        mFlipper.setDisplayedChild(3);
                        break;
                }
            }
        });

        for (int i = 0; i < mInputNum.getChildCount(); ++i) {
            final Device.InputSettings curr = mSource.inputs[i];

            View input = inflater.inflate(R.layout.input_setting, mFlipper, false);
            assert input != null;
            mFlipper.addView(input, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            mTimeToWait = (EditText) input.findViewById(R.id.time_to_wait_edit);
            mTimeToRearm = (EditText) input.findViewById(R.id.time_to_rearm_edit);
            mConstantControl = (ThreeStateCheckBox) input.findViewById(R.id.constant_control_check);
            mInnerSound = (ThreeStateCheckBox) input.findViewById(R.id.inner_sound_check);
            mSmsText = (EditText) input.findViewById(R.id.sms_text_edit);

            // Handlers
            mTimeToWait.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    curr.timeToWaitBeforeCall = getValue(s.toString(), 0);
                }
            });
            mTimeToRearm.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    curr.timeToRearm = getValue(s.toString(), 0);
                }
            });
            mConstantControl.setOnStateChangedListener(new ThreeStateButton.OnStateChangedListener() {
                @Override
                public void onStateChanged(View v, int newState) {
                    switch (newState) {
                        case ThreeStateButton.STATE_UNKNOWN:
                            curr.constantControl = null;
                            break;
                        case ThreeStateButton.STATE_NO:
                            curr.constantControl = false;
                            break;
                        case ThreeStateButton.STATE_YES:
                            curr.constantControl = true;
                            break;
                    }
                }
            });
            mInnerSound.setOnStateChangedListener(new ThreeStateButton.OnStateChangedListener() {
                @Override
                public void onStateChanged(View v, int newState) {
                    switch (newState) {
                        case ThreeStateButton.STATE_UNKNOWN:
                            curr.innerSound = null;
                            break;
                        case ThreeStateButton.STATE_NO:
                            curr.innerSound = false;
                            break;
                        case ThreeStateButton.STATE_YES:
                            curr.innerSound = true;
                            break;
                    }
                }
            });
            mSmsText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    curr.smsText = s.toString();
                }
            });
        }

        mInputNum.check(R.id.input_1_radio);

        return layout;
    }
}