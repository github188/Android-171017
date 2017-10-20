package com.repair.zhoushan.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.mapgis.mmt.MyApplication;

public class CaseItem implements Parcelable {

    public CaseItem() {
        // 工单信息
        CaseNo = "";
        ActiveName = "";
        NodeType = "";
        ActiveID = "";
        NextNodeID = "";
        NextStepID = "";
        IsOver = "";
        StepID = "";
        FlowID = "";
        FlowName = "";
        UnderTakeMan = "";
        UnderTakeManID = "";
        UnderTakeTime = "";
        Direction = "";
        Opinion = "";
        FinishTime = "";
        UndertakeNodes = "";
        DoingPage = "";
        MobileDoingPage = "";
        CloseEvent = 0;
        OperType = "";
        FlowMap = "";
        IsOverTime = "";
        OverTimeInfo = "";
        ReadCaseTime = "";
        // 事件信息
        BusinessType = "";
        EventName = "";
        EventCode = "";
        EventState = "";
        Station = "";
        ReporterName = "";
        ReporterDepart = "";
        ReportTime = "";
        XY = "";
        Summary = "";
        SummaryDetail = "";
        BusinessCode = "";
        EventMainTable = "";
        FieldGroup = "";
        SummaryField = "";
        EventPurview = "";
        IsCreate = "";
        IsReport = "";
        InterfaceConfig = "";
        GeoPath = "";
        GeoArea = "";

        IsArrive = -1;
        IsFeedback = -1;
    }

    /**
     * 工单编号
     */
    public String CaseNo;

    /**
     * 事件名称
     */
    public String EventName;

    /**
     * 事件编码
     */
    public String EventCode;

    /**
     * 事件状态
     */
    public String EventState;

    /**
     * 站点名称
     */
    public String Station;

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
     * 坐标位置
     */
    public String XY;

    /**
     * 业务类型
     */
    public String BusinessType;

    /**
     * 案卷ID
     */
    public String StepID;
    /**
     * 事件摘要
     */
    public String Summary;
    /**
     * 事件摘要详情，包括摘要字段和摘要字段值
     */
    public String SummaryDetail;
    /**
     * 节点名称
     */
    public String ActiveName;

    /**
     * 节点类型
     */
    public String NodeType;

    /**
     * 活动ID
     */
    public String ActiveID;

    /**
     * 下一节点ID
     */
    public String NextNodeID;

    /**
     * 下一节点ID0
     */
    public String NextStepID;

    /**
     * 流程是否结束
     */
    public String IsOver;

    /**
     * 活动名称
     */
    public String FlowName;

    /**
     * 活动ID
     */
    public String FlowID;

    /**
     * 承办人
     */
    public String UnderTakeMan;

    /**
     * 承办人ID
     */
    public String UnderTakeManID;

    /**
     * 承办时间
     */
    public String UnderTakeTime;

    /**
     * 办结时间
     */
    public String FinishTime;
    /**
     * 移交方向
     */
    public String Direction;

    /**
     * 承办意见
     */
    public String Opinion;

    /**
     * 已办节点信息
     */
    public String UndertakeNodes;

    /**
     * 办理页面
     */
    public String DoingPage;

    /**
     * 手持办理页面
     */
    public String MobileDoingPage;

    /**
     * 关闭事件
     */
    public int CloseEvent;

    /**
     * 操作类型 办理-分派-审核
     */
    public String OperType;

    /**
     * 流程地图
     */
    public String FlowMap;

    /**
     * 是否超时： 超时 "1" ，没有超时 "0"，未配置显示""
     */
    public String IsOverTime;

    /**
     * 超时信息
     */
    public String OverTimeInfo;

    /**
     * 阅单时间
     */
    public String ReadCaseTime;

    /**
     * 接单状态
     */
    public String ReceivedState;

    /////////////////////
    /////事件相关信息//////
    /////////////////////

    /**
     * 业务编码
     */
    public String BusinessCode;

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
     * 事件权限
     */
    public String EventPurview;

    /**
     * 是否发起
     */
    public String IsCreate;

    /**
     * 是否上报
     */
    public String IsReport;

    /**
     * 接口配置
     */
    public String InterfaceConfig;

    /**
     * 路径位置
     */
    public String GeoPath;

    /**
     * 区域位置
     */
    public String GeoArea;

    /**
     * 是否到位（绍兴）
     */
    public int IsArrive = -1;

    /**
     * 是否反馈（绍兴）
     */
    public int IsFeedback = -1;

    /* 手机本地为列表显示额外添加的字段 */

    /**
     * 距离当前坐标的距离
     */
    public double Distance;

    /**
     * 距离当前坐标的距离的用于显示的字符串
     */
    public String DistanceStr;

