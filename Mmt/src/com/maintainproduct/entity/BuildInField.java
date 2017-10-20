package com.maintainproduct.entity;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.UserBean;

/** 内置规定字段 */
public class BuildInField {

	public static String[] getFields(Context context) {
		return new String[] { "UserID", "UserName", "Telephone","DepartName","DepartID" };
	}

	public static String getValue(final ImageTextView view, String field) {
		if (BaseClassUtil.isNullOrEmptyString(field)) {
			return null;
		}

		if (field.equalsIgnoreCase("$UserID$")) {// 用户ID $UserID$

			view.setValue(MyApplication.getInstance().getUserId() + "");

			return MyApplication.getInstance().getUserId() + "";

		} else if (field.equalsIgnoreCase("$UserName$")) {// 用户姓名 $UserName$

			view.setValue(MyApplication.getInstance().getConfigValue(
					"UserBean", UserBean.class).TrueName);

			return MyApplication.getInstance().getConfigValue("UserBean",
					UserBean.class).TrueName;
		} else if (field.equalsIgnoreCase("$DepartName$")) {
			// 取直接部门
			UserBean user = MyApplication.getInstance().getConfigValue(
					"UserBean", UserBean.class);
			if(user.DepartName==null) return null;
			view.setValue(user.DepartName[user.DepartName.length - 1]);
			return user.DepartName[user.DepartName.length - 1];

		} else if (field.equalsIgnoreCase("$DepartID$")) {
			new AsyncTask<String, String, ResultData<String>>() {
				@Override
				protected ResultData<String> doInBackground(String... params) {

					String url = ServerConnectConfig.getInstance()
							.getMobileBusinessURL()
							+ "/BaseREST.svc/GetDepartID";
					UserBean user = MyApplication.getInstance().getConfigValue(
							"UserBean", UserBean.class);
					String phoneJson = NetUtil.executeHttpGet(url,
							"DepartCode",
							user.DepartCode[user.DepartCode.length - 1]);

					ResultData<String> departID = new Gson().fromJson(
							phoneJson, new TypeToken<ResultData<String>>() {
							}.getType());

					return departID;
				}

				@Override
				protected void onPostExecute(ResultData<String> departID) {
					if (departID.ResultCode > 0) {
						view.setValue(departID.getSingleData());
					}
				}
			}.executeOnExecutor(MyApplication.executorService);

		} else if (field.equalsIgnoreCase("$Telephone$")) {// 用户电话 $Telephone$

			new AsyncTask<String, String, ResultData<String>>() {
				@Override
				protected ResultData<String> doInBackground(String... params) {

					String url = ServerConnectConfig.getInstance()
							.getMobileBusinessURL()
							+ "/BaseREST.svc/GetPhoneNum";

					String phoneJson = NetUtil.executeHttpGet(url, "UserID",
							MyApplication.getInstance().getUserId() + "");

					ResultData<String> phoneData = new Gson().fromJson(
							phoneJson, new TypeToken<ResultData<String>>() {
							}.getType());

					return phoneData;
				}

				@Override
				protected void onPostExecute(ResultData<String> phoneData) {
					if (phoneData.ResultCode > 0) {
						view.setValue(phoneData.getSingleData());
					}
				}
			}.executeOnExecutor(MyApplication.executorService);
		}

		return null;
	}
}
