package com.repair.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseTaskResultData<Params, Progress, T>
        extends MmtBaseTask<Params, Progress, ResultData<T>> {

    public BaseTaskResultData(Context context) {
        super(context);
    }

    public BaseTaskResultData(Context context, boolean showLoading) {
        super(context, showLoading);
    }

    @Override
    protected ResultData<T> doInBackground(Params... params) {

        ResultData<T> resultData;
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
            ParameterizedType parameterizedType = getParameterizedType(ResultData.class, entityClass);
            resultData = new Gson().fromJson(jsonResult, parameterizedType);

            if (resultData.DataList == null) {
                resultData.DataList = new ArrayList<>();
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultData = new ResultData<>();
            resultData.ResultCode = -100;
            resultData.ResultMessage = e.getMessage();
        }

        return resultData;
    }

    @NonNull
    protected abstract String getRequestUrl() throws Exception;

}
