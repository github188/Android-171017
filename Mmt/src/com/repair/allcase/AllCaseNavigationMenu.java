package com.repair.allcase;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 工单浏览
 */
public class AllCaseNavigationMenu extends BaseNavigationMenu {

    public AllCaseNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, AllCaseActivity.class);

        intent.putExtra("alias", item.Function.Alias);

        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
       return getIcons("总览");
    }
}
