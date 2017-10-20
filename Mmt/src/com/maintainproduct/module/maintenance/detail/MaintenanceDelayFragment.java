package com.maintainproduct.module.maintenance.detail;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.maintainproduct.module.casehandover.CaseHandoverUserFragment;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;

/**
 * 工单延期
 */
public class MaintenanceDelayFragment extends OkCancelDialogFragment {

    public MaintenanceDelayFragment(String title) {
        super(title);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View backView = getActivity().getLayoutInflater().inflate(R.layout.maintenance_delay, null);
        view.findViewById(R.id.layout_ok_cancel_dialog_content).setVisibility(View.VISIBLE);
        ((LinearLayout) view.findViewById(R.id.layout_ok_cancel_dialog_content)).addView(backView);

        view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MaintainSimpleInfo itemEntity = getActivity().getIntent().getParcelableExtra("ListItemEntity");

                DelayEntity delayEntity = new DelayEntity();
                delayEntity.UserID = MyApplication.getInstance().getUserId();
                delayEntity.ApplicationTime = BaseClassUtil.getSystemTime();
                delayEntity.Applicator = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;
                delayEntity.CaseNo = itemEntity.CaseNo;
                delayEntity.CaseStep = itemEntity.ActiveName;
                delayEntity.Reason = ((EditText) getView().findViewById(R.id.maintenanceDelayReason)).getText().toString();

                if (((EditText) getView().findViewById(R.id.maintenanceDelayLength)).getText().toString().trim().length() == 0) {
                    Toast.makeText(getActivity(), "请填写延期时长", Toast.LENGTH_SHORT).show();
                    return;
                }

                delayEntity.DelayTimeLength = Integer.valueOf(((EditText) getView().findViewById(R.id.maintenanceDelayLength))
                        .getText().toString());

                new MaintenanceDelayTask().executeOnExecutor(MyApplication.executorService, delayEntity);
            }
        });

    }

    class MaintenanceDelayTask extends AsyncTask<DelayEntity, String, ResultData<Integer>> {

        @Override
        protected ResultData<Integer> doInBackground(DelayEntity... params) {
            ResultData<Integer> result = null;

            try {
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_WorkFlowForm/REST/WorkFlowFormREST.svc/ApplicateCaseDelay";

                String data = new Gson().toJson(params[0], DelayEntity.class);

                String resultStr = NetUtil.executeHttpPost(url, data, "Content-Type", "application/json; charset=utf-8");

                result = new Gson().fromJson(resultStr, new TypeToken<ResultData<Integer>>() {
                }.getType());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(ResultData<Integer> result) {
            if (result == null) {
                Toast.makeText(getActivity(), "延期申请失败,联系技术人员解决", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), result.ResultMessage, Toast.LENGTH_SHORT).show();
            }

            if (result.ResultCode > 0) {

                int stepId = result.getSingleData();

                MaintainSimpleInfo itemEntity = getActivity().getIntent().getParcelableExtra("ListItemEntity");
                itemEntity.ID0 = stepId;

                CaseHandoverUserFragment fragment = new CaseHandoverUserFragment(itemEntity);
                fragment.show(getChildFragmentManager(), "");

                dismiss();

                getActivity().setResult(Activity.RESULT_OK);
                AppManager.finishActivity(getActivity());
            }

        }
    }

    class DelayEntity {
        public int UserID;
        public String ApplicationTime;
        public String Applicator;
        public String CaseNo;
        public String CaseStep;
        public String CurrentEndTime;
        public String DelayCaseNo;
        public int DelayTimeLength;
        public int ID;
        public String Reason;
    }

}
