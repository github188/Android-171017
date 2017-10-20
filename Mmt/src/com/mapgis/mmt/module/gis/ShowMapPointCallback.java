package com.mapgis.mmt.module.gis;

import android.content.Context;
import android.os.Message;

import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.pointoper.ShowPointMapMenu;

public class ShowMapPointCallback extends BaseMapCallback {
    protected Context context;
    protected String loc;
    protected String title;
    protected String text;
    protected int pos;

    public ShowMapPointCallback() {
    }

    public ShowMapPointCallback(Context context, String loc, String title, String text, int pos) {
        this.context = context;
        this.loc = loc;
        this.title = title;
        this.text = text;
        this.pos = pos;
    }

    @Override
    public boolean handleMessage(Message msg) {
        BaseMapMenu menu = new ShowPointMapMenu(mapGISFrame, context, loc, title, text, pos);

        mapGISFrame.getFragment().menu = menu;

        return menu.onOptionsItemSelected();
    }
}
