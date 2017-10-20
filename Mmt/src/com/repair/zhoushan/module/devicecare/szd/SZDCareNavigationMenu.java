package com.repair.zhoushan.module.devicecare.szd;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 水质点养护
 */
public class SZDCareNavigationMenu extends BaseNavigationMenu {
    public SZDCareNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        navigationActivity.startActivity(new Intent(navigationActivity, SZDFeedbackActivity.class));
    }
}