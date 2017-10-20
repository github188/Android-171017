package com.repair.zhoushan.module.eventreport;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.repair.zhoushan.entity.FlowNodeMeta;

/**
 * Parameters: FlowCenterData.toString().
 */
public class FetchFormBeanTask extends AsyncTask<String, Void, String> {

    private final BaseActivity activity;
    private final Handler handler;
    private final int msgWhat;

    private final ProgressDialog loadingDialog;

    public FetchFormBeanTask(BaseActivity activity, Handler handler, int what) {
        this.activity = activity;
        this.handler = handler;
        this.msgWhat = what;
        this.loadingDialog = MmtProgressDialog.getLoadingProgressDialog(activity, "正在处理,请稍候...");
    }

    @Override
    protected void onPreExecute() {
        //activity.setBaseProgressBarVisibility(true);
        loadingDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {

        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetBizMetaData";
        try {

            return NetUtil.executeHttpPost(url, params[0], "Content-Type", "application/json; charset=utf-8");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {

        // activity.setBaseProgressBarVisibility(false);
        if (loadingDialog.isShowing()) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        loadingDialog.dismiss();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }, 300);
        }

        if (BaseClassUtil.isNullOrEmptyString(result)) {
            activity.showErrorMsg("未正确获取信息");
            return;
        }

        Results<FlowNodeMeta> rawData = new Gson().fromJson(result, new TypeToken<Results<FlowNodeMeta>>() {
        }.getType());

        if (rawData == null) {
            activity.showErrorMsg("未获取正确信息");
            return;
        }

        ResultData<FlowNodeMeta> data = rawData.toResultData();

        if (data.ResultCode < 0 || data.DataList == null || data.DataList.size() == 0) {
            activity.showErrorMsg(data.ResultMessage);
            return;
        }

        Message message = handler.obtainMessage();
        message.what = msgWhat;
        message.obj = data.getSingleData();
        handler.sendMessage(message);
    }
}
