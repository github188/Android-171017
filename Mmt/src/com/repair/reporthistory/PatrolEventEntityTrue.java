package com.repair.reporthistory;

import com.repair.eventreport.EventReportEntity;

public class PatrolEventEntityTrue extends EventReportEntity {
	private String EventState;
	private String ReportTime;
	
	/**
	 * 事件编号：老版本无，新版本才有该字段
	 */
   private String eventCode;

	public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}

	public String getEventState() {
		return EventState;
	}

	public void setEventState(String eventState) {
		EventState = eventState;
	}

	public String getReportTime() {
		return ReportTime;
	}

	public void setReportTime(String ReportTime) {
		this.ReportTime = ReportTime;
	}

	public PatrolEventEntityTrue() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PatrolEventEntityTrue(EventReportEntity pre, String State,
			String ReportTime) {
		this.EventType = pre.EventType;// 大类
		this.EventClass = pre.EventClass; // 小类，内容
		this.Address = pre.Address;
		this.Description = pre.Description;
		this.ImageUrl = pre.ImageUrl;
		this.AudiosUrl = pre.AudiosUrl;
		this.ReportID = pre.ReportID;
		this.ReportName = pre.ReportName;
		this.Position = pre.Position;// 坐标
		this.EventState = State;
		this.ReportTime = ReportTime;
	}
	public PatrolEventEntityTrue(EventReportEntity pre, String State,
			String ReportTime,String eventCode) {
		this.EventType = pre.EventType;// 大类
		this.EventClass = pre.EventClass; // 小类，内容
		this.Address = pre.Address;
		this.Description = pre.Description;
		this.ImageUrl = pre.ImageUrl;
		this.AudiosUrl = pre.AudiosUrl;
		this.ReportID = pre.ReportID;
		this.ReportName = pre.ReportName;
		this.Position = pre.Position;// 坐标
		this.EventState = State;
		this.ReportTime = ReportTime;
		this.eventCode=eventCode;
	}
}
