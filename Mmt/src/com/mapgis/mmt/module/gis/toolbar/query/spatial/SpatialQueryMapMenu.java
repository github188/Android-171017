package com.mapgis.mmt.module.gis.toolbar.query.spatial;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.spatialquery.SpatialSearchResultList;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicMultiPoint;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.graphic.GraphicStippleLine;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 空间查询
 *
 * @author Administrator
 */
public class SpatialQueryMapMenu extends BaseMapMenu {

    ProgressDialog progressDialog;

    protected final MmtAnnotationListener listener;

    protected GraphicPolylin pipeLine;
    private GraphicStippleLine directionLine;

    public SpatialQueryMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);

        listener = new MmtAnnotationListener();

        progressDialog = MmtProgressDialog.getLoadingProgressDialog(mapGISFrame, "正在查询数据，请稍等...");
    }

    /**
     * 选择空间查询菜单
     */
    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();
            return false;
        }

        Intent intent = new Intent(mapGISFrame, LayerSelectActivity.class);

        intent.putStringArrayListExtra("layers", GisQueryUtil.getVisibleVectorLayerNames(mapView));

        mapGISFrame.startActivityForResult(intent, 0);

        mapView.setTapListener(null);

        return true;
    }

    Rect rect;

    @Override
    public View initTitleView() {

        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.header_bar_plan_name, null);

        view.findViewById(R.id.tvPlanBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mapGISFrame.resetMenuFunction();
            }
        });

        ((TextView) view.findViewById(R.id.tvPlanName)).setText("查询功能");

        view.findViewById(R.id.ivPlanDetail).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mapGISFrame, SpatialSearchResultList.class);

                intent.putExtra("layerName", layer.getName());
                intent.putExtra("page", currentPage);
                intent.putExtra("clickWhichIndex", listener.clickWhichIndex);

                mapGISFrame.startActivityForResult(intent, 0);

                MyApplication.getInstance().startActivityAnimation(mapGISFrame);
            }
        });

        return view;

    }

    protected MapLayer layer = null;
    protected int currentPage = 1;
    // private int pageCount = 1;
    public static FeaturePagedResult featurePagedResult;
    String where;

    /**
     * 空间查询相关的地图回调函数
     *
     * @param resultCode
     * @param intent
     * @return
     */
    @Override
    public boolean onActivityResult(int resultCode, Intent intent) {
        try {
            if (resultCode == Activity.RESULT_CANCELED) {
//                ClearMapMenu menu = new ClearMapMenu(mapGISFrame);
//                menu.onOptionsItemSelected();
                return true;
            }

            if (intent == null) {
                return true;
            }

            boolean isLocate = false;
            switch (resultCode) {
                case ResultCode.RESULT_LAYER_SELECTED:
                    onLayerSelectedResult(intent);
                    break;
                case ResultCode.RESULT_PIPE_LOCATE:
                    isLocate = true;
                case ResultCode.RESULT_PIPE_REFREASH:
                    currentPage = intent.getIntExtra("page", 1);
                    listener.clickWhichIndex = intent.getIntExtra("clickWhichIndex", -1);
                    showPageResultOnMap(isLocate);
                    break;
                default:
                    return false;
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        }
    }

    @Override
    public boolean onBackPressed() {
        featurePagedResult = null;

        return super.onBackPressed();
    }

    /**
     * 选择空间查询菜单并且已经选择了图层之后的触发函数
     *
     * @param intent
     */
    public void onLayerSelectedResult(Intent intent) {
        if (!findLayerByName(intent)) {
            return;
        }

        //rect = mapView.getDispRange();
        where = "";
        Rect mapRect = mapView.getMap().getRange();
        Rect dispRect = mapView.getDispRange();
        //取magRect和dispRect两者的交集，null默认查全部
        rect = GisUtil.getMixRect(mapRect, dispRect);
        if (GisUtil.IsInEnvelope(mapRect, rect)) {
            rect = null;
        }
        ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvTaskState)).setText("选择图层: " + layer.getName());

        task.executeOnExecutor(MyApplication.executorService, "");
    }

    AsyncTask<String, Integer, String> task = new AsyncTask<String, Integer, String>() {

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(1000);

                featurePagedResult = FeatureQuery.query((VectorLayer) layer, where,
                        rect == null ? null : new FeatureQuery.QueryBound(rect), FeatureQuery.SPATIAL_REL_OVERLAP, true,
                        true, "", 10);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (featurePagedResult.getTotalFeatureCount() > 0) {
                    // pageCount = featurePagedResult.getPageCount();
                    showPageResultOnMap(false);
                } else {
                    Toast.makeText(mapGISFrame, "当前查询条件下无查询结果返回", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                progressDialog.dismiss();
            }
        }
    };

    public boolean findLayerByName(Intent intent) {
        String name = intent.getStringExtra("layer");

        LayerEnum layerEnum = mapView.getMap().getLayerEnum();
        layerEnum.moveToFirst();
        while ((layer = layerEnum.next()) != null) {

            if (layer.getName().equals(name)) {
                break;
            }
        }

        return !(layer == null || !(layer instanceof VectorLayer));
    }

    /**
     * 显示当前页的结果在地图上     *
     */
    void showPageResultOnMap(boolean isLocate) {
        try {
            mapView.getAnnotationLayer().removeAllAnnotations();

            List<Graphic> graphics = new ArrayList<Graphic>();

            graphics.addAll(Convert.fromFeaturesToGraphics(featurePagedResult.getPage(currentPage), layer.getName()));

            mapView.setAnnotationListener(listener);

            Graphic graphic = null;

            // 给最新查询出来的数据编号
            int[] icons = {R.drawable.icon_marka, R.drawable.icon_markb, R.drawable.icon_markc, R.drawable.icon_markd,
                    R.drawable.icon_marke, R.drawable.icon_markf, R.drawable.icon_markg, R.drawable.icon_markh, R.drawable.icon_marki,
                    R.drawable.icon_markj};

            String field = LayerConfig.getInstance().getConfigInfo(layer.getName()).HighlightField;

            for (int i = 0; i < graphics.size(); i++) {
                graphic = graphics.get(i);

                String title = BaseClassUtil.isNullOrEmptyString(field) ? "" : graphic.getAttributeValue(field);

                Dot dot = getCenterDot(graphic);

                MmtAnnotation annotation = new MmtAnnotation(graphic,
                        BaseClassUtil.isNullOrEmptyString(title) ? graphic.getAttributeValue(0) : title, "", dot,
                        BitmapFactory.decodeResource(mapGISFrame.getResources(), icons[i]));

                // Focus on a specified point.
                if (isLocate && listener.clickWhichIndex != -1 && listener.clickWhichIndex == i) {
                    annotation.setImage(BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_gcoding));
                    mapView.panToCenter(annotation.getPoint(), true);
                    annotation.showAnnotationView();

                    if (pipeLine != null) {
                        mapView.getGraphicLayer().removeGraphic(pipeLine);
                    }

                    if (directionLine != null)
                        mapView.getGraphicLayer().removeGraphic(directionLine);

                    if (graphic instanceof GraphicPolylin) {
                        pipeLine = (GraphicPolylin) graphic;
                        pipeLine.setColor(Color.RED);
                        pipeLine.setLineWidth(5);

                        mapView.getGraphicLayer().addGraphic(pipeLine);
                    }

                    if (MyApplication.getInstance().getConfigValue("showDirectionLine", 0) > 0) {
                        GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

                        if (xyz.isUsefull()) {
                            directionLine = new GraphicStippleLine(xyz.convertToPoint(), graphic.getCenterPoint());

                            directionLine.setColor(Color.RED);
                            directionLine.setLineWidth(10);

                            mapView.getGraphicLayer().addGraphic(directionLine);
                        }
                    }
                }

                mapView.getAnnotationLayer().addAnnotation(annotation);

            }
            mapView.refresh();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected Dot getCenterDot(Graphic graphic) {
        Dot dot = graphic.getCenterPoint();

        if (graphic instanceof GraphicMultiPoint) {
            GraphicMultiPoint g = (GraphicMultiPoint) graphic;

            double xs = 0;
            double ys = 0;

            for (Dot d : g.getPoints()) {
                xs += d.getX();
                ys += d.getY();
            }

            return new Dot(xs / g.getPointCount(), ys / g.getPointCount());

        }
        return dot;
    }
}
