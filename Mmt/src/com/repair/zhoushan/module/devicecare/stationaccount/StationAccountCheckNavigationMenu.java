package com.repair.zhoushan.module.devicecare.stationaccount;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 场站设备检定
 */
public class StationAccountCheckNavigationMenu extends BaseNavigationMenu {

    public StationAccountCheckNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, StationAccountListActivity.class);
        intent.putExtra("Title", item.Function.Alias);
        intent.putExtra("BizName", "场站设备检定");
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return getIcons("在办");
    }
}
