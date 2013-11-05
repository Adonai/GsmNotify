package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adonai.GsmNotify.Device;
import com.adonai.GsmNotify.R;

public class SettingsPage5 extends SettingsFragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.settings_fragment_5, container, false);
    }

    @Override
    public String compileDiff(Device source)
    {
        return null;
    }
}