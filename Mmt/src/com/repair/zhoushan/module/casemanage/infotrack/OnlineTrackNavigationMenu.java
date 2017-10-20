package com.repair.zhoushan.module.casemanage.infotrack;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.mapgis.mmt.R;

/**
 * 热线追踪
 */
public class OnlineTrackNavigationMenu extends BaseNavigationMenu {

    public OnlineTrackNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, OnlineTrackListActivity.class);
        intent.putExtra("Title", item.Function.Alias);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return new int[]{R.drawable.main_menu_task_control, R.drawable.home_org, R.drawable.home_circle_relation};
    }
}
