package com.repair.zhoushan.module.eventmanage.eventbox;

import android.content.Intent;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 领单箱
 */
public class EventReceiveNavigationMenu extends BaseNavigationMenu {

    public EventReceiveNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {

        Intent intent = new Intent(navigationActivity, EventListActivity.class);

        String eventNames = item.Function.getModuleParamValue("事件名称");
        intent.putExtra("DefaultEventNames", eventNames);

        intent.putExtra("Alias", item.Function.Alias);
        intent.putExtra("MODE", EventListActivity.Mode.RECEIVE);
        intent.putExtra("Title", "领单");
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return new int[]{R.drawable.main_menu_maintenance_handover, R.drawable.ic_menu_dispatch_32dp, R.drawable.home_circle_case_doing};
    }
}
