package com.repair.zhoushan.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class EventItem implements Parcelable {

    public EventItem() {
        ID = 0;
        EventName = "";
        EventCode = "";
        EventState = "";
        ReportStation = "";
        DealStation = "";
        ReporterName = "";
        ReporterDepart = "";
        ReportTime = "";
        UpdateTime = "";
        UpdateState = "";
        Picture = "";
        Radios = "";
        XY = "";
        BusinessType = "";
        Summary = "";
        SummaryDetail = "";

        BizCode = "";
        EventMainTable = "";
        FieldGroup = "";
        SummaryField = "";
        IsCreate = "";
        IsReport = "";
        IsRelatedCase = 0;
        IsStick = 0;
        Distance = 0;
        DistanceStr = "";
    }

    /**
     * 事件ID
     */
    public int ID;

    /**
     * 事件名称
     */
    public String EventName;

    /**
     * 事件编号
     */
    public String EventCode;

    /**
     * 事件状态
     */
    public String EventState;

    /**
     * 上报站点
     */
    public String ReportStation;

    /**
     * 处理站点
     */
    public String DealStation;

    /**
     * 上报人名称
     */
    public String ReporterName;

    /**
     * 上报人部门
     */
    public String ReporterDepart;

    /**
     * 上报时间
     */
    public String ReportTime;

    /**
     * 最后上报时间
     */
    public String UpdateTime;

    /**
     * 更新状态
     */
    public String UpdateState;

    /**
     * 现场图片
     */
    public String Picture;

    /**
     * 现场录音
     */
    public String Radios;

    /**
     * 坐标位置
     */
    public String XY;

    /**
     * 业务类型
     */
    public String BusinessType;

    /**
     * 事件摘要
     */
    public String Summary;

    /**
     * 事件摘要详情，包括摘要字段和摘要字段值
     */
    public String SummaryDetail;

    /**
     * 业务编码
     */
    public String BizCode;

    /**
     * 事件主表
     */
    public String EventMainTable;

    /**
     * 字段集
     */
    public String FieldGroup;

    /**
     * 摘要字段
     */
    public String SummaryField;

    /**
     * 是否发起
     */
    public String IsCreate;

    /**
     * 是否上报
     */
    public String IsReport;

    /**
     * 事件是否已经发起过流程 - 标识
     */
    public int IsRelatedCase;

    /**
     * 事件是否置顶
     */
    public int IsStick;

    // 手机本地为列表显示额外添加的字段

    /**
     * 距离当前坐标的距离
     */
    public double Distance;

    /**
     * 距离当前坐标的距离的用于显示的字符串
     */
    public String DistanceStr;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(ID);
        out.writeString(EventName);
        out.writeString(EventCode);
        out.writeString(EventState);
        out.writeString(ReportStation);
        out.writeString(DealStation);
        out.writeString(ReporterName);
        out.writeString(ReporterDepart);
        out.writeString(ReportTime);
        out.writeString(UpdateTime);
        out.writeString(UpdateState);
        out.writeString(Picture);
        out.writeString(Radios);
        out.writeString(XY);
        out.writeString(BusinessType);
        out.writeString(Summary);
        out.writeString(SummaryDetail);
        out.writeString(BizCode);
        out.writeString(EventMainTable);
        out.writeString(FieldGroup);
        out.writeString(SummaryField);
        out.writeString(IsCreate);
        out.writeString(IsReport);
        out.writeInt(IsRelatedCase);
        out.writeInt(IsStick);
    }

    public static final Parcelable.Creator<EventItem> CREATOR = new Parcelable.Creator<EventItem>() {
        @Override
        public EventItem createFromParcel(Parcel in) {
            return new EventItem(in);
        }

        @Override
        public EventItem[] newArray(int size) {
            return new EventItem[size];
        }
    };

    private EventItem(Parcel in) {

        ID = in.readInt();
        EventName = in.readString();
        EventCode = in.readString();
        EventState = in.readString();
        ReportStation = in.readString();
        DealStation = in.readString();
        ReporterName = in.readString();
        ReporterDepart = in.readString();
        ReportTime = in.readString();
        UpdateTime = in.readString();
        UpdateState = in.readString();
        Picture = in.readString();
        Radios = in.readString();
        XY = in.readString();
        BusinessType = in.readString();
        Summary = in.readString();
        SummaryDetail = in.readString();
        BizCode = in.readString();
        EventMainTable = in.readString();
        FieldGroup = in.readString();
        SummaryField = in.readString();
        IsCreate = in.readString();
        IsReport = in.readString();
        IsRelatedCase = in.readInt();
        IsStick = in.readInt();
    }
}