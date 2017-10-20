package com.repair.live.player;

import android.content.Intent;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * Created by Comclay on 2016/11/21.
 * 现场直播菜单
 */

public class LivePlayerMenu extends BaseNavigationMenu {
    public LivePlayerMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, LiveListActivity.class);
        navigationActivity.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(navigationActivity);
    }
}
