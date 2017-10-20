package com.mapgis.mmt.module.gis.toolbar.query.point;

import android.graphics.BitmapFactory;
import android.graphics.PointF;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

/**
 * Created by cmios on 2017/3/9.
 */

public class PanoramaListener implements MapView.MapViewTapListener {
    private final MapGISFrame mapGISFrame;
    private PanoramaAnnotationListener mmtAnnotationListener;
    private final MapView mapView;
    private Dot clickPoint;
    public PanoramaListener(MapGISFrame mapGISFrame,MapView mapView) {
        this.mapGISFrame = mapGISFrame;
        this.mapView = mapView;
    }
    public void setPanoramaAnnotationListener( PanoramaAnnotationListener mmtAnnotationListener){

        this.mmtAnnotationListener = mmtAnnotationListener;
    }

    @Override
    public void mapViewTap(PointF pointF) {
//        if (mmtAnnotationListener != null && mmtAnnotationListener.getHideTapListener()) {
//            mmtAnnotationListener.setHideTapListener(false);
//            return;
//        }
        mapView.getGraphicLayer().removeAllGraphics();
        mapView.getAnnotationLayer().removeAllAnnotations();

        clickPoint = mapView.viewPointToMapPoint(pointF);
        MmtAnnotation mmtAnnotation = new MmtAnnotation("查看全景","","",clickPoint,
                BitmapFactory.decodeResource(mapView.getContext().getResources(),R.drawable.icon_mark_pt));
        mapView.setAnnotationListener(mmtAnnotationListener);


        mapView.getAnnotationLayer().addAnnotation(mmtAnnotation);
        mapView.getAnnotationLayer().getAnnotation(0).showAnnotationView();
    }
}
