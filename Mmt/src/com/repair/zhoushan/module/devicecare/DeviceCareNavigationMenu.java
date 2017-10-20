package com.repair.zhoushan.module.devicecare;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 设备养护
 */
public class DeviceCareNavigationMenu extends BaseNavigationMenu {

    public DeviceCareNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, DeviceCareListActivity.class);
        // BizName: 调压器维护、阀门维护
        intent.putExtra("BizName", item.Function.Alias);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return getIcons("养护");
    }
}
