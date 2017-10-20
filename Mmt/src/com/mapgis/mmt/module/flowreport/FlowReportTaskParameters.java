package com.mapgis.mmt.module.flowreport;

import android.content.ContentValues;
import android.database.Cursor;

import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.net.BaseTaskParameters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FlowReportTaskParameters extends BaseTaskParameters implements ISQLiteOper {

	public FlowReportTaskParameters() {
	}

	public FlowReportTaskParameters(String url) {
		super(url);
	}

	private int userId;
	private String flowId;
	private String caseName;
	private String roadName;
	private int state;

	/**
	 * 上报事件保存的临时对象SaveReportInfo生成的Guid对应的TaskID
	 */
	private String reportRowId;

	public String getReportRowId() {
		return reportRowId;
	}

	public void setReportRowId(String reportRowId) {
		this.reportRowId = reportRowId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public String getCaseName() {
		return caseName;
	}

	public void setCaseName(String caseName) {
		this.caseName = caseName;
	}

	public String getRoadName() {
		return roadName;
	}

	public void setRoadName(String roadName) {
		this.roadName = roadName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getPipeId() {
		return pipeId;
	}

	public void setPipeId(String pipeId) {
		this.pipeId = pipeId;
	}

	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public String getEventSituation() {
		return eventSituation;
	}

	public void setEventSituation(String eventSituation) {
		this.eventSituation = eventSituation;
	}

	public byte[] getPicture() {
		return picture;
	}

	public void setPicture(byte[] picture) {
		this.picture = picture;
	}

	private String content;
	private String position;
	private String pipeId;
	private String layerName;
	private String eventSituation;
	private byte[] picture;
	private String flowName;
	private String caseDesc;
	private String mediaString;
	private String recordString;
	private String time;

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@Override
	public Map<String, String> generateRequestParams() {
		Map<String, String> params = new HashMap<String, String>();

		// params.put("caseName", caseName);
		// params.put("Addr", roadName);
		// params.put("Desc", content);
		// params.put("Position", position);
		// params.put("ETime", time);

		// params.put("flowId", flowId);
		// params.put("pipeId", pipeId);
		// params.put("tableName", layerName);
		// params.put("eventSituation", eventSituation);

		params.put("UserID", String.valueOf(userId));
		params.put("EType", caseName);
		params.put("Desc", content);
		params.put("Addr", roadName);
		params.put("Position", position);

		params.put("EquipEntity", pipeId);
		params.put("EquipType", layerName);

		params.put("CaseNO", recordString + "," + photoNames);

		return params;
	}

	@Override
	public boolean validate() {
		return false;
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public String getCaseDesc() {
		return caseDesc;
	}

	public void setCaseDesc(String caseDesc) {
		this.caseDesc = caseDesc;
	}

	public String getMediaString() {
		return mediaString;
	}

	public void setMediaString(String mediaString) {
		this.mediaString = mediaString;
	}

	public String getRecordString() {
		return recordString;
	}

	public void setRecordString(String recordString) {
		this.recordString = recordString;
	}

	public String recPaths;
	public String photoNames;

	@Override
	public ContentValues generateContentValues() {
		ContentValues cv = new ContentValues();

		cv.put("userId", userId);
		cv.put("flowid", flowId);
		cv.put("flowName", flowName);
		cv.put("caseId", caseName);
		cv.put("caseName", caseDesc);
		cv.put("roadName", roadName);
		cv.put("position", position);
		cv.put("content", content);
		cv.put("option", eventSituation);
		cv.put("pipeId", pipeId);
		cv.put("layerName", layerName);

		cv.put("media", mediaString);
		cv.put("record", recordString);

		cv.put("state", state);

		cv.put("reportRowId", reportRowId);

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
			cv.put("time", simpleDateFormat.parse(time).getTime());
		} catch (ParseException e) {
			e.printStackTrace();

			cv.put("time", new Date().getTime());
		}

		return cv;
	}

	@Override
	public void buildFromCursor(Cursor cursor) {
		this.userId = cursor.getInt(0);
		this.flowId = cursor.getString(1);
		this.flowName = cursor.getString(2);
		this.caseName = cursor.getString(3);
		this.caseDesc = cursor.getString(4);
		this.roadName = cursor.getString(5);
		this.position = cursor.getString(6);
		this.content = cursor.getString(7);
		this.eventSituation = cursor.getString(8);
		this.pipeId = cursor.getString(9);
		this.time = cursor.getString(10);
		this.mediaString = cursor.getString(11);
		this.recordString = cursor.getString(12);
		this.layerName = cursor.getString(13);
		this.state = cursor.getInt(14);
		this.reportRowId = cursor.getString(15);
	}

	@Override
	public SQLiteQueryParameters getSqLiteQueryParameters() {
		return null;
	}

	@Override
	public String getTableName() {
		return "EventHistory";
	}

	@Override
	public String getCreateTableSQL() {
		return "(userId,flowid,flowName,caseId,caseName,roadName,position,content,option,pipeId,time,media,record,layerName,state,reportRowId )";
	}
}
