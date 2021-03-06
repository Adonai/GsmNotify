package com.adonai.GsmNotify.database;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;

/**
 * Created by adonai on 29.06.14.
 */
public class DbProvider {
    private static PersistManager databaseHelper;

    public static PersistManager getHelper(){
        return databaseHelper;
    }

    public static void setHelper(Context context) {
        databaseHelper = OpenHelperManager.getHelper(context, PersistManager.class);
    }

    public static PersistManager getTempHelper(Context context) {
        return OpenHelperManager.getHelper(context, PersistManager.class);
    }

    public static void releaseTempHelper() {
        OpenHelperManager.releaseHelper();
    }

    public static void releaseHelper() {
        OpenHelperManager.releaseHelper();
        databaseHelper = null;
    }
}
