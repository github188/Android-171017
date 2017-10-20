package com.mapgis.mmt.module.gis.toolbar.query.spatial;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;

public class WhereQueryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InfoQueryFragment fragment = new InfoQueryFragment();

        Bundle args = new Bundle();
        args.putAll(getIntent().getExtras());
        fragment.setArguments(args);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.baseFragment, fragment);
        ft.show(fragment);
        ft.commit();

        getBaseTextView().setText("高级查询");
    }
}
