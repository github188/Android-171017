package com.repair.zhoushan.module.casemanage.mycase;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.mapgis.mmt.BaseActivity;
import com.repair.zhoushan.common.Constants;

public class MyCaseListActivity extends BaseActivity {

    @Override
    protected void setDefaultContentView() {
        setSwipeBackEnable(false);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(CaseListFragment.TAG);

        if (fragment == null) {
            String defaultFlowNames = getIntent().getStringExtra("DefaultFlowNames");
            boolean showEventCode = getIntent().getBooleanExtra("ShowEventCode", false);
            fragment = CaseListFragment.newInstance(defaultFlowNames, showEventCode);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(android.R.id.content, fragment, CaseListFragment.TAG);
            transaction.show(fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 办理界面办理成功后跳转回该列表界面刷新数据
        if (intent.hasCategory(Constants.CATEGORY_BACK_TO_LIST)) {
            intent.removeCategory(Constants.CATEGORY_BACK_TO_LIST);
            // fragment.refreshData();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(CaseListFragment.TAG);
            if (fragment != null) {
                ((CaseListFragment) fragment).requestRefreshData();
            }
        }
    }
}
