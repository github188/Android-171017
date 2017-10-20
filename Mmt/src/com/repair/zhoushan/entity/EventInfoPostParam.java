package com.repair.zhoushan.entity;

/**
 * 事件信息提交
 */
public class EventInfoPostParam {

    public EventInfoPostParam() {
        BizCode = "";
        TableName = "";
        EventName = "";
        DataParam = new FlowInfoPostParam();
    }

    public FlowInfoPostParam DataParam;

    /**
     * 业务编码
     */
    public String BizCode;

    /**
     * 事件名称
     */
    public String EventName;

    /**
     * 事件主表
     */
    public String TableName;
}
