package com.project.enn.login;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.project.enn.R;

public class SystemSetter extends BaseActivity {
    private SystemSetterFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        findViewById(R.id.baseActionBarImageView).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fragment = new SystemSetterFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.add(R.id.baseFragment, fragment);

        ft.show(fragment);

        ft.commit();
    }

    @Override
    public void onBackPressed() {
        fragment.saveServerSetter();

        this.finish();

        MyApplication.getInstance().finishActivityAnimation(SystemSetter.this);
    }
}

