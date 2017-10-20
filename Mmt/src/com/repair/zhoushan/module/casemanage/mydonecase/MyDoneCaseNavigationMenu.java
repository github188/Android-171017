package com.repair.zhoushan.module.casemanage.mydonecase;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 已办理工单
 */
public class MyDoneCaseNavigationMenu extends BaseNavigationMenu {

    public MyDoneCaseNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, MyDoneCaseListActivity.class);
        String flowNames = item.Function.getModuleParamValue("流程名称");
        intent.putExtra("DefaultFlowNames", flowNames);
        intent.putExtra("Alias", item.Function.Alias);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return getIcons("已办");
    }
}
