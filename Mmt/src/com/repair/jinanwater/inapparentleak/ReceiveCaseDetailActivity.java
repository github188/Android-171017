package com.repair.jinanwater.inapparentleak;

import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.MyApplication;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;

/**
 * 暗漏维修流程："接单鉴定"、"维修回复" 两个节点的自定义视图
 */
public class ReceiveCaseDetailActivity extends CaseDetailActivity {

    @Override
    protected void createHandoverBtn() {
        String handoverDesc = editableFormIndex >= 0 ? flowInfoItemList.get(editableFormIndex).FlowInfoConfig.NodeName : "移交";
        addBottomUnitView(handoverDesc, false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReceiveCaseDetailActivity.this, ReceiveCaseHandoverActivity.class);
                intent.putExtra("ListItemEntity", caseItemEntity);
                intent.putExtra("FlowInfoItemList", flowInfoItemListStr);
                startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(ReceiveCaseDetailActivity.this);
            }
        });
    }
}
