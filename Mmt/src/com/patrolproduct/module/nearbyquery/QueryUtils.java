package com.patrolproduct.module.nearbyquery;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.R;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.attr.Field;
import com.zondy.mapgis.attr.Fields;
import com.zondy.mapgis.featureservice.Feature;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Comclay on 2016/11/10.
 * 范围搜索帮助类
 */

public class QueryUtils {
    /**
     * 查询圆形区域内的某一图层的信息
     *
     * @param mapView   地图
     * @param layerName 图层名称
     * @param dot       查询的圆心位置
     * @param radius    查询的半径
     * @return 查询的结果
     */
    public static ArrayList<Feature> searchLayerWithCircle(MapView mapView, @NonNull String layerName, @NonNull final Dot dot, @Nullable final double radius) {
        try {
            if (mapView == null || BaseClassUtil.isNullOrEmptyString(layerName)) return null;
            // 根据图层名称获取图层MapLayer对象
            MapLayer layer = GisUtil.getPointQueryVectorLayer(mapView, layerName);

            FeatureQuery.QueryBound bound = new FeatureQuery.QueryBound(mapView.getMap().getRange());
            if (radius > 0) {
                // 查询的矩形区域
                Rect rect = new Rect();
                rect.setXMin(dot.x - radius);
                rect.setYMin(dot.y - radius);
                rect.setXMax(dot.x + radius);
                rect.setYMax(dot.y + radius);
                bound = new FeatureQuery.QueryBound(rect);
            }
            // 存储要素查询结果
            FeaturePagedResult featurePagedResult = FeatureQuery.query(
                    (VectorLayer) layer
                    , ""
                    , bound
                    , FeatureQuery.SPATIAL_REL_MBROVERLAP
                    , true
                    , true
                    , ""
                    , 20);
            if (featurePagedResult == null) return null;

            // 再将不在圆形区域内的点过滤掉
            ArrayList<Feature> featureList = new ArrayList<>();
            for (int i = 0; i < featurePagedResult.getPageCount(); i++) {
                List<Feature> page = featurePagedResult.getPage(i + 1);
                for (Feature feature : page) {
                    if (isInnerCircle(feature, dot, radius)) featureList.add(feature);
                }
            }

            // 定义一个比较器，根据距离降序排序
            Comparator<Feature> featureComparator = new Comparator<Feature>() {
                @Override
                public int compare(Feature lhs, Feature rhs) {
                    return GisUtil.calcDistance(lhs.toGraphics(false).get(0).getCenterPoint(), dot)
                            < GisUtil.calcDistance(rhs.toGraphics(false).get(0).getCenterPoint(), dot)
                            ? -1 : 1;
                }

                @Override
                public boolean equals(Object object) {
                    return false;
                }
            };
            Collections.sort(featureList, featureComparator);

            return featureList;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 判断点是否在圆里面
     *
     * @param resDot    将要判断的点
     * @param centerDot 圆心点
     * @param radius    半径
     * @return 是否在圆里面
     */
    public static boolean isInnerCircle(Dot resDot, Dot centerDot, double radius) {
        return !(resDot == null || centerDot == null || radius <= 0)
                && GisUtil.calcDistance(resDot, centerDot) <= radius;
    }

    /**
     * 判断Feature对象所包含的Graphic是否有点包含在圆形区域内
     *
     * @return true 包含，false 不包含
     */
    public static boolean isInnerCircle(Feature feature, Dot centerDot, double radius) {
        List<Graphic> graphicList = feature.toGraphics(true);
        for (Graphic graphic : graphicList) {
            Dot centerPoint = graphic.getCenterPoint();
            if (isInnerCircle(centerPoint, centerDot, radius)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 进入到详情界面
     *
     * @param context 上下文
     * @param attrMap 属性信息
     */
    public static void enterDetailActivity(Context context, LinkedHashMap<String, String> attrMap, boolean showLocateView) {
        Intent intent = new Intent(context, PipeDetailActivity.class);
        intent.putExtra("FragmentClass", ElemDetailFragment.class);
        intent.putExtra("layerName", "详细信息");
        intent.putExtra("graphicMap", attrMap);
        intent.putExtra("graphicMapStr", new Gson().toJson(attrMap));
        intent.putExtra("unvisiable_detail_fragment", true);
        intent.putExtra("needLoc", showLocateView);
        context.startActivity(intent);
    }

    /**
     * 计算两点之间的距离并格式化为字符串数据
     *
     * @param srcDot    坐标1
     * @param targetDot 坐标2
     * @return 格式化后的字符串
     */
    public static String getFormatDistance(Dot srcDot, Dot targetDot) {
        // 计算两点之间的距离，单位：米
        double distance = GisUtil.calcDistance(srcDot, targetDot);
        String style;
        if (distance > 1000) {
            distance = distance / 1000;
            style = "0.00公里";
        } else {
            style = "0米";
        }
        DecimalFormat df = new DecimalFormat(style);
        return df.format(distance);
    }

    /**
     * 将Feature转换成键值对形式的Map集合
     *
     * @param feature Featrue对象
     * @return 键值对
     */
    public static LinkedHashMap<String, String> featureToMap(Feature feature) {
        LinkedHashMap<String, String> dataMap = new LinkedHashMap<>();
        if (feature == null) {
            return dataMap;
        }

        Fields fields = feature.getFields();
        for (short i = 0; i < fields.getFieldCount(); i++) {
            Graphic graphic = feature.toGraphics(true).get(0);
            Field field = fields.getField(i);
            String fieldName = field.getFieldName();
            dataMap.put(fieldName, graphic.getAttributeValue(fieldName));
        }
        return dataMap;
    }

    public static LinkedHashMap<String, String> graphicToMap(Graphic graphic) {
        LinkedHashMap<String, String> dataMap = new LinkedHashMap<>();
        if (graphic == null) {
            return dataMap;
        }

        for (short i = 0; i < graphic.getAttributeNum(); i++) {
            dataMap.put(graphic.getAttributeName(i), graphic.getAttributeValue(i));
        }
        return dataMap;
    }

    /**
     * 定位
     *
     * @param context 上下文
     * @param feature 属性对象
     * @param attMap  属性名-属性值 键值对 可为空
     */
    public static void onLocated(Context context, final Feature feature, @Nullable final LinkedHashMap<String, String> attMap) {
        showElemOnMap(context, feature);
    }

    public static void showAllElemOnMap(Context context, List<Feature> featureList) {
        if (featureList == null || featureList.size() == 0) return;

        Graphic graphic = featureList.get(0).toGraphics(true).get(0);
        LinkedHashMap<String, String> attMap = graphicToMap(graphic);

        String titleAttName = "组分类型";
        String textAttName = "编号";
        if (BaseClassUtil.isNullOrEmptyString(attMap.get(textAttName))) {
            textAttName = "GIS编号";
        }

        ArrayList<Graphic> graphicList = new ArrayList<>();
        for (Feature feature : featureList) {
            graphicList.add(feature.toGraphics(true).get(0));
        }

        LocateMapCallBack locateMapCallBack = new LocateMapCallBack(graphicList, context, titleAttName, textAttName, -1);
        MyApplication.getInstance().sendToBaseMapHandle(locateMapCallBack);
    }

    /**
     * 将属性featrue标注到到地图上
     */
    public static void showElemOnMap(Context context, Feature featrue) {
        Graphic graphic = featrue.toGraphics(true).get(0);
        LinkedHashMap<String, String> attMap = graphicToMap(graphic);

        String title = attMap.get("组分类型");
        String text = attMap.get("编号");
        if (BaseClassUtil.isNullOrEmptyString(text)) {
            text = attMap.get("GIS编号");
        }

        LocateMapCallBack locateMapCallBack = new LocateMapCallBack(graphic, context, title, text, -1);
        MyApplication.getInstance().sendToBaseMapHandle(locateMapCallBack);
    }

    @Deprecated
    public static void showElemOnMap(final Feature feature, @Nullable final LinkedHashMap<String, String> attMap) {
        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
            @Override
            public boolean handleMessage(Message msg) {
                Graphic graphic = feature.toGraphics(true).get(0);
                LinkedHashMap<String, String> tempMap = attMap;
                if (attMap == null) {
                    tempMap = featureToMap(feature);
                }
                Dot dot = graphic.getCenterPoint();
                String title = tempMap.get("组分类型");   // 图层类型
                String text = tempMap.get("编号");        // 编号

                mapView.getAnnotationLayer().removeAllAnnotations();
                mapView.getGraphicLayer().removeAllGraphics();
                mapView.setShowUserLocation(true);
                MmtAnnotation mmtAnnotation = new MmtAnnotation(
                        graphic
                        , title
                        , text
                        , dot
                        , BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_select_point));
                mmtAnnotation.showAnnotationView();
                mapView.panToCenter(dot, true);
                mapView.getAnnotationLayer().addAnnotation(mmtAnnotation);

                if (graphic instanceof GraphicPolylin) {
                    // 管段
                    graphic.setColor(Color.RED);
                    ((GraphicPolylin) graphic).setLineWidth(5);
                    mapView.getGraphicLayer().addGraphic(graphic);
                }

                mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
                    @Override
                    public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
                        Annotation annotation = annotationview.getAnnotation();
                        if (annotation instanceof MmtAnnotation) {
                            enterDetailActivity(mapGISFrame, graphicToMap(((MmtAnnotation) annotation).graphic), false);
                        }
                    }
                });

                mapView.refresh();
                return true;
            }
        });
    }
}
