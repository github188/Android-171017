package com.repair.zhoushan.module.devicecare.touchchange;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.R;
import com.repair.zhoushan.entity.CrashPointRecord;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;

import java.util.ArrayList;

/**
 * Created by liuyunfan on 2016/3/15.
 */
public class TJPointListActivity extends SimplePagerListActivity {
    String caseno;

    @Override
    public void init() {
        caseno = getIntent().getStringExtra("caseno");
        final ArrayList<CrashPointRecord> eventItemList = new ArrayList<CrashPointRecord>();

        mSimplePagerListDelegate = new SimplePagerListDelegate<CrashPointRecord>(TJPointListActivity.this, eventItemList, CrashPointRecord.class) {
            @Override
            protected SimpleBaseAdapter generateAdapter() {

                return new TJPointAdapter(TJPointListActivity.this, eventItemList);
            }

            @Override
            protected String generateUrl() {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CrashReplace/GetCrashPointWithPaging")
                        .append("?pageSize=").append(getPageSize())
                        .append("&pageIndex=").append(getLoadPageIndex())
                        .append("&sortFields=ID&direction=desc")
                        .append("&caseNO=").append(caseno);

                return sb.toString();
            }
        };

    }

    @Override
    protected void afterViewCreated() {
        super.afterViewCreated();
        //添加底部按钮
        View rootView =findViewById(android.R.id.content);

        BottomUnitView backUnitView = new BottomUnitView(TJPointListActivity.this);

        backUnitView.setContent("上报碰接点");
        backUnitView.setImageResource(R.drawable.handoverform_report);
        backUnitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TJPointListActivity.this, TJPointReportActivity.class);
                intent.putExtra("caseno", caseno);
                TJPointListActivity.this.startActivityForResult(intent, 1001);
                MyApplication.getInstance().startActivityAnimation(TJPointListActivity.this);
            }
        });
        RelativeLayout bottomBtn = (RelativeLayout) rootView.findViewById(R.id.bottombtn);
        bottomBtn.setVisibility(View.VISIBLE);
        bottomBtn.addView(backUnitView);
    }

}
