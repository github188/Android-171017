package com.repair.quanzhou.module;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 热线工单
 */
public class HotLineGDNavigationMenu extends BaseNavigationMenu {

    public HotLineGDNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, GDQueryActivity.class);
        intent.putExtra("Alias", item.Function.Alias);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
       return getIcons("在办");
    }
}
