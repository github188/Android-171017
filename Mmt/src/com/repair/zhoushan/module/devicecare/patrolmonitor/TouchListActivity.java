package com.repair.zhoushan.module.devicecare.patrolmonitor;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.mapgis.mmt.BaseActivity;

/**
 * Created by liuyunfan on 2016/7/6.
 */
public class TouchListActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = new Bundle();
        bundle.putString("EventCode", getIntent().getStringExtra("EventCode"));
        setTitleAndClear("碰接点记录");
        Fragment fragment = new TouchListFragment();
        fragment.setArguments(bundle);
        replaceFragment(fragment);
    }
}
