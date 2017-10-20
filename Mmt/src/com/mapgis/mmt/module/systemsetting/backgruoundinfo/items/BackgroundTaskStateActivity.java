package com.mapgis.mmt.module.systemsetting.backgruoundinfo.items;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;

/**
 * Created by Administrator on 2017/8/22 0022.
 */

public class BackgroundTaskStateActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        getBaseTextView().setText("后台任务");

        Fragment fragment = BackgroundTaskStateFragment.getInstance();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.add(R.id.baseFragment, fragment);
        ft.show(fragment);

        ft.commitAllowingStateLoss();
    }
}
