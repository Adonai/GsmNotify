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
 * Created by adonai on 26.01.15.
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
            final List<HistoryEntry> entries = manager.getHistoryDao().queryBuilder().orderBy("eventDate", false)
                    .where()
                    .like("smsText", getActivity().getString(R.string.alarm_db_matcher)) // usual
                    .or()
                    .like("smsText", getActivity().getString(R.string.alarm_db_matcher_qaud)) // GSM Qaud
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
                    String dbMatcher = getString(R.string.alarm_db_matcher);
                    String dbMatcherQaud = getString(R.string.alarm_db_matcher_qaud);

                    // put alarm to archive
                    List<HistoryEntry> alarmEntries = dao.queryBuilder().where()
                            .like("smsText", dbMatcher)
                            .or()
                            .like("smsText", dbMatcherQaud)
                            .query();
                    for(HistoryEntry alarm : alarmEntries) {
                        String originalText = alarm.getSmsText();
                        String textToReplace = dbMatcher.substring(1, dbMatcher.length() - 1); // remove percent signs
                        String textToReplaceQaud = dbMatcherQaud.substring(1, dbMatcherQaud.length() - 1);

                        String maskedValue = originalText
                                .replace(textToReplace, getString(R.string.alarm_archive_mask)
                                .replace(textToReplaceQaud, getString(R.string.alarm_archive_mask_qaud)));
                        String archiveSuffix =  getString(R.string.archive_suffix);

                        alarm.setSmsText(maskedValue + " " + archiveSuffix);
                        dao.update(alarm);
                    }

                    /*
                    // just delete all the alarms
                    DeleteBuilder<HistoryEntry, Long> stmt = dao.deleteBuilder();
                    stmt.where().like("smsText", getActivity().getString(R.string.alarm_db_matcher));
                    dao.delete(stmt.prepare());
                    */
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
