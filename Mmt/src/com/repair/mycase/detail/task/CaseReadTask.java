package com.repair.mycase.detail.task;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.repair.common.CaseItem;
import com.mapgis.mmt.global.MmtBaseTask;

/**
 * 上传阅读工单时间
 * <p/>
 * <p/>
 * 参数0：阅读状态
 */
public class CaseReadTask extends MmtBaseTask<CaseItem, Integer, ResultWithoutData> {
    CaseItem item;

    public CaseReadTask(Context context, boolean showLoading, OnWxyhTaskListener<ResultWithoutData> listener) {
        super(context, showLoading, listener);
    }

    @Override
    protected ResultWithoutData doInBackground(CaseItem... params) {
        try {
            this.item = params[0];

            String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/RepairStandardRest.svc/CaseRead";

            String json = NetUtil.executeHttpGet(url, "CaseID", this.item.CaseID);

            if (TextUtils.isEmpty(json))
                return null;
            else
                return new Gson().fromJson(json, ResultWithoutData.class);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(ResultWithoutData resultWithoutData) {
        super.onPostExecute(resultWithoutData);

        if (resultWithoutData != null && resultWithoutData.ResultCode > 0)
            this.item.IsRead = "已阅读";
    }
}
