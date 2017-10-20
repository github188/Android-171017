package com.maintainproduct.module.maintenance.history;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 已办工单
 */
public class MaintenanceNavigationHistoryMenu extends BaseNavigationMenu {

    public MaintenanceNavigationHistoryMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, MaintenanceHistoryListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        navigationActivity.startActivityForResult(intent, 3);
    }

    @Override
    public int[] getIcons() {
       return getIcons("已办");
    }
}
