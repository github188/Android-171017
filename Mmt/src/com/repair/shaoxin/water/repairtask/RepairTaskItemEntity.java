package com.repair.shaoxin.water.repairtask;

import com.google.gson.annotations.SerializedName;
import com.zondy.mapgis.geometry.Dot;

public class RepairTaskItemEntity {

    public int ID0;

    @SerializedName("编号")
    public String no;

    @SerializedName("案件编号")
    public String caseNo;

    @SerializedName("流程名称")
    public String flowName;

    @SerializedName("活动名称")
    public String activityName;

    @SerializedName("案件名称")
    public String caseName;

    @SerializedName("TaskID")
    public int taskID;

    @SerializedName("活动ID")
    public int activityID;

    @SerializedName("流程ID")
    public int flowID;

    @SerializedName("PATROLPOSITION")
    public String patrolPosition;

    @SerializedName("EVENT_SOURCE")
    public String eventSource;

    @SerializedName("NodeType")
    public int nodeType;

    @SerializedName("承办时间")
    public String undertakeTime;

    @SerializedName("EVENTADDR")
    public String eventAddress;

    @SerializedName("OrderID")
    public int orderID;

    @SerializedName("处理时限")
    public int handleTimeLimit;

    public Dot mDot;

}
