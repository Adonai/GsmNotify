package com.adonai.GsmNotify.test;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.Button;
import android.widget.EditText;

import com.adonai.GsmNotify.R;
import com.adonai.GsmNotify.SettingsActivity;

import java.util.Random;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class SelectorActivityTest extends ActivityInstrumentationTestCase2<SettingsActivity> {
    Button mAddDeviceButton;
    EditText mName, mNumber, mPassword;

    public SelectorActivityTest() {
        super(SettingsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAddDeviceButton = (Button) getActivity().findViewById(R.id.device_apply_button);
        mName = (EditText) getActivity().findViewById(R.id.device_name_text);
        mNumber = (EditText) getActivity().findViewById(R.id.device_number_text);
        mPassword = (EditText) getActivity().findViewById(R.id.device_password_text);
    }

    @UiThreadTest
    public void testDeviceCreate() {
        for(int i = 0; i < 50; ++i) {
            mName.setText(randomString());
            mPassword.setText(randomPassword().toString());
            mNumber.setText(randomNumber().toString());
            mAddDeviceButton.performClick();
        }
    }

    private String randomString() {
        final Random rnd = new Random();
        final String chars = "qwertyuiopasdfghjklzxcvbnm";
        final int len = Math.abs(rnd.nextInt() % 10);
        final StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; ++i) {
            sb.append(chars.charAt(Math.abs(rnd.nextInt() % chars.length())));
        }
        return sb.toString();
    }

    private Long randomNumber() {
        return Math.abs(new Random().nextLong() % 99999999999l);
    }

    private Integer randomPassword() {
        return Math.abs(new Random().nextInt() % 9999);
    }
}