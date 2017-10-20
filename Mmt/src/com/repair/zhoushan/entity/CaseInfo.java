package com.repair.zhoushan.entity;

public class CaseInfo {

    public CaseInfo() {
        UserID = 1;
        StepID = 0;
        CaseNo = "";
        FlowName = "";
        NodeName = "";
        Undertakeman = "";
        Opinion = "";
        Direction = 1;
        TableGroup = "";
        Station = "";

        //流程配置信息
        CloseEvent = 0;
        UpdateEvent = "";
        AddappointHandRols = "";
        OperType = "";

        //事件配置信息
        BizCode = "";
        EventCode = "";
        EventName = "";
        EventMainTable = "";
        FieldGroup = "";
        IsCreate = 0;
    }

    /**
     * 案卷办理人ID
     */
    public int UserID;

    /**
     * 案卷节点ID
     */
    public int StepID;

    /**
     * 工单编号
     */
    public String CaseNo;

    /**
     * 案卷流程名称
     */
    public String FlowName;

    /**
     * 流程节点名称
     */
    public String NodeName;

    /**
     * 承办人
     */
    public String Undertakeman;

    /**
     * 交办意见
     */
    public String Opinion;

    /**
     * 1:Forward
     * -1:Back
     * 0:Local
     */
    public int Direction;

    /**
     * 表组
     */
    public String TableGroup;

    /**
     * 站点名称
     */
    public String Station;

    /******流程配置信息*****/

    /**
     * 关闭事件
     */
    public int CloseEvent;

    /**
     * 流程的最新状态更新到事件状态上(可选值)
     */
    public String UpdateEvent;

    /**
     * 在下一个办理人的基础上添加默认移交的角色的承办人
     */
    public String AddappointHandRols;

    /**
     * 操作类型
     */
    public String OperType;

    /******事件配置信息*****/

    /**
     * 事件前缀
     */
    public String BizCode;

    /**
     * 事件编号
     */
    public String EventCode;

    /**
     * 事件名称
     */
    public String EventName;

    /**
     * 事件主表
     */
    public String EventMainTable;

    /**
     * 字段集
     */
    public String FieldGroup;

    /**
     * 是否创建流程
     */
    public int IsCreate;
}