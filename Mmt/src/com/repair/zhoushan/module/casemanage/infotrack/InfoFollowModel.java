package com.repair.zhoushan.module.casemanage.infotrack;

/**
 * 信息跟踪 Model
 */
public class InfoFollowModel {

    public InfoFollowModel() {
        ID = 0;
        EventCode = "";
        MainStatus = "";
        DealStation = "";
        ReportTime = "";
        FollowTime = "";
        Result = "";
        Remark = "";
        Follower = "";
        FillTime = "";
    }

    /**
     * ID
     */
    public int ID;

    /**
     * 事件编号
     */
    public String EventCode;

    /**
     * 主状态
     */
    public String MainStatus;

    /**
     * 处理站点
     */
    public String DealStation;

    /**
     * 上报时间
     */
    public String ReportTime;

    /**
     * 跟踪时间
     */
    public String FollowTime;

    /**
     * 跟踪结果
     */
    public String Result;

    /**
     * 备注
     */
    public String Remark;

    /**
     * 跟踪登录员
     */
    public String Follower;

    /**
     * 跟踪输入时间
     */
    public String FillTime;
}