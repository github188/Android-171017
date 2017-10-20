package com.mapgis.mmt.module.gis.toolbar.accident2.presenter;

import android.graphics.Color;
import android.graphics.Point;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapgis.mmt.common.widget.AssistAnnotation;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureGeometry;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureItem;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaItem;
import com.mapgis.mmt.module.gis.toolbar.accident2.util.IconFactory;
import com.mapgis.mmt.module.gis.toolbar.accident2.util.MetaType;
import com.mapgis.mmt.module.gis.toolbar.accident2.view.IPipeBrokenAnalysisView;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;

/**
 * Created by Comclay on 2017/3/3.
 * 子线程与UI主线程交互用的Runnable对象
 */

class InteractionRunnable implements Runnable {
    private String mType;
    private IPipeBrokenAnalysisView mBrokenView;
    private FeatureMetaItem mItem;
    private static int mRunCount = 0;
    private boolean showPipeAnnotation = false;

    InteractionRunnable(IPipeBrokenAnalysisView view, String mType, FeatureMetaItem mItem) {
        this.mBrokenView = view;
        this.mType = mType;
        this.mItem = mItem;
        mRunCount++;
    }

    @Override
    public void run() {
        MapView mapView = this.mBrokenView.getMapView();
        switch (this.mType) {
            case MetaType.TYPE_INCIDENT_POINT:
                showBrokenPoint(mapView, new Point(0, 0));
                break;
            case MetaType.TYPE_PIPE_LINE:   // 受影响管段
                showLine(mapView);
                if (showPipeAnnotation) {
                    showPipeAnnotation(mapView, new Point());
                }
                break;
            case MetaType.TYPE_SWI_EFFECT:  // 受影响用户
                Point point = new Point(0, -mBrokenView.getBitmap(mType).getHeight() / 2);
                showPoint(mapView, point);
                break;
            default:
                showPoint(mapView, new Point(0, 0));
                break;
        }
//        mRunCount--;
//        if (mRunCount == 0) {
//            Rect rect = this.mBrokenView.getRect();
//            if (rect != null) {
//                mapView.zoomToRange(rect, true);
//            } else {
//                mapView.zoomToCenter(this.mBrokenView.getBrokenPoint(), 7.0f, true);
//            }
//        }
//        mapView.refresh();
        this.mBrokenView = null;
    }

    /**
     * 显示爆管点
     */
    private void showBrokenPoint(MapView mapView, Point point) {
        FeatureGroup featureGroup = this.mItem.getFeatureGroup();
        if (featureGroup == null) return;
        ArrayList<FeatureItem> features = featureGroup.getFeatures();
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        for (int i = 0; i < features.size(); i++) {
//            Dot dot = mBrokenView.getBrokenPoint();
            Dot dot = getCurrectBrokenDot(features.get(i));
            if (dot == null) continue;
            Annotation annotation = AssistAnnotation.create(dot
                    , mBrokenView.getBitmap(MetaType.TYPE_INCIDENT_POINT))
                    .setTipTitle(featureGroup.getDisplayFieldValue(i))
                    .setInfo(featureGroup.getLayerName())
                    .setAttribute(gson.toJson(featureGroup.getAllVisiableAttribute(i)));
            annotation.setCenterOffset(point);
            mapView.getAnnotationLayer().addAnnotation(annotation);
        }
        mapView.refresh();
    }

    private Dot getCurrectBrokenDot(FeatureItem feature) {
    /*
    爆管点坐标矫正
    如果是管段则用选中的坐标和管段垂线的交叉点作为爆管点
     */
        Dot dot = mBrokenView.getBrokenPoint();
        double[][][] paths = feature.geometry.getPaths();
        if (paths != null) {
            Dot dot1 = new Dot(paths[0][0][0], paths[0][0][1]);
            Dot dot2 = new Dot(paths[0][1][0], paths[0][1][1]);
            return currentDot(dot1, dot2, dot);
        }
        return feature.getDot();
    }

    /**
     * 二分法计算管段之外一点到管段的垂线的交点坐标
     *
     * @param dot1 管段端点
     * @param dot2 管段端点
     * @param dot  管段外一点
     * @return 交点坐标
     */
    private Dot currentDot(Dot dot1, Dot dot2, Dot dot) {
        Dot midDot = new Dot((dot1.getX() + dot2.getX()) / 2, (dot1.getY() + dot2.getY()) / 2);
        double dist = Math.pow(dot1.getX() - dot2.getX(), 2) + Math.pow(dot1.getY() - dot2.getY(), 2);
        if (dist < 0.0000001) {
            return midDot;
        }

        double dotToDot1 = Math.pow(dot.getX() - dot1.getX(), 2) + Math.pow(dot.getY() - dot1.getY(), 2);
        double dotToDot2 = Math.pow(dot.getX() - dot2.getX(), 2) + Math.pow(dot.getY() - dot2.getY(), 2);

        if (dotToDot1 < dotToDot2) {
            return currentDot(dot1, midDot, dot);
        } else {
            return currentDot(midDot, dot2, dot);
        }
    }

