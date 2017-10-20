package com.repair.gisdatagather.product;

import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.module.gis.MmtMapTool;
import com.mapgis.mmt.module.gis.MmtMapView;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

/**
 * Created by liuyunfan on 2016/1/13.
 */
public class MmtMapToolCustrom extends MmtMapTool {
    private View xEditText;
    private View yEditText;
    private Dot dot;

    public MmtMapToolCustrom(MmtMapView mapView, View xEditText, View yEditText, Dot dot) {
        super(mapView);
        this.xEditText = xEditText;
        this.yEditText = yEditText;
        this.dot = dot;
    }

    @Override
    public boolean panStateChanged(MotionEvent motionEvent, MapView mapView) {
        Dot tempdot = mapView.getCenterPoint();
        this.dot.setX(tempdot.getX());
        this.dot.setY(tempdot.getY());
        if (xEditText instanceof TextView) {
            ((TextView) xEditText).setText(String.valueOf(Convert.FormatDouble(dot.getX())));
        }
        if (yEditText instanceof TextView) {
            ((TextView) yEditText).setText(String.valueOf(Convert.FormatDouble(dot.getY())));
        }
        return super.panStateChanged(motionEvent, mapView);
    }
}
