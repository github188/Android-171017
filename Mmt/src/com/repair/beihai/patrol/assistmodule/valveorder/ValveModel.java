package com.repair.beihai.patrol.assistmodule.valveorder;

import com.google.gson.annotations.SerializedName;

public class ValveModel {

    public int ID;

    @SerializedName("事件编号")
    public String eventCode;

    @SerializedName("工单编号")
    public String caseNo;

    @SerializedName("编号")
    public String no;

    @SerializedName("GIS图层")
    public String gisLayer;

    @SerializedName("坐标")
    public String coordinate;

    @SerializedName("设备规格")
    public String specification;

    @SerializedName("位置")
    public String address;

    @SerializedName("开关状态")
    public String state;

    @SerializedName("关阀人")
    public String closePerson;

    @SerializedName("关阀时间")
    public String closeTime;

    @SerializedName("关阀图片")
    public String closePic;

    @SerializedName("关阀描述")
    public String closeDesc;

    @SerializedName("开阀人")
    public String openPerson;

    @SerializedName("开阀时间")
    public String openTime;

    @SerializedName("开阀图片")
    public String openPic;

    @SerializedName("开阀描述")
    public String openDesc;
}