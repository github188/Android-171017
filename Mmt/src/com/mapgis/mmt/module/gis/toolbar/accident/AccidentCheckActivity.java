package com.mapgis.mmt.module.gis.toolbar.accident;

import android.os.Bundle;
import android.view.View;

import com.mapgis.mmt.BaseActivity;

/**
 * 爆管分析结果
 */

public class AccidentCheckActivity extends BaseActivity {

    private AccidentCheckFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseTextView().setText("爆管分析结果");

        fragment = AccidentCheckFragment.newInstance(getIntent().getStringExtra("strResult"));

        addFragment(fragment);

        getBaseLeftImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.getBtnOk().performClick();
            }
        });
    }

    @Override
    public void onBackPressed() {
        fragment.getBtnOk().performClick();
//        super.onBackPressed();
    }
}
