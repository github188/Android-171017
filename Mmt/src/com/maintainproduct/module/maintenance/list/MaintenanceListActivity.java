package com.maintainproduct.module.maintenance.list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.BaseActivity;

/** 维修养护工单列表 */
public class MaintenanceListActivity extends BaseActivity {

	protected MaintenanceListFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getBaseTextView().setText("工单列表");

		fragment = new MaintenanceListFragment();
		addFragment(fragment);
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// 有数据处理完毕后,会到该界刷新数据
		if (arg1 == Activity.RESULT_OK && arg0 == MaintenanceConstant.DEFAULT_REQUEST_CODE) {
			//fragment.updateData();
            fragment.updateView();
		}
	}

    protected void onNoCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
