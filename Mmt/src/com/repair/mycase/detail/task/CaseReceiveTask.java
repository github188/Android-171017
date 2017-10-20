package com.repair.mycase.detail.task;

import android.content.Context;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;

/**
 * 接单操作
 * <p>
 * <p>
 * 参数0：CaseID<br>
 * 参数1：意见描述
 */
public class CaseReceiveTask extends MmtBaseTask<String, Void, ResultWithoutData> {

    public CaseReceiveTask(Context context, boolean showLoading) {
        super(context, showLoading);
    }

    @Override
    protected ResultWithoutData doInBackground(String... params) {
        String url = this.repairService + "/CaseReceive";

        String result = NetUtil.executeHttpGet(url, "UserID", userID, "CaseID", params[0],
                "Opinion", params[1]);

        return new Gson().fromJson(result, ResultWithoutData.class);
    }
}