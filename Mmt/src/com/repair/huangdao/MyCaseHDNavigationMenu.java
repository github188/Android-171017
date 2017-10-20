package com.repair.huangdao;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 黄岛在办
 */
public class MyCaseHDNavigationMenu extends BaseNavigationMenu {

    public MyCaseHDNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, MyCaseHDActivity.class);

        intent.putExtra("alias", item.Function.Alias);

        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return getIcons("在办");
    }
}
