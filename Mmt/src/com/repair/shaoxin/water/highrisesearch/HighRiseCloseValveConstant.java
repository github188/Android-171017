package com.repair.shaoxin.water.highrisesearch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BitmapUtil;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationLayer;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicLayer;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.featureservice.Feature;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class HighRiseCloseValveConstant {
    public static final String LAYER_NAME = "用户";
    public static final String LAYER_FIELDS = "水表卡号";// CN-15-000016

    public static final String VALVE_NAME = "阀门";
    public static final String VALVE_FIELDS = "编号";

    /**
     * 高层关阀是否开启
     */
    public static boolean isOn = false;

    public static String MeterNo;// 水表卡号值
    public static final List<HRCVUser> queryUser = new ArrayList<>();// 根据卡号查询到的用户
    public static final List<String> queryValve = new ArrayList<>();// 查询到的受关联的阀门

    public static final List<Graphic> VALVE_GRAPHICS = new ArrayList<>();
    public static final List<Graphic> USER_GRAPHICS = new ArrayList<>();

    /**
     * 显示卡号关联的用户
     */
    public static Dialog showQueryUser() {

        if (queryUser.size() == 0) {
            Toast.makeText(MyApplication.getInstance().mapGISFrame, "未查询到" + LAYER_FIELDS + "关联的用户", Toast.LENGTH_SHORT).show();
            return null;
        }

        String[] showStrs = new String[1];
        showStrs[0] = queryUser.get(0).toString();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MyApplication.getInstance().mapGISFrame, R.layout.simple_list_item_1,
                showStrs);

        AlertDialog.Builder builder = new AlertDialog.Builder(MyApplication.getInstance().mapGISFrame);
        builder.setTitle("用户");
        builder.setAdapter(adapter, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("确认", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("查询关联阀门", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new GetCloseValveInfoTask(MyApplication.getInstance().mapGISFrame).execute(HighRiseCloseValveConstant.MeterNo);
            }
        });

        return builder.create();
    }

    /**
     * 显示用户关联的阀门对话框
     */
    public static void showQueryValve() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MyApplication.getInstance().mapGISFrame, R.layout.simple_list_item_1,
                queryValve);

        AlertDialog.Builder builder = new AlertDialog.Builder(MyApplication.getInstance().mapGISFrame);
        builder.setTitle("关联阀门");
        builder.setAdapter(adapter, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setNegativeButton("确认", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton("上报结果", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new ReportHRCVTask(MyApplication.getInstance().mapGISFrame).execute();
            }
        });

        builder.create().show();
    }

    /**
     * 将查询的数据集转换成Graphic集合
     * @param featurePagedResult FeatruePageResult对象
     * @return    Graphic的集合
     */
    public static List<Graphic> exchangeToGraphics(FeaturePagedResult featurePagedResult) {
        List<Graphic> graphics = new ArrayList<>();

        for (int i = 1; i < featurePagedResult.getPageCount() + 1; i++) {
            List<Feature> featureList = featurePagedResult.getPage(i);
            for (int j = 0; j < featureList.size(); j++) {
                Feature feature = featureList.get(j);
                // 属性转换成图形列表
                graphics.addAll(feature.toGraphics(true));
            }
        }
        return graphics;
    }

    /**
     * 将查询结果显示在地图上
     */
    public static void showOnMap() {
        MapView mapView = MyApplication.getInstance().mapGISFrame.getMapView();
        GraphicLayer graphicLayer = mapView.getGraphicLayer();
        final AnnotationLayer annotationLayer = mapView.getAnnotationLayer();

        graphicLayer.removeAllGraphics();
        annotationLayer.removeAllAnnotations();


//        Bitmap userBitmap = BitmapUtil.getBitmap(MyApplication.getInstance().mapGISFrame.getResources()
//                , R.drawable.maptip_user_locator);
        Bitmap userBitmap = BitmapUtil.getBitmap(MyApplication.getInstance().mapGISFrame.getResources()
                , R.drawable.icon_marka);
        //icon_gcoding
        for (Graphic graphic : USER_GRAPHICS) {
//            Dot dot = new Dot(Double.valueOf(graphic.getAttributeValue("横座标"))
//                    ,Double.valueOf(graphic.getAttributeValue("纵座标")));
            Dot dot = graphic.getCenterPoint();
            String title = "用户号：" + graphic.getAttributeValue("用户号");
            String msg = "地址：" + graphic.getAttributeValue("地址");
            DecorAnnotation annotation = new DecorAnnotation(graphic,title,msg,dot,userBitmap);
            annotationLayer.addAnnotation(annotation);
            mapView.panToCenter(dot,true);
            anno = annotation;
        }

//        Bitmap valveBitmap = BitmapUtil.getBitmap(MyApplication.getInstance().mapGISFrame.getResources()
//                , R.drawable.maptip_nousevalve_locator);
        Bitmap valveBitmap = BitmapUtil.getBitmap(MyApplication.getInstance().mapGISFrame.getResources()
                , R.drawable.icon_markb);
                //icon_marka
        for (Graphic graphic : VALVE_GRAPHICS) {
//            Dot dot = new Dot(Double.valueOf(graphic.getAttributeValue("横座标"))
//                    ,Double.valueOf(graphic.getAttributeValue("纵座标")));
            Dot dot = graphic.getCenterPoint();
            String title = "编号：" + graphic.getAttributeValue("编号");
            String msg = "位置：" + graphic.getAttributeValue("位置");
            DecorAnnotation annotation = new DecorAnnotation(graphic,title, msg, dot, valveBitmap);
            annotation.setCenterOffset(new Point(0,1));

            annotationLayer.addAnnotation(annotation);
        }

        mapView.setAnnotationListener(new DefaultMapViewAnnotationListener(){
            @Override
            public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
                Annotation annotation = annotationview.getAnnotation();
                if (annotation instanceof DecorAnnotation){
                    Graphic graphic = ((DecorAnnotation) annotation).getGraphic();
                    toDetailActivity(graphic,graphic.getAttributeValue("名称"));
                }
            }
        });

        mapView.setZoomChangedListener(new MapView.MapViewZoomChangedListener() {
            @Override
            public void mapViewZoomChanged(MapView mapView, float v, float v1) {
                anno.getPoint();
                System.out.println("1111="+anno.getPoint().toString());
                System.out.println("1111="+anno.getCenterOffset().toString());
                System.out.println("1111="+anno.getGraphic().getCenterPoint().toString());
            }
        });

        mapView.refresh();
    }

    private static DecorAnnotation anno = null;


    private static void toDetailActivity(Graphic graphic,String layerName) {
        LinkedHashMap<String, String> graphicMap = new LinkedHashMap<>();

        for (int m = 0; m < graphic.getAttributeNum(); m++) {
            graphicMap.put(graphic.getAttributeName(m), graphic.getAttributeValue(m));
        }

        Intent intent = new Intent(MyApplication.getInstance().mapGISFrame, PipeDetailActivity.class);

        intent.putExtra("layerName", layerName);
        // intent.putExtra("isSetResult", true);
        intent.putExtra("graphicMap", graphicMap);
        intent.putExtra("graphicMapStr",new Gson().toJson(graphicMap));
        MyApplication.getInstance().mapGISFrame.startActivityForResult(intent, 0);
    }
}
