package com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.entity.CaseInfo;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.BaseDialogActivity;

import java.util.List;
import java.util.UUID;

public class OSCFeedbackDialogActivity extends BaseDialogActivity {
    CaseInfo caseInfo;

    @Override
    protected void handleOkEvent(String tag, List<FeedItem> feedItemList) {

        try {

            Intent intent = getIntent();
            if (intent == null) {
                return;
            }

            CaseItem caseItemEntity = intent.getParcelableExtra("CaseItem");
            FlowNodeMeta flowNodeMeta = (FlowNodeMeta) intent.getSerializableExtra("FlowNodeMeta");

            caseInfo = caseItemEntity.mapToCaseInfo();

            for (FlowNodeMeta.TableValue valItem : flowNodeMeta.Values) {
                String fieldName = valItem.FieldName;
                for (FeedItem feedItem : feedItemList) {
                    if (fieldName.equals(feedItem.Name)) {
                        valItem.FieldValue = feedItem.Value;
                        break;
                    }
                }
            }

            FlowInfoPostParam flowInfoPostParam = new FlowInfoPostParam();

            flowInfoPostParam.flowNodeMeta = flowNodeMeta;
            flowInfoPostParam.caseInfo = caseInfo;

            // 创建服务路径
            final String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/FeedbackEventData";


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
                    caseItemEntity.ActiveName,
                    mFlowBeanFragment.getAbsolutePaths(),
                    mFlowBeanFragment.getRelativePaths());

            new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
                @Override
                public void doAfter(ResultData<Integer> result) {
                    try {
                        if (result.ResultCode == 200) {
                            Toast.makeText(OSCFeedbackDialogActivity.this, "反馈成功!", Toast.LENGTH_SHORT).show();

                            caseFinish();

                        } else {
                            Toast.makeText(OSCFeedbackDialogActivity.this, "反馈失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).mmtExecute(entity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void caseFinish() {

        new MmtBaseTask<String, Void, String>(this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {
            @Override
            public void doAfter(String result) {

                if (BaseClassUtil.isNullOrEmptyString(result) || result.equals("\"\"")) {

                    Toast.makeText(OSCFeedbackDialogActivity.this, "关单成功", Toast.LENGTH_SHORT).show();

                    setResult(Activity.RESULT_OK);

                    AppManager.finishActivity(OSCFeedbackDialogActivity.this);
                } else {
                    Toast.makeText(OSCFeedbackDialogActivity.this, "关闭工单失败！", Toast.LENGTH_SHORT).show();
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

}
