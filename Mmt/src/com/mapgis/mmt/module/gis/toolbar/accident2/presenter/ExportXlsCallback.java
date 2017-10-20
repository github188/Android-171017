package com.mapgis.mmt.module.gis.toolbar.accident2.presenter;

/**
 * Created by Comclay on 2017/3/8.
 * 导出Excel文件的回调接口
 */

public abstract class ExportXlsCallback {
    public abstract void onExportStart() ;

    public void onExportSuccess(String fileName) {
        onExportFinish();
    }

    public void onStartDownload() {

    }

    public void onExportProcess(int process) {

    }

    public void onExportFaild() {
        onExportFinish();
    }

    public void onExportFinish() {

    }
}
