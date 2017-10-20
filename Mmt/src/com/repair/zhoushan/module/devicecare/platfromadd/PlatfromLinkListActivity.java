package com.repair.zhoushan.module.devicecare.platfromadd;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.repair.zhoushan.common.ConfigFieldsAdapter;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.TableColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyunfan on 17/6/12.
 */

public class PlatfromLinkListActivity extends SimplePagerListActivity {

    private String bizName = "";
    private String deviceID = "";

    private ArrayList<List<TableColumn>> adapterdataList = new ArrayList<List<TableColumn>>();

    private ArrayList<ScheduleTask> scheduleTasks = new ArrayList<>();

    @Override
    public void init() {
        if (!initParams()) {

            return;
        }

        mSimplePagerListDelegate = new SimplePagerListDelegate<ScheduleTask>(PlatfromLinkListActivity.this, scheduleTasks, ScheduleTask.class) {
            @Override
            protected SimpleBaseAdapter generateAdapter() {

                return new ConfigFieldsAdapter(PlatfromLinkListActivity.this, adapterdataList);
            }

            @Override
            protected String generateUrl() {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                        .append(MyApplication.getInstance().getUserId() + "")
                        .append("/CommericalEquipmentList")
                        .append("?deviceID=").append(deviceID)
                        .append("&bizName=").append(bizName);

                return sb.toString();
            }

            @Override
            protected void initRootView() {
                setContentView(R.layout.platfrom_list_link_activity_xa);
            }

            @Override
            protected void adapterAddData(ResultData<ScheduleTask> newData) {

                if (newData == null) {
                    return;
                }

                if (newData.DataList.size() == 0) {
                    if (!getIsLoadMoreMode()) { // Refresh
                        Toast.makeText(PlatfromLinkListActivity.this, "没有记录", Toast.LENGTH_SHORT).show();
                        scheduleTasks.clear();
                        adapterdataList.clear();
                        adapter.notifyDataSetChanged();
                    } else {               // LoadMore
                        Toast.makeText(PlatfromLinkListActivity.this, "没有更多数据", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    if (getIsLoadMoreMode()) {
                        int curIdx = getCurrentPageIndex();
                        setCurrentPageIndex(curIdx + 1);
                    } else {
                        Toast.makeText(PlatfromLinkListActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                        scheduleTasks.clear();
                        adapterdataList.clear();
                    }
                    scheduleTasks.addAll(newData.DataList);

                    for (ScheduleTask scheduleTask : newData.DataList) {
                        adapterdataList.add(scheduleTask.WebRow);
                    }

                    adapter.notifyDataSetChanged();
                }

            }
        };
    }

    @Override
    protected void afterViewCreated() {
        super.afterViewCreated();

        TextView titleView = (TextView) findViewById(R.id.baseActionBarTextView);
        titleView.setText(bizName);
        addBottomBtn();
        // 每项的点击事件
        mSimplePagerListDelegate.getmPullRefreshListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ScheduleTask scheduleTask = scheduleTasks.get(arg2 - 1);

                Intent intent = new Intent(PlatfromLinkListActivity.this, PlatfromLinkDetailActivity.class);
                intent.putExtra("mode", "view");
                intent.putExtra("bizName", bizName);
                intent.putExtra("GisCode", scheduleTask.GisCode);
                intent.putExtra("ID", scheduleTask.ID);
                intent.putExtra("deviceID", deviceID);
                PlatfromLinkListActivity.this.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(PlatfromLinkListActivity.this);
            }
        });
    }

    private void addBottomBtn() {
        //添加底部按钮
        View rootView = findViewById(android.R.id.content);

        BottomUnitView backUnitView = new BottomUnitView(this);

        backUnitView.setContent("新增" + bizName);
        backUnitView.setImageResource(R.drawable.handoverform_report);
        backUnitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlatfromLinkListActivity.this, PlatfromLinkDetailActivity.class);
                intent.putExtra("mode", "add");
                intent.putExtra("bizName", bizName);
                intent.putExtra("deviceID", deviceID);
                PlatfromLinkListActivity.this.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(PlatfromLinkListActivity.this);
            }
        });
        RelativeLayout bottomBtn = (RelativeLayout) rootView.findViewById(R.id.bottombtn);
        bottomBtn.setVisibility(View.VISIBLE);
        bottomBtn.addView(backUnitView);

    }

    private boolean initParams() {

        Intent intent = getIntent();

        bizName = intent.getStringExtra("bizName");

        if (TextUtils.isEmpty(bizName)) {
            showErrorMsg("关联类型错误");
            return false;
        }

        deviceID = intent.getStringExtra("deviceID");
        if (TextUtils.isEmpty(deviceID)) {
            showErrorMsg("关联台账不存在");
            return false;
        }

        return true;
    }
}
