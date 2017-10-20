package com.repair.shaoxin.water.riskonsitereport;

import android.text.TextUtils;
import android.view.View;

import com.mapgis.mmt.common.widget.customview.ImageDotView;
import com.repair.zhoushan.module.casemanage.casedetail.FeebReportActivity;

public class RiskOnSiteFBReportActivity extends FeebReportActivity {

    @Override
    protected void doOnViewCreated() {
        super.doOnViewCreated();

        String riskArea = getIntent().getStringExtra("RiskArea");

        if (!TextUtils.isEmpty(riskArea)) {
            View view = flowBeanFragment.findViewByName("隐患区域");

            if (view instanceof ImageDotView) {
                ((ImageDotView) view).setValue(riskArea);
            }
        }
    }
}
