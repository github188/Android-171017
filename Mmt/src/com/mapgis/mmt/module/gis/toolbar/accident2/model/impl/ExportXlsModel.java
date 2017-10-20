package com.mapgis.mmt.module.gis.toolbar.accident2.model.impl;

import android.os.AsyncTask;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IExportXlsModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.presenter.ExportXlsCallback;

import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;

/**
 * Created by Comclay on 2017/3/8.
 * 导出excel模块
 */

public class ExportXlsModel implements IExportXlsModel {
    // 文件名称
    private String fileName;
    private ExportXlsCallback callback;

    public ExportXlsModel(ExportXlsCallback callback) {
        this.callback = callback;
    }

    @Override
    public void buildXlsTask(final String url, final String serverPath) {
        new AsyncTask<String, Void, String>() {

            @Override
            protected void onPreExecute() {
                callback.onExportStart();
            }

            @Override
            protected String doInBackground(String... params) {
                return NetUtil.executeHttpGet(params[0]);
            }

            @Override
            protected void onPostExecute(String s) {
                try {
                    if (BaseClassUtil.isNullOrEmptyString(s)) {
                        callback.onExportFaild();
                        return;
                    }

                    JSONObject jsonObject = new JSONObject(s);
                    fileName = (String) jsonObject.get("fileName");

                    if (BaseClassUtil.isNullOrEmptyString(fileName)) {
                        callback.onExportFaild();
                    } else {
                        // 开始下载
                        String fixedPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Temp, true);
                        String localPath = fixedPath + fileName;
                        downLoadXls(serverPath + URLEncoder.encode(fileName, "UTF-8"), localPath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onExportFaild();
                }
            }
        }.execute(url);
    }

    /**
     * 下载xls文件
     */
    private void downLoadXls(final String serverFile, final String localFile) {
        new AsyncTask<String, Integer, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                NetUtil.downloadFileV2(serverFile, new File(params[1]));
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPreExecute() {
                callback.onStartDownload();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                File file = new File(localFile);
                if (file.exists() && file.length() > 0) {
                    callback.onExportSuccess(localFile);
                } else {
                    callback.onExportFaild();
                }
            }
        }.execute(serverFile, localFile);
    }

    @Override
    public String getExportServerFilePath() {
        return fileName;
    }
}
