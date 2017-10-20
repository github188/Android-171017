package com.repair.zhoushan.module.casemanage.mycase;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 标准工单
 */
public class MyCaseZSNavigationMenu extends BaseNavigationMenu {

    public MyCaseZSNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, MyCaseListActivity.class);
        intent.putExtra("Alias", item.Function.Alias);

        String flowNames = item.Function.getModuleParamValue("流程名称");
        intent.putExtra("DefaultFlowNames", flowNames);

        if (item.Function.containsModuleParam("事件编号")) {
            intent.putExtra("ShowEventCode", true);
        }
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return getIcons("在办");
    }
}
