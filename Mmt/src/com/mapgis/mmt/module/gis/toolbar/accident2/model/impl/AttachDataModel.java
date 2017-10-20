package com.mapgis.mmt.module.gis.toolbar.accident2.model.impl;

import android.accounts.NetworkErrorException;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaItem;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IAttachDataModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.util.ConfigUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Request;

/**
 * Created by Comclay on 2017/5/17.
 * 附属数据业务类
 */

public class AttachDataModel implements IAttachDataModel {
    private static final String TAG = "AttachDataModel";
    // post请求阈值
    private static final int REQUEST_POST_THRESHOLD = 0;
    private FeatureMetaItem metaItem;
    private FeatureGroup mFeatures;
    private AttDataTask mTask;
    private String mAttIds;
    private String mExportPath;
    private String mLocalPath;

    public AttachDataModel(FeatureMetaItem metaItem) {
        this.metaItem = metaItem;
    }

    public int getSize() {
        if (mFeatures ==null || mFeatures.features == null || mFeatures.features.size() == 0){
            return 0;
        }
        return mFeatures.features.size();
    }

    @Override
    public FeatureGroup getAttData() {
        return mFeatures;
    }

    @Override
    public String getAttIds() {
        return mAttIds;
    }

    public String getXlsPath() {
        return this.mLocalPath;
    }

    @Override
    public String getRelationShipTableName() {
        if (metaItem == null || metaItem.relationships == null || metaItem.relationships.size() == 0){
            return null;
        }
        return this.metaItem.relationships.get(0).relationshipTableName;
    }

    @Override
    public Map<String, String> getAttData(int index) {
        if (index < 0 && index >= mFeatures.features.size()) {
            return null;
        }
        return mFeatures.getAllVisiableAttribute(index);
    }

    @Override
    public void queryAttachData(TaskCallback callback) {
        cancelTask();
        mTask = new AttDataTask(callback);
        mTask.executeOnExecutor(MyApplication.executorService);
    }

    @Override
    public void cancelTask() {
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    @Override
    public String getLayerName() {
        return metaItem.layerName;
    }

    private class AttDataTask extends AsyncTask<Void, Void, String> {

        TaskCallback callback;

        AttDataTask(TaskCallback callback) {
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            if (callback != null) callback.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            // 根据设备OID查询关联的附属数据的id
            try {
                mAttIds = queryAttIds(metaItem);
                if (BaseClassUtil.isNullOrEmptyString(mAttIds)) {
                    return null;
                }
                mFeatures = queryAttDataByIds(metaItem, mAttIds);
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof NetworkErrorException) {
                    return "网络异常";
                } else if (e instanceof JSONException) {
                    return "数据解析异常";
                }
                return "附属数据查询失败";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String msg) {
            if (msg != null) {
                if (callback != null) callback.onFailed(msg);
            } else {
                if (callback != null) callback.onSuccess();
            }
        }
    }

    @Nullable
    private String queryAttIds(FeatureMetaItem item) throws Exception {
        String result;
        if (item.objectIds.size() < REQUEST_POST_THRESHOLD) {
            // get请求
            String getUrl = getGetQueryAttIdsUrl(item);
            result = NetUtil.executeHttpGet(getUrl);
        } else {
            // post请求
            Request request = getPostQueryAttIdsRequest(item);
            result = NetUtil.executeHttpPost(request);
        }

        if (BaseClassUtil.isNullOrEmptyString(result)) {
            throw new NetworkErrorException("网络异常");
        }
        Log.i(TAG, result);
        JSONObject jsonObject = new JSONObject(result);
        JSONArray objectIds = jsonObject.getJSONArray("objectIds");
        List<String> attIds = new ArrayList<>();
        for (int i = 0; i < objectIds.length(); i++) {
            String id = String.valueOf(objectIds.get(i));
            attIds.add(id);
        }
        if (attIds.size() == 0) return null;
        return BaseClassUtil.listToString(attIds);
    }

    /*
     * http://192.168.12.200:806/cityinterface/rest/services/AuxDataServer.svc
     * /hzbj/12/QueryObjectIds?
     * relationshipTableName=%E7%BB%93%E6%9E%9C&%5Fts=1495009740622&objectIds=11523%2C12065%2C13558%2C13559
     */
    private String getGetQueryAttIdsUrl(FeatureMetaItem item) {
        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
        sb.append("/rest/services/AuxDataServer.svc/")
                .append(MobileConfig.MapConfigInstance.VectorService).append("/")
                .append(item.layerId).append("/")
                .append("QueryObjectIds?")
                .append("relationshipTableName=").append(item.relationships.get(0).relationshipTableName)
                .append("&_ts=").append(System.currentTimeMillis())
                .append("&objectIds=").append(BaseClassUtil.listToString(item.objectIds));
        return sb.toString();
    }

    private Request getPostQueryAttIdsRequest(FeatureMetaItem item) {
        String url = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath())
                .append("/rest/services/AuxDataServer.svc/")
                .append(MobileConfig.MapConfigInstance.VectorService).append("/")
                .append(item.layerId).append("/").append("QueryObjectIds?").toString();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("relationshipTableName", item.relationships.get(0).relationshipTableName)
                .add("&_ts=", System.currentTimeMillis() + "")
                .add("objectIds", BaseClassUtil.listToString(item.objectIds));
        Request.Builder reqBuilder = new Request.Builder();
        return reqBuilder.post(builder.build()).url(url).build();
    }

