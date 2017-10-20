package com.repair.gisdatagather.product;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.repair.gisdatagather.product.projectlist.ProjectListActivity;

/**
 * GIS数据采集
 */
public class GisProjectNavigationMenu extends BaseNavigationMenu {
    public GisProjectNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, ProjectListActivity.class);
        navigationActivity.startActivity(intent);
    }
}
