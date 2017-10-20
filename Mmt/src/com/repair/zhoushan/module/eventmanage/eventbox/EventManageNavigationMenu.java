package com.repair.zhoushan.module.eventmanage.eventbox;

import android.content.Intent;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 调度箱
 */
public class EventManageNavigationMenu extends BaseNavigationMenu {

    public EventManageNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {

        Intent intent = new Intent(navigationActivity, EventListActivity.class);

        String eventNames = item.Function.getModuleParamValue("事件名称");
        intent.putExtra("DefaultEventNames", eventNames);

        intent.putExtra("Alias", item.Function.Alias);
        intent.putExtra("MODE", EventListActivity.Mode.DISPATCH);
        intent.putExtra("Title", "分派");
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return new int[]{R.drawable.main_menu_maintenance_handover, R.drawable.ic_menu_dispatch_32dp, R.drawable.home_circle_case_doing};
    }
}
