package com.repair.beihai.poj.hbpoj.module.userwaterecheck;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.login.UserBean;
import com.repair.beihai.poj.hbpoj.entity.CheckWatermeterPoj;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;

import java.util.ArrayList;

/**
 * Created by liuyunfan on 2016/8/8.
 */
public class UserWatermeterCheckPojListActivity extends SimplePagerListActivity {

    private ArrayList<CheckWatermeterPoj> eventItemList = new ArrayList<>();

    @Override
    public void init() {

        mSimplePagerListDelegate = new SimplePagerListDelegate<CheckWatermeterPoj>(UserWatermeterCheckPojListActivity.this, eventItemList, CheckWatermeterPoj.class) {
            @Override
            protected SimpleBaseAdapter generateAdapter() {
                return new UserWatermeterCheckPojAdapter(UserWatermeterCheckPojListActivity.this, eventItemList);
            }

            @Override
            protected String generateUrl() {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/MapgisCity_ProjectManage_BH/REST/ProjectManageREST.svc/ProjectManage/GetCheckWaterMeterPojWithPaging")
                        .append("?pageSize=").append(mSimplePagerListDelegate.getPageSize())
                        .append("&pageIndex=").append(mSimplePagerListDelegate.getLoadPageIndex())
                        .append("&sortFields=承办时间")
                        .append("&direction=desc")
                        .append("&userName=").append(MyApplication.getInstance().getConfigValue("UserBean",UserBean.class).TrueName);

                return sb.toString();
            }
        };

    }
}
