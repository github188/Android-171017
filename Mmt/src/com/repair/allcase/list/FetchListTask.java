package com.repair.allcase.list;

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

    public FetchListTask(Context context, boolean showLoading) {
        super(context, showLoading);
    }

    @Override
    protected ResultDataWC<CaseItem> doInBackground(ConditionParameter... params) {
        try {
            String url = ServerConnectConfig.getInstance().getWebWXYHProductURL() + "/" + this.userID + "/AllWorkBill";

            String json = NetUtil.executeHttpGet(url, params[0].generateRequestArgs());

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
