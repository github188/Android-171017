package com.mapgis.mmt.module.gis;

import android.content.Context;
import android.os.Message;

import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.pointoper.ShowDeviceMapMenu;

public class ShowDeviceCallback extends BaseMapCallback {
    Context context;
    String loc;
    String title;
    String text;
    int pos;

    public ShowDeviceCallback(Context context, String loc, String title, String text, int pos) {
        this.context = context;
        this.loc = loc;
        this.title = title;
        this.text = text;
        this.pos = pos;
    }

    @Override
    public boolean handleMessage(Message msg) {
        BaseMapMenu menu = new ShowDeviceMapMenu(mapGISFrame, context, loc, title, text, pos);

        mapGISFrame.getFragment().menu = menu;

        return menu.onOptionsItemSelected();
    }
}
