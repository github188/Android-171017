package com.patrolproduct.module.myplan.feedback;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.Hashtable;

public class MyPlanFeedback extends BaseActivity {
	private ProgressDialog loadingDialog;

	public static Hashtable<String, ArrayList<PointFeedbackWordsModel>> allfeedbackItems = new Hashtable<String, ArrayList<PointFeedbackWordsModel>>();
	private ArrayList<PointFeedbackWordsModel> models = new ArrayList<PointFeedbackWordsModel>();

	private MyPlanFeedbackFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String PlanTypeID = getIntent().getStringExtra("PlanTypeID");
		String planName = getIntent().getStringExtra("planName");

		getBaseTextView().setText(planName);

		getBaseRightImageView().setVisibility(View.VISIBLE);
		getBaseRightImageView().setImageResource(R.drawable.flex_flow_report);
		getBaseRightImageView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (fragment != null) {
					fragment.planFeedback();
				}
			}
		});

		PlanTypeID = PlanTypeID == null ? "" : PlanTypeID;

		if (!allfeedbackItems.containsKey(PlanTypeID)) {
			getFbGroupByTypeAsync(PlanTypeID);
		} else {
			models = allfeedbackItems.get(PlanTypeID);

			addFragment();
		}
	}

	private void addFragment() {
//		fragment = new MyPlanFeedbackFragment(models);
		fragment = MyPlanFeedbackFragment.newInstance(models);
		addFragment(fragment);
	}

	private void getFbGroupByTypeAsync(final String PlanTypeID) {

		new AsyncTask<String, Void, String>() {

			@Override
			protected void onPreExecute() {
				loadingDialog = MmtProgressDialog.getLoadingProgressDialog(MyPlanFeedback.this, "向服务器请求数据中，请稍候...");
				loadingDialog.show();
			}

            @Override
			protected String doInBackground(String... params) {
				String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/PatrolREST.svc/GetFbGroupByType";
				String jsonStr = NetUtil.executeHttpGet(url, "PatrolTypeList", params[0], "f", "json");
				return jsonStr;
			}

			@Override
			protected void onPostExecute(String result) {

				try {

					if (BaseClassUtil.isNullOrEmptyString(result)) {
						showErrorMsg("获取反馈信息失败,请检查网络情况或者服务版本");
					}

					ResultData<FeedbackGroup> groups = new Gson().fromJson(result, new TypeToken<ResultData<FeedbackGroup>>() {
					}.getType());

					if (groups.ResultCode < 0) {
						showErrorMsg("获取反馈信息失败:" + groups.ResultMessage);
						return;
					}

					models = new ArrayList<PointFeedbackWordsModel>();

					for (FeedbackGroup p : groups.DataList) {
						PointFeedbackWordsModel model = new PointFeedbackWordsModel();

						model.feedbackDescription = p.FeedbackDesc;
						model.feedbackType = p.FeedbackType;
						model.group = p.FeedbackGroup;

						model.isTrigger = p.IsTrigger;
						model.trigCondition = p.TrigCondition;
						model.isMust = p.IsMust;

						models.add(model);
					}

					allfeedbackItems.put(PlanTypeID, models);

					addFragment();
				} catch (Exception e) {
					e.printStackTrace();

					showErrorMsg("获取反馈信息失败,请检查网络情况或者服务版本");
				} finally {
					loadingDialog.cancel();
				}
			}
        }.executeOnExecutor(MyApplication.executorService, PlanTypeID);
	}

}
