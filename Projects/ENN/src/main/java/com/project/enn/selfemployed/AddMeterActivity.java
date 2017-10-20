package com.project.enn.selfemployed;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddMeterActivity extends BaseActivity {

    private CaseItem caseItem;
    private FlowNodeMeta flowNodeMeta;

    private FlowBeanFragment flowBeanFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseTextView().setText("添加表具信息");

        caseItem = getIntent().getParcelableExtra("ListItemEntity");

        initData();
    }

    private void initData() {

        new FetchMeterTableTask(AddMeterActivity.this) {
            @Override
            protected void onSuccess(Results<FlowNodeMeta> results) {
                ResultData<FlowNodeMeta> data = results.toResultData();

                if (data.ResultCode != 200) {
                    showErrorMsg(data.ResultMessage);
                } else {

                    flowNodeMeta = data.getSingleData();
                    for (FlowNodeMeta.TableValue tableValue : flowNodeMeta.Values) {
                        if ("事件编号".equals(tableValue.FieldName)) {
                            tableValue.FieldValue = caseItem.EventCode;
                            break;
                        }
                    }

                    initView();
                    initBottomView();
                }
            }
        }.mmtExecute();
    }

    private void initView() {

        GDFormBean gdFormBean = flowNodeMeta.mapToGDFormBean();

        flowBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", gdFormBean);
        flowBeanFragment.setArguments(args);

        addFragment(flowBeanFragment);
    }

    private void initBottomView() {

        BottomUnitView updateUnitView = new BottomUnitView(AddMeterActivity.this);
        updateUnitView.setContent("提交");
        updateUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(updateUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                doReport();
            }
        });
    }

    private void doReport() {

        if (flowBeanFragment == null) return;

        List<FeedItem> feedbackItems = flowBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);
        if (feedbackItems == null) return;

        // 把Feedback的值映射到Value上
        ArrayList<FlowNodeMeta.TableValue> values = flowNodeMeta.Values;
        for (FlowNodeMeta.TableValue value : values) {
            for (FeedItem item : feedbackItems) {
                if (value.FieldName.equals(item.Name)) {
                    value.FieldValue = item.Value;
                    break;
                }
            }
        }

        // 将对信息转换为JSON字符串
        String reportData = new Gson().toJson(flowNodeMeta);

        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SaveTableDataInfo?tableName=" + Uri.encode("工商户置换表具记录表");

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                reportData,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                this.caseItem.ActiveName,
                flowBeanFragment.getAbsolutePaths(),
                flowBeanFragment.getRelativePaths());

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {

                        Toast.makeText(AddMeterActivity.this, "添加成功!", Toast.LENGTH_SHORT).show();

                        setResult(Activity.RESULT_OK);
                        finish();

                    } else {
                        Toast.makeText(AddMeterActivity.this, "添加失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);

    }
}
