package com.repair.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class CaseItem extends SortByTimeAndDistanceOper implements Parcelable {
    // 71个业务所需成员变量...

    public String ID0;

    /**
     * 工单流程编码
     */
    public String FlowCaseID;

    // 协助状态
    public String AssistState;

    /**
     * 事件编号
     */
    public String CaseCode;

    /**
     * 流程编码
     */
    public String CaseNO;

    /**
     * 工单任务编号
     */
    public String TaskNO;

    /**
     * 用户ID
     */
    public String UserID;

    /**
     * 用户名称
     */
    public String UserName;

    /**
     * 流程名称
     */
    public String FlowName;

    /**
     * 活动ID
     */
    public String ActiveID;

    /**
     * 活动名称
     */
    public String ActiveName;

    /**
     * 承办意见
     */
    public String Opinion;

    /**
     * 热线上报用户编号
     */
    public String UserCode;

    /**
     * 热线上报用户联系电话
     */
    public String UserTel;

    /**
     * 热线上报用户真实姓名
     */
    public String UserTrueName;

    /**
     * 事件来源
     */
    public String EventSource;

    /**
     * 事件内容
     */
    public String EventClass;

    /**
     * 事件类型
     */
    public String EventType;

    /**
     * 事件ID
     */
    public String EventID;

    /**
     * ID
     */
    public String CaseID;

    /**
     * 客户编号
     */
    public String CID;

    /**
     * 处理状态
     */
    public String State;

    /**
     * 信息编号
     */
    public String Code;

    /**
     * 接报时间
     */
    public String ReceiveTime;

    /**
     * 紧急程度
     */
    public String EmergencyLevel;

    /**
     * 处理级别
     */
    public String Level;

    /**
     * 反映人所属部门
     */
    public String ReportGroup;

    /**
     * 联系电话
     */
    public String ContactTel;

    /**
     * 手机
     */
    public String MobilePhone;

    /**
     * Email
     */
    public String Email;

    /**
     * 反映类别
     */
    public String ReportType;

    /**
     * 反映内容
     */
    public String ReportContent;

    /**
     * 反映来源
     */
    public String ReportSource;

    /**
     * 反映形式
     */
    public String ReportForm;

    /**
     * 反应区域
     */
    public String ReportST;

    /**
     * 表位状态
     */
    public String MeterState;

    /**
     * 预约时间
     */
    public String AppointmentTime;

    /**
     * 受理单位
     */
    public String AcceptDepartment;

    /**
     * 发生地址
     */
    public String Address;

    /**
     * 受理备注
     */
    public String Remark;

    /**
     * 登录员
     */
    public String LoginMan;

    /**
     * 登录站点
     */
    public String LoginST2;

    /**
     * 坐标
     */
    public String Position;

    /**
     * 图片
     */
    public String Picture;

    /**
     * 录音
     */
    public String Recording;

    /**
     * 受理时间,指流程
     */
    public String AcceptTime;

    /**
     * 是否结案
     */
    public String IsOver;

    /**
     * 移交方向
     */
    public String Direction;

    /**
     * 结束时间
     */
    public String FinishTime;

    /**
     * 下一步活动ID
     */
    public String NextStepID;

    /**
     * 流程ID
     */
    public String FlowID;

    /**
     * 挂起人
     */
    public String DepoistMen;

    /**
     * 事件描述
     */
    public String Description;

    /**
     * 挂起时间
     */
    public String DepoistTime;

    /**
     * 挂起原因
     */
    public String DepoistOpinion;

    /**
     * 案卷状态 进行中/已完成/挂起/注销
     */
    public String CaseState;

    /**
     * 派单员ID
     */
    public String DispatchManID;

    /**
     * 工单是否阅读
     */
    public String IsRead;

    /**
     * 预计完成时间
     */
    public String PredictFinishTime;

    /**
     * 延期完成时间
     */
    public String DelayFinishTime;

    // ////////////////////////////////////////////////////////
    // 工单处理过程信息 ////////////////////////////
    // ////////////////////////////////////////////////////////

    /**
     * 反映人
     */
    public String ReportMan;

    /**
     * 反映单位
     */
    public String ReportDepartment;

    /**
     * 上报时间
     */
    public String OccurTime;

    /**
     * 上报时间
     */
    public String ReportTime;

    /**
     * 分派人员
     */
    public String DispatchMan;

    /**
     * 分派单位
     */
    public String DispatchDepartment;

    /**
     * 分派时间
     */
    public String DispatchTime;

    /**
     * 维修人员
     */
    public String RepairMan;

    /**
     * 维修单位
     */
    public String RepairDepartment;

    /**
     * 接单时间
     */
    public String ConfirmTime;

    /**
     * 到场时间
     */
    public String ArriveTime;

    /**
     * 修复时间
     */
    public String RepairTime;

    /**
     * 审核人员
     */
    public String VerifyMan;

    /**
     * 审核部门
     */
    public String VerifyDepartment;

    /**
     * 审核时间
     */
    public String VerifyTime;

    /**
     * 原始上报事件编号
     */
    public String EventCode;

    /**
     * 延期申请推迟的目标完成时间
     */
    public String DelayTargetTime;

    /**
     * 延期申请的发起时间
     */
    public String DelayRequestTime;

    /**
     * 延期申请状态
     */
    public String DelayRequestState;

    /**
     * 关联事件的设备信息
     */
    public String LayerName;
    public String FieldName;
    public String FieldValue;

    /**
     * 表号
     */
    public String WaterMeterNo;

    // ////////////////////////////////////////////////////////
    // 业务所需成员变量,定义结束 ////////////////////////////
    // ////////////////////////////////////////////////////////

    protected CaseItem(Parcel in) {
        ID0 = in.readString();
        FlowCaseID = in.readString();
        AssistState = in.readString();
        CaseCode = in.readString();
        CaseNO = in.readString();
        TaskNO = in.readString();
        UserID = in.readString();
        UserName = in.readString();
        FlowName = in.readString();
        ActiveID = in.readString();
        ActiveName = in.readString();
        Opinion = in.readString();
        UserCode = in.readString();
        UserTel = in.readString();
        UserTrueName = in.readString();
        EventSource = in.readString();
        EventClass = in.readString();
        EventType = in.readString();
        EventID = in.readString();
        CaseID = in.readString();
        CID = in.readString();
        State = in.readString();
        Code = in.readString();
        ReceiveTime = in.readString();
        EmergencyLevel = in.readString();
        Level = in.readString();
        ReportGroup = in.readString();
        ContactTel = in.readString();
        MobilePhone = in.readString();
        Email = in.readString();
        ReportType = in.readString();
        ReportContent = in.readString();
        ReportSource = in.readString();
        ReportForm = in.readString();
        ReportST = in.readString();
        MeterState = in.readString();
        AppointmentTime = in.readString();
        AcceptDepartment = in.readString();
        Address = in.readString();
        Remark = in.readString();
        LoginMan = in.readString();
        LoginST2 = in.readString();
        Position = in.readString();
        Picture = in.readString();
        Recording = in.readString();
        AcceptTime = in.readString();
        IsOver = in.readString();
        Direction = in.readString();
        FinishTime = in.readString();
        NextStepID = in.readString();
        FlowID = in.readString();
        DepoistMen = in.readString();
        Description = in.readString();
        DepoistTime = in.readString();
        DepoistOpinion = in.readString();
        CaseState = in.readString();
        DispatchManID = in.readString();
        IsRead = in.readString();
        PredictFinishTime = in.readString();
        DelayFinishTime = in.readString();
        ReportMan = in.readString();
        ReportDepartment = in.readString();
        OccurTime = in.readString();
        ReportTime = in.readString();
        DispatchMan = in.readString();
        DispatchDepartment = in.readString();
        DispatchTime = in.readString();
        RepairMan = in.readString();
        RepairDepartment = in.readString();
        ConfirmTime = in.readString();
        ArriveTime = in.readString();
        RepairTime = in.readString();
        VerifyMan = in.readString();
        VerifyDepartment = in.readString();
        VerifyTime = in.readString();
        EventCode = in.readString();
        DelayTargetTime = in.readString();
        DelayRequestTime = in.readString();
        DelayRequestState = in.readString();
        LayerName = in.readString();
        FieldName = in.readString();
        FieldValue = in.readString();
        WaterMeterNo = in.readString();
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
    public String getSortTimeKey() {
        return this.DispatchTime;
    }

    @Override
    public String getSortDistanceKey() {
        return Position;
    }

    @Override
    public boolean equals(Object o) {
        return !(o == null || !(o instanceof CaseItem))
                && !(TextUtils.isEmpty(this.CaseID) || TextUtils.isEmpty(((CaseItem) o).CaseID))
                && (this.CaseID.equals(((CaseItem) o).CaseID));
    }

    @Override
    public String toString() {
        return this.CaseID;
    }

    public void calculateRemainTime() {
        String lTime = TextUtils.isEmpty(this.DelayFinishTime) ? this.PredictFinishTime : this.DelayFinishTime;

        this.calculateBetTime(lTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID0);
        dest.writeString(FlowCaseID);
        dest.writeString(AssistState);
        dest.writeString(CaseCode);
        dest.writeString(CaseNO);
        dest.writeString(TaskNO);
        dest.writeString(UserID);
        dest.writeString(UserName);
        dest.writeString(FlowName);
        dest.writeString(ActiveID);
        dest.writeString(ActiveName);
        dest.writeString(Opinion);
        dest.writeString(UserCode);
        dest.writeString(UserTel);
        dest.writeString(UserTrueName);
        dest.writeString(EventSource);
        dest.writeString(EventClass);
        dest.writeString(EventType);
        dest.writeString(EventID);
        dest.writeString(CaseID);
        dest.writeString(CID);
        dest.writeString(State);
        dest.writeString(Code);
        dest.writeString(ReceiveTime);
        dest.writeString(EmergencyLevel);
        dest.writeString(Level);
        dest.writeString(ReportGroup);
        dest.writeString(ContactTel);
        dest.writeString(MobilePhone);
        dest.writeString(Email);
        dest.writeString(ReportType);
        dest.writeString(ReportContent);
        dest.writeString(ReportSource);
        dest.writeString(ReportForm);
        dest.writeString(ReportST);
        dest.writeString(MeterState);
        dest.writeString(AppointmentTime);
        dest.writeString(AcceptDepartment);
        dest.writeString(Address);
        dest.writeString(Remark);
        dest.writeString(LoginMan);
        dest.writeString(LoginST2);
        dest.writeString(Position);
        dest.writeString(Picture);
        dest.writeString(Recording);
        dest.writeString(AcceptTime);
        dest.writeString(IsOver);
        dest.writeString(Direction);
        dest.writeString(FinishTime);
        dest.writeString(NextStepID);
        dest.writeString(FlowID);
        dest.writeString(DepoistMen);
        dest.writeString(Description);
        dest.writeString(DepoistTime);
        dest.writeString(DepoistOpinion);
        dest.writeString(CaseState);
        dest.writeString(DispatchManID);
        dest.writeString(IsRead);
        dest.writeString(PredictFinishTime);
        dest.writeString(DelayFinishTime);
        dest.writeString(ReportMan);
        dest.writeString(ReportDepartment);
        dest.writeString(OccurTime);
        dest.writeString(ReportTime);
        dest.writeString(DispatchMan);
        dest.writeString(DispatchDepartment);
        dest.writeString(DispatchTime);
        dest.writeString(RepairMan);
        dest.writeString(RepairDepartment);
        dest.writeString(ConfirmTime);
        dest.writeString(ArriveTime);
        dest.writeString(RepairTime);
        dest.writeString(VerifyMan);
        dest.writeString(VerifyDepartment);
        dest.writeString(VerifyTime);
        dest.writeString(EventCode);
        dest.writeString(DelayTargetTime);
        dest.writeString(DelayRequestTime);
        dest.writeString(DelayRequestState);
        dest.writeString(LayerName);
        dest.writeString(FieldName);
        dest.writeString(FieldValue);
        dest.writeString(WaterMeterNo);
    }
}
