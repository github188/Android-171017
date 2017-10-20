package com.repair.shaoxin.water.repairtask;

import com.google.gson.annotations.SerializedName;

public class RepairTaskEntity {

    public int ID;
    @SerializedName("编号")
    public String no;

    @SerializedName("所属公司")
    public String belongCompany;

    @SerializedName("责任部门")
    public String dutyDept;

    @SerializedName("信息来源")
    public String infoSource;

    @SerializedName("接报时间")
    public String receiveTime;

    @SerializedName("用户姓名")
    public String userName;

    @SerializedName("用户电话")
    public String userTel;

    @SerializedName("地址")
    public String address;

    @SerializedName("坐标")
    public String coordinate;

    @SerializedName("报损情况")
    public String damagedInfo;

    @SerializedName("接报人")
    public String receivePerson;

    @SerializedName("施工人")
    public String executePerson;

    @SerializedName("开工时间")
    public String startTime;

    @SerializedName("完工时间")
    public String endTime;

    @SerializedName("结果")
    public String result;

    @SerializedName("公")
    public int GONG;

    @SerializedName("用")
    public int YONG;

    @SerializedName("备注")
    public String notes;

    @SerializedName("addtime")
    public String addTime;

    @SerializedName("阅读时限")
    public int readTimeLimit;

    @SerializedName("处理时限")
    public int  handleTimeLimit;

    @SerializedName("漏点管径")
    public int leakPointDiameter;

    @SerializedName("漏点管径1")
    public int leakPointDiameter1;

}
