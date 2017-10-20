package com.patrol.module.posandpath2.beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedHashMap;

/**
 * Created by Comclay on 2016/10/24.
 * 位置信息业务类
 */

public class Point implements Parcelable {
    public String Position;   // "187552.426,351425.41"
    public String time;       //"2016/1/29 17:54:22"
    public String address;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.Position);
        dest.writeString(this.time);
    }

    public Point() {
    }

    protected Point(Parcel in) {
        this.Position = in.readString();
        this.time = in.readString();
    }

    public static final Creator<Point> CREATOR = new Creator<Point>() {
        @Override
        public Point createFromParcel(Parcel source) {
            return new Point(source);
        }

        @Override
        public Point[] newArray(int size) {
            return new Point[size];
        }
    };

    public  LinkedHashMap<String,String> toMapData() {
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        map.put("位置",this.Position);
        map.put("时间",this.time);
        return map;
    }

    @Override
    public String toString() {
        return "Point{" +
                "Position='" + Position + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
