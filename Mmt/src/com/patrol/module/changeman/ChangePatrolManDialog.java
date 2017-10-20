package com.patrol.module.changeman;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.patrol.common.MyPlanUtil;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.module.tablecommonhand.TableOneRecordDialog;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by lyunfan on 16/10/25.
 */
public class ChangePatrolManDialog extends TableOneRecordDialog {

    private String taskID;
    private String taskName;
    private String taskState;
    private String preTaskMan = MyApplication.getInstance().getUserBean().TrueName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        taskID = intent.getStringExtra("taskID");
        if (TextUtils.isEmpty(taskID)) {
            MyApplication.getInstance().showMessageWithHandle("taskID不能为空");
            return;
        }
        taskName = intent.getStringExtra("taskName");
        taskState = intent.getStringExtra("taskState");

        tableMode.tableName = "CIV_PATROL_CHANGE_MAN";
        //不存在就插入，存在就更新
        tableMode.viewMode = TabltViewMode.REPORT.getTableViewMode();

        mTitle = "申请换人";
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onViewCreated() {
        View taskIDView = mFlowBeanFragment.findViewByName("TaskID");
        if (taskIDView == null) {
            MyApplication.getInstance().showMessageWithHandle("换人纪录表必须配置任务ID字段");
            return;
        }
        if (taskIDView instanceof ImageEditView) {
            ((ImageEditView) taskIDView).setValue(taskID);
        }
        if (taskIDView instanceof ImageTextView) {
            ((ImageTextView) taskIDView).setValue(taskID);
        }
        taskIDView.setVisibility(View.GONE);

        View taskNameView = mFlowBeanFragment.findViewByName("TaskName");
        if (taskNameView instanceof ImageEditView) {
            ImageEditView taskNameEditView = ((ImageEditView) taskNameView);
            taskNameEditView.setEditable(false);
            taskNameEditView.setValue(taskName);
        }
        if (taskNameView instanceof ImageTextView) {
            ((ImageTextView) taskNameView).setValue(taskName);
        }

        View taskStateView = mFlowBeanFragment.findViewByName("TaskState");
        if (taskStateView instanceof ImageEditView) {
            ImageEditView taskStateEditView = ((ImageEditView) taskStateView);
            taskStateEditView.setEditable(false);
            taskStateEditView.setValue(taskState);
        }
        if (taskStateView instanceof ImageTextView) {
            ((ImageTextView) taskStateView).setValue(taskState);
        }

        View oldTaskManView = mFlowBeanFragment.findViewByName("OldMan");
        if (oldTaskManView instanceof ImageEditView) {
            ImageEditView oldTaskManEditView = ((ImageEditView) oldTaskManView);
            oldTaskManEditView.setEditable(false);
            oldTaskManEditView.setValue(preTaskMan);
        }
        if (oldTaskManView instanceof ImageTextView) {
            ((ImageTextView) oldTaskManView).setValue(preTaskMan);
        }
    }

    String newMan = "";
    String oldMan = "";

    @Override
    protected void reportData(List<FeedItem> feedItemList) {

        for (FeedItem fi : feedItemList) {
            if (fi.Name.equals("OldMan")) {
                oldMan = fi.Value;
            }
            if (fi.Name.equals("NewMan")) {
                newMan = fi.Value;
            }
        }

        if (TextUtils.isEmpty(oldMan)) {
            Toast.makeText(ChangePatrolManDialog.this, "原执行人不能为空", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(newMan)) {
            Toast.makeText(ChangePatrolManDialog.this, "新执行人不能为空", Toast.LENGTH_LONG).show();
            return;
        }
        if (oldMan.equals(newMan)) {
            Toast.makeText(ChangePatrolManDialog.this, "新执行人原执行人不能相同", Toast.LENGTH_LONG).show();
            return;
        }

        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
        sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/");
        sb.append("SaveTableDataInfo?tableName=" + tableMode.tableName);

        final ReportInBackEntity entity = getReportBackEntity(sb.toString(), feedItemList);

        if (entity == null) {
            return;
        }

        new MmtBaseTask<Void, Void, String>(ChangePatrolManDialog.this) {
            @Override
            protected String doInBackground(Void... voids) {
                String url = MyPlanUtil.getStandardURL() + "/ChangePartolMan?taskID=" + taskID + "&oldMan=" + oldMan + "&newMan=" + newMan;
                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                ResultWithoutData uret = Utils.resultWithoutDataJson2ResultDataToast(ChangePatrolManDialog.this, s, "换人失败", "换人成功");
                if (uret == null) {
                    return;
                }

                new EventReportTask(ChangePatrolManDialog.this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
                    @Override
                    public void doAfter(ResultData<Integer> result) {
                        try {
                            if (result.ResultCode != 200) {
                                Toast.makeText(ChangePatrolManDialog.this, "添加失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                                return;
                            }
                            Toast.makeText(ChangePatrolManDialog.this, "记录成功", Toast.LENGTH_LONG).show();

                            onSuccessV2();
                            EventBus.getDefault().post(new NotifyRefreshPaln(Integer.parseInt(taskID)));

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }).mmtExecute(entity);


            }
        }.mmtExecute();


    }
}
