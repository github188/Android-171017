package com.repair.zhoushan.module.casemanage.mycase;


import android.content.Context;
import android.os.Message;
import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.areaopr.ShowAreaAndPointMapMenu;

public class ShowAreaAndPointMapCallback extends BaseMapCallback {

    private Context context;
    private int pos;

    private String xy; // Coordinate
    private String geoArea; // Area
    private int isArrive; // Arrived or not, default '-1'
    private int isFeedback; // Feedback or not, default '-1'
    private String title; // Map annotation title
    private String desc; // Map annotation desc

    public ShowAreaAndPointMapCallback(Context context, String xy, String geoArea, int isArrive,
                                       int isFeedback, String title, String desc, int pos) {
        this.context = context;
        this.xy = xy;
        this.geoArea = geoArea;
        this.isArrive = isArrive;
        this.isFeedback = isFeedback;
        this.title = title;
        this.desc = desc;
        this.pos = pos;
    }

    @Override
    public boolean handleMessage(Message msg) {

        boolean areaValid = true, coorValid = true;

        if (TextUtils.isEmpty(geoArea) || !geoArea.contains("rings")) {
            areaValid = false;

        }
        if (TextUtils.isEmpty(xy) || !xy.contains(",")) {
            coorValid = false;
        }

        if (!areaValid && !coorValid) {
            MyApplication.getInstance().showMessageWithHandle("无效的坐标区域");
            return false;
        }

        int state = 0;
        if (isArrive != -1 || isFeedback != -1) {
            if (isFeedback == 1) {
                state = 3;
            } else if (isArrive == 1) {
                state = 2;
            } else {
                state = 1;
            }
        }

        BaseMapMenu menu = new ShowAreaAndPointMapMenu(mapGISFrame, context, geoArea, xy, state, title, desc, pos);
        mapGISFrame.getFragment().menu = menu;
        return menu.onOptionsItemSelected();
    }
}
