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
import android.widget.Toast;

import com.adonai.GsmNotify.database.DbProvider;
import com.adonai.GsmNotify.database.PersistManager;
import com.adonai.GsmNotify.entities.HistoryEntry;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Shows and archives history for alarms
 */
public class AlarmHistoryListFragment extends DialogFragment {
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

    private ListView mListView;

    public AlarmHistoryListFragment() {
    }

    public static AlarmHistoryListFragment newInstance() {
        AlarmHistoryListFragment hlf = new AlarmHistoryListFragment();
        Bundle args = new Bundle(1);
        hlf.setArguments(args);
        return hlf;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View layout = LayoutInflater.from(getActivity()).inflate(R.layout.history_dialog, null);

        mListView = (ListView) layout.findViewById(R.id.history_listview);
        try {
            PersistManager manager = DbProvider.getTempHelper(getActivity());
            final List<HistoryEntry> entries = manager.getHistoryDao().queryBuilder()
                    .orderBy("eventDate", false)
                    .where().eq("status", Utils.DeviceStatus.ALARM)
                    .and().eq("archived", false)
                    .query();
            DbProvider.releaseTempHelper(); // it's ref-counted thus will not close if activity uses it...
            ListAdapter entryAdapter = new ArrayAdapter<HistoryEntry>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, entries) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    HistoryEntry item = getItem(position);
                    View itemLayout = super.getView(position, convertView, parent);

                    TextView text = (TextView) itemLayout.findViewById(android.R.id.text1);
                    text.setText(item.getDeviceName() + ": " + item.getSmsText());

                    TextView date = (TextView) itemLayout.findViewById(android.R.id.text2);
                    date.setText(sdf.format(item.getEventDate()));

                    return itemLayout;
                }
            };
            mListView.setAdapter(entryAdapter);
        } catch (SQLException e) {
            Toast.makeText(getActivity(), R.string.db_cant_query_history, Toast.LENGTH_LONG).show();
        }

        View header = LayoutInflater.from(getActivity()).inflate(R.layout.history_dialog_header, null);
        TextView title = (TextView) header.findViewById(R.id.header_title);
        title.setText(R.string.alarm_history);
        ImageView deleteButton = (ImageView) header.findViewById(R.id.delete_history);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // deleting from database
                try {
                    PersistManager manager = DbProvider.getTempHelper(getActivity());
                    RuntimeExceptionDao<HistoryEntry, Long> dao = manager.getHistoryDao();

                    // put alarm to archive
                    List<HistoryEntry> alarmEntries = dao.queryBuilder()
                            .where().eq("status", Utils.DeviceStatus.ALARM)
                            .and().eq("archived", false)
                            .query();
                    for(HistoryEntry alarm : alarmEntries) {
                        String originalText = alarm.getSmsText();
                        String archiveSuffix =  getString(R.string.archive_suffix);

                        alarm.setSmsText(originalText + " " + archiveSuffix);
                        alarm.setArchived(true);
                        dao.update(alarm);
                    }
                    
                    DbProvider.releaseTempHelper(); // it's ref-counted thus will not close if activity uses it...
                    if(Utils.isTablet(getActivity())) {
                        getActivity().getLoaderManager().getLoader(SelectorActivity.STATUS_LOADER).onContentChanged();
                    }
                } catch (SQLException e) {
                    Toast.makeText(getActivity(), R.string.db_cant_query_history, Toast.LENGTH_LONG).show();
                }

                AlarmHistoryListFragment.this.dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout);
        builder.setCustomTitle(header);
        return builder.create();
    }
}
