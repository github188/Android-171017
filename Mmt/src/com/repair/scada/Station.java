package com.repair.scada;

import android.os.Parcel;
import android.os.Parcelable;

public class Station implements Parcelable {
    public String stationID;
    public String stationTypeID;
    public String stationNO;
    public String stationName;
    public String areaName;
    public String address;
    public String stationTypeName;
    public double minScale;
    public double maxScale;
    public double x;
    public double y;
    public String gongyitu;
    public int width;
    public int height;

    protected Station(Parcel in) {
        stationID = in.readString();
        stationTypeID = in.readString();
        stationNO = in.readString();
        stationName = in.readString();
        areaName = in.readString();
        address = in.readString();
        stationTypeName = in.readString();
        minScale = in.readDouble();
        maxScale = in.readDouble();
        x = in.readDouble();
        y = in.readDouble();
        gongyitu = in.readString();
        width = in.readInt();
        height = in.readInt();
    }

    public static final Creator<Station> CREATOR = new Creator<Station>() {
        @Override
        public Station createFromParcel(Parcel in) {
            return new Station(in);
        }

        @Override
        public Station[] newArray(int size) {
            return new Station[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station)) return false;

        Station station = (Station) o;

        if (!stationID.equals(station.stationID)) return false;
        return stationTypeID.equals(station.stationTypeID);

    }

    @Override
    public int hashCode() {
        int result = stationID.hashCode();
        result = 31 * result + stationTypeID.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(stationID);
        dest.writeString(stationTypeID);
        dest.writeString(stationNO);
        dest.writeString(stationName);
        dest.writeString(areaName);
        dest.writeString(address);
        dest.writeString(stationTypeName);
        dest.writeDouble(minScale);
        dest.writeDouble(maxScale);
        dest.writeDouble(x);
        dest.writeDouble(y);
        dest.writeString(gongyitu);
        dest.writeInt(width);
        dest.writeInt(height);
    }
}