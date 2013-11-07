package com.adonai.GsmNotify.settings;

import android.os.Bundle;
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

    public SettingsPage2(Device source)
    {
        super(source);
    }

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

        for (int i = 0; i < mPhones.getChildCount(); ++i)
        {
            TableRow currentRow = (TableRow) mPhones.getChildAt(i + 1);
            ((EditText)currentRow.getChildAt(1)).setText(mSource.phones[i].phoneNum);
            ((CheckBox)currentRow.getChildAt(2)).setChecked(mSource.phones[i].info);
            ((CheckBox)currentRow.getChildAt(3)).setChecked(mSource.phones[i].manage);
            ((CheckBox)currentRow.getChildAt(4)).setChecked(mSource.phones[i].confirm);
        }
        mRecallCycles.setText(mSource.recallCycles);
        mRecallWait.setText(mSource.recallWait);
        mBalanceNumber.setText(mSource.checkBalanceNum);

        return layout;
    }

    @Override
    public void compileDiff()
    {
        for (int i = 0; i < mSource.phones.length; ++i)
        {
            TableRow currentRow = (TableRow) mPhones.getChildAt(i + 1);
            mSource.phones[i].phoneNum = ((EditText)currentRow.getChildAt(1)).getText().toString();
            mSource.phones[i].info = ((CheckBox)currentRow.getChildAt(2)).isChecked();
            mSource.phones[i].manage = ((CheckBox)currentRow.getChildAt(3)).isChecked();
            mSource.phones[i].confirm = ((CheckBox)currentRow.getChildAt(4)).isChecked();
        }

        mSource.recallCycles = getValue(mRecallCycles.getText().toString(), 1);
        mSource.recallWait = getValue(mRecallWait.getText().toString(), 30);
        mSource.checkBalanceNum = mBalanceNumber.getText().toString();
    }
}