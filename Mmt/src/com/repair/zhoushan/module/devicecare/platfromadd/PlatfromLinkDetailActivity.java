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
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.entity.TableMetaData;
import com.repair.zhoushan.module.devicecare.ScheduleTaskSaveInfo;
import com.repair.zhoushan.module.tablecommonhand.FlowTableInfoActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by lyunfan on 17/6/12.
 */

public class PlatfromLinkDetailActivity extends FlowTableInfoActivity {
    protected String bizName = "";

    protected String mode = "view";
    private int ID = -1; //记录id
    private String deviceID = ""; //台账id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();

        bizName = intent.getStringExtra("bizName");
        mode = intent.getStringExtra("mode");
        deviceID = intent.getStringExtra("deviceID");

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


            BottomUnitView manageUnitViewdel = new BottomUnitView(this);
            manageUnitViewdel.setContent("删除");
            manageUnitViewdel.setImageResource(R.drawable.handoverform_report);
            addBottomUnitView(manageUnitViewdel, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    delLink();

                }
            });


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

    private void delLink() {

        OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("确定删除？");
        okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
            @Override
            public void onRightButtonClick(View view) {

                new MmtBaseTask<Void, Void, String>(PlatfromLinkDetailActivity.this) {
                    @Override
                    protected String doInBackground(Void... params) {
                        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/"
                                + MyApplication.getInstance().getUserId() + "/DeleteAccountTableInfo";
                        url += "?bizName=" + bizName;
                        url += "&id=" + ID;
                        return NetUtil.executeHttpGet(url);
                    }

                    @Override
                    protected void onSuccess(String s) {
                        super.onSuccess(s);

                        ResultWithoutData resultWithoutData = Utils.resultjson2ResultWithoutDataToast(PlatfromLinkDetailActivity.this, s, "服务异常");
                        if (resultWithoutData == null) {
                            return;
                        }
                        backByReorder(true);
                    }
                }.mmtExecute();
            }
        });
        okCancelDialogFragment.show(getSupportFragmentManager(), "");
    }

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

                        Toast.makeText(PlatfromLinkDetailActivity.this, "保存成功", Toast.LENGTH_SHORT).show();

                        backByReorder(true);

                    } else {
                        Toast.makeText(PlatfromLinkDetailActivity.this, "保存失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }

    protected void report() {
        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/"
                + MyApplication.getInstance().getUserId() + "/AddAccountInfo";

        ReportInBackEntity entity = getReportInBackEntity(url, true);

        if (entity == null) {
            return;
        }

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {

            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {

                        Toast.makeText(PlatfromLinkDetailActivity.this, "保存成功", Toast.LENGTH_SHORT).show();

                        backByReorder(true);

                    } else {
                        Toast.makeText(PlatfromLinkDetailActivity.this, "保存失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);


    }

    protected ReportInBackEntity getReportInBackEntity(String url) {
        return getReportInBackEntity(url, false);
    }

    protected ReportInBackEntity getReportInBackEntity(String url, boolean isReport) {

        if (TextUtils.isEmpty(deviceID)) {
            showErrorMsg("台账不存在");
            return null;
        }
        if(!isReport&&ID<=0){
            showErrorMsg("记录不存在");
            return null;
        }

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
        mScheduleTaskSaveInfo.scheduleTask.Area = "";
        mScheduleTaskSaveInfo.scheduleTask.BizAccountTable = BizAccountTable;
        mScheduleTaskSaveInfo.scheduleTask.BizName = bizName;
        mScheduleTaskSaveInfo.scheduleTask.UserID = String.valueOf(MyApplication.getInstance().getUserId());
//        //编辑关键信息
       // mScheduleTaskSaveInfo.scheduleTask.GisCode = GisCode;
        if(!isReport) {
            mScheduleTaskSaveInfo.scheduleTask.ID = ID;
        }

        List<FlowNodeMeta.TableValue> values = new ArrayList<>();
        for (FeedItem item : feedbackItems) {
            FlowNodeMeta.TableValue tableValue = mScheduleTaskSaveInfo.flowNodeMeta.new TableValue(item.Name, item.Value);
            values.add(tableValue);
        }

        mScheduleTaskSaveInfo.flowNodeMeta.Values.clear();
        mScheduleTaskSaveInfo.flowNodeMeta.Values.addAll(values);

        if (isReport) {
            //PID
            String pidName = "PID";
            String pidVal = deviceID;
            //PID schem
            FlowNodeMeta.TableGroup PIDGroup = mScheduleTaskSaveInfo.flowNodeMeta.new TableGroup();
            PIDGroup.GroupName = "内部字段";
            PIDGroup.Visible = 1;

            FlowNodeMeta.FieldSchema pidSchema = mScheduleTaskSaveInfo.flowNodeMeta.new FieldSchema();
            pidSchema.Alias = pidName;
            pidSchema.FieldName = pidName;
            pidSchema.Shape = "文本框";
            pidSchema.Type = "文本";
            pidSchema.Visible = 1;

            PIDGroup.Schema.add(pidSchema);

            mScheduleTaskSaveInfo.flowNodeMeta.Groups.add(PIDGroup);
            //PID value

            FlowNodeMeta.TableValue PIDValue = mScheduleTaskSaveInfo.flowNodeMeta.new TableValue(pidName, pidVal);
            mScheduleTaskSaveInfo.flowNodeMeta.Values.add(PIDValue);
        }
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
}
