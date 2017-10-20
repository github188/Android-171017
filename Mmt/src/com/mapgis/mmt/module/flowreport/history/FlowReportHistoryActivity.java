package com.mapgis.mmt.module.flowreport.history;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;

public class FlowReportHistoryActivity extends BaseActivity {

	/**
	 * 数据库以反馈记录表名称
	 */
	public static final String DB_TABLE_NAME = "EventHistory";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final EventViewFragment fragment = new EventViewFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.baseFragment, fragment);
		ft.commit();

		getBaseTextView().setText("历史事件");

		getBaseRightImageView().setVisibility(View.VISIBLE);
		getBaseRightImageView().setImageResource(R.drawable.icon_more);
		getBaseRightImageView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fragment.getDateSelectSelfButton().performClick();
			}
		});
	}
}
