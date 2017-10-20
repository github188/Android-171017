package com.repair.reporthistory;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 巡线历史
 */
public class PatrolReportHistoryNavigationMenu extends BaseNavigationMenu {

    public PatrolReportHistoryNavigationMenu(
            NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity,
                PatrolReportHistoryActivity.class);
        intent.putExtra("title", item.Function.Alias);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
       return getIcons("已办");
    }
}
