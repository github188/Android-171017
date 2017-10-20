package com.maintainproduct.module.maintenance.detail;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;

/** 获取工单详情任务 */
public class MaintenanceDetailTask extends AsyncTask<String, Integer, String> {
	private final BaseActivity activity;
	private final Handler handler;

	public MaintenanceDetailTask(BaseActivity activity, Handler handler) {
		this.activity = activity;
		this.handler = handler;
	}

	@Override
	protected void onPreExecute() {
		activity.setBaseProgressBarVisibility(true);
	}

	@Override
	protected String doInBackground(String... params) {

		String url = ServerConnectConfig.getInstance().getBaseServerPath()
				+ "/Services/MapgisCity_WorkFlowForm/REST/WorkFlowFormREST.svc/GetGDDetailByIDAndSource";

		String jsonStr = NetUtil.executeHttpGet(url, "id", params[0], "source", params[1]);

		return jsonStr;
	}

	@Override
	protected void onPostExecute(String result) {
		activity.setBaseProgressBarVisibility(false);

		if (result == null || result.length() == 0) {
			activity.showErrorMsg("未正确获取信息");
			return;
		}

		ResultData<GDFormBean> data = new Gson().fromJson(result, new TypeToken<ResultData<GDFormBean>>() {
		}.getType());

		if (data.ResultCode < 0 || data.DataList == null || data.DataList.size() == 0) {
			activity.showErrorMsg(data.ResultMessage);
			return;
		}

		Message message = handler.obtainMessage();
		message.what = MaintenanceConstant.SERVER_GET_DETAIL_SUCCESS;
		message.obj = data.getSingleData();
		handler.sendMessage(message);
	}
}
