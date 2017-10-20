package com.repair.zhoushan.entity;

import com.repair.zhoushan.module.devicecare.MaintenanceFeedBack;

/**
 * Created by liuyunfan on 2016/7/25.
 */
public class FeedbackInfo {
    public String FBBiz = "";
    public String FBTable = "";
    public String FBIncludeFields = "";
    public String FBExcludeFields = "";

    //内部字段，提高代码效率
    public boolean isRead=false;

    public FeedbackInfo(String FBBiz, String FBTable) {
        this(FBBiz, FBTable, "", "");
    }

    public FeedbackInfo(String FBBiz, String FBTable, String FBIncludeFields, String FBExcludeFields) {
        this.FBBiz = FBBiz;
        this.FBTable = FBTable;
        this.FBIncludeFields = FBIncludeFields;
        this.FBExcludeFields = FBExcludeFields;
    }


    public MaintenanceFeedBack feedbackInfo2MaintenanceFeedBack(){
        MaintenanceFeedBack maintenanceFeedBack=new MaintenanceFeedBack();
        maintenanceFeedBack.bizName=this.FBBiz;
        maintenanceFeedBack.deviceFBTbl=this.FBTable;
        maintenanceFeedBack.fileds=this.FBIncludeFields;
        maintenanceFeedBack.excludeFileds=this.FBExcludeFields;
        return maintenanceFeedBack;
    }
}
