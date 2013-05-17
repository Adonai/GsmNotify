package com.adonai.GsmNotify;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.*;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.gson.Gson;

public class MainActivity extends Activity implements View.OnClickListener
{
    String SENT = "SMS_SENT_NOTIFY_MAIN";
    String DELIVERED = "SMS_DELIVERED_NOTIFY_MAIN";
    MessageQueue incMessages;

    String mAddressID;
    SharedPreferences mPrefs;
    BroadcastReceiver sentReceiver, deliveryReceiver;
    Device mDevice;

    ImageButton mNotifyEnable, mNotifyDisable, mRelay1Enable, mRelay1Disable, mRelay2Enable, mRelay2Disable;
    ImageButton mGetData, mGetTemperature;
    EditText mResultText;

    public  class sentConfirmReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            switch (getResultCode())
            {
                case Activity.RESULT_OK: Toast.makeText(MainActivity.this, getString(R.string.sms_sent_success), Toast.LENGTH_SHORT).show(); break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE: Toast.makeText(MainActivity.this, getString(R.string.generic_failure), Toast.LENGTH_SHORT).show(); break;
                case SmsManager.RESULT_ERROR_NO_SERVICE: Toast.makeText(MainActivity.this, getString(R.string.no_service), Toast.LENGTH_SHORT).show(); break;
                case SmsManager.RESULT_ERROR_NULL_PDU: Toast.makeText(MainActivity.this, getString(R.string.null_message), Toast.LENGTH_SHORT).show(); break;
                case SmsManager.RESULT_ERROR_RADIO_OFF: Toast.makeText(getBaseContext(), getString(R.string.radio_off), Toast.LENGTH_SHORT).show(); break;
            }
        }
    }

    public  class deliveryConfirmReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            switch (getResultCode())
            {
                case Activity.RESULT_OK: Toast.makeText(MainActivity.this, getString(R.string.sms_deliver_success), Toast.LENGTH_SHORT).show(); break;
                case Activity.RESULT_CANCELED: Toast.makeText(MainActivity.this, getString(R.string.result_canceled), Toast.LENGTH_SHORT).show(); break;
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        incMessages = new MessageQueue();
        sentReceiver = new sentConfirmReceiver();
        deliveryReceiver = new deliveryConfirmReceiver();

        mPrefs = getSharedPreferences(SMSReceiveService.PREFERENCES, MODE_PRIVATE);
        mNotifyEnable = (ImageButton) findViewById(R.id.signal_on_button);
        mNotifyDisable = (ImageButton) findViewById(R.id.signal_off_button);
        mRelay1Enable = (ImageButton) findViewById(R.id.relay1_on_button);
        mRelay1Disable = (ImageButton) findViewById(R.id.relay1_off_button);
        mRelay2Enable = (ImageButton) findViewById(R.id.relay2_on_button);
        mRelay2Disable = (ImageButton) findViewById(R.id.relay2_off_button);

        mNotifyEnable.setOnClickListener(this);
        mNotifyDisable.setOnClickListener(this);
        mRelay1Enable.setOnClickListener(this);
        mRelay1Disable.setOnClickListener(this);
        mRelay2Enable.setOnClickListener(this);
        mRelay2Disable.setOnClickListener(this);

        mGetData = (ImageButton) findViewById(R.id.get_data_button);
        mGetData.setOnClickListener(this);
        mGetTemperature = (ImageButton) findViewById(R.id.get_temperature_button);
        mGetTemperature.setOnClickListener(this);

        mResultText = (EditText) findViewById(R.id.result_text);


        if(getIntent().hasExtra("ID")) // запускаем из настроек
        {
            mAddressID = getIntent().getStringExtra("ID");
            extractParams();
        }
        else if(getIntent().hasExtra("number")) // запускаем из ресивера
        {
            mAddressID = getIntent().getStringExtra("number");
            extractParams();

            mResultText.setText(getIntent().getStringExtra("text"));
        } else // запускаем сами
        {
            String[] IDs = mPrefs.getString("IDs", "").split(";");
            if (IDs.length > 1)
            {
                Intent selector = new Intent(this, SelectorActivity.class);
                startActivity(selector);
                finish();
            }
            else if(IDs[0].length() != 0)
            {
                mAddressID = IDs[0];
                extractParams();
            }
            else // first launch
            {
                Toast.makeText(getApplicationContext(), R.string.first_launch, Toast.LENGTH_LONG).show();
                Intent firstLaunch = new Intent(this, SettingsActivity.class);
                startActivity(firstLaunch);
                finish();
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //--- When the SMS has been sent ---
        registerReceiver(sentReceiver, new IntentFilter(SENT));
        //--- When the SMS has been delivered. ---
        registerReceiver(deliveryReceiver, new IntentFilter(DELIVERED));
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliveryReceiver);
    }

    private void extractParams()
    {
        String gson = mPrefs.getString(mAddressID, "");
        if (!gson.equals(""))
        {
            mDevice = new Gson().fromJson(gson, Device.class);
            setTitle(mDevice.name);
        } else
            finish();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        //setIntent(intent);

        if(intent.hasExtra("number"))
        {
            if(intent.getStringExtra("number").equals(mAddressID))
            {
                String newMessage = intent.getStringExtra("text");
                incMessages.add(newMessage);
                mResultText.setTextKeepState(incMessages.toString());
            }
            else
            {
                incMessages.clear();
                incMessages.add(intent.getStringExtra("text"));
                mAddressID = intent.getStringExtra("number");
                extractParams();
                mResultText.setTextKeepState(incMessages.toString());
            }
        }
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
                final String[] IDs = mPrefs.getString("IDs", "").split(";");

                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        switch (i)
                        {
                            case 0: //  add_device
                                startActivity(intent);
                                break;
                            case 1: // remove_device
                            {
                                String IDStrings = mPrefs.getString("IDs", "");
                                SharedPreferences.Editor edit = mPrefs.edit();
                                edit.putString("IDs", IDStrings.replace(mAddressID + ";", ""));
                                edit.remove(mAddressID);
                                edit.commit();

                                if(IDs.length != 1) // если есть еще устройства, даем выбор
                                {
                                    Intent selector = new Intent(MainActivity.this, SelectorActivity.class);
                                    startActivity(selector);
                                }
                                else // если нет, надо добавить
                                    startActivity(intent);
                                break;
                            }
                            case 2: // edit_device
                                startActivity(intent.putExtra("ID", mAddressID));
                                break;
                        }
                        finish();
                    }
                };

                if (IDs.length > 1 || IDs.length == 1 && IDs[0].length() != 0)
                    settingsSelector.setItems(new CharSequence[]{getString(R.string.add_device), getString(R.string.remove_device), getString(R.string.edit_device)}, listener);
                else
                    settingsSelector.setItems(new CharSequence[]{getString(R.string.add_device), getString(R.string.remove_device)}, listener);
                settingsSelector.create().show();

                return true;
        }

        return false;
    }

    @Override
    public void onClick(View view)
    {
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
        SmsManager sms = SmsManager.getDefault();
        switch (view.getId())
        {
            case R.id.relay1_on_button:
                sms.sendTextMessage(mDevice.number, null, "*" + mDevice.password + "#_set_1=1#", sentPI, deliveredPI);
                break;
            case R.id.relay1_off_button:
                sms.sendTextMessage(mDevice.number, null, "*" + mDevice.password + "#_set_1=0#", sentPI, deliveredPI);
                break;
            case R.id.relay2_on_button:
                sms.sendTextMessage(mDevice.number, null, "*" + mDevice.password + "#_set_2=1#", sentPI, deliveredPI);
                break;
            case R.id.relay2_off_button:
                sms.sendTextMessage(mDevice.number, null, "*" + mDevice.password + "#_set_2=0#", sentPI, deliveredPI);
                break;
            case R.id.get_data_button:
                sms.sendTextMessage(mDevice.number, null, "*" + mDevice.password + "#_info#", sentPI, deliveredPI);
                break;
            case R.id.get_temperature_button:
                sms.sendTextMessage(mDevice.number, null, "*" + mDevice.password + "#_temp#", sentPI, deliveredPI);
                break;
            case R.id.signal_on_button:
                sms.sendTextMessage(mDevice.number, null, "*" + mDevice.password + "#_on#", sentPI, deliveredPI);
                break;
            case R.id.signal_off_button:
                sms.sendTextMessage(mDevice.number, null, "*" + mDevice.password + "#_off#", sentPI, deliveredPI);
                break;
        }
    }
}
