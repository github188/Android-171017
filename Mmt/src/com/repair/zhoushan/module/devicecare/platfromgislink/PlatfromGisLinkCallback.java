package com.repair.zhoushan.module.devicecare.platfromgislink;

import android.content.Context;
import android.os.Message;

import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;

/**
 * Created by liuyunfan on 2016/3/23.
 */
public class PlatfromGisLinkCallback extends BaseMapCallback {
    Context context;
    String key;
    String layerName;
    String tableName;
    String taskKey;

    public PlatfromGisLinkCallback(Context context, String key, String layerName, String tableName, String taskKey) {
        this.context = context;
        this.key = key;
        this.layerName = layerName;
        this.tableName = tableName;
        this.taskKey = taskKey;
    }

    @Override
    public boolean handleMessage(Message msg) {
        BaseMapMenu menu = new PlatfromGisLinkMapMenu(mapGISFrame, context, key, layerName, tableName, taskKey);
        mapGISFrame.getFragment().menu = menu;
        return menu.onOptionsItemSelected();
    }
}
