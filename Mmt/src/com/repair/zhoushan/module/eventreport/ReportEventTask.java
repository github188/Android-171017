package com.repair.zhoushan.module.eventreport;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.FlowCenterData;
import com.repair.zhoushan.module.flowcenter.FlowCenterNavigationActivity;

import java.util.ArrayList;

public class ReportEventTask extends MmtBaseTask<String, Integer, ArrayList<FlowCenterData>> {
    private final String eventType;
    private final String eventName;
    private final Bundle bundleArgs;

    private boolean isFromNavigation = false;

    public ReportEventTask(Context context, String eventType, String eventName) {
        this(context, eventType, eventName, null);
    }

    public ReportEventTask(Context context, String eventType, String eventName, Bundle bundle) {
        super(context);
        this.eventType = eventType;
        this.eventName = eventName;
        this.bundleArgs = bundle;
    }

    /**
     * 标志是从主界面导航到流程中心，只有这种情况上报界面才需要缓存数据
     */
    public void setFromNavigationMenu(boolean isFromNavigation) {
        this.isFromNavigation = isFromNavigation;
    }

    @Override
    protected ArrayList<FlowCenterData> doInBackground(String... params) {

        final ArrayList<FlowCenterData> results = new ArrayList<>();

        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/"
                + MyApplication.getInstance().getUserId() + "/GetFlowCenterData?type=mobile";

        String jsonResult = NetUtil.executeHttpGet(url);
        Results<FlowCenterData> rawResult = new Gson().fromJson(jsonResult,
                new TypeToken<Results<FlowCenterData>>() {
                }.getType());

        if (rawResult == null || rawResult.getMe.size() == 0) {
            return results;
        }

        final ArrayList<FlowCenterData> flowCenterDataList = rawResult.getMe;

        if (!TextUtils.isEmpty(eventType) && !TextUtils.isEmpty(eventName)) {
            for (FlowCenterData item : flowCenterDataList) {
                if (eventType.equals(item.BusinessType) && eventName.equals(item.EventName)) {
                    results.add(item);
                    break;
                }
            }
        } else if (!TextUtils.isEmpty(eventName)) {
            for (FlowCenterData item : flowCenterDataList) {
                if (eventName.equals(item.EventName)) {
                    results.add(item);
                    break;
                }
            }
        } else if (!TextUtils.isEmpty(eventType)) {
            for (FlowCenterData item : flowCenterDataList) {
                if (eventType.equals(item.BusinessType)) {
                    results.add(item);
                }
            }
        } else {
            results.addAll(flowCenterDataList);
        }

        return results;
    }

    @Override
    protected void onSuccess(ArrayList<FlowCenterData> results) {

        if (results.size() == 0) {
            Toast.makeText(context, "未能获取到流程信息", Toast.LENGTH_SHORT).show();

        } else if (results.size() == 1) {
            Intent intent = new Intent(context, ZSEventReportActivity.class);
            intent.putExtra("FlowCenterData", results.get(0));
            intent.putExtra(ZSEventReportActivity.FLAG_FROM_NAVIGATION_MENU, isFromNavigation);
            if (bundleArgs != null) {
                intent.putExtra("gisInfo", bundleArgs);
            }
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(context, FlowCenterNavigationActivity.class);
            intent.putParcelableArrayListExtra("FlowCenterDataList", results);
            intent.putExtra(ZSEventReportActivity.FLAG_FROM_NAVIGATION_MENU, isFromNavigation);
            if (bundleArgs != null) {
                intent.putExtra("gisInfo", bundleArgs);
            }
            context.startActivity(intent);
        }
    }
}
