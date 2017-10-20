package com.repair.zhoushan.module.eventmanage.eventoverview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.CaseProcedure;
import com.maintainproduct.entity.CaseProcedureResult;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.casemanage.casedetail.LineLinearLayout;

import java.util.List;
import java.util.UUID;

public class CaseProcedureActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    private void initView() {

        getBaseTextView().setText("流程进展");

        CaseProcedureFragment fragment = new CaseProcedureFragment();
        Bundle args = new Bundle();
        args.putString("CaseNo", getIntent().getStringExtra("CaseNo"));
        fragment.setArguments(args);

        addFragment(fragment);
    }

    public static class CaseProcedureFragment extends Fragment {

        private String caseNo;
        private LineLinearLayout mLineLinearLayout;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Bundle args = getArguments();
            if (args != null && args.containsKey("CaseNo")) {
                this.caseNo = args.getString("CaseNo");
            } else {
                Toast.makeText(getActivity(), "未获取到参数", Toast.LENGTH_SHORT).show();
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.handle_procedure_view, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mLineLinearLayout = (LineLinearLayout) view.findViewById(R.id.line_layout);

            loadData();
        }

        private void loadData() {

            if (TextUtils.isEmpty(caseNo)) {
                return;
            }

            MmtBaseTask<Void, Void, CaseProcedureResult> mmtBaseTask = new MmtBaseTask<Void, Void, CaseProcedureResult>(getActivity()) {
                @Override
                protected CaseProcedureResult doInBackground(Void... params) {

                    CaseProcedureResult resultData;

                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/MapgisCity_WorkFlow/REST/WorkFlowREST.svc/WorkFlow/FetchCaseProcedure?caseNo="
                            + caseNo + "&_token=" + UUID.randomUUID().toString();

                    try {
                        String jsonStr = NetUtil.executeHttpGet(url);

                        if (TextUtils.isEmpty(jsonStr)) {
                            throw new Exception("获取数据失败");
                        }

                        jsonStr = jsonStr.replace("[\"[", "[[").replace("]\"]", "]]").replace("\\", "");

                        resultData = new Gson().fromJson(jsonStr, CaseProcedureResult.class);

                    } catch (Exception e) {
                        e.printStackTrace();

                        resultData = new CaseProcedureResult();
                        resultData.ResultCode = -100;
                        resultData.ResultMessage = e.getMessage();
                    }

                    return resultData;
                }

                @Override
                protected void onSuccess(CaseProcedureResult result) {
                    if (result.ResultCode > 0) {
                        setContent(result.DataList.get(0));
                    } else {
                        Toast.makeText(getActivity(), result.ResultMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            };
            mmtBaseTask.setCancellable(false);
            mmtBaseTask.mmtExecute();

        }

        private void setContent(List<CaseProcedure> caseProcedures) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            for (CaseProcedure caseProcedure : caseProcedures) {

                View view = layoutInflater.inflate(R.layout.handle_procedure_item, mLineLinearLayout, false);

                String handOverType = caseProcedure.HandOverDirection == 1 ? "移交" : (caseProcedure.HandOverDirection > 1 ? "创建" : "回退");
                ((TextView) view.findViewById(R.id.txt_action)).setText(caseProcedure.StepName + " - " + handOverType);

                ((TextView) view.findViewById(R.id.txt_action_undertakeman)).setText("承办人：" + caseProcedure.UndertakeDept + " - " + caseProcedure.UndertakeMan);
                ((TextView) view.findViewById(R.id.txt_action_desc)).setText("交办意见：" + (TextUtils.isEmpty(caseProcedure.UndertakeOpinion) ? "无" : caseProcedure.UndertakeOpinion));

                ((TextView) view.findViewById(R.id.txt_action_time)).setText("承办时间：" + caseProcedure.UndertakeTime);
                TextView tvFinishTime = (TextView) view.findViewById(R.id.txt_action_time1);
                tvFinishTime.setVisibility(View.VISIBLE);
                tvFinishTime.setText("办完时间：" + caseProcedure.FinishTime);

                mLineLinearLayout.addView(view);

            }

        }
    }
}
