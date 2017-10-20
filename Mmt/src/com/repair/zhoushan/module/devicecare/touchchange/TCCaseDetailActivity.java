package com.repair.zhoushan.module.devicecare.touchchange;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.CaseInfo;
import com.repair.zhoushan.entity.FlowInfoConfig;
import com.repair.zhoushan.module.casemanage.casedetail.CaseBackTask;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;
import com.repair.zhoushan.module.casemanage.mycase.MyCaseListActivity;

/**
 * Created by liuyunfan on 2016/3/14.
 */
public class TCCaseDetailActivity extends CaseDetailActivity {
    FlowInfoConfig flowInfoConfig;

    @Override
    protected void createBottomView() {
        // 当前节点不是开始节点就有“回退”按钮
        if ((flowInfoConfig = flowInfoItemList.get(editableFormIndex).FlowInfoConfig).NodeType != 1) {
            BottomUnitView backUnitView = new BottomUnitView(TCCaseDetailActivity.this);
            backUnitView.setContent("回退");
            backUnitView.setImageResource(R.drawable.handoverform_report);

            addBottomUnitView(backUnitView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final View backView = getLayoutInflater().inflate(R.layout.maintenance_back, null);
                    final OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("回退原因", backView);

                    okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                        @Override
                        public void onRightButtonClick(View view) {
                            CaseInfo caseInfo = caseItemEntity.mapToCaseInfo();
                            caseInfo.Opinion = ((EditText) backView.findViewById(R.id.maintenanceBackReason)).getText().toString();
                            new CaseBackTask(TCCaseDetailActivity.this, true, "CaseHandBack", "回退失败",
                                    new MmtBaseTask.OnWxyhTaskListener<String>() {
                                @Override
                                public void doAfter(String result) {

                                    if (!TextUtils.isEmpty(result)) {
                                        Toast.makeText(TCCaseDetailActivity.this, result, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(TCCaseDetailActivity.this, "回退成功", Toast.LENGTH_SHORT).show();
                                        //成功后自己打开自己，达到重置界面的目的
                                        Intent intent = new Intent(TCCaseDetailActivity.this, MyCaseListActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                    okCancelDialogFragment.dismiss();
                                }
                            }).mmtExecute(caseInfo);
                        }
                    });
                    okCancelDialogFragment.setCancelable(true);
                    okCancelDialogFragment.show(getSupportFragmentManager(), "1");
                }
            });
        }


        BottomUnitView manageUnitView = new BottomUnitView(TCCaseDetailActivity.this);
        manageUnitView.setContent("延长停气");
        manageUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GDFormBean gdFormBean = GDFormBean.generateSimpleForm(
                        new String[]{"DisplayName", "延长时间", "Name", "延长时间", "Type", "日期框", "Validate", "1"},
                        new String[]{"DisplayName", "延长原因", "Name", "延长原因", "Type", "短文本", "DisplayColSpan", "100"});
                Intent intent = new Intent(TCCaseDetailActivity.this, DelayStopGasDialogActivity.class);
                intent.putExtra("Title", "申请延长停气");
                intent.putExtra("GDFormBean", gdFormBean);
                intent.putExtra("caseno", caseItemEntity.EventCode);
                startActivity(intent);
            }
        });

        BottomUnitView fbUnitView = new BottomUnitView(TCCaseDetailActivity.this);
        fbUnitView.setContent("反馈");
        fbUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(fbUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TCCaseDetailActivity.this, TCCaseFBActivity.class);
                intent.putExtra("ListItemEntity", caseItemEntity);
                intent.putExtra("FlowInfoConfig", new Gson().toJson(flowInfoConfig));
                intent.putExtra("FlowInfoItem", new Gson().toJson(flowInfoItemList));
                startActivity(intent);
            }
        });

        super.multFBAndAssistModule();
    }
}
