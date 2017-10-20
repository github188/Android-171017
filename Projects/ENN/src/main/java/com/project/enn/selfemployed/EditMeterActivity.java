package com.project.enn.selfemployed;

import android.content.Intent;
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
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 修改表具信息
 */
public class EditMeterActivity extends BaseActivity {

    private CommeicalChangeInfo commercialChangeInfo;
    private FlowNodeMeta flowNodeMeta;

    private FlowBeanFragment flowBeanFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseTextView().setText("修改表具信息");

        Intent outerIntent = getIntent();
        commercialChangeInfo = outerIntent.getParcelableExtra("CommercialChangeInfo");

        String flowNodeMetaStr = outerIntent.getStringExtra("FlowNodeMetaStr");
        flowNodeMeta = new Gson().fromJson(flowNodeMetaStr, FlowNodeMeta.class);

        if (commercialChangeInfo == null || flowNodeMeta == null) {
            showErrorMsg("获取参数错误");
            return;
        }

        initView();
        initBottomView();
    }

    private void initView() {

        GDFormBean gdFormBean = flowNodeMeta.mapToGDFormBean();

        gdFormBean.setEditable(true, Arrays.asList("事件编号"));

        flowBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", gdFormBean);
        flowBeanFragment.setArguments(args);

        addFragment(flowBeanFragment);
    }

    private void initBottomView() {

        BottomUnitView updateUnitView = new BottomUnitView(EditMeterActivity.this);
        updateUnitView.setContent("保存");
        updateUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(updateUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                doUpdate();
            }
        });
    }

    private void doUpdate() {

        if (flowBeanFragment == null) return;

        final List<FeedItem> feedbackItems = flowBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);
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
        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EditTableData"
                + "?tableName=" + Uri.encode("工商户置换表具记录表") + "&id=" + commercialChangeInfo.ID;

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                reportData,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                url,
                UUID.randomUUID().toString(),
                commercialChangeInfo.EventCode,
                flowBeanFragment.getAbsolutePaths(),
                flowBeanFragment.getRelativePaths());

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {

                        Toast.makeText(EditMeterActivity.this, "修改成功!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(EditMeterActivity.this, MeterListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("Caller", EditMeterActivity.class.getName());
                        startActivity(intent);

                    } else {
                        Toast.makeText(EditMeterActivity.this, "修改失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);

    }

}
