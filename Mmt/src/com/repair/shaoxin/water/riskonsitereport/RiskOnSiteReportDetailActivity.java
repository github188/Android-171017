package com.repair.shaoxin.water.riskonsitereport;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.R;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.entity.FeedbackInfo;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

import java.util.ArrayList;

/**
 * 绍兴隐患流程-现场反馈:事件字段编辑
 */
public class RiskOnSiteReportDetailActivity extends CaseDetailActivity {

    @Override
    protected void createBottomView() {

        final ArrayList<FlowNodeMeta.FieldSchema> schemaList = getSchemaListByNames("发现方式", "事件类型");

        BottomUnitView editUnitView = new BottomUnitView(RiskOnSiteReportDetailActivity.this);
        editUnitView.setContent("编辑");
        editUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(editUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {

                String findMethodConfig = "", eventTypeConfig = "";
                String tableName = "";
                for (FlowNodeMeta.FieldSchema schema : schemaList) {
                    if ("发现方式".equals(schema.FieldName)) {
                        tableName = schema.TableName;

                        if (!TextUtils.isEmpty(schema.ConfigInfo)) {
                            String items = schema.ConfigInfo;
                            int index = items.indexOf("主动");
                            if (index > 0 && (',' == items.charAt(index - 1))) {
                                findMethodConfig = "主动," + items.replaceFirst(",主动", "");
                            }
                        }

                    } else if ("事件类型".equals(schema.FieldName)) {

                        if (!TextUtils.isEmpty(schema.ConfigInfo)) {
                            String items = schema.ConfigInfo;

                            int index = items.indexOf("隐患点");
                            if (index > 0 && (',' == items.charAt(index - 1))) {
                                eventTypeConfig = "隐患点," + items.replaceFirst(",隐患点", "");
                            }
                        }
                    }
                }

                GDFormBean gdFormBean = GDFormBean.generateSimpleForm(
                        new String[]{"DisplayName", "发现方式", "Name", "发现方式", "Type", "值选择器", "ConfigInfo", findMethodConfig},
                        new String[]{"DisplayName", "事件类型", "Name", "事件类型", "Type", "值选择器", "ConfigInfo", eventTypeConfig});

                Intent intent = new Intent(RiskOnSiteReportDetailActivity.this, EditDataDialogActivity.class);
                intent.putExtra("Tag", "事件编辑");
                intent.putExtra("Title", "事件编辑");
                intent.putExtra("GDFormBean", gdFormBean);
                intent.putExtra("ListItemEntity", caseItemEntity);

                intent.putExtra("TableName", tableName);

                startActivity(intent);
            }
        });

        // 创建移交按钮
        BottomUnitView manageUnitView = new BottomUnitView(RiskOnSiteReportDetailActivity.this);
        manageUnitView.setContent("申请销单");
        manageUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(MaintenanceConstant.SERVER_SELECT_NEXT);
            }
        });

        // 创建多次反馈的按钮
        multFBAndAssistModule();

        // super.createBottomView();
    }

    @Override
    protected void feedbackReportActivity(String caseNo, FeedbackInfo feedbackInfo) {

        Intent intent = new Intent(RiskOnSiteReportDetailActivity.this, RiskOnSiteFBReportActivity.class);
        intent.putExtra("caseno", caseNo);
        intent.putExtra("bizName", feedbackInfo.FBBiz);
        intent.putExtra("tableName", feedbackInfo.FBTable);
        intent.putExtra("viewMode", TabltViewMode.REPORT.getTableViewMode());
        intent.putExtra("eventCode", caseItemEntity.EventCode);
        intent.putExtra("eventTableName", caseItemEntity.EventMainTable);

        // 将隐患区域位置传入到多次反馈界面
        intent.putExtra("RiskArea", getValueByFieldName("区域位置"));

        startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(RiskOnSiteReportDetailActivity.this);
    }
}
