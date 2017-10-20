package com.mapgis.mmt.module.systemsetting.backgruoundinfo;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;


public class BackgroundInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        getBaseTextView().setText("后台统计");

        Fragment fragment = BackgroundInfoFragment.newInstance();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.add(R.id.baseFragment, fragment);
        ft.show(fragment);

        ft.commitAllowingStateLoss();
    }
}
