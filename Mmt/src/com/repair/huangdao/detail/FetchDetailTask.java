package com.repair.huangdao.detail;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.ResultDataWC;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.huangdao.CaseItemV21;

public class FetchDetailTask extends MmtBaseTask<String, Integer, ResultDataWC<CaseItemV21>> {

    public FetchDetailTask(Context context) {
        super(context);
    }

    @Override
    protected ResultDataWC<CaseItemV21> doInBackground(String... params) {
        ResultDataWC<CaseItemV21> data = null;

        try {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/MapgisCity_WXYH_Huangdao/REST/ServiceManageREST.svc/CaseManage/GongDanInfo";

            String json = NetUtil.executeHttpGet(url, "CaseID", params[0]);

            if (BaseClassUtil.isNullOrEmptyString(json)) {
                return null;
            }

            data = new Gson().fromJson(json, new TypeToken<ResultDataWC<CaseItemV21>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

}
