package com.adonai.GsmNotify;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.google.gson.Gson;

public class SelectorActivity extends Activity implements View.OnClickListener
{
    SharedPreferences mPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences(SMSReceiveService.PREFERENCES, MODE_PRIVATE);

        LinearLayout LL = new LinearLayout(this);
        LL.setOrientation(LinearLayout.VERTICAL);

        String[] IDs = mPrefs.getString("IDs", "").split(";");
        Gson conv = new Gson();
        for(String ID : IDs)
        {
            String gson = mPrefs.getString(ID, "");
            if(gson.equals(""))
                continue;

            Device dev = conv.fromJson(gson, Device.class);
            Button viewer = new Button(this);
            viewer.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            viewer.setText(dev.details.name);
            viewer.setTag(ID);
            viewer.setOnClickListener(this);
            LL.addView(viewer);
        }
        Button addNew = new Button(this);
        addNew.setText(R.string.add_device);
        addNew.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SelectorActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        LL.addView(addNew);


        setContentView(LL);

}

    @Override
    public void onClick(View v)
    {
        Intent starter = new Intent(this, MainActivity.class).putExtra("ID", v.getTag().toString());
        startActivity(starter);
        finish();
    }
}
