package com.project.enn.selfemployed;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultStatus;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;

public class MeterDetailActivity extends BaseActivity {

    private CaseItem caseItem;
    private CommeicalChangeInfo commercialChangeInfo;

    private FlowNodeMeta flowNodeMeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBaseTextView().setText("表具详情");

        caseItem = getIntent().getParcelableExtra("ListItemEntity");
        commercialChangeInfo = getIntent().getParcelableExtra("CommercialChangeInfo");

        initData();
    }

    private void initData() {

        new FetchMeterTableTask(MeterDetailActivity.this) {

            @Override
            protected void onSuccess(Results<FlowNodeMeta> results) {

                ResultData<FlowNodeMeta> data = results.toResultData();

                if (data.ResultCode != 200) {
                    showErrorMsg(data.ResultMessage);
                    return;
                }

                flowNodeMeta = data.getSingleData();

                for (FlowNodeMeta.TableValue tableValue : flowNodeMeta.Values) {

                    switch (tableValue.FieldName) {
                        case "事件编号":
                            tableValue.FieldValue = caseItem.EventCode;
                            break;
                        case "表钢号":
                            tableValue.FieldValue = commercialChangeInfo.SteelGrade;
                            break;
                        case "类型":
                            tableValue.FieldValue = commercialChangeInfo.Type;
                            break;
                        case "型号":
                            tableValue.FieldValue = commercialChangeInfo.ModelType;
                            break;
                        case "表底数":
                            tableValue.FieldValue = commercialChangeInfo.InitialValue;
                            break;
                        case "表具计量是否正常":
                            tableValue.FieldValue = commercialChangeInfo.MeasurementIsNormal;
                            break;
                        case "是否注油":
                            tableValue.FieldValue = commercialChangeInfo.IsInfuseOil;
                            break;
                        case "备注":
                            tableValue.FieldValue = commercialChangeInfo.Remark;
                            break;
                    }
                }

                createView();
                createBottomView();
            }
        }.mmtExecute();
    }

    private void createView() {

        GDFormBean gdFormBean = flowNodeMeta.mapToGDFormBean();
        gdFormBean.setOnlyShow();

        FlowBeanFragment flowBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", gdFormBean);
        flowBeanFragment.setArguments(args);

        addFragment(flowBeanFragment);
    }

    private void createBottomView() {

        BottomUnitView deleteUnitView = new BottomUnitView(MeterDetailActivity.this);
        deleteUnitView.setContent("删除");
        deleteUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(deleteUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OkCancelDialogFragment fragment = new OkCancelDialogFragment("是否删除该条表具信息");
                fragment.setLeftBottonText("取消");
                fragment.setRightBottonText("删除");
                fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                    @Override
                    public void onRightButtonClick(View view) {
                        doDelete();
                    }
                });
                fragment.show(getSupportFragmentManager(), fragment.getClass().getName());
            }
        });

        BottomUnitView updateUnitView = new BottomUnitView(MeterDetailActivity.this);
        updateUnitView.setContent("修改");
        updateUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(updateUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                Intent intent = new Intent(MeterDetailActivity.this, EditMeterActivity.class);
                intent.putExtra("FlowNodeMetaStr", new Gson().toJson(flowNodeMeta));
                intent.putExtra("CommercialChangeInfo", commercialChangeInfo);
                startActivity(intent);
            }
        });
    }

    private void doDelete() {

        MmtBaseTask<String, Void, ResultStatus> mmtBaseTask = new MmtBaseTask<String, Void, ResultStatus>(MeterDetailActivity.this) {
            @Override
            protected ResultStatus doInBackground(String... params) {

                ResultStatus resultStatus;

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeleteTableData"
                        + "?tableName=" + Uri.encode("工商户置换表具记录表")
                        + "&id=" + params[0];

                try {
                    String jsonResult = NetUtil.executeHttpGet(url);

                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("删除记录失败");
                    }

                    resultStatus = new Gson().fromJson(jsonResult, ResultStatus.class);

                } catch (Exception e) {
                    e.printStackTrace();
                    resultStatus = new ResultStatus("1001", e.getMessage());
                }

                return resultStatus;
            }

            @Override
            protected void onSuccess(ResultStatus resultStatus) {

                ResultWithoutData result = resultStatus.toResultWithoutData();

                if (result.ResultCode != 200) {
                    Toast.makeText(MeterDetailActivity.this, result.ResultMessage, Toast.LENGTH_SHORT).show();
                } else {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute(commercialChangeInfo.ID);
    }

}
