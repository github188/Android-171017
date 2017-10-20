package com.maintainproduct.module.basereport;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;

public class BaseFlowFormTask extends AsyncTask<String, String, String> {
	private final Handler handler;

	private final BaseActivity baseActivity;

	public BaseFlowFormTask(Handler handler, BaseActivity baseActivity) {
		this.handler = handler;
		this.baseActivity = baseActivity;
	}

	@Override
	protected void onPreExecute() {
		baseActivity.setProgressBarVisibility(true);
	}

	@Override
	protected String doInBackground(String... params) {
		// 通过服务获取表单配置信息
		String url = ServerConnectConfig.getInstance().getBaseServerPath()
				+ "/Services/MapgisCity_WorkFlowForm/REST/WorkFlowFormREST.svc/GetGDFormBySource";

		String result = NetUtil.executeHttpGet(url, "source", params[0]);

		return result;
	}

	@Override
	protected void onPostExecute(String result) {

		try {

			if (result == null || result.length() <= 2) {
				baseActivity.showErrorMsg("未加载到表单数据");
				return;
			}

			ResultData<GDFormBean> data = new Gson().fromJson(result, new TypeToken<ResultData<GDFormBean>>() {
			}.getType());

			if (data.ResultCode < 0) {
				baseActivity.showErrorMsg(data.ResultMessage);
				return;
			}

			Message msg = handler.obtainMessage();
			msg.obj = data.getSingleData();
			handler.sendMessage(msg);

		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} finally {
			baseActivity.setProgressBarVisibility(false);
		}

	}
}
