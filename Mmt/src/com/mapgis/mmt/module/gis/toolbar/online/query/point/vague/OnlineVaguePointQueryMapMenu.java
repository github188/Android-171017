package com.mapgis.mmt.module.gis.toolbar.online.query.point.vague;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.SelfView;
import com.mapgis.mmt.common.widget.fragment.SplitListViewFragment;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineFeature;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineQueryResult;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineSpatialQueryTask;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.GraphicPolygon;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class OnlineVaguePointQueryMapMenu extends BaseMapMenu {

    public static final int RESULT_PIPE_LOCATE = 200;
    public static final int RESULT_PIPE_GOBACK = 201;

    /**
     * 显示 模糊点击查询结果 的 Fragment
     */
    private SplitListViewFragment splitListViewFragment;
    private List<String> leftListVData;
    private List<List<String>> rightListVData;

    private LinkedHashMap<String, ArrayList<OnlineFeature>> resultLinkedHashMap;

    public ProgressBar progressBar;

    private MmtAnnotationListener mmtAnnotationListener;
    public ImageView queryResultListImg;
    private OnlineLayerInfo[] layerinfoArr;
    private int currentLayerIndex = 0;
    /**
     * 当前查询 的 范围
     */
    private Rect searchExtent;

    /**
     * 第一次点击 时 矩形 的 中心点 屏幕坐标
     */
    private PointF centerPoint;
    /**
     * 第一次点击 时 矩形 的 中心点 地理坐标
     */
    private Dot centerDot;
    /**
     * 矩形 的 Graphic
     */
    private GraphicPolygon rectGraPolygon;
    /**
     * 矩形 左上角 的 地理点
     */
    private Dot leftUpDot;
    /**
     * 矩形右下角 的 地理坐标 点
     */
    private Dot rightBottom;
    /**
     * 标识 当前 是否 正在 查询
     */
    public Boolean isQuerying = false;
    /**
     * 方形查询范围 最大 半边长
     */
    private final int maxRadius = 200;
    /**
     * 方形查询范围 最小 半边长
     */
    private final int minRadius = 50;
    /**
     * 自定义 的 拉伸范围
     */
    private SelfView extendView;
    /**
     * 点击时 显示 的 属性字段 的 名称
     */
    public String shouField;
    /**
     * 查询 到 有数据 的 图层 数量
     */
    private int layerCount = 0;
    /**
     * 当前绘制到第几个Graphic， 用于 决定 选择markDrawable 中的 第几个 Drawable
     */
    private int graphicCount = 0;
    /**
     * 1-10 的 点标签， 11 小点标签
     */
    private final int markDrawable[] = {R.drawable.icon_marka, R.drawable.icon_markb, R.drawable.icon_markc,
            R.drawable.icon_markd, R.drawable.icon_marke, R.drawable.icon_markf, R.drawable.icon_markg, R.drawable.icon_markh,
            R.drawable.icon_marki, R.drawable.icon_markj, R.drawable.icon_mark_pt};

    public OnlineVaguePointQueryMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);

    }

    @Override
    public boolean onOptionsItemSelected() {
//        if (mapView == null || mapView.getMap() == null) {
//            mapGISFrame.stopMenuFunction();
//            return false;
//        }
//
//        layerinfoArr = MapServiceInfo.getInstance().getLayers();
//
//        leftListVData = new ArrayList<String>();
//        rightListVData = new ArrayList<List<String>>();
//
//        initSplitListViewFragment();
//
//        mmtAnnotationListener = new MmtAnnotationListener();
//        mmtAnnotationListener.setBlueShow(false);
//        mapView.setAnnotationListener(mmtAnnotationListener);
//
//        ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvPlanName)).setText("模糊点击查询");
//
//        ((ImageButton) mapGISFrame.getCustomView().findViewById(R.id.tvPlanBack)).setOnClickListener(backImgBtnClickListener);
//
//        mapView.setTapListener(tapListener);
        Toast.makeText(mapGISFrame, "此功能已经废弃，请从OMS配置中移除", Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.vague_point_query_titlebar, null);
        view.findViewById(R.id.tvPlanBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mapGISFrame.resetMenuFunction();
            }
        });

        ((TextView) view.findViewById(R.id.tvPlanName)).setText("模糊点击查询");
        progressBar = (ProgressBar) view.findViewById(R.id.searchProgressBar);
        queryResultListImg = (ImageView) view.findViewById(R.id.ivPlanDetail);

        view.findViewById(R.id.ivPlanDetail).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                splitListViewFragment.show(mapGISFrame.getSupportFragmentManager().beginTransaction(), "");
            }
        });

        return view;

    }

    @Override
    public boolean onActivityResult(int resultCode, Intent intent) {
        switch (resultCode) {
            case RESULT_PIPE_GOBACK:
                handler.sendEmptyMessage(5);
                break;
        }
        return super.onActivityResult(resultCode, intent);
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 5:
                    splitListViewFragment.show(mapGISFrame.getSupportFragmentManager().beginTransaction(), "");
                    break;
            }
        }
    };

    private void initSplitListViewFragment() {
        splitListViewFragment = new SplitListViewFragment("查询结果", "图层", "设备信息", leftListVData, rightListVData);
        splitListViewFragment.setRightListItemSingleLine(true);
        splitListViewFragment.setCancelable(false);
        splitListViewFragment.setLeftLayoutWeight(4);
        splitListViewFragment.setRightLayoutWeight(6);
        splitListViewFragment.setSplitListViewPositiveClick(new SplitListViewFragment.SplitListViewPositiveClick() {
            @Override
            public void onSplitListViewPositiveClick(String leftListValue, String rightListValue, int leftPos, int rightPos) {

                int position = 0;

                for (int i = 0; i < leftPos; i++) {
                    position = position + rightListVData.get(i).size();
                }
                position = position + rightPos;

                Annotation annotation = mapView.getAnnotationLayer().getAnnotation(position);
                annotation.showAnnotationView();

                String layerName = leftListValue.substring(0, leftListValue.indexOf("("));
                OnlineFeature itemClickGraphic = resultLinkedHashMap.get(layerName).get(rightPos);

                Intent intent = new Intent(mapGISFrame, PipeDetailActivity.class);

                intent.putExtra("graphicMap", itemClickGraphic.attributes);
                intent.putExtra("graphicMapStr", new Gson().toJson(itemClickGraphic.attributes));
                intent.putExtra("fromWhere", "gisDevice");
                String place = itemClickGraphic.attributes.get("位置");
                if (BaseClassUtil.isNullOrEmptyString(place)) {
                    place = itemClickGraphic.attributes.get("所在位置");
                }
                if (BaseClassUtil.isNullOrEmptyString(place)) {
                    place = itemClickGraphic.attributes.get("道路名");
                }
                if (BaseClassUtil.isNullOrEmptyString(place)) {
                    place = "";
                }
                intent.putExtra("place", place);
                intent.putExtra("xy", itemClickGraphic.geometry.toDot().toString());
                intent.putExtra("isSetResult", true);
                mapGISFrame.startActivityForResult(intent, 0);
            }
        });
    }

    private final MapView.MapViewTapListener tapListener = new MapView.MapViewTapListener() {
        @Override
        public void mapViewTap(PointF tapPointF) {

            if (isQuerying == true) {
                Toast.makeText(mapGISFrame, "正在查询，请稍后...", Toast.LENGTH_SHORT).show();
                return;
            }

            Boolean b = mmtAnnotationListener.getHideTapListener();
            if (b) {
                mmtAnnotationListener.setHideTapListener(false);
                return;
            }

            mapView.getGraphicLayer().removeAllGraphics();

            centerPoint = tapPointF;
            centerDot = mapView.viewPointToMapPoint(centerPoint);
            leftUpDot = mapView.viewPointToMapPoint(new PointF(tapPointF.x - 100, tapPointF.y - 100));
            rightBottom = mapView.viewPointToMapPoint(new PointF(tapPointF.x + 100, tapPointF.y + 100));

            rectGraPolygon = new GraphicPolygon(new Dot[]{leftUpDot, new Dot(rightBottom.x, leftUpDot.y), rightBottom,
                    new Dot(leftUpDot.x, rightBottom.y), leftUpDot});
            rectGraPolygon.setBorderlineWidth(2);
            rectGraPolygon.setBorderlineColor(Color.RED);
            rectGraPolygon.setColor(Color.argb(33, 0, 0, 160));

            creatExtentImgView(tapPointF);

            mapView.getGraphicLayer().addGraphic(rectGraPolygon);
            mapView.refresh();

            startQuery();
        }
    };

    /**
     * 清空 本次 模糊点击查询 的 结果， 执行 下一次 模糊点击查询
     */
    private final OnClickListener backImgBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvPlanName)).setText("模糊点击查询");
            ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvTaskState)).setText("");

            LinkedList<SelfView> selfViewsListTemp = mapGISFrame.getFragment().selfViewsList;
            for (SelfView item : selfViewsListTemp) {
                mapView.removeView(item.view);
            }
            selfViewsListTemp.clear();
            selfViewsListTemp = null;

            // 清除 地图上的所有杂物，退出 模糊查询
            mapView.getGraphicLayer().removeAllGraphics();
            mapView.getAnnotationLayer().removeAllAnnotations();
            mapView.refresh();

            mapGISFrame.getFragment().getView().findViewById(R.id.mapviewClear).performClick();
        }
    };

    /**
     * 创建 拖拉 选择 查询 范围 的 ImageView
     */
    private void creatExtentImgView(PointF tapPointF) {

        final Bitmap bitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.search_extent_selector2);

        final RelativeLayout.LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        final ImageView imgV = new ImageView(mapGISFrame);
        imgV.setImageBitmap(bitmap);
        imgV.setScaleType(ScaleType.CENTER);
        imgV.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isQuerying) {
                    Toast.makeText(mapGISFrame, "正在查询，请稍后...", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 以防 地图 被 移动， 相同 屏幕坐标上 不再 是 矩形 中心
                    centerPoint = mapView.mapPointToViewPoint(centerDot);
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int radius = (int) event.getRawX() - (int) centerPoint.x;
                    if (radius > maxRadius || radius < minRadius) {
                        return true;
                    }

                    RelativeLayout.LayoutParams lp2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    lp2.setMargins((int) event.getRawX() - bitmap.getWidth() / 2, (int) centerPoint.y - bitmap.getHeight() / 2,
                            0, 0);
                    imgV.setLayoutParams(lp2);

                    leftUpDot = mapView.viewPointToMapPoint(new PointF(centerPoint.x - radius, centerPoint.y - radius));
                    rightBottom = mapView.viewPointToMapPoint(new PointF(centerPoint.x + radius, centerPoint.y + radius));
                    Dot[] dots = new Dot[]{leftUpDot, new Dot(rightBottom.x, leftUpDot.y), rightBottom,
                            new Dot(leftUpDot.x, rightBottom.y), leftUpDot};
                    rectGraPolygon.setPoints(dots);
                    mapView.refresh();

                    int marginLeft = (int) event.getRawX();
                    int marginTop = (int) centerPoint.y;
                    extendView.setDot(mapView.viewPointToMapPoint(new PointF(marginLeft, marginTop)));
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int radius = (int) event.getRawX() - (int) centerPoint.x;
                    if (radius > maxRadius || radius < minRadius) {
                        startQuery();

                        return true;
                    }

                    int marginLeft = (int) event.getRawX();
                    int marginTop = (int) centerPoint.y;
                    extendView.setDot(mapView.viewPointToMapPoint(new PointF(marginLeft, marginTop)));

                    startQuery();
                }
                return true;
            }
        });
        int marginLeft = (int) tapPointF.x - bitmap.getWidth() / 2 + 100;
        int marginTop = (int) tapPointF.y - bitmap.getHeight() / 2;
        lp.setMargins(marginLeft, marginTop, 0, 0);
        mapView.addView(imgV, lp);

        if (extendView != null) {
            mapGISFrame.getFragment().selfViewsList.remove(extendView);
            mapView.removeView(extendView.view);
        }
        extendView = new SelfView(mapView, imgV, bitmap.getWidth(), bitmap.getHeight(), mapView.viewPointToMapPoint(new PointF(
                (int) tapPointF.x + 100, (int) tapPointF.y)));
        mapGISFrame.getFragment().selfViewsList.add(extendView);
    }

    private void startQuery() {
        mapView.refresh();

        if (resultLinkedHashMap != null) {
            resultLinkedHashMap = null;
        }
        resultLinkedHashMap = new LinkedHashMap<String, ArrayList<OnlineFeature>>();
        leftListVData.clear();
        rightListVData.clear();

        progressBar.setVisibility(View.VISIBLE);
        queryResultListImg.setVisibility(View.INVISIBLE);
        ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvPlanName)).setText("正在进行 模糊点击查询");

        ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvTaskState)).setText("");

        mapView.getAnnotationLayer().removeAllAnnotations();

        searchExtent = new Rect();
        searchExtent.setXMin(leftUpDot.x < rightBottom.x ? leftUpDot.x : rightBottom.x);
        searchExtent.setYMin(leftUpDot.y < rightBottom.y ? leftUpDot.y : rightBottom.y);
        searchExtent.setXMax(leftUpDot.x > rightBottom.x ? leftUpDot.x : rightBottom.x);
        searchExtent.setYMax(leftUpDot.y > rightBottom.y ? leftUpDot.y : rightBottom.y);

        currentLayerIndex = 0;
        graphicCount = 0;
        layerCount = 0;
        if (layerinfoArr.length > 0) {
            isQuerying = true;
            new OnlineVaguePointQueryTask(mapGISFrame, searchExtent, layerinfoArr[currentLayerIndex].id)
                    .executeOnExecutor(MyApplication.executorService);
        }

    }

    /**
     * 一个图层 查询 完毕
     */
    public void oneLayerQueryComplete(OnlineQueryResult data) {
        // 绘制 上一图层 查询到的 结果
        if (data != null && data.features != null) {
            layerCount++;
            resultLinkedHashMap.put(layerinfoArr[currentLayerIndex].name,
                    new ArrayList<OnlineFeature>(Arrays.asList(data.features)));
            OnlineFeature[] onlineFeature = data.features;

            leftListVData.add(layerinfoArr[currentLayerIndex].name + "(" + onlineFeature.length + ")");
            ArrayList<String> rightListItem = new ArrayList<String>();

            for (int i = 0; i < onlineFeature.length; i++) {

                OnlineFeature featureResult = onlineFeature[i];

                String field = LayerConfig.getInstance().getConfigInfo(layerinfoArr[currentLayerIndex].name).HighlightField;

                String highlight = BaseClassUtil.isNullOrEmptyString(field) ? "" : featureResult.attributes.get(field);

                MmtAnnotation mmtAnnotation = featureResult.showAnnotationOnMap(mapView, highlight, String.valueOf(i),
                        BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_lcoding));
                mmtAnnotation.attrMap.putAll(featureResult.attributes);

                graphicCount++;
                // TODO
                // mmtAnnotationListener.addFeature(featureResult);

                rightListItem.add(field + ":" + highlight);
            }
            rightListVData.add(rightListItem);
            mapView.refresh();
        }

        // 绘制 完毕 判断 是否 还有 图层 需要 查询
        currentLayerIndex++;
        if (currentLayerIndex < layerinfoArr.length) {
            new OnlineVaguePointQueryTask(mapGISFrame, searchExtent, layerinfoArr[currentLayerIndex].id)
                    .executeOnExecutor(MyApplication.executorService);
            // Toast.makeText(mapGISFrame, currentLayerIndex +"号图层" ,
            // Toast.LENGTH_SHORT).show();
        } else {
            // Toast.makeText(mapGISFrame, data.displayFieldName,
            // Toast.LENGTH_SHORT).show();
            ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvPlanName)).setText("模糊点击查询  完成");
            ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvTaskState)).setText("共  " + layerCount + "个图层   "
                    + graphicCount + "个设备");
            progressBar.setVisibility(View.INVISIBLE);
            queryResultListImg.setVisibility(View.VISIBLE);
            // 查询 结束 后 进行
            isQuerying = false;
        }
    }

    public class OnlineVaguePointQueryTask extends OnlineSpatialQueryTask {

        public OnlineVaguePointQueryTask(MapGISFrame mapGISFrame, Rect rect, String objectIds) {
            super(mapGISFrame, rect, objectIds);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String result) {
            OnlineQueryResult data = null;
            result = result.replace("\\", "");
            try {

                String[] attArrTemp = result.split("attributes");

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                if (attArrTemp.length > 1 && attArrTemp[1].indexOf("\"ID") == attArrTemp[1].lastIndexOf("\"ID")) {
                    data = gson.fromJson(result, OnlineQueryResult.class);
                } else {
                    result = "";
                    for (int i = 0; i < attArrTemp.length; i++) {
                        attArrTemp[i] = attArrTemp[i].replaceFirst("\"ID", "\"DUMPLICATE_ID");
                        result += attArrTemp[i] + "attributes";
                    }
                    result = result.substring(0, result.lastIndexOf("attributes"));
                    data = gson.fromJson(result, OnlineQueryResult.class);

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                onTaskDone(data);
            }
        }

        @Override
        protected void onTaskDone(OnlineQueryResult data) {
            oneLayerQueryComplete(data);
        }

    }

}
