package com.repair.zhoushan.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 当前节点的配置信息及流程地图相关表信息
 */
public class FlowTableInfo {

    public FlowTableInfo() {
        FlowInfoConfig = new ArrayList<>();
        TableMetaDatas = new ArrayList<>();
        AssistModules = new ArrayList<>();
        Feedback = new ArrayList<>();
    }

    /**
     * 在办箱：在办节点的流程配置信息；已办箱：经办节点的流程配置信息
     */
    public List<FlowInfoConfig> FlowInfoConfig;

    /**
     * 流程地图相关表的架构
     */
    public List<TableMetaData> TableMetaDatas;

    /**
     * 在办箱：当前节点的辅助模块；已办箱：经办节点的辅助模块
     */
    public List<AssistModule> AssistModules;
    /**
     * 工单办理中的反馈业务
     */
    public List<FeedbackInfo> Feedback;
}
