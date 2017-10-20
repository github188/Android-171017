package com.mapgis.mmt.module.gis.place;

import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.module.gps.trans.MmtLocation;

public class SingleSearchResult {
    public String name;

    public MmtLocation location;

    public String address;
    public String telephone;
    public String uid;

    /**
     * 将获取到的BD-09(百度经纬度坐标)转换为当地的平面坐标
     *
     * @return 平面坐标
     */
    public GpsXYZ getXyz() {
        try {
            if (location != null)
                return location.convert2Xyz();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new GpsXYZ();
    }
}