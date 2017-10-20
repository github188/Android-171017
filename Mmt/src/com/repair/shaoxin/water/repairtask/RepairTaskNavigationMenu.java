package com.repair.shaoxin.water.repairtask;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 抢修工单
 */
public class RepairTaskNavigationMenu extends BaseNavigationMenu {

    public RepairTaskNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {

        Intent intent = new Intent(navigationActivity, RepairTaskListActivity.class);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return getIcons("在办");
    }

}
