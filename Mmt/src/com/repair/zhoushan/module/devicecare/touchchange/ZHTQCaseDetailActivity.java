package com.repair.zhoushan.module.devicecare.touchchange;

import android.content.Intent;
import android.view.View;

import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;

/**
 * Created by liuyunfan on 2016/3/15.
 */
public class ZHTQCaseDetailActivity extends CaseDetailActivity {

    @Override
    protected void createBottomView() {
        super.createBottomView();
        BottomUnitView manageUnitView = new BottomUnitView(ZHTQCaseDetailActivity.this);
        manageUnitView.setContent("延长停气");
        manageUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GDFormBean gdFormBean = GDFormBean.generateSimpleForm(
                        new String[]{"DisplayName", "延长时间", "Name", "延长时间", "Type", "日期框", "Validate", "1"},
                        new String[]{"DisplayName", "延长原因", "Name", "延长原因", "Type", "短文本", "DisplayColSpan", "100"});
                Intent intent = new Intent(ZHTQCaseDetailActivity.this, DelayStopGasDialogActivity.class);
                intent.putExtra("Title", "申请延长停气");
                intent.putExtra("GDFormBean", gdFormBean);
                intent.putExtra("caseno", caseItemEntity.EventCode);
                startActivity(intent);
            }
        });
        super.multFBAndAssistModule();
    }
}
