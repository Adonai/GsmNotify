package com.adonai.GsmNotify;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.gson.Gson;

public class SelectorActivity extends Activity implements View.OnClickListener {
    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences(SMSReceiveService.PREFERENCES, MODE_PRIVATE);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout deviceList = new LinearLayout(this);
        deviceList.setOrientation(LinearLayout.VERTICAL);

        String[] IDs = mPrefs.getString("IDs", "").split(";");
        for (String ID : IDs) {
            String gson = mPrefs.getString(ID, "");
            if (gson.equals("")) {
                continue;
            }

            Device dev = new Device();
            dev.details = new Gson().fromJson(gson, Device.CommonSettings.class);
            Button viewer = new Button(this);
            viewer.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            viewer.setText(dev.details.name);
            viewer.setTag(ID);
            viewer.setOnClickListener(this);
            deviceList.addView(viewer);
        }
        Button addNew = new Button(this);
        addNew.setText(R.string.add_device);
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectorActivity.this, SettingsActivity.class);
                startActivity(intent);
                //finish();
            }
        });
        deviceList.addView(addNew);
        scrollView.addView(deviceList);

        setContentView(scrollView);
    }

    @Override
    public void onClick(View v) {
        Intent starter = new Intent(this, MainActivity.class).putExtra("ID", v.getTag().toString());
        startActivity(starter);
        //finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.selector_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem smsOption = menu.findItem(R.id.notify_on_sms);
        boolean shouldOpen = mPrefs.getBoolean(SMSReceiveService.OPEN_ON_SMS_KEY, true);
        smsOption.setChecked(shouldOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notify_on_sms:
                boolean shouldOpen = mPrefs.getBoolean(SMSReceiveService.OPEN_ON_SMS_KEY, true);
                shouldOpen = !shouldOpen;

                // write to prefs
                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putBoolean(SMSReceiveService.OPEN_ON_SMS_KEY, shouldOpen);
                edit.commit();

                // update menu checked state
                invalidateOptionsMenu();
                return true;
        }

        return false;
    }
}
