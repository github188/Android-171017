package com.repair.auxiliary.conditionquery.changsha;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.mapgis.mmt.BaseActivity;

/**
 * Created by liuyunfan on 2016/4/20.
 */
public class CS_ConditionQueryAuxListActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleAndClear("附属数据");
        Fragment fragment = new CS_ConditionQueryAuxListFragment();
        fragment.setArguments(getIntent().getBundleExtra("bundle"));
        replaceFragment(fragment);
    }

}
