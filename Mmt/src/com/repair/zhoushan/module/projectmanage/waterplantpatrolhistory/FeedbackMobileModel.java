package com.repair.zhoushan.module.projectmanage.waterplantpatrolhistory;

import android.os.Parcel;
import android.os.Parcelable;

public class FeedbackMobileModel implements Parcelable {

    public FeedbackMobileModel() {
        FactoryName = "";
        TaskCode = "";
        TaskFinishMan = "";
        FinishTime = "";
        Type = "";
        Name = "";
        Code = "";
        IsFeedback = "";
        State = "";
    }

    /**
     * 水厂名称
     */
    public String FactoryName;

    /**
     * 任务编号
     */
    public String TaskCode;

    /**
     * 任务养护人
     */
    public String TaskFinishMan;

    /**
     * 完成时间
     */
    public String FinishTime;

    /**
     * 巡检点种类
     */
    public String Type;

    /**
     * 巡检点名称
     */
    public String Name;

    /**
     * 巡检点编码
     */
    public String Code;

    /**
     * 是否反馈
     */
    public String IsFeedback;

    /**
     * 反馈状态
     */
    public String State;

    protected FeedbackMobileModel(Parcel in) {
        FactoryName = in.readString();
        TaskCode = in.readString();
        TaskFinishMan = in.readString();
        FinishTime = in.readString();
        Type = in.readString();
        Name = in.readString();
        Code = in.readString();
        IsFeedback = in.readString();
        State = in.readString();
    }

    public static final Creator<FeedbackMobileModel> CREATOR = new Creator<FeedbackMobileModel>() {
        @Override
        public FeedbackMobileModel createFromParcel(Parcel in) {
            return new FeedbackMobileModel(in);
        }

        @Override
        public FeedbackMobileModel[] newArray(int size) {
            return new FeedbackMobileModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(FactoryName);
        dest.writeString(TaskCode);
        dest.writeString(TaskFinishMan);
        dest.writeString(FinishTime);
        dest.writeString(Type);
        dest.writeString(Name);
        dest.writeString(Code);
        dest.writeString(IsFeedback);
        dest.writeString(State);
    }
}