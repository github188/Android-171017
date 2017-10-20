package com.mapgis.mmt.module.gis.toolbar.accident2.view;

import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Comclay on 2017/3/1.
 * 爆管分析结果界面的View接口
 */

public interface IAnalysisResultView {

    void findView(View view);

    /**
     * 扩大关阀
     */
    void expandCloseValve();

    /**
     * 重选爆管点
     */
    void reselectBrokenPoint();

    LayoutInflater getLayoutInflater();

    /**
     * 显示导出excel对话框
     */
    void showExportDialog();

    /**
     * 隐藏导出excel对话框
     */
    void hidenExportDialog();

    /**
     * 查看Excel
     * @param fileName 文件名称
     */
    void viewExportXls(String fileName);

    void resultInvalidateValve();

    /**
     * 将地图界面调到前面来
     */
    void startMapViewToFront();

    void showToast(String msg);

    void onBackMapView();

    void post(Runnable runnable);

    void showLoadingDialog();

    void hidenLoadingDialog();
}
