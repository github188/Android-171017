package com.repair.gisdatagather.product.projectlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.MultiSwitchFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.repair.gisdatagather.product.addproject.AddProjectDialogActivity;
import com.simplecache.ACache;

/**
 * Created by liuyunfan on 2016/5/3.
 */
public class ProjectListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultiSwitchFragment fragment = new MultiSwitchFragment();
        ProjectListFragment doingFragment = new ProjectListFragment();
        Bundle doingbundle = new Bundle();
        doingbundle.putBoolean("isdoingBox", true);
        doingFragment.setArguments(doingbundle);

        ProjectListFragment doneFragment = new ProjectListFragment();
        Bundle donebundle = new Bundle();
        donebundle.putBoolean("isdoingBox", false);
        doneFragment.setArguments(donebundle);
        fragment.setDate(new String[]{"在办工程", "已办工程"}, new Fragment[]{doingFragment, doneFragment});
        replaceFragment(fragment);

        BottomUnitView reportButton = new BottomUnitView(this, "新建工程", null);
        reportButton.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(reportButton, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GDFormBean gdFormBean = GDFormBean.generateSimpleForm(
                        new String[]{"DisplayName", "工程名称", "Name", "工程名称", "Type", "长文本", "Validate", "1", "Value", BaseClassUtil.getSystemTime("yyyy-MM-dd")}
                );
                Intent intent = new Intent(ProjectListActivity.this, AddProjectDialogActivity.class);
                intent.putExtra("Title", "新建工程");
                intent.putExtra("GDFormBean", gdFormBean);
                ProjectListActivity.this.startActivity(intent);
            }
        });

        cacheLayer();
    }

    public void cacheLayer() {
        MyApplication.getInstance().submitExecutorService(new Runnable() {
            @Override
            public void run() {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GisUpdate/GetGISDeviceConfigList";

                String result = NetUtil.executeHttpGet(url);

                if (TextUtils.isEmpty(result)) {
                    return;
                }
                ACache mCache = BaseClassUtil.getACache();
                mCache.put("layerInfo", result);

            }
        });

    }
}
