package com.adonai.GsmNotify.misc;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.adonai.GsmNotify.R;

/**
* Created by adonai on 04.02.15.
*/
public class SentConfirmReceiver extends BroadcastReceiver {

    private final Context mContext;
    
    public SentConfirmReceiver(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                Toast.makeText(mContext, mContext.getString(R.string.sms_sent_success), Toast.LENGTH_SHORT).show();
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Toast.makeText(mContext, mContext.getString(R.string.generic_failure), Toast.LENGTH_SHORT).show();
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                Toast.makeText(mContext, mContext.getString(R.string.no_service), Toast.LENGTH_SHORT).show();
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                Toast.makeText(mContext, mContext.getString(R.string.null_message), Toast.LENGTH_SHORT).show();
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                Toast.makeText(mContext, mContext.getString(R.string.radio_off), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
