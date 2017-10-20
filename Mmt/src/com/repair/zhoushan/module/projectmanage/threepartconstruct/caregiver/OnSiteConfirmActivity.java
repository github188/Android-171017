package com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.entity.CaseInfo;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.EventInfoPostParam;
import com.repair.zhoushan.entity.FlowInfoItem;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.casemanage.mycase.MyCaseListActivity;

import java.util.List;
import java.util.UUID;

public class OnSiteConfirmActivity extends BaseActivity {

    private FlowBeanFragment formBeanFragment;

    private CaseItem caseItemEntity;

    private List<FlowInfoItem> flowInfoItemList;
    // 当前节点的表单信息，用于构造界面
    private FlowInfoItem flowInfoItem;

    // 上报信息体
    public final EventInfoPostParam eventInfoPostParam = new EventInfoPostParam();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent outIntent = getIntent();

        this.caseItemEntity = outIntent.getParcelableExtra("ListItemEntity");
        if (caseItemEntity == null) {
            this.showErrorMsg("未获取到流程信息");
            return;
        }

        String flowInfoItemListStr = outIntent.getStringExtra("FlowInfoItemList");
        if (!BaseClassUtil.isNullOrEmptyString(flowInfoItemListStr)) {
            flowInfoItemList = new Gson().fromJson(flowInfoItemListStr, new TypeToken<List<FlowInfoItem>>() {
            }.getType());
        }
        if (flowInfoItemList == null || flowInfoItemList.size() == 0) {
            this.showErrorMsg("未获取到流程节点信息");
            return;
        }

        for (FlowInfoItem flowInfoItem : flowInfoItemList) {
            if (flowInfoItem.FlowInfoConfig.ViewState.equalsIgnoreCase("edit")) {
                this.flowInfoItem = flowInfoItem;
            }
        }
        if (flowInfoItem == null) {
            this.showErrorMsg("未获取到当前流程节点信息");
            return;
        }

        createView();
        createBottomView();
    }

    private void createView() {

        getBaseTextView().setText(flowInfoItem.FlowInfoConfig.NodeName);
        eventInfoPostParam.DataParam.flowNodeMeta = flowInfoItem.FlowNodeMeta;

        // 将Fragment显示在界面上
        formBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", flowInfoItem.FlowNodeMeta.mapToGDFormBean());
        formBeanFragment.setArguments(args);

        formBeanFragment.setFragmentFileRelativePath(caseItemEntity.CaseNo); // caseItemEntity.EventCode
        formBeanFragment.setCls(OnSiteConfirmActivity.class);
        formBeanFragment.setAddEnable(true);

        addFragment(formBeanFragment);

    }

    private void createBottomView() {

//        // 客户要求： 直接点击"关单"后，后台无信息反馈，也无法重新派单，故建议删除此"关单"按钮
//        BottomUnitView backUnitView = new BottomUnitView(OnSiteConfirmActivity.this);
//        backUnitView.setContent("关单");
//        backUnitView.setImageResource(R.drawable.handoverform_report);
//
//        addBottomUnitView(backUnitView, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                caseFinish(1);
//            }
//        });

        BottomUnitView reportUnitView = new BottomUnitView(OnSiteConfirmActivity.this);
        reportUnitView.setContent("上报");
        reportUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(reportUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportEvent();
            }
        });
    }

    private void reportEvent() {

        // 1.上报事件

        List<FeedItem> feedbackItems = formBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

        eventInfoPostParam.DataParam.caseInfo = caseItemEntity.mapToCaseInfo();
        eventInfoPostParam.EventName = "第三方施工上报";
        eventInfoPostParam.TableName = "第三方施工事件表";
        eventInfoPostParam.BizCode = "CC";
        eventInfoPostParam.DataParam.caseInfo.EventName=eventInfoPostParam.EventName;

        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostEventData";

        if (feedbackItems == null) {
            return;
        }

        eventInfoPostParam.DataParam.flowNodeMeta.Values.clear();

        for (FeedItem item : feedbackItems) {
            FlowNodeMeta.TableValue tv=eventInfoPostParam.DataParam.flowNodeMeta.new TableValue(item.Name,item.Value);
            eventInfoPostParam.DataParam.flowNodeMeta.Values.add(tv);
        }

        // 将信息转换为JSON字符串
        String reportData = new Gson().toJson(eventInfoPostParam, new TypeToken<EventInfoPostParam>() {
        }.getType());

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                reportData,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                caseItemEntity.ActiveName,
                formBeanFragment.getAbsolutePaths(),
                formBeanFragment.getRelativePaths());

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                if (result.ResultCode > 0) {
                    Toast.makeText(OnSiteConfirmActivity.this, "上报成功!", Toast.LENGTH_SHORT).show();
                    // 上报成功则继续往下进行关单操作

                    caseFinish(2);
                } else {
                    Toast.makeText(OnSiteConfirmActivity.this, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }).mmtExecute(entity);

    }

    /**
     * 关单
     *
     * @param callSrc 调用源：1.直接关单 2.上报并关单
     */
    private void caseFinish(final int callSrc) {

        CaseInfo caseInfo = caseItemEntity.mapToCaseInfo();

        if (callSrc == 1) {
            caseInfo.Opinion = "关闭工单";
        } else if (callSrc == 2) {
            caseInfo.Opinion = "第三方施工上报并关单";
        }

        new MmtBaseTask<String, Void, String>(this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {
            @Override
            public void doAfter(String result) {

                if (BaseClassUtil.isNullOrEmptyString(result) || result.equals("\"\"")) {

                    String successMsg = "关单成功!";
                    if (callSrc == 1) {
                        successMsg = "关闭工单成功!";
                    } else if (callSrc == 2) {
                        successMsg = "上报并关单成功";
                    }
                    Toast.makeText(OnSiteConfirmActivity.this, successMsg, Toast.LENGTH_SHORT).show();

                    success();
                } else {
                    Toast.makeText(OnSiteConfirmActivity.this, "关闭工单失败！", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected String doInBackground(String... params) {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/WorkFlow/CaseFinish";
                try {

                    return NetUtil.executeHttpPost(url, params[0],
                            "Content-Type", "application/json; charset=utf-8");

                } catch (Exception e) {
                    e.printStackTrace();
                    return "关闭工单失败: " + e.getMessage();
                }
            }
        }.mmtExecute(new Gson().toJson(caseInfo));
    }

    private void success() {

        //成功后自己打开自己，达到重置界面的目的
        Intent intent = new Intent(OnSiteConfirmActivity.this, MyCaseListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);

        this.finish();
    }

}