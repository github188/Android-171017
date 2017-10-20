package com.repair.beihai.poj.hbpoj.module.userwaterecheck;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * Created by liuyunfan on 2016/8/8.
 */
public class UserWatermeterCheckNavigationnMenu extends BaseNavigationMenu {

    public UserWatermeterCheckNavigationnMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, UserWatermeterCheckPojListActivity.class);
        intent.putExtra("Title", item.Function.Alias);
        navigationActivity.startActivity(intent);
    }
}
