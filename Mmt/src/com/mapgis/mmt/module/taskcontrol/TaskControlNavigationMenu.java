package com.mapgis.mmt.module.taskcontrol;

import android.content.Intent;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 任务监控
 */
public class TaskControlNavigationMenu extends BaseNavigationMenu {

    public TaskControlNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, TaskControlActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        navigationActivity.startActivityForResult(intent, 0);
    }

    @Override
    public int[] getIcons() {
        return new int[]{R.drawable.main_menu_task_control, R.drawable.home_monitor, R.drawable.home_circle_task};
    }
}
