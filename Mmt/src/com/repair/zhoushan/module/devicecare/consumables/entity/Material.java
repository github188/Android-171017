package com.repair.zhoushan.module.devicecare.consumables.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 工单材料
 */
public class Material implements Parcelable {
    /**
     * 材料类型
     */
    public String MType;

    /**
     * 材料名称
     */
    public String Name;

    /**
     * 材料编号
     */
    public String No;

    /**
     * 规格
     */
    public String Specifications;

    /**
     * 单位
     */
    public String Unit;

    /**
     * 价格
     */
    public double Price;

    /**
     * 厂商
     */
    public String Firm;

    /**
     * 库存量
     */
    public double Stock;

    /**
     * 数量
     */
    public int Num;

    /**
     * 添加人员
     */
    public String AddMan;

    /**
     * 添加时间
     */
    public String AddTime;

    /**
     * 设备类型
     */
    public String DeviceType;

    /**
     * 设备材质
     */
    public String DeviceTexture;

    /**
     * 成本类型
     */
    public String CostType;

    /**
     * 总价
     */
    public double TotalPrice;

    protected Material(Parcel in) {
        MType = in.readString();
        Name = in.readString();
        No = in.readString();
        Specifications = in.readString();
        Unit = in.readString();
        Price = in.readDouble();
        Firm = in.readString();
        Stock = in.readDouble();
        Num = in.readInt();
        AddMan = in.readString();
        AddTime = in.readString();
        DeviceType = in.readString();
        DeviceTexture = in.readString();
        CostType = in.readString();
        TotalPrice = in.readDouble();
    }

    public static final Creator<Material> CREATOR = new Creator<Material>() {
        @Override
        public Material createFromParcel(Parcel in) {
            return new Material(in);
        }

        @Override
        public Material[] newArray(int size) {
            return new Material[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(MType);
        dest.writeString(Name);
        dest.writeString(No);
        dest.writeString(Specifications);
        dest.writeString(Unit);
        dest.writeDouble(Price);
        dest.writeString(Firm);
        dest.writeDouble(Stock);
        dest.writeInt(Num);
        dest.writeString(AddMan);
        dest.writeString(AddTime);
        dest.writeString(DeviceType);
        dest.writeString(DeviceTexture);
        dest.writeString(CostType);
        dest.writeDouble(TotalPrice);
    }
}