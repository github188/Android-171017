package com.repair.zhoushan.module.devicecare.platfromgislink;

import android.view.MotionEvent;
import android.widget.TextView;

import com.mapgis.mmt.module.gis.MmtMapTool;
import com.mapgis.mmt.module.gis.MmtMapView;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

/**
 * Created by liuyunfan on 2016/1/13.
 */
public class MmtMapToolCustrom extends MmtMapTool {
    private TextView textView;

    public MmtMapToolCustrom(MmtMapView mapView, TextView textView) {
        super(mapView);
        this.textView = textView;
    }

    @Override
    public boolean panStateChanged(MotionEvent motionEvent, MapView mapView) {
        Dot tempdot = mapView.getCenterPoint();
        textView.setText(tempdot.toString());
        return super.panStateChanged(motionEvent, mapView);
    }
}
