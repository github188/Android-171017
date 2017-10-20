package com.repair.eventreport;

import android.content.Intent;

import com.mapgis.mmt.constant.ActivityAlias;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 巡线上报
 */
public class EventReportNavigationMenu extends BaseNavigationMenu {

    public EventReportNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Class<?> patrolReportActivity = ActivityClassRegistry.getInstance().getActivityClass(ActivityAlias.PATROL_REPORT_ACTIVITY);
        Intent intent = new Intent(navigationActivity, patrolReportActivity);
        String moduleParam = item.Function.ModuleParam;
        intent.putExtra("AllowMultiple", "多选".equals(moduleParam));
        navigationActivity.startActivityForResult(intent, 0);
    }
}
