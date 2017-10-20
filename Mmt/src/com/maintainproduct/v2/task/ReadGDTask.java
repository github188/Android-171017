package com.maintainproduct.v2.task;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.maintainproduct.v2.caselist.MaintainConstant;
import com.maintainproduct.v2.caselist.ReportItem;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;

import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 接单
 * 
 * @author meikai
 */
public class ReadGDTask extends AsyncTask<ReportItem, Integer, String> {

	private BaseActivity activity;
	private Handler handler;

	public ReadGDTask(BaseActivity activity, Handler handler) {
		super();
		this.activity = activity;
		this.handler = handler;
	}

	@Override
	protected void onPreExecute() {
		activity.setBaseProgressBarVisibility(true);
	}

	@Override
	protected String doInBackground(ReportItem... params) {

		try {
			String url = ServerConnectConfig.getInstance().getBaseServerPath()
					+ "/Services/MapgisCity_WXYH_GL_MOBILE/REST/CaseManageREST.svc/ReadGD";

			Request request = new Request.Builder()
					.url(url)
					.header("Content-Type", "application/json; charset=utf-8")
					.post(RequestBody.create(NetUtil.JSON, new Gson().toJson(params[0])))
					.build();

			return NetUtil.executeHttpPost(request);

		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(String result) {
		if (result == null) {
			return;
		}
		activity.setBaseProgressBarVisibility(false);
		Message msg = handler.obtainMessage();
		msg.what = MaintainConstant.SERVER_READ_GD_DONE;
		msg.obj = result.replaceFirst("\"resultContent\"", "\"DataList\"");
		msg.sendToTarget();
	}
}
