package com.repair.zhoushan.module.eventreport;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.v2.module.EventTypeItem;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;

/**
 * 专门获取级联的字典值
 */
public class FetchEventTypeTask extends MmtBaseTask<String, Integer, ResultData<EventTypeItem>> {
    public FetchEventTypeTask(Context context, boolean showLoading, OnWxyhTaskListener<ResultData<EventTypeItem>> listener) {
        super(context, showLoading, listener);
    }

    @Override
    protected ResultData<EventTypeItem> doInBackground(String... params) {
        Results<EventTypeItem> data;

        try {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EventManage/GetEventType";

            String json = NetUtil.executeHttpGet(url, "pName", params[0], "cName", params[1]);

            if (TextUtils.isEmpty(json))
                return null;

            data = new Gson().fromJson(json, new TypeToken<Results<EventTypeItem>>() {}.getType());

            return data.toResultData();
        } catch (Exception e) {
            return null;
        }
    }
}