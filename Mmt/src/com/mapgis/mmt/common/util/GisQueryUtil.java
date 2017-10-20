package com.mapgis.mmt.common.util;

import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineFeature;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineGermetry;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * 统一在线和离线点击查询工具类
 */
public class GisQueryUtil {
    private static boolean isOfflineQuery = isUseOfflineQuery();
    public static List<MapLayer> layers = null;


    /**
     * 不指定图层，查全部图层
     * 该方法同时查管点,管线,区 和其他类型
     *
     * @param mapView
     * @param mapDot
     * @return
     */
    public static Graphic pointQueryForSingleV2(MapView mapView, Dot mapDot) {

        List<Graphic> graphics = pointQueryForListV2(mapView, mapDot, 1);
        if (graphics == null || graphics.size() == 0) {
            return null;
        }
        return graphics.get(0);

    }

    /**
     * 不指定图层，查全部图层
     * 该方法只查管点,管线
     *
     * @param mapView
     * @param mapDot
     * @return
     */
    public static Graphic pointQueryForSingle(MapView mapView, Dot mapDot) {

        List<Graphic> graphics = pointQueryForList(mapView, mapDot, 1);
        if (graphics == null || graphics.size() == 0) {
            return null;
        }
        return graphics.get(0);

    }

    /**
     * 不指定图层，查全部图层
     * 该方法同时查管点,管线,区 和其他类型
     *
     * @param mapView
     * @param mapDot
     * @return
     */
    public static List<Graphic> pointQueryForListV2(MapView mapView, Dot mapDot, int expectRetCount) {

        if (isOfflineQuery) {
            if (layers == null) {
                layers = GisUtil.getPointQueryVectorLayer(mapView);
                if (layers == null || layers.size() == 0) {
                    isOfflineQuery = false;
                }
            }
        }

        if (isOfflineQuery) {
            return GisQueryOfflineUtil.offlinePointQueryV2(mapView, mapDot, layers, expectRetCount);
        }
        return GisQueryOnlineUtil.onlinePointQuery(mapView, mapDot, expectRetCount);

    }

    /**
     * 不指定图层，查全部图层
     * 该方法只查管点,管线
     *
     * @param mapView
     * @param mapDot
     * @param expectRetCount
     * @return
     */
    public static List<Graphic> pointQueryForList(MapView mapView, Dot mapDot, int expectRetCount) {

        if (isOfflineQuery) {
            if (layers == null) {
                layers = GisUtil.getPointQueryVectorLayer(mapView);
                if (layers == null || layers.size() == 0) {
                    isOfflineQuery = false;
                }
            }
        }

        if (isOfflineQuery) {
            return GisQueryOfflineUtil.offlinePointQuery(mapView, mapDot, layers, expectRetCount);
        }
        return GisQueryOnlineUtil.onlinePointQuery(mapView, mapDot, expectRetCount);

    }


    /**
     * 属性条件查询 开始
     */

    /**
     * @param layerName 图片名
     * @param strWhere  查询属性条件
     * @return
     */
    public static List<Graphic> conditionQuery(String layerName, String strWhere) {

        if (isOfflineQuery) {
            return GisQueryOfflineUtil.conditionQuery(layerName, strWhere);
        }
        return GisQueryOnlineUtil.conditionQuery(layerName, strWhere);
    }

    /**
     * 条件查询 结束
     */


    /** 公共代码 开始**/


    /**
     * 只考虑管点和管线
     *
     * @param onlineFeature
     * @return
     */
    public static Graphic onlineFeature2Graphic(OnlineFeature onlineFeature) {
        if (onlineFeature == null) {
            return null;
        }
        OnlineGermetry onlineGermetry = onlineFeature.geometry;
        if (onlineGermetry == null) {
            return null;
        }

        if (onlineGermetry.paths == null || onlineGermetry.paths.length < 1) {
            GraphicPoint graphic = new GraphicPoint();
            graphic.setPoint(new Dot(onlineGermetry.x, onlineGermetry.y));
            setKVs2GraphicV2(graphic, onlineFeature);
            return graphic;
        } else {
            GraphicPolylin graphic = new GraphicPolylin();
            List<Dot> dots = new ArrayList<>();
            double[][] lines = onlineGermetry.paths[0];

            for (int i = 0; i < lines.length; i++) {
                dots.add(new Dot(lines[i][0], lines[i][1]));
            }
            graphic.setPoints(dots);
            setKVs2GraphicV2(graphic, onlineFeature);
            return graphic;
        }

    }

