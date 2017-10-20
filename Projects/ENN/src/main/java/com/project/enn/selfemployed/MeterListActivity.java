package com.project.enn.selfemployed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.entity.CaseItem;

/**
 * 表具信息列表
 */
public class MeterListActivity extends BaseActivity {

    private MeterListFragment fragment;

    private CaseItem caseItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.caseItem = getIntent().getParcelableExtra("ListItemEntity");

        createView();
        createBottomView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String caller = intent.getStringExtra("Caller");
        if (EditMeterActivity.class.getName().equals(caller)) {
            fragment.updateData();
        }
    }

    private void createView() {

        getBaseTextView().setText("表具信息");

        this.fragment = new MeterListFragment();

        Bundle args = new Bundle();
        args.putParcelable("ListItemEntity", caseItem);
        fragment.setArguments(args);

        addFragment(fragment);
    }

    private void createBottomView() {

        BottomUnitView addUnitView = new BottomUnitView(MeterListActivity.this);
        addUnitView.setContent("添加表具");
        addUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(addUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                Intent intent = new Intent(MeterListActivity.this, AddMeterActivity.class);
                intent.putExtra("ListItemEntity", caseItem);
                fragment.startActivityForResult(intent, MeterListFragment.ADD_INFO_REQUEST_CODE);
            }
        });
    }
}
