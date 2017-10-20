package com.patrol.module.posandpath;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.mapgis.mmt.R;

/**
 * 位置与轨迹
 */
public class PosAndPathNavigationMenu extends BaseNavigationMenu {
    public PosAndPathNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, PosAndPathActivity.class);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return new int[]{R.drawable.main_menu_task_control, R.drawable.home_monitor, R.drawable.home_circle_task};
    }
}
