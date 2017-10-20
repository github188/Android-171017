package com.repair.zhoushan.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 辅助模块配置
 */
public class AssistModule implements Parcelable {

    public AssistModule() {
        FlowName = "";
        NodeName = "";

        ViewLabel = "";
        ViewModule = "";
        ViewParam = "";

        MobileViewLabel = "";
        MobileViewModule = "";
        MobileViewParam = "";

        Order = 0;
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
     * 视图标签
     */
    public String ViewLabel;
    /**
     * 视图模块
     */
    public String ViewModule;
    /**
     * 视图参数
     */
    public String ViewParam;
    /**
     * 手持视图标签
     */
    public String MobileViewLabel;
    /**
     * 手持视图模块
     */
    public String MobileViewModule;
    /**
     * 手持视图参数
     */
    public String MobileViewParam;
    /**
     * 显示顺序
     */
    public int Order;

    protected AssistModule(Parcel in) {
        FlowName = in.readString();
        NodeName = in.readString();
        ViewLabel = in.readString();
        ViewModule = in.readString();
        ViewParam = in.readString();
        MobileViewLabel = in.readString();
        MobileViewModule = in.readString();
        MobileViewParam = in.readString();
        Order = in.readInt();
    }

    public static final Creator<AssistModule> CREATOR = new Creator<AssistModule>() {
        @Override
        public AssistModule createFromParcel(Parcel in) {
            return new AssistModule(in);
        }

        @Override
        public AssistModule[] newArray(int size) {
            return new AssistModule[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(FlowName);
        dest.writeString(NodeName);
        dest.writeString(ViewLabel);
        dest.writeString(ViewModule);
        dest.writeString(ViewParam);
        dest.writeString(MobileViewLabel);
        dest.writeString(MobileViewModule);
        dest.writeString(MobileViewParam);
        dest.writeInt(Order);
    }
}