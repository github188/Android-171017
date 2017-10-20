package com.repair.zhoushan.module.casemanage.casedetail;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.repair.zhoushan.entity.FlowInfoItem;
import com.repair.zhoushan.entity.FlowTableInfo;

public class FetchCaseDetailTask extends AsyncTask<String, Void, String> {

    private final BaseActivity activity;
    private final Handler handler;
    private boolean isAllCaseScan = false;

    public FetchCaseDetailTask(BaseActivity activity, Handler handler, boolean isAllCaseScan) {
        this.activity = activity;
        this.handler = handler;
        this.isAllCaseScan = isAllCaseScan;
    }

    @Override
    protected void onPreExecute() {
        activity.setBaseProgressBarVisibility(true);
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/WorkFlow/GetFlowInfo";

            if (isAllCaseScan) {
                url = ServerConnectConfig.getInstance().getBaseServerPath() +
                        "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/WorkFlow/CaseOverviewGetFlowTableInfo";
            }

            return NetUtil.executeHttpGet(url, "eventCode", params[0], "caseNo", params[1],
                    "flowName", params[2], "nodeName", params[3]);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            activity.setBaseProgressBarVisibility(false);

            if (result == null || result.length() == 0) {
                activity.showErrorMsg("未正确获取信息");
                return;
            }

            if (isAllCaseScan) {
                Results<FlowTableInfo> rawData = new Gson().fromJson(result,
                        new TypeToken<Results<FlowTableInfo>>() {
                        }.getType());

                if (rawData == null || rawData.getMe == null || rawData.getMe.size() <= 0) {
                    activity.showErrorMsg(rawData == null ? "获取详情错误，可能网络不畅" : rawData.say.errMsg);
                    return;
                }

                ResultData<FlowTableInfo> data = rawData.toResultData();

                Message message = handler.obtainMessage();
                message.what = MaintenanceConstant.SERVER_GET_DETAIL_SUCCESS;
                message.obj = data.DataList;
                handler.sendMessage(message);
            } else {
                Results<FlowInfoItem> rawData = new Gson().fromJson(result,
                        new TypeToken<Results<FlowInfoItem>>() {
                        }.getType());

                if (rawData == null || rawData.getMe == null || rawData.getMe.size() <= 0) {
                    activity.showErrorMsg(rawData == null ? "获取详情错误，可能网络不畅" : rawData.say.errMsg);
                    return;
                }

                ResultData<FlowInfoItem> data = rawData.toResultData();

                Message message = handler.obtainMessage();
                message.what = MaintenanceConstant.SERVER_GET_DETAIL_SUCCESS;
                message.obj = data.DataList;
                handler.sendMessage(message);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
