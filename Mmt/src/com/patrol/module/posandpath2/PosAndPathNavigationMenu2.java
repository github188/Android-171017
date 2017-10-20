package com.patrol.module.posandpath2;

import android.os.Message;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 位置与轨迹
 */
public class PosAndPathNavigationMenu2 extends BaseNavigationMenu {
    public PosAndPathNavigationMenu2(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
            @Override
            public boolean handleMessage(Message msg) {
                BaseMapMenu menu = new PosAndPathMapMenu2(mapGISFrame);

                mapGISFrame.getFragment().menu = menu;

                return menu.onOptionsItemSelected();
            }
        });

        navigationActivity.finish();
    }

    @Override
    public int[] getIcons() {
        return new int[]{R.drawable.main_menu_task_control, R.drawable.home_monitor, R.drawable.home_circle_task};
    }
}
