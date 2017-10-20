package com.mapgis.mmt.module.systemsetting;

import android.content.Intent;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 系统设置
 */
public class SystemSettingNavigationMenu extends BaseNavigationMenu {

    public SystemSettingNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        try {
            Intent intent = new Intent(navigationActivity, SystemSettingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            navigationActivity.startActivityForResult(intent, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int[] getIcons() {
        return new int[]{R.drawable.main_menu_sysytem_setting, R.drawable.home_setting, R.drawable.home_circle_setting};
    }
}
