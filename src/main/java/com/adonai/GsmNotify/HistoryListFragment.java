package com.adonai.GsmNotify;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.adonai.GsmNotify.database.DbProvider;
import com.adonai.GsmNotify.entities.HistoryEntry;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by adonai on 26.01.15.
 */
public class HistoryListFragment extends DialogFragment {
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

    private static final String NAME_KEY = "name.key";

    private ListView mListView;

    public HistoryListFragment() {
    }

    public static HistoryListFragment newInstance(String deviceName) {
        HistoryListFragment hlf = new HistoryListFragment();
        Bundle args = new Bundle(1);
        args.putString(NAME_KEY, deviceName);
        hlf.setArguments(args);
        return hlf;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View layout = LayoutInflater.from(getActivity()).inflate(R.layout.history_dialog, null);

        mListView = (ListView) layout.findViewById(R.id.history_listview);
        final String device = getArguments().getString(NAME_KEY);

        try {
            final List<HistoryEntry> entries = DbProvider.getHelper().getHistoryDao().queryBuilder().orderBy("eventDate", false).where().eq("deviceName", device).query();
            ListAdapter entryAdapter = new ArrayAdapter<HistoryEntry>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, entries) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    HistoryEntry item = getItem(position);
                    View itemLayout = super.getView(position, convertView, parent);

                    TextView text = (TextView) itemLayout.findViewById(android.R.id.text1);
                    text.setText(item.getSmsText());

                    TextView date = (TextView) itemLayout.findViewById(android.R.id.text2);
                    date.setText(sdf.format(item.getEventDate()));

                    return itemLayout;
                }
            };
            mListView.setAdapter(entryAdapter);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        View header = LayoutInflater.from(getActivity()).inflate(R.layout.history_dialog_header, null);
        ImageView deleteButton = (ImageView) header.findViewById(R.id.delete_history);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // deleting from database
                try {
                    RuntimeExceptionDao<HistoryEntry, Long> dao = DbProvider.getHelper().getHistoryDao();
                    DeleteBuilder<HistoryEntry, Long> stmt = dao.deleteBuilder();
                    stmt.where().eq("deviceName", device);
                    dao.delete(stmt.prepare());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                HistoryListFragment.this.dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout);
        builder.setCustomTitle(header);
        return builder.create();
    }
}
