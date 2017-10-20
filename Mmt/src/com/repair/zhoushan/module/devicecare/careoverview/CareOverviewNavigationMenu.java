package com.repair.zhoushan.module.devicecare.careoverview;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

public class CareOverviewNavigationMenu extends BaseNavigationMenu {

    public CareOverviewNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {

        Intent intent = new Intent(navigationActivity, CareOverviewListActivity.class);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return super.getIcons("总览");
    }
}
