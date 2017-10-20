package com.mapgis.mmt.module.taskcontrol;

import android.content.ContentValues;
import android.database.Cursor;

import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

import java.util.List;

public class TaskControlEntity implements ISQLiteOper {

	private ITaskControlOper op = null;
	
	public int id;

	// 对所有数据规定 只有上传成功时 状态值为1
	public int status = -1;

	// 上报时间
	public String reportTime;

	// 后台上报服务返回结果
	public String serverResult = "--";

	// 任务类型
	public String type;

	// 外键
	public String taskId;

	public int userId;

	// 关联类名
	public String className;

	public TaskControlEntity() {
	}
	
	public TaskControlEntity(int status, String reportTime, String type, String taskId, int userId, String className) {
		this.status = status;
		this.reportTime = reportTime;
		this.type = type;
		this.taskId = taskId;
		this.userId = userId;
		this.className = className;
	}

	/** 插入数据 */
	public long insertData() {
		return DatabaseHelper.getInstance().insert(this);
	}

	/** 更新数据 */
	public long updateData() {
		return DatabaseHelper.getInstance().update(TaskControlEntity.class, generateContentValues(), "id=" + id);
	}

	/** 删除数据 */
	public long deleteData() {
		return DatabaseHelper.getInstance().delete(TaskControlEntity.class, "id=" + id);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("事件来源:").append(type).append("\n");
		builder.append("上报时间:").append(reportTime).append("\n");
		builder.append("服务结果:").append(serverResult).append("\n");
		builder.append("状态标识:").append(status);
		return builder.toString();
	}

	@Override
	public String getTableName() {
		return "TaskControlEntity";
	}

	@Override
	public String getCreateTableSQL() {
		return "(id integer primary key,status,reportTime, serverResult,type,taskId,userId,className)";
	}

	@Override
	public SQLiteQueryParameters getSqLiteQueryParameters() {
		return null;
	}

	@Override
	public ContentValues generateContentValues() {
		ContentValues cv = new ContentValues();
		cv.put("status", status);
		cv.put("reportTime", reportTime);
		cv.put("serverResult", serverResult);
		cv.put("type", type);
		cv.put("taskId", taskId);
		cv.put("userId", userId);
		cv.put("className", className);
		return cv;
	}

	@Override
	public void buildFromCursor(Cursor cursor) {
		this.id = cursor.getInt(0);
		this.status = cursor.getInt(1);
		this.reportTime = cursor.getString(2);
		this.serverResult = cursor.getString(3);
		this.type = cursor.getString(4);
		this.taskId = cursor.getString(5);
		this.userId = cursor.getInt(6);
		this.className = cursor.getString(7);
	}
	
	/**  获取 此条 日志  关联 的 上报事件 的 显示信息  */
	public String getShowData(){
		try {
			@SuppressWarnings("unchecked")
			Class<? extends ISQLiteOper> obj = (Class<? extends ISQLiteOper>) Class.forName( className );
			List<? extends ISQLiteOper> opers = DatabaseHelper.getInstance().query(obj, new SQLiteQueryParameters("id=" + id));
			if (opers != null && opers.size() > 0) {
				op = (ITaskControlOper) opers.get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if( op != null )
			return op.showData();
		else
			return null;
	}
	
}
