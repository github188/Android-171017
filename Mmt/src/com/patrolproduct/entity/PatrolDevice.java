package com.patrolproduct.entity;

import android.content.ContentValues;
import android.database.Cursor;

import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

import java.io.Serializable;

public class PatrolDevice implements Serializable, ISQLiteOper {
    private static final long serialVersionUID = 1L;

    public int ID;
    public int TaskId;
    public int FlowId;

    public String LayerName;
    public String PipeNo;
    public double X;
    public double Y;

    public int Index;

    public boolean IsArrived;
    public String ArrivedDate;
    public boolean IsFeedbacked;
    public String FeedbackDate;

    // 新增属性，用来区分管线设备
    public String Type;// "Point" "Line"
    public String DotsStr;// 线段的点

    public PatrolDevice() {
    }

    public PatrolDevice(int taskID, String layerName, String pipeNo) {
        this.TaskId = taskID;
        this.LayerName = layerName;
        this.PipeNo = pipeNo;
    }

    @Override
    public boolean equals(Object o) {
        try {
            if (o == null) {
                return false;
            }

            if (!(o instanceof PatrolDevice)) {
                return false;
            }

            PatrolDevice d = (PatrolDevice) o;

            return this.TaskId == d.TaskId && this.LayerName.equals(d.LayerName) && this.PipeNo.equals(d.PipeNo);
        } catch (Exception e) {
            return super.equals(o);
        }
    }

    @Override
    public String toString() {
        return "PatrolDevice [TaskId=" + TaskId + ", FlowId=" + FlowId + ", X=" + X + ", Y=" + Y + ", Index=" + Index + "]";
    }

    @Override
    public String getTableName() {
        return "PatrolDevice";
    }

    @Override
    public String getCreateTableSQL() {
        return "(id integer primary key,taskId,flowId,layerName,pipeNo,x,y,sn,isArrived,arrivedDate,isFeedbacked,feedbackDate,Type,DotsStr)";
    }

    @Override
    public SQLiteQueryParameters getSqLiteQueryParameters() {
        return null;
    }

    @Override
    public ContentValues generateContentValues() {
        ContentValues cv = new ContentValues();

        cv.put("taskId", TaskId);
        cv.put("flowId", FlowId);
        cv.put("layerName", LayerName);
        cv.put("pipeNo", PipeNo);
        cv.put("x", X);
        cv.put("y", Y);
        cv.put("sn", Index);// index 是Sqlite的保留关键字,不允许作为列名存在,所以用sn替代
        cv.put("isArrived", IsArrived);
        cv.put("arrivedDate", ArrivedDate);
        cv.put("isFeedbacked", IsFeedbacked);
        cv.put("feedbackDate", FeedbackDate);

        cv.put("Type", Type);
        cv.put("DotsStr", DotsStr);

        return cv;
    }

    @Override
    public void buildFromCursor(Cursor cursor) {
        this.ID = cursor.getInt(0);
        this.TaskId = cursor.getInt(1);
        this.FlowId = cursor.getInt(2);
        this.LayerName = cursor.getString(3);
        this.PipeNo = cursor.getString(4);
        this.X = cursor.getDouble(5);
        this.Y = cursor.getDouble(6);
        this.Index = cursor.getInt(7);
        this.IsArrived = cursor.getShort(8) > 0;
        this.ArrivedDate = cursor.getString(9);
        this.IsFeedbacked = cursor.getShort(10) > 0;
        this.FeedbackDate = cursor.getString(11);

        this.Type = cursor.getString(12);
        this.DotsStr = cursor.getString(13);
    }

    public String getUid() {
        return "taskId=" + TaskId + " and layerName='" + LayerName + "' and pipeNo='" + PipeNo + "'";
    }

    public PatrolDevice fromDB() {
        return DatabaseHelper.getInstance().queryScalar(PatrolDevice.class, getUid());
    }
}
