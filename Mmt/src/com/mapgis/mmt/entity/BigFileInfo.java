package com.mapgis.mmt.entity;

import android.content.ContentValues;
import android.database.Cursor;

import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

public class BigFileInfo implements ISQLiteOper {

	/** 主键 唯一 */
	int id;
	/** 存储在本地 时 与 本地 事件表的 关联 */
	String taskId;
	/** 标识 此大附件 所属事件的ID */
	String eventId;
	String fileName;
	String fileType;
	String state;

	public BigFileInfo() {
		super();
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public BigFileInfo(String taskId, String eventId, String fileName, String fileType, String state) {
		super();
		this.taskId = taskId;
		this.eventId = eventId;
		this.fileName = fileName;
		this.fileType = fileType;
		this.state = state;
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

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String getTableName() {
		return "BigFileInfo";
	}

	@Override
	public String getCreateTableSQL() {
		return "(id integer primary key, taskId ,eventId,fileName,fileType ,state )";
	}

	@Override
	public SQLiteQueryParameters getSqLiteQueryParameters() {
		return new SQLiteQueryParameters();
	}

	@Override
	public ContentValues generateContentValues() {
		ContentValues cv = new ContentValues();
		cv.put("taskId", this.taskId);
		cv.put("eventId", this.eventId);
		cv.put("fileName", this.fileName);
		cv.put("fileType", this.fileType);
		cv.put("state", this.state);
		return cv;
	}

	@Override
	public void buildFromCursor(Cursor cursor) {
		this.id = cursor.getInt(0);
		this.taskId = cursor.getString(1);
		this.eventId = cursor.getString(2);
		this.fileName = cursor.getString(3);
		this.fileType = cursor.getString(4);
		this.state = cursor.getString(5);
	}

}
