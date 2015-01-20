package com.adonai.GsmNotify;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
}
