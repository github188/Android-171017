package com.repair.zhoushan.module.projectmanage.threepartconstruct.supervisereporthistory;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 第三方施工监管表
 */
public class ConstructSupervision implements Parcelable{

    public ConstructSupervision() {
        CaseNo = "";
        Progress = "";
        IsNormal = "";
        Distance = "";
        DynamicState = "";
        Picture = "";
        ReportTime = "";
        UpdateStatusTime = "";
        UpdateStatus = "";
        FlowName = "";
        ReporterName ="";
    }

    /**
     * 工单编号
     */
    public String CaseNo;

    /**
     * 施工进度
     */
    public String Progress;

    /**
     * 施工是否正常
     */
    public String IsNormal;

    /**
     * 施工点与管道距离
     */
    public String Distance;

    /**
     * 施工动态
     */
    public String DynamicState;

    /**
     * 现场照片
     */
    public String Picture;

    /**
     * 上报时间
     */
    public String ReportTime;

    /**
     * 更新流程更新状态字段的时间
     */
    public String UpdateStatusTime;

    /**
     * 流程的更新状态
     */
    public String UpdateStatus;

    /**
     * 流程名称
     */
    public String FlowName;

    /**
     * 上报人
     */
    public String ReporterName;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(CaseNo);
        out.writeString(Progress);
        out.writeString(IsNormal);
        out.writeString(Distance);
        out.writeString(DynamicState);
        out.writeString(Picture);
        out.writeString(ReportTime);
        out.writeString(UpdateStatusTime);
        out.writeString(UpdateStatus);
        out.writeString(FlowName);
        out.writeString(ReporterName);
    }

    public static final Parcelable.Creator<ConstructSupervision> CREATOR = new Parcelable.Creator<ConstructSupervision>() {
        @Override
        public ConstructSupervision createFromParcel(Parcel in) {
            return new ConstructSupervision(in);
        }

        @Override
        public ConstructSupervision[] newArray(int size) {
            return new ConstructSupervision[size];
        }
    };

    private ConstructSupervision(Parcel in) {

        CaseNo = in.readString();
        Progress = in.readString();
        IsNormal = in.readString();
        Distance = in.readString();
        DynamicState = in.readString();
        Picture = in.readString();
        ReportTime = in.readString();
        UpdateStatusTime = in.readString();
        UpdateStatus = in.readString();
        FlowName = in.readString();
        ReporterName = in.readString();
    }
}