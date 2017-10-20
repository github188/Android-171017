package com.mapgis.mmt.module.gis.spatialquery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.mapgis.mmt.BaseActivity;

public class PipeDetailActivity extends BaseActivity {

    @Override
    protected void setDefaultContentView() {
        setSwipeBackEnable(false);

        @SuppressWarnings("unchecked")
        Class<? extends PipeDetailFragment> fragmentClass
                = (Class<? extends PipeDetailFragment>) getIntent().getSerializableExtra("FragmentClass");

        // 先检查指定的 Fragment，若未指定，则展示默认的 Fragment
        if (fragmentClass == null) {
            fragmentClass = PipeDetailFragment.class;
        }

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentClass.getName());

        if (fragment == null) {

            try {
                fragment = fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (fragment == null) {
                Toast.makeText(this, "初始化界面失败", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle arguments = new Bundle();
            arguments.putAll(getIntent().getExtras());
            fragment.setArguments(arguments);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, fragment, fragmentClass.getName());
            ft.show(fragment);
            ft.commit();
        }
    }
}
