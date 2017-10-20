package com.repair.zhoushan.module.casemanage.casedetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FeedbackInfo;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.devicecare.MaintenanceFeedBack;
import com.repair.zhoushan.module.tablecommonhand.TableOneRecordActivity;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by liuyunfan on 2016/7/22.
 */
public class FeebReportActivity extends TableOneRecordActivity {

    protected String caseno = "";
    protected String bizName = "";

    protected String eventCode;
    protected String eventTableName;

    FeedbackInfo feedbackInfo;

    @Override
    protected void reportData() {

        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
        sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SaveTableDataAndSyncInfo")
                .append("?tableName=").append(tableMode.tableName)
                .append("&eventCode=").append(eventCode)
                .append("&eventTableName=").append(eventTableName);

        ReportInBackEntity entity = getReportBackEntity(sb.toString());

        if (entity == null) {
            return;
        }

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {

                        Toast.makeText(FeebReportActivity.this, "添加成功!", Toast.LENGTH_SHORT).show();

                        backByReorder();

                        EventBus.getDefault().post(new FeedBackListFragment.FBRefreshFragment());
                        setResult(Activity.RESULT_OK);

                    } else {
                        Toast.makeText(FeebReportActivity.this, "添加失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);

    }

    @Override
    protected void deleteData() {
        OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("确定删除");
        okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
            @Override
            public void onRightButtonClick(View view) {
                final StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/");
                sb.append("DeleteTableData?tableName=" + tableMode.tableName + "&id=" + tableMode.ID);

                new MmtBaseTask<Void, Void, String>(FeebReportActivity.this) {
                    @Override
                    protected String doInBackground(Void... params) {

                        return NetUtil.executeHttpGet(sb.toString());

                    }

                    @Override
                    protected void onSuccess(String s) {
                        super.onSuccess(s);
                        ResultData<Integer> tableResult = Utils.json2ResultDataActivity(Integer.class,
                                FeebReportActivity.this, s, "删除失败", false);
                        if (tableResult == null) return;
                        MyApplication.getInstance().showMessageWithHandle("删除成功");

                        backByReorder();

                        EventBus.getDefault().post(new FeedBackListFragment.FBRefreshFragment());
                    }
                }.mmtExecute();
            }
        });

        okCancelDialogFragment.show(this.getSupportFragmentManager(), "");
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        if (!intent.hasExtra("title")) {
            getIntent().putExtra("title", bizName);
        }
        if (feedbackInfo != null) {
            maintenanceFeedBacks = new ArrayList<>();
            maintenanceFeedBacks.add(feedbackInfo.feedbackInfo2MaintenanceFeedBack());
        }

        if (maintenanceFeedBacks != null) {
            super.initView();
        } else {
            getBizTable();
        }
    }


    private void getBizTable() {

        new MmtBaseTask<Void, Void, String>(FeebReportActivity.this) {
            @Override
            protected String doInBackground(Void... params) {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetMaintenanceFBConfigList?BizName=" + bizName;

                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String s) {
                try {
                    super.onSuccess(s);

                    ResultData<MaintenanceFeedBack> filterResult = Utils.json2ResultDataActivity(MaintenanceFeedBack.class,
                            FeebReportActivity.this, s, "获取过滤条件失败", false);

                    if (filterResult == null) {
                        return;
                    }

                    if (filterResult.DataList.size() == 0) {
                        return;
                    }
                    maintenanceFeedBacks = filterResult.DataList;
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    FeebReportActivity.super.initView();
                }


            }
        }.mmtExecute();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            Intent intent = getIntent();

            this.bizName = intent.getStringExtra("bizName");
            this.eventCode = intent.getStringExtra("eventCode");
            this.eventTableName = intent.getStringExtra("eventTableName");

            String feedbackInfoStr = intent.getStringExtra("feedbackInfo");
            if (!TextUtils.isEmpty(feedbackInfoStr)) {
                feedbackInfo = new Gson().fromJson(feedbackInfoStr, FeedbackInfo.class);
            }

            if (TextUtils.isEmpty(bizName)) {
                MyApplication.getInstance().showMessageWithHandle("业务名不能为空");
                return;
            }

            if (flowBeanFragment != null && intent.getIntExtra("viewMode", -1) == TabltViewMode.REPORT.getTableViewMode()) {

                caseno = intent.getStringExtra("caseno");

                if (TextUtils.isEmpty(caseno)) {
                    MyApplication.getInstance().showMessageWithHandle("caseno未找到");
                    return;
                }

                cacheKey = caseno;

                flowBeanFragment.setBeanFragmentOnCreate(new FlowBeanFragment.BeanFragmentOnCreate() {
                    @Override
                    public void onCreated() {
                        doOnViewCreated();
                    }
                });
            }
        } catch (Exception ex) {

            MyApplication.getInstance().showMessageWithHandle(ex.getMessage());

        } finally {
            super.onCreate(savedInstanceState);

            setTitleAndClear(bizName);
        }

    }

    protected void doOnViewCreated() {

        View view = flowBeanFragment.findViewByName("工单编号");

        if (view instanceof ImageEditView) {
            ImageEditView iv = (ImageEditView) view;

            iv.setValue(caseno);
            return;
        }

        if (view instanceof ImageTextView) {
            ImageTextView iv = (ImageTextView) view;

            iv.setValue(caseno);
            return;
        }

        showErrorMsg("请将工单编号附加到反馈表");
    }


}
