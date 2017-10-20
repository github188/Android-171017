package com.project.enn.selfemployed;

import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.project.enn.R;
import com.repair.zhoushan.module.casemanage.casehandover.CaseHandoverActivity;

public class ExchangeExecuteActivity extends CaseHandoverActivity {

    @Override
    protected void createBottomView() {

        BottomUnitView manageUnitView = new BottomUnitView(this);
        manageUnitView.setContent("表具信息");
        manageUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ExchangeExecuteActivity.this, MeterListActivity.class);
                intent.putExtra("ListItemEntity", caseItemEntity);
                startActivity(intent);
            }
        });

        super.createBottomView();
    }
}
