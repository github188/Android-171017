package com.repair.zhoushan.module.casemanage.casedetail;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;
import com.repair.zhoushan.entity.CaseItem;

public class CaseHandleProcedureActivity extends BaseActivity {

    private CaseItem caseItemEntity;
    private CaseHandleProcedureFragment caseHandleProcedureFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBaseTextView().setText("办理过程");
        caseItemEntity = getIntent().getParcelableExtra("ListItemEntity");

        caseHandleProcedureFragment = new CaseHandleProcedureFragment();

        Bundle augBundle = new Bundle();
        augBundle.putParcelable("ListItemEntity", caseItemEntity);
        caseHandleProcedureFragment.setArguments(augBundle);

        addFragment(caseHandleProcedureFragment);
    }
}
