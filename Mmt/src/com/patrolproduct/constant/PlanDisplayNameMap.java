package com.patrolproduct.constant;

import java.util.HashMap;
import java.util.Map;

public class PlanDisplayNameMap {
	protected static Map<String, String> dataMap;

	public static Map<String, String> getDataMap() {
		return dataMap;
	}

	static {
		dataMap = new HashMap<String, String>();

		dataMap.put("planName", "任务名称");
		dataMap.put("flowid", "流程编号");
		dataMap.put("title", "计划名称");
		dataMap.put("PLAN_MAKER", "计划制订人");
		dataMap.put("AREANAME", "区域名称");
		dataMap.put("PLANTYPE", "计划类型");
		dataMap.put("TIMELEN", "时长");
		dataMap.put("PLANNAME", "计划名称");
		dataMap.put("TIMEUNIT", "时间单位");
		dataMap.put("EquipAREA", "设备区域");
		dataMap.put("ISFEEDBACK", "是否反馈");
		dataMap.put("PLANCYCLE", "计划周期");
		dataMap.put("EQUIP_TYPE", "设备类型");
		dataMap.put("EQUIP_ENTITYIDS", "设备实体编号");
		dataMap.put("EQUIP_ENTITIES", "设备实体");
		dataMap.put("PLAN_STARTTIME", "开始时间");
		dataMap.put("PLAN_ENDTIME", "结束时间");
		dataMap.put("PLAN_CREATETIME", "计划制定时间");
		dataMap.put("ISOPEN", "是否打开");
		dataMap.put("CASENO", "关联案件编号");
	}
}
