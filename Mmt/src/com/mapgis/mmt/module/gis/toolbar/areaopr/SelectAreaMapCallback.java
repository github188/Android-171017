package com.mapgis.mmt.module.gis.toolbar.areaopr;

import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;

/**
 * Created by liuyunfan on 2016/3/21.
 */
public class SelectAreaMapCallback extends BaseMapCallback {
    Context context;
    String value;
    public SelectAreaMapCallback(Context context,String value) {
        this.context = context;
        this.value=value;
        Intent intent = new Intent(context, MapGISFrame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }

    @Override
    public boolean handleMessage(Message msg) {
        BaseMapMenu menu = new SelectAreaMapMenu(mapGISFrame, context,value);

        mapGISFrame.getFragment().menu = menu;

        return menu.onOptionsItemSelected();
    }
}
