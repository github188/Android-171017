package com.repair.zhoushan.module.casemanage.casehandover;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.entity.CaseInfo;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.FlowInfoItem;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.HandoverUserFragment;
import com.mapgis.mmt.global.OnResultListener;
import com.repair.zhoushan.module.casemanage.casedetail.CaseDetailActivity;
import com.repair.zhoushan.module.casemanage.casedetail.FetchCaseDetailTask;
import com.repair.zhoushan.module.casemanage.casedetail.GetHandoverUsersTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class CaseHandoverActivity extends BaseActivity {

    // 移交（需选择承办人）
    protected final int SELECT_TO_NEXT = 1;

    // 自处理
    protected final int SELECT_TO_NEXT_DEFAULT = 2;

    protected final int HANDOVER_TO_NEXT = 3;
    // 结案
    protected final int GO_TO_END = 4;
    // 保存
    protected final int SAVE_CURRENT_NODE = 5;
    // 获取工单详情
    protected final int GET_DETAIL_FORM = 6;
    // 移交默认人
    protected final int SELECT_TO_DEFAULT_PERSON = 7;
    // 提前结案关单
    protected final int JUMP_TO_END = 8;

    private int currentInvokeEvent = 0;

    // 当前工单的基本信息
    protected CaseItem caseItemEntity;

    // 服务总是一个流程的多个节点一起返回
    protected List<FlowInfoItem> flowInfoItemList;

    // 当前节点的表单信息，用于构造界面
    protected FlowInfoItem flowInfoItem;

    // 实体界面
    protected FlowBeanFragment formBeanFragment;

    // 上报信息体
    protected final FlowInfoPostParam flowInfoPostParam = new FlowInfoPostParam();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        onCreateCus();
    }

    protected void onCreateCus() {
        // 界面有两个入口：1.在办箱列表页面  2.工单详情页面
        caseItemEntity = getIntent().getParcelableExtra("ListItemEntity");

        if (getIntent().hasExtra("FlowInfoItemList")) {
            String flowInfoItemListStr = getIntent().getStringExtra("FlowInfoItemList");
            if (!BaseClassUtil.isNullOrEmptyString(flowInfoItemListStr)) {
                flowInfoItemList = new Gson().fromJson(flowInfoItemListStr, new TypeToken<List<FlowInfoItem>>() {
                }.getType());
            }
        }

        if (flowInfoItemList == null || flowInfoItemList.size() == 0) {
            handler.sendEmptyMessage(GET_DETAIL_FORM);
        } else {
            createView();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void createView() {

        if (flowInfoItem == null || flowInfoItem.FlowNodeMeta == null) {
            for (FlowInfoItem flowInfoItem : flowInfoItemList) {
                if (flowInfoItem.FlowInfoConfig.ViewState.equalsIgnoreCase("edit")) {
                    this.flowInfoItem = flowInfoItem;
                    break;
                }
            }
        }

        if (flowInfoItem == null) {
            showErrorMsg("数据错误");
            return;
        }

        flowInfoPostParam.flowNodeMeta = flowInfoItem.FlowNodeMeta;

        getBaseTextView().setText(flowInfoItem.FlowInfoConfig.NodeName);

        // 将Fragment显示在界面上
        formBeanFragment = new FlowBeanFragment();

        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", flowInfoItem.FlowNodeMeta.mapToGDFormBean());
        formBeanFragment.setArguments(args);

        formBeanFragment.setFragmentFileRelativePath(caseItemEntity.CaseNo); // caseItemEntity.EventCode
        formBeanFragment.setCls(CaseHandoverActivity.class);
        formBeanFragment.setAddEnable(true);

        addFragment(formBeanFragment);

        createBottomView();
    }

    protected void createBottomView() {
        createBottomView(false);
    }

    protected void createBottomView(final boolean needConfirm) {

        // 保存
        createSaveBtn();

        // "自处理或移交选择人"，需要同时有自处理和移交选择人两种方式供选择
        createSelfHandleBtn();

        // 移交
        createHandoverBtn(needConfirm);

        // 提前结案关单
        if ("办理关单".equals(flowInfoItem.FlowInfoConfig.OperType)) {
            addBottomUnitView("结案关单", true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.sendEmptyMessage(JUMP_TO_END); // 结案关单
                }
            });
        }

    }

    protected void createSaveBtn() {
        BottomUnitView backUnitView = new BottomUnitView(CaseHandoverActivity.this);
        backUnitView.setContent("保存");
        backUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(backUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSaveAction();
            }
        });
    }

    protected void createSelfHandleBtn() {
        if (flowInfoItem.FlowInfoConfig.HandOverMode.equals("自处理或移交选择人")) {
            BottomUnitView handleUnitView = new BottomUnitView(CaseHandoverActivity.this);
            handleUnitView.setContent("自处理");
            handleUnitView.setImageResource(R.drawable.handoverform_report);
            addBottomUnitView(handleUnitView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.sendEmptyMessage(SELECT_TO_NEXT_DEFAULT); // 自处理
                }
            });
        }
    }

    protected void createHandoverBtn(final boolean needConfirm) {
        BottomUnitView manageUnitView = new BottomUnitView(CaseHandoverActivity.this);
        // manageUnitView.setContent(flowInfoItem.NodeName);
        manageUnitView.setContent("移交");
        manageUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (needConfirm) {

                    OkCancelDialogFragment fragment = new OkCancelDialogFragment("是否确定移交？");
                    fragment.setRightBottonText("取消");
                    fragment.setLeftBottonText("移交");
                    fragment.setOnLeftButtonClickListener(new OkCancelDialogFragment.OnLeftButtonClickListener() {
                        @Override
                        public void onLeftButtonClick(View view) {
                            performHandoverAction();
                        }
                    });
                    fragment.show(getSupportFragmentManager(), "");

                } else {
                    performHandoverAction();
                }
            }
        });
    }

    protected void performSaveAction() {
        handler.sendEmptyMessage(SAVE_CURRENT_NODE);
    }

    protected void performHandoverAction() {
        handover();
    }

    private void handover() {

        if (flowInfoItem.FlowInfoConfig.NodeType == 2) {
            // 结束节点
            handler.sendEmptyMessage(GO_TO_END);
        } else {

            switch (flowInfoItem.FlowInfoConfig.HandOverMode) {
                case "移交选择人":
                case "跨站移交":
                case "自处理或移交选择人":
                case "处理站点移交":
                case "本人站点移交":
                    handler.sendEmptyMessage(SELECT_TO_NEXT); // 移交选择人(需要获取人员树选人)
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
    }

    protected Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            try {

                currentInvokeEvent = msg.what;

                switch (msg.what) {

                    case GET_DETAIL_FORM:
                        getDetailForm(caseItemEntity.EventCode, caseItemEntity.CaseNo, caseItemEntity.FlowName, caseItemEntity.ActiveName);
                        break;

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
                        // 自处理：首节点分派或者上报的时候 caseInfo 中的 Undertakeman传自己的ID即可，
                        // 不需要下一个节点的ID（服务已处理）,但是中间节点还是需要传入下一个节点ID(保持原样).
                        if (flowInfoItem.FlowInfoConfig.NodeType == 1) {
                            flowInfoPostParam.caseInfo.Undertakeman = String.valueOf(MyApplication.getInstance().getUserId());
                        } else {
                            flowInfoPostParam.caseInfo.Undertakeman = caseItemEntity.NextNodeID + "/" + MyApplication.getInstance().getUserId();
                        }
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
                        // 移交默认人 Undertakeman传空，只会移交给自己站点下面的人
                        flowInfoPostParam.caseInfo = caseItemEntity.mapToCaseInfo();
                        reportEvent();
                        break;

                    case GO_TO_END:
                        flowInfoPostParam.caseInfo = caseItemEntity.mapToCaseInfo();
                        reportEvent();
                        break;

                    case JUMP_TO_END:
                        flowInfoPostParam.caseInfo = caseItemEntity.mapToCaseInfo();
                        flowInfoPostParam.caseInfo.OperType = flowInfoItem.FlowInfoConfig.OperType;
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

    private void getHandoverUsersTree(String handMode) {
        CaseInfo ci = this.caseItemEntity.mapToCaseInfo();
        switch (handMode) {
            case "移交选择人":
                break;
            case "跨站移交":
                ci.Station = "";
                break;
            case "处理站点移交":
                ci.Station = "";
                boolean hasStationConfig = false;
                // 读取界面上选中的处理站点(如果存在)
                if (formBeanFragment != null) {
                    List<FeedItem> feedbackItems
                            = formBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);
                    if (feedbackItems != null) {
                        for (FeedItem feedItem : feedbackItems) {
                            if ("处理站点".equals(feedItem.Name)) {
                                hasStationConfig = true;
                                ci.Station = feedItem.Value;
                                break;
                            }
                        }
                    }
                }
                if (!hasStationConfig) {
                    Toast.makeText(this, "流程节点信息，请配置处理站点字段", Toast.LENGTH_LONG).show();
                    return;
                }
                break;
            case "本人站点移交":
                List<String> stations = MyApplication.getInstance().getUserBean().getBelongStation();
                ci.Station = BaseClassUtil.listToString(stations);
                break;
        }

        final String url = getHandoverUsersUrl(ci);

        new GetHandoverUsersTask(this, true, new MmtBaseTask.OnWxyhTaskListener<Node>() {
            @Override
            public void doAfter(Node node) {
                if (node != null) {
                    HandoverUserFragment fragment = new HandoverUserFragment(handler, HANDOVER_TO_NEXT, node);
                    fragment.setIsContainNodeId(true);
                    fragment.show(getSupportFragmentManager(), "");
                } else {
                    Toast.makeText(CaseHandoverActivity.this, "获取移交人员失败", Toast.LENGTH_SHORT).show();
                }
            }
        }).mmtExecute(new Gson().toJson(ci), url);
    }

    protected String getHandoverUsersUrl(CaseInfo ci) {

        // Special case for changzhou PDA update
        if ("常州供水事件上报表".equals(ci.EventMainTable)
                && ("维修养护".equals(ci.FlowName) || "抢修管理".equals(ci.FlowName))
                && "接单".equals(ci.NodeName)) {
            return ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CZ/REST/CaseManageREST.svc/WorkFlow/GetHandoverTreeForMobile";
        }
        return GetHandoverUsersTask.DEFAULT_URL;
    }

    /**
     * 获取详情配置表单信息
     */
    protected void getDetailForm(String eventCode, String caseNo, String flowName, String nodeName) {

        new FetchCaseDetailTask(CaseHandoverActivity.this, handler, false).executeOnExecutor(MyApplication.executorService,
                eventCode, caseNo, flowName, nodeName);
    }

    private void reportEvent() {

        if (formBeanFragment == null) {
            return;
        }

        // 创建服务路径
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostFlowNodeData";

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
                    break;
                }
            }
        }

        // 将对信息转换为JSON字符串
        String data = new Gson().toJson(flowInfoPostParam, new TypeToken<FlowInfoPostParam>() {
        }.getType());

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
                    // 服务 PostFlowNodeData 的返回的状态码为新插入记录的 Id
                    if (result.ResultCode > 0) {
                        Toast.makeText(CaseHandoverActivity.this, "上传成功", Toast.LENGTH_SHORT).show();

                        success();
                    } else {
                        Toast.makeText(CaseHandoverActivity.this, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }

    private void success() {

        Toast.makeText(CaseHandoverActivity.this, "移交成功", Toast.LENGTH_SHORT).show();

        // 如移交方式是"自处理"，则跳转至详情页面
        if (currentInvokeEvent == SELECT_TO_NEXT_DEFAULT) {

//            Intent intentList = new Intent(CaseHandoverActivity.this, MyCaseListActivity.class);
//            intentList.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//            intentList.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intentList);
//
//            Intent intentDetail = new Intent(CaseHandoverActivity.this, CaseDetailActivity.class);
//            intentDetail.putExtra("CaseItemCaseNo", caseItemEntity.CaseNo);
//            intentDetail.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//            intentDetail.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intentDetail);
            AppManager.finishActivity();
            backByReorder(true);

            Intent intentDetail = new Intent(CaseHandoverActivity.this, CaseDetailActivity.class);
            intentDetail.putExtra("CaseItemCaseNo", caseItemEntity.CaseNo);
            intentDetail.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intentDetail.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentDetail);

        } else {
//            // 成功后跳转至列表页面
//            Intent intent = new Intent(CaseHandoverActivity.this, MyCaseListActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            intent.addCategory(Constants.CATEGORY_BACK_TO_LIST);
//            startActivity(intent);
            AppManager.finishActivity();

            backByReorder(true);
        }

        // this.finish();
    }

    private void saveEvent() {

        if (formBeanFragment == null) {
            return;
        }

        flowInfoPostParam.caseInfo = caseItemEntity.mapToCaseInfo();

        // 创建服务路径
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SaveFlowNodeData";

        List<FeedItem> feedbackItems
                = formBeanFragment.getFeedbackItems(ReportInBackEntity.SAVING);

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
        String data = new Gson().toJson(flowInfoPostParam, new TypeToken<FlowInfoPostParam>() {
        }.getType());

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
                    if (onSavedResultListener != null) {
                        if (result.ResultCode == 200) {
                            onSavedResultListener.onSuccess(null);
                        } else {
                            onSavedResultListener.onFailed("上报失败:" + result.ResultMessage);
                        }
                    } else {
                        if (result.ResultCode == 200) {
                            Toast.makeText(CaseHandoverActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                            saveSuccess();
                        } else {
                            Toast.makeText(CaseHandoverActivity.this, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }

    private void saveSuccess() {
        Toast.makeText(CaseHandoverActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
        // Notify CaseDetailActivity the form data is dirty
        setResult(Activity.RESULT_OK);
    }

    private OnResultListener<Void> onSavedResultListener;
    public void setOnSavedResultListener(OnResultListener<Void> onSavedResultListener) {
        this.onSavedResultListener = onSavedResultListener;
    }
}
