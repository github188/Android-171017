package com.mapgis.mmt.common.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineFeature;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineQueryResult;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineQueryService;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.List;

/**
 * 在线查询传入geometryTypeValue 无效
 * 无法区分是查询管点管网还是其他，只能统一查询全部
 */
public class GisQueryOnlineUtil {

    /**
     * 不指定图层，查询所有图层
     *
     * @param mapView
     * @param mapDot
     * @param retCount
     * @return
     */
    public static List<Graphic> onlinePointQuery(MapView mapView, Dot mapDot, int retCount) {

        List<String> layerIds = MapServiceInfo.getInstance().getLayerIds();
        return onlinePointQuery(mapView, mapDot, layerIds, 1);
    }

    /**
     * 指定图层查询
     *
     * @param mapView
     * @param mapDot
     * @param layerIds
     * @param retCount
     * @return
     */
    public static List<Graphic> onlinePointQuery(MapView mapView, Dot mapDot, List<String> layerIds, int retCount) {

        if (layerIds == null || layerIds.size() == 0) {
            return null;
        }

        String layersValue = "0:" + TextUtils.join(",", layerIds);
        List<String> queryParams = onlineQueryParams(mapView, mapDot, layersValue, "");

        return onlinePointQueryGraphic(mapView, mapDot, queryParams, retCount);
    }


    /**
     * 在线遍历查询所有图层，返回多个结果
     *
     * @param mapView
     * @param mapDot
     * @param retCount
     * @param geometryTypeValue
     * @return
     */
    public static List<Graphic> onlinePointQuery(MapView mapView, Dot mapDot, int retCount, String geometryTypeValue) {

        String layersValue = "";
        List<String> queryParams = onlineQueryParams(mapView, mapDot, layersValue, geometryTypeValue);

        return onlinePointQueryGraphic(mapView, mapDot, queryParams, retCount);
    }


    /**
     * 在线遍历查询传入的图层，返回多个结果
     *
     * @param mapView
     * @param mapDot
     * @param retCount
     * @param layerIds
     * @param geometryTypeValue
     * @return
     */
    public static List<Graphic> onlinePointQuery(MapView mapView, Dot mapDot, int retCount, List<Integer> layerIds, String geometryTypeValue) {
        if (layerIds == null || layerIds.size() == 0) {
            return null;
        }
        String layersValue = "0:" + TextUtils.join(",", layerIds);
        List<String> queryParams = onlineQueryParams(mapView, mapDot, layersValue, geometryTypeValue);

        return onlinePointQueryGraphic(mapView, mapDot, queryParams, retCount);
    }

    /**
     * 子线程中调用 在线点击查询
     *
     * @param mapView
     * @param mapDot
     * @param params
     * @return
     */
    public static List<Graphic> onlinePointQueryGraphic(MapView mapView, Dot mapDot, List<String> params, int retCount) {

        if (mapView == null) {
            return null;
        }
        if (mapDot == null) {
            return null;
        }
        if (params == null || params.size() < 7) {
            return null;
        }

        String result = NetUtil.executeHttpGet(OnlineQueryService.getPointQueryService(), "geometry", params.get(0), "layers",
                params.get(1), "imageDisplay", params.get(2), "mapExtent", params.get(3), "geometryType", params.get(4), "f", params.get(5),
                "tolerance", params.get(6), "returnGeometry", "true", "sr", "1");


        if (TextUtils.isEmpty(result)) {
            return null;
        }

        result = result.replace("\\", "");

        String[] attArrTemp = result.split("attributes");

        OnlineGisData data;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (attArrTemp.length > 1 && attArrTemp[1].indexOf("\"ID") == attArrTemp[1].lastIndexOf("\"ID")) {
            data = gson.fromJson(result, OnlineGisData.class);
        } else {
            result = "";
            for (int i = 0; i < attArrTemp.length; i++) {
                attArrTemp[i] = attArrTemp[i].replaceFirst("\"ID", "\"DUMPLICATE_ID");
                result += attArrTemp[i] + "attributes";
            }
            result = result.substring(0, result.lastIndexOf("attributes"));
            data = gson.fromJson(result, OnlineGisData.class);

        }

        if (data == null || data.results.length == 0) {
            return null;
        }

        return OnlineFeature2GraphicList(data.results);
    }

