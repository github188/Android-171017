package com.repair.zhoushan.module.casemanage.mydonecase;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.mapgis.mmt.BaseActivity;

public class MyDoneCaseListActivity extends BaseActivity {


    @Override
    protected void setDefaultContentView() {

        setSwipeBackEnable(false);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(DoneCaseListFragment.class.getName());

        if (fragment == null) {
            String defaultFlowNames = getIntent().getStringExtra("DefaultFlowNames");
            fragment = DoneCaseListFragment.newInstance(defaultFlowNames);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.add(android.R.id.content, fragment, DoneCaseListFragment.class.getName());
            transaction.show(fragment);
            transaction.commitAllowingStateLoss();
        }
    }

}
