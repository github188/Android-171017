package com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDControl;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.entity.CaseInfo;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.FlowInfoItem;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.HandoverUserFragment;
import com.repair.zhoushan.module.casemanage.casedetail.GetHandoverUsersTask;
import com.repair.zhoushan.module.casemanage.casehandover.CaseHandoverActivity;
import com.repair.zhoushan.module.casemanage.mycase.MyCaseListActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by liuyunfan on 2015/12/11.
 * 自定义处理界面
 */
public class HandActivity extends BaseActivity {

    // 移交（需选择承办人）
    protected final int SELECT_TO_NEXT = 1;
    // 自处理
    protected final int SELECT_TO_NEXT_DEFAULT = 2;
    // 移交默认人
    public final static int SELECT_TO_DEFAULT_PERSON = 7;

    protected final int HANDOVER_TO_NEXT = 3;
    // 结案
    protected final int GO_TO_END = 4;
    // 保存
    protected final int SAVE_CURRENT_NODE = 5;


    // 当前工单的基本信息
    private CaseItem caseItemEntity;
    // 服务总是一个流程的多个节点一起返回
    private List<FlowInfoItem> flowInfoItemList;

    // 当前节点的表单信息，用于构造界面
    private FlowInfoItem flowInfoItem;

    // 实体界面
    private FlowBeanFragment formBeanFragment;

    private CaseInfo caseinfo;

    // 上报信息体
    public final FlowInfoPostParam flowInfoPostParam = new FlowInfoPostParam();

    boolean hasSave = false;
    GDFormBean gdFormBean;
    //是否全部换人view
    View changeManView;
    //是否全部换人
    boolean isChangeManAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        caseItemEntity = getIntent().getParcelableExtra("ListItemEntity");
        if (caseItemEntity == null) {
            Toast.makeText(HandActivity.this, "未获取到流程信息!", Toast.LENGTH_SHORT).show();
            return;
        }

        String flowInfoItemListStr = getIntent().getStringExtra("FlowInfoItemList");
        if (BaseClassUtil.isNullOrEmptyString(flowInfoItemListStr)) {
            Toast.makeText(HandActivity.this, "未获取到界面结构!", Toast.LENGTH_SHORT).show();
            return;
        }
        flowInfoItemList = new Gson().fromJson(flowInfoItemListStr, new TypeToken<List<FlowInfoItem>>() {
        }.getType());
        if (flowInfoItemList == null) {
            Toast.makeText(HandActivity.this, "界面结构解析失败!", Toast.LENGTH_SHORT).show();
            return;
        }

