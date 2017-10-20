package com.repair.gisdatagather.common;

import android.os.Handler;
import android.os.Message;

import com.repair.gisdatagather.common.entity.TextDot;
import com.repair.gisdatagather.common.entity.TextLine;
import com.zondy.mapgis.android.mapview.MapView;

/**
 * Created by liuyunfan on 2016/3/30.
 */
public class PainGisdata2MapViewHander extends Handler {
    MapView mapView;

    public PainGisdata2MapViewHander(MapView mapView) {
        this.mapView = mapView;
    }

    @Override
    public void handleMessage(Message msg) {
        try {
            switch (msg.what) {
                case 1: {
                    TextDot textDot = (TextDot) msg.obj;
                    textDot.addTextDot(mapView, false);
                }
                break;
                case 2: {
                    TextLine textLine = (TextLine) msg.obj;
                    textLine.addLine(mapView, false);
                }
                break;
                case 3: {
                    mapView.refresh();
                }
                break;
                case 4: {
                    mapView.getGraphicLayer().removeAllGraphics();
                    mapView.refresh();
                }
                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

