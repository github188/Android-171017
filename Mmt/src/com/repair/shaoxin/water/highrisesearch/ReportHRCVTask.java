package com.repair.shaoxin.water.highrisesearch;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.login.UserBean;
import com.zondy.mapgis.geometry.Dot;

public class ReportHRCVTask extends AsyncTask<Void, Void, String> {
	private final Context context;

	private ProgressDialog loadingDialog;

	public ReportHRCVTask(Context context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		loadingDialog = MmtProgressDialog.getLoadingProgressDialog(context, "正在上报数据");
		loadingDialog.show();
	}

	@Override
	protected String doInBackground(Void... params) {
		ReportEntity entity = new ReportEntity();
		entity.UserID = MyApplication.getInstance().getUserId();
		entity.UserName = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).LoginName;

		Dot point = HighRiseCloseValveConstant.queryUser.get(0).getPoint();
		if (point.getX() != 0 && point.getY() != 0) {
			entity.Position = Convert.FormatDouble(point.getX()) + "," + Convert.FormatDouble(point.getY());
		} else {
			entity.Position = Convert.FormatDouble(GpsReceiver.getInstance().getLastLocalLocation().getX()) + ","
					+ Convert.FormatDouble(GpsReceiver.getInstance().getLastLocalLocation().getY());
		}

//		Point point = HighRiseCloseValveConstant.queryUser.get(0).getPoint();
//		if (point.getX() != 0 && point.getY() != 0) {
//			entity.Position = TypeFormat.FormatDouble(point.getX()) + "," + TypeFormat.FormatDouble(point.getY());
//		} else {
//			entity.Position = TypeFormat.FormatDouble(GpsReceiver.getInstance().getLastLocalLocation().getX()) + ","
//					+ TypeFormat.FormatDouble(GpsReceiver.getInstance().getLastLocalLocation().getY());
//		}

		entity.ReportTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

		if (HighRiseCloseValveConstant.MeterNo == null) {
			return "未指定" + HighRiseCloseValveConstant.LAYER_FIELDS;
		}

		entity.MeterNo = HighRiseCloseValveConstant.MeterNo;

		if (HighRiseCloseValveConstant.queryValve.size() == 0) {
			return "未查询到用户关联的阀门";
		}

		entity.ValveList = new ArrayList<>(HighRiseCloseValveConstant.queryValve);

		int total = 0;
		for (String str : entity.ValveList) {
			if (str.contains(",")) {
				total = total + str.split(",").length;
			} else {
				total = total + 1;
			}
		}

		entity.ValveNum = total;

		try {
			String uri = ServerConnectConfig.getInstance().getBaseServerPath()
					+ "/Services/Zondy_MapGISCitySvr_HighFloor_SX/REST/HighFloorREST.svc/MobileReportInfos?userID="
					+ entity.UserID + "&position=" + entity.Position + "&reportTime="
					+ URLEncoder.encode(entity.ReportTime, "utf-8") + "&meterNo=" + entity.MeterNo + "&valveNum="
					+ entity.ValveNum + "&valvaList=" + URLEncoder.encode(entity.listToString(), "utf-8");
			return NetUtil.executeHttpGet(uri);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onPostExecute(String result) {
		loadingDialog.cancel();

		if (result == null) {
			Toast.makeText(context, "上报失败,请确认服务是否存在", Toast.LENGTH_SHORT).show();
			return;
		}

		result = result.replace("\"", "");

		Toast.makeText(context, result, Toast.LENGTH_SHORT).show();

		if ("上报成功".equals(result)) {
			HighRiseCloseValveConstant.queryUser.clear();
			HighRiseCloseValveConstant.queryValve.clear();

//			MapGISFrameMenuFactory.getMenuInstance("高层关阀").onOptionsItemSelected(SessionManager.MainActivity,
//					SessionManager.MainMapView);
		}
	}

	class ReportEntity {
		public int UserID;
		public String UserName;
		public String Position;
		public String ReportTime;
		public String MeterNo;
		public int ValveNum;
		public List<String> ValveList;

		public String listToString() {

			if (ValveList == null || ValveList.size() == 0) {
				return "";
			}

			if (ValveList.size() == 0) {
				return ValveList.get(0);
			} else {

				HashMap<String, String> map = new HashMap<>();

				for (String str : ValveList) {
					String[] valve = str.split(":");

					if (map.containsKey(valve[0])) {
						String value = map.get(valve[0]);
						map.put(valve[0], value + "," + valve[1]);
					} else {
						map.put(valve[0], valve[1]);
					}
				}

				String result = "";

				for (String layer : map.keySet()) {
					result = result + layer + ":" + map.get(layer) + "|";
				}

				return result.substring(0, result.length() - 1);
			}
		}
	}
}
