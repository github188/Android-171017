package com.repair.zhoushan.entity;

import java.io.Serializable;
import java.util.List;

public class FlowInfoItem implements Serializable {

    public FlowInfoItem() {

//        FlowInfoConfig = new FlowInfoConfig();
//        FlowNodeMeta = new FlowNodeMeta();
//        AssistModules = new ArrayList<>();
    }

    /**
     * 流程基础信息
     */
    public FlowInfoConfig FlowInfoConfig;

    /**
     * 字段架构
     */
    public FlowNodeMeta FlowNodeMeta;

    /**
     * 节点的辅助模块
     */
    public List<AssistModule> AssistModules;

    /**
     * 工单办理中的反馈业务
     */
    public List<FeedbackInfo> Feedback;

}