    private FeatureGroup queryAttDataByIds(FeatureMetaItem metaItem, String objIds) throws Exception {
        String result;
        if (metaItem.objectIds.size() < REQUEST_POST_THRESHOLD) {
            // get请求
            String getUrl = getGetQueryAttDataUrl(metaItem, objIds);
            result = NetUtil.executeHttpGet(getUrl);
        } else {
            // post请求
            Request request = getPostQueryAttDataRequest(metaItem, objIds);
            result = NetUtil.executeHttpPost(request);
        }

        if (BaseClassUtil.isNullOrEmptyString(result)) {
            throw new Exception("网络异常");
        }
        Log.i(TAG, result);
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.fromJson(result, FeatureGroup.class);
    }

    private Request getPostQueryAttDataRequest(FeatureMetaItem metaItem, String attIds) {
        String url = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath())
                .append("/rest/services/AuxDataServer.svc/")
                .append(MobileConfig.MapConfigInstance.VectorService).append("/")
                .append(metaItem.layerId).append("/QueryByObjectIds?").toString();
        FormBody formBody = new FormBody.Builder()
                .add("_ts", System.currentTimeMillis() + "")
                .add("relationshipTableName", metaItem.relationships.get(0).relationshipTableName)
                .add("objectIds", attIds).build();
        return new Request.Builder().url(url).post(formBody).build();
    }

    /**
     * http://192.168.12.200:806/cityinterface
     * /rest/services/AuxDataServer.svc/hzbj/12/QueryByObjectIds?
     * %5Fts=1495069349861&objectIds=389262%2C389270%2C389291%2C393906%2C420332%2C420333%2C421851
     * &relationshipTableName=%E7%BB%93%E6%9E%9C
     */
    private String getGetQueryAttDataUrl(FeatureMetaItem item, String attIds) {
        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
        sb.append("/rest/services/AuxDataServer.svc/")
                .append(MobileConfig.MapConfigInstance.VectorService).append("/")
                .append(item.layerId).append("/QueryByObjectIds?")
                .append("_ts").append(System.currentTimeMillis())
                .append("&relationshipTableName=").append(item.relationships.get(0).relationshipTableName)
                .append("&objectIds=").append(attIds);
        return sb.toString();
    }

    @Override
    public boolean isEmpty() {
        return mFeatures == null || mFeatures.features == null || mFeatures.features.size() == 0;
    }

    @Override
    public void exportAttData(final TaskCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                mLocalPath = null;
                mExportPath = null;
                if (callback != null) callback.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    // 读取配置
                    String outFields = ConfigUtil.getBrokenOutFieldsConfig();
                    // 导出excel
                    mExportPath = exportXls(outFields);
                    if (BaseClassUtil.isNullOrEmptyString(mExportPath)) {
                        return "导出失败";
                    }
                    // 下载excel
                    downloadXls(mExportPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if (BaseClassUtil.isNullOrEmptyString(mLocalPath)) {
                    if (callback != null) callback.onFailed("导出失败");
                }
                File file = new File(mLocalPath);
                if (!file.exists() || file.length() == 0) {
                    if (callback != null) callback.onFailed("导出失败");
                } else {
                    if (callback != null) callback.onSuccess();
                }
            }
        }.executeOnExecutor(MyApplication.executorService);
    }

    private String exportXls(String outFields) throws JSONException {
        String exportXlsUrl = getExportXlsUrl(metaItem.relationships.get(0).relationshipTableName, outFields);
        String exportResult = NetUtil.executeHttpGet(exportXlsUrl);
        if (BaseClassUtil.isNullOrEmptyString(exportResult)) return "";

        JSONObject jsonObject = new JSONObject(exportResult);
        String success = jsonObject.getString("success");
        if ("true".equalsIgnoreCase(success)) {
            return jsonObject.getString("fileName");
        }
        return "";
    }

    private void downloadXls(String serverPath) throws UnsupportedEncodingException {
        String url = getDownloadXlsUrl(serverPath);
        File localXlsFile = getLocalXlsFile(serverPath);
        mLocalPath = localXlsFile.getPath();
        NetUtil.downloadFileV2(url, localXlsFile);
    }

    private File getLocalXlsFile(String serverPath) {
        int index = serverPath.lastIndexOf("/");
        if (index < 0) {
            index = serverPath.lastIndexOf("\\");
        }
        String fileName = serverPath.substring(index + 1);
        int dotIndex = fileName.indexOf(".");
        String relativePath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Temp, false);
        String prefix = fileName.substring(0, dotIndex);
        String suffix = fileName.substring(dotIndex + 1);
        int count = 0;
        String alias = "";
        String path = String.format(Locale.CHINA, "%s/%s%s.%s", relativePath, prefix, alias, suffix);
        File file = new File(path);
        while (file.exists()) {
            alias = String.format(Locale.CHINA, "(%d)", ++count);
            path = String.format(Locale.CHINA, "%s/%s%s.%s", relativePath, prefix, alias, suffix);
            file = new File(path);
        }
        return file;
    }

    /**
     * http://192.168.12.200:806/cityinterface
     * /rest/services/AuxDataServer.svc/hzbj/12/ExportAuxAttByIDs?
     * %5Fts=1495091714795&auxObjectIds=389262%2C389270%2C389291%2C393906%2C420332%2C420333%2C421851
     * &auxTableName=%E7%BB%93%E6%9E%9C&outFields=%E6%88%B7%E5%8F%B7%2C%E6%88%B7%E5%90%8D%2C%E5%9C%B0%E5%9D%80%2C%E6%9C%AC%E6%9C%88%E7%94%A8%E6%B0%B4%E9%87%8F
     */
    public String getExportXlsUrl(String auxTableName, String outFields) {
        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
        sb.append("/rest/services/AuxDataServer.svc/")
                .append(MobileConfig.MapConfigInstance.VectorService).append("/")
                .append(metaItem.layerId)
                .append("/ExportAuxAttByIDs?")
                .append("_ts=").append(System.currentTimeMillis())
                .append("&auxTableName=").append(auxTableName)
                .append("&auxObjectIds=").append(mAttIds)
                .append("&outFields=").append(outFields);
        return sb.toString();
    }

    /**
     * http://192.168.12.200:806/cityinterface
     * /rest/services/filedownload.svc/download/ExcelExport/
     * 8fbbeb6a-2931-40e8-957e-c2354f69042c/%E4%BE%9B%E6%B0%B4%E7%AE%A1%E7%BD%91_%E6%B0%B4%E8%A1%A8-%E7%BB%93%E6%9E%9C-2017-5-19%209-57-36.xls
     */
    public String getDownloadXlsUrl(String xlsUri) {
        return ServerConnectConfig.getInstance().getBaseServerPath()
                + "/rest/services/filedownload.svc/download/"
                + xlsUri;
    }
}
