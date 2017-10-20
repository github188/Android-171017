package com.maintainproduct.module.maintenance.detail;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;

public class MaintenanceFlowStepTask extends AsyncTask<MaintainSimpleInfo, String, ResultData<GDFormBean>> {
	private final BaseActivity activity;
	private final Handler handler;

	private MaintainSimpleInfo itemEntity;

	public MaintenanceFlowStepTask(BaseActivity activity, Handler handler) {
		this.activity = activity;
		this.handler = handler;
	}

	@Override
	protected ResultData<GDFormBean> doInBackground(MaintainSimpleInfo... params) {

		itemEntity = params[0];

		// 通过服务获取表单配置信息
		String url = ServerConnectConfig.getInstance().getBaseServerPath()
				+ "/Services/MapgisCity_WorkFlowForm/REST/WorkFlowFormREST.svc/GetGDFormByFlowStep";

		String json = NetUtil.executeHttpGet(url, "flow", itemEntity.FlowName, "step", itemEntity.ActiveName + "_移动");

		ResultData<GDFormBean> data = new Gson().fromJson(json, new TypeToken<ResultData<GDFormBean>>() {
		}.getType());

		return data;
	}

	@Override
	protected void onPreExecute() {
		activity.setBaseProgressBarVisibility(true);
	}

	@Override
	protected void onPostExecute(final ResultData<GDFormBean> result) {
		activity.setBaseProgressBarVisibility(false);

		// 若未获取到表单信息，则进入移交界面
		if (result == null || result.ResultCode < 0 || result.getSingleData() == null || result.getSingleData().Groups == null
				|| result.getSingleData().Groups.length == 0) {

			if (!itemEntity.ActiveName.contains("审核")) {// 非审核节点，手动选择受理人
				handler.sendEmptyMessage(MaintenanceConstant.SERVER_SELECT_NEXT);
			} else {
				handler.sendEmptyMessage(MaintenanceConstant.SERVER_DEFAULT_NEXT);
			}
		} else {
			Message msg = handler.obtainMessage();
			msg.what = MaintenanceConstant.SERVER_GET_FEEDBACK;
			msg.obj = result;
			handler.sendMessage(msg);
		}
	}
}
