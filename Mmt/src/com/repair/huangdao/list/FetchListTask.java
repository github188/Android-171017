package com.repair.huangdao.list;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.ResultDataWC;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.repair.common.CaseItem;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.entity.ConditionParameter;

public class FetchListTask extends MmtBaseTask<ConditionParameter, Integer, ResultDataWC<CaseItem>> {

    public FetchListTask(Context context) {
        super(context);
    }

    @Override
    protected ResultDataWC<CaseItem> doInBackground(ConditionParameter... params) {
        try {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/MapgisCity_WXYH_Huangdao/REST/ServiceManageREST.svc/CaseManage/" + this.userID + "/DoingCase";

            ConditionParameter param = params[0];

            param.sortFields = "承办时间";

            String json = NetUtil.executeHttpGet(url, param.generateRequestArgs());

            if (BaseClassUtil.isNullOrEmptyString(json)) {
                return null;
            }

            return new Gson().fromJson(json, new TypeToken<ResultDataWC<CaseItem>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}
