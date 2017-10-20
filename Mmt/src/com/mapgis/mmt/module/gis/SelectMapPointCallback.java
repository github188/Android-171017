package com.mapgis.mmt.module.gis;

import android.content.Context;
import android.os.Message;

import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.pointoper.SelectPointMapMenu;

public class SelectMapPointCallback extends BaseMapCallback {
    Context context;
    String loc;

    public SelectMapPointCallback(Context context, String loc) {
        this.context = context;
        this.loc = loc;
    }

    @Override
    public boolean handleMessage(Message msg) {
        BaseMapMenu menu = new SelectPointMapMenu(mapGISFrame, context, loc);

        mapGISFrame.getFragment().menu = menu;

        return menu.onOptionsItemSelected();
    }
}
