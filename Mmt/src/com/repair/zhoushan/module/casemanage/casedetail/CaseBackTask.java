package com.repair.zhoushan.module.casemanage.casedetail;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.CaseInfo;

public class CaseBackTask extends MmtBaseTask<CaseInfo, Void, String> {

    private String serviceName;
    private String defaultErrMsg;

    private CaseBackTask(Context context, boolean showLoading, OnWxyhTaskListener<String> listener) {
        super(context, showLoading, listener);
    }

    public CaseBackTask(Context context, boolean showLoading, String serviceName, String defaultErrMsg, OnWxyhTaskListener<String> listener) {
        this(context, showLoading, listener);
        if (TextUtils.isEmpty(serviceName)) {
            throw new NullPointerException("Argument 'serviceName' cannot be empty");
        }
        this.serviceName = serviceName;
        this.defaultErrMsg = defaultErrMsg;
    }

    @Override
    protected String doInBackground(CaseInfo... params) {

        try {
            String data = new Gson().toJson(params[0], CaseInfo.class);
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/WorkFlow/" + serviceName;

            // if (response == null || response == "") notify.show("撤回成功", "success");
            return NetUtil.executeHttpPost(url, data, "Content-Type", "application/json; charset=utf-8");

        } catch (Exception e) {
            e.printStackTrace();
            return !TextUtils.isEmpty(defaultErrMsg) ? defaultErrMsg : ("操作失败：" + e.getMessage());
        }
    }
}
