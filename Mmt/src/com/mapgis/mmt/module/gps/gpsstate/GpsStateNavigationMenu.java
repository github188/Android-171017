package com.mapgis.mmt.module.gps.gpsstate;

import android.content.Intent;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gps.gpsstate.GpsStateActivity;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * GPS状态
 */
public class GpsStateNavigationMenu extends BaseNavigationMenu {

    public GpsStateNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, GpsStateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return new int[]{R.drawable.main_menu_gps, R.drawable.home_gps, R.drawable.home_circle_gps_state};
    }
}
