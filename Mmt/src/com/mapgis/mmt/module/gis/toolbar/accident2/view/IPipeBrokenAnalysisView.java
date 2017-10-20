package com.mapgis.mmt.module.gis.toolbar.accident2.view;

import android.graphics.Bitmap;

import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

/**
 * 爆管分析界面的View接口
 * Created by Comclay on 2017/3/1.
 */

public interface IPipeBrokenAnalysisView {
    /**
     * 判断是否加载地图
     *
     * @return true已经加载，false未加载
     */
    boolean isLoadMapView();

    MapView getMapView();

    void clearMapView();

    /**
     * 保守关阀
     *
     * @return true保守关阀，false关闭保守关阀
     */
    boolean isCloseValve();

    /**
     * 是否允许开启已关闭阀门
     *
     * @return true允许，false不允许
     */
    boolean isAllowCloseValve();

    /**
     * 绘制爆管点
     *
     * @param dot 爆管点坐标
     */
    void drawBrokenPoint(Dot dot);

    /**
     * 显示圆形进度条，并隐藏爆管结果查看按钮
     */
    void showProgress();

    /**
     * 隐藏圆形进度条，并显示爆管结果查看按钮
     */
    void hidenProgress();

    /**
     * 同时隐藏进度条和结果按钮
     */
    void hidenIvAndProgress();

    /**
     * 获取爆管点坐标
     */
    Dot getBrokenPoint();

    void setBrokenPoint(Dot brokenPoint);

    Rect getRect();

    void setRect(Rect rect);

    /**
     * 判断爆管点是否在地图范围内
     */
    boolean isEffective(Dot dot);

    Rect getMapRange();

    void showBottomView();

    void hidenBottomView();

    /**
     * 重置爆管分析功能，即重新选择爆管点
     */
    void resetBrokenFunction();

    void showToast(String msg);

    void analysisSuccess(FeatureMetaGroup[] featureMetaArray);

    void showResultOnMap(FeatureMetaGroup[] featureMetaArray);

    void analysisFailed(String msg);

    /**
     * 进入分析结果界面
     */
    void enterResultActivity();

    /**
     * 用于子线程与UI线程之间的交互接口
     *
     * @param runnable
     */
    void post(Runnable runnable);

    void toDetailActivity(String layerName, String mapData);

    void setAnnotationListener();

    Bitmap getBitmap(String type);

    /**
     * 失效关阀
     */
    String getInvalidateValve();

    void setBack(boolean isBack);
}

