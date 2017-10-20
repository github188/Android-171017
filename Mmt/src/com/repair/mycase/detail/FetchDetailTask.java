package com.repair.mycase.detail;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.repair.common.CaseItem;
import com.mapgis.mmt.global.MmtBaseTask;

public class FetchDetailTask extends MmtBaseTask<CaseItem, Integer, ResultData<CaseItem>> {

    public FetchDetailTask(Context context, boolean showLoading, OnWxyhTaskListener<ResultData<CaseItem>> listener) {
        super(context, showLoading, listener);
    }

    public CaseItem item;

    @Override
    protected ResultData<CaseItem> doInBackground(CaseItem... params) {
        ResultData<CaseItem> data = null;

        try {
            this.item = params[0];

            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/RepairStandardRest.svc/FetchCaseDetail";

            String json = NetUtil.executeHttpGet(url, "caseID", this.item.CaseID);

            if (BaseClassUtil.isNullOrEmptyString(json)) {
                return null;
            }

            data = new Gson().fromJson(json, new TypeToken<ResultData<CaseItem>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
