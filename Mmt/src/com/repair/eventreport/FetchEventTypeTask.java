package com.repair.eventreport;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.v2.module.EventTypeItem;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;

/**
 * 获取 巡线上报 事件 类型
 */
public class FetchEventTypeTask extends MmtBaseTask<String, Integer, ResultData<EventTypeItem>> {
    public FetchEventTypeTask(Context context, boolean showLoading, OnWxyhTaskListener<ResultData<EventTypeItem>> listener) {
        super(context, showLoading, listener);
    }

    @Override
    protected ResultData<EventTypeItem> doInBackground(String... params) {
        ResultData<EventTypeItem> data;

        try {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/services/MapgisCity_WXYH_GL_MOBILE/REST/CaseManageREST.svc/" + params[0];

            String json = NetUtil.executeHttpGet(url);

            if (TextUtils.isEmpty(json))
                return null;

            data = new Gson().fromJson(json.replaceFirst("\"resultContent\"", "\"DataList\""),
                    new TypeToken<ResultData<EventTypeItem>>() {
                    }.getType());

            return data;
        } catch (Exception e) {
            return null;
        }
    }
}
