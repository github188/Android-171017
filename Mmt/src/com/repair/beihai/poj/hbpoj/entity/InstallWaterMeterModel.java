package com.repair.beihai.poj.hbpoj.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by liuyunfan on 2016/8/17.
 */
public class InstallWaterMeterModel {
    public String ID;

    @SerializedName("档案编号")
    public String docCode;

    @SerializedName("用户名称")
    public String userName;

    @SerializedName("用户移动电话")
    public String userTel;

    @SerializedName("用水性质")
    public String waterType;

    @SerializedName("用户地址")
    public String userAdds;

    @SerializedName("水表口径")
    public String watermeterCaliber;

    @SerializedName("横坐标")
    public String x;

    @SerializedName("纵坐标")
    public String y;

    @SerializedName("表身号")
    public String watermeterNo;

    @SerializedName("新水表行度")
    public String watermeterHD;

    @SerializedName("水表产地")
    public String watermeterComeFrom;

    @SerializedName("装表人")
    public String installMan;

    @SerializedName("装表日期")
    public String installDate;

    @SerializedName("领表时间")
    public String acceptDate;

    @SerializedName("备注")
    public String remark;

    @SerializedName("是够立即供水")
    public String giveWaterNow;

    @SerializedName("用户确认")
    public String userConfirm;

    @SerializedName("装表状态")
    public String installState;

}
