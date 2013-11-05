package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adonai.GsmNotify.Device;
import com.adonai.GsmNotify.R;

public class SettingsPage4 extends SettingsFragment
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.settings_fragment_4, container, false);
        assert layout != null;

        return layout;
    }

    @Override
    public String compileDiff(Device source)
    {
        return null;
    }
}