package com.patrolproduct.module.myplan;

import android.content.Intent;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.mapgis.mmt.R;
import com.patrolproduct.module.myplan.map.PlanFragmentCallback;

/**
 * 计划任务
 */
public class MyPlanNavigationMenu extends BaseNavigationMenu {

    public MyPlanNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        MyApplication.getInstance().sendToBaseMapHandle(new PlanFragmentCallback(item));
        Intent intent = new Intent(navigationActivity, MapGISFrame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        navigationActivity.startActivity(intent);

        AppManager.finishActivity(navigationActivity);
    }

    @Override
    public int[] getIcons() {
        return new int[]{R.drawable.main_menu_maintenance_list, R.drawable.home_mans, R.drawable.home_circle_relation};
    }
}
