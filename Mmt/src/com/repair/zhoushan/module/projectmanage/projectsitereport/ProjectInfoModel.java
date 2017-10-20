package com.repair.zhoushan.module.projectmanage.projectsitereport;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 工程信息 Model
 */
public class ProjectInfoModel implements Parcelable {

    public ProjectInfoModel() {
        ID = "";
        CaseNo = "";
        ProjectType = "";
        ProjectName = "";
        PlanFund = "";
        FundSource = "";
        PlanBeginDate = "";
        PlanEndDate = "";
        DealStation = "";
        ProjectDesc = "";
        Reporter = "";
        ReportTime = "";
        PlanArea = "";
        IsBigProject = "";
        Team = "";
        ActiveName = "";
        ConstructProgress = "";
        Photo = "";
        ProjectContractNo = "";
        ProjectReplyNo = "";
    }

    public String ID;

    /**
     * 工程编号
     */
    public String CaseNo;

    /**
     * 项目类别
     */
    public String ProjectType;

    /**
     * 项目名称
     */
    public String ProjectName;

    /**
     * 计划资金
     */
    public String PlanFund;

    /**
     * 资金来源
     */
    public String FundSource;

    /**
     * 立项申请时间
     */
    public String ApprovalTime;

    /**
     * 计划实施开始时间
     */
    public String PlanBeginDate;

    /**
     * 计划实施结束时间
     */
    public String PlanEndDate;

    /**
     * 实施部门
     */
    public String DealStation;

    /**
     * 工程概况
     */
    public String ProjectDesc;

    /**
     * 发起人
     */
    public String Reporter;

    /**
     * 发起时间
     */
    public String ReportTime;

    /**
     * 计划区域
     */
    public String PlanArea;

    /**
     * 是否重大项目
     */
    public String IsBigProject;

    /**
     * 实施班组
     */
    public String Team;

    /**
     * 项目进度/当前工程节点
     */
    public String ActiveName;

    /**
     * 施工进度
     */
    public String ConstructProgress;

    /**
     * 施工现场图片
     */
    public String Photo;

    /**
     * 工程合同编号
     */
    public String ProjectContractNo;

    /**
     * 项目批复号
     */
    public String ProjectReplyNo;

    protected ProjectInfoModel(Parcel in) {
        ID = in.readString();
        CaseNo = in.readString();
        ProjectType = in.readString();
        ProjectName = in.readString();
        PlanFund = in.readString();
        FundSource = in.readString();
        ApprovalTime = in.readString();
        PlanBeginDate = in.readString();
        PlanEndDate = in.readString();
        DealStation = in.readString();
        ProjectDesc = in.readString();
        Reporter = in.readString();
        ReportTime = in.readString();
        PlanArea = in.readString();
        IsBigProject = in.readString();
        Team = in.readString();
        ActiveName = in.readString();
        ConstructProgress = in.readString();
        Photo = in.readString();
        ProjectContractNo = in.readString();
        ProjectReplyNo = in.readString();
    }

    public static final Creator<ProjectInfoModel> CREATOR = new Creator<ProjectInfoModel>() {
        @Override
        public ProjectInfoModel createFromParcel(Parcel in) {
            return new ProjectInfoModel(in);
        }

        @Override
        public ProjectInfoModel[] newArray(int size) {
            return new ProjectInfoModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ID);
        dest.writeString(CaseNo);
        dest.writeString(ProjectType);
        dest.writeString(ProjectName);
        dest.writeString(PlanFund);
        dest.writeString(FundSource);
        dest.writeString(ApprovalTime);
        dest.writeString(PlanBeginDate);
        dest.writeString(PlanEndDate);
        dest.writeString(DealStation);
        dest.writeString(ProjectDesc);
        dest.writeString(Reporter);
        dest.writeString(ReportTime);
        dest.writeString(PlanArea);
        dest.writeString(IsBigProject);
        dest.writeString(Team);
        dest.writeString(ActiveName);
        dest.writeString(ConstructProgress);
        dest.writeString(Photo);
        dest.writeString(ProjectContractNo);
        dest.writeString(ProjectReplyNo);
    }
}