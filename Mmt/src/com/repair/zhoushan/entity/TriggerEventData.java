package com.repair.zhoushan.entity;

/**
 * 流程中心数据
 */
public class TriggerEventData {

    public TriggerEventData() {
        flowData = new FlowCenterData();
        nodeMeta = new FlowNodeMeta();
    }

    /**
     * 事件的架构信息
     */
    public FlowNodeMeta nodeMeta;

    /**
     * 流程中心
     */
    public FlowCenterData flowData;
}