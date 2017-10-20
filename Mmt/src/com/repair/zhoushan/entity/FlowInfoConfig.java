package com.repair.zhoushan.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 流程信息配置
 */
public class FlowInfoConfig implements Parcelable {

    public FlowInfoConfig() {
        FlowName = "";
        NodeName = "";
        TableName = "";
        TableAlias = "";
        ExportTemp = "";
        ShowTemp = "";
        FieldGroup = "";
        ViewModule = "";
        MobileViewModule = "";
        ViewParam = "";
        AllowCorrect = 0;
        AllowBack = 1;
        BackToNode = "";
        HandOverMode = "";
        OperType = "";
        NodeType = 0;
        ViewState = "";
    }

    /**
     * 流程名称
     */
    public String FlowName;
    /**
     * 节点名称
     */
    public String NodeName;
    /**
     * 表名
     */
    public String TableName;
    /**
     * 表的别名
     */
    public String TableAlias;
    /**
     * 导出模板
     */
    public String ExportTemp;
    /**
     * 显示模板
     */
    public String ShowTemp;
    /**
     * 字段集
     */
    public String FieldGroup;
    /**
     * 视图模块
     */
    public String ViewModule;
    /**
     * 手持视图模块
     */
    public String MobileViewModule;
    /**
     * 视图参数
     */
    public String ViewParam;
    /**
     * 可否补正
     */
    public int AllowCorrect;
    /**
     * 可否回退
     */
    public int AllowBack;
    /**
     * 回退至节点
     */
    public String BackToNode;
    /**
     * 移交方式： 1-移交选择人  2-自处理（移交给自己） 3-跨站移交  4-移交默认人
     */
    public String HandOverMode;
    /**
     * 操作类型： 办理、分派
     */
    public String OperType;
    /**
     * 节点类型 1-开始节点 0-办理节点 2-结束节点
     */
    public int NodeType;
    /**
     * 视图状态
     */
    public String ViewState;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(FlowName);
        out.writeString(NodeName);
        out.writeString(TableName);
        out.writeString(TableAlias);
        out.writeString(ExportTemp);
        out.writeString(ShowTemp);
        out.writeString(FieldGroup);
        out.writeString(ViewModule);
        out.writeString(MobileViewModule);
        out.writeString(ViewParam);
        out.writeInt(AllowCorrect);
        out.writeInt(AllowBack);
        out.writeString(BackToNode);
        out.writeString(HandOverMode);
        out.writeString(OperType);
        out.writeInt(NodeType);
        out.writeString(ViewState);
    }

    public static final Parcelable.Creator<FlowInfoConfig> CREATOR = new Parcelable.Creator<FlowInfoConfig>() {
        @Override
        public FlowInfoConfig createFromParcel(Parcel in) {
            return new FlowInfoConfig(in);
        }

        @Override
        public FlowInfoConfig[] newArray(int size) {
            return new FlowInfoConfig[size];
        }
    };

    private FlowInfoConfig(Parcel in) {
        FlowName = in.readString();
        NodeName = in.readString();
        TableName = in.readString();
        TableAlias = in.readString();
        ExportTemp = in.readString();
        ShowTemp = in.readString();
        FieldGroup = in.readString();
        ViewModule = in.readString();
        MobileViewModule = in.readString();
        ViewParam = in.readString();
        AllowCorrect = in.readInt();
        AllowBack = in.readInt();
        BackToNode = in.readString();
        HandOverMode = in.readString();
        OperType = in.readString();
        NodeType = in.readInt();
        ViewState = in.readString();
    }
}