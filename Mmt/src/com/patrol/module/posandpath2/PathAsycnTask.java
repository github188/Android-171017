package com.patrol.module.posandpath2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.patrol.module.posandpath2.beans.PathBean;
import com.patrol.module.posandpath2.beans.PointInfo;
import com.patrol.module.posandpath2.detailinfo.DetailInfoActivity;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationLayer;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.graphic.GraphicLayer;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;
import com.zondy.mapgis.geometry.Rect;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Comclay on 2016/10/21.
 * 显示今日轨迹的异步任务
 * <p>
 * 需要传入日期参数,以便查看其当天轨迹
 */

class PathAsycnTask extends AsyncTask<String, Void, String> {
    // 坐标中与上一个位置的距离小于MAX_DIST就不显示
    private static final double MAX_DIST = 0.0f;
    // 显示坐标的标注的地图最小缩放等级
    private final float DEFAULT_ZOOM = 7.0f;
    private String id;
    private String mTime;
    private double mPatrolDistance = 0.00f;
    private boolean isShowPoint = false;
    private boolean isSuccess = false;
    private float mLastDrawLevel;
    private Bitmap mStartBitmap;
    private Bitmap mCenterBitmap;
    private Bitmap mEndBitmap;
    private Rect mRect;
    private MapView mapView;
    private MapGISFrame mapGisFrame;
    private Annotation mStartAnnotation;
    private Annotation mEndAnnotation;
    private GraphicLayer mGraphicLineLayer;
    private List<PointInfo> mPointList;
    private List<Annotation> annotationList;
    private PatrolPathListener mPatrolPathListener;

    private final static int[] resID = {
            R.drawable.icon_track_navi_end,
//            R.drawable.point_trace_start,
            R.drawable.yuandian,
            R.drawable.icon_track_navi_start
//            R.drawable.point_trace_end
    };

    interface PatrolPathListener {
        void onPreTask();

        void onBackTask();

        void onPostTask();
    }

    PathAsycnTask(MapGISFrame mapGisFrame, String id, PatrolPathListener listener) {
        this.mapGisFrame = mapGisFrame;
        this.mapView = mapGisFrame.getMapView();
        this.id = id;
        this.mPatrolPathListener = listener;
    }

    String getmTime() {
        return mTime;
    }

    String getId() {
        return id;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    protected void onPreExecute() {
        if (mPatrolPathListener != null) {
            mPatrolPathListener.onPreTask();
        }

        if (mGraphicLineLayer == null) {
            mGraphicLineLayer = new GraphicLayer();
            mapView.getGraphicLayers().add(mGraphicLineLayer);
        }

        clearMapView();
        mapView.refresh();
    }

    @Override
    protected String doInBackground(String... params) {
        if (params.length == 0 || BaseClassUtil.isNullOrEmptyString(params[0])) {
            // 如果时间为空,就默认显示今日时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            mTime = sdf.format(new Date());
        } else {
            mTime = params[0];
        }
        String url = getUrl(mTime);
        String result = NetUtil.executeHttpGet(url);
        if (mPatrolPathListener != null) {
            mPatrolPathListener.onBackTask();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            if (BaseClassUtil.isNullOrEmptyString(result)) {
                mapGisFrame.showToast("网络异常");
                throw new Exception("网络异常");
            }

            PathBean bean = new Gson().fromJson(result, PathBean.class);
            if (!"true".equals(bean.rntinfo.IsSuccess)
                    || BaseClassUtil.isNullOrEmptyString(bean.Ppoint.get(0).Ppoint)) {
                mapGisFrame.showToast("请求轨迹失败");
                throw new Exception("请求轨迹失败");
            }
            PathBean.Path path = bean.Ppoint.get(0);
            String pointInfos = path.Ppoint;

            mPointList = PathBean.stringToPointList(path.PerName, pointInfos);
            if (mPointList == null || mPointList.size() == 0) {
                mapGisFrame.showToast("轨迹信息为空");
                throw new Exception("轨迹信息为空");
            }

            initData();
            showNotCollectPath();

            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mPatrolPathListener != null) {
            mPatrolPathListener.onPostTask();
        }
    }

    private void initData() {
        try {
            mStartBitmap = BitmapFactory.decodeResource(mapGisFrame.getResources(), resID[0]);
            mCenterBitmap = BitmapFactory.decodeResource(mapGisFrame.getResources(), resID[1]);
            mEndBitmap = BitmapFactory.decodeResource(mapGisFrame.getResources(), resID[2]);
            annotationList = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置监听器
     */

    private void setMapListener() {
        mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
            @Override
            public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
                super.mapViewClickAnnotationView(mapview, annotationview);
                Annotation annotation = annotationview.getAnnotation();
                if (annotation instanceof DataBindAnnotation) {
                    DataBindAnnotation dataBindAnnotation = (DataBindAnnotation) annotation;
                    Parcelable parcelable = dataBindAnnotation.getT();
                    if (parcelable instanceof PointInfo) {
                        enterPointDetailActivity((PointInfo) parcelable);
                    }
                }
            }

            @Override
            public AnnotationView mapViewViewForAnnotation(MapView mapview, Annotation annotation) {
                AnnotationView annotationView = super.mapViewViewForAnnotation(mapview, annotation);
                annotationView.setPanToMapViewCenter(false);
                return annotationView;
            }
        });

        // 监听缩放的事件
        mapView.setZoomChangedListener(new MapView.MapViewZoomChangedListener() {
            @Override
            public void mapViewZoomChanged(MapView mapView, float v, float v1) {
                if (annotationList == null || annotationList.size() == 0) return;
                if (mLastDrawLevel >= DEFAULT_ZOOM && v1 < DEFAULT_ZOOM) {
                    isShowPoint = false;
                } else if (mLastDrawLevel < DEFAULT_ZOOM && v1 >= DEFAULT_ZOOM) {
                    isShowPoint = true;
                } else {
                    return;
                }
                mLastDrawLevel = v1;
                mapGisFrame.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AnnotationLayer annotationLayer = PathAsycnTask.this.mapView.getAnnotationLayer();
                        if (isShowPoint) {
                            annotationList.clear();
                            annotationList = getAnnotationList();
                            annotationLayer.addAnnotations(annotationList);
                            annotationLayer.moveAnnotation(annotationLayer.getAllAnnotations().indexOf(mEndAnnotation), -1);
                            annotationLayer.moveAnnotation(annotationLayer.getAllAnnotations().indexOf(mStartAnnotation), -1);
                        } else {
                            annotationLayer.removeAnnotations(annotationList);
                        }
                        PathAsycnTask.this.mapView.refresh();
                    }
                });
            }
        });
    }

    /**
     * 显示没有聚集的轨迹
     */
    private void showNotCollectPath() {
        if (mPointList == null || mPointList.size() == 0) {
            return;
        }
        Dots dots = new Dots();
        Annotation annotation;

        Rect rect = mapView.getMap().getEntireRange();
        mRect = new Rect(rect.getXMax(), rect.getYMax(), rect.getXMin(), rect.getYMin());

        Dot dot;
        Dot preDot;
        double distance;
        for (int i = 0; i < mPointList.size(); i++) {
            dot = convertToDot(mPointList.get(i).getPosition());
            preDot = dots.get(dots.size() - 1);
            if (i != 0) {
                // 与之前的点距离小于MAX_DIST就不显示这个点
                distance = Math.sqrt(Math.pow(dot.getX() - preDot.getX(), 2) + Math.pow(dot.getY() - preDot.getY(), 2));
                mPatrolDistance += distance;
                if (distance < MAX_DIST) {
                    continue;
                }
            }

            dots.append(dot);

            if (dot.getX() < mRect.getXMin()) mRect.setXMin(dot.getX());
            if (dot.getX() > mRect.getXMax()) mRect.setXMax(dot.getX());
            if (dot.getY() < mRect.getYMin()) mRect.setYMin(dot.getY());
            if (dot.getY() > mRect.getYMax()) mRect.setYMax(dot.getY());

            if (i == 0 || i >= mPointList.size() - 1) continue;

            annotation = createAnnotation(mPointList.get(i), mCenterBitmap);
            annotation.setCenterOffset(new Point(0, -mCenterBitmap.getHeight() / 2));
            annotationList.add(annotation);
        }
        mapView.zoomToRange(mRect, false);
        mLastDrawLevel = mapView.getZoom();
        // 轨迹
        showPath(dots);
        if (mapView.getZoom() >= DEFAULT_ZOOM) {
            mapView.getAnnotationLayer().addAnnotations(annotationList);
        }
        // 结束点
        mEndAnnotation = createAnnotation(mPointList.get(mPointList.size() - 1), mEndBitmap);
        mapView.getAnnotationLayer().addAnnotation(mEndAnnotation);
        // 开始点
        mStartAnnotation = createAnnotation(mPointList.get(0), mStartBitmap);
        mapView.getAnnotationLayer().addAnnotation(mStartAnnotation);

        setMapListener();
        mapView.refresh();
    }

    public List<Annotation> getAnnotationList() {
        List<Annotation> annotationList = new ArrayList<>();
        Annotation annotation;
        for (int i = 0; i < mPointList.size(); i++) {
            if (i == 0 || i >= mPointList.size() - 1) continue;

            annotation = createAnnotation(mPointList.get(i), mCenterBitmap);
            annotation.setCenterOffset(new Point(0, -mCenterBitmap.getHeight() / 2));
            annotationList.add(annotation);
        }
        return annotationList;
    }

    /**
     * 轨迹
     *
     * @param dots 坐标集合
     */
    private void showPath(Dots dots) {
        GraphicPolylin polylin = new GraphicPolylin(dots);
        polylin.setLineWidth(6F);
        polylin.setIsDisposable(true);
        polylin.setColor(mapGisFrame.getResources().getColor(R.color.color_dd462c));
        mGraphicLineLayer.addGraphic(polylin);
    }

    void showReset() {
        // 设置地图显示去
        mapView.zoomToRange(mRect, true);
    }

    private void enterPointDetailActivity(PointInfo pointInfo) {
        Intent intent = new Intent(mapGisFrame, DetailInfoActivity.class);
        intent.putExtra(DetailInfoActivity.ARG_MAP_OBJECT, pointInfo);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mapGisFrame.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(mapGisFrame);
    }

    String getSpan() {
        String str = "- 到 -";
        if (mPointList == null || mPointList.size() == 0) {
            return str;
        }
        int size = mPointList.size();

        String startTime = mPointList.get(0).getTime();

        String endTime = mPointList.get(size - 1).getTime();

        return String.format("%s 到 %s", formatTime(startTime), formatTime(endTime));
    }

    String getPatrolDistance(){
        double distKM = mPatrolDistance / 1000;
        if (mPatrolDistance < 0.01f){
            return "总里程：0公里";
        }
        DecimalFormat df = new DecimalFormat("总里程：#.00公里");
        return df.format(distKM);
    }

    @Nullable
    private String formatTime(String endTime) {
        if (!"-".equals(endTime) || !BaseClassUtil.isNullOrEmptyString(endTime)) {
            endTime = endTime.split(" ")[1];
        }
        return endTime;
    }

    /**
     * 清空地图
     */
    void clearMapView() {
        mapView.setTapListener(null);
        mapView.setZoomChangedListener(null);
        mapView.getAnnotationLayer().removeAllAnnotations();
        mapView.getGraphicLayer().removeAllGraphics();

        if (mGraphicLineLayer != null) {
            mGraphicLineLayer.removeAllGraphics();
        }
    }

    private Annotation createAnnotation(PointInfo pointInfo, Bitmap bitmap) {
        return new DataBindAnnotation<>(
                pointInfo/*.toMapData()*/
                , pointInfo.getTime()
                , ""
                , convertToDot(pointInfo.getPosition())
                , bitmap);
    }

    /**
     * 将字符串表示的坐标转换成Dot对象
     */
    private Dot convertToDot(String position) {
        String[] xy = position.split(",");
        return new Dot(Double.valueOf(xy[0]), Double.valueOf(xy[1]));
    }

    /**
     * 拼接出URL
     *
     * @param time 某一天的轨迹
     * @return url
     */
    private String getUrl(String time) {
        StringBuilder sb = new StringBuilder();
        sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                .append("/services/zondy_mapgiscitysvr_newpatrol/rest/newpatrolrest.svc/GetPerHisPosition?")
                .append("xmin=0")
                .append("&xmax=0")
                .append("&ymin=0")
                .append("&ymax=0")
                .append("&IDList=").append(id)
                .append("&OnlyWorkTime=false")
                .append("&STTime=").append(time + " 00:00:00")
                .append("&ENDTime=").append(time + " 23:59:59")
                .append("&f=json")
                .append("&OptimizeTrace=true")
                .append("&time=").append(new Date().toString());

        return sb.toString();
    }
}
