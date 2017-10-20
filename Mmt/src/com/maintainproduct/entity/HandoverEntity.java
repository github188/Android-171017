package com.maintainproduct.entity;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.login.UserBean;

/** 移交处理数据模型信息 */
public class HandoverEntity {
	// public String userID;
	// public int stepID;
	// public int direction = 1;

	public HandoverEntity(MaintainSimpleInfo simpleInfo) {
        this.flowName=simpleInfo.FlowName;
		this.activeID = simpleInfo.ActiveID;
		this.activeName = simpleInfo.ActiveName;
		this.caseName = simpleInfo.CaseName;
		this.caseNo = simpleInfo.CaseNo;
		this.stepID = simpleInfo.ID0;
		this.userTrueName = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;
		this.userID = MyApplication.getInstance().getUserId() + "";
		this.direction = 1;
        this.nextActiveID=simpleInfo.nextActiveID;
	}

	public int activeID;

	public String activeName;
	public String caseName;
	public String caseNo;
	public int direction = 1;
    public String flowName;
	public String flowID;
	public String nextActiveID;
	public String option;
	public int stepID;
	public String undertakeman;
	public String userID;
	public String userTrueName;
}
