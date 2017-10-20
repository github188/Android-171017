package com.project.enn.selfemployed;

import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.project.enn.R;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;

/**
 * 在办箱"户内置换-置换执行"节点自定义模块
 */
public class ExchangeExecuteDetailActivity extends CaseDetailActivity {

    @Override
    protected void createBottomView() {

        createRollbackBottomButton(this);

        BottomUnitView manageUnitView = new BottomUnitView(ExchangeExecuteDetailActivity.this);
        manageUnitView.setContent(editableFormIndex >= 0 ? flowInfoItemList.get(editableFormIndex).FlowInfoConfig.NodeName : "移交");
        manageUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ExchangeExecuteDetailActivity.this, ExchangeExecuteActivity.class);
                intent.putExtra("ListItemEntity", caseItemEntity);
                intent.putExtra("FlowInfoItemList", flowInfoItemListStr);
                startActivity(intent);
            }
        });

        super.multFBAndAssistModule();
    }

}
