package com.patrolproduct.module.myplan.map;

import android.os.Message;

import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.patrolproduct.module.myplan.MyPlanMapMenu;

public class PlanFragmentCallback extends BaseMapCallback {
    private final NavigationItem item;

    public PlanFragmentCallback(NavigationItem item) {
        this.item = item;
    }

    @Override
    public boolean handleMessage(Message msg) {
        BaseMapMenu menu = new MyPlanMapMenu(mapGISFrame, item);

        mapGISFrame.getFragment().menu = menu;

        return menu.onOptionsItemSelected();
    }
}
