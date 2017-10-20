package com.mapgis.mmt.module.gis.toolbar.online.query.point;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineFeature;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineQueryService;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolygon;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.android.mapview.MapView.MapViewTapListener;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.List;

public class OnlinePointQueryListener implements MapViewTapListener {
    private final MapGISFrame mapGISFrame;
    private final MapView mapView;

    private MmtAnnotationListener mmtAnnotationListener;

    public OnlinePointQueryListener(MapGISFrame mapGISFrame, MapView mapView) {
        this.mapGISFrame = mapGISFrame;
        this.mapView = mapView;
    }

    /**
     * 设置AnnotationView的点击事件
     */
    public void setPointQueryAnnotationListener(MmtAnnotationListener mmtAnnotationListener) {
        this.mmtAnnotationListener = mmtAnnotationListener;
    }

    @Override
    public void mapViewTap(PointF arg0) {
        mapView.getGraphicLayer().removeAllGraphics();
        mapView.getAnnotationLayer().removeAllAnnotations();

        Dot clickPoint = mapView.viewPointToMapPoint(arg0);

        String geometryValue = "{\"x\":" + clickPoint.getX() + ",\"y\":" + clickPoint.getY()
                + ",\"spatialReference\":{\"wkid\":1}}";

        List<String> layerIds = MapServiceInfo.getInstance().getLayerIds();
        String layersValue = "";
        Node appTree = MyApplication.getInstance().getConfigValue("appTree", Node.class);

        if (appTree != null) {
            StringBuilder sb = new StringBuilder();
            recursionTree(appTree, sb);
            layersValue = sb.toString();
            layersValue = layersValue.substring(0, layersValue.length() - 1);
            layersValue = "0:" + layersValue;
        } else if (layerIds != null && layerIds.size() > 0) {
            layersValue = "0:" + TextUtils.join(",", layerIds);
        }

        Rect curRect = mapView.getDispRange();
        String mapExtentValue = curRect.toString();

        int dpi = mapGISFrame.getResources().getDisplayMetrics().densityDpi;

        int dpi2 = MyApplication.getInstance().getResources().getDisplayMetrics().densityDpi;

        int dpi3 = mapView.getResources().getDisplayMetrics().densityDpi;

        DisplayMetrics metrics = new DisplayMetrics();
        mapGISFrame.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int dpi4 = metrics.densityDpi;

        //伪造屏幕高宽，适配成96dpi情况下的高宽，以绕过服务端没有识别dpi的漏洞
        int w = (int) (mapView.getWidth() / (dpi / 96.0));
        int h = (int) (mapView.getHeight() / (dpi / 96.0));

        String imageDisplayValue = w + "," + h + "," + 96;

        String geometryTypeValue = "esriGeometryPoint";
        String fValue = "json";
        String returnGeometryValue = "true";
        String srValue = "1";

        //默认查询半径10像素,以PC上默认的96dpi为基础的
        long radius = MyApplication.getInstance().getConfigValue("PointQueryRadiusPX", 10);

        //转换为手机实际对应的查询范围，像素为单位
        radius = (long) (radius * (dpi / 96.0));

        //转换为手机实际对应的查询范围，米为单位
        double radiusMeter = mapView.getResolution(mapView.getZoom()) * radius;

        painQueryRect(clickPoint, radiusMeter);

        new PointQueryTask().executeOnExecutor(MyApplication.executorService, geometryValue, layersValue, imageDisplayValue,
                mapExtentValue, geometryTypeValue, fValue, String.valueOf(radius), returnGeometryValue, srValue);
    }

