package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.adonai.GsmNotify.Device;
import com.adonai.GsmNotify.R;
import com.adonai.GsmNotify.SettingsActivity;

/**
 * Created by adonai on 26.11.13.
 */
public class SettingsPage7 extends SettingsFragment {
    public static SettingsPage7 newInstance(Device source) {
        SettingsPage7 fragment = new SettingsPage7();
        fragment.mSource = source;
        return fragment;
    }

    Button mReboot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.settings_fragment_7, container, false);
        assert layout != null;

        mReboot = (Button) layout.findViewById(R.id.reset_device_button);

        // Handlers
        mReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof SettingsActivity) {
                    Message reset = new Message();
                    reset.what = SettingsActivity.HANDLE_FORCE_RESET;
                    ((SettingsActivity) getActivity()).handleMessage(reset);
                }
            }
        });

        return layout;
    }
}
