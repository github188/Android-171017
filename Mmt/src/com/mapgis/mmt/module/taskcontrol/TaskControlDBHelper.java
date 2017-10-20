package com.mapgis.mmt.module.taskcontrol;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.doinback.ReportInBackEntity;

import java.util.List;

/**
 * 用于 操作 后台日志 表
 * 
 * @author meikai
 */
public class TaskControlDBHelper {

	private static TaskControlDBHelper instance;

	/** 禁止 单例 类 实例化 对象 */
	private TaskControlDBHelper() {

	}

	public static TaskControlDBHelper getIntance() {
		if (instance == null)
			instance = new TaskControlDBHelper();
		return instance;
	}

	/** 查询监控记录 */
	public List<TaskControlEntity> queryControlData(int userId) {
		SQLiteQueryParameters parameters = new SQLiteQueryParameters();
		parameters.selection = "userId=" + userId;
		parameters.orderBy = "reportTime desc";
		return DatabaseHelper.getInstance().query(TaskControlEntity.class, parameters);
	}

	/** 创建一条监控记录 */
	public long createControlData(String taskId) {
		TaskControlEntity taskControlEntity = new TaskControlEntity(0, BaseClassUtil.getSystemTime(), "案件移交", taskId,
				MyApplication.getInstance().getUserId(), ReportInBackEntity.class.getName());
		return taskControlEntity.insertData();
	}

	/** 创建一条监控记录 */
	public long createControlData(String taskId, String type) {
		TaskControlEntity taskControlEntity = new TaskControlEntity(0, BaseClassUtil.getSystemTime(), type, taskId, MyApplication
				.getInstance().getUserId(), ReportInBackEntity.class.getName());
		return taskControlEntity.insertData();
	}

	/** 删除一条监控记录 */
	public void deleteControlData(String taskId) {
		DatabaseHelper.getInstance().delete(TaskControlEntity.class, "taskId='" + taskId + "'");
	}

	/** 删除 所有 监控记录 */
	public void deleteAllControlData() {
		DatabaseHelper.getInstance().delete(TaskControlEntity.class, "1=1");
	}

	/** 修改taskId为指定值的任务监控数据的服务返回结果 */
	public void updateControlData(String taskId, String reason, int statusCode) {
		List<TaskControlEntity> entities = DatabaseHelper.getInstance().query(TaskControlEntity.class, "taskId='" + taskId + "'");
		if (entities != null && entities.size() != 0) {
			TaskControlEntity entity = entities.get(0);
			entity.serverResult = reason;
			entity.status = statusCode;
			entity.updateData();
		}
	}
}
