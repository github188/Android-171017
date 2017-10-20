package com.patrolproduct.entity;

import com.google.gson.annotations.SerializedName;

public class FlowReportInfo {
	@SerializedName("CaseNo")
	public String caseNo;

	@SerializedName("EventDesc")
	public String eventDesc;

	@SerializedName("EventSource")
	public String eventSource;

	@SerializedName("EventAddr")
	public String eventAddr;

	@SerializedName("ImageNames")
	public String imageNames;

	@SerializedName("Position")
	public String position;

	@SerializedName("ReportTime")
	public String reportTime;

	@SerializedName("Reporter")
	public String reporter;

	@SerializedName("ReporterId")
	public int reporterId;
}
