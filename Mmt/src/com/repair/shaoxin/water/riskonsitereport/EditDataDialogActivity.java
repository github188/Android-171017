package com.repair.shaoxin.water.riskonsitereport;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.entity.CaseInfo;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.EventInfoPostParam;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.BaseDialogActivity;

import java.util.List;
import java.util.UUID;

public class EditDataDialogActivity extends BaseDialogActivity {

    private EventInfoPostParam mEventInfoPostParam = new EventInfoPostParam();
    private FlowInfoPostParam mFlowInfoPostParam = mEventInfoPostParam.DataParam;

    private FlowNodeMeta flowNodeMeta = new FlowNodeMeta();
    private String tableName;
    private CaseItem caseItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.tableName = getIntent().getStringExtra("TableName");
        this.caseItem = getIntent().getParcelableExtra("ListItemEntity");

        FlowNodeMeta.TableGroup tableGroup = flowNodeMeta.new TableGroup();
        tableGroup.Schema.add(flowNodeMeta.new FieldSchema("发现方式", "值选择器", tableName));
        tableGroup.Schema.add(flowNodeMeta.new FieldSchema("事件类型", "值选择器", tableName));
        flowNodeMeta.Groups.add(tableGroup);
    }

    @Override
    protected void handleOkEvent(String tag, List<FeedItem> feedItemList) {

        String findMethod = "", eventType = "";

        for (FeedItem feedItem : feedItemList) {

            switch (feedItem.Name) {
                case "发现方式":
                    findMethod = feedItem.Value;
                    break;
                case "事件类型":
                    eventType = feedItem.Value;
                    break;
            }
        }

        flowNodeMeta.Values.add(flowNodeMeta.new TableValue("发现方式", findMethod));
        flowNodeMeta.Values.add(flowNodeMeta.new TableValue("事件类型", eventType));

        reportModification();
    }

    private void reportModification() {

        CaseInfo caseInfo = caseItem.mapToCaseInfo();
        mFlowInfoPostParam.caseInfo = caseInfo;
        mFlowInfoPostParam.flowNodeMeta = flowNodeMeta;

        mEventInfoPostParam.BizCode = caseInfo.BizCode;
        mEventInfoPostParam.EventName = caseInfo.EventName;
        mEventInfoPostParam.TableName = caseInfo.EventMainTable;

        // 创建服务路径、将信息转换为JSON字符串
        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EditEventData?eventCode=" + caseInfo.EventCode;

        // 将对信息转换为JSON字符串
        String reportData = new Gson().toJson(mEventInfoPostParam, new TypeToken<EventInfoPostParam>() {
        }.getType());

        // 将所有信息封装成后台上传的数据模型
        ReportInBackEntity entity = new ReportInBackEntity(
                reportData,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                url,
                UUID.randomUUID().toString(),
                tableName,
                "",
                "");

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {

                        Toast.makeText(EditDataDialogActivity.this, "编辑成功", Toast.LENGTH_SHORT).show();

                        Intent intentDetail = new Intent(EditDataDialogActivity.this, RiskOnSiteReportDetailActivity.class);
                        intentDetail.putExtra("CaseItemCaseNo", caseItem.CaseNo);
                        intentDetail.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        intentDetail.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intentDetail);

                        finish();

                    } else {
                        Toast.makeText(EditDataDialogActivity.this, "编辑失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }
}
