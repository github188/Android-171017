package com.mapgis.mmt.module.gis.toolbar.areaopr;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;

/**
 * Created by liuyunfan on 2016/3/22.
 */
public class ShowAreaMapCallback extends BaseMapCallback {
    Context context;
    String value;

    public ShowAreaMapCallback(Context context, String value) {
        this.context = context;
        this.value = value;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (TextUtils.isEmpty(value) || !value.contains("rings")) {
            MyApplication.getInstance().showMessageWithHandle("无效范围");
            return false;
        }
        Intent intent = new Intent(context, MapGISFrame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
        BaseMapMenu menu = new ShowAreaMapMenu(mapGISFrame, context, value);

        mapGISFrame.getFragment().menu = menu;

        return menu.onOptionsItemSelected();
    }
}
