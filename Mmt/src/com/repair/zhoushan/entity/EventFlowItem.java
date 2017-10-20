package com.repair.zhoushan.entity;

/**
 * 事件发起流程的记录
 */
public class EventFlowItem {

    public EventFlowItem() {
        EventCode = "";
        CaseNo = "";
        FlowName = "";
        CreateManID = "";
        CreateName = "";
        CreateDepart = "";
        CreateTime = "";
        IsOver = "";
        Opinion = "";
    }

    /**
     * 事件编号
     */
    public String EventCode;

    /**
     * 工单编号
     */
    public String CaseNo;

    /**
     * 流程名称
     */
    public String FlowName;

    /**
     * 流程发起人ID
     */
    public String CreateManID;

    /**
     * 流程发起人名称
     */
    public String CreateName;

    /**
     * 流程发起部门
     */
    public String CreateDepart;

    /**
     * 流程发起时间
     */
    public String CreateTime;

    /**
     * 是否结束
     */
    public String IsOver;

    /**
     * 交办意见
     */
    public String Opinion;
}