package com.repair.zhoushan.module.devicecare.stationaccount;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 车用养护
 */
public class VehicleDeviceNavigationMenu extends BaseNavigationMenu {

    public VehicleDeviceNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, StationAccountListActivity.class);
        intent.putExtra("Title", item.Function.Alias);
        intent.putExtra("BizName", "车用设备");
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return getIcons("养护");
    }
}
