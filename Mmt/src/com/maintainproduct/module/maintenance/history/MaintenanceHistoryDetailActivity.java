package com.maintainproduct.module.maintenance.history;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.maintainproduct.module.BeanFragment;
import com.maintainproduct.module.BeanFragment.BeanFragmentOnCreate;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.maintainproduct.module.maintenance.detail.FetchCaseProcedureFragment;
import com.maintainproduct.module.maintenance.detail.MaintenanceDetailTask;
import com.maintainproduct.module.maintenance.history.MaintenanceHistoryListActivity.MaintenanceHistoryFragment;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;

import java.util.List;

public class MaintenanceHistoryDetailActivity extends BaseActivity {
	private BeanFragment formBeanFragment;

	private String id;
	private String FlowName;
	private String CaseNo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getBaseTextView().setText("工单详情");

		setBaseProgressBarVisibility(true);

		id = getIntent().getStringExtra("id");
		FlowName = getIntent().getStringExtra("FlowName");
		CaseNo = getIntent().getStringExtra("CaseNo");

		if (BaseClassUtil.isNullOrEmptyString(FlowName)) {
			showErrorMsg("返回数据中未查询到流程名称信息");
			return;
		}

		new MaintenanceDetailTask(MaintenanceHistoryDetailActivity.this, handler).executeOnExecutor(
				MyApplication.executorService, id, FlowName + "_工单详情");
	}

	private void showFetchCaseProcedure(String produce) {
		List<String> groupNames = formBeanFragment.getGroupNames();

		if (groupNames != null && groupNames.contains(produce)) {
			int index = groupNames.indexOf(produce);

			MaintainSimpleInfo itemEntity = new MaintainSimpleInfo();
			itemEntity.CaseNo = CaseNo;

			FetchCaseProcedureFragment caseProcedureFragment = new FetchCaseProcedureFragment(itemEntity);
			formBeanFragment.replaceFragment(caseProcedureFragment, index);

			if (formBeanFragment.getShowFragmentIndex() != 0) {
				getSupportFragmentManager().beginTransaction().hide(caseProcedureFragment).commitAllowingStateLoss();
			}

		}
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MaintenanceConstant.SERVER_GET_DETAIL_SUCCESS:
				// 将Fragment显示在界面上
				final GDFormBean bean = (GDFormBean) msg.obj;
				bean.setOnlyShow();
				formBeanFragment = new BeanFragment(bean);
				formBeanFragment.setCls(MaintenanceHistoryFragment.class);
				formBeanFragment.setFragmentFileRelativePath("Repair/" + CaseNo + "/");
				formBeanFragment.setAddEnable(false);
				formBeanFragment.setBeanFragmentOnCreate(new BeanFragmentOnCreate() {
					@Override
					public void onCreated() {
						if (bean.hasGroupName("办理过程")) {
							showFetchCaseProcedure("办理过程");
						}
					}
				});

				addFragment(formBeanFragment);

				setBaseProgressBarVisibility(false);

				break;
			}
		}
	};
}
