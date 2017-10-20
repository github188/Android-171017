package com.mapgis.mmt.module.gis;

import com.zondy.mapgis.android.mapview.MapView;

public class MmtMapViewAnimationListener implements MapView.MapViewAnimationListener {
    MapViewExtentChangeListener listener;

    public MmtMapViewAnimationListener(MapViewExtentChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void mapViewAnimationStart(MapView mapView, int i) {
        if (i == MapView.AnimationTypePan)//移图操作不监听这个事件，改为监听下面的panStateBegan
            return;

        listener.ExtentChanging();
    }

    @Override
    public void mapViewAnimationFinish(MapView mapView, int i, boolean b) {
        if (i == MapView.AnimationTypePan)
            return;

        listener.ExtentChanged();
    }
}
