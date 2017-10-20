package com.mapgis.mmt.module.gis.toolbar.areaopr;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;

public class ShowAreaAndPointMapCallback extends BaseMapCallback {

    private Context context;
    private String areaStr;
    private String coorStr;

    private String title;
    private String text;
    private int pos;

    public ShowAreaAndPointMapCallback(Context context, String area, String coordinate, String title, String text, int pos) {

        this.context = context;
        this.areaStr = area;
        this.coorStr = coordinate;

        this.title = title;
        this.text = text;
        this.pos = pos;
    }

    @Override
    public boolean handleMessage(Message msg) {

        boolean areaValid = true, coorValid = true;

        if (TextUtils.isEmpty(areaStr) || !areaStr.contains("rings")) {
            areaValid = false;

        }
        if (TextUtils.isEmpty(coorStr) || !coorStr.contains(",")) {
            coorValid = false;
        }

        if (!areaValid && !coorValid) {
            MyApplication.getInstance().showMessageWithHandle("无效的坐标区域");
            return false;
        }

        BaseMapMenu menu = new ShowAreaAndPointMapMenu(mapGISFrame, context, areaStr, coorStr, 0, title, text, pos);
        mapGISFrame.getFragment().menu = menu;
        return menu.onOptionsItemSelected();
    }
}
