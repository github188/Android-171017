package com.repair.zhoushan.module.devicecare;

import android.os.Parcel;
import android.os.Parcelable;

public class MaintenanceFeedBack implements Parcelable {

    /**
     * 业务名称
     */
    public String bizName;
    /**
     * 过滤条件字段
     */
    public String filterConditionFiled;
    /**
     * 过滤条件值
     */
    public String filterConditionVal;
    /**
     * 设备反馈表
     */
    public String deviceFBTbl;
    /**
     * 包含字段集
     */
    public String fileds;
    /**
     * 排除字段集
     */
    public String excludeFileds;
    /**
     * 设备名称
     */
    public String DeviceName;
    /**
     * 部件名称
     */
    public String PartName;
    /**
     * 反馈类型
     */
    public String feedBackType;
    /**
     * 触发异常值
     */
    public String triggerProblemValue;
    /**
     * 触发事件
     */
    public String triggerEvent;
    /**
     * 触发事件字段集
     */
    public String triggerEventField;

    public MaintenanceFeedBack() {
        this.bizName = "";
        this.filterConditionFiled = "";
        this.filterConditionVal = "";
        this.deviceFBTbl = "";
        this.fileds = "";
        this.excludeFileds = "";
        this.DeviceName = "";
        this.PartName = "";
        this.feedBackType = "";
        this.triggerProblemValue = "";
        this.triggerEvent = "";
        this.triggerEventField = "";
    }

    protected MaintenanceFeedBack(Parcel in) {
        bizName = in.readString();
        filterConditionFiled = in.readString();
        filterConditionVal = in.readString();
        deviceFBTbl = in.readString();
        fileds = in.readString();
        excludeFileds = in.readString();
        DeviceName = in.readString();
        PartName = in.readString();
        feedBackType = in.readString();
        triggerProblemValue = in.readString();
        triggerEvent = in.readString();
        triggerEventField = in.readString();
    }

    public static final Creator<MaintenanceFeedBack> CREATOR = new Creator<MaintenanceFeedBack>() {
        @Override
        public MaintenanceFeedBack createFromParcel(Parcel in) {
            return new MaintenanceFeedBack(in);
        }

        @Override
        public MaintenanceFeedBack[] newArray(int size) {
            return new MaintenanceFeedBack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bizName);
        dest.writeString(filterConditionFiled);
        dest.writeString(filterConditionVal);
        dest.writeString(deviceFBTbl);
        dest.writeString(fileds);
        dest.writeString(excludeFileds);
        dest.writeString(DeviceName);
        dest.writeString(PartName);
        dest.writeString(feedBackType);
        dest.writeString(triggerProblemValue);
        dest.writeString(triggerEvent);
        dest.writeString(triggerEventField);
    }
}
