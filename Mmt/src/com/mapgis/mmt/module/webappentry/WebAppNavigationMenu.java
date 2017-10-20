package com.mapgis.mmt.module.webappentry;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * Created by cmios on 2017/5/16.
 */

public class WebAppNavigationMenu extends BaseNavigationMenu {
    public WebAppNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, WebAppActivity.class);
        intent.putExtra("url", item.Function.ModuleParam);
        navigationActivity.startActivity(intent);
    }
}
