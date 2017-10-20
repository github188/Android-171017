package com.mapgis.mmt.module.gis.toolbar.accident2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.zondy.mapgis.geometry.Dot;

import java.util.LinkedHashMap;

public class FeatureItem implements Parcelable {
    public LinkedHashMap<String, String> attributes;
    public String strAtt;
    public FeatureGeometry geometry;

    public Dot getDot() {
        if (geometry == null) {
            return null;
        }
        return this.geometry.getDot();
    }

    public void setAttributes(LinkedHashMap<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getStrAtt() {
        return strAtt;
    }

    public void setStrAtt(String strAtt) {
        this.strAtt = strAtt;
    }

    public FeatureGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(FeatureGeometry geometry) {
        this.geometry = geometry;
    }

    @Override
    public String toString() {
        return "FeatureItem{" +
                "attributes=" + (attributes != null ? attributes.get("OID") : "") +
                ", geometry=" + geometry +
                '}';
    }

    public LinkedHashMap<String, String> getAttributes() {
        if (this.attributes != null) {
            return this.attributes;
        }

        if (!BaseClassUtil.isNullOrEmptyString(this.strAtt)) {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            this.attributes = gson.fromJson(this.strAtt
                    , new TypeToken<LinkedHashMap<String, String>>() {
                    }.getType());
        }
        return this.attributes;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        this.strAtt = gson.toJson(this.attributes);
        dest.writeString(this.strAtt);
        dest.writeParcelable(this.geometry, flags);
    }

    public FeatureItem() {
    }

    protected FeatureItem(Parcel in) {
        this.strAtt = in.readString();
        this.geometry = in.readParcelable(FeatureGeometry.class.getClassLoader());
        this.attributes = getAttributes();
    }

    public static final Creator<FeatureItem> CREATOR = new Creator<FeatureItem>() {
        @Override
        public FeatureItem createFromParcel(Parcel source) {
            return new FeatureItem(source);
        }

        @Override
        public FeatureItem[] newArray(int size) {
            return new FeatureItem[size];
        }
    };
}
