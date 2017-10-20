package com.repair.zhoushan.entity;

public class EventLogItem {

    public EventLogItem() {
        EventCode = "";
        FlowName = "";
        NodeName = "";
        Direction = "";
        OperName = "";
        OperDepart = "";
        OperTime = "";
        Photos = "";
        Audios = "";
        Files = "";
        Description = "";
    }

    /**
     * 事件编码
     */
    public String EventCode;

    /**
     * 流程名称
     */
    public String FlowName;

    /**
     * 节点名称
     */
    public String NodeName;

    /**
     * 移交方向 1：移交 2：受理 -1：回退 -2：撤回
     */
    public String Direction;

    /**
     * 操作人名称
     */
    public String OperName;

    /**
     * 操作人部门
     */
    public String OperDepart;

    /**
     * 操作时间
     */
    public String OperTime;

    /**
     * 图片
     */
    public String Photos;

    /**
     * 录音
     */
    public String Audios;

    /**
     * 附件
     */
    public String Files;

    /**
     * 描述
     */
    public String Description;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
//        builder.append("步骤名称").append("：").append(FlowName == null ? "" : FlowName + "-" + NodeName == null ? "" : NodeName).append("\n");
//        builder.append("承办人    ").append("：").append(OperDepart == null ? "" : OperDepart + "/" + OperName == null ? "" : OperName).append("\n");builder.append("步骤名称").append("：").append(FlowName == null ? "" : FlowName + "-" + NodeName == null ? "" : NodeName).append("\n");
        builder.append("步骤名称").append("：").append(FlowName + "-" + NodeName).append("\n");
        builder.append("承办人    ").append("：").append(OperDepart + "/" + OperName).append("\n");
        builder.append("承办时间").append("：").append(OperTime == null ? "" : OperTime).append("\n");
        builder.append("承办意见").append("：").append(Description == null ? "" : Description);
        return builder.toString();
    }
}
