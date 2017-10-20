package com.repair.gisdatagather.common.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.DeviceUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.MmtMapView;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.R;
import com.repair.gisdatagather.common.entity.GISDataBeanBase;
import com.repair.gisdatagather.common.entity.GISDataProject;
import com.repair.gisdatagather.common.entity.TextDot;
import com.repair.gisdatagather.common.entity.TextDotState;
import com.repair.gisdatagather.common.entity.TextLine;
import com.repair.gisdatagather.common.entity.TextLineState;
import com.repair.gisdatagather.common.entity.TodayGISData;
import com.repair.gisdatagather.enn.bean.GISDeviceSetBean;
import com.repair.gisdatagather.product.editdata.EditDataActivity;
import com.repair.gisdatagather.product.gisgather.GisGather;
import com.repair.gisdatagather.product.gisgather.GisGatherMobile;
import com.repair.gisdatagather.product.gisgather.GisGatherPad;
import com.repair.zhoushan.entity.FlowCenterData;
import com.simplecache.ACache;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.GeomType;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuyunfan on 2016/1/14.
 */
public class GisDataGatherUtils {

    public static class GisDataFrom {
        //当前工程非今日采集的数据
        public static int currentProject = 0;
        //当前工程今日采集的数据
        public static int todayProject = 1;
        //非当前工程的数据
        public static int mapNExist = 2;
    }

    private static ACache mCache;
    // static long zoomNum = MyApplication.getInstance().getConfigValue("MyPlanDetailLevel", 7);

    public static HashMap<String, String> str2HashMap(String attrs) {
        HashMap<String, String> hashMap = new HashMap<>();
        if (!TextUtils.isEmpty(attrs)) {
            String[] attrArr = GisDataGatherUtils.splitGisVal(attrs);

            hashMap.putAll(GisDataGatherUtils.gisValsMap(attrArr, false));

        }
        return hashMap;
    }

    public static int getColorByGIsDataFrom(int from, int state) {
        int color;
        switch (from) {
            case 1: {
                color = Color.parseColor("#f87f39");
            }
            break;
            case 0: {
                color = Color.parseColor("#a22680");
                if (state == 1) {
                    color = Color.parseColor("#c1ae0c");
                }
            }
            break;
            default: {
                color = Color.parseColor("#f87f39");
            }
        }
        return color;
    }

    private static List<String> allEditPointLayer;
    private static List<String> allEditLineLayer;

    public static List<String> getAllEditLayer(List<GISDeviceSetBean> gisDeviceSetBeans, int type) {
        if (type == 1) {
            if (allEditPointLayer == null) {
                allEditPointLayer = new ArrayList<>();
                if (gisDeviceSetBeans != null) {
                    for (GISDeviceSetBean gisDeviceSetBean : gisDeviceSetBeans) {
                        if (gisDeviceSetBean.layerType == type) {
                            allEditPointLayer.add(gisDeviceSetBean.layerName);
                        }
                    }
                }
            }
            return allEditPointLayer;
        } else {
            if (allEditLineLayer == null) {
                allEditLineLayer = new ArrayList<>();
                if (gisDeviceSetBeans != null) {
                    for (GISDeviceSetBean gisDeviceSetBean : gisDeviceSetBeans) {
                        if (gisDeviceSetBean.layerType == type) {
                            allEditLineLayer.add(gisDeviceSetBean.layerName);
                        }
                    }
                }
            }
            return allEditLineLayer;
        }
    }

//    public static int hasContaionTextDot(List<TextDot> textDots, TextDot textDot) {
//        try {
//            if (textDots.size() == 0) {
//                return -1;
//            }
//            for (int i = 0; i < textDots.size(); i++) {
//                TextDot temp = textDots.get(i);
//                if (temp.dot.toString().equals(textDot.dot.toString())) {
//                    return i;
//                }
//            }
//            return -1;
//        } catch (Exception ex) {
//            return -1;
//        }
//    }

