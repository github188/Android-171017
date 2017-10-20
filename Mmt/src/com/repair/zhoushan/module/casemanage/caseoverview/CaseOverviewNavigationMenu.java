package com.repair.zhoushan.module.casemanage.caseoverview;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 工单总览
 */
public class CaseOverviewNavigationMenu extends BaseNavigationMenu {

    public CaseOverviewNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, CaseOverviewListActivity.class);
        String flowNames = item.Function.getModuleParamValue("流程名称");
        intent.putExtra("FlowNames", flowNames);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return getIcons("总览");
    }

}
