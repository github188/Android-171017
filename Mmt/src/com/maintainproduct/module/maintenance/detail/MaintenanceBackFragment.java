package com.maintainproduct.module.maintenance.detail;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;

/** 工单回退 */
public class MaintenanceBackFragment extends OkCancelDialogFragment {

	public MaintenanceBackFragment(String title) {
		super(title);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		View backView = getActivity().getLayoutInflater().inflate(R.layout.maintenance_back, null);
		view.findViewById(R.id.layout_ok_cancel_dialog_content).setVisibility(View.VISIBLE);
		((LinearLayout) view.findViewById(R.id.layout_ok_cancel_dialog_content)).addView(backView);

		view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MaintainSimpleInfo itemEntity = getActivity().getIntent().getParcelableExtra("ListItemEntity");

				BackEntity backEntity = new BackEntity();
				backEntity.UserID = MyApplication.getInstance().getUserId();
				backEntity.ApplicationTime = BaseClassUtil.getSystemTime();
				backEntity.Applicator = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;
				backEntity.CaseNo = itemEntity.CaseNo;
				backEntity.CaseStep = itemEntity.ActiveName;
				backEntity.Reason = ((EditText) getView().findViewById(R.id.maintenanceBackReason)).getText().toString().length()==0?"退单":"退单:"+((EditText) getView().findViewById(R.id.maintenanceBackReason)).getText().toString();
				backEntity.ID0 = itemEntity.ID0;

				String json = new Gson().toJson(backEntity, BackEntity.class);

				String uri = ServerConnectConfig.getInstance().getBaseServerPath()
						+ "/Services/MapgisCity_WorkFlowForm/REST/WorkFlowFormREST.svc/ApplicateCaseBack";

				ReportInBackEntity entity = new ReportInBackEntity(json, backEntity.UserID, ReportInBackEntity.REPORTING, uri,
						backEntity.CaseNo, "工单回退", null, null);

				long line = entity.insert();

				if (line > 0) {
					Toast.makeText(getActivity(), "信息保存成功!", Toast.LENGTH_SHORT).show();
					dismiss();

					getActivity().setResult(Activity.RESULT_OK);
					AppManager.finishActivity(getActivity());

				}

				// new MaintenanceBackTask().executeOnExecutor(MyApplication.executorService,backEntity);
			}
		});

	}

	class BackEntity {
		public int UserID;
		public String ApplicationTime;
		public String Applicator;
		public String CaseNo;
		public String CaseStep;
		public int ID;
		public String Reason;
		public int ID0;
	}

}
