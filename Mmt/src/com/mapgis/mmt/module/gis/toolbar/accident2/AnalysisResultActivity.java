package com.mapgis.mmt.module.gis.toolbar.accident2;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;

/**
 * Created by Comclay on 2017/3/1.
 * 爆管分析结果界面
 */

public class AnalysisResultActivity extends BaseActivity{

    private AnalysisResultFragment mFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseTextView().setText(R.string.title_analsysis_result);
        mFragment = AnalysisResultFragment.newInstance();
        addFragment(mFragment);
    }

    /**
     * 如果用户是按返回键返回需要将对应的数据结构传回
     */
    @Override
    public void onCustomBack() {
        mFragment.onBackMapView();
    }
}
