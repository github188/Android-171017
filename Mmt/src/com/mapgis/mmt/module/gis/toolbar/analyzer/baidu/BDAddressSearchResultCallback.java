package com.mapgis.mmt.module.gis.toolbar.analyzer.baidu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;

import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.place.BDPlaceSearchResult;
import com.mapgis.mmt.module.gis.place.SingleSearchResult;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

public class BDAddressSearchResultCallback extends BaseMapCallback {
    private final int SHOW_ALL = 0;
    private final int SHOW_SINGLE = 1;
    private final int SHOW_MINE = 2;
    private int mode = SHOW_ALL;

    private BDPlaceSearchResult bdPlaceSearchResult;

    public BDAddressSearchResultCallback(BDPlaceSearchResult bdPlaceSearchResult) {
        this.bdPlaceSearchResult = bdPlaceSearchResult;
        this.mode = SHOW_ALL;
    }

    private Dot dot;

    public BDAddressSearchResultCallback(Dot dot) {
        this.dot = dot;
        this.mode = SHOW_MINE;
    }

    private SingleSearchResult singleSearchResult;

    public BDAddressSearchResultCallback(SingleSearchResult singleSearchResult) {
        this.singleSearchResult = singleSearchResult;
        this.mode = SHOW_SINGLE;
    }

    @Override
    public boolean handleMessage(Message arg0) {
        try {
            switch (mode) {
                case SHOW_ALL:// 绘制全部
                    mapView.getGraphicLayer().removeAllGraphics();
                    mapView.getAnnotationLayer().removeAllAnnotations();

                    for (SingleSearchResult s : bdPlaceSearchResult.results) {
                        showDotOnMap(mapView, new Dot(s.getXyz().getX(), s.getXyz().getY()), s.name, s.address);
                    }
                    break;
                case SHOW_SINGLE:// 绘制单一坐标
                    Annotation annotation = showDotOnMap(mapView, new Dot(singleSearchResult.getXyz().getX(), singleSearchResult.getXyz()
                            .getY()), singleSearchResult.name, singleSearchResult.address);

                    annotation.showAnnotationView();
                    break;
                case SHOW_MINE:// 绘制自定义坐标
                    showDotOnMap(mapView, dot, "自定义坐标点", null);
                    break;
            }

            mapView.refresh();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    // 绘制点
    private Annotation showDotOnMap(MapView mapView, Dot dot, String str1, String str2) {
        Annotation annotation = new Annotation(str1, str2, dot, null);

        Bitmap bitmap = BitmapFactory.decodeResource(mapView.getResources(), R.drawable.icon_lcoding);

        annotation.setImage(bitmap);

        mapView.getAnnotationLayer().addAnnotation(annotation);

        return annotation;
    }

}