    public static int hasContaionDot(List<TextDot> textDots, Dot dot) {
        try {
            if (textDots == null || textDots.size() == 0) {
                return -1;
            }
            if (dot == null) {
                return -1;
            }
            for (int i = 0; i < textDots.size(); i++) {

                if (GisUtil.equals(textDots.get(i).dot, dot, 0.001f)) {
                    return i;
                }

            }
            return -1;
        } catch (Exception ex) {
            return -1;
        }
    }

    public static TextDot findTextDotInTextDots(List<TextDot> textDots, Dot dot) {
        try {
            if (textDots.size() == 0) {
                return null;
            }
            for (int i = 0; i < textDots.size(); i++) {
                TextDot temp = textDots.get(i);

                if (GisUtil.equals(temp.dot, dot, 0.001f)) {
                    return temp;
                }
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

//    public static TextLine findTextLineInTextLines(List<TextLine> textLines, List<Dot> dots) {
//        try {
//            if (textLines.size() == 0) {
//                return null;
//            }
//            if (dots.size() != 2) {
//                return null;
//            }
//            for (int i = 0; i < textLines.size(); i++) {
//                TextLine temp = textLines.get(i);
//                if (hasContaionDot(temp.dots, dots.get(0)) == -1) {
//                    continue;
//                }
//                if (hasContaionDot(temp.dots, dots.get(1)) == -1) {
//                    continue;
//                }
//                return temp;
//            }
//            return null;
//        } catch (Exception ex) {
//            return null;
//        }
//    }

//    public static int hasContaionTextLine(List<TextLine> textLines, TextLine textLine) {
//        try {
//            if (textLines.size() == 0) {
//                return -1;
//            }
//            for (int i = 0; i < textLines.size(); i++) {
//                TextLine temp = textLines.get(i);
//                if (hasContaionDot(temp.dots, textLine.dots.get(0).dot) == -1) {
//                    continue;
//                }
//                if (hasContaionDot(temp.dots, textLine.dots.get(1).dot) == -1) {
//                    continue;
//                }
//                return i;
//            }
//            return -1;
//        } catch (Exception ex) {
//            return -1;
//        }
//    }

    public static int hasContaionLine(List<TextLine> textLines, GraphicPolylin graphicPolylin) {
        try {
            if (textLines == null || textLines.size() == 0) {
                return -1;
            }
            if (graphicPolylin == null) {
                return -1;
            }

            //很奇怪，一条线还有个0，0点
            if (graphicPolylin.getPointSize() < 2) {
                return -1;
            }

            for (int i = 0; i < textLines.size(); i++) {
                if (hasContaionDot(textLines.get(i).dots, graphicPolylin.getPoint(0)) == -1) {
                    continue;
                }
                if (hasContaionDot(textLines.get(i).dots, graphicPolylin.getPoint(1)) == -1) {
                    continue;
                }
                return i;
            }
            return -1;
        } catch (Exception ex) {
            return -1;
        }
    }

    public static int hasContaionTextLine(List<TextLine> textLines, Dot dot1, Dot dot2) {
        try {
            if (textLines == null || textLines.size() == 0) {
                return -1;
            }
            if (dot1 == null || dot2 == null) {
                return -1;
            }

            for (int i = 0; i < textLines.size(); i++) {
                if (hasContaionDot(textLines.get(i).dots, dot1) == -1) {
                    continue;
                }
                if (hasContaionDot(textLines.get(i).dots, dot2) == -1) {
                    continue;
                }
                return i;
            }
            return -1;
        } catch (Exception ex) {
            return -1;
        }
    }

    public static ACache getMCache(BaseActivity context) {
        if (mCache == null) {
            mCache = BaseClassUtil.getACache();
        }
        return mCache;
    }

    private static FlowCenterData flowCenterData;

    public static FlowCenterData getFlowCenterData() {
        if (flowCenterData == null) {
            flowCenterData = new FlowCenterData();
            flowCenterData.BizCode = "GIS";
            flowCenterData.BusinessType = "GIS属性上报";
            flowCenterData.EventName = "GIS属性上报";
            flowCenterData.FieldGroup = ",现场图片,";
            flowCenterData.FlowName = "GIS属性更新流程";
            flowCenterData.HandoverMode = "移交默认人";
            flowCenterData.TableName = "CIV_PATROL_ATT_PROJECT";
            flowCenterData.OperType = "上报";
            flowCenterData.NodeName = "GIS属性上报";
            flowCenterData.IsCreate = 1;
        }
        return flowCenterData;
    }

    public static FlowCenterData getFlowCenterDataForProduct() {
        if (flowCenterData == null) {
            flowCenterData = new FlowCenterData();
            flowCenterData.BizCode = "GW";
            flowCenterData.BusinessType = "管网采集";
            flowCenterData.EventName = "管网采集";
            flowCenterData.FieldGroup = "";
            flowCenterData.FlowName = "管网采集";
            flowCenterData.HandoverMode = "自处理";
            flowCenterData.TableName = "CIV_PATROL_ATT_PROJECT";
            flowCenterData.OperType = "上报";
            flowCenterData.NodeName = "新建工程";
            flowCenterData.IsCreate = 1;
        }
        return flowCenterData;
    }

    public static void putAttr2Graphic(Graphic graphic, GISDataBeanBase gisDataBeanBase) {
        if (gisDataBeanBase == null) {
            return;
        }
        if (TextUtils.isEmpty(gisDataBeanBase.NewAtt)) {
            return;
        }

        String[] attrs = splitGisVal(gisDataBeanBase.NewAtt);

        for (String kv : attrs) {
            String[] kvAttr = kv.split(":");
            if (kvAttr.length != 2) {
                continue;
            }
            if (TextUtils.isEmpty(kvAttr[1])) {
                continue;
            }
            graphic.setAttributeValue(kvAttr[0], kvAttr[1]);
        }
    }


//    /**
//     * 为了提高查询效率规定按一下优先级查询：
//     * 1.今日工程 2.当前工程的，3.地图上没有（初始化时加上去的）4 地图上已有的
//     * 说明：只在编辑和判断是否有点时使用，当查询的是当前工程，Operation一定是新增,，其他来源都是编辑
//     *
//     * @additionalParas mapView
//     * @additionalParas queryLayers
//     * @additionalParas dot
//     * @additionalParas pointF
//     * @additionalParas froms
//     * @additionalParas textDots
//     * @return
//     */
//    public static TextDot hasTextDot(MmtMapView mapView, List<String> queryLayers, Dot dot, PointF pointF, int[] froms, List<TextDot> textDots) {
//
//        Graphic graphic;
//
//        for (int from : froms) {
//            if (from != GisDataFrom.mapExist) {
//                graphic = GisDataGatherUtils.getCustormGraphic(mapView, pointF, queryLayers, "flag", from + "-" + "Point", false);
//                if (graphic != null) {
//
//                    boolean isCur = (from == GisDataFrom.currentProject || from == GisDataFrom.todayProject);
//                    //需要获取真实的管点（需要管点的本点号生成管线的本点号和上点号）
//                    if (isCur) {
//                        return findTextDotInTextDots(textDots, graphic.getCenterPoint());
//                    }
//                    GISDataBeanBase gisDataBeanBase = new GISDataBeanBase(graphic, "编辑", graphic.getAttributeValue("$图层名称$"), "管点");
//                    return new TextDot(graphic, gisDataBeanBase, from, graphic.getCenterPoint(), TextDotState.EDIT.getState());
//                }
//
//            } else {
//                graphic = GisDataGatherUtils.searchTargetGeomLayer(mapView, queryLayers, dot, GeomType.GeomPnt);
//                if (graphic != null) {
//                    GISDataBeanBase gisDataBeanBase = new GISDataBeanBase(graphic, "编辑", graphic.getAttributeValue("$图层名称$"), "管点");
//                    return new TextDot(graphic, gisDataBeanBase, from, graphic.getCenterPoint(), TextDotState.EDIT.getState());
//                }
//            }
//        }
//        return null;
//    }

//    /**
//     * 精准判断是否当前选择的位置有没有管点
//     * 添点时使用
//     *
//     * @additionalParas mapView
//     * @additionalParas queryLayers
//     * @additionalParas dot
//     * @additionalParas pointF
//     * @additionalParas froms
//     * @return
//     */
//    public static boolean hasTextDot(MmtMapView mapView, List<String> queryLayers, Dot dot, PointF pointF, int[] froms) {
//
//        Graphic graphic;
//
//        for (int from : froms) {
//            if (from != GisDataFrom.mapExist) {
//                graphic = GisDataGatherUtils.getCustormGraphic(mapView, pointF, queryLayers, "flag", from + "-" + "Point", true);
//                if (graphic != null) {
//                    return true;
//                }
//
//            } else {
//                graphic = GisDataGatherUtils.searchTargetGeomLayer(mapView, queryLayers, dot, GeomType.GeomPnt);
//                if (graphic != null) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

//    //为了提高查询效率规定按一下优先级查询：
//    //1.当前工程的 2.今日工程，3.地图上没有（初始化时加上去的）4 地图上已有的
//    public static TextLine hasTextLine(MmtMapView mapView, List<String> queryLayers, PointF pointF, int[] froms, List<TextLine> textLines) {
//        Graphic graphic;
//        for (int from : froms) {
//            if (from != GisDataFrom.mapExist) {
//                graphic = GisDataGatherUtils.getCustormGraphic(mapView, pointF, queryLayers, "flag", from + "-" + "Line", false);
//                if (graphic != null) {
//                    boolean isCur = (from == GisDataFrom.currentProject || from == GisDataFrom.todayProject);
//                    //需要获取真实的管线（删除需要gisDataBeanBase的ID）
//                    if (isCur) {
//                        if (graphic instanceof GraphicPolylin) {
//                            final GraphicPolylin polylin = ((GraphicPolylin) graphic);
//
//                            List<Dot> dots = new ArrayList<Dot>() {{
//                                add(polylin.getPoint(0));
//                                add(polylin.getPoint(1));
//                            }};
//                            return findTextLineInTextLines(textLines, dots);
//                        }
//                    } else {
//
//                        GISDataBeanBase gisDataBeanBase = new GISDataBeanBase(graphic, "编辑", graphic.getAttributeValue("$图层名称$"), "管线");
//                        return new TextLine(mapView.getContext(), graphic, gisDataBeanBase, from, TextLineState.EDIT.getState());
//                    }
//                }
//            } else {
//                Dot dot = mapView.viewPointToMapPoint(pointF);
//                graphic = GisDataGatherUtils.searchTargetGeomLayer(mapView, queryLayers, dot, GeomType.GeomLin);
//                if (graphic != null) {
//                    GISDataBeanBase gisDataBeanBase = new GISDataBeanBase(graphic, "编辑", graphic.getAttributeValue("$图层名称$"), "管线");
//                    return new TextLine(mapView.getContext(), graphic, gisDataBeanBase, from, TextLineState.EDIT.getState());
//                }
//            }
//        }
//        return null;
//    }

//    public static Graphic getCustormGraphic(MmtMapView mapView, PointF pointF, List<String> queryLayers, String attrName, String attrValue, int flag) {
//
//        for (Graphic graphic : mapView.getGraphicLayer().getGraphicsByAttribute(attrName, attrValue)) {
//            if (queryLayers != null && !queryLayers.contains(graphic.getAttributeValue("$图层名称$"))) {
//                continue;
//            }
//            //flag=1 为添点,需要精度较高，不能采用自定义的graphicHitTest查询
//            if (flag == 1) {
//                if (mapView.superGraphicHitTest(graphic, pointF.x, pointF.y)) {
//                    return graphic;
//                }
//            } else {
//                if (mapView.graphicHitTest(graphic, pointF.x, pointF.y)) {
//                    return graphic;
//                }
//            }
//        }
//        return null;
//    }

    /**
     * 使用该方法查询自定义graphic,graphic必须有$图层名称$ 属性
     *
     * @param mapView
     * @param pointF
     * @param queryLayers
     * @param attrName
     * @param attrValue
     * @param isExact
     * @return
     */
    public static Graphic getCustormGraphic(MmtMapView mapView, PointF pointF, List<String> queryLayers, String attrName, String attrValue, boolean isExact) {

        for (Graphic graphic : mapView.getGraphicLayer().getGraphicsByAttribute(attrName, attrValue)) {
            String layerName = graphic.getAttributeValue("$图层名称$");
            if (TextUtils.isEmpty(layerName)) {
                continue;
            }
            if (queryLayers != null && !queryLayers.contains(layerName)) {
                continue;
            }
            //精准查询，不能采用自定义的graphicHitTest查询
            if (isExact) {
                if (mapView.superGraphicHitTest(graphic, pointF.x, pointF.y)) {
                    return graphic;
                }
            } else {
                if (mapView.graphicHitTest(graphic, pointF.x, pointF.y)) {
                    return graphic;
                }
            }
        }
        return null;
    }

    public static Graphic searchTargetGeomLayer(MapView mapView, List<String> visibleVectorLayerNames, Dot mapDot, GeomType geomType) {

        try {
            MapLayer layer;

            LayerEnum layerEnum = mapView.getMap().getLayerEnum();
            layerEnum.moveToFirst();

            while ((layer = layerEnum.next()) != null) {
                if (!visibleVectorLayerNames.contains(layer.getName()) || !layer.GetGeometryType().equals(geomType)) {
                    continue;
                }

                Rect rect = new Rect();
                double temp = mapView.getResolution(mapView.getZoom()) * 10;
                rect.setXMin(mapDot.x - temp);
                rect.setYMin(mapDot.y - temp);
                rect.setXMax(mapDot.x + temp);
                rect.setYMax(mapDot.y + temp);

                FeaturePagedResult featurePagedResult = FeatureQuery.query((VectorLayer) layer, "", new FeatureQuery.QueryBound(
                        rect), FeatureQuery.SPATIAL_REL_MBROVERLAP, true, true, "", 1);

                if (featurePagedResult.getTotalFeatureCount() > 0) {
                    return Convert.fromFeaturesToGraphics(featurePagedResult.getPage(1), layer.getName()).get(0);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return null;
    }


    public static Rect getRectFromTextLines(List<TextLine> textLines) {
        if (textLines == null || textLines.size() == 0) {
            return null;
        }
        TextDot textDotFirst = textLines.get(0).dots.get(0);
        double xMin = textDotFirst.dot.getX(), yMin = textDotFirst.dot.getY(), xMax = textDotFirst.dot.getX(), yMax = textDotFirst.dot.getY();
        for (int i = 0; i < textLines.size(); i++) {
            for (TextDot temp : textLines.get(i).dots) {
                if (temp.dot.getX() < xMin) {
                    xMin = temp.dot.getX();
                }
                if (temp.dot.getX() > xMax) {
                    xMax = temp.dot.getX();
                }
                if (temp.dot.getY() < yMin) {
                    yMin = temp.dot.getY();
                }
                if (temp.dot.getY() > yMax) {
                    yMax = temp.dot.getY();
                }
            }
        }
        return new Rect(xMin, yMin, xMax, yMax);
    }

    public static Rect getRectFromTextDots(List<TextDot> textDots) {
        if (textDots == null || textDots.size() == 0) {
            return null;
        }
        TextDot textDotFirst = textDots.get(0);
        double xMin = textDotFirst.dot.getX(), yMin = textDotFirst.dot.getY(), xMax = textDotFirst.dot.getX(), yMax = textDotFirst.dot.getY();
        for (int i = 1; i < textDots.size(); i++) {
            TextDot temp = textDots.get(i);
            if (temp.dot.getX() < xMin) {
                xMin = temp.dot.getX();
            }
            if (temp.dot.getX() > xMax) {
                xMax = temp.dot.getX();
            }
            if (temp.dot.getY() < yMin) {
                yMin = temp.dot.getY();
            }
            if (temp.dot.getY() > yMax) {
                yMax = temp.dot.getY();
            }
        }
        return new Rect(xMin, yMin, xMax, yMax);
    }
//
//    public static boolean hasChoseLine(GISDeviceSetBean hasChoseGISDeviceSetBean, List<GISDeviceSetBean> gisDeviceSetBeans) {
//        if (hasChoseGISDeviceSetBean.layerType != 1) {
//            return true;
//        }
////        if (getAllEditLayer(gisDeviceSetBeans, 2).size() == 1) {
////            return true;
////        }
//        return false;
//    }

    public static boolean openGisGather(final MapGISFrame mapGISFrame, final GISDataProject gisDataProject, final boolean readOnly, boolean isSelfAdd) {
        try {
            final boolean isPad = DeviceUtil.isPad();
            Intent intent = new Intent(mapGISFrame, MapGISFrame.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            if (isPad) {
                if (mapGISFrame.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    mapGISFrame.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
            mapGISFrame.startActivity(intent);
            final GisGather gisGather;
            if (isPad) {
                gisGather = new GisGatherPad(mapGISFrame, mapGISFrame.findViewById(R.id.gisgatherPanel_pad), gisDataProject, readOnly, isPad, isSelfAdd);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gisGather.initGisGather();
                        gisDataProject.painPoj2MapView(mapGISFrame.getMapView());
                    }
                }, 1000);
            } else {
                gisGather = new GisGatherMobile(mapGISFrame, mapGISFrame.findViewById(R.id.gisgatherPanel_mobile), gisDataProject, readOnly, isPad, isSelfAdd);
                gisGather.initGisGather();
                gisDataProject.painPoj2MapView(mapGISFrame.getMapView());
            }

            return true;

        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 编辑属性
     *
     * @param isEditDefaultAttr
     * @param gisDataBeanBase
     * @param dataIndex
     */
    public static void editAttrForProduct(Context context, GISDeviceSetBean hasChoseGISDeviceSetBean, boolean isEditDefaultAttr, GISDataBeanBase gisDataBeanBase, int dataIndex, int from, boolean isPad) {
        Intent intent = new Intent(context, EditDataActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isEditDefaultAttr", isEditDefaultAttr);
        bundle.putString("hasChoseGISDeviceSetBean", new Gson().toJson(hasChoseGISDeviceSetBean));
        bundle.putString("gisDataBeanBase", new Gson().toJson(gisDataBeanBase));
        bundle.putInt("dataIndex", dataIndex);
        bundle.putInt("from", from);
        bundle.putBoolean("isPad", isPad);
        intent.putExtra("bundle", bundle);
        context.startActivity(intent);
    }


    public static void handgisDatas(Context context, List<GISDataBeanBase> gisDatas, int from, List<TextDot> textDots, List<TextLine> textLines, TodayGISData todayGISData) {
        Date now = new Date();
        for (GISDataBeanBase gisDataBeanBase : gisDatas) {
            if (TextUtils.isEmpty(gisDataBeanBase.GeomType)) {
                continue;
            }
            if (TextUtils.isEmpty(gisDataBeanBase.NewGeom)) {
                continue;
            }
            boolean istodaydata = BaseClassUtil.isInDate(gisDataBeanBase.更新时间.replace("T", " "), now);
            if (istodaydata) {
                from = GisDataGatherUtils.GisDataFrom.todayProject;
            }
            if (gisDataBeanBase.GeomType.equals("管点")) {
                Dot dot = null;
                String[] xys = gisDataBeanBase.NewGeom.split(",");
                if (xys != null && xys.length == 2) {
                    dot = new Dot(Double.valueOf(xys[0]), Double.valueOf(xys[1]));
                }
                if (dot == null) {
                    continue;
                }
                int state = TextDotState.ADD.getState();
                if ("编辑".equals(gisDataBeanBase.Operation)) {
                    state = TextDotState.EDIT.getState();
                }
                TextDot textDot = new TextDot(context, gisDataBeanBase, from, dot, state);
                if (istodaydata) {
                    todayGISData.textDots.add(textDot);
                } else {
                    textDots.add(textDot);
                }
            } else {
                List<TextDot> textDotsTpl = new ArrayList<>();
                String[] dotxys = gisDataBeanBase.NewGeom.split("\\|");
                for (String xydot : dotxys) {
                    String[] xy = xydot.split(",");
                    if (xy.length == 2) {
                        TextDot textDot = new TextDot(context, new GISDataBeanBase(), GisDataGatherUtils.GisDataFrom.mapNExist, new Dot(Double.valueOf(xy[0]), Double.valueOf(xy[1])), TextDotState.OHTER.getState());
                        textDotsTpl.add(textDot);
                    }
                }
                if (textDotsTpl.size() != 2) {
                    continue;
                }
                int state = TextLineState.ADD.getState();
                if ("编辑".equals(gisDataBeanBase.Operation)) {
                    state = TextLineState.EDIT.getState();
                }
                TextLine textLine = new TextLine(context, textDotsTpl, gisDataBeanBase, from, state);

                if (istodaydata) {
                    todayGISData.textLines.add(textLine);
                } else {
                    textLines.add(textLine);
                }
            }
        }
    }


    /**
     * @param reportPos
     * @return
     */
    public static boolean isUsefullPosition(String reportPos) {

        if (TextUtils.isEmpty(reportPos)) {
            return false;
        }

        String[] reportArr = reportPos.split(",");

        if (reportArr == null || reportArr.length != 2) {
            return false;
        }

        if (!BaseClassUtil.isNum(reportArr[0]) || !BaseClassUtil.isNum(reportArr[1])) {
            return false;
        }

        float reportX = Float.valueOf(reportArr[0]);
        float reportY = Float.valueOf(reportArr[1]);

        return isUsefullPosition(new Dot(reportX, reportY));

    }

    /**
     * @param
     * @return
     */
    public static boolean isUsefullPosition(Dot gatherDot) {

        GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

        if (xyz == null) {
            return false;
        }

        if (!xyz.isUsefullGPS()) {
            return false;
        }

        if (gatherDot == null) {
            return false;
        }

        double distanse = GisUtil.calcDistance(gatherDot, new Dot(xyz.getX(), xyz.getY()));

        long configAccuracy = MyApplication.getInstance().getConfigValue("ConfigGISAccuracy", 50);

        return distanse < configAccuracy;

    }

    public static String[] splitGisVal(String gisVal) {
        if (TextUtils.isEmpty(gisVal)) {
            return new String[]{};
        }

        String[] gisVals = gisVal.split(",");
        if (gisVal.length() <= 1) {
            gisVals = gisVal.split("\\|");
        }

        return gisVals;

    }

    public static String gisVals2Str(String[] gisVals, boolean isCopyKey) {
        if (gisVals == null) {
            return "";
        }
        List<String> gisValList = new ArrayList<>();
        for (String val : gisVals) {
            String[] kvs = val.split(":");
            if (!isCopyKey) {
                if (kvs.length != 2) {
                    continue;
                }
                gisValList.add(kvs[0] + ":" + kvs[1]);

            } else {
                gisValList.add(kvs[0] + ":" + "");
            }
        }
        return TextUtils.join(",", gisValList);
    }

    public static HashMap<String, String> gisValsMap(String[] gisVals, boolean allowAllEnglist) {
        if (gisVals == null) {
            return new HashMap<>();
        }

        HashMap<String, String> map = new HashMap<>();
        for (String str : gisVals) {
            String[] keyVal = str.split(":");
            if (keyVal.length != 2) {
                continue;
            }
            if (!allowAllEnglist) {
                if (!BaseClassUtil.isContainChinese(keyVal[0])) {
                    continue;
                }
            }
            map.put(keyVal[0], keyVal[1]);
        }
        return map;
    }

    public static String gisKVs2Str(List<String> kvs) {
        if (kvs == null) {
            return "";
        }
        return TextUtils.join(",", kvs);
    }

    public static String gisFeedback2Str(List<FeedItem> fis) {
        if (fis == null) {
            return "";
        }

        List<String> gisKVs = new ArrayList<>();
        for (FeedItem item : fis) {
            //英文冒号替换为中文冒号，解决解析错误
            item.Value = getRightGisVal(item.Value);
            gisKVs.add(item.Name + ":" + item.Value);
        }
        return gisKVs2Str(gisKVs);
    }

    public static String getRightGisVal(String gisVal) {
        if (!TextUtils.isEmpty(gisVal)) {
            return gisVal.replaceAll(":", "：").replaceAll(",", "，").replaceAll("'", "‘");
        }
        return "";
    }
}
