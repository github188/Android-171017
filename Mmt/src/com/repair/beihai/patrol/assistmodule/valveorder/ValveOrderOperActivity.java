package com.repair.beihai.patrol.assistmodule.valveorder;

import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.tablecommonhand.TableOneRecordActivity;

public class ValveOrderOperActivity extends TableOneRecordActivity {

    @Override
    protected void handViewData() {

        // 关阀节点只允许填写关阀相关信息，开阀的时候只允许填写开阀的信息
        boolean isOpenValveNode = getIntent().getBooleanExtra("OpenValveNode", false);
        String readonlyGroupName = isOpenValveNode ? "关阀信息" : "开阀信息";

        if (mFlowNodeMetas != null && mFlowNodeMetas.size() > 0) {
            boolean isFound = false;
            for (int i = 0, length = mFlowNodeMetas.size(); i < length && !isFound; i++) {
                FlowNodeMeta flowNodeMeta = mFlowNodeMetas.get(i);
                for (FlowNodeMeta.TableGroup tableGroup : flowNodeMeta.Groups) {
                    if (readonlyGroupName.equals(tableGroup.GroupName)) {
                        for (FlowNodeMeta.FieldSchema fieldSchema : tableGroup.Schema) {
                            fieldSchema.ReadOnly = 1;
                        }
                        isFound = true;
                        break;
                    }
                }
            }
        }

        super.handViewData();
    }
}
