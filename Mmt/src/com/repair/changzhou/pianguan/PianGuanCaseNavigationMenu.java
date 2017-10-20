package com.repair.changzhou.pianguan;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 片管工单查询
 */
public class PianGuanCaseNavigationMenu extends BaseNavigationMenu {

    public PianGuanCaseNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, PianGuanCaseListActivity.class);
        intent.putExtra("Alias", item.Function.Alias);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return getIcons("在办");
    }
}
