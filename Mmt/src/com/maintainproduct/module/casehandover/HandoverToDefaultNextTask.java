package com.maintainproduct.module.casehandover;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.HandoverEntity;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;

/** 将工单移交给默认承办人 */
public class HandoverToDefaultNextTask extends AsyncTask<MaintainSimpleInfo, String, String> {
	private final BaseActivity activity;

	public HandoverToDefaultNextTask(BaseActivity activity) {
		this.activity = activity;
	}

	private MaintainSimpleInfo itemEntity;

	@Override
	protected String doInBackground(MaintainSimpleInfo... params) {

		itemEntity = params[0];

		// 通过服务获取表单配置信息
		String url = ServerConnectConfig.getInstance().getBaseServerPath()
				+ "/Services/MapgisCity_WorkFlowForm/REST/WorkFlowFormREST.svc/GetTimeAxis";
		String result = NetUtil.executeHttpGet(url, "caseNo", itemEntity.CaseNo);

		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		ResultData<FlowInfo> resultData = new Gson().fromJson(result, new TypeToken<ResultData<FlowInfo>>() {
		}.getType());

		final String receivedRexianId = resultData.getSingleData().TransactionManId;

		OkCancelDialogFragment fragment = new OkCancelDialogFragment("确认移交");

		fragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
			@Override
			public void onRightButtonClick(View view) {
				HandoverEntity handoverEntity = new HandoverEntity(itemEntity);
				handoverEntity.undertakeman = "22/" + receivedRexianId;
				handoverEntity.option = "移交案件";

				new CaseHandoverTask().createCaseHandoverData(handoverEntity);

				Toast.makeText(activity, "案件移交保存成功", Toast.LENGTH_SHORT).show();

				activity.setResult(Activity.RESULT_OK);
				AppManager.finishActivity(activity);
			}
		});

		fragment.show(activity.getSupportFragmentManager(), "");

	}

    class FlowInfo {
		public String TransactionManId;
	}
}
