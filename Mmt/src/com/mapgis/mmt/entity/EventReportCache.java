package com.mapgis.mmt.entity;

import android.content.ContentValues;
import android.database.Cursor;

import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

import java.util.ArrayList;

public class EventReportCache implements ISQLiteOper {

    private int id;
    private int userId;
    private String key;
    private String value;
    private int recordId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getRecordId(){
        return recordId;
    }

    public EventReportCache() {
    }

    public EventReportCache(int userId, String key, String value) {
        this(userId, key, value, 0);
    }

    public EventReportCache(int userId, String key, String value, int recordId) {
        this.userId = userId;
        this.key = key;
        this.value = value;
        this.recordId = recordId;
    }

    @Override
    public String getTableName() {
        return "EventReportCache";
    }

    @Override
    public String getCreateTableSQL() {
        return "(id integer primary key, userId, key, value,recordId)";
    }

    @Override
    public SQLiteQueryParameters getSqLiteQueryParameters() {
        return null;
    }

    @Override
    public ContentValues generateContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("userId", this.userId);
        cv.put("key", this.key);
        cv.put("value", this.value);
        cv.put("recordId", this.recordId);
        return cv;
    }

    @Override
    public void buildFromCursor(Cursor cursor) {
        this.id = cursor.getInt(0);
        this.userId = cursor.getInt(1);
        this.key = cursor.getString(2);
        this.value = cursor.getString(3);
        this.recordId = cursor.getInt(4);
    }

    public long insert() {
        ArrayList<EventReportCache> list = DatabaseHelper.getInstance().query(EventReportCache.class,
                new SQLiteQueryParameters("userId=" + this.userId + " and key='" + this.key + "'"));

        if (list.size() == 0) {
            return DatabaseHelper.getInstance().insert(this);
        } else {
            ContentValues cv = new ContentValues();
            cv.put("value", this.value);
            cv.put("recordId", this.recordId);
            return DatabaseHelper.getInstance().update(EventReportCache.class, cv,
                    "userId=" + this.userId + " and key='" + this.key + "'");
        }
    }


}
