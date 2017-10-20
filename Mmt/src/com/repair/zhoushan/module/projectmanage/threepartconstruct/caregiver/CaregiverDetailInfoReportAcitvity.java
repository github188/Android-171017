package com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by liuyunfan on 2015/12/10.
 */
public class CaregiverDetailInfoReportAcitvity extends BaseActivity {

    private CaseItem caseItem;
    private FlowNodeMeta FlowNodeMeta;
    private FlowBeanFragment formBeanFragment;
    public final FlowInfoPostParam flowInfoPostParam = new FlowInfoPostParam();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createView();
        createBottomView();
    }

    private void createView() {
        caseItem = getIntent().getParcelableExtra("ListItemEntity");
        if (caseItem == null) {
            Toast.makeText(CaregiverDetailInfoReportAcitvity.this, "未获取到流程信息!", Toast.LENGTH_SHORT).show();
            return;
        }

        String FlowInfoItemStr = getIntent().getStringExtra("FlowInfoItem");
        if (BaseClassUtil.isNullOrEmptyString(FlowInfoItemStr)) {
            Toast.makeText(CaregiverDetailInfoReportAcitvity.this, "未获取到界面结构!", Toast.LENGTH_SHORT).show();
            return;
        }

        FlowNodeMeta = new Gson().fromJson(
                FlowInfoItemStr,
                new TypeToken<FlowNodeMeta>() {
                }.getType());

        if (FlowNodeMeta == null) {
            Toast.makeText(CaregiverDetailInfoReportAcitvity.this, "界面结构转换失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if (FlowNodeMeta.Groups.size() <= 0) {
            Toast.makeText(CaregiverDetailInfoReportAcitvity.this, "未获取到信息", Toast.LENGTH_SHORT).show();
            return;
        }
        //确认信息中 管控级别和施工点与管道距离不可编辑
        //FlowNodeMeta中最多只有1个group
        if (FlowNodeMeta.Groups.get(0).GroupName.equals("确认信息")) {
            for (FlowNodeMeta.FieldSchema filed : FlowNodeMeta.Groups.get(0).Schema) {
                if (filed.FieldName.equals("管控级别") || filed.Alias.equals("管控级别")) {
                    filed.ReadOnly = 1;
                }
                if (filed.FieldName.equals("施工点与管道距离") || filed.Alias.equals("施工点与管道距离")) {
                    filed.ReadOnly = 1;
                }
            }
//
        }
//        if (flowInfoItem == null || flowInfoItem.FlowNodeMeta == null) {
//            for (FlowInfoItem flowInfoItem : flowInfoItemList) {
//                if (flowInfoItem.FlowInfoConfig.ViewState.equalsIgnoreCase("edit")) {
//                    this.flowInfoItem = flowInfoItem;
//                }
//            }
//        }

        // flowInfoPostParam.flowNodeMeta = flowInfoItem.FlowNodeMeta;

        // this.setCustomView(getTopView());
        // String flowInfoItemStr=getIntent().getStringExtra("")

        // 将Fragment显示在界面上
        formBeanFragment = new FlowBeanFragment();

        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", FlowNodeMeta.mapToGDFormBean());
        formBeanFragment.setArguments(args);

        formBeanFragment.setCls(EveryDayCheck.class);
        formBeanFragment.setFragmentFileRelativePath(caseItem.CaseNo);
        formBeanFragment.setAddEnable(true);

        addFragment(formBeanFragment);
    }

    private void createBottomView() {

        BottomUnitView backUnitView = new BottomUnitView(CaregiverDetailInfoReportAcitvity.this);
        backUnitView.setContent("保存");
        backUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(backUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  handler.sendEmptyMessage(SAVE_CURRENT_NODE);
                saveEvent();
            }
        });
    }


    private void saveEvent() {
        if (formBeanFragment == null) {
            return;
        }
        flowInfoPostParam.flowNodeMeta = FlowNodeMeta;
        flowInfoPostParam.caseInfo = caseItem.mapToCaseInfo();

        // 创建服务路径
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/FeedbackEventData";

        List<FeedItem> feedbackItems
                = formBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

        if (feedbackItems == null) {
            return;
        }
//        items.addAll(getDefaultFeedItem(items, itemEntity));

        // 把Feedback的值映射到Value上
        ArrayList<FlowNodeMeta.TableValue> values = flowInfoPostParam.flowNodeMeta.Values;
        for (FlowNodeMeta.TableValue value : values) {
            for (FeedItem item : feedbackItems) {
                if (value.FieldName.equals(item.Name)) {
                    value.FieldValue = item.Value;
                }
            }
        }

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
                this.caseItem.ActiveName,
                formBeanFragment.getAbsolutePaths(),
                formBeanFragment.getRelativePaths());

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {
                        Toast.makeText(CaregiverDetailInfoReportAcitvity.this, "上传成功!", Toast.LENGTH_SHORT).show();

                        saveSuccess();
                    } else {
                        Toast.makeText(CaregiverDetailInfoReportAcitvity.this, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);


    }

    private void saveSuccess() {
        Toast.makeText(CaregiverDetailInfoReportAcitvity.this, "保存成功", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("FlowNodeMeta", new Gson().toJson(FlowNodeMeta));
        setResult(Activity.RESULT_OK, intent);
        AppManager.finishActivity(CaregiverDetailInfoReportAcitvity.this);
    }
}
