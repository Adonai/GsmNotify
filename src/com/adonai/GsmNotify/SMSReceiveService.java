package com.adonai.GsmNotify;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public class SMSReceiveService extends Service
{
    final public static String PREFERENCES = "devicePrefs";
    Activity boundListener;
    SharedPreferences preferences;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        boundListener = null;
        return false;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent != null && intent.hasExtra("number"))
        {
            String[] IDs = preferences.getString("IDs", "").split(";");
            String current = preferences.getString("currentEdit", "");
            for (String ID : IDs)
                if(intent.getStringExtra("number").endsWith(ID.substring(1)) && !ID.equals(current)) // +7 / 8 handling
                {
                    Intent starter = new Intent(this, MainActivity.class);
                    starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("number", ID).putExtra("text", intent.getStringExtra("text"));
                    startActivity(starter);
                }
        }
        return START_STICKY;
    }
}
