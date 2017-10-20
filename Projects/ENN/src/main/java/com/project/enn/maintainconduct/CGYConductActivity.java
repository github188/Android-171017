package com.project.enn.maintainconduct;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.casemanage.casehandover.CaseHandoverActivity;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.consumables.MaterialListActivity;
import com.repair.zhoushan.module.devicecare.consumables.PurchaseOrderListActivity;

public class CGYConductActivity extends CaseHandoverActivity {

    private ScheduleTask mScheduleTask;

    @Override
    protected void onCreateCus() {
        super.onCreateCus();
        initData();
    }

    private void initData() {

        // 获取工单的流程对应的编号前缀
        final MmtBaseTask<String, Void, ResultData<String>> mmtBaseTask = new MmtBaseTask<String, Void, ResultData<String>>(CGYConductActivity.this) {
            @Override
            protected ResultData<String> doInBackground(String... params) {
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/FetchPreCodeByFlowName?flowName="
                        + params[0];

                String result = NetUtil.executeHttpGet(url);

                if (TextUtils.isEmpty(result)) {
                    return null;
                }
                ResultData<String> resultData = new Gson().fromJson(result, new TypeToken<ResultData<String>>() {
                }.getType());

                return resultData;
            }

            @Override
            protected void onSuccess(ResultData<String> resultData) {

                String defErrMsg = "获取编号前缀信息失败";

                if (resultData == null) {
                    Toast.makeText(CGYConductActivity.this, defErrMsg, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (resultData.ResultCode != 200) {
                    Toast.makeText(CGYConductActivity.this,
                            TextUtils.isEmpty(resultData.ResultMessage) ? defErrMsg : resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (resultData.DataList.size() == 0) {
                    Toast.makeText(CGYConductActivity.this, "未查询到流程的编号前缀信息", Toast.LENGTH_SHORT).show();
                }

                // 目前只初始化用到的两个字段
                mScheduleTask = new ScheduleTask();
                mScheduleTask.TaskCode = caseItemEntity.CaseNo;
                mScheduleTask.PreCodeFormat = resultData.getSingleData();
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute(caseItemEntity.FlowName);
    }


    @Override
    protected void createBottomView() {
        BottomUnitView backUnitView = new BottomUnitView(CGYConductActivity.this);
        backUnitView.setContent("材料");
        backUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(backUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mScheduleTask != null) {
                    ListDialogFragment listDialogFragment = new ListDialogFragment("材料选择", new String[]{"物料清单", "采购订单"});
                    listDialogFragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int arg2, String value) {
                            if (value.equals("物料清单")) {
                                Intent intent = new Intent(CGYConductActivity.this, MaterialListActivity.class);
                                intent.putExtra("ListItemEntity", mScheduleTask);
                                startActivity(intent);

                            } else if (value.equals("采购订单")) {
                                Intent intent = new Intent(CGYConductActivity.this, PurchaseOrderListActivity.class);
                                intent.putExtra("ListItemEntity", mScheduleTask);
                                startActivity(intent);
                            }
                        }
                    });
                    listDialogFragment.show(getSupportFragmentManager(), "");
                } else {

                    Toast.makeText(CGYConductActivity.this, "未获取到流程的编号前缀信息", Toast.LENGTH_SHORT).show();
                }
            }
        });

        super.createBottomView();
    }
}
