package com.patrol.module.patrolanalyze;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.mapgis.mmt.R;

/**
 * 巡检分析
 */
public class PatrolAnalyzeNavigationMenu extends BaseNavigationMenu {

    public PatrolAnalyzeNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, PatrolAnalyzeActivity.class);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return new int[]{R.drawable.main_menu_task_control, R.drawable.home_analyze, R.drawable.home_circle_task};
    }
}
