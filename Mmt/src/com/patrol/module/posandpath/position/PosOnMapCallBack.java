package com.patrol.module.posandpath.position;

import android.content.Context;
import android.os.Message;

import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.zondy.mapgis.geometry.Dot;


/**
 * User: zhoukang
 * Date: 2016-03-22
 * Time: 11:43
 * <p/>
 * 在位置与轨迹界面点击定位功能时回调
 */
public class PosOnMapCallBack extends BaseMapCallback {
    private Context context;
    private String name;   // 用户名
    private Dot dot;

    public PosOnMapCallBack(Context context,String name, Dot dot) {
        this.context = context;
        this.name = name;
        this.dot = dot;
    }

    @Override
    public boolean handleMessage(Message message) {
        BaseMapMenu menu = new PosMapMenu(mapGISFrame, context,name, dot);

        mapGISFrame.getFragment().menu = menu;

        return menu.onOptionsItemSelected();
    }

}
