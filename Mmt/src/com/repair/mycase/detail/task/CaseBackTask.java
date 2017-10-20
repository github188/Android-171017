package com.repair.mycase.detail.task;

import android.content.Context;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;

/**
 * 退单操作
 * <p>
 * <p>
 * 参数0：CaseID<br>
 * 参数1：ID0<br>
 * 参数2：意见描述<br>
 */
public class CaseBackTask extends MmtBaseTask<String, Void, ResultWithoutData> {

    public CaseBackTask(Context context, boolean showLoading) {
        super(context, showLoading);
    }

    @Override
    protected ResultWithoutData doInBackground(String... params) {
        String url = this.repairService + "/CaseBack";

        String result = NetUtil.executeHttpGet(url, "UserID", this.userID, "CaseID", params[0],
                "StepID", params[1], "Opinion", params[2]);

        return new Gson().fromJson(result, ResultWithoutData.class);
    }
}
