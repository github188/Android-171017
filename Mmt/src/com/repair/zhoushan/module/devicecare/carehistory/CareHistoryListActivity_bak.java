package com.repair.zhoushan.module.devicecare.carehistory;

import com.mapgis.mmt.config.ServerConnectConfig;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;
import com.repair.zhoushan.module.devicecare.ScheduleTask;

import java.util.ArrayList;
import java.util.UUID;

public class CareHistoryListActivity_bak extends SimplePagerListActivity {

    private String bizName;
    private String gisCode;

    @Override
    public void init() {

        this.bizName = getIntent().getStringExtra("BizName");
        this.gisCode = getIntent().getStringExtra("GisCode");

        final ArrayList<ScheduleTask> dataList = new ArrayList<ScheduleTask>();

        mSimplePagerListDelegate = new SimplePagerListDelegate<ScheduleTask>(CareHistoryListActivity_bak.this, dataList, ScheduleTask.class) {
            @Override
            protected SimpleBaseAdapter generateAdapter() {
                return new CareHistoryAdapter(CareHistoryListActivity_bak.this, dataList);
            }

            @Override
            protected String generateUrl() {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                        .append(getUserIdStr()).append("/ScheduleTasks")
                        .append("?_mid=").append(UUID.randomUUID().toString())
                        .append("&pageIndex=").append(getLoadPageIndex())
                        .append("&pageSize=").append(getPageSize())
                        .append("&sortFields=开始时间&direction=desc")
                        .append("&bizName=").append(bizName)
                        .append("&gisCode=").append(gisCode);

                return sb.toString();
            }
        };
    }

}
