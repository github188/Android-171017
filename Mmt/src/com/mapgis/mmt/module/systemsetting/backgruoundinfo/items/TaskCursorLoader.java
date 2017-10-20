package com.mapgis.mmt.module.systemsetting.backgruoundinfo.items;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.os.CancellationSignal;
import android.support.v4.os.OperationCanceledException;

import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

public class TaskCursorLoader extends BaseCursorLoader {

    private SQLiteQueryParameters params;

    public TaskCursorLoader(Context context, SQLiteQueryParameters parameters) {
        super(context);
        this.params = parameters;
    }

    @Override
    public Cursor loadInBackground() {

        synchronized (this) {
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            mCancellationSignal = new CancellationSignal();
        }

        Cursor cursor = null;

        try {
            SQLiteDatabase sqLiteDatabase = DatabaseHelper.getInstance().getSqLiteDatabase();

            cursor = sqLiteDatabase.query("ReportInBack",
                    params.columns, params.selection, params.selectionArgs, params.groupBy, params.having, params.orderBy, params.limit);

            if (cursor != null) {
                try {
                    // Ensure the cursor window is filled.
                    cursor.getCount();
                    cursor.registerContentObserver(getObserver());
                } catch (RuntimeException ex) {
                    cursor.close();
                    throw ex;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            synchronized (this) {
                mCancellationSignal = null;
            }
        }

        return cursor;
    }

}