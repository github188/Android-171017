package com.maintainproduct.module.maintenance;

import android.content.Intent;

import com.maintainproduct.module.maintenance.list.MaintenanceListActivity;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 维修养护 在办工单
 */
public class MaintenanceNavigationMenu extends BaseNavigationMenu {

    public MaintenanceNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, MaintenanceListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        navigationActivity.startActivityForResult(intent, 2);
    }

    @Override
    public int[] getIcons() {
        return getIcons("总览");
    }
}
