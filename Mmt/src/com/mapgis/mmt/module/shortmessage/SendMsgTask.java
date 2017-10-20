package com.mapgis.mmt.module.shortmessage;

import android.os.AsyncTask;
import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.entity.ShortMessageBean;

public class SendMsgTask extends AsyncTask<String, String, String> {

	private String names;

	private final BaseActivity activity;
	private final Handler handler;

	private String content;

	public SendMsgTask(BaseActivity activity, Handler handler) {
		this.activity = activity;
		this.handler = handler;
	}

	@Override
	protected String doInBackground(String... params) {

		names = params[1].substring(0, params[1].length() - 1);
		String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc";

		content = params[2];

		String result = NetUtil.executeHttpGet(url + "/SendMessage", "f", "json", "MsgDetail", content, "SenderID",
				String.valueOf(MyApplication.getInstance().getUserId()), "ReceiverID",
				params[0].substring(0, params[0].length() - 1));

		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		try {

			ResultWithoutData resultData = new Gson().fromJson(result, ResultWithoutData.class);

			if (resultData.ResultCode < 0) {
				activity.showErrorMsg(resultData.ResultMessage);
			} else {
				activity.showToast(resultData.ResultMessage);

				ShortMessageBean msg = new ShortMessageBean(BaseClassUtil.getSystemTime(), content + "#" + names, 3,
						MyApplication.getInstance().getUserId());
				DatabaseHelper.getInstance().insert(msg);

				handler.sendEmptyMessage(3);
			}

		} catch (JsonSyntaxException e) {
			activity.showErrorMsg("返回数据未能正确解析,请确认服务是否为最新版本");
		}

	}

}
