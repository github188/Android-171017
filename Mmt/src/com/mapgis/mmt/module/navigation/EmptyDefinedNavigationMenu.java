package com.mapgis.mmt.module.navigation;

/**
 * 空模板导航模块
 */
public class EmptyDefinedNavigationMenu extends BaseNavigationMenu {

    public EmptyDefinedNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
    }
}
