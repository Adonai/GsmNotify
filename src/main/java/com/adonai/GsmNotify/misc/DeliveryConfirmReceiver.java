package com.adonai.GsmNotify.misc;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.adonai.GsmNotify.R;

/**
* Created by adonai on 04.02.15.
*/
public class DeliveryConfirmReceiver extends BroadcastReceiver {

    private final Context mContext;

    public DeliveryConfirmReceiver(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                Toast.makeText(mContext, mContext.getString(R.string.sms_deliver_success), Toast.LENGTH_SHORT).show();
                break;
            case Activity.RESULT_CANCELED:
                Toast.makeText(mContext, mContext.getString(R.string.result_canceled), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