    protected CaseItem(Parcel in) {
        CaseNo = in.readString();
        EventName = in.readString();
        EventCode = in.readString();
        EventState = in.readString();
        Station = in.readString();
        ReporterName = in.readString();
        ReporterDepart = in.readString();
        ReportTime = in.readString();
        XY = in.readString();
        BusinessType = in.readString();
        StepID = in.readString();
        Summary = in.readString();
        SummaryDetail = in.readString();
        ActiveName = in.readString();
        NodeType = in.readString();
        ActiveID = in.readString();
        NextNodeID = in.readString();
        NextStepID = in.readString();
        IsOver = in.readString();
        FlowName = in.readString();
        FlowID = in.readString();
        UnderTakeMan = in.readString();
        UnderTakeManID = in.readString();
        UnderTakeTime = in.readString();
        FinishTime = in.readString();
        Direction = in.readString();
        Opinion = in.readString();
        UndertakeNodes = in.readString();
        DoingPage = in.readString();
        MobileDoingPage = in.readString();
        CloseEvent = in.readInt();
        OperType = in.readString();
        FlowMap = in.readString();
        IsOverTime = in.readString();
        OverTimeInfo = in.readString();
        IsOverTime = in.readString();
        OverTimeInfo = in.readString();
        ReadCaseTime = in.readString();
        ReceivedState = in.readString();
        BusinessCode = in.readString();
        EventMainTable = in.readString();
        FieldGroup = in.readString();
        SummaryField = in.readString();
        EventPurview = in.readString();
        IsCreate = in.readString();
        IsReport = in.readString();
        InterfaceConfig = in.readString();
        GeoPath = in.readString();
        GeoArea = in.readString();
        IsArrive = in.readInt();
        IsFeedback = in.readInt();
    }

    public static final Creator<CaseItem> CREATOR = new Creator<CaseItem>() {
        @Override
        public CaseItem createFromParcel(Parcel in) {
            return new CaseItem(in);
        }

        @Override
        public CaseItem[] newArray(int size) {
            return new CaseItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(CaseNo);
        dest.writeString(EventName);
        dest.writeString(EventCode);
        dest.writeString(EventState);
        dest.writeString(Station);
        dest.writeString(ReporterName);
        dest.writeString(ReporterDepart);
        dest.writeString(ReportTime);
        dest.writeString(XY);
        dest.writeString(BusinessType);
        dest.writeString(StepID);
        dest.writeString(Summary);
        dest.writeString(SummaryDetail);
        dest.writeString(ActiveName);
        dest.writeString(NodeType);
        dest.writeString(ActiveID);
        dest.writeString(NextNodeID);
        dest.writeString(NextStepID);
        dest.writeString(IsOver);
        dest.writeString(FlowName);
        dest.writeString(FlowID);
        dest.writeString(UnderTakeMan);
        dest.writeString(UnderTakeManID);
        dest.writeString(UnderTakeTime);
        dest.writeString(FinishTime);
        dest.writeString(Direction);
        dest.writeString(Opinion);
        dest.writeString(UndertakeNodes);
        dest.writeString(DoingPage);
        dest.writeString(MobileDoingPage);
        dest.writeInt(CloseEvent);
        dest.writeString(OperType);
        dest.writeString(FlowMap);
        dest.writeString(IsOverTime);
        dest.writeString(OverTimeInfo);
        dest.writeString(IsOverTime);
        dest.writeString(OverTimeInfo);
        dest.writeString(ReadCaseTime);
        dest.writeString(ReceivedState);
        dest.writeString(BusinessCode);
        dest.writeString(EventMainTable);
        dest.writeString(FieldGroup);
        dest.writeString(SummaryField);
        dest.writeString(EventPurview);
        dest.writeString(IsCreate);
        dest.writeString(IsReport);
        dest.writeString(InterfaceConfig);
        dest.writeString(GeoPath);
        dest.writeString(GeoArea);
        dest.writeInt(IsArrive);
        dest.writeInt(IsFeedback);
    }

    public CaseInfo mapToCaseInfo() {

        CaseInfo caseInfo = new CaseInfo();

        caseInfo.UserID = MyApplication.getInstance().getUserId();
        caseInfo.StepID = Integer.parseInt(this.StepID);
        caseInfo.CaseNo = this.CaseNo;
        caseInfo.FlowName = this.FlowName;
        caseInfo.NodeName = this.ActiveName;
        caseInfo.Direction = Integer.parseInt(this.Direction);
        caseInfo.TableGroup = "";

        // 不能要
        // caseInfo.Opinion = this.Opinion;
        // caseInfo.Undertakeman = this.UnderTakeMan;

        caseInfo.Station = this.Station;
        caseInfo.CloseEvent = this.CloseEvent;

        //事件配置信息
        caseInfo.EventCode = this.EventCode;
        caseInfo.EventName = this.EventName;
        caseInfo.EventMainTable = this.EventMainTable;
        caseInfo.FieldGroup = this.FieldGroup;
        caseInfo.OperType = this.OperType;
        try {
            caseInfo.IsCreate = Integer.parseInt(this.IsCreate);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return caseInfo;
    }
}