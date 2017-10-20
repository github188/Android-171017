package com.mapgis.mmt.module.gps.entity;

import android.content.ContentValues;
import android.database.Cursor;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;

public class GPSTraceInfo extends GpsXYZ {

    public GPSTraceInfo() {
        super();
    }

    public GPSTraceInfo(GpsXYZ xyz) {
        this.setReportTime(xyz.getReportTime());
        this.setUserId(MyApplication.getInstance().getUserId());

        this.setX(xyz.getX());
        this.setY(xyz.getY());
        this.setLocation(xyz.getLocation());
    }

    @Override
    public String getTableName() {
        return "GPSTraceInfo";
    }

    public String getCreateTableSQL() {
        return "(id integer primary key,x,y,reportTime,isSuccess,latitude,longitude,accuracy,cpu,battery,memory,speed,userId,provider)";
    }

    @Override
    public void buildFromCursor(Cursor cursor) {
        try {
            super.buildFromCursor(cursor);

            super.getLocation().setProvider(cursor.getString(13));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ContentValues generateContentValues() {
        ContentValues cv = null;

        try {
            cv = super.generateContentValues();

            cv.put("accuracy", super.getLocation().getAccuracy());
            cv.put("provider", super.getLocation().getProvider());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return cv;
    }
}
