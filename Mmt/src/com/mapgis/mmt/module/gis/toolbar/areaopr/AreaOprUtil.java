package com.mapgis.mmt.module.gis.toolbar.areaopr;

import android.graphics.Color;
import android.text.TextUtils;

import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolygon;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by liuyunfan on 2016/3/22.
 */
public class AreaOprUtil {
    public static String okbtn = "确定";
    public static String rebtn = "重新绘制";

    public static String startPainTxt = "点击绘制";
    public static String showPainTxt = "点击查看";

    public static String showTxt = "点击查看";
    public static String hasPainTxt = "已绘制点击查看";

    public static String notPainTxt = "点击地图开始区域";
    public static String areatip = "绘制中";

    public static Dot getFirstDotOfArea(String areaValue) {

        if (TextUtils.isEmpty(areaValue)) {
            return null;
        }

        Dot dot = null;
        try {

            String[] areas = areaValue.split("\\*");
            if (areas.length > 0) {

                JSONObject jsonObject = new JSONObject(areas[0]);
                JSONArray jsonArray = jsonObject.getJSONArray("rings");
                JSONArray dotArr = (JSONArray) jsonArray.get(0);

                if (dotArr.length() > 0) {
                    JSONArray dotxy = (JSONArray) dotArr.get(0);
                    dot = new Dot((double) dotxy.get(0), (double) dotxy.get(1));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dot;
    }


    public static String painArea(MapView mapView, String value) {
        try {
            if (TextUtils.isEmpty(value)) {
                throw new Exception("");
            }
            String[] areas = value.split("\\*");
            if (areas == null) {
                throw new Exception("");
            }
            Double[] areaNum = new Double[areas.length];
            int k = 0;
            for (String areaItem : areas) {
                Dots dots = new Dots();

                JSONObject jsonObject = new JSONObject(areaItem);
                JSONArray jsonArray = jsonObject.getJSONArray("rings");

                JSONArray dotArr = (JSONArray) jsonArray.get(0);

                for (int i = 0; i < dotArr.length(); i++) {
                    JSONArray dotxy = (JSONArray) dotArr.get(i);

                    Dot dot = new Dot((double) dotxy.get(0), (double) dotxy.get(1));
                    GraphicPoint point = new GraphicPoint(dot, 10);

                    point.setColor(Color.RED);
                    dots.append(dot);
                    mapView.getGraphicLayer().addGraphic(point);
                }
                if (dots.size() > 2) {
                    Dots fullDots = new Dots();

                    fullDots.append(dots);
                    fullDots.append(dots.get(0));

                    GraphicPolygon polygon = new GraphicPolygon(fullDots);
                    polygon.setColor(Color.argb(100, 0, 255, 0));

                    mapView.getGraphicLayer().addGraphic(polygon);

                    mapView.panToCenter(polygon.getCenterPoint(), true);
                    areaNum[k] = Math.abs(polygon.getArea());

                } else if (dots.size() > 1) {
                    GraphicPolylin polylin = new GraphicPolylin(dots);
                    polylin.setLineWidth(5);
                    polylin.setColor(Color.BLACK);
                    mapView.getGraphicLayer().addGraphic(polylin);
                }
                k++;
            }

            mapView.refresh();
            double area = 0;
            // 兼容“逆时针在地图上点击范围时，测量结果是负值”问题
            for (double areaNumItem : areaNum) {
                area += areaNumItem;
            }

            String areaString = "";

            // 1平方公里=1000000平方米
            if (area < 1000000) {
                areaString = String.format("%.1f", area) + "平方米";
            } else {
                areaString = String.format("%.1f", area / 1000000) + "平方公里";
            }
            return "已绘制" + areaString;
        } catch (Exception ex) {
            return "绘制区域错误";
        }
    }

    public static void clearArea(MapView mapView) {
        mapView.getGraphicLayer().removeAllGraphics();
        mapView.getAnnotationLayer().removeAllAnnotations();
        mapView.setAnnotationListener(new DefaultMapViewAnnotationListener());
        mapView.refresh();
    }
}
