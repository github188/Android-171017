package com.patrolproduct.module.spatialquery;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.mapgis.mmt.R;

import java.util.HashMap;

/**
 * 巡检产品的设备详情界面，存在切换到<计划反馈>情况
 *
 * @author Zoro
 */
public class PipeDetailForPatrolActivity extends PipeDetailActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getIntent().putExtra("FragmentClass", PipeDetailForPatrolFragment.class);
        super.onCreate(savedInstanceState);
    }
}
