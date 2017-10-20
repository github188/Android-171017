package com.mapgis.mmt.module.gis.toolbar.accident2.model.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaItem;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IFeatureItemModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Comclay on 2017/3/2.
 * 具体实现类
 */

public class FeatureItemModel implements IFeatureItemModel {
    private static final String TAG = "FeatureItemModel";
    // 当请求中设备id超过20个就采用post请求
    private final static int MAX_REQUEST_OBJ_SIZE = 50;
    private transient List<Future<?>> mFutureList;

    public FeatureItemModel() {
        mFutureList = new ArrayList<>();
    }

    @Override
    public void loadFeatureMetaGroup(FeatureMetaGroup group
            , LoadCallback callback) {
        try {
            if (group == null || group.getResultList() == null) {
                return;
            }
            ArrayList<FeatureMetaItem> resultList = group.getResultList();
            if (resultList == null || resultList.size() == 0) {
                return;
            }
            for (int i = 0; i < resultList.size(); i++) {
                if (loadFeatureMetaItem(group, callback, i)) return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean loadFeatureMetaItem(FeatureMetaGroup group, LoadCallback callback, int i) {
        FeatureMetaItem featureMetaItem = group.getResultList().get(i);
        if (featureMetaItem == null || featureMetaItem.objectIds == null
                || featureMetaItem.objectIds.size() == 0) {
            return true;
        }
        LoadFeatureRunnable loadFeatureRunnable = new LoadFeatureRunnable(group, i, callback);
        Future<?> future = MyApplication.getInstance().submitExecutorService(loadFeatureRunnable);
        mFutureList.add(future);
        return false;
    }

    private class LoadFeatureRunnable implements Runnable {
        private FeatureMetaGroup mGroup;
        private int index;
        private LoadCallback mCallback;

        LoadFeatureRunnable(FeatureMetaGroup group, int index, LoadCallback callback) {
            this.mGroup = group;
            this.index = index;
            if (callback == null) {
                this.mCallback = new DefaultLoadCallback();
            } else {
                this.mCallback = callback;
            }
        }

        @Override
        public void run() {
            try {
                ArrayList<FeatureMetaItem> resultList = this.mGroup.getResultList();
                FeatureMetaItem item = resultList.get(index);
                if (item == null) {
                    return;
                }
                String result = requestData(item);
                if (BaseClassUtil.isNullOrEmptyString(result)) {
                    this.mCallback.onLoadFailed("网络异常");
                    return;
                }
                item.setFeatureGroup(parseData(result));
                this.mCallback.onLoadSuccess(this.mGroup, index);
            } catch (Exception e) {
                this.mCallback.onLoadFailed("数据请求失败");
                e.printStackTrace();
            }
        }

        /**
         * 根据设备数量选择使用get请求或者post请求
         *
         * @throws Exception
         */
        private String requestData(FeatureMetaItem item) throws Exception {
            String result;
            if (item.objectIds.size() < MAX_REQUEST_OBJ_SIZE) {
                String featureUrl = getFeatureGetUrl(item);
                result = NetUtil.executeHttpGet(featureUrl);
                Log.i(TAG, "requestData: GET方式请求");
            } else {
                String featureUrl = getFeaturePostUrl(item);
                RequestBody formBody = getRequestBody(item);
                Request.Builder builder1 = new Request.Builder();
                Request request = builder1.url(featureUrl).post(formBody).build();
                result = NetUtil.executeHttpPost(request);
                Log.i(TAG, "requestData: POST方式请求");
            }
            return result;
        }

        @NonNull
        private RequestBody getRequestBody(FeatureMetaItem item) {
            ArrayList<String> objectIds = item.objectIds;
            String objs = BaseClassUtil.listToString(objectIds);
            //创建一个FormBody.Builder
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("returnGeometry", "false")
                    .add("returnDistinctValues", "false")
                    .add("objectIds", objs)
                    .add("_ts", String.valueOf(System.currentTimeMillis()))
                    .add("f", "json")
                    .add("spatialRel", "civSpatialRelIntersects");
            return builder.build();
        }

        @Nullable
        private FeatureGroup parseData(String result) {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            return gson.fromJson(result, new TypeToken<FeatureGroup>() {
            }.getType());
        }

        /**
         * http://192.168.12.200:803/cityinterface/rest/services/mapserver.svc/hzbj/1/query
         */
        private String getFeaturePostUrl(FeatureMetaItem item) {
            String layerId = item.layerId;
            String mapServerName = MobileConfig.MapConfigInstance.VectorService;
            return new StringBuilder()
                    .append(ServerConnectConfig.getInstance().getBaseServerPath())
                    .append("/rest/services/mapserver.svc/")
                    .append(mapServerName).append("/").append(layerId).append("/")
                    .append("query").toString();
        }


        /**
         * http://192.168.12.200:803/cityinterface/rest/services/mapserver.svc
         * /hzbj/12/query?returnGeometry=false&returnDistinctValues=false
         * &objectIds=13674&%5Fts=1488419630777&f=json&spatialRel=civSpatialRelIntersects
         *
         * @param item FeatureMetaItem
         */
        private String getFeatureGetUrl(FeatureMetaItem item) {
            if (item == null) {
                return null;
            }

            String layerId = item.layerId;
            ArrayList<String> objectIds = item.objectIds;
            String objs = BaseClassUtil.listToString(objectIds);

            String mapServerName = MobileConfig.MapConfigInstance.VectorService;
            String s = new StringBuilder()
                    .append(ServerConnectConfig.getInstance().getBaseServerPath())
                    .append("/rest/services/mapserver.svc/")
                    .append(mapServerName).append("/").append(layerId).append("/")
                    .append("query?returnGeometry=false&returnDistinctValues=false")
                    .append("&objectIds=").append(objs)
                    .append("&_ts").append(System.currentTimeMillis())
                    .append("&f=json&spatialRel=civSpatialRelIntersects").toString();
            Log.i(TAG, "请求数据: " + s);
            return s;
        }

    }

    public void stopAllTask() {
        try {
            for (Future future : mFutureList) {
                if (future.isCancelled()) {
                    future.cancel(true);
                }
            }
            mFutureList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}