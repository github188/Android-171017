package com.repair.zhoushan.module.flownodecommonhand;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * Created by liuyunfan on 2016/8/8.
 */
public class FlowNodeReportNavigationnMenu extends BaseNavigationMenu {

    public FlowNodeReportNavigationnMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, PojListActivity.class);
        intent.putExtra("Title", item.Function.Alias);
        intent.putExtra("params",item.Function.ModuleParam);
        navigationActivity.startActivity(intent);
    }
}
