package com.mapgis.mmt.entity;

import android.content.ContentValues;
import android.database.Cursor;

import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

public class ShortMessageBean implements ISQLiteOper {
    private int msgId;
    private String msgTime;
    private String msgDetail;
    private int msgState;
    private int userId;

    public ShortMessageBean() {
    }

    public ShortMessageBean(String msgTime, String msgDetail, int msgState, int userId) {
        super();
        this.msgTime = msgTime;
        this.msgDetail = msgDetail;
        this.msgState = msgState;
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getMsgTime() {
        return msgTime;
    }

    public void setMsgTime(String msgTime) {
        this.msgTime = msgTime;
    }

    public String getMsgDetail() {
        return msgDetail;
    }

    public void setMsgDetail(String msgDetail) {
        this.msgDetail = msgDetail;
    }

    public int getMsgState() {
        return msgState;
    }

    public void setMsgState(int msgState) {
        this.msgState = msgState;
    }

    @Override
    public String getTableName() {
        return "ShortMessage";
    }

    @Override
    public ContentValues generateContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put("msgTime", this.msgTime);
        contentValues.put("msgDetail", this.msgDetail);
        contentValues.put("msgState", this.msgState);
        contentValues.put("userId", this.userId);

        return contentValues;
    }

    public ContentValues generateStateContentValues(int i) {
        ContentValues contentValues = new ContentValues();

        contentValues.put("msgState", i);

        return contentValues;
    }

    @Override
    public void buildFromCursor(Cursor cursor) {
        this.msgId = cursor.getInt(0);
        this.msgTime = cursor.getString(1);
        this.msgDetail = cursor.getString(2);
        this.msgState = cursor.getInt(3);
        this.userId = cursor.getInt(4);
    }

    @Override
    public String toString() {
        return "ShortMessageBean [msgId=" + msgId + ", msgTime=" + msgTime + ", msgDetail=" + msgDetail + ", msgState=" + msgState + "]";
    }

    @Override
    public SQLiteQueryParameters getSqLiteQueryParameters() {
        return null;
    }

    @Override
    public String getCreateTableSQL() {
        return "(msgId integer primary key,msgTime,msgDetail, msgState,userId)";
    }

    public String getMsgContent() {
        return msgDetail.substring(0, msgDetail.indexOf("#"));
    }
}
