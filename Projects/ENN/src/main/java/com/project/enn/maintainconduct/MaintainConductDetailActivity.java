package com.project.enn.maintainconduct;

import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;

public class MaintainConductDetailActivity extends CaseDetailActivity {

    @Override
    protected void createBottomView() {

        createRollbackBottomButton(this);

        BottomUnitView manageUnitView = new BottomUnitView(MaintainConductDetailActivity.this);
        manageUnitView.setContent(editableFormIndex >= 0 ? flowInfoItemList.get(editableFormIndex).FlowInfoConfig.NodeName : "移交");
        manageUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MaintainConductDetailActivity.this, MaintainConductActivity.class);
                intent.putExtra("ListItemEntity", caseItemEntity);
                intent.putExtra("FlowInfoItemList", flowInfoItemListStr);
                startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(MaintainConductDetailActivity.this);
            }
        });

        super.multFBAndAssistModule();
    }
}
