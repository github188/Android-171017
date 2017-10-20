package com.patrolproduct;

import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.constant.NavigationMenuRegistry;
import com.mapgis.mmt.db.DatabaseHelper;
import com.patrolproduct.constant.PatrolActivityAlias;
import com.patrolproduct.entity.MyPlanBean;
import com.patrolproduct.entity.PatrolDevice;
import com.patrolproduct.module.myplan.MyPlanNavigationMenu;
import com.patrolproduct.module.myplan.list.PlanDetailActivity;

public class PatrolFunctionRegistry {

	public static void regist() {
		// 注册 计划任务 主页模块
		NavigationMenuRegistry.getInstance().regist("计划任务", MyPlanNavigationMenu.class.getName());

		// 增加<我的计划>表,用于 往日计划
		DatabaseHelper.getInstance().createTable(MyPlanBean.class);

		// 增加<巡检设备>表
		DatabaseHelper.getInstance().createTable(PatrolDevice.class);

		ActivityClassRegistry.getInstance().regist(PatrolActivityAlias.MY_PLAN_DETAIL, PlanDetailActivity.class);
	}
}
