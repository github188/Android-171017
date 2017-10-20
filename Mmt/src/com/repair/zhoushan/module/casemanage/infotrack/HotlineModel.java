package com.repair.zhoushan.module.casemanage.infotrack;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 承诺服务信息跟踪事件 Model
 */
public class HotlineModel implements Parcelable {

    public HotlineModel() {
        ID = 0;
        EventName = "";
        EventCode = "";
        EventState = "";
        FollowState = "";
        DealStation = "";
        ReporterName = "";
        ReporterDepart = "";
        ReportTime = "";
        UpdateTime = "";
        UpdateState = "";
        XY = "";
        Summary = "";

        MobileCaller = "";
        TelNum = "";
        CallTime = "";
        Address = "";
        Content = "";
        Surveyor = "";
        SurveyTime = "";
        SurveyRemark = "";
        Solution = "";
        DealResult = "";
        DealRemark = "";
    }

    //region 手机本地为列表显示额外添加的字段

    /**
     * 距离当前坐标的距离
     */
    public double Distance;

    /**
     * 距离当前坐标的距离的用于显示的字符串
     */
    public String DistanceStr;

    //endregion

    /**
     * ID
     */
    public int ID;

    /**
     * 事件类型
     */
    public String EventName;

    /**
     * 事件编号
     */
    public String EventCode;

    /**
     * 事件状态
     */
    public String EventState;

    /**
     * 跟踪状态
     */
    public String FollowState;

    /**
     * 处理站点
     */
    public String DealStation;

    /**
     * 上报人
     */
    public String ReporterName;

    /**
     * 上报部门
     */
    public String ReporterDepart;

    /**
     * 上报时间
     */
    public String ReportTime;

    /**
     * 更新时间
     */
    public String UpdateTime;

    /**
     * 更新状态
     */
    public String UpdateState;

    /**
     * 坐标位置
     */
    public String XY;

    /**
     * 事件摘要
     */
    public String Summary;


    /**
     * 反映人
     */
    public String MobileCaller;

    /**
     * 来电号码
     */
    public String TelNum;

    /**
     * 反映时间
     */
    public String CallTime;

    /**
     * 发生地址
     */
    public String Address;

    /**
     * 反映内容
     */
    public String Content;

    /**
     * 勘察人
     */
    public String Surveyor;

    /**
     * 勘查时间
     */
    public String SurveyTime;

    /**
     * 现场勘查情况
     */
    public String SurveyRemark;

    /**
     * 解决措施
     */
    public String Solution;

    /**
     * 处理结果
     */
    public String DealResult;

    /**
     * 处理备注
     */
    public String DealRemark;

    protected HotlineModel(Parcel in) {
        ID = in.readInt();
        EventName = in.readString();
        EventCode = in.readString();
        EventState = in.readString();
        FollowState = in.readString();
        DealStation = in.readString();
        ReporterName = in.readString();
        ReporterDepart = in.readString();
        ReportTime = in.readString();
        UpdateTime = in.readString();
        UpdateState = in.readString();
        XY = in.readString();
        Summary = in.readString();
        MobileCaller = in.readString();
        TelNum = in.readString();
        CallTime = in.readString();
        Address = in.readString();
        Content = in.readString();
        Surveyor = in.readString();
        SurveyTime = in.readString();
        SurveyRemark = in.readString();
        Solution = in.readString();
        DealResult = in.readString();
        DealRemark = in.readString();
    }

    public static final Creator<HotlineModel> CREATOR = new Creator<HotlineModel>() {
        @Override
        public HotlineModel createFromParcel(Parcel in) {
            return new HotlineModel(in);
        }

        @Override
        public HotlineModel[] newArray(int size) {
            return new HotlineModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ID);
        dest.writeString(EventName);
        dest.writeString(EventCode);
        dest.writeString(EventState);
        dest.writeString(FollowState);
        dest.writeString(DealStation);
        dest.writeString(ReporterName);
        dest.writeString(ReporterDepart);
        dest.writeString(ReportTime);
        dest.writeString(UpdateTime);
        dest.writeString(UpdateState);
        dest.writeString(XY);
        dest.writeString(Summary);
        dest.writeString(MobileCaller);
        dest.writeString(TelNum);
        dest.writeString(CallTime);
        dest.writeString(Address);
        dest.writeString(Content);
        dest.writeString(Surveyor);
        dest.writeString(SurveyTime);
        dest.writeString(SurveyRemark);
        dest.writeString(Solution);
        dest.writeString(DealResult);
        dest.writeString(DealRemark);
    }
}