package com.repair.zhoushan.module.eventmanage.eventreporthistory;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 历史上报
 */
public class ERHNavigationMenu extends BaseNavigationMenu {

    public ERHNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        String eventNames = item.Function.getModuleParamValue("事件名称");

        Intent intent = new Intent(navigationActivity, ERHListActivity.class);
        intent.putExtra(ERHListFragment.PARAM_NAME_DEFAULT_EVENT_NAME, eventNames);
        intent.putExtra("Title", "上报历史");
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return getIcons("已办");
    }
}
