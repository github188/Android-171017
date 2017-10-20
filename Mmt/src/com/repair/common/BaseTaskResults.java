package com.repair.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

public abstract class BaseTaskResults<Params, Progress, T>
        extends MmtBaseTask<Params, Progress, Results<T>> {

    public BaseTaskResults(Context context) {
        super(context);
    }

    public BaseTaskResults(Context context, boolean showLoading) {
        super(context, showLoading);
    }

    @Override
    protected Results<T> doInBackground(Params... params) {

        Results<T> results;
        try {
            final String url = getRequestUrl();
            if (TextUtils.isEmpty(url)) {
                throw new Exception("请求Url不能为空");
            }

            String jsonResult = NetUtil.executeHttpGet(url);
            if (TextUtils.isEmpty(jsonResult)) {
                throw new Exception("请求失败");
            }

            @SuppressWarnings("unchecked")
            Class<T> entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[2];
            ParameterizedType parameterizedType = getParameterizedType(Results.class, entityClass);
            results = new Gson().fromJson(jsonResult, parameterizedType);

            if (results.getMe == null) {
                results.getMe = new ArrayList<>();
            }

        } catch (Exception e) {
            e.printStackTrace();
            results = new Results<>("-100", e.getMessage());
        }

        return results;
    }

    @NonNull
    protected abstract String getRequestUrl() throws Exception;

}