    private void painQueryRect(Dot clickPoint, double radiusMeter) {
        Dot dot1 = new Dot(clickPoint.x - radiusMeter, clickPoint.y - radiusMeter);
        Dot dot2 = new Dot(clickPoint.x + radiusMeter, clickPoint.y - radiusMeter);
        Dot dot3 = new Dot(clickPoint.x + radiusMeter, clickPoint.y + radiusMeter);
        Dot dot4 = new Dot(clickPoint.x - radiusMeter, clickPoint.y + radiusMeter);

        GraphicPolygon graphic = new GraphicPolygon();

        graphic.setColor(Color.argb(125, 255, 0, 0));

        graphic.appendPoint(dot1);
        graphic.appendPoint(dot2);
        graphic.appendPoint(dot3);
        graphic.appendPoint(dot4);

        mapView.getGraphicLayer().addGraphic(graphic);

        mapView.refresh();
    }

    private void recursionTree(Node node, StringBuilder sb) {

        if (node != null) {

            List<Node> children = node.getChildren();
            if (children != null && children.size() > 0) {
                for (Node child : children) {
                    if (child != null) {
                        recursionTree(child, sb);
                    }
                }
            } else {
                if (node.isChecked() == true)
                    sb.append(node.getValue()).append(",");
            }
        }
    }


    class PointQueryTask extends AsyncTask<String, String, String> {
        private ProgressDialog loadDialog;

        @Override
        protected void onPreExecute() {
            loadDialog = MmtProgressDialog.getLoadingProgressDialog(mapGISFrame, " 正在查询信息");
            loadDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String result = NetUtil.executeHttpGet(OnlineQueryService.getPointQueryService(), "geometry", params[0], "layers",
                    params[1], "imageDisplay", params[2], "mapExtent", params[3], "geometryType", params[4], "f", params[5],
                    "tolerance", params[6], "returnGeometry", params[7], "sr", params[8]);

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            try {

                if (result == null || result.length() == 0) {
                    mapGISFrame.showToast("未查询到信息!");
                    return;
                }

                result = result.replace("\\", "");

                String[] attArrTemp = result.split("attributes");

                ResultData data = null;
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                if (attArrTemp.length > 1 && attArrTemp[1].indexOf("\"ID") == attArrTemp[1].lastIndexOf("\"ID")) {
                    data = gson.fromJson(result, ResultData.class);
                } else {
                    result = "";
                    for (int i = 0; i < attArrTemp.length; i++) {
                        attArrTemp[i] = attArrTemp[i].replaceFirst("\"ID", "\"DUMPLICATE_ID");
                        result += attArrTemp[i] + "attributes";
                    }
                    result = result.substring(0, result.lastIndexOf("attributes"));
                    data = gson.fromJson(result, ResultData.class);

                }

                if (data == null || data.results.length == 0) {
                    mapGISFrame.showToast("未查询到信息!");
                } else {
                    OnlineFeature queryResult = data.results[0];

                    if (mmtAnnotationListener == null) {
                        mapView.setAnnotationListener(new MmtAnnotationListener());
                    } else {
                        mapView.setAnnotationListener(mmtAnnotationListener);
                    }

                    String field = LayerConfig.getInstance().getConfigInfo(queryResult.layerName).HighlightField;

                    String highlight = TextUtils.isEmpty(field) ? "" : queryResult.attributes.get(field);

                    if (TextUtils.isEmpty(highlight))
                        highlight = "-";

                    queryResult.showAnnotationOnMap(mapView, highlight, queryResult.layerName,
                            BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_lcoding))
                            .showAnnotationView();

                    GraphicPoint graphic = new GraphicPoint();

                    graphic.setColor(Color.RED);
                    graphic.setSize(5);
                    graphic.setPoint(queryResult.geometry.toDot());

                    mapView.getGraphicLayer().addGraphic(graphic);

                    mapView.getGraphicLayer().addGraphic(queryResult.geometry.createGraphicPolylin());

                    mapView.refresh();
                }

            } catch (Exception e) {

                mapGISFrame.showToast("查询结果异常:" + e.toString());

                e.printStackTrace();
            } finally {
                loadDialog.cancel();
            }
        }

        private class ResultData {
            public OnlineFeature[] results;
        }
    }
}
