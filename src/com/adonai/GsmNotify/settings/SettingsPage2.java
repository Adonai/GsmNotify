package com.adonai.GsmNotify.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

        // Initial layout
        for (int i = 0; i < mSource.phones.length; ++i)
        {
            TableRow currentRow = (TableRow) mPhones.getChildAt(i + 1);
            if(mSource.phones[i].phoneNum != null)
                ((EditText)currentRow.getChildAt(1)).setText(mSource.phones[i].phoneNum);
            if(mSource.phones[i].info != null)
                ((CheckBox)currentRow.getChildAt(2)).setChecked(mSource.phones[i].info);
            if(mSource.phones[i].manage != null)
                ((CheckBox)currentRow.getChildAt(3)).setChecked(mSource.phones[i].manage);
            if(mSource.phones[i].confirm != null)
                ((CheckBox)currentRow.getChildAt(4)).setChecked(mSource.phones[i].confirm);
        }
        if(mSource.recallCycles != null)
            mRecallCycles.setText(mSource.recallCycles);
        if(mSource.recallWait != null)
            mRecallWait.setText(mSource.recallWait);
        if(mSource.checkBalanceNum != null)
            mBalanceNumber.setText(mSource.checkBalanceNum);

        // Handlers
        for (int i = 0; i < mSource.phones.length; ++i)
        {
            final int index = i;
            TableRow currentRow = (TableRow) mPhones.getChildAt(i + 1);
            ((EditText)currentRow.getChildAt(1)).addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {

                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    mSource.phones[index].phoneNum = s.toString();
                }
            });
            ((CheckBox)currentRow.getChildAt(2)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    mSource.phones[index].info = isChecked;
                }
            });
            ((CheckBox)currentRow.getChildAt(3)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    mSource.phones[index].manage = isChecked;
                }
            });
            ((CheckBox)currentRow.getChildAt(4)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    mSource.phones[index].confirm = isChecked;
                }
            });
        }
        mRecallCycles.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                mSource.recallCycles = getValue(s.toString(), 1);
            }
        });
        mRecallWait.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                mSource.recallWait = getValue(s.toString(), 30);
            }
        });
        mBalanceNumber.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                mSource.checkBalanceNum = s.toString();
            }
        });

        return layout;
    }

    @Override
    public void compileDiff()
    {

    }
}