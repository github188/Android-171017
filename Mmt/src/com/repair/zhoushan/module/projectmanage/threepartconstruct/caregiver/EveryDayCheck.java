package com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by liuyunfan on 2015/12/10.
 */
public class EveryDayCheck extends BaseActivity {

    private String tableName;
    private String groupName;
    private CaseItem caseItem;
    private FlowNodeMeta flowNodeMeta;

    private FlowBeanFragment formBeanFragment;
    public final FlowInfoPostParam flowInfoPostParam = new FlowInfoPostParam();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent outerIntent = getIntent();
        this.tableName = outerIntent.getStringExtra("TableName");
        this.groupName = outerIntent.getStringExtra("GroupName");
        this.caseItem = outerIntent.getParcelableExtra("ListItemEntity");

        if (caseItem == null || TextUtils.isEmpty(tableName) || TextUtils.isEmpty(groupName)) {
            Toast.makeText(EveryDayCheck.this, "未获取到流程信息", Toast.LENGTH_SHORT).show();
            return;
        }

        initData();
    }

    private void initData() {

        MmtBaseTask<String, Void, ResultData<FlowNodeMeta>> mmtBaseTask = new MmtBaseTask<String, Void, ResultData<FlowNodeMeta>>(EveryDayCheck.this) {
            @Override
            protected ResultData<FlowNodeMeta> doInBackground(String... params) {

                ResultData<FlowNodeMeta> resultData;

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetTableGroupMeta?tableName="
                        + tableName + "&uiGroup=" + groupName;

                try {
                    String jsonResult = NetUtil.executeHttpGet(url);

                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取界面数据失败：网络错误");
                    }
                    Results<FlowNodeMeta> results = new Gson().fromJson(jsonResult, new TypeToken<Results<FlowNodeMeta>>() {
                    }.getType());
                    resultData = results.toResultData();

                } catch (Exception e) {
                    e.printStackTrace();
                    resultData = new ResultData<>();
                    resultData.ResultCode = -100;
                    resultData.ResultMessage = e.getMessage();
                }

                return resultData;
            }

            @Override
            protected void onSuccess(ResultData<FlowNodeMeta> resultData) {
                if (resultData.ResultCode != 200) {
                    Toast.makeText(EveryDayCheck.this, resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (resultData.DataList == null || resultData.DataList.size() == 0) {
                    Toast.makeText(EveryDayCheck.this, "未获取到界面结构", Toast.LENGTH_SHORT).show();
                    return;
                }

                flowNodeMeta = resultData.getSingleData();

                createView();
                createBottomView();
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();

    }

    private void createView() {

        // 将Fragment显示在界面上
        formBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", flowNodeMeta.mapToGDFormBean());
        formBeanFragment.setArguments(args);
        formBeanFragment.setCls(EveryDayCheck.class);
        formBeanFragment.setFragmentFileRelativePath(caseItem.CaseNo);
        formBeanFragment.setAddEnable(true);

        addFragment(formBeanFragment);
    }

    private void createBottomView() {

        BottomUnitView backUnitView = new BottomUnitView(EveryDayCheck.this);
        backUnitView.setContent("保存");
        backUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(backUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {

                OkCancelDialogFragment fragment = new OkCancelDialogFragment("是否确定保存？");
                fragment.setRightBottonText("取消");
                fragment.setLeftBottonText("保存");
                fragment.setOnLeftButtonClickListener(new OkCancelDialogFragment.OnLeftButtonClickListener() {
                    @Override
                    public void onLeftButtonClick(View view) {
                        saveEvent();
                    }
                });
                fragment.show(getSupportFragmentManager(), "");
            }
        });
    }


    private void saveEvent() {

        if (formBeanFragment == null) {
            return;
        }

        flowInfoPostParam.flowNodeMeta = flowNodeMeta;
        flowInfoPostParam.caseInfo = caseItem.mapToCaseInfo();

        // 创建服务路径
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/FeedbackCaseData?table=" + tableName + "&group=" + groupName;

        List<FeedItem> feedbackItems = formBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

        if (feedbackItems == null) {
            return;
        }
        // 把Feedback的值映射到Value上
        ArrayList<FlowNodeMeta.TableValue> values = flowInfoPostParam.flowNodeMeta.Values;
        for (FlowNodeMeta.TableValue value : values) {
            for (FeedItem item : feedbackItems) {
                if (value.FieldName.equals(item.Name)) {
                    value.FieldValue = item.Value;
                    break;
                }
            }
        }

        flowInfoPostParam.caseInfo.EventMainTable = "第三方施工监管表";

        // 将对信息转换为JSON字符串
        String data = new Gson().toJson(flowInfoPostParam, new TypeToken<FlowInfoPostParam>() {
        }.getType());

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                data,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                this.caseItem.ActiveName,
                formBeanFragment.getAbsolutePaths(),
                formBeanFragment.getRelativePaths());

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {

                        OkDialogFragment fragment = new OkDialogFragment("上报保存成功！");
                        fragment.setOnButtonClickListener(new OkDialogFragment.OnButtonClickListener() {
                            @Override
                            public void onButtonClick(View view) {
                                backByReorder();
                            }
                        });
                        fragment.setCancelable(false);
                        fragment.show(getSupportFragmentManager(), "");

                    } else {
                        Toast.makeText(EveryDayCheck.this, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }

}
