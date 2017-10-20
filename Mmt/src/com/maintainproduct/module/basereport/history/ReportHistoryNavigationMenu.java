package com.maintainproduct.module.basereport.history;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 自定义上报历史
 */
public class ReportHistoryNavigationMenu extends BaseNavigationMenu {

    public ReportHistoryNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, ReportHistoryActivity.class);
        intent.putExtra("Alias", item.Function.Alias);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
       return getIcons("已办");
    }
}
