package com.patrol.module.patroltrace;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.text.TextUtils;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.zondy.mapgis.android.graphic.GraphicImage;
import com.zondy.mapgis.android.graphic.GraphicLayer;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyunfan on 16/11/23.
 */

public class PatrolTraceToday {
    MapGISFrame context;
    MapView mapView;

    //今日轨迹图层
    GraphicLayer graphicLayer;
    public List<Dot> dots;
    Bitmap startIcon;
    Bitmap endIcon;

    public PatrolTraceToday(MapGISFrame mapGISFrame, String traceStr) {
        context = mapGISFrame;
        mapView = mapGISFrame.getMapView();
        startIcon = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_track_navi_end);
        endIcon = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_track_navi_start);
        dots = patrolStr2DotList(traceStr);

    }

    public void showTrace2Map() {
        if (dots == null || dots.size() < 2) {
            return;
        }
        graphicLayer = new GraphicLayer();
        GraphicPolylin graphicPolylin = new GraphicPolylin(dots.toArray(new Dot[dots.size()]));
        graphicPolylin.setLineWidth(5);
        graphicPolylin.setColor(Color.RED);
        graphicLayer.addGraphic(graphicPolylin);

        Dot startDot = dots.get(0);
        GraphicImage startImage = new GraphicImage(startDot, startIcon);
        startImage.setAnchorPoint(new PointF(0.5f, 0f));
        graphicLayer.addGraphic(startImage);

        Dot endDot = dots.get(dots.size() - 1);
        GraphicImage endImage = new GraphicImage(endDot, endIcon);
        endImage.setAnchorPoint(new PointF(0.5f, 0f));
        graphicLayer.addGraphic(endImage);

        mapView.getGraphicLayers().add(graphicLayer);

        mapView.zoomToRange(graphicPolylin.getBoundingRect(), true);
        mapView.refresh();
    }

    public void hideTrace() {

        if (graphicLayer != null) {
            mapView.getGraphicLayers().remove(graphicLayer);
            //  `mapView.refresh();
        }

    }

    public Dot patrolDotStr2Dot(String patrolDotStr) {
        try {

            String[] infos = patrolDotStr.split(",");
            if (infos.length < 3) {
                return null;
            }
            String x = infos[1];
            String y = infos[2];

            return new Dot(Double.valueOf(x), Double.valueOf(y));
        } catch (Exception ex) {
            return null;
        }
    }

    public List<Dot> patrolStr2DotList(String pointInfos) {
        List<Dot> dots = new ArrayList<>();
        if (TextUtils.isEmpty(pointInfos)) {
            return dots;
        }
        String[] points = pointInfos.split("\\|");
        for (String pointInfo : points) {
            Dot dot = patrolDotStr2Dot(pointInfo);
            if (dot == null) {
                continue;
            }
            dots.add(dot);
        }

        return dots;
    }

}
