package com.repair.changzhou.pianguan;

import android.support.v4.app.FragmentTransaction;

import com.mapgis.mmt.BaseActivity;

public class PianGuanCaseListActivity extends BaseActivity {

    protected PianGuanCaseListFragment fragment;

    @Override
    protected void setDefaultContentView() {

        setSwipeBackEnable(false);

        fragment = new PianGuanCaseListFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(android.R.id.content, fragment, PianGuanCaseListFragment.TAG);
        transaction.show(fragment);
        transaction.commitAllowingStateLoss();
    }

}
