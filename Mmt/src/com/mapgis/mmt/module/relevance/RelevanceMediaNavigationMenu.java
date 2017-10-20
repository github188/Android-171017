package com.mapgis.mmt.module.relevance;

import android.content.Intent;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 设备拍照
 */
public class RelevanceMediaNavigationMenu extends BaseNavigationMenu {

    public RelevanceMediaNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {

        Intent intent = new Intent(navigationActivity, MapGISFrame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        navigationActivity.startActivity(intent);

        RelevanceCallback relevanceCallback = new RelevanceCallback();
        MyApplication.getInstance().sendToBaseMapHandle(relevanceCallback);
    }
}
