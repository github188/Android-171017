package com.mapgis.mmt.module.gps.entity;

public class BLHInfo {
    public double Latitude;
    public double Longitude;
    public double Altitude;

    @Override
    public String toString() {
        return "经度：" + Longitude + "---纬度：" + Latitude;
    }
}