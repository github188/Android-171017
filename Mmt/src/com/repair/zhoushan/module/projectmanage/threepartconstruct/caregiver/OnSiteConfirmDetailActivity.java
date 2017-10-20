package com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.entity.FlowInfoItem;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;

import java.util.List;

/**
 * "施工确认流程"-"现场确认"，自定义界面
 */
public class OnSiteConfirmDetailActivity extends CaseDetailActivity {

    @Override
    protected void createBottomView() {

        createRollbackBottomButton(OnSiteConfirmDetailActivity.this);

        createFeedbackBottomButton(); // 反馈

        BottomUnitView manageUnitView = new BottomUnitView(OnSiteConfirmDetailActivity.this);
        manageUnitView.setContent(editableFormIndex >= 0 ? flowInfoItemList.get(editableFormIndex).FlowInfoConfig.NodeName : "移交");
        manageUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(OnSiteConfirmDetailActivity.this, OnSiteConfirmActivity.class);
                intent.putExtra("ListItemEntity", caseItemEntity);
                intent.putExtra("FlowInfoItemList", flowInfoItemListStr);
                startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(OnSiteConfirmDetailActivity.this);
            }
        });

        super.multFBAndAssistModule();
    }


    FlowNodeMeta.TableGroup fbTableGroup = null;
    List<FlowNodeMeta.TableValue> values = null;

    private void createFeedbackBottomButton() {

        for (FlowInfoItem flowInfoItem : flowInfoItemList) {
            FlowNodeMeta flowNodeMeta = flowInfoItem.FlowNodeMeta;

            if (flowNodeMeta == null) {
                continue;
            }

            List<FlowNodeMeta.TableGroup> Groups = flowNodeMeta.Groups;

            List<FlowNodeMeta.TableValue> Values = flowNodeMeta.Values;

            for (FlowNodeMeta.TableGroup tableGroupItem : Groups) {
                if ("反馈信息".equals(tableGroupItem.GroupName)) {
                    fbTableGroup = tableGroupItem;
                    break;
                }
            }
            if (fbTableGroup != null) {
                values = Values;
                break;
            }
        }

        if (fbTableGroup == null || values == null) {
            MyApplication.getInstance().showMessageWithHandle("请为事件表配置反馈信息分组");
            return;
        }

        final FlowNodeMeta fbFlowNodeMeta = new FlowNodeMeta();

        FlowNodeMeta.TableGroup tableGroup = fbFlowNodeMeta.new TableGroup();

        tableGroup.Schema.addAll(fbTableGroup.Schema);
        fbFlowNodeMeta.Groups.add(tableGroup);
        for (FlowNodeMeta.FieldSchema fieldSchema : fbTableGroup.Schema) {
            String name = fieldSchema.FieldName;
            String val = fieldSchema.PresetValue;
            for (FlowNodeMeta.TableValue item : values) {
                if (item.FieldName.equals(name)) {
                    if (!TextUtils.isEmpty(item.FieldValue)) {
                        val = item.FieldValue;
                    }
                    break;
                }
            }
            fbFlowNodeMeta.Values.add(fbFlowNodeMeta.new TableValue(name, val));
        }

        BottomUnitView manageUnitView = new BottomUnitView(OnSiteConfirmDetailActivity.this);
        manageUnitView.setContent("反馈");
        manageUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(OnSiteConfirmDetailActivity.this, OSCFeedbackDialogActivity.class);
                intent.putExtra("Tag", fbTableGroup.GroupName);
                intent.putExtra("Title", fbTableGroup.GroupName);
                intent.putExtra("CaseItem", caseItemEntity);
                intent.putExtra("FlowNodeMeta", fbFlowNodeMeta);
                intent.putExtra("GDFormBean", fbFlowNodeMeta.mapToGDFormBean());

                startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Constants.DEFAULT_REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK) {

            backByReorder(true);
        }
    }
}