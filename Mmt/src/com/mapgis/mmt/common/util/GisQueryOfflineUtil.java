package com.mapgis.mmt.common.util;

import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.featureservice.Feature;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.GeomType;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.Map;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyunfan on 16/10/11.
 */
public class GisQueryOfflineUtil {

    /**点击查询 开始**/


    /**
     * 指定多个图层点击查询
     * 查询包括管点和管线和其他
     *
     * @param mapView
     * @param mapDot
     * @param layers
     * @param pageSize
     * @return
     */
    public static List<Graphic> offlinePointQueryV2(MapView mapView, Dot mapDot, List<MapLayer> layers, int pageSize) {
        if (layers == null || layers.size() == 0) {
            return null;
        }

        if (pageSize <= 0) {
            pageSize = 1;
        }

        double temp = mapView.getResolution(mapView.getZoom()) * 10;
        Rect rect = new Rect();
        rect.setXMin(mapDot.x - temp);
        rect.setYMin(mapDot.y - temp);
        rect.setXMax(mapDot.x + temp);
        rect.setYMax(mapDot.y + temp);

        for (MapLayer layer : layers) {

            List<Graphic> graphics = offlineQueryGraphic(layer, rect, "", pageSize);
            if (graphics == null || graphics.size() == 0) {
                continue;
            }
            return graphics;
        }
        return null;
    }


    /**
     * 指定多个图层点击查询
     * 只查询管点和管线
     *
     * @param mapView
     * @param mapDot
     * @param layers
     * @param pageSize
     * @return
     */
    public static List<Graphic> offlinePointQuery(MapView mapView, Dot mapDot, List<MapLayer> layers, int pageSize) {
        if (layers == null || layers.size() == 0) {
            return null;
        }

        if (pageSize <= 0) {
            pageSize = 1;
        }

        double temp = mapView.getResolution(mapView.getZoom()) * 10;
        Rect rect = new Rect();
        rect.setXMin(mapDot.x - temp);
        rect.setYMin(mapDot.y - temp);
        rect.setXMax(mapDot.x + temp);
        rect.setYMax(mapDot.y + temp);

        for (MapLayer layer : layers) {
            if ((!layer.GetGeometryType().equals(GeomType.GeomPnt)) && (!layer.GetGeometryType().equals(GeomType.GeomLin))) {
                continue;
            }
            List<Graphic> graphics = offlineQueryGraphic(layer, rect, "", pageSize);
            if (graphics == null || graphics.size() == 0) {
                continue;
            }
            return graphics;
        }
        return null;
    }

    /**点击查询   结束**/


    /**
     * 属性条件查询   开始
     **/


    public static List<Graphic> conditionQuery(String layerName, String strWhere) {
        List<Graphic> graphics = new ArrayList<>();
        if (TextUtils.isEmpty(layerName)) {
            return graphics;
        }
        if (TextUtils.isEmpty(strWhere)) {
            return graphics;
        }
        MapView mapView = MyApplication.getInstance().mapGISFrame.getMapView();
        if (mapView == null) {
            return graphics;
        }

        MapLayer layer = null;
        Map map = mapView.getMap();
        if (map == null) {
            MyApplication.getInstance().showMessageWithHandle("请配置地图");
            return graphics;
        }
        LayerEnum layerEnum = map.getLayerEnum();
        layerEnum.moveToFirst();
        MapLayer mapLayer;
        while ((mapLayer = layerEnum.next()) != null) {
            if (!(mapLayer instanceof VectorLayer)) {
                continue;
            }
            if (!mapLayer.getName().equals(layerName)) {
                continue;
            }
            layer = mapLayer;
            break;
        }
        if (layer == null) {
            return graphics;
        }

        List<Graphic> graphicList = offlineQueryGraphic(layer, null, strWhere, 10);
        if (graphicList == null) {
            return graphics;
        }
        graphics.addAll(graphicList);

        return graphics;
    }

    /**
     * 属性条件查询   结束
     **/

    public static List<Graphic> offlineQueryGraphic(MapLayer layer, Rect rect, String strWhere, int pageSize) {

        List<Graphic> graphics = new ArrayList<>();

        try {
            FeatureQuery.QueryBound queryBound = rect != null ? new FeatureQuery.QueryBound(rect) : null;

            FeaturePagedResult featurePagedResult = FeatureQuery.query((VectorLayer) layer, strWhere, queryBound, FeatureQuery.SPATIAL_REL_MBROVERLAP, true, true, "", pageSize);

            int pageCount = featurePagedResult.getPageCount();
            for (int i = 0; i < pageCount; i++) {
                List<Feature> featureList = featurePagedResult.getPage(i+1);
                if (featureList == null) {
                    continue;
                }
                List<Graphic> graphicList = Convert.fromFeaturesToGraphics(featureList, layer.getName());
                if (graphicList == null) {
                    continue;
                }
                graphics.addAll(graphicList);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return graphics;
    }

}
