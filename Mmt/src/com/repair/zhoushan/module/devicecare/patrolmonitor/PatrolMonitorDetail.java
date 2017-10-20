package com.repair.zhoushan.module.devicecare.patrolmonitor;

import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;

/**
 * Created by liuyunfan on 2016/6/24.
 */
public class PatrolMonitorDetail extends CaseDetailActivity {

    @Override
    protected void createBottomView() {

        super.createBottomView();

        BottomUnitView manageUnitView = new BottomUnitView(PatrolMonitorDetail.this);
        manageUnitView.setContent("碰接点");
        manageUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatrolMonitorDetail.this, TouchListActivity.class);
                intent.putExtra("EventCode", caseItemEntity.EventCode);
                startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(PatrolMonitorDetail.this);
            }
        });

        super.multFBAndAssistModule();
    }
}
