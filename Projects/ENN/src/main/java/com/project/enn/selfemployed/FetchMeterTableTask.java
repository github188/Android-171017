package com.project.enn.selfemployed;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.FlowNodeMeta;

public class FetchMeterTableTask extends MmtBaseTask<Void, Void, Results<FlowNodeMeta>> {

    public FetchMeterTableTask(Context context) {
        super(context);
        setCancellable(false);
    }

    @Override
    protected Results<FlowNodeMeta> doInBackground(Void... params) {

        Results<FlowNodeMeta> results;

        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetTableGroupMeta?tableName="
                + Uri.encode("工商户置换表具记录表");

        try {
            String jsonResult = NetUtil.executeHttpGet(url);
            if (TextUtils.isEmpty(jsonResult)) {
                throw new Exception("获取数据失败");
            }

            results = new Gson().fromJson(jsonResult, new TypeToken<Results<FlowNodeMeta>>() {
            }.getType());

        } catch (Exception e) {
            e.printStackTrace();
            results = new Results<>("1001", e.getMessage());
        }

        return results;
    }
}
