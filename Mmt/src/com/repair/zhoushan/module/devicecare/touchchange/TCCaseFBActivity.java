package com.repair.zhoushan.module.devicecare.touchchange;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowInfoConfig;
import com.repair.zhoushan.entity.FlowInfoItem;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.casemanage.casehandover.CaseHandoverActivity;
import com.repair.zhoushan.module.devicecare.MaintenanceFeedBack;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by liuyunfan on 2016/3/14.
 */
public class TCCaseFBActivity extends CaseHandoverActivity {
    FlowInfoConfig flowInfoConfig;
    // 过滤条件
    private ArrayList<MaintenanceFeedBack> filterCriterias;

    // 界面结构数据
    private FlowNodeMeta mFlowNodeMeta;

    @Override
    protected void onCreateCus() {
        Intent intent = getIntent();
        caseItemEntity = intent.getParcelableExtra("ListItemEntity");
        flowInfoConfig = new Gson().fromJson(intent.getStringExtra("FlowInfoConfig"), FlowInfoConfig.class);
        flowInfoItemList = new Gson().fromJson(intent.getStringExtra("FlowInfoItem"), new TypeToken<ArrayList<FlowInfoItem>>() {
        }.getType());
        createView();
    }

    void createView() {
        new MmtBaseTask<String, Void, String[]>(TCCaseFBActivity.this, true) {

            @Override
            protected String[] doInBackground(String... params) {

                // 0.FilterCriteria;  1.FormStructure;
                String[] results = new String[2];

                // 过滤条件
                String url0 = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetMaintenanceFBConfigList";

                results[0] = NetUtil.executeHttpGet(url0, "BizName", params[0]);

                // 界面结构
                String url1 = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetTableGroupMeta";

                results[1] = NetUtil.executeHttpGet(url1, "tableName", params[1], "uiGroup", params[2]);

                return results;
            }

            @Override
            protected void onSuccess(String[] results) {

                ResultData<MaintenanceFeedBack> filterResult = Utils.json2ResultDataActivity(MaintenanceFeedBack.class,
                        TCCaseFBActivity.this, results[0], "获取过滤条件失败", false);
                if (filterResult == null) return;

                ResultData<FlowNodeMeta> tableResult = Utils.json2ResultDataActivity(FlowNodeMeta.class,
                        TCCaseFBActivity.this, results[1], "获取界面结构失败", false);
                if (tableResult == null) return;

                filterCriterias = filterResult.DataList;
                mFlowNodeMeta = tableResult.getSingleData();
                //继承时必须赋值
                flowInfoPostParam.flowNodeMeta = mFlowNodeMeta;
                startCreateView();
            }

        }.mmtExecute(caseItemEntity.BusinessType, flowInfoConfig.TableName, "");
    }

    String getfilterContidionVal(String name) {
        for (FlowInfoItem flowInfoItem : flowInfoItemList) {
            for (FlowNodeMeta.TableValue tableValue : flowInfoItem.FlowNodeMeta.Values) {
                if (name.equals(tableValue.FieldName)) {
                    return tableValue.FieldValue;
                }
            }
        }
        return "是";
    }

    void startCreateView() {
        formBeanFragment = new FlowBeanFragment();

        Bundle args = new Bundle();
        args.putString("CacheSearchParam", ("userId=" + MyApplication.getInstance().getUserId() + " and key='" + caseItemEntity.CaseNo + "'"));

        args.putParcelable("GDFormBean", mFlowNodeMeta.mapToGDFormBean());
        formBeanFragment.setArguments(args);

        formBeanFragment.setFilterCriteria(filterCriterias);

        Map<String, String> filterFields = formBeanFragment.getFilterFields();
        filterFields.clear();
        filterFields.put(filterCriterias.get(0).filterConditionFiled, getfilterContidionVal(filterCriterias.get(0).filterConditionFiled));

        addFragment(formBeanFragment);
        createBottomView();
    }

    @Override
    protected void createBottomView() {
        BottomUnitView recordUnitView = new BottomUnitView(TCCaseFBActivity.this);
        recordUnitView.setContent("碰接点记录");
        recordUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(recordUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TCCaseFBActivity.this, TJPointListActivity.class);
                intent.putExtra("caseno", caseItemEntity.CaseNo);
                intent.putExtra("Title", "碰接点列表");
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(TCCaseFBActivity.this);
            }
        });

        BottomUnitView saveUnitView = new BottomUnitView(TCCaseFBActivity.this);
        saveUnitView.setContent("保存");
        saveUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(saveUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                handler.sendEmptyMessage(SAVE_CURRENT_NODE);
            }
        });

        BottomUnitView finishUnitView = new BottomUnitView(TCCaseFBActivity.this);
        finishUnitView.setContent("完成");
        finishUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(finishUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {

                OkCancelDialogFragment fragment = new OkCancelDialogFragment("是否确定完成？");
                fragment.setRightBottonText("取消");
                fragment.setLeftBottonText("完成");
                fragment.setOnLeftButtonClickListener(new OkCancelDialogFragment.OnLeftButtonClickListener() {
                    @Override
                    public void onLeftButtonClick(View view) {
                        accomplish();
                    }
                });
                fragment.show(getSupportFragmentManager(), "");
            }
        });
    }

    private void accomplish() {

        if (flowInfoConfig.NodeType == 2) {
            // 结束节点
            handler.sendEmptyMessage(GO_TO_END);
        } else {

            switch (flowInfoConfig.HandOverMode) {
                case "移交选择人":
                    handler.sendEmptyMessage(SELECT_TO_NEXT); // 移交选择人
                    break;
                case "自处理":
                    handler.sendEmptyMessage(SELECT_TO_NEXT_DEFAULT); // 自处理
                    break;
                case "移交默认人":
                    handler.sendEmptyMessage(SELECT_TO_DEFAULT_PERSON); // 移交默认人
                    break;
                case "自处理或移交选择人":
                    handler.sendEmptyMessage(SELECT_TO_NEXT); // 移交选择人
                    break;
                default:
                    handler.sendEmptyMessage(SELECT_TO_NEXT); // 默认"移交选择人"
                    break;
            }
        }

        isOver = true;
        if (formBeanFragment != null) {
            formBeanFragment.deleteCacheData(MyApplication.getInstance().getUserId(), caseItemEntity.CaseNo);
        }
    }

    boolean isOver = false;

    @Override
    protected void onPause() {
        super.onPause();

        if (!isOver && formBeanFragment != null) {
            formBeanFragment.saveCacheData(MyApplication.getInstance().getUserId(), (caseItemEntity.CaseNo));
        }
    }
}
