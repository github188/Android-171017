package com.mapgis.mmt.module.gis.toolbar.accident2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * 爆管后受影响设备对象
 * Created by Comclay on 2017/3/1.
 */

public class FeatureMetaItem implements Parcelable {
    public String layerId;
    public String layerName;
    public ArrayList<String> objectIds;
    public ArrayList<RelationShip> relationships;
    public FeatureGroup featureGroup;

    public void setFeatureGroup(FeatureGroup featureGroup) {
        this.featureGroup = featureGroup;
    }

    public FeatureGroup getFeatureGroup() {
        return featureGroup;
    }

    @Override
    public String toString() {
        return "FeatureMetaItem{" +
                "layerId='" + layerId + '\'' +
                ", layerName='" + layerName + '\'' +
                ", objectIds=" + objectIds.size() +
                ", featureGroup=" + featureGroup +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.layerId);
        dest.writeString(this.layerName);
        dest.writeStringList(this.objectIds);
        dest.writeTypedList(this.relationships);
        dest.writeParcelable(this.featureGroup, flags);
    }

    public FeatureMetaItem() {
    }

    protected FeatureMetaItem(Parcel in) {
        this.layerId = in.readString();
        this.layerName = in.readString();
        this.objectIds = in.createStringArrayList();
        this.relationships = in.createTypedArrayList(RelationShip.CREATOR);
        this.featureGroup = in.readParcelable(FeatureGroup.class.getClassLoader());
    }

    public static final Creator<FeatureMetaItem> CREATOR = new Creator<FeatureMetaItem>() {
        @Override
        public FeatureMetaItem createFromParcel(Parcel source) {
            return new FeatureMetaItem(source);
        }

        @Override
        public FeatureMetaItem[] newArray(int size) {
            return new FeatureMetaItem[size];
        }
    };
}
