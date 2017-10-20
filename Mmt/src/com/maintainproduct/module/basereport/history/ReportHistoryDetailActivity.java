package com.maintainproduct.module.basereport.history;

import android.os.AsyncTask;
import android.os.Bundle;

import com.maintainproduct.entity.BaseReportEntity;
import com.maintainproduct.module.BeanFragment;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;

public class ReportHistoryDetailActivity extends BaseActivity {
	private BaseReportEntity entity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getBaseTextView().setText("详情信息");

		entity = getIntent().getParcelableExtra("BaseReportEntity");

		new AsyncTask<Void, Void, BeanFragment>() {
			@Override
			protected void onPreExecute() {
				setBaseProgressBarVisibility(true);
			}

			@Override
			protected BeanFragment doInBackground(Void... params) {
				BeanFragment fragment = new BeanFragment(entity.toFormBean());
				fragment.setAddEnable(false);
				fragment.setFormOnlyShow();
				fragment.setFragmentFileRelativePath(entity.RelativePath);
				return fragment;
			}

			@Override
			protected void onPostExecute(BeanFragment result) {
				setBaseProgressBarVisibility(false);
				addFragment(result);
			}
		}.executeOnExecutor(MyApplication.executorService);
	}
}
