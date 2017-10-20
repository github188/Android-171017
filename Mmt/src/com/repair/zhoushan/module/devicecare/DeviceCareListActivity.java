package com.repair.zhoushan.module.devicecare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.mapgis.mmt.BaseActivity;
import com.repair.zhoushan.common.Constants;

public class DeviceCareListActivity extends BaseActivity {

    private DeviceCareListFragment fragment;

    @Override
    protected void setDefaultContentView() {

        setSwipeBackEnable(false);

        this.fragment = (DeviceCareListFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);

        if (fragment == null) {
            fragment = new DeviceCareListFragment();

            Bundle argBundle = new Bundle();
            argBundle.putString("BizName", getIntent().getStringExtra("BizName"));
            fragment.setArguments(argBundle);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, fragment, DeviceCareListFragment.class.getName());
            ft.show(fragment);
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasCategory(Constants.CATEGORY_BACK_TO_LIST)) {
            intent.removeCategory(Constants.CATEGORY_BACK_TO_LIST);
            fragment.updateData();
        }
        setIntent(intent);
    }

}