    private void showPipeAnnotation(MapView mapView, Point point) {
        FeatureGroup featureGroup = this.mItem.getFeatureGroup();
        if (featureGroup == null) return;
        ArrayList<FeatureItem> features = featureGroup.getFeatures();
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        for (int i = 0; i < features.size(); i++) {
            FeatureItem featureItem = features.get(i);
            Dot dot = featureItem.getDot();
            if (dot == null) continue;
            Annotation annotation = AssistAnnotation.create(dot
                    , mBrokenView.getBitmap(IconFactory.PIPE_ANNOTATION))
                    .setTipTitle(featureGroup.getDisplayFieldValue(i))
                    .setInfo(featureGroup.getLayerName())
                    .setAttribute(gson.toJson(featureGroup.getAllVisiableAttribute(i)));
            annotation.setCenterOffset(point);
            mapView.getAnnotationLayer().addAnnotation(annotation);
        }
        mapView.refresh();
    }

    /**
     * 显示线类型，GraphicLine
     * 并计算受影响的整个范围大小
     */
    private void showLine(MapView mapView) {
        FeatureGroup featureGroup = this.mItem.getFeatureGroup();
        if (featureGroup == null) {
            return;
        }
        ArrayList<FeatureItem> features = featureGroup.getFeatures();
        Rect rect = new Rect();
        Rect range = mapView.getMap().getRange();
        rect.setXMax(range.getXMin());
        rect.setXMin(range.getXMax());
        rect.setYMax(range.getYMin());
        rect.setYMin(range.getYMax());

        for (int i = 0; i < features.size(); i++) {
            FeatureItem featureItem = features.get(i);
            FeatureGeometry geometry = featureItem.geometry;
            double[][][] paths = geometry.getPaths();
            if (paths == null) {
                continue;
            }

            double x;
            double y;
            Dots dots = new Dots();
            GraphicPolylin polylin;
            for (double[][] arrayDot : paths) {
                for (double[] array : arrayDot) {
                    if (array.length == 2) {
                        x = array[0];
                        y = array[1];
                        if (rect.getXMin() > x) rect.setXMin(x);
                        if (rect.getXMax() < x) rect.setXMax(x);
                        if (rect.getYMin() > y) rect.setYMin(y);
                        if (rect.getYMax() < y) rect.setYMax(y);

                        dots.append(new Dot(x, y));
                        polylin = new GraphicPolylin(dots);
                        polylin.setLineWidth(6f);
                        polylin.setColor(Color.BLUE);
//                        polylin.setFillTexture(IconFactory.create(MyApplication.getInstance().getResources())
//                                .getBitmap(MetaType.TYPE_PIPE_LINE));
                        mapView.getGraphicLayer().addGraphic(polylin);
                    }
                }
            }
        }
        if (this.mBrokenView.getRect() == null) {
            this.mBrokenView.setRect(rect);
            mapView.zoomToRange(rect, true);
        }
        mapView.refresh();
    }

    /**
     * 显示点类型，标注
     */
    private void showPoint(MapView mapView, Point point) {
        FeatureGroup featureGroup = this.mItem.getFeatureGroup();
        if (featureGroup == null) return;
        ArrayList<FeatureItem> features = featureGroup.getFeatures();
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        for (int i = 0; i < features.size(); i++) {
            FeatureItem featureItem = features.get(i);
            Dot dot = featureItem.getDot();
            if (dot == null) continue;
            Annotation annotation = AssistAnnotation.create(dot
                    , mBrokenView.getBitmap(mType))
                    .setTipTitle(featureGroup.getDisplayFieldValue(i))
                    .setInfo(featureGroup.getLayerName())
                    .setAttribute(gson.toJson(featureGroup.getAllVisiableAttribute(i)));
            annotation.setCenterOffset(point);
            mapView.getAnnotationLayer().addAnnotation(annotation);
        }
        mapView.refresh();
    }

    public void setShowPipeAnnotation(boolean showPipeAnnotation) {
        this.showPipeAnnotation = showPipeAnnotation;
    }
}
