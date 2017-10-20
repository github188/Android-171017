package com.repair.beihai.poj.custrompag;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.repair.beihai.poj.hbpoj.module.construct.ConstructReportActivity;
import com.repair.zhoushan.entity.AssistModule;
import com.repair.zhoushan.entity.FeedbackInfo;
import com.repair.zhoushan.entity.FlowInfoItem;
import com.repair.zhoushan.entity.FlowTableInfo;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;
import com.repair.zhoushan.module.casemanage.casedetail.FeebReportActivity;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/8/1.
 */
public class ConstructDetailActivity extends CaseDetailActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSwipeFinish(false);
    }

    @Override
    protected void feedbackReportActivity(String caseNo, FeedbackInfo feedbackInfo) {
        Intent intent;
        if ("户表报装施工进度".equals(feedbackInfo.FBBiz)) {
            intent = new Intent(ConstructDetailActivity.this, ConstructReportActivity.class);
        } else {
            intent = new Intent(ConstructDetailActivity.this, FeebReportActivity.class);
        }
        intent.putExtra("caseno", caseNo);
        intent.putExtra("bizName", feedbackInfo.FBBiz);
        intent.putExtra("tableName", feedbackInfo.FBTable);
        intent.putExtra("viewMode", TabltViewMode.REPORT.getTableViewMode());
        intent.putExtra("eventCode", caseItemEntity.EventCode);
        intent.putExtra("eventTableName", caseItemEntity.EventMainTable);

        ConstructDetailActivity.this.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(ConstructDetailActivity.this);
    }

    @Override
    protected List<FeedbackInfo> getFeedbackInfos() {

        List<FeedbackInfo> feedbackInfos = new ArrayList<>();

        //已办箱（工单总览）
        if (mFlowTableInfos != null) {
            for (FlowTableInfo fti : mFlowTableInfos) {
                List<AssistModule> ams = fti.AssistModules;
                if (ams == null) {
                    continue;
                }
                for (AssistModule am : ams) {
                    String params = am.ViewParam;
                    if (TextUtils.isEmpty(params)) {
                        continue;
                    }
                    String[] paramArr = params.split(",");
                    if (paramArr.length != 2) {
                        continue;
                    }
                    String tableName = paramArr[1];
                    String bizName = paramArr[0];

                    if (TextUtils.isEmpty(tableName) || TextUtils.isEmpty(bizName)) {
                        continue;
                    }

                    feedbackInfos.add(new FeedbackInfo(bizName, tableName));
                }
            }
            return feedbackInfos;
        }

        //在办箱
        if (flowInfoItemList != null) {
            for (FlowInfoItem fti : flowInfoItemList) {
                List<AssistModule> ams = fti.AssistModules;
                if (ams == null) {
                    continue;
                }
                for (AssistModule am : ams) {
                    String params = am.ViewParam;
                    if (TextUtils.isEmpty(params)) {
                        continue;
                    }
                    String[] paramArr = params.split(",");
                    if (paramArr.length != 2) {
                        continue;
                    }
                    String tableName = paramArr[1];
                    String bizName = paramArr[0];

                    if (TextUtils.isEmpty(tableName) || TextUtils.isEmpty(bizName)) {
                        continue;
                    }
                    feedbackInfos.add(new FeedbackInfo(bizName, tableName));
                }
            }
            return feedbackInfos;
        }

        return feedbackInfos;
    }

}
