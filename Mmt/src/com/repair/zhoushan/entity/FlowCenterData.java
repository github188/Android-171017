package com.repair.zhoushan.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 流程中心数据，不同用户具有不同的业务、发起不同的流程
 */
public class FlowCenterData implements Parcelable {

    public FlowCenterData() {
        BusinessType = "";
        EventName = "";
        TableName = "";
        BizCode = "";
        FieldGroup = "";
        FlowName = "";
        NodeName = "";
        OperType = "";
        HandoverMode = "";
        IsCreate = 0;
        ViewModule = "";
    }

    /**
     * 业务类型
     */
    public String BusinessType;

    /**
     * 事件名称
     */
    public String EventName;

    /**
     * 事件主表名称
     */
    public String TableName;

    /**
     * 业务编码
     */
    public String BizCode;

    /**
     * 字段集
     */
    public String FieldGroup;

    /**
     * 流程名称
     */
    public String FlowName;

    /**
     * 节点名称
     */
    public String NodeName;

    /**
     * 操作类型
     */
    public String OperType;

    /**
     * 移交方式
     */
    public String HandoverMode;

    /**
     * 是否发起
     */
    public int IsCreate;

    /**
     * 视图模块
     */
    public String ViewModule;

    /**
     * 在流程中心中的图标，本地添加的字段服务端没有
     */
    public int Icon;

    @Override
    public int hashCode() {
        return 31 * BusinessType.hashCode() + EventName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FlowCenterData)) return false;

        FlowCenterData flowCenterData = (FlowCenterData) obj;
        return BusinessType.equals(flowCenterData.BusinessType)
                && EventName.equals(flowCenterData.EventName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<FlowCenterData> CREATOR = new Parcelable.Creator<FlowCenterData>() {
        @Override
        public FlowCenterData createFromParcel(Parcel in) {
            return new FlowCenterData(in);
        }

        @Override
        public FlowCenterData[] newArray(int size) {
            return new FlowCenterData[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {

        out.writeString(BusinessType);
        out.writeString(EventName);
        out.writeString(TableName);
        out.writeString(BizCode);
        out.writeString(FieldGroup);
        out.writeString(FlowName);
        out.writeString(NodeName);
        out.writeString(OperType);
        out.writeString(HandoverMode);
        out.writeInt(IsCreate);
        out.writeString(ViewModule);
    }

    private FlowCenterData(Parcel in) {

        BusinessType = in.readString();
        EventName = in.readString();
        TableName = in.readString();
        BizCode = in.readString();
        FieldGroup = in.readString();
        FlowName = in.readString();
        NodeName = in.readString();
        OperType = in.readString();
        HandoverMode = in.readString();
        IsCreate = in.readInt();
        ViewModule = in.readString();
    }

}
