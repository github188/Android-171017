package com.mapgis.mmt.module.gps.entity;

import android.location.Location;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TracePoint {
    public double X;
    public double Y;
    public String ReportTime;
    public boolean IsSuccess;
    public double Latitude;
    public double Longitude;
    public String Accuracy;
    public String CPU;
    public String Battery;
    public String Memory;
    public String Speed;
    public int UserID;
    public String Provider;

    public String State;

    public static List<TracePoint> fromGPSXYZList(List<GpsXYZ> xyzList) {
        List<TracePoint> points = new ArrayList<>();

        for (GpsXYZ xyz : xyzList)
            points.add(fromGPSXYZ(xyz));

        return points;
    }

    public static TracePoint fromGPSXYZ(GpsXYZ xyz) {
        TracePoint point = new TracePoint();

        try {
            point.X = xyz.getX();
            point.Y = xyz.getY();
            point.ReportTime = xyz.getReportTime();

            Location location = xyz.getLocation();

            point.Latitude = location != null ? Convert.FormatDouble(location.getLatitude(), ".0000") : 0;
            point.Longitude = location != null ? Convert.FormatDouble(location.getLongitude(), ".0000") : 0;
            point.Accuracy = location != null ? String.valueOf(location.getAccuracy()) : "";
            point.CPU = xyz.cpu;
            point.Battery = xyz.battery;
            point.Memory = xyz.memory;
            point.Speed = xyz.speed;
            point.UserID = xyz.getUserId();
            point.Provider = location != null ? location.getProvider() : "";
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return point;
    }

    public void correctTime() {
        try {
            Date gpsTime = BaseClassUtil.parseTime(this.ReportTime);
            Date nowTime = new Date();

            if (gpsTime != null)
                this.State = "相隔：" + ((nowTime.getTime() - gpsTime.getTime()) / 1000) + "秒";
            else
                this.State = "相隔：-秒";

            this.ReportTime = BaseClassUtil.getSystemTime(nowTime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "TracePoint{" +
                "X=" + X +
                ", Y=" + Y +
                ", ReportTime='" + ReportTime + '\'' +
                ", Latitude=" + Latitude +
                ", Longitude=" + Longitude +
                ", UserID=" + UserID +
                ", Provider='" + Provider + '\'' +
                '}';
    }
}
