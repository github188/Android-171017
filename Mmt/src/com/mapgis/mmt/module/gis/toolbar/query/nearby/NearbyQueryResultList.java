package com.mapgis.mmt.module.gis.toolbar.query.nearby;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.R;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.gis.spatialquery.SpatialSearchResultList;

public class NearbyQueryResultList extends SpatialSearchResultList {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setCustomView(initMyTitleView());

		setBottomViewVisible(View.GONE);
	}

	/**
	 * 自定义标题视图
	 * 
	 * @return
	 */
	private View initMyTitleView() {
		View view = LayoutInflater.from(NearbyQueryResultList.this).inflate(R.layout.header_bar_plan_name, null);

		((TextView) view.findViewById(R.id.tvPlanName)).setText("附近查询结果列表");
		((TextView) view.findViewById(R.id.tvTaskState)).setText(getIntent().getStringExtra("whereInfo"));

		// 返回按钮
		view.findViewById(R.id.tvPlanBack).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AppManager.finishActivity(NearbyQueryResultList.this);
			}
		});

		((ImageView) view.findViewById(R.id.ivPlanDetail)).setImageResource(R.drawable.login_setting);
		view.findViewById(R.id.ivPlanDetail).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(ResultCode.RESULT_WHERE_SELECTED);
				AppManager.finishActivity(NearbyQueryResultList.this);
			}
		});

		return view;
	}
}