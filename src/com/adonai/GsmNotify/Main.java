package com.adonai.GsmNotify;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class Main extends Activity implements View.OnClickListener
{
    ToggleButton mNotify, mRelay1, mRelay2;
    Button mGetData, mGetTemperature;
    EditText mResultText;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mNotify = (ToggleButton) findViewById(R.id.notify_switch);
        mRelay1 = (ToggleButton) findViewById(R.id.relay_1_switch);
        mRelay2 = (ToggleButton) findViewById(R.id.relay_2_switch);

        mGetData = (Button) findViewById(R.id.get_data_button);
        mGetData.setOnClickListener(this);
        mGetTemperature = (Button) findViewById(R.id.get_temperature_button);
        mGetTemperature.setOnClickListener(this);

        mResultText = (EditText) findViewById(R.id.result_text);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.settings_menu:
                AlertDialog.Builder settingsSelector = new AlertDialog.Builder(this);

                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        Intent intent = new Intent(Main.this, SettingsActivity.class);
                        if(i == 0)
                            startActivity(intent);
                        else
                            startActivity(intent.putExtra("ID", getIntent().getIntExtra("ID", -1)));
                    }
                };

                if (getIntent().hasExtra("ID"))
                    settingsSelector.setItems(new CharSequence[]{getString(R.string.add_device), getString(R.string.edit_device)}, listener);
                else
                    settingsSelector.setItems(new CharSequence[]{getString(R.string.add_device)}, listener);
                settingsSelector.create().show();

                return true;
        }

        return false;
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.get_data_button:
                break;
            case R.id.get_temperature_button:
                break;
        }
    }
}
