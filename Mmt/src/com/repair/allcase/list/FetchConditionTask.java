package com.repair.allcase.list;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.entity.CaseCondition;

public class FetchConditionTask extends MmtBaseTask<String, Integer, ResultData<CaseCondition>> {
    public FetchConditionTask(Context context) {
        super(context);
    }

    @Override
    protected ResultData<CaseCondition> doInBackground(String... params) {
        ResultData<CaseCondition> data = null;

        try {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/RepairStandardRest.svc/FetchSearchCondition";

            String json = NetUtil.executeHttpGet(url);

            if (BaseClassUtil.isNullOrEmptyString(json)) {
                return null;
            }

            data = new Gson().fromJson(json, new TypeToken<ResultData<CaseCondition>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