    private class OnlineGisData {
        public OnlineFeature[] results;
    }

    /**
     * 建议在主线程中调用
     *
     * @param mapView
     * @param mapDot
     * @param layersValue       type:ids
     *                          type:枚举类型
     *                          show,
     *                          hide,
     *                          include,
     *                          exclude,
     *                          visible
     * @param geometryTypeValue
     * @return
     */
    public static List<String> onlineQueryParams(MapView mapView, Dot mapDot, String layersValue, String geometryTypeValue) {
        List<String> params = new ArrayList<>();
        String geometryValue = "{\"x\":" + mapDot.getX() + ",\"y\":" + mapDot.getY()
                + ",\"spatialReference\":{\"wkid\":1}}";

        int dpi = mapView.getResources().getDisplayMetrics().densityDpi;

        //伪造屏幕高宽，适配成96dpi情况下的高宽，以绕过服务端没有识别dpi的漏洞
        int w = (int) (mapView.getWidth() / (dpi / 96.0));
        int h = (int) (mapView.getHeight() / (dpi / 96.0));

        String imageDisplayValue = w + "," + h + "," + 96;


        String mapExtentValue = mapView.getDispRange().toString();

        if (TextUtils.isEmpty(geometryTypeValue)) {
            geometryTypeValue = "esriGeometryPoint";
        }

        String fValue = "json";

        String toleranceValue = "10";

        params.add(geometryValue);
        params.add(layersValue);
        params.add(imageDisplayValue);
        params.add(mapExtentValue);
        params.add(geometryTypeValue);
        params.add(fValue);
        params.add(toleranceValue);

        return params;
    }

    /**
     * 属性条件查询   开始
     **/


    public static List<Graphic> conditionQuery(String layerName, String strWhere) {
        List<Graphic> graphics = new ArrayList<>();

        try {
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

            OnlineLayerInfo layerInfo = MapServiceInfo.getInstance().getLayerByName(layerName);

            if (layerInfo == null) {
                return graphics;
            }

            String layerId = layerInfo.id;

            if (TextUtils.isEmpty(layerId)) {
                return graphics;
            }

            String result = NetUtil.executeHttpGetAppointLastTime(180, OnlineQueryService.getOnlineQueryService(layerId), "ExportFlag", "0",
                    "where", strWhere, "geometry", null, "geometryType", "Envelope", "f", "json", "paging", "all", "returnGeometry", "true");

            if (TextUtils.isEmpty(result)) {
                return graphics;
            }

            result = result.replace("\\", "");

            OnlineQueryResult data = new Gson().fromJson(result, OnlineQueryResult.class);

            if (data == null || data.features == null || data.features.length == 0) {
                return graphics;
            }

            for (OnlineFeature feature : data.features) {
                if (TextUtils.isEmpty(feature.layerName))
                    feature.layerName = layerName;
            }

            return OnlineFeature2GraphicList(data.features);
        } catch (Exception ex) {
            ex.printStackTrace();

            return graphics;
        }
    }

    /**
     * 属性条件查询   开始
     **/

    public static List<Graphic> OnlineFeature2GraphicList(OnlineFeature[] onlineFeatures) {
        List<Graphic> graphics = new ArrayList<>();
        if (onlineFeatures == null) {
            return graphics;
        }
        for (int i = 0; i < onlineFeatures.length; i++) {
            Graphic graphic = GisQueryUtil.onlineFeature2Graphic(onlineFeatures[i]);
            if (graphic == null) {
                continue;
            }
            graphics.add(graphic);
        }
        return graphics;
    }
}
