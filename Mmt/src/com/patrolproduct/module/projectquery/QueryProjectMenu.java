package com.patrolproduct.module.projectquery;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * Created by KANG on 2016/9/5.
 *      工程查询菜单
 */
public class QueryProjectMenu  extends BaseNavigationMenu{
    public QueryProjectMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity,QueryProjectActivity.class);
        navigationActivity.startActivity(intent);
    }
}
