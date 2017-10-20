package com.repair.beihai.poj.hbpoj.module.userwatermeter;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * Created by liuyunfan on 2016/8/8.
 */
public class UserWatermeterNavigationnMenu extends BaseNavigationMenu {

    public UserWatermeterNavigationnMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, UserWatermeterPojListActivity.class);
        intent.putExtra("Title", item.Function.Alias);
        navigationActivity.startActivity(intent);
    }
}
