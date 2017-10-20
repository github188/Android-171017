package com.mapgis.mmt.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

import java.io.Serializable;


public class AppLogger implements Serializable, ISQLiteOper {
    public int id;
    public String time;
    public String content;

    public AppLogger() {
        this("");
    }

    public AppLogger(String content) {
        this(BaseClassUtil.getSystemTime(), content);
    }

    private AppLogger(String time, String content) {
        this.time = time;
        this.content = content;
    }

    @Override
    public String getTableName() {
        return "AppLogger";
    }

    @Override
    public String getCreateTableSQL() {
        return "(id integer primary key,time,content)";
    }

    @Override
    public SQLiteQueryParameters getSqLiteQueryParameters() {
        return new SQLiteQueryParameters();
    }

    @Override
    public ContentValues generateContentValues() {
        ContentValues cv = new ContentValues();

        if (TextUtils.isEmpty(this.time))
            this.time = BaseClassUtil.getSystemTime();

        cv.put("time", this.time);
        cv.put("content", this.content);

        return cv;
    }

    @Override
    public void buildFromCursor(Cursor cursor) {
        this.id = cursor.getInt(0);
        this.time = cursor.getString(1);
        this.content = cursor.getString(2);
    }

    @Override
    public String toString() {
        return "[" + time + "]" + content;
    }
}
