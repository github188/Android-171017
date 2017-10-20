package com.patrolproduct.entity;

import android.content.ContentValues;
import android.database.Cursor;

import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.patrolproduct.module.myplan.entity.PatrolTask;

public class MyPlanBean implements ISQLiteOper {
	PatrolTask table;

	public PatrolTask getTable() {
		return table;
	}

	public void setTable(PatrolTask table) {
		this.table = table;
	}

	@Override
	public String getTableName() {
		return "MyPlan";
	}

	@Override
	public SQLiteQueryParameters getSqLiteQueryParameters() {
		return null;
	}

	@Override
	public ContentValues generateContentValues() {
		return null;
	}

	@Override
	public void buildFromCursor(Cursor cursor) {
		table = new PatrolTask();

		table.PlanInfo.PArea.AreaName = cursor.getString(0);
		// table.put("PLAN_MAKER", cursor.getString(1));
		table.PlanFlowID = cursor.getString(2);
		table.PlanInfo.PType = cursor.getString(3);
		// table.put("TIMELEN", cursor.getString(4));
		table.PlanInfo.PlanType = cursor.getString(5);
		table.PlanInfo.PArea.AreaRange = cursor.getString(6);
		table.PlanInfo.PlanName = cursor.getString(7);
		// table.put("TIMEUNIT", cursor.getString(8));
		table.PlanInfo.PEquip.EquipArea = cursor.getString(9);
		table.PlanInfo.PlanID = cursor.getString(10);
		// table.put("PLANCYCLE", cursor.getString(11));
		table.PlanInfo.IsFeedBack = cursor.getString(12);
		table.PlanInfo.PEquip.EquipPos = cursor.getString(13);
		table.PlanInfo.PEquip.EquipType = cursor.getString(14);
		table.PlanInfo.PEquip.EquipID = cursor.getString(15);
		table.PlanInfo.PEquip.EquipEntity = cursor.getString(16);
		table.StartTime = cursor.getString(17);
		table.PlanInfo.PPath.PathRange = cursor.getString(18);
		table.ArriveState = cursor.getString(19);
		table.EndTime = cursor.getString(20);
		// table.put("PLAN_CREATETIME", cursor.getString(21));
		table.PlanInfo.PPath.PathName = cursor.getString(22);
		table.TaskID = cursor.getString(23);
		// table.put("ISOPEN", cursor.getString(24));
		table.FeedBackState = cursor.getString(25);
		// table.put("CASENO", cursor.getString(26));
	}

	@Override
	public String getCreateTableSQL() {
		return "(areaName,planMaker,flowId,pId,timeLen,planType,areaRange,planName,timeUnit,equipArea,planId,planCycle,isFeedback"
				+ ",equipPos,equipType,equipEntityIds,equipEntities,planStartTime,pathRange,arriveState,planEndTime,planCreateTime,"
				+ "pathName,taskId,isOpen,feedbackState,caseNo )";
	}

}
