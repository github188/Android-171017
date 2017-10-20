package com.mapgis.mmt.module.gis;

import android.content.Context;
import android.util.Log;

import com.mapgis.mmt.MyApplication;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.android.mapview.MapView.MapViewRenderContextListener;

import java.util.concurrent.TimeUnit;

public class MmtMapViewRenderContextListener implements MapViewRenderContextListener {
    MapView mapView;
    public volatile static boolean hasOpened;

    public MmtMapViewRenderContextListener(Context context) {
        this.mapView = ((MapGISFrame) context).getMapView();
    }

    @Override
    public void mapViewRenderContextCreated() {
        Log.i(getClass().getSimpleName(), "mapViewRenderContextCreated");

        if (hasOpened) {
            return;
        }

        MyApplication.getInstance().submitExecutorService(task);
    }

    @Override
    public void mapViewRenderContextDestroyed() {
        Log.i(getClass().getSimpleName(), "mapViewRenderContextDestroyed");
    }

    Runnable task = new Runnable() {

        @Override
        public void run() {
            try {
                TimeUnit.MILLISECONDS.sleep(800);

                if (mapView != null){
                    mapView.setRenderContextListener(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                hasOpened = true;
            }
        }
    };
}
