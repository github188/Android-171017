package com.repair.zhoushan.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 可处理的流程 以及 事件箱的辅助模块
 */
public class DealFlowInfo {

    public DealFlowInfo() {
        EditableFields = "";
        EventInfo = new FlowNodeMeta();
        FlowInfoConfig = new ArrayList<FlowInfoConfig>();
        AssistModules = new ArrayList<AssistModule>();
    }

    /**
     * 编辑字段集
     */
    public String EditableFields;

    /**
     * 事件信息
     */
    public FlowNodeMeta EventInfo;

    /**
     * 可发起的流程
     */
    public List<FlowInfoConfig> FlowInfoConfig;

    /**
     * 事件箱的辅助模块
     */
    public List<AssistModule> AssistModules;
}