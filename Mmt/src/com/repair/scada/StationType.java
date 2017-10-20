package com.repair.scada;

import android.support.annotation.DrawableRes;

public class StationType {
    private String stationTypeID;
    private String stationTypeName;

    @DrawableRes
    private int bitmapRes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StationType)) return false;

        StationType that = (StationType) o;

        if (!stationTypeID.equals(that.stationTypeID)) return false;
        return stationTypeName.equals(that.stationTypeName);

    }

    @Override
    public int hashCode() {
        int result = stationTypeID.hashCode();
        result = 31 * result + stationTypeName.hashCode();
        return result;
    }

    public String getStationTypeID() {
        return stationTypeID;
    }

    public void setStationTypeID(String stationTypeID) {
        this.stationTypeID = stationTypeID;
    }

    public String getStationTypeName() {
        return stationTypeName;
    }

    public void setStationTypeName(String stationTypeName) {
        this.stationTypeName = stationTypeName;
    }

    @DrawableRes
    public int getBitmapRes() {
        return bitmapRes;
    }

    public void setBitmapRes(@DrawableRes int bitmapRes) {
        this.bitmapRes = bitmapRes;
    }
}
