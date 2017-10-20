package com.repair.zhoushan.module.projectmanage.waterplantpatrolhistory;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 三巡历史查看
 */
public class WaterPlantPatrolHistoryNavigationMenu extends BaseNavigationMenu {

    public WaterPlantPatrolHistoryNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, WaterPlantPatrolHistoryListActivity.class);
        intent.putExtra("Title", "三巡历史");
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return getIcons("已办");
    }
}
