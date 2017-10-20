package com.mapgis.mmt.common.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Location;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.config.CitySystemConfig;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.module.gis.MmtMapView;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicImage;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.graphic.GraphicText;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;
import com.zondy.mapgis.geometry.GeomType;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.Map;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class GisUtil {
    /**
     * 判断一个Rect的范围是否在另一个Rect的范围内
     *
     * @param smallEnvelope 需要比较的Rect
     * @param largeEnvelope 被比较的Rect
     * @return 如果 smallEnvelope在largeEnvelope的范围内，则返回true，否则返回false
     */

    public static Boolean IsInEnvelope(Rect smallEnvelope, Rect largeEnvelope) {
        if (smallEnvelope == null || largeEnvelope == null) {
            return false;
        } else if (smallEnvelope.getXMin() > smallEnvelope.getXMax()
                || smallEnvelope.getYMin() > smallEnvelope.getYMax()) {
            return false;
        } else {
            return smallEnvelope.getXMin() >= largeEnvelope.getXMin()
                    && smallEnvelope.getXMax() <= largeEnvelope.getXMax()
                    && smallEnvelope.getYMin() >= largeEnvelope.getYMin();
        }
    }

    /**
     * 二维空间点到直线的垂足
     *
     * @param pt    直线外一点
     * @param begin 直线开始点
     * @param end   直线结束点
     * @return 垂足
     */
    public static Dot GetFootOfPerpendicular(Dot pt, Dot begin, Dot end) {
        Dot target = new Dot();

        double dx = begin.x - end.x;
        double dy = begin.y - end.y;

        if (Math.abs(dx) < 0.00000001 && Math.abs(dy) < 0.00000001) {
            target = begin;

            return target;
        }

        double u = (pt.x - begin.x) * (begin.x - end.x) + (pt.y - begin.y)
                * (begin.y - end.y);
        u = u / ((dx * dx) + (dy * dy));

        target.x = begin.x + u * dx;
        target.y = begin.y + u * dy;

        return target;
    }

    /**
     * 获取设备的标示字段
     *
     * @param map 属性集合
     * @return ["图层名称","关联字段名","关联字段值"]
     */
    public static String[] getIdentityField(HashMap<String, String> map) {
        String[] args = {"", "", ""};

        try {
            args[0] = map.get("$图层名称$");

            args[1] = "编号";

            if (map.containsKey(args[1])) {
                args[2] = map.get(args[1]);

                if (!BaseClassUtil.isNullOrEmptyString(args[2])) {
                    return args;
                }
            }

            args[1] = "GUID";

            if (map.containsKey(args[1])) {
                args[2] = map.get(args[1]);

                if (!BaseClassUtil.isNullOrEmptyString(args[2])) {
                    return args;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return args;
    }

    public static String getPlaceField(HashMap<String, String> map) {
        try {
            String place = map.get("位置");

            if (TextUtils.isEmpty(place)) {
                place = map.get("所在位置");
            }

            if (TextUtils.isEmpty(place)) {
                place = map.get("道路名");
            }

            if (TextUtils.isEmpty(place)) {
                place = "";
            }

            return place;
        } catch (Exception ex) {
            ex.printStackTrace();

            return "";
        }
    }

    public static boolean equals(Dot dot1, Dot dot2) {
        if (dot1 != null && dot2 != null) {
            if (dot1.x == dot2.x && dot1.y == dot2.y) {
                return true;
            }
        }
        return false;
    }

    public static boolean equals(Dot dot1, Dot dot2, float tolerance) {
        if (dot1 != null && dot2 != null) {
            if (Math.abs(dot1.x - dot2.x) <= tolerance && Math.abs(dot1.y - dot2.y) <= tolerance) {
                return true;
            }
        }
        return false;
    }

    public static Rect showLine(MapView mapView, Dots dots) {
        if (dots != null && dots.size() <= 10)
            return showLine(mapView, true, dots);
        else
            return showLine(mapView, false, dots);
    }

    public static Rect showLine(MapView mapView, boolean showDistance, Dots dots) {
        if (dots == null || dots.size() == 0)
            return null;

        Resources resources = mapView.getContext().getResources();

        GraphicImage startImage = new GraphicImage(dots.get(0), BitmapFactory.decodeResource(resources, R.drawable.icon_track_navi_end));
        startImage.setAnchorPoint(new PointF(0.5f, 0f));

        mapView.getGraphicLayer().addGraphic(startImage);

        if (dots.size() < 2)
            return null;

        GraphicPolylin polylin = new GraphicPolylin(dots);

        polylin.setColor(resources.getColor(R.color.darkviolet));
        polylin.setLineWidth(6);

        mapView.getGraphicLayer().addGraphic(polylin);

        if (showDistance) {
            double distance = 0;

            for (int i = 0; i < dots.size() - 1; i++) {
                distance += Math.sqrt(Math.pow((dots.get(i + 1).x - dots.get(i).x), 2) + Math.pow((dots.get(i + 1).y - dots.get(i).y), 2));
                DecimalFormat format = new DecimalFormat("0.0");
                String dis;

                if (distance >= 1000) {
                    dis = format.format(distance / 1000) + "公里";
                } else {
                    dis = (int) distance + "米";
                }

                GraphicText text = new GraphicText(dots.get(i + 1), dis);
                text.setAnchorPoint(new PointF(0f, 0.5f));
                text.setFontSize(30);

                mapView.getGraphicLayer().addGraphic(text);
            }
        }

        GraphicImage endImage = new GraphicImage(dots.get(dots.size() - 1), BitmapFactory.decodeResource(resources, R.drawable.icon_track_navi_start));
        endImage.setAnchorPoint(new PointF(0.5f, 0f));

        mapView.getGraphicLayer().addGraphic(endImage);

        return polylin.getBoundingRect();
    }

    public static Dot[] dots2Array(Dots dots) {
        if (dots == null || dots.size() == 0)
            return null;

        Dot[] dotArray = new Dot[dots.size()];

        for (int i = 0; i < dotArray.length; i++)
            dotArray[i] = dots.get(i);

        return dotArray;
    }

    public static double calcDistance(Dot d1, Dot d2) {
        return Math.sqrt(Math.pow(d1.x - d2.x, 2) + Math.pow(d1.y - d2.y, 2));
    }

    public static Rect getEntireRange() {
        Rect rect = null;
        try {
            MmtMapView mapView = (MmtMapView) MyApplication.getInstance().mapGISFrame.getMapView();
            rect = mapView.getEntireRange();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rect;
    }

    /**
     * 判断点是否在矩形内
     *
     * @param rect      矩形范围
     * @param dot       目标点
     * @param tolerance 容差半径，单位米
     * @return 是否在矩形内
     */
    public static boolean isInRect(Rect rect, Dot dot, double tolerance) {
        return rect != null && dot.x >= rect.xMin - tolerance && dot.x <= rect.xMax + tolerance
                && dot.y >= rect.yMin - tolerance && dot.y <= rect.yMax + tolerance;
    }

    public static boolean isInRect(Rect rect, Dot dot) {
        return isInRect(rect, dot, 0);
    }

    public static boolean isInRect(Rect rect, String dot) {
        return isInRect(rect, convertDot(dot), 0);
    }

    /**
     * 获取rect较长边的一半
     *
     * @param rect
     * @return
     */
    public static double getMaxRadius(Rect rect) {
        if (rect == null) {
            return 0;
        }
        double maxr = Math.max(rect.getXMax() - rect.getXMin(), rect.getYMax() - rect.getYMin());
        if (maxr > 0) {
            return maxr / 2.0;
        }
        return 0;
    }

    /**
     * 取两个Rect的交集
     *
     * @param mapRect
     * @param dispRect
     * @return
     */
    public static Rect getMixRect(Rect mapRect, Rect dispRect) {
        if (mapRect == null || dispRect == null) {
            new Rect(0, 0, 0, 0);
        }

        double xmin = 0, xmax = 0, ymin = 0, ymax = 0;
        if (mapRect.getXMin() >= dispRect.getXMin()) {
            xmin = mapRect.getXMin();
        } else {
            xmin = dispRect.getXMin();
        }

        if (mapRect.getXMax() >= dispRect.getXMax()) {
            xmax = dispRect.getXMax();
        } else {
            xmax = mapRect.getXMax();
        }

        if (mapRect.getYMin() >= dispRect.getYMin()) {
            ymin = mapRect.getYMin();
        } else {
            ymin = dispRect.getYMin();
        }

        if (mapRect.getYMax() >= dispRect.getYMax()) {
            ymax = dispRect.getYMax();
        } else {
            ymax = mapRect.getYMax();
        }
        //只有这种情况才有交集
        if (xmax >= xmin && ymax >= ymin) {
            return new Rect(xmin, ymin, xmax, ymax);
        }

        return new Rect(0, 0, 0, 0);
    }

    public static Dot convertDot(String xy) {
        try {
            String[] xys = xy.split(",");

            return new Dot(Double.valueOf(xys[0]), Double.valueOf(xys[1]));
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        }
    }

    /**
     * Call native baidumap app.
     *
     * @param context The current context.
     * @param destDot The plane coordinate of destination.
     */
    public static void callOuterNavigationApp(Context context, Dot destDot) {

        if (destDot == null) {
            MyApplication.getInstance().showMessageWithHandle("坐标无效，无法导航");
            return;
        }

        // Get current geographical coordinate.
        Location startLoc = GpsReceiver.getInstance().getLastLocation();
        if (startLoc == null) {
            MyApplication.getInstance().showMessageWithHandle("定位失败，无法导航");
            return;
        }

        // Convert plane coordinate to geographical coordinate.
        Location destLoc = GpsReceiver.getInstance()
                .getLastLocationConverse(new GpsXYZ(destDot.x, destDot.y));
        if (destLoc == null) {
            MyApplication.getInstance().showMessageWithHandle("目的地坐标转换失败，无法导航");
            return;
        }

        GisUtil.callOuterNavigationApp(context, startLoc, destLoc, "当前位置", "目的地");
    }

    /**
     * Call native baidumap app.
     * <p>The format of coordinate should be WGS-84. </p>
     *
     * @param context     The current context.
     * @param origin      The coordinate of origin.
     * @param destination The coordinate of destination.
     */
    public static void callOuterNavigationApp(Context context, Location origin, Location destination,
                                              String originAddress, String destAddress) {

        callOuterNavigationApp(context, origin.getLatitude(), origin.getLongitude(),
                destination.getLatitude(), destination.getLongitude(), originAddress, destAddress);
    }

    /**
     * The format of coordinate should be WGS-84.
     * <p>For now, just support Baidu map app.</p>
     *
     * @param context  The current context.
     * @param slat     The latitude of origin.
     * @param slng     The longitude of origin.
     * @param dlat     The latitude of destination.
     * @param dlng     The longitude of destination.
     * @param sAddress The name of the origin.
     * @param dAddress The name of the destination.
     */
    public static void callOuterNavigationApp(final Context context, double slat, double slng,
                                              double dlat, double dlng, String sAddress, String dAddress) {

        if (context == null) {
            throw new NullPointerException("context == null.");
        }
        if (TextUtils.isEmpty(sAddress)) {
            sAddress = "起点";
        }
        if (TextUtils.isEmpty(dAddress)) {
            dAddress = "终点";
        }

        Intent callIntent = null;
        final PackageManager packageManager = context.getPackageManager();

        String locateParamsStr = "origin=latlng:%.13f,%.13f|name:%s&destination=latlng:%.13f,%.13f|name:%s&coord_type=wgs84&mode=driving&src=yourCompanyName|RoutePlan#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end";
        try {
            callIntent = Intent.parseUri("intent://map/direction?" + String.format(locateParamsStr, slat, slng, sAddress, dlat, dlng, dAddress), 0);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            callIntent = null;
        }

        if (callIntent != null) {

            if (callIntent.resolveActivity(packageManager) != null) {
                context.startActivity(callIntent);
                return;
            }

            final Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://search?q=pname=com.baidu.BaiduMap"));
            if (marketIntent.resolveActivity(packageManager) != null) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("提示");
                builder.setMessage("您的手机未安装百度地图，是否进入应用市场下载？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        context.startActivity(marketIntent);
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();

            } else {
                Toast.makeText(context, "请先安装百度地图！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 将,(14325423,21331541)分割的字符串数组建立为Dot对象
     */
    public static Dots buildDots(String[] lineStrings) {
        Dots dots = new Dots();

        for (String line : lineStrings) {
            String[] xy = line.split(",");

            dots.append(new Dot(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
        }

        return dots;
    }

    public static String[] getGISFields(String layerName) {
        if (BaseClassUtil.isNullOrEmptyString(layerName)) {
            return null;
        }

        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            if (MobileConfig.MapConfigInstance == null) {
                return null;
            }

            if (!GisQueryUtil.isofflineMap()) {
                return null;
            }

            String name = MobileConfig.MapConfigInstance.getMapType(MapConfig.MOBILE_EMS).Name;

            String path = MyApplication.getInstance().getMapFilePath() + name + "/" + name + ".db";

            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

            cursor = database.rawQuery("SELECT * FROM " + layerName + " WHERE 1=-1", null);

            return cursor.getColumnNames();
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }

    /**
     * 采用离线地图时，对巡检计划的设备进行条件查询，以获取设备详情信息
     */
    public static LinkedHashMap<String, String> offlinePipeQuery(MapView mapView, String layerName, String fieldName, String fieldValue) {
        LinkedHashMap<String, String> graphicMap = new LinkedHashMap<>();

        if (!BaseClassUtil.isNullOrEmptyString(layerName) && !BaseClassUtil.isNullOrEmptyString(fieldValue)) {
            LayerEnum layerEnum = mapView.getMap().getLayerEnum();
            layerEnum.moveToFirst();
            MapLayer layer;

            while ((layer = layerEnum.next()) != null) {
                if (!(layer instanceof VectorLayer) || !layer.getName().equals(layerName)) {
                    continue;
                }

                try {
                    FeaturePagedResult featurePagedResult = FeatureQuery.query((VectorLayer) layer, fieldName + " like '" + fieldValue + "'",
                            null, FeatureQuery.SPATIAL_REL_OVERLAP, true, true, "", 1);

                    if (featurePagedResult != null && featurePagedResult.getTotalFeatureCount() > 0) {
                        Graphic graphic = Convert.fromFeatureToGraphic(featurePagedResult.getPage(1).get(0));

                        for (int m = 0; m < graphic.getAttributeNum(); m++) {
                            graphicMap.put(graphic.getAttributeName(m), graphic.getAttributeValue(m));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                break;
            }
        }

        return graphicMap;
    }

    public static List<MapLayer> getVisibleVectorLayerNames(MapView mapView) {
        List<MapLayer> mapLayers = new ArrayList<MapLayer>();

        Map map = mapView.getMap();
        if (map == null) {
            return mapLayers;
        }
        LayerEnum layerEnum = map.getLayerEnum();
        if (layerEnum == null) {
            return mapLayers;
        }
        layerEnum.moveToFirst();
        MapLayer layer;

        List<MapLayer> mapPointLayers = new ArrayList<MapLayer>();
        List<MapLayer> mapLineLayers = new ArrayList<MapLayer>();

        while ((layer = layerEnum.next()) != null) {

            if (!(layer instanceof VectorLayer && layer.getIsVisible() && LayerConfig.getInstance().getConfigInfo(layer.getName()).IsEquipment)) {
                continue;
            }

            if (layer.GetGeometryType().value() == GeomType.GeomLin.value()) {
                mapLineLayers.add(layer);
            }
            if (layer.GetGeometryType().value() == GeomType.GeomPnt.value()) {
                mapPointLayers.add(layer);
            }

        }
        if (mapPointLayers.size() > 0) {
            mapLayers.addAll(mapPointLayers);
        }
        if (mapLineLayers.size() > 0) {
            mapLayers.addAll(mapLineLayers);
        }

        return mapLayers;
    }

    /**
     * 获取离线地图点击查询的所有图层
     * 先存储管点在存储管线
     *
     * @param mapView
     * @return
     */
    public static List<MapLayer> getPointQueryVectorLayer(MapView mapView) {
        List<MapLayer> mapLayers = new ArrayList<MapLayer>();
        Map map = mapView.getMap();
        if (map == null) {
            return mapLayers;
        }
        String pointQueryLayerFilter = MyApplication.getInstance().getConfigValue("PointQueryLayerFilter");

        if (BaseClassUtil.isNullOrEmptyString(pointQueryLayerFilter))
            return getVisibleVectorLayerNames(mapView);

        LayerEnum layerEnum = map.getLayerEnum();
        if (layerEnum == null) {
            return mapLayers;
        }
        layerEnum.moveToFirst();
        MapLayer layer;

        List<MapLayer> mapPointLayers = new ArrayList<MapLayer>();
        List<MapLayer> mapLineLayers = new ArrayList<MapLayer>();
        List<String> pointQueryLayerFilterList = Arrays.asList(pointQueryLayerFilter.split("\\|"));

        while ((layer = layerEnum.next()) != null) {

            if (!(layer instanceof VectorLayer && layer.getIsVisible() && LayerConfig.getInstance().getConfigInfo(layer.getName()).IsEquipment)) {
                continue;
            }
            if (pointQueryLayerFilterList.contains(layer.getName())) {
                continue;
            }
            if (layer.GetGeometryType().value() == GeomType.GeomLin.value()) {
                mapLineLayers.add(layer);
            }
            if (layer.GetGeometryType().value() == GeomType.GeomPnt.value()) {
                mapPointLayers.add(layer);
            }

        }
        if (mapPointLayers.size() > 0) {
            mapLayers.addAll(mapPointLayers);
        }
        if (mapLineLayers.size() > 0) {
            mapLayers.addAll(mapLineLayers);
        }
        return mapLayers;
    }

    /**
     * 生成指定图层名的 MapLayer
     *
     * @param mapView
     * @param layerName
     * @return
     */
    public static MapLayer getPointQueryVectorLayer(MapView mapView, String layerName) {
        Map map = mapView.getMap();
        if (map == null) {
            return null;
        }
        LayerEnum layerEnum = map.getLayerEnum();
        layerEnum.moveToFirst();
        MapLayer layer;
        while ((layer = layerEnum.next()) != null) {
            if (layerName.equals(layer.getName())) {
                return layer;
            }
        }
        return null;
    }

    /**
     * 点击查询
     * 离线地图中查询10个像素范围内某个图层的第一个gis设备
     *
     * @param mapView
     * @param mapDot
     * @param layer
     * @return
     */
    public static Graphic pointQuerySingle(MapView mapView, Dot mapDot, MapLayer layer) {
        double temp = mapView.getResolution(mapView.getZoom()) * 10;
        Rect rect = new Rect();
        rect.setXMin(mapDot.x - temp);
        rect.setYMin(mapDot.y - temp);
        rect.setXMax(mapDot.x + temp);
        rect.setYMax(mapDot.y + temp);
        List<Graphic> graphics = queryAreaGraphicByMapLayer(mapView, mapDot, layer, rect, 1);
        return (graphics == null || graphics.size() == 0) ? null : graphics.get(0);
    }

    /**
     * 范围查询
     * 离线地图中查询某个范围内某个图层第一页的pagesize个gis设备
     *
     * @param mapView
     * @param mapDot
     * @param layer
     * @param rect
     * @param pageSize
     * @return
     */
    public static List<Graphic> queryAreaGraphicByMapLayer(MapView mapView, Dot mapDot, MapLayer layer, Rect rect, int pageSize) {

        FeaturePagedResult featurePagedResult = FeatureQuery.query((VectorLayer) layer, "", new FeatureQuery.QueryBound(
                rect), FeatureQuery.SPATIAL_REL_MBROVERLAP, true, true, "", pageSize);

        if (featurePagedResult.getTotalFeatureCount() > 0) {
            return Convert.fromFeaturesToGraphics(featurePagedResult.getPage(1), layer.getName());
        }
        return null;
    }


    /**
     * 获取离线地图的最后更改时间
     *
     * @param mapName
     * @return
     */
    public static String getMapLastUpdateTime(String mapName) {
        // 若矢量图需要更新，则查询Sqlite中存储的该名称的矢量图最近修改时间
        // 若未查询到本地存储的地图的最近修改时间，优先读取本地DB文件最后修改时间，否则将默认的时间插入到数据库
        String nativeTime = "1900-01-01 00:00:01";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<CitySystemConfig> queryResult = DatabaseHelper.getInstance().query(CitySystemConfig.class,
                "ConfigKey = '" + mapName + "ModifyTime'");

        if (queryResult != null && queryResult.size() > 0) {
            nativeTime = queryResult.get(0).ConfigValue;
        } else {
            File file = new File(MyApplication.getInstance().getMapFilePath() + mapName + "/" + mapName + ".db");

            if (file.exists()) {
                nativeTime = format.format(new Date(file.lastModified()));
            }

            CitySystemConfig configValue = new CitySystemConfig(mapName + "ModifyTime", nativeTime, MyApplication.getInstance()
                    .getUserId());

            DatabaseHelper.getInstance().insert(configValue);
        }
        return nativeTime;
    }

    /**
     * 获取格式化的区域
     *
     * @param dots
     * @return
     */
    public static String getFormatAreaByDots(Dots dots) {
        JSONObject areaobject = new JSONObject();
        try {
            JSONArray jos = new JSONArray();
            for (int i = 0; i < dots.size(); i++) {
                JSONArray jo = new JSONArray();
                jo.put(dots.get(i).getX());
                jo.put(dots.get(i).getY());
                jos.put(jo);
            }
            JSONArray joParent = new JSONArray();
            joParent.put(jos);
            areaobject.put("rings", joParent);

        } catch (Exception ex) {
            MyApplication.getInstance().showMessageWithHandle(ex.getMessage());
        } finally {
            return areaobject.toString();
        }
    }

    /**
     * 获取区域的外包矩形
     */
    public static Rect getAreaCoverRect(String area) {

        Rect rect = null;

        if (BaseClassUtil.isNullOrEmptyString(area)) {
            return rect;
        }

        double minX = 0, maxX = 0, minY = 0, maxY = 0;

        try {
            JSONObject jsonObject = new JSONObject(area);
            JSONArray jsonArray = jsonObject.getJSONArray("rings").getJSONArray(0);

            String dotStr;
            boolean initialized = false;
            for (int i = 0, length = jsonArray.length(); i < length; i++) {
                dotStr = jsonArray.getString(i);
                if (!BaseClassUtil.isNullOrEmptyString(dotStr)) {

                    String[] xy = dotStr.split(",");

                    double x = Double.valueOf(xy[0]);
                    double y = Double.valueOf(xy[1]);

                    if (!initialized) {
                        initialized = true;
                        minX = maxX = x;
                        minY = maxY = y;
                    }

                    if (x < minX) minX = x;
                    else if (x > maxX) maxX = x;

                    if (y < minY) minY = y;
                    else if (y > maxY) maxY = y;
                }
            }
            if (minX != maxX && minY != maxY) {
                rect = new Rect(minX, minY, maxX, maxY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rect;
    }

    public static String getMinMaxAreaVal(String areaControlVal) {
        return "";
    }

    public static void removeAnnotationByType(MapView mapView, int type) {
        List<Annotation> removedAnnotations = new ArrayList<>();

        for (Annotation annotation : mapView.getAnnotationLayer().getAllAnnotations()) {
            if (annotation instanceof MmtAnnotation && ((MmtAnnotation) annotation).Type == type)
                removedAnnotations.add(annotation);
        }

        if (removedAnnotations.size() > 0)
            mapView.getAnnotationLayer().removeAnnotations(removedAnnotations);
    }

    public static Rect convert2Rect(String dots) {
        try {
            String[] args = dots.split(",");

            return new Rect(Double.parseDouble(args[0]), Double.parseDouble(args[1]),
                    Double.parseDouble(args[2]), Double.parseDouble(args[3]));
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        }
    }
}
