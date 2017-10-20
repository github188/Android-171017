package com.repair.shaoxin.water.valveinstruction;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.repair.zhoushan.common.Locatable;
import com.zondy.mapgis.geometry.Dot;

/**
 * 阀门
 */
public class ValveModel implements Parcelable, Locatable {

    public int ID;

    @SerializedName("事件编号")
    public String eventCode;

    @SerializedName("GIS图层")
    public String gisLayer;

    @SerializedName("GIS编号")
    public String gisCode;

    @SerializedName("阀门编号")
    public String valveCode;

    @SerializedName("规格")
    public String size;

    @SerializedName("位置")
    public String location;

    @SerializedName("图片")
    public String pic;

    // GIS坐标 - X
    public String X;

    // GIS坐标 - Y
    public String Y;

    @SerializedName("完成情况")
    public String completeInfo;

    @SerializedName("备注")
    public String notes;

    @SerializedName("指令内容")
    public String introductionContent;

    @SerializedName("操作顺序")
    public int operateOrder;

    protected ValveModel(Parcel in) {
        ID = in.readInt();
        eventCode = in.readString();
        gisLayer = in.readString();
        gisCode = in.readString();
        valveCode = in.readString();
        size = in.readString();
        location = in.readString();
        pic = in.readString();
        X = in.readString();
        Y = in.readString();
        completeInfo = in.readString();
        notes = in.readString();
        introductionContent = in.readString();
        operateOrder = in.readInt();
    }

    public static final Creator<ValveModel> CREATOR = new Creator<ValveModel>() {
        @Override
        public ValveModel createFromParcel(Parcel in) {
            return new ValveModel(in);
        }

        @Override
        public ValveModel[] newArray(int size) {
            return new ValveModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ID);
        dest.writeString(eventCode);
        dest.writeString(gisLayer);
        dest.writeString(gisCode);
        dest.writeString(valveCode);
        dest.writeString(size);
        dest.writeString(location);
        dest.writeString(pic);
        dest.writeString(X);
        dest.writeString(Y);
        dest.writeString(completeInfo);
        dest.writeString(notes);
        dest.writeString(introductionContent);
        dest.writeInt(operateOrder);
    }

    private Dot mDot;

    @Override
    public Dot getLocationDot() {
        if (mDot != null) {
            return mDot;
        }
        if (!TextUtils.isEmpty(X) && !TextUtils.isEmpty(Y)) {
            try {
                mDot = new Dot(Double.valueOf(X), Double.valueOf(Y));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return mDot;
    }

    @Override
    public String getAnnotationTitle() {
        return valveCode;
    }

    @Override
    public String getAnnotationDesc() {
        return introductionContent + "-" + gisLayer;
    }
}