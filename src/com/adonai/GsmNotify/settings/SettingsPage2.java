package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.adonai.GsmNotify.Device;
import com.adonai.GsmNotify.R;

public class SettingsPage2 extends SettingsFragment
{
    TableLayout mPhones;
    EditText mRecallCycles, mRecallWait, mBalanceNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        View layout = inflater.inflate(R.layout.settings_fragment_2, container, false);
        assert layout != null;

        mPhones = (TableLayout) layout.findViewById(R.id.phones_table);

        mRecallCycles = (EditText) layout.findViewById(R.id.recall_cycles_edit);
        mRecallWait = (EditText) layout.findViewById(R.id.call_length_edit);
        mBalanceNumber = (EditText) layout.findViewById(R.id.balance_number_edit);

        return layout;
    }

    @Override
    public void resetUI(Device source)
    {

    }

    @Override
    public void compileDiff(Device source)
    {
        for (int i = 0; i < source.phones.length; ++i)
        {
            TableRow currentRow = (TableRow) mPhones.getChildAt(i + 1);
            source.phones[i].phoneNum = ((EditText)currentRow.getChildAt(1)).getText().toString();
            source.phones[i].info = ((CheckBox)currentRow.getChildAt(2)).isChecked();
            source.phones[i].manage = ((CheckBox)currentRow.getChildAt(3)).isChecked();
            source.phones[i].confirm = ((CheckBox)currentRow.getChildAt(4)).isChecked();
        }

        source.recallCycles = getValue(mRecallCycles.getText().toString(), 1);
        source.recallWait = getValue(mRecallWait.getText().toString(), 30);
        source.checkBalanceNum = mBalanceNumber.getText().toString();
    }
}