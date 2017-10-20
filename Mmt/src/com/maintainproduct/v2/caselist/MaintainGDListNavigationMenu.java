package com.maintainproduct.v2.caselist;

import android.content.Intent;
import android.text.TextUtils;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * V2 版  维修工单
 */
public class MaintainGDListNavigationMenu extends BaseNavigationMenu {

    public MaintainGDListNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {

        Intent intent = new Intent(navigationActivity, MaintainGDListActivity.class);
        navigationActivity.startActivityForResult(intent, 0);
    }

    @Override
    public int[] getIcons() {
        return getIcons("总览");
    }
}
