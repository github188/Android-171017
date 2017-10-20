package com.repair.zhoushan.module.eventmanage.eventreporthistory;

import android.app.Activity;
import android.content.Intent;

import com.mapgis.mmt.config.ServerConnectConfig;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.entity.EventItem;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;

import java.util.ArrayList;

public class ERHListActivity_bak extends SimplePagerListActivity {

    @Override
    public void init() {

        final ArrayList<EventItem> eventItemList = new ArrayList<EventItem>();

        mSimplePagerListDelegate = new SimplePagerListDelegate<EventItem>(ERHListActivity_bak.this, eventItemList, EventItem.class) {
            @Override
            protected SimpleBaseAdapter generateAdapter() {
                return new ERHAdapter(ERHListActivity_bak.this, eventItemList);
            }

            @Override
            protected String generateUrl() {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EventManage/GetEventHistoryWithPaging")
                        .append("?pageSize=").append(getPageSize())
                        .append("&pageIndex=").append(getLoadPageIndex())
                        .append("&sortFields=上报时间&direction=desc")
                        .append("&userID=").append(getUserIdStr());

                return sb.toString();
            }
        };
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // 有数据处理完毕后,会回到该界刷新数据
            if (requestCode == Constants.DEFAULT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                updateView();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