    /**
     * 判断当前使用的是离线还是在线地图（管网）
     * 初始化状态
     * @return
     */
    public static boolean isofflineMap() {

        return "true".equalsIgnoreCase(MobileConfig.MapConfigInstance.getMapType(MapConfig.MOBILE_EMS).Visible);
    }

    /**
     * 判断当前地图应该采用哪种查询模式（在线查询或离线查询）
     * 配置离线地图，且没有勾选在线查询 就采用离线查询方式，反之在线查询
     *
     * @return 是否是离线查询
     */
    public static boolean isUseOfflineQuery() {
        return !MobileConfig.MapConfigInstance.IsVectorQueryOnline && isofflineMap();
    }

    public static void setKVs2GraphicV2(Graphic graphic, OnlineFeature onlineFeature) {
        if (onlineFeature == null) {
            return;
        }
        setKVs2Graphic(graphic, onlineFeature.attributes);

        //离线查询中自定义了 $图层名称$
        //方便统一处理，这里一定要加
        if (!onlineFeature.attributes.containsKey("$图层名称$")) {
            graphic.setAttributeValue("$图层名称$", onlineFeature.layerName);
        }

        if (!onlineFeature.attributes.containsKey("编号")) {
            graphic.setAttributeValue("编号", "-");
        }
    }

    public static void setKVs2Graphic(Graphic graphic, LinkedHashMap<String, String> attrs) {
        if (graphic == null) {
            return;
        }
        if (attrs == null) {
            return;
        }
        Set<String> keys = attrs.keySet();
        for (String key : keys) {

            String val = attrs.get(key);
            if (TextUtils.isEmpty(val)) {
                val = "-";
            }
            graphic.setAttributeValue(key, val);
        }

    }

    /** 公共代码 结束**/

    /**
     * 原先就有的开始
     */

    public static ArrayList<String> getVisibleVectorLayerNames(MapView mapView) {
        ArrayList<String> layers = new ArrayList<>();

        LayerEnum layerEnum = mapView.getMap().getLayerEnum();
        layerEnum.moveToFirst();
        MapLayer layer;
        while ((layer = layerEnum.next()) != null) {

            if (!(layer instanceof VectorLayer && layer.getIsVisible() && LayerConfig.getInstance().getConfigInfo(layer.getName()).IsEquipment)) {
                continue;
            }

            layers.add(layer.getName());
        }

        return layers;
    }

    /**
     * 获取 点击查询图层名， 过滤掉 mobileconfig.json里配置的 PointQueryLayerFilter 图层
     * PointQueryLayerFilter 例值
     * {
     * "Key": "PointQueryLayerFilter",
     * "Value": "道路网|居民地|地名点|地名注记",
     * "Label": "点击查询过滤图层",
     * "Description": "过滤点击查询时不想查询的某些图层，如地形图的道路网、居民用地等",
     * "ExtraInfo": ""
     * },
     */
    public static ArrayList<String> getPointQueryVectorLayerNames(MapView mapView) {
        ArrayList<String> layers = new ArrayList<>();
        String pointQueryLayerFilter = MyApplication.getInstance().getConfigValue("PointQueryLayerFilter");

        if (BaseClassUtil.isNullOrEmptyString(pointQueryLayerFilter))
            return getVisibleVectorLayerNames(mapView);

        List<String> pointQueryLayerFilterList = Arrays.asList(pointQueryLayerFilter.split("\\|"));

        LayerEnum layerEnum = mapView.getMap().getLayerEnum();
        layerEnum.moveToFirst();
        MapLayer layer;
        while ((layer = layerEnum.next()) != null) {

            if (!(layer instanceof VectorLayer && layer.getIsVisible() && LayerConfig.getInstance().getConfigInfo(layer.getName()).IsEquipment)) {
                continue;
            }
            if (pointQueryLayerFilterList.contains(layer.getName())) {
                continue;
            }

            layers.add(layer.getName());
        }

        return layers;
    }
    /**
     *    原先就有的结束
     */

}
