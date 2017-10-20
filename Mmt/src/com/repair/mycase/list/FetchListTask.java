package com.repair.mycase.list;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.repair.common.CaseItem;
import com.mapgis.mmt.global.MmtBaseTask;

public class FetchListTask extends MmtBaseTask<Void, Void, ResultData<CaseItem>> {

    public FetchListTask(Context context, boolean showLoading) {
        super(context, showLoading);
    }

    @Override
    protected ResultData<CaseItem> doInBackground(Void... params) {
        ResultData<CaseItem> resultData = null;

        try {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/RepairStandardRest.svc/GetMaintenanceDoingCase";

            String json = NetUtil.executeHttpGet(url, "userID", MyApplication.getInstance().getUserId() + "");

            if (BaseClassUtil.isNullOrEmptyString(json)) {
                return null;
            }

            resultData = new Gson().fromJson(json, new TypeToken<ResultData<CaseItem>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultData;
    }
}
