package com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.R;
import com.repair.zhoushan.entity.FlowInfoItem;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;
import com.repair.zhoushan.module.projectmanage.threepartconstruct.supervisereporthistory.SuperviseReportHistoryListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2015/12/8.
 * 第三方施工巡视和管控流程的自定义详情页面
 */
public class CaregiverDetailActivity extends CaseDetailActivity {

    List<String> reportItemList = new ArrayList<>();

    @Override
    protected void onCreateCus() {
        super.onCreateCus();
        setCustomView(getTopView());
    }

    @Override
    protected void createBottomView() {
        creatBottomBtn();

        multFBAndAssistModule();
    }

    private View getTopView() {
        View view = LayoutInflater.from(this).inflate(R.layout.head_eventrport, null);

        TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        txtTitle.setMaxWidth(400);
        txtTitle.setTextSize(18);
        txtTitle.setEllipsize(TextUtils.TruncateAt.END);
        txtTitle.setText("工单详情");

        // 详情按钮
        TextView txtChangeType = (TextView) view.findViewById(com.mapgis.mmt.R.id.txtChangeType);
        txtChangeType.setText("监管历史");

        txtChangeType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaregiverDetailActivity.this, SuperviseReportHistoryListActivity.class);
                intent.putExtra("caseItem", caseItemEntity);
                intent.putExtra("title", "监管上报历史");
                startActivity(intent);
            }
        });

        // 返回按钮
        view.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return view;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //一定是在办箱
        if (resultCode == Activity.RESULT_OK && requestCode == 10011) {

            //日常上报后更新页面信息
            String FlowNodeMetaStr = data.getStringExtra("FlowNodeMeta");

            FlowNodeMeta FlowNodeMeta = new Gson().fromJson(
                    FlowNodeMetaStr,
                    new TypeToken<FlowNodeMeta>() {
                    }.getType());

            String groupName = FlowNodeMeta.Groups.get(0).GroupName;

            for (FlowInfoItem listItem : flowInfoItemList) {
                for (final FlowNodeMeta.TableGroup group : listItem.FlowNodeMeta.Groups) {
                    if (groupName.equals(group.GroupName)) {
                        listItem.FlowNodeMeta.Values = FlowNodeMeta.Values;
                        break;
                    }
                }
            }

            createDoingViewNoBtn(flowInfoItemList,false,null);
        }

    }

    public List<String> getAllGroupNames() {
        List<String> list = new ArrayList<>();
        for (FlowInfoItem item : flowInfoItemList) {
//            if (!item.FlowInfoConfig.ViewState.equalsIgnoreCase("scan")) {
//                continue;
//            }
            // 要求只能修改事件信息，不能修改接单信息和派单信息
            if ("scan".equalsIgnoreCase(item.FlowInfoConfig.ViewState) && "上报".equals(item.FlowInfoConfig.OperType)) {
                for (FlowNodeMeta.TableGroup group : item.FlowNodeMeta.Groups) {
                    list.add(group.GroupName);
                }
                break;
            }
        }
        return list;
    }

    public void creatBottomBtn() {
        try {
            reportItemList.addAll(getAllGroupNames());

            BottomUnitView reportUnitView = new BottomUnitView(this);
            reportUnitView.setContent("信息上报");
            addBottomUnitView(reportUnitView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ListDialogFragment fragment = new ListDialogFragment("信息上报", reportItemList
                    );
                    fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int arg2, String value) {
                            infoReport(value);
                        }
                    });
                    fragment.show(getSupportFragmentManager(), "");
                }
            });

            BottomUnitView flowManageUnitView = new BottomUnitView(this);
            flowManageUnitView.setContent("日常监管");
            addBottomUnitView(flowManageUnitView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(CaregiverDetailActivity.this, EveryDayCheck.class);
                    intent.putExtra("ListItemEntity", caseItemEntity);
                    intent.putExtra("TableName", "第三方施工监管表");
                    intent.putExtra("GroupName", "日常监管");
                    startActivity(intent);
                }
            });


            BottomUnitView manageUnitView = new BottomUnitView(CaregiverDetailActivity.this);
            manageUnitView.setContent("流程管理");
            addBottomUnitView(manageUnitView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CaregiverDetailActivity.this, HandActivity.class);
                    intent.putExtra("ListItemEntity", caseItemEntity);
                    intent.putExtra("FlowInfoItemList", new Gson().toJson(flowInfoItemList));
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    MyApplication.getInstance().startActivityAnimation(CaregiverDetailActivity.this);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void infoReport(String groupName) {

        Intent intent = new Intent(CaregiverDetailActivity.this, CaregiverDetailInfoReportAcitvity.class);
        intent.putExtra("ListItemEntity", caseItemEntity);
        intent.putExtra("FlowInfoItem", new Gson().toJson(setValue(groupName)));
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent, 10011);
    }

    public FlowNodeMeta setValue(String groupName) {
        FlowNodeMeta flowNodeMeta = new FlowNodeMeta();
        for (FlowInfoItem listItem : flowInfoItemList) {
            for (final FlowNodeMeta.TableGroup group : listItem.FlowNodeMeta.Groups) {
                if (groupName.equals(group.GroupName)) {
                    //data.get(0).Values=listItem.FlowNodeMeta.Values;
                    flowNodeMeta.Values = listItem.FlowNodeMeta.Values;
                    flowNodeMeta.Groups = new ArrayList<FlowNodeMeta.TableGroup>() {{
                        add(group);
                    }};
                    break;
                }
            }
        }
        return flowNodeMeta;
    }

}
