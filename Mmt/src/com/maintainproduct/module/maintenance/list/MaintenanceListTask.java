package com.maintainproduct.module.maintenance.list;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 访问服务，获取工单任务列表 */
public class MaintenanceListTask extends AsyncTask<String, Integer, String> {

	private final Handler handler;

	public MaintenanceListTask(Handler handler) {
        this.handler = handler;
    }

	@Override
	protected String doInBackground(String... params) {
		String url = ServerConnectConfig.getInstance().getBaseServerPath()
				+ "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/RepairStandardRest.svc/FetchDoingCaseList";

		String json = NetUtil.executeHttpGet(url, "userID", MyApplication.getInstance().getUserId() + "");

		return json;
	}

	@Override
	protected void onPostExecute(String result) {
		try {
			String jsonStr = result.replace("[\"[", "[[").replace("]\"]", "]]");
			// jsonStr = jsonStr.replace("\\", "");
			jsonStr = jsonStr.replace("\\\"", "\"").replace("\\\\", "\\");

			String rule = "\\d{2}T\\d{2}";
			Pattern pattern = Pattern.compile(rule);
			Matcher matcher = pattern.matcher(jsonStr);
			while (matcher.find()) {
				String str = matcher.group();
				jsonStr = jsonStr.replace(str, str.replace("T", " "));
			}

			JsonFactory jsonFactory = new JsonFactory();
			JsonParser jsonParser = jsonFactory.createJsonParser(jsonStr);

			ArrayList<LinkedHashMap<String, String>> tasks = new ArrayList<LinkedHashMap<String, String>>();

			LinkedHashMap<String, String> current = null;

			while (jsonParser.nextToken() != null) {

				JsonToken token = jsonParser.getCurrentToken();
				String name = jsonParser.getCurrentName();
				String text = jsonParser.getText();

				switch (token) {
				case START_ARRAY:
					break;
				case START_OBJECT:
					current = new LinkedHashMap<String, String>();
					break;
				case VALUE_NUMBER_INT:
					if (current != null) {
						current.put(name, text);
					}
					break;
				case VALUE_STRING:
					if (current != null) {
						current.put(name, text);
					}
					break;
				case END_OBJECT:
					if (current != null) {
						tasks.add(current);
						current = null;
					}
				case END_ARRAY:
					break;
				default:
					break;
				}
			}

			Message msg = handler.obtainMessage();
			msg.what = MaintenanceConstant.SERVER_GET_LIST_SUCCESS;

			if (!tasks.get(0).containsKey("ResultCode")) {
				msg.obj = tasks;
			}

			handler.sendMessage(msg);

		} catch (Exception e) {
			e.printStackTrace();

			handler.sendEmptyMessage(MaintenanceConstant.SERVER_GET_LIST_FAIL);
		}
}
}
