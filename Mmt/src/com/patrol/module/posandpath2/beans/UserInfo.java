package com.patrol.module.posandpath2.beans;

import android.os.Parcel;
import android.os.Parcelable;

import com.patrol.module.posandpath2.detailinfo.DetailInfoMapData;

import java.util.LinkedHashMap;

/**
 * Created by Comclay on 2016/10/24.
 * 用户信息业务对象
 */

public class UserInfo extends DetailInfoMapData implements Parcelable,Comparable<UserInfo>{
    public PersonInfo Perinfo;
    public Point point;

    @Override
    public int describeContents() {
        return 0;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> toMapData() {
        LinkedHashMap<String, LinkedHashMap<String, String>> mapData = new LinkedHashMap<>();
        mapData.put("用户信息",this.Perinfo.toMapData());
        mapData.put("位置信息",this.point.toMapData());
        return mapData;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.Perinfo, flags);
        dest.writeParcelable(this.point, flags);
    }

    public UserInfo() {
    }

    protected UserInfo(Parcel in) {
        this.Perinfo = in.readParcelable(UserInfo.class.getClassLoader());
        this.point = in.readParcelable(Point.class.getClassLoader());
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        if (Perinfo != null ? !Perinfo.equals(userInfo.Perinfo) : userInfo.Perinfo != null)
            return false;
        return point != null ? point.equals(userInfo.point) : userInfo.point == null;

    }

    @Override
    public int hashCode() {
        int result = Perinfo != null ? Perinfo.hashCode() : 0;
        result = 31 * result + (point != null ? point.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "point=" + point +
                ", Perinfo=" + Perinfo +
                '}';
    }

    /**
     * 用户对象比较器
     */
    @Override
    public int compareTo(UserInfo another) {
        return another.Perinfo.USERID.compareTo(this.Perinfo.USERID);
    }
}
