package com.adonai.GsmNotify.settings;

import android.support.v4.app.Fragment;

import com.adonai.GsmNotify.Device;

import java.lang.reflect.Method;

public abstract class SettingsFragment extends Fragment
{
    public <T extends Object> T getValue(String value, T defaultValue)
    {
        T result;
        try
        {
            final Method valueOf = defaultValue.getClass().getMethod("valueOf", String.class);
            result = (T) valueOf.invoke(null, value);
            return result;
        } catch (Exception e)
        {
            result = defaultValue;
        }
        return result;
    }

    public abstract void resetUI(final Device source);

    public abstract void compileDiff(final Device source);
}
