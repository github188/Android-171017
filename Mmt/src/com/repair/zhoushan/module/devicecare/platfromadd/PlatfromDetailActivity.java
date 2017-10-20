package com.repair.zhoushan.module.devicecare.platfromadd;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.entity.TableMetaData;
import com.repair.zhoushan.module.devicecare.ScheduleTaskSaveInfo;
import com.repair.zhoushan.module.tablecommonhand.FlowTableInfoActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by liuyunfan on 2016/7/19.
 */
public class PlatfromDetailActivity extends FlowTableInfoActivity {

    protected String bizName = "";

    protected String mode = "view";

    private String area = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();

        bizName = intent.getStringExtra("bizName");
        mode = intent.getStringExtra("mode");



        area = intent.getStringExtra("Area");

        if ("view".equals(mode)) {
            StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
            sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                    .append(MyApplication.getInstance().getUserId() + "")
                    .append("/AccountTableInfo")
                    .append("?bizName=").append(bizName);
            ID = getIntent().getIntExtra("ID", -1);
            if (ID < 1) {
                showErrorMsg("ID异常");
                return;
            }
            sb.append("&id=" + ID);

            GisCode = getIntent().getStringExtra("GisCode");

            getViewDataUrl = sb.toString();
        } else {

            StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
            sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                    .append(MyApplication.getInstance().getUserId() + "")
                    .append("/AddAccountTableInfo")
                    .append("?bizName=").append(bizName);
            getViewDataUrl = sb.toString();
        }


        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setBottonBtn() {

        isRead = false;

        if ("view".equals(mode)) {

            BottomUnitView manageUnitViewEdit = new BottomUnitView(this);
            manageUnitViewEdit.setContent("保存");
            manageUnitViewEdit.setImageResource(R.drawable.handoverform_report);
            addBottomUnitView(manageUnitViewEdit, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    edit();

                }
            });


            BottomUnitView manageUnitViewTask = new BottomUnitView(this);
            manageUnitViewTask.setContent("临时任务");
            manageUnitViewTask.setImageResource(R.drawable.handoverform_report);
            addBottomUnitView(manageUnitViewTask, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    addTask();

                }
            });

            if ("工商户安检".equals(bizName)) {
                BottomUnitView manageUnitView_bj_add = new BottomUnitView(this);
                manageUnitView_bj_add.setContent("工商户表具");
                manageUnitView_bj_add.setImageResource(R.drawable.handoverform_report);
                addBottomUnitView(manageUnitView_bj_add, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(PlatfromDetailActivity.this, PlatfromLinkListActivity.class);
                        intent.putExtra("deviceID", String.valueOf(ID));
                        intent.putExtra("bizName", "工商户表具");
                        PlatfromDetailActivity.this.startActivity(intent);
                    }
                });
            }
        } else {
            BottomUnitView manageUnitView = new BottomUnitView(this);
            manageUnitView.setContent("上报");
            manageUnitView.setImageResource(R.drawable.handoverform_report);
            addBottomUnitView(manageUnitView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    report();

                }
            });
        }

    }

    private void addTask() {

        Intent intent = new Intent(this, AddTaskActivity.class);
        intent.putExtra("GisCode", GisCode);
        intent.putExtra("bizType", bizName);
        intent.putExtra("ID", ID);

        startActivity(intent);

    }

    private String GisCode = "";
    private int ID = -1;

    protected void edit() {


        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/"
                + MyApplication.getInstance().getUserId() + "/AccountInfoSave";

        ReportInBackEntity entity = getReportInBackEntity(url);

        if (entity == null) {
            return;
        }

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {

            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {

                        Toast.makeText(PlatfromDetailActivity.this, "保存成功", Toast.LENGTH_SHORT).show();

                        backByReorder(true);

                    } else {
                        Toast.makeText(PlatfromDetailActivity.this, "保存失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }

    protected ReportInBackEntity getReportInBackEntity(String url) {

        if (flowBeanFragment == null) {
            showErrorMsg("上报页面不存在");
            return null;
        }

        List<FeedItem> feedbackItems = flowBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

        if (feedbackItems == null) {
            showErrorMsg("上报项不存在");
            return null;
        }

        if (mFlowTableInfos == null || mFlowTableInfos.size() == 0) {
            showErrorMsg("上报项不存在");
            return null;
        }


        List<TableMetaData> tableMetaDatas = mFlowTableInfos.get(0).TableMetaDatas;

        if (tableMetaDatas == null || tableMetaDatas.size() == 0) {
            showErrorMsg("未获取到上报的表架构");
            return null;
        }

        String BizAccountTable = tableMetaDatas.get(0).TableName;

        if (TextUtils.isEmpty(BizAccountTable)) {
            showErrorMsg("未获取到配置的表名，无法提交");
            return null;
        }

        ScheduleTaskSaveInfo mScheduleTaskSaveInfo = new ScheduleTaskSaveInfo();

        mScheduleTaskSaveInfo.flowNodeMeta = tableMetaDatas.get(0).FlowNodeMeta;

        //上报关键信息
        mScheduleTaskSaveInfo.scheduleTask.Area = area;
        mScheduleTaskSaveInfo.scheduleTask.BizAccountTable = BizAccountTable;
        mScheduleTaskSaveInfo.scheduleTask.BizName = bizName;
        mScheduleTaskSaveInfo.scheduleTask.UserID = String.valueOf(MyApplication.getInstance().getUserId());
        //编辑关键信息
        mScheduleTaskSaveInfo.scheduleTask.GisCode = GisCode;
        mScheduleTaskSaveInfo.scheduleTask.ID = ID;

//        // 把Feedback的值映射到Value上
//        ArrayList<FlowNodeMeta.TableValue> values = mScheduleTaskSaveInfo.flowNodeMeta.Values;
//        for (FlowNodeMeta.TableValue value : values) {
//            for (FeedItem item : feedbackItems) {
//                if (value.FieldName.equals(item.Name)) {
//                    value.FieldValue = item.Value;
//                    break;
//                }
//            }
//        }
        List<FlowNodeMeta.TableValue> values = new ArrayList<>();
        for (FeedItem item : feedbackItems) {
            FlowNodeMeta.TableValue tableValue = mScheduleTaskSaveInfo.flowNodeMeta.new TableValue(item.Name, item.Value);
            values.add(tableValue);
        }

        mScheduleTaskSaveInfo.flowNodeMeta.Values.clear();
        mScheduleTaskSaveInfo.flowNodeMeta.Values.addAll(values);

        // 将信息转换为 JSON字符串
        String reportData = new Gson().toJson(mScheduleTaskSaveInfo, new TypeToken<ScheduleTaskSaveInfo>() {
        }.getType());

        // 将所有信息封装成后台上传的数据模型
        return new ReportInBackEntity(
                reportData,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                url,
                UUID.randomUUID().toString(),
                mScheduleTaskSaveInfo.scheduleTask.BizName,
                flowBeanFragment.getAbsolutePaths(),
                flowBeanFragment.getRelativePaths());
    }

    protected void report() {
        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/"
                + MyApplication.getInstance().getUserId() + "/AddAccountInfo";

        ReportInBackEntity entity = getReportInBackEntity(url);

        if (entity == null) {
            return;
        }

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {

            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {

                        Toast.makeText(PlatfromDetailActivity.this, "保存成功", Toast.LENGTH_SHORT).show();

                        backByReorder(true);

                    } else {
                        Toast.makeText(PlatfromDetailActivity.this, "保存失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);

    }
}
