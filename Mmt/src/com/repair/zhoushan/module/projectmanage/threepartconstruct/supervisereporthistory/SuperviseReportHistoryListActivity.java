package com.repair.zhoushan.module.projectmanage.threepartconstruct.supervisereporthistory;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.repair.zhoushan.entity.CaseItem;

/**
 * 监管上报历史
 */
public class SuperviseReportHistoryListActivity extends BaseActivity {

    protected SuperviseReportHistoryListFragment fragment;

    private CaseItem caseItemEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        caseItemEntity = getIntent().getParcelableExtra("caseItem");
        String title = getIntent().getStringExtra("title");

        if (!BaseClassUtil.isNullOrEmptyString(title)) {
            getBaseTextView().setText(title);
        } else {
            getBaseTextView().setText("监管上报历史");
        }

        fragment = new SuperviseReportHistoryListFragment();
        Bundle argBundle = new Bundle();
        argBundle.putParcelable("CaseItemEntity", caseItemEntity);
        fragment.setArguments(argBundle);
        addFragment(fragment);
    }

}
