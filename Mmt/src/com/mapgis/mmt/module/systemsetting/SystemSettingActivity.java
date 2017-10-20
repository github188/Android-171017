package com.mapgis.mmt.module.systemsetting;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;

public class SystemSettingActivity extends BaseActivity {
    private SystemSettingFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            getBaseTextView().setText("系统设置");

            fragment = SystemSettingFragment.newInstance();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            ft.add(R.id.baseFragment, fragment);
            ft.show(fragment);

            ft.commitAllowingStateLoss();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public SystemSettingFragment getSettingFragment(){
        return this.fragment;
    }
}

