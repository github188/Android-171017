package com.repair.huangdao;

public class CaseItemV21 {
    public CaseItemV21() {
        ID0 = "";
        CaseNO = "";
        UserID = "";
        UserName = "";
        FlowName = "";
        ActiveID = "";
        ActiveName = "";
        Opinion = "";
        IsOver = "";
        Direction = "";
        AcceptTime = "";
        FinishTime = "";
        NextStepID = "";
        FlowID = "";
        CaseState = "";
        DepoistMen = "";
        DepoistOpinion = "";
        DepoistTime = "";
        UserCode = "";
        UserTel = "";
        UserTrueName = "";
        AssistState = "";
        EventSource = "";
        EventType = "";
        EventClass = "";
        EventID = "";
        EventCode = "";
        Description = "";
        Position = "";
        Picture = "";
        Recording = "";
        CaseID = "";
        State = "";
        CaseCode = "";
        EmergencyLevel = "";
        Level = "";
        Address = "";
        Remark = "";
        IsRead = "";
        PredictFinishTime = "";
        DelayFinishTime = "";
        RemainFinishTime = "";
        DelayTargetTime = "";
        DelayRequestTime = "";
        DelayRequestState = "";
        ReportMan = "";
        ReportManID = "";
        ReportDepartment = "";
        ReportTime = "";
        DispatchMan = "";
        DispatchManID = "";
        DispatchDepartment = "";
        DispatchTime = "";
        RepairMan = "";
        RepairDepartment = "";
        ReadTime = "";
        ConfirmTime = "";
        ArriveTime = "";
        RepairTime = "";
        StopTime = "";
        VerifyMan = "";
        VerifyManID = "";
        VerifyDepartment = "";
        VerifyTime = "";

        DepartDispatchMan = "";
        DepartDispatchManID = "";
        DepartDispatchDepartment = "";
        DepartDispatchTime = "";
        CompanytDispatchMan = "";
        CompanyDispatchManID = "";
        CompanyDispatchDepartment = "";
        CompanyDispatchTime = "";
        DepartVerifyManID = "";
        DepartVerifyMan = "";
        DepartVerifyDepartment = "";
        DepartVerifyTime = "";
        CompanyVerifyManID = "";
        CompanyVerifyMan = "";
        CompanyVerifyDepartment = "";
        CompanyVerifyTime = "";

        fbStartTime = "";
        fbEventType = "";
        fbDiameter = "";
        fbDepth = "";
        fbStopRange = "";
        fbUserNotice = "";
        fbTempWater = "";
        fbWorkRetore = "";
        fbTroops = "";
        fbRepairManID = 0;
        fbRepairMan = "";
        fbRepairGroup = "";
        fbDevice = "";
        fbMaterial = "";
        fbPhotoBefore = "";
        fbPhotoMid = "";
        fbPhotoAfter = "";
        fbReason = "";
        fbSituation = "";
        fbEndTime = "";

        EventFile="";
        RepairManID="";
    }

    /// 步骤ID
    public String ID0;
    /// 流程编码
    public String CaseNO;
    /// 流程用户ID
    public String UserID;
    /// 流程用户名称
    public String UserName;
    /// 流程名称
    public String FlowName;
    /// 活动ID
    public String ActiveID;
    /// 活动名称
    public String ActiveName;
    /// 承办意见
    public String Opinion;
    /// 是否结案
    public String IsOver;
    /// 移交方向
    public String Direction;
    /// 受理时间,指流程
    public String AcceptTime;
    /// 结束时间
    public String FinishTime;
    /// 下一步活动ID
    public String NextStepID;
    /// 流程ID
    public String FlowID;
    /// 案卷状态 进行中/已完成/挂起/注销
    public String CaseState;
    /// 挂起人
    public String DepoistMen;
    /// 挂起原因
    public String DepoistOpinion;
    /// 挂起时间
    public String DepoistTime;


    /// 热线上报用户编号
    public String UserCode;
    /// 热线上报用户联系电话
    public String UserTel;
    /// 热线上报用户真实姓名
    public String UserTrueName;
    /// 事件来源
    public String EventSource;
    /// 事件类型
    public String EventType;
    /// 事件内容
    public String EventClass;
    /// 事件ID
    public String EventID;
    /// 事件编码
    public String EventCode;
    /// 事件描述
    public String Description;
    /// 坐标
    public String Position;
    /// 图片
    public String Picture;
    /// 录音
    public String Recording;
    /// 文件
    public String EventFile;

