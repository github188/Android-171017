package com.repair.zhoushan.module.eventmanage.eventreporthistory;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.mapgis.mmt.BaseActivity;

public class ERHListActivity extends BaseActivity {

    @Override
    protected void setDefaultContentView() {
        setSwipeBackEnable(false);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(ERHListFragment.TAG);

        if (fragment == null) {
            String defaultEventNames = getIntent().getStringExtra(ERHListFragment.PARAM_NAME_DEFAULT_EVENT_NAME);
            fragment = ERHListFragment.newInstance(defaultEventNames);

            FragmentTransaction transaction = fm.beginTransaction();
            transaction.add(android.R.id.content, fragment, ERHListFragment.TAG);
            transaction.show(fragment);
            transaction.commitAllowingStateLoss();
        }
    }
}
