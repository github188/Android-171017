package com.maintainproduct.module.basereport;

import android.content.Intent;

import com.maintainproduct.constant.RepairActivityAlias;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 自定义上报
 */
public class BaseReportNavigationMenu extends BaseNavigationMenu {

    public BaseReportNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, ActivityClassRegistry.getInstance().getActivityClass(
                RepairActivityAlias.CUSTOM_FORM_REPORT));

        intent.putExtra("Alias", item.Function.Alias);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        navigationActivity.startActivity(intent);
    }
}