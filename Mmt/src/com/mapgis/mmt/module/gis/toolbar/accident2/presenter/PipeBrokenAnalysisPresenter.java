package com.mapgis.mmt.module.gis.toolbar.accident2.presenter;

import android.util.Log;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaItem;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IFeatureItemModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IFeatureMetaModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.impl.FeatureMetaModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.util.MetaType;
import com.mapgis.mmt.module.gis.toolbar.accident2.view.IPipeBrokenAnalysisView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.Request;

/**
 * 爆管分析界面中ui与数据的交互控制类
 * Created by Comclay on 2017/3/1.
 */

public class PipeBrokenAnalysisPresenter {
    private static final String TAG = "PipeBrokenAnalysisPrese";

    private IPipeBrokenAnalysisView mPipeBrokenAnalysisView;
    // 爆管分析时数据量较大会导致activity间传输数据失败，所以改为静态的方式共享
    private static IFeatureMetaModel mFeatureMetaModel;

    public PipeBrokenAnalysisPresenter(IPipeBrokenAnalysisView mPipeBrokenAnalysisView) {
        this.mPipeBrokenAnalysisView = mPipeBrokenAnalysisView;
        mFeatureMetaModel = new FeatureMetaModel();
    }

    public static IFeatureMetaModel getFeatureMetaModel(){
        return mFeatureMetaModel;
    }

    public static void clearMetaModel(){
        mFeatureMetaModel.cancelBrokenAnalysis();
        mFeatureMetaModel = null;
    }

    /**
     * 开始进行爆管分析
     */
    public void startPipeBrokenAnalysis(Dot dot) {
        String url = getPipeBrokenAnalysisUrl(mPipeBrokenAnalysisView.getInvalidateValve());
        mFeatureMetaModel.pipeBrokenAnalysis(url
                , new IFeatureMetaModel.AnalysisCallback<FeatureMetaGroup[]>() {
                    @Override
                    public void onAnalysisSuccess(FeatureMetaGroup[] featureMetaGroups) {
                        // 爆管分析成功
                        mPipeBrokenAnalysisView.analysisSuccess(featureMetaGroups);
                        mPipeBrokenAnalysisView.setAnnotationListener();

                        loadFeatureItemData();
                    }

                    @Override
                    public void onAnalysisFailed(String msg) {
                        // 爆管分析失败
                        mPipeBrokenAnalysisView.analysisFailed(msg);
                    }
                });
    }

    /**
     * 获取默认可见设备的详细信息
     * 且item数据封装在FeatureMetaGroup中返回
     */
    private void loadFeatureItemData() {
        this.mPipeBrokenAnalysisView.clearMapView();
        mFeatureMetaModel.loadFeatureData(new IFeatureItemModel.LoadCallback() {
            @Override
            public void onLoadSuccess(FeatureMetaGroup group, int index) {
                Log.i(TAG, "数据请求成功: " + group.toString() + "[" + index + "]" + group.getResultList().get(index).toString());
                showFeatrueMetaItem(group.getResultList().get(index)
                        , group.getCivFeatureMetaType());
            }

            @Override
            public void onLoadFailed(String msg) {
                Log.i(TAG, "数据请求失败");
            }
        });
    }

    /**
     * 显示一组数据
     *
     * @param group FeatureMetaGroup
     */
    public void showFeatrueMetaGroup(FeatureMetaGroup group) {
        for (FeatureMetaItem item : group.getResultList()) {
            InteractionRunnable runnable = new InteractionRunnable(PipeBrokenAnalysisPresenter.this.mPipeBrokenAnalysisView
                    , group.getCivFeatureMetaType(), item);
            if (MetaType.TYPE_PIPE_LINE.equals(group.getCivFeatureMetaType())) {
                runnable.setShowPipeAnnotation(true);
            }
            runnable.run();
        }
    }

    /**
     * 显示一项数据
     *
     * @param featureMetaItem FeatureMetaItem
     * @param type            类型
     */
    public void showFeatrueMetaItem(FeatureMetaItem featureMetaItem, String type) {
        Runnable runnable = new InteractionRunnable(
                PipeBrokenAnalysisPresenter.this.mPipeBrokenAnalysisView
                , type, featureMetaItem);
        PipeBrokenAnalysisPresenter.this.mPipeBrokenAnalysisView.post(runnable);
    }

    /**
     * 获取爆管分析结果
     */
    public FeatureMetaGroup[] getAnalysisResult() {
        return mFeatureMetaModel.getFeatureMetaArray();
    }

    /**
     * 停止爆管分析
     */
    public void stopPipeBrokenAnalysis() {
        clearMetaModel();
//        mFeatureMetaModel.cancelBrokenAnalysis();
    }

    private Request getPipeBrokenAnalysisRequest(String invalidateValves) {
        int isOpenFlg = mPipeBrokenAnalysisView.isAllowCloseValve() ? 0 : 1;
        boolean isexactAcc = !mPipeBrokenAnalysisView.isCloseValve();

        // 屏幕的像素
        String imageDisplay = getImageDisplay();
        String mapExtent = getMapExtent();
        String geometry = getGeometry();

        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/rest/services/GPServer.svc/" + MobileConfig.MapConfigInstance.VectorService
                + "/IncidentOperNew";
        Request.Builder builder = new Request.Builder();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("geometryType", "Point")
                .add("isOpenFlg", String.valueOf(isOpenFlg))
                .add("tolerance", "10")
                .add("isexactAcc", String.valueOf(isexactAcc))
                .add("imageDisplay", String.valueOf(imageDisplay))
                .add("_ts", String.valueOf(System.currentTimeMillis()))
                .add("mapExtent", mapExtent)
                .add("geometry", geometry)
                .add("invalidateValves", invalidateValves)
                .add("barrier", "");
        return builder.url(url).post(formBuilder.build()).build();
    }

    private String getImageDisplay() {
        int hpx = MyApplication.getInstance().getResources().getDisplayMetrics().heightPixels;
        int wpx = MyApplication.getInstance().getResources().getDisplayMetrics().widthPixels;
        float scale = DimenTool.getScale(MyApplication.getInstance().mapGISFrame);
        return String.format(Locale.CHINA, "%d,%d,%d", hpx, wpx, (int) scale);
    }

    /**
     * 拼接爆管分析服务接口
     */
    private String getPipeBrokenAnalysisUrl(String invalidateValves) {
        int isOpenFlg = mPipeBrokenAnalysisView.isAllowCloseValve() ? 0 : 1;
        boolean isexactAcc = !mPipeBrokenAnalysisView.isCloseValve();

        // 屏幕的像素
        String imageDisplay = getImageDisplay();
        String mapExtent = getMapExtent();
        String geometry = getGeometry();

        // 爆管分析查询的URL地址
        StringBuilder sb = new StringBuilder();
        sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                .append("/rest/services/GPServer.svc/")
                .append(MobileConfig.MapConfigInstance.VectorService)
                .append("/IncidentOperNew?")
                .append("geometryType=Point")
                .append("&isOpenFlg=").append(isOpenFlg)
                .append("&tolerance=10")
                .append("&isexactAcc=").append(isexactAcc)
                .append("&imageDisplay=").append(imageDisplay)
                .append("&_ts=").append(System.currentTimeMillis())
                .append("&mapExtent=").append(mapExtent)
                .append("&barrier=")
                .append("&geometry=").append(geometry)
                .append("&invalidateValves=").append(invalidateValves);

        Log.i(TAG, "爆管分析：" + sb.toString());
        return sb.toString();
    }

    /**
     * {"xmax":83041.98608356617,"ymin":74186.90364592329,"ymax":74282.94758801117,"xmin":82770.91991643382,"spatialReference":{"wkid":1}}
     */
    private String getMapExtent() {
        Dot brokenPoint = mPipeBrokenAnalysisView.getBrokenPoint();
//        MapView mapView = MyApplication.getInstance().mapGISFrame.getMapView();
//        PointF pointF = mapView.mapPointToViewPoint(brokenPoint);
//        pointF.set(pointF.x + 100, pointF.y + 100);
//        Dot dot = mapView.viewPointToMapPoint(pointF);
//        double sqrt = Math.sqrt(Math.pow(brokenPoint.getX() - dot.getX(), 2)+ Math.pow(brokenPoint.getY() - dot.getY(), 2));
        int sqrt = 50;
        Rect mapRange = new Rect(brokenPoint.getX() - sqrt, brokenPoint.getY()
                - sqrt, brokenPoint.getX() + sqrt, brokenPoint.getY() + sqrt);
//        Rect mapRange = MyApplication.getInstance().mapGISFrame.getMapView().getDispRange();
        Log.i(TAG, "getMapExtent: " + mapRange.toString());
        return String.format(Locale.CHINA, "{\"xmax\":%f,\"ymin\":%f,\"ymax\":%f,\"xmin\":%f,\"spatialReference\":{\"wkid\":1}}"
                , mapRange.getXMax(), mapRange.getYMin(), mapRange.getYMax(), mapRange.getXMin());
    }

    /**
     * {"x":82898.31704622808,"spatialReference":{"wkid":1},"y":74222.22559156717}
     */
    private String getGeometry() {
        Dot brokenPoint = mPipeBrokenAnalysisView.getBrokenPoint();
        return String.format(Locale.CHINA, "{\"x\":%f,\"spatialReference\":{\"wkid\":1},\"y\":%f}"
                , brokenPoint.getX(), brokenPoint.getY());
    }
}
