package com.repair.zhoushan.entity;

public class FeedbackData
{
    public FeedbackData()
    {
        TableName = "";
        DefaultParam = "";
        DataParam = new FlowInfoPostParam();
    }

    /**
     * 反馈表名称
     */
    public String TableName;

    /**
     * 默认参数
     */
    public String DefaultParam;

    /**
     * 反馈字段架构与对应的值
     */
    public FlowInfoPostParam DataParam;
}