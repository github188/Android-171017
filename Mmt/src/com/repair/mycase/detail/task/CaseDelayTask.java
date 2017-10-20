package com.repair.mycase.detail.task;

import android.content.Context;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;

/**
 * 延期操作
 * <p>
 * <p>
 * 参数0：CaseID<br>
 * 参数1：申请延期的完成时间<br>
 * 参数2：意见描述<br>
 * 参数3：预计完成时间
 */
public class CaseDelayTask extends MmtBaseTask<String, Void, ResultWithoutData> {

    public CaseDelayTask(Context context, boolean showLoading) {
        super(context, showLoading);
    }

    @Override
    protected ResultWithoutData doInBackground(String... params) {
        ResultWithoutData data;

        String url = this.repairService + "/CaseDelay";
        String result = NetUtil.executeHttpGet(url, "UserID", this.userID, "CaseID", params[0],
                "ApplyFinishTime", params[1], "Opinion", params[2]);

        data = new Gson().fromJson(result, ResultWithoutData.class);

        return data;
    }
}
