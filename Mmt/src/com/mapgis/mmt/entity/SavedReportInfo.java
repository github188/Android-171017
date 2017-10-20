package com.mapgis.mmt.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.module.taskcontrol.ITaskControlOper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SavedReportInfo implements ISQLiteOper, ITaskControlOper {
	int id;
	String taskId;
	String reportContent;
	String medias;
	String state;
	String reportType;

	public SavedReportInfo() {
	}

	public SavedReportInfo(Object taskId, String reportContent, String records, String reportType) {
		this(taskId, reportContent, "", records, "unreported", "unreported", reportType);
	}

	public SavedReportInfo(Object taskId, String reportContent, String medias, String records, String reportType) {
		this(taskId, reportContent, medias, records, "unreported", "unreported", reportType);
	}

	public SavedReportInfo(Object taskId, String reportContent, String medias, String records, String state, String bigPicState,
			String reportType) {
		this.taskId = String.valueOf(taskId);
		this.reportContent = reportContent;
		this.medias = medias;
		this.state = state;
		this.reportType = reportType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getReportContent() {
		return reportContent;
	}

	public void setReportContent(String reportContent) {
		this.reportContent = reportContent;
	}

	public String getMedias() {
		return medias;
	}

	public void setMedias(String medias) {
		this.medias = medias;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	@Override
	public String getTableName() {
		return "FeedbackInfo";
	}

	@Override
	public ContentValues generateContentValues() {
		ContentValues cv = new ContentValues();

		cv.put("taskId", this.taskId);
		cv.put("reportContent", this.reportContent);
		cv.put("medias", this.medias);
		cv.put("state", this.state);
		cv.put("reportType", this.reportType);

		return cv;
	}

	@Override
	public void buildFromCursor(Cursor cursor) {
		this.id = cursor.getInt(0);
		this.taskId = cursor.getString(1);
		this.reportContent = cursor.getString(2);
		this.medias = cursor.getString(3);
		this.state = cursor.getString(4);
		this.reportType = cursor.getString(5);
	}

	public List<String> getMediaList() {
		List<String> photoNames = new ArrayList<String>();

		if (this.medias != null && this.medias.length() > 0) {
			for (String m : this.medias.split(",")) {
				photoNames.add(m);
			}
		}
		return photoNames;
	}

	@Override
	public SQLiteQueryParameters getSqLiteQueryParameters() {
		return new SQLiteQueryParameters();
	}

	@Override
	public String getCreateTableSQL() {
		return "(id integer primary key,taskId,reportContent,medias,state,reportType)"; // fkId
																						// 是此事件上报到服务端的，返回的
																						// 事件id
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String showData() {

		StringBuilder builder = new StringBuilder();

		if (reportType.equals("arrive")) {
			ArrayList<LinkedHashMap> items = new ArrayList<LinkedHashMap>();

			items = new Gson().fromJson(reportContent, new TypeToken<ArrayList<LinkedHashMap>>() {
			}.getType());

			for (LinkedHashMap item : items) {
				if (item.get("Type").equals("0") && item.get("Name").equals("flowid")) {
					builder.append("flowId : ").append(String.valueOf(item.get("Value"))).append("\n");
				} else if (item.get("Type").equals("0") && item.get("Name").equals("index")) {
					builder.append("index : ").append(String.valueOf(item.get("Value"))).append("\n");
				}
			}

		} else if (reportType.equals("feedback")) {

			ArrayList<LinkedHashMap> items = new ArrayList<LinkedHashMap>();

			items = new Gson().fromJson(reportContent, new TypeToken<ArrayList<LinkedHashMap>>() {
			}.getType());

			for (LinkedHashMap item : items) {
				if (item.get("Type").equals("0") && item.get("Name").equals("flowid")) {
					builder.append("flowid : ").append(String.valueOf(item.get("Value"))).append("\n");
				} else if (item.get("Type").equals("0") && item.get("Name").equals("equiptype")) {
					builder.append("equiptype : ").append(String.valueOf(item.get("Value"))).append("\n");
				} else if (item.get("Type").equals("0") && item.get("Name").equals("equipentity")) {
					builder.append("equipentity : ").append(String.valueOf(item.get("Value"))).append("\n");
				}
			}

			if (medias != null) {
				builder.append("图片 : ").append(medias.toString());
			}

		} else if (reportType.equals("flow")) {

			String paras = reportContent.split("\\?")[1];

			String[] keyValues = paras.split("&");

			for (String keyValue : keyValues) {
				String[] kv = keyValue.split("=");
				if (kv.length == 2) {
					builder.append(keyValue.split("=")[0]).append(" : ").append(Uri.decode(keyValue.split("=")[1])).append("\n");
				} else {
					builder.append(keyValue.split("=")[0]).append(" : ").append("").append("\n");
				}
			}

			if (medias != null) {
				builder.append("图片 : ").append(medias.toString());
			}
		}

		return builder.toString();
	}
}
