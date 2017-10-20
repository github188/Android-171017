package com.mapgis.mmt.module.gis;

import android.view.MotionEvent;

import com.zondy.mapgis.android.mapview.MapTool;
import com.zondy.mapgis.android.mapview.MapView;

public class MmtMapTool implements MapTool {
    MapViewExtentChangeListener listener;
    MapTool mapOperationTool;

    public MmtMapTool(MmtMapView mapView) {
        listener = mapView;
        mapOperationTool = mapView.createMapOperationTool();
    }

    @Override
    public boolean panStateBegan(MotionEvent motionEvent, MapView mapView) {
        listener.ExtentChanging();

        return mapOperationTool.panStateBegan(motionEvent, mapView);
    }

    @Override
    public boolean panStateChanged(MotionEvent motionEvent, MapView mapView) {
        return mapOperationTool.panStateChanged(motionEvent, mapView);
    }

    @Override
    public boolean panStateEnded(MotionEvent motionEvent, MapView mapView, float v, float v1) {
        listener.ExtentChanged();

        return mapOperationTool.panStateEnded(motionEvent, mapView, v, v1);
    }

    @Override
    public boolean zoomAndRotateStateBegan(MotionEvent motionEvent, MapView mapView) {
        listener.ExtentChanging();

        return mapOperationTool.zoomAndRotateStateBegan(motionEvent, mapView);
    }

    @Override
    public boolean zoomAndRotateStateChanged(MotionEvent motionEvent, MapView mapView, float v, float v1) {
        return mapOperationTool.zoomAndRotateStateChanged(motionEvent, mapView, v, v1);
    }

    @Override
    public boolean zoomAndRotateStateEnded(MotionEvent motionEvent, MapView mapView, float v, float v1) {
        listener.ExtentChanged();

        return mapOperationTool.zoomAndRotateStateEnded(motionEvent, mapView, v, v1);
    }

    @Override
    public boolean longTap(MotionEvent motionEvent, MapView mapView) {
        return mapOperationTool.longTap(motionEvent, mapView);
    }

    @Override
    public void tap(MotionEvent motionEvent, MapView mapView) {
        mapOperationTool.tap(motionEvent, mapView);
    }

    @Override
    public boolean doubleTap(MotionEvent motionEvent, MapView mapView) {
        return mapOperationTool.doubleTap(motionEvent, mapView);
    }
}
