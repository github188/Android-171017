package com.repair.beihai.patrol.assistmodule.valveorder;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.common.BaseTaskResults;
import com.repair.zhoushan.entity.AssistModule;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

import java.util.ArrayList;

/**
 * 阀门启闭列表
 */
public class ValveOrderListActivity extends BaseActivity implements ValveOrderAdapter.OnListItemClickListener {

    public static final int RC_VALVE_ORDER_DETAIL = 1;

    private FloatingActionButton syncButton;
    private RecyclerView mRecyclerView;
    private ValveOrderAdapter adapter;
    private final ArrayList<ValveModel> data = new ArrayList<>();

    private CaseItem caseItem;
    private AssistModule assistModule;

    private boolean isOpenValveNode;

    @Override
    protected void setDefaultContentView() {
        this.assistModule = getIntent().getParcelableExtra("AssistModule");
        this.caseItem = getIntent().getParcelableExtra("ListItemEntity");

        setSwipeBackEnable(false);
        setContentView(R.layout.activity_valve_order_list);
        addBackBtnListener(getBaseLeftImageView());
        getBaseTextView().setText(assistModule.MobileViewLabel);

        // recycler view
        this.mRecyclerView = (RecyclerView) findViewById(R.id.content_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        // 根据辅助视图手持参数配置(open)判断是否是"开阀处理"节点
        // String nodeName = getIntent().getStringExtra("CurrentNodeName");
        // final boolean isOpenValveNode = "开阀处理".equals(nodeName);
        isOpenValveNode = "open".equalsIgnoreCase(assistModule.MobileViewParam);
        this.adapter = new ValveOrderAdapter(this, data, isOpenValveNode);
        // 只读，不让操作
        if (!"scan".equalsIgnoreCase(assistModule.MobileViewParam)) {
            adapter.setOnListItemClickListener(this);
        }
        mRecyclerView.setAdapter(adapter);

        this.syncButton = (FloatingActionButton) findViewById(R.id.btn_sync);
        // syncButton.setVisibility("开阀处理".equals(nodeName) ? View.VISIBLE : View.GONE);
        syncButton.setVisibility(isOpenValveNode ? View.VISIBLE : View.GONE);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncValveState();
            }
        });

        // fetch data from network
        fetchValveOrder(caseItem.EventCode);
    }

    private void fetchValveOrder(final String eventCode) {

        BaseTaskResults<Void, Void, ValveModel> baseTask
                = new BaseTaskResults<Void, Void, ValveModel>(ValveOrderListActivity.this) {
            @NonNull
            @Override
            protected String getRequestUrl() {
                return ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_ProjectManage_BH/REST/ProjectManageREST.svc/GetValveOrderInfo?eventCode="
                        + eventCode;
            }

            @Override
            protected void onSuccess(Results<ValveModel> results) {
                final ResultData<ValveModel> resultData = results.toResultData();
                if (resultData.ResultCode != 200) {
                    Toast.makeText(ValveOrderListActivity.this, resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (resultData.DataList.size() == 0) {
                    Toast.makeText(ValveOrderListActivity.this, "阀门列表为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                data.clear();
                data.addAll(resultData.DataList);
                adapter.notifyDataSetChanged();
            }
        };
        baseTask.setCancellable(false);
        baseTask.mmtExecute();
    }

    private void syncValveState() {

        if (data.size() == 0) {
            return;
        }

        MmtBaseTask<String, Void, Boolean[]> baseTask
                = new MmtBaseTask<String, Void, Boolean[]>(ValveOrderListActivity.this, true, "同步中...") {
            @Override
            protected Boolean[] doInBackground(String... params) {

                StringBuilder nos = new StringBuilder();
                for (ValveModel valveModel : data) {
                    nos.append("'").append(valveModel.no).append("',");
                }
                nos.deleteCharAt(nos.length() - 1);

                // ?attCon=编号 in ('C1892','DGS739')&layerName=供水管网_蝶阀,供水管网_闸阀&fldName=开关状态&fldVal=开
                StringBuilder baseUrl = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                baseUrl.append("/services/Zondy_MapGISCitySvr_Edit/rest/EditREST.svc/mapName/MapServer/ChangeFldByAttCon")
                        .append("?attCon=").append("编号 in (").append(nos.toString()).append(")")
                        .append("&layerName=供水管网_蝶阀,供水管网_闸阀&fldName=开关状态");

                final String urlClose = baseUrl.toString() + "&fldVal=关";
                final String urlOpen = baseUrl.toString() + "&fldVal=开";

                Boolean[] results = new Boolean[2];

                String result1 = NetUtil.executeHttpGetAppointLastTime(30, urlClose);
                String result2 = NetUtil.executeHttpGetAppointLastTime(30, urlOpen);

                results[0] = Boolean.parseBoolean(result1);
                results[1] = Boolean.parseBoolean(result2);

                return results;
            }

            @Override
            protected void onSuccess(Boolean[] results) {
                if (results[0] && results[1]) {
                    Toast.makeText(context, "阀门信息同步GIS成功", Toast.LENGTH_SHORT).show();
                } else if (results[0]) {
                    Toast.makeText(context, "关阀信息同步GIS成功, 开阀信息同步GIS失败", Toast.LENGTH_SHORT).show();
                } else if (results[1]) {
                    Toast.makeText(context, "关阀信息同步GIS失败，开阀信息同步GIS成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "阀门信息同步GIS失败", Toast.LENGTH_SHORT).show();
                }
            }
        };
        baseTask.setCancellable(false);
        baseTask.mmtExecute();
    }

    @Override
    public void onItemClicked(ValveModel valveModel) {

        Intent intent = new Intent(ValveOrderListActivity.this, ValveOrderOperActivity.class);
        intent.putExtra("tableName", "阀门开关信息表");
        intent.putExtra("ID", valveModel.ID);
        intent.putExtra("OpenValveNode", isOpenValveNode);
        intent.putExtra("viewMode", TabltViewMode.EDIT.getTableViewMode());
        ValveOrderListActivity.this.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(ValveOrderListActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_VALVE_ORDER_DETAIL && resultCode == Activity.RESULT_OK) {
        }
    }
}
