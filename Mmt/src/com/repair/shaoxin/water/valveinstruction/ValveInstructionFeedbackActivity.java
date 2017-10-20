package com.repair.shaoxin.water.valveinstruction;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.BaseDialogActivity;

import java.util.List;
import java.util.UUID;

public class ValveInstructionFeedbackActivity extends BaseDialogActivity {

    private FlowNodeMeta mFeedbackFlowNodeMeta;
    private String tableName;
    private String tableRecordID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.tableName = getIntent().getStringExtra("TableName");
        this.tableRecordID = getIntent().getStringExtra("TableRecordID");
        String flowNodeMetaStr = getIntent().getStringExtra("FlowNodeMeta");
        if (!TextUtils.isEmpty(flowNodeMetaStr)) {
            this.mFeedbackFlowNodeMeta = new Gson().fromJson(flowNodeMetaStr, FlowNodeMeta.class);
        }
    }

    @Override
    protected void handleOkEvent(String tag, List<FeedItem> feedItemList) {
        doFeedback(feedItemList);
    }

    private void doFeedback(final List<FeedItem> feedbackItems) {

        if (mFeedbackFlowNodeMeta == null || feedbackItems == null) {
            return;
        }

        FlowNodeMeta.TableGroup tableGroup = mFeedbackFlowNodeMeta.Groups.get(0);
        mFeedbackFlowNodeMeta.Values.clear();
        for (FlowNodeMeta.FieldSchema fieldSchema : tableGroup.Schema) {
            for (FeedItem feedItem : feedbackItems) {
                if (feedItem.Name.equals(fieldSchema.FieldName)) {
                    mFeedbackFlowNodeMeta.Values.add(
                            mFeedbackFlowNodeMeta.new TableValue(feedItem.Name, feedItem.Value));
                    break;
                }
            }
        }

        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath())
                .append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/")
                .append("EditTableData?tableName=").append(tableName)
                .append("&id=").append(tableRecordID);
        String url = sb.toString();

        // 将对信息转换为JSON字符串
        String reportData = new Gson().toJson(mFeedbackFlowNodeMeta);

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                reportData,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                url,
                UUID.randomUUID().toString(),
                tableName,
                mFlowBeanFragment.getAbsolutePaths(),
                mFlowBeanFragment.getRelativePaths());

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {
                        Toast.makeText(ValveInstructionFeedbackActivity.this, "反馈成功", Toast.LENGTH_SHORT).show();
                        onSuccess();
                        // backByReorder(true);
                    } else {
                        Toast.makeText(ValveInstructionFeedbackActivity.this, "反馈失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);

    }

}
