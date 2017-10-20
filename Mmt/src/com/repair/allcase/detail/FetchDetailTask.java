package com.repair.allcase.detail;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.entity.CaseFullyDetail;

public class FetchDetailTask extends MmtBaseTask<String, Integer, ResultData<CaseFullyDetail>> {

    public FetchDetailTask(Context context, boolean showLoading, OnWxyhTaskListener<ResultData<CaseFullyDetail>> listener) {
        super(context, showLoading, listener);
    }

    @Override
    protected ResultData<CaseFullyDetail> doInBackground(String... params) {
        ResultData<CaseFullyDetail> data = null;

        try {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/RepairStandardRest.svc/FetchCaseDetail";

            String json = NetUtil.executeHttpGet(url, "caseID", params[0]);

            if (BaseClassUtil.isNullOrEmptyString(json)) {
                return null;
            }

            data = new Gson().fromJson(json, new TypeToken<ResultData<CaseFullyDetail>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
