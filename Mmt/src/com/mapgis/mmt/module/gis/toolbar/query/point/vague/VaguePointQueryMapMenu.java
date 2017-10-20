package com.mapgis.mmt.module.gis.toolbar.query.point.vague;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.SelfView;
import com.mapgis.mmt.common.widget.fragment.SplitListViewFragment;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.constant.ActivityAlias;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPolygon;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 模糊点击 查询
 *
 * @author meikai
 */
public class VaguePointQueryMapMenu extends BaseMapMenu {

    private MmtAnnotationListener mmtAnnotationListener;

    private LinkedHashMap<String, ArrayList<Graphic>> resultLinkedHashMap;
    private List<Graphic> listenerList;

    /**
     * 显示 模糊点击查询结果 的 Fragment
     */
    private SplitListViewFragment splitListViewFragment;
    private List<String> leftListVData;
    private List<List<String>> rightListVData;
    /**
     * 标识 当前 是否 正在 查询
     */
    public Boolean isQuerying = false;
    public ImageView queryResultListImg;
    public ProgressBar progressBar;
    /**
     * 点击时 显示 的 属性字段 的 名称
     */
    public String showField;

    /**
     * 左右边选中的位置
     */
    int leftSelectedPos = -1, rightSelectedPos = -1;

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
     * 第一次点击 时 矩形 的 中心点 屏幕坐标
     */
    private PointF centerPoint;
    /**
     * 第一次点击 时 矩形 的 中心点 地理坐标
     */
    private Dot centerDot;
    /**
     * 自定义 的 拉伸范围
     */
    private SelfView extendView;
    /**
     * 方形查询范围 最大 半边长
     */
    private final int maxRadius = 200;
    /**
     * 方形查询范围 最小 半边长
     */
    private final int minRadius = 50;

    int[] icons = {R.drawable.icon_marka, R.drawable.icon_markb, R.drawable.icon_markc, R.drawable.icon_markd, R.drawable.icon_marke,
            R.drawable.icon_markf, R.drawable.icon_markg, R.drawable.icon_markh, R.drawable.icon_marki, R.drawable.icon_markj};

    public VaguePointQueryMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onActivityResult(int resultCode, Intent intent) {
        switch (resultCode) {
            case ResultCode.RESULT_PIPE_LOCATE:
                if (leftSelectedPos != -1 && rightSelectedPos != -1) {
                    int locateIndex = 0;

                    for (int i = 0; i < leftSelectedPos; i++) {
                        locateIndex += rightListVData.get(i).size();
                    }

                    locateIndex = locateIndex + rightSelectedPos;

                    mmtAnnotationListener.clickWhichIndex = locateIndex;
                }

                for (int i = 0; i < mapView.getAnnotationLayer().getAnnotationCount(); i++) {
                    Annotation annotation = mapView.getAnnotationLayer().getAnnotation(i);
                    if (mmtAnnotationListener.clickWhichIndex == i) {
                        annotation.setImage(BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_gcoding));
                        annotation.showAnnotationView();
                        mmtAnnotationListener.clickWhichIndex = mmtAnnotationListener.clickWhichIndex;
                    } else {
                        annotation.setImage(BitmapFactory.decodeResource(mapGISFrame.getResources(),
                                i >= icons.length ? R.drawable.icon_mark_pt : icons[i]));
                    }
                }

                break;
            case ResultCode.RESULT_PIPE_GOBACK:
                handler.sendEmptyMessage(5);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected() {
//		if (mapView == null || mapView.getMap() == null) {
//			mapGISFrame.stopMenuFunction();
//			return false;
//		}
//
//		leftListVData = new ArrayList<String>();
//		rightListVData = new ArrayList<List<String>>();
//
//		initSplitListViewFragment();
//
//		listenerList = new ArrayList<Graphic>();
//		mmtAnnotationListener = new MmtAnnotationListener();
//
//		mapView.setAnnotationListener(mmtAnnotationListener);
//
//		((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvPlanName)).setText("模糊点击查询");
//
//		((ImageButton) mapGISFrame.getCustomView().findViewById(R.id.tvPlanBack)).setOnClickListener(backImgBtnClickListener);
//
//		mapView.setTapListener(tapListener);
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

    private final MapView.MapViewTapListener tapListener = new MapView.MapViewTapListener() {
        @Override
        public void mapViewTap(PointF tapPointF) {

            if (mmtAnnotationListener.getHideTapListener()) {
                mmtAnnotationListener.setHideTapListener(false);
                return;
            }

            // 2015.1.5 by WL 模糊查询如果查询的数据过多，点击地图后在未查出结果之前再点击地图切换查询位置，则系统闪退
            if (isQuerying) {
                return;
            }

            mmtAnnotationListener.clickWhichIndex = -1;

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
                    // lp2.setMargins((int)event.getRawX() - bitmap.getWidth()/2
                    // , (int)centerPoint.y - bitmap.getHeight()/2 , 0, 0);
                    lp2.setMargins((int) (v.getX() + event.getX()) - bitmap.getWidth() / 2, (int) centerPoint.y - bitmap.getHeight() / 2,
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

    private final VaguePointQueryTask.onSearchListener onSearchListener = new VaguePointQueryTask.onSearchListener() {

        @Override
        public void onUpdate(String layerName, ArrayList<Graphic> graphicList, int currentCount) {
            Message msg = Message.obtain(handler);
            msg.what = 1;
            msg.obj = graphicList;
            Bundle b = new Bundle();
            b.putString("layerName", layerName);
            b.putInt("currentCount", currentCount);
            msg.setData(b);
            handler.sendMessage(msg);
        }

        @Override
        public void onComplete(int totalCount) {
            Message msg = Message.obtain(handler);
            msg.what = 0;
            msg.obj = totalCount;
            handler.sendMessage(msg);
        }

        @Override
        public void onUpdate2(String layerName, FeaturePagedResult featurePagedResult, int currentCount) {
            Message msg = Message.obtain(handler);
            msg.what = 1;
            msg.obj = featurePagedResult;
            Bundle b = new Bundle();
            b.putString("layerName", layerName);
            b.putInt("currentCount", currentCount);
            msg.setData(b);
            handler.sendMessage(msg);
        }
    };

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 5:
                    splitListViewFragment.show(mapGISFrame.getSupportFragmentManager().beginTransaction(), "");
                    break;
                case 0: // 0 表示 所有图层 查询 完毕
                    final String totalCount = msg.obj.toString();
                    new AsyncTask<Integer, Integer, Integer>() {
                        @Override
                        protected Integer doInBackground(Integer... params) {
                            for (String one : resultLinkedHashMap.keySet()) {
                                ArrayList<Graphic> graphicArr = resultLinkedHashMap.get(one);
                                leftListVData.add(one + "(" + graphicArr.size() + ")");
                                ArrayList<String> stringArr = new ArrayList<String>();
                                for (Graphic item : graphicArr) {
                                    String myShowField = LayerConfig.getInstance().getConfigInfo(one).HighlightField;

                                    if (BaseClassUtil.isNullOrEmptyString(showField)) {// 未设置高亮字段则显示前3个属性
                                        stringArr.add(getGraphicInfo(item));
                                    } else {// 设置高亮字段则显示高亮字段
                                        stringArr.add(myShowField + ":" + item.getAttributeValue(myShowField));
                                    }
                                }
                                rightListVData.add(stringArr);
                            }

                            int size = mapView.getAnnotationLayer().getAnnotationCount() > 10 ? 10 : mapView.getAnnotationLayer()
                                    .getAnnotationCount();
                            for (int i = 0; i < size; i++) {
                                mapView.getAnnotationLayer().getAnnotation(i)
                                        .setImage(BitmapFactory.decodeResource(mapGISFrame.getResources(), icons[i]));
                            }

                            mapView.refresh();

                            return leftListVData.size();
                        }

                        @Override
                        protected void onPostExecute(Integer result) {
                            super.onPostExecute(result);

                            ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvPlanName)).setText("模糊点击查询  完成");
                            ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvTaskState)).setText("共  " + resultLinkedHashMap.size()
                                    + "个图层   " + totalCount + "个设备");
                            progressBar.setVisibility(View.INVISIBLE);
                            queryResultListImg.setVisibility(View.VISIBLE);
                            // 查询 结束 后 进行
                            isQuerying = false;
                        }
                    }.executeOnExecutor(MyApplication.executorService);
                    break;
                case 1: // 1 表示 查询完 某一图层 后 更新 UI
                    Bundle b = msg.getData();
                    int currentCount = b.getInt("currentCount");

                    @SuppressWarnings("unchecked")
                    ArrayList<Graphic> graphicList = new ArrayList<Graphic>((ArrayList<Graphic>) msg.obj);

                    resultLinkedHashMap.put(b.getString("layerName"), graphicList);
                    listenerList.addAll(graphicList);

                    if (graphicList.size() > 0) {
                        showGraphicsOnMap(graphicList, b.getString("layerName"), false);
                    }
                    graphicList = null;
                    ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvTaskState)).setText("当前  " + resultLinkedHashMap.size()
                            + "个图层   " + currentCount + "个设备");
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 将从图层上模糊查询到 设备信息显示在地图界面上
     *
     * @param graphics  需要显示的图形信息集合
     * @param layerName 该图形信息所属的图层名称
     */
    private void showGraphicsOnMap(List<Graphic> graphics, String layerName, Boolean refresh) {
        if (refresh) {
            mapView.getAnnotationLayer().removeAllAnnotations();
        }

        // 高亮显示的属性
        showField = LayerConfig.getInstance().getConfigInfo(layerName).HighlightField;

        for (int i = 0; i < graphics.size(); i++) {
            Graphic graphic = graphics.get(i);

            MmtAnnotation annotation = new MmtAnnotation(graphic, layerName, BaseClassUtil.isNullOrEmptyString(showField) ? ""
                    : graphic.getAttributeValue(showField), graphic.getCenterPoint(), BitmapFactory.decodeResource(
                    mapGISFrame.getResources(), R.drawable.icon_mark_pt));

            mapView.getAnnotationLayer().addAnnotation(annotation);
        }
        mapView.refresh();
    }

    private void initSplitListViewFragment() {
        splitListViewFragment = new SplitListViewFragment("查询结果", "图层", "设备信息", leftListVData, rightListVData);
        splitListViewFragment.setRightListItemSingleLine(true);
        splitListViewFragment.setCancelable(false);
        splitListViewFragment.setLeftLayoutWeight(4);
        splitListViewFragment.setRightLayoutWeight(6);
        splitListViewFragment.setSplitListViewPositiveClick(new SplitListViewFragment.SplitListViewPositiveClick() {
            @Override
            public void onSplitListViewPositiveClick(String leftListValue, String rightListValue, int leftPos, int rightPos) {
                leftSelectedPos = leftPos;
                rightSelectedPos = rightPos;

                String layerName = leftListValue.substring(0, leftListValue.indexOf("("));
                Graphic itemClickGraphic = resultLinkedHashMap.get(layerName).get(rightPos);
                HashMap<String, String> graphicMap = new LinkedHashMap<String, String>();
                for (int m = 0; m < itemClickGraphic.getAttributeNum(); m++) {
                    graphicMap.put(itemClickGraphic.getAttributeName(m), itemClickGraphic.getAttributeValue(m));
                }

                Intent intent = new Intent(mapGISFrame, PipeDetailActivity.class);

                intent.putExtra("layerName", layerName);
                intent.putExtra("isSetResult", true);
                intent.putExtra("graphic", itemClickGraphic);
                intent.putExtra("graphicMap", graphicMap);
                intent.putExtra("graphicMapStr", new Gson().toJson(graphicMap));
                mapGISFrame.startActivityForResult(intent, 0);
            }
        });
    }

    private String getGraphicInfo(Graphic graphic) {

        StringBuilder strBuilder = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            String key = graphic.getAttributeName(i);
            String value = graphic.getAttributeValue(i);

            // edit by wangLin 图层不含有该信息时，会留白
            // if (key.equals(showField) || key.equals("OID")) {
            strBuilder.append(key + ":" + value + ";");
            // }

        }
        return strBuilder.toString();
    }

    private void startQuery() {
        try {
            mapView.refresh();

            isQuerying = true;

            if (resultLinkedHashMap != null) {
                resultLinkedHashMap = null;
            }
            resultLinkedHashMap = new LinkedHashMap<String, ArrayList<Graphic>>();
            leftListVData.clear();
            rightListVData.clear();
            listenerList.clear();

            progressBar.setVisibility(View.VISIBLE);
            queryResultListImg.setVisibility(View.INVISIBLE);

            View tvPlanName = mapGISFrame.getCustomView().findViewById(R.id.tvPlanName);

            if (tvPlanName != null && tvPlanName instanceof TextView) {
                ((TextView) tvPlanName).setText("正在进行 模糊点击查询");
            }

            ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvTaskState)).setText("");

            mapView.getAnnotationLayer().removeAllAnnotations();

            Rect searchExtent = new Rect();
            searchExtent.setXMin(leftUpDot.x < rightBottom.x ? leftUpDot.x : rightBottom.x);
            searchExtent.setYMin(leftUpDot.y < rightBottom.y ? leftUpDot.y : rightBottom.y);
            searchExtent.setXMax(leftUpDot.x > rightBottom.x ? leftUpDot.x : rightBottom.x);
            searchExtent.setYMax(leftUpDot.y > rightBottom.y ? leftUpDot.y : rightBottom.y);

            VaguePointQueryTask queryTask = new VaguePointQueryTask(mapView, onSearchListener, searchExtent);
            MyApplication.getInstance().submitExecutorService(queryTask);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