    /// CaseID
    public String CaseID;
    /// 处理状态
    public String State;
    /// 信息编号
    public String CaseCode;
    /// 紧急程度
    public String EmergencyLevel;
    /// 处理级别
    public String Level;
    /// 发生地址
    public String Address;
    /// 受理备注
    public String Remark;
    // 协助状态
    public String AssistState;


    /// 工单是否阅读
    public String IsRead;
    /// 预计完成时间
    public String PredictFinishTime;
    /// 延期完成时间
    public String DelayFinishTime;
    /// 剩余完成时间
    public String RemainFinishTime;
    /// 延期申请推迟的目标完成时间
    public String DelayTargetTime;
    /// 延期申请的发起时间
    public String DelayRequestTime;
    /// 延期申请状态
    public String DelayRequestState;

    /// 反映人
    public String ReportMan;
    /// 反映人ID
    public String ReportManID;
    /// 反映单位
    public String ReportDepartment;
    /// 上报时间
    public String ReportTime;
    /// 分派人员
    public String DispatchMan;
    /// 派单员ID
    public String DispatchManID;
    /// 分派单位
    public String DispatchDepartment;
    /// 分派时间
    public String DispatchTime;
    /// 部门分派人员
    public String DepartDispatchMan;
    /// 部门派单员ID
    public String DepartDispatchManID;
    /// 部门分派单位
    public String DepartDispatchDepartment;
    /// 部门分派时间
    public String DepartDispatchTime;
    /// 公司分派人员
    public String CompanytDispatchMan;
    /// 公司派单员ID
    public String CompanyDispatchManID;
    /// 公司分派单位
    public String CompanyDispatchDepartment;
    /// 公司分派时间
    public String CompanyDispatchTime;

    /// 维修人员
    public String RepairMan;
    /// 维修人员ID
    public String RepairManID;
    /// 维修单位
    public String RepairDepartment;
    /// 接单时间
    public String ReadTime;
    /// 接单时间
    public String ConfirmTime;
    /// 到场时间
    public String ArriveTime;
    /// 维修时间
    public String RepairTime;
    /// 完工时间
    public String StopTime;
    /// 审核人员
    public String VerifyMan;
    /// 审核人员ID
    public String VerifyManID;
    /// 审核部门
    public String VerifyDepartment;
    /// 审核时间
    public String VerifyTime;

    /// 部门审核人员ID
    public String DepartVerifyManID;
    /// 部门审核人员
    public String DepartVerifyMan;
    /// 部门审核部门
    public String DepartVerifyDepartment;
    /// 部门审核时间
    public String DepartVerifyTime;
    // 公司审核人员ID
    public String CompanyVerifyManID;
    /// 公司审核人员
    public String CompanyVerifyMan;
    /// 公司审核部门
    public String CompanyVerifyDepartment;
    /// 公司审核时间
    public String CompanyVerifyTime;

    /// 开始时间
    public String fbStartTime;
    /// 事件类型
    public String fbEventType;
    /// 维修管径
    public String fbDiameter;
    /// 埋深
    public String fbDepth;
    /// 停水影响范围
    public String fbStopRange;
    /// 用户告知情况
    public String fbUserNotice;
    /// 临时水供应情况
    public String fbTempWater;
    /// 工作面恢复情况
    public String fbWorkRetore;
    /// 工建队伍
    public String fbTroops;
    /// 维修人员ID
    public int fbRepairManID;
    /// 维修人员
    public String fbRepairMan;
    /// 维修部门
    public String fbRepairGroup;
    /// 机械设备
    public String fbDevice;
    /// 用料情况
    public String fbMaterial;
    /// 现场照片前
    public String fbPhotoBefore;
    /// 现场照片中
    public String fbPhotoMid;
    /// 现场照片后
    public String fbPhotoAfter;
    /// 事故原因
    public String fbReason;
    /// 维修情况
    public String fbSituation;
    /// 结束时间
    public String fbEndTime;
}
