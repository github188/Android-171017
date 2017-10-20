package com.mapgis.mmt.module.gis.toolbar.accident2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Locale;

/**
 * 爆管后受影响的设备类型对象
 * Created by Comclay on 2017/3/1.
 */

public class FeatureMetaGroup implements Parcelable {
    public String civFeatureMetaType;
    public String civFeatureMetaTypeName;
    public ArrayList<FeatureMetaItem> resultList;

    /**
     * 自定义字段，用来标识是否在地图上显示
     */
    private boolean isShow = false;

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    public String getCivFeatureMetaType() {
        return civFeatureMetaType;
    }

    public void setCivFeatureMetaType(String civFeatureMetaType) {
        this.civFeatureMetaType = civFeatureMetaType;
    }

    public String getCivFeatureMetaTypeName() {
        return civFeatureMetaTypeName;
    }

    public void setCivFeatureMetaTypeName(String civFeatureMetaTypeName) {
        this.civFeatureMetaTypeName = civFeatureMetaTypeName;
    }

    public ArrayList<FeatureMetaItem> getResultList() {
        return resultList;
    }

    public void setResultList(ArrayList<FeatureMetaItem> resultList) {
        this.resultList = resultList;
    }

    @Override
    public String toString() {
        return String.format(Locale.CHINA, "[%s，%s，%s]\n"
                , civFeatureMetaTypeName
                , Boolean.valueOf(isShow).toString()
                , resultList.toString());
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.civFeatureMetaType);
        dest.writeString(this.civFeatureMetaTypeName);
        dest.writeList(this.resultList);
        dest.writeByte(this.isShow ? (byte) 1 : (byte) 0);
    }

    public FeatureMetaGroup() {
    }

    protected FeatureMetaGroup(Parcel in) {
        this.civFeatureMetaType = in.readString();
        this.civFeatureMetaTypeName = in.readString();
        this.resultList = new ArrayList<FeatureMetaItem>();
        in.readList(this.resultList, FeatureMetaItem.class.getClassLoader());
        this.isShow = in.readByte() != 0;
    }

    public static final Parcelable.Creator<FeatureMetaGroup> CREATOR = new Parcelable.Creator<FeatureMetaGroup>() {
        @Override
        public FeatureMetaGroup createFromParcel(Parcel source) {
            return new FeatureMetaGroup(source);
        }

        @Override
        public FeatureMetaGroup[] newArray(int size) {
            return new FeatureMetaGroup[size];
        }
    };
}
