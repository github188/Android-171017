package com.repair.auxiliary.conditionquery.module;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;

public class ConditionChoseActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InfoQueryFragment fragment = new InfoQueryFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.baseFragment, fragment);
        ft.show(fragment);
        ft.commit();

        getBaseTextView().setText("条件选择");
    }
}
