package com.maintainproduct.v2.caselist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;

/**
 * V2版 维修工单
 * 
 * @author meikai
 */
public class MaintainGDListActivity extends BaseActivity {

	private MaintainGDListFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getBaseTextView().setText("维修工单");

		// 显示定位按钮
		getBaseRightImageView().setVisibility(View.VISIBLE);
		getBaseRightImageView().setImageResource(R.drawable.common_location);
		getBaseRightImageView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fragment.showOnMap();
			}
		});

		fragment = new MaintainGDListFragment();
		addFragment(fragment);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fragment.onActivityResult(requestCode, resultCode, data);
	}
}
