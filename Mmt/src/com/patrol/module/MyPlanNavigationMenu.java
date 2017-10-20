package com.patrol.module;

import android.os.Message;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.mapgis.mmt.R;

/**
 * 巡线任务
 */
public class MyPlanNavigationMenu extends BaseNavigationMenu {

    public MyPlanNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
            @Override
            public boolean handleMessage(Message msg) {
                BaseMapMenu menu = new MyPlanMapMenu(mapGISFrame, item);

                mapGISFrame.getFragment().menu = menu;

                return menu.onOptionsItemSelected();
            }
        });

        navigationActivity.finish();
    }

    @Override
    public int[] getIcons() {
        return new int[]{R.drawable.main_menu_maintenance_list, R.drawable.home_mans, R.drawable.home_circle_relation};
    }
}