        setValue("申请关单");
        createView();
    }

    public void createView() {

        if (flowInfoItem == null || flowInfoItem.FlowNodeMeta == null) {
            for (FlowInfoItem flowInfoItem : flowInfoItemList) {
                if (flowInfoItem.FlowInfoConfig.ViewState.equalsIgnoreCase("edit")) {
                    this.flowInfoItem = flowInfoItem;
                }
            }
        }

        flowInfoPostParam.flowNodeMeta = flowInfoItem.FlowNodeMeta;

        // 将Fragment显示在界面上
        gdFormBean = flowInfoItem.FlowNodeMeta.mapToGDFormBean();
        formBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", gdFormBean);
        formBeanFragment.setArguments(args);

        formBeanFragment.setCls(CaseHandoverActivity.class);
        formBeanFragment.setFragmentFileRelativePath(caseItemEntity.CaseNo); // caseItemEntity.EventCode
        formBeanFragment.setAddEnable(true);

        addFragment(formBeanFragment);

        createBottomView();


        // 对申请换人特殊处理（选择申请换人时，允许选择全部换人）
        formBeanFragment.setBeanFragmentOnCreate(new FlowBeanFragment.BeanFragmentOnCreate() {
            @Override
            public void onCreated() {
                View view = formBeanFragment.findViewByName("申请关单方式");

                if (!(view instanceof ImageButtonView)) {
                    return;
                }
                ImageButtonView ImageButtonView = (ImageButtonView) view;
                initGDTypeClickLinster(gdFormBean, ImageButtonView);

                if ("申请换人".equals(ImageButtonView.getValue())) {
                    addChangeManView();
                }
            }
        });
    }

    public void addChangeManView() {
        GDControl gdControl1 = new GDControl("是否全部换人", "是否全部换人", "值选择器", "否");
        gdControl1.ConfigInfo = "否,是";
        changeManView = gdControl1.createView(HandActivity.this);
        // 给 View 设置 Id
        do {
            Integer newId = new Integer((int) ((Math.random() * 9 + 1) * 1000000));
            if (!formBeanFragment.controlIds.containsValue(newId)) {
                changeManView.setId(newId);
                formBeanFragment.controlIds.put(gdControl1.Name, newId);
                break;
            }
        } while (true);

        formBeanFragment.getEventReportMainForm().addView(changeManView);

    }

    public void initGDTypeClickLinster(GDFormBean gdFormBean, final ImageButtonView imageButtonView) {

        final GDControl gdControl = (GDControl) imageButtonView.getTag();

        final List<String> values = new ArrayList<>();
        if (!BaseClassUtil.isNullOrEmptyString(gdControl.ConfigInfo)) {
            if (gdControl.ConfigInfo.contains(",")) {
                values.addAll(BaseClassUtil.StringToList(gdControl.ConfigInfo, ","));
            } else {
                values.add(gdControl.ConfigInfo);
            }
        }
        imageButtonView.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialogFragment fragment = new ListDialogFragment(gdControl.DisplayName, values);
                fragment.show(HandActivity.this.getSupportFragmentManager(), "");
                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {
                        imageButtonView.setValue(value);

                        changeManView = formBeanFragment.findViewByName("是否全部换人");
                        if (changeManView != null) {
                            formBeanFragment.getEventReportMainForm().removeView(changeManView);
                            changeManView = null;
                        }

                        if ("申请换人".equals(value)) {
                            addChangeManView();
                        }
                    }
                });
            }
        });
    }

    public void setValue(String groupName) {
        for (FlowInfoItem listItem : flowInfoItemList) {
            for (final FlowNodeMeta.TableGroup group : listItem.FlowNodeMeta.Groups) {
                if (!groupName.equals(group.GroupName)) {
                    break;
                }

                for (final FlowNodeMeta.FieldSchema filedSchema : group.Schema) {
                    if (filedSchema.FieldName.contains("关单方式") || filedSchema.Alias.contains("关单方式")) {
                        List<String> configInfos = new ArrayList<>();
                        if (!filedSchema.ConfigInfo.contains("申请升级")) {
                            filedSchema.ConfigInfo += ",申请升级";
                        }
                        String[] types = filedSchema.ConfigInfo.split(",");
                        if (types != null) {
                            for (String s : types) {
                                if (s.equals("申请降级")) {
                                    if (caseItemEntity.FlowName.contains("巡视") && caseItemEntity.ActiveName.contains("巡视")) {
                                        continue;
                                    }
                                }
                                if (s.equals("申请升级")) {
                                    if (caseItemEntity.FlowName.contains("管控") && caseItemEntity.ActiveName.contains("管控")) {
                                        continue;
                                    }
                                }
                                configInfos.add(s);
                            }
                        }

                        filedSchema.ConfigInfo = TextUtils.join(",", configInfos);

                        break;

                    }
                }
            }
        }
    }

    private void createBottomView() {

        clearAllBottomUnitView();

        BottomUnitView backUnitView = new BottomUnitView(HandActivity.this);
        backUnitView.setContent("保存");
        backUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(backUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEvent();
            }
        });

        BottomUnitView manageUnitView = new BottomUnitView(HandActivity.this);
        // manageUnitView.setContent(flowInfoItem.NodeName);
        manageUnitView.setContent("提交");
        manageUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OkCancelDialogFragment fragment = new OkCancelDialogFragment("是否确定提交？");
                fragment.setRightBottonText("取消");
                fragment.setLeftBottonText("提交");
                fragment.setOnLeftButtonClickListener(new OkCancelDialogFragment.OnLeftButtonClickListener() {
                    @Override
                    public void onLeftButtonClick(View view) {
                        submit();
                    }
                });
                fragment.show(getSupportFragmentManager(), "");
            }
        });
    }

    private void submit() {

        if (hasSave) {
            //升级只改状态，其他的走流程
            if (caseinfo == null || BaseClassUtil.isNullOrEmptyString(caseinfo.UpdateEvent) || caseinfo.UpdateEvent.contains("升级")) {
                UpdateState();
            } else {

                if (flowInfoItem.FlowInfoConfig.NodeType == 2) {
                    // 结束节点
                    handler.sendEmptyMessage(GO_TO_END);
                    return;
                }

                switch (flowInfoItem.FlowInfoConfig.HandOverMode) {
                    case "移交选择人":
                    case "跨站移交":
                    case "自处理或移交选择人":
                    case "处理站点移交":
                        handler.sendEmptyMessage(SELECT_TO_NEXT); // 移交选择人
                        break;
                    case "自处理":
                        handler.sendEmptyMessage(SELECT_TO_NEXT_DEFAULT); // 自处理
                        break;
                    case "移交默认人":
                        handler.sendEmptyMessage(SELECT_TO_DEFAULT_PERSON); // 移交默认人
                        break;
                    default:
                        handler.sendEmptyMessage(SELECT_TO_NEXT); // 默认"移交选择人"
                        break;
                }
            }
        } else {
            Toast.makeText(HandActivity.this, "请先保存再提交", Toast.LENGTH_SHORT).show();
        }
    }


    protected Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {

                    case MaintenanceConstant.SERVER_GET_DETAIL_SUCCESS:
                        // 将Fragment显示在界面上
                        flowInfoItemList = (ArrayList<FlowInfoItem>) msg.obj;

//                        // 利用全局Application暂存数据，不用序列化是因为该对象过于复杂难于序列化，而后在进入工单列表时候清空
//                        // 用于在工单详情和工单处理页面传递FlowInfo页面结构，避免重复访问Server
//                        MyApplication.getInstance().putConfigValue("FlowInfoFormEntity", flowInfoItemList);
                        createView();
                        break;

                    case SELECT_TO_NEXT:
                        getHandoverUsersTree(flowInfoItem.FlowInfoConfig.HandOverMode);
                        break;

                    case SELECT_TO_NEXT_DEFAULT:
                        flowInfoPostParam.caseInfo = caseItemEntity.mapToCaseInfo();
                        flowInfoPostParam.caseInfo.Direction = 1;
                        flowInfoPostParam.caseInfo.Undertakeman = caseItemEntity.NextNodeID + "/" + MyApplication.getInstance().getUserId();
                        flowInfoPostParam.caseInfo.Opinion = "";
                        reportEvent();
                        break;

                    case HANDOVER_TO_NEXT:
                        flowInfoPostParam.caseInfo = caseItemEntity.mapToCaseInfo();
                        flowInfoPostParam.caseInfo.Direction = 1;
                        flowInfoPostParam.caseInfo.Undertakeman = msg.getData().getString("undertakeman");
                        flowInfoPostParam.caseInfo.Opinion = msg.getData().getString("option");
                        reportEvent();
                        break;
                    case SELECT_TO_DEFAULT_PERSON:
                    case GO_TO_END:
                        flowInfoPostParam.caseInfo = caseItemEntity.mapToCaseInfo();
                        reportEvent();
                        break;

                    case SAVE_CURRENT_NODE:
                        saveEvent();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void reportEvent() {

        if (formBeanFragment == null) {
            return;
        }


        List<FeedItem> feedbackItems
                = formBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

        if (feedbackItems == null) {
            return;
        }
//        items.addAll(getDefaultFeedItem(items, itemEntity));

        // 把Feedback的值映射到Value上
        ArrayList<FlowNodeMeta.TableValue> values = flowInfoPostParam.flowNodeMeta.Values;
        for (FlowNodeMeta.TableValue value : values) {
            for (FeedItem item : feedbackItems) {
                if (value.FieldName.equals(item.Name)) {
                    value.FieldValue = item.Value;
                }
            }
        }
        // 将对信息转换为JSON字符串
        final String data = new Gson().toJson(flowInfoPostParam, new TypeToken<FlowInfoPostParam>() {
        }.getType());
        // 创建服务路径
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostFlowNodeData";

        if (isChangeManAll) {
            uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostAllFlowNodeDataForChangeMan";

        }
        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                data,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                this.caseItemEntity.ActiveName,
                formBeanFragment.getAbsolutePaths(),
                formBeanFragment.getRelativePaths());

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {
                        Toast.makeText(HandActivity.this, "上传成功!", Toast.LENGTH_SHORT).show();

                        success();
                    } else {
                        Toast.makeText(HandActivity.this, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }

    public void getHandoverUsersTree(String handOverMode) {
        CaseInfo ci = this.caseItemEntity.mapToCaseInfo();
        switch (handOverMode) {
            case "移交选择人": {
                break;
            }
            case "跨站移交": {
                ci.Station = "";
            }
            case "处理站点移交": {
                //逻辑未知
                break;
            }
            default: {
            }
        }

        new GetHandoverUsersTask(this, true, new MmtBaseTask.OnWxyhTaskListener<Node>() {
            @Override
            public void doAfter(Node node) {
                if (node != null) {
                    HandoverUserFragment fragment = new HandoverUserFragment(handler, HANDOVER_TO_NEXT, node);
                    fragment.setIsContainNodeId(true);
                    fragment.show(getSupportFragmentManager(), "");
                } else {
                    Toast.makeText(HandActivity.this, "获取移交人员失败", Toast.LENGTH_SHORT).show();
                }
            }
        }).mmtExecute(new Gson().toJson(ci));
    }

    public void saveEvent() {

        if (formBeanFragment == null) {
            return;
        }
        caseinfo = caseItemEntity.mapToCaseInfo();
        flowInfoPostParam.caseInfo = caseinfo;


        List<FeedItem> feedbackItems
                = formBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

        if (feedbackItems == null) {
            Toast.makeText(HandActivity.this, "界面信息获取失败", Toast.LENGTH_SHORT).show();
            return;
        }
//        items.addAll(getDefaultFeedItem(items, itemEntity));

        // 把Feedback的值映射到Value上
        ArrayList<FlowNodeMeta.TableValue> values = flowInfoPostParam.flowNodeMeta.Values;
        for (FlowNodeMeta.TableValue value : values) {
            for (FeedItem item : feedbackItems) {
                if (item.Name.equals("是否全部换人")) {
                    continue;
                }
                if (value.FieldName.equals(item.Name)) {
                    value.FieldValue = item.Value;
                }
                if (item.Name.contains("关单方式")) {
                    caseinfo.UpdateEvent = item.Value;
                } else if (item.Name.contains("原因")) {
                    caseinfo.Opinion = item.Value;
                }
            }
        }

        // 将对信息转换为JSON字符串
        final String data = new Gson().toJson(flowInfoPostParam, new TypeToken<FlowInfoPostParam>() {
        }.getType());

        boolean changeManAll = false;

        if (changeManView instanceof ImageButtonView) {
            ImageButtonView view = (ImageButtonView) changeManView;
            if (caseinfo.UpdateEvent.equals("申请换人") && view.getValue().equals("是")) {
                changeManAll = true;
            }
        }
        if (changeManAll) {
            //将手上的第三方施工全部换人
            OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("确定全部换人");
            okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                @Override
                public void onRightButtonClick(View view) {
                    //    SaveALLFlowNodeDataForChangeMan
                    // 创建服务路径
                    final String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SaveALLFlowNodeDataForChangeMan";

                    final ReportInBackEntity entity = new ReportInBackEntity(
                            data,
                            MyApplication.getInstance().getUserId(),
                            ReportInBackEntity.REPORTING,
                            uri,
                            UUID.randomUUID().toString(),
                            HandActivity.this.caseItemEntity.ActiveName,
                            formBeanFragment.getAbsolutePaths(),
                            formBeanFragment.getRelativePaths());

                    new EventReportTask(HandActivity.this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
                        @Override
                        public void doAfter(ResultData<Integer> result) {
                            try {
                                if (result.ResultCode == 200) {
                                    Toast.makeText(HandActivity.this, "上传成功!", Toast.LENGTH_SHORT).show();
                                    isChangeManAll = true;
                                    saveSuccess();
                                } else {
                                    Toast.makeText(HandActivity.this, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }).mmtExecute(entity);
                }
            });
            okCancelDialogFragment.show(this.getSupportFragmentManager(), "");
        } else {
            // 创建服务路径
            final String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SaveFlowNodeData";

            // 将所有信息封装成后台上传的数据模型
            final ReportInBackEntity entity = new ReportInBackEntity(
                    data,
                    MyApplication.getInstance().getUserId(),
                    ReportInBackEntity.REPORTING,
                    uri,
                    UUID.randomUUID().toString(),
                    this.caseItemEntity.ActiveName,
                    formBeanFragment.getAbsolutePaths(),
                    formBeanFragment.getRelativePaths());
            new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
                @Override
                public void doAfter(ResultData<Integer> result) {
                    try {
                        if (result.ResultCode == 200) {
                            Toast.makeText(HandActivity.this, "上传成功!", Toast.LENGTH_SHORT).show();

                            saveSuccess();
                        } else {
                            Toast.makeText(HandActivity.this, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).mmtExecute(entity);
        }
    }

    private void saveSuccess() {
        Toast.makeText(HandActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
        hasSave = true;

    }

    private void UpdateState() {
        if (caseinfo == null || BaseClassUtil.isNullOrEmptyString(caseinfo.UpdateEvent) || caseinfo.UpdateEvent.contains("升级")) {
            caseinfo = caseItemEntity.mapToCaseInfo();
            flowInfoPostParam.caseInfo = caseinfo;

            List<FeedItem> feedbackItems
                    = formBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

            if (feedbackItems == null) {
                Toast.makeText(HandActivity.this, "界面信息获取失败", Toast.LENGTH_SHORT).show();
                return;
            }
            // 把Feedback的值映射到Value上
            ArrayList<FlowNodeMeta.TableValue> values = flowInfoPostParam.flowNodeMeta.Values;
            for (FlowNodeMeta.TableValue value : values) {
                for (FeedItem item : feedbackItems) {
                    if (value.FieldName.equals(item.Name)) {
                        value.FieldValue = item.Value;
                    }
                    if (item.Name.contains("关单方式")) {
                        caseinfo.UpdateEvent = item.Value;
                    } else if (item.Name.contains("原因")) {
                        caseinfo.Opinion = item.Value;
                    }
                }
            }
        }
        UpdateStateTask updateStateTask = new UpdateStateTask(this, caseinfo.UpdateEvent.contains("升级"), caseinfo);
        updateStateTask.executeOnExecutor(MyApplication.executorService);
        updateStateTask.setListener(new MmtBaseTask.OnWxyhTaskListener<ResultWithoutData>() {
            @Override
            public void doAfter(ResultWithoutData resultWithoutData) {

                if (resultWithoutData.ResultCode < 0) {
                    Toast.makeText(HandActivity.this, resultWithoutData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(HandActivity.this, caseinfo.UpdateEvent.contains("升级") ? "申请成功" : "更改状态成功", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void success() {
        Toast.makeText(HandActivity.this, "移交成功", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(HandActivity.this, MyCaseListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        AppManager.finishActivity();

    }
}
