package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adonai.GsmNotify.Device;
import com.adonai.GsmNotify.R;
import com.adonai.contrib.ThreeStateButton;

public class SettingsPage6 extends SettingsFragment
{
    public static SettingsPage6 newInstance(Device source) {
        SettingsPage6 fragment = new SettingsPage6();
        fragment.mSource = source;
        return fragment;
    }

    ThreeStateButton mTempReport, mInfoReport;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.settings_fragment_6, container, false);
        assert layout != null;

        mTempReport = (ThreeStateButton) layout.findViewById(R.id.temp_report_switch);
        mInfoReport = (ThreeStateButton) layout.findViewById(R.id.info_report_switch);

        // Handlers
        mTempReport.setOnStateChangedListener(new ThreeStateButton.OnStateChangedListener()
        {
            @Override
            public void onStateChanged(View v, int newState)
            {
                switch (newState)
                {
                    case ThreeStateButton.STATE_UNKNOWN: mSource.enableTempReport = null; break;
                    case ThreeStateButton.STATE_NO: mSource.enableTempReport = false; break;
                    case ThreeStateButton.STATE_YES: mSource.enableTempReport = true; break;
                }
            }
        });
        mInfoReport.setOnStateChangedListener(new ThreeStateButton.OnStateChangedListener()
        {
            @Override
            public void onStateChanged(View v, int newState)
            {
                switch (newState)
                {
                    case ThreeStateButton.STATE_UNKNOWN: mSource.enableInfoReport = null; break;
                    case ThreeStateButton.STATE_NO: mSource.enableInfoReport = false; break;
                    case ThreeStateButton.STATE_YES: mSource.enableInfoReport = true; break;
                }
            }
        });

        return layout;
    }
}
