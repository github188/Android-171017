package com.project.enn.selfemployed;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 工商户置换表具记录表
 */
public class CommeicalChangeInfo implements Parcelable {

    public CommeicalChangeInfo() {
        ID = "";
        EventCode = "";
        Code = "";
        SteelGrade = "";
        Type = "";
        ModelType = "";
        InitialValue = "";
        MeasurementIsNormal = "";
        IsInfuseOil = "";
        Remark = "";
    }

    /**
     * ID
     */
    public String ID;

    /**
     * 事件编号
     */
    public String EventCode;

    /**
     * 编号
     */
    public String Code;

    /**
     * 表钢号
     */
    public String SteelGrade;

    /**
     * 类型
     */
    public String Type;

    /**
     * 型号
     */
    public String ModelType;

    /**
     * 表底数
     */
    public String InitialValue;

    /**
     * 表具计量是否正常
     */
    public String MeasurementIsNormal;

    /**
     * 是否注油
     */
    public String IsInfuseOil;

    /**
     * 备注
     */
    public String Remark;

    protected CommeicalChangeInfo(Parcel in) {
        ID = in.readString();
        EventCode = in.readString();
        Code = in.readString();
        SteelGrade = in.readString();
        Type = in.readString();
        ModelType = in.readString();
        InitialValue = in.readString();
        MeasurementIsNormal = in.readString();
        IsInfuseOil = in.readString();
        Remark = in.readString();
    }

    public static final Creator<CommeicalChangeInfo> CREATOR = new Creator<CommeicalChangeInfo>() {
        @Override
        public CommeicalChangeInfo createFromParcel(Parcel in) {
            return new CommeicalChangeInfo(in);
        }

        @Override
        public CommeicalChangeInfo[] newArray(int size) {
            return new CommeicalChangeInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeString(EventCode);
        dest.writeString(Code);
        dest.writeString(SteelGrade);
        dest.writeString(Type);
        dest.writeString(ModelType);
        dest.writeString(InitialValue);
        dest.writeString(MeasurementIsNormal);
        dest.writeString(IsInfuseOil);
        dest.writeString(Remark);
    }
}