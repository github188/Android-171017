package com.repair.zhoushan.module.devicecare;

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
import com.maintainproduct.entity.OnAsyncSelectorLoadFinishedListener;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.entity.EventInfoPostParam;
import com.repair.zhoushan.entity.FlowCenterData;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.HandoverUserFragment;
import com.repair.zhoushan.module.eventreport.GetHandoverUsersTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.repair.zhoushan.module.eventreport.ZSEventReportActivity.TO_DEFAULT_NEXT;
import static com.repair.zhoushan.module.eventreport.ZSEventReportActivity.TO_SELF_NEXT;

public class DeviceFBEventReportActivity extends BaseActivity {

    private FlowNodeMeta flowNodeMeta;
    private FlowCenterData mFlowCenterData;
    private final ArrayList<String> tableFields = new ArrayList<>(); // 表中所有字段集合

    private List<FeedItem> feedbackItems = null;

    // 上报信息，两种：流程上报、事件上报
    private FlowInfoPostParam flowInfoPostParam = new FlowInfoPostParam();
    private EventInfoPostParam eventInfoPostParam = new EventInfoPostParam();

    private FlowBeanFragment fragment;

    private int userId;
    private String triggerControlName;

    private boolean isCreateWorkFlow; // 标志是否创建流程

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.userId = MyApplication.getInstance().getUserId();

        Intent outerIntent = getIntent();
        this.flowNodeMeta = new Gson().fromJson(outerIntent.getStringExtra("FlowNodeMetaStr"), FlowNodeMeta.class);
        flowInfoPostParam.flowNodeMeta = flowNodeMeta;
        this.mFlowCenterData = outerIntent.getParcelableExtra("FlowCenterData");
        this.triggerControlName = outerIntent.getStringExtra("TriggerControlName");

        initData();
    }

    private void initData() {

        MmtBaseTask<String, Void, ResultData<FlowNodeMeta>> mmtBaseTask = new MmtBaseTask<String, Void, ResultData<FlowNodeMeta>>(DeviceFBEventReportActivity.this) {
            @Override
            protected ResultData<FlowNodeMeta> doInBackground(String... params) {

                ResultData<FlowNodeMeta> resultData;

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetTableGroupMeta"
                        + "?tableName=" + mFlowCenterData.TableName + "&uiGroup=";

                try {
                    String jsonResult = NetUtil.executeHttpGet(url);

                    Results<FlowNodeMeta> results = new Gson().fromJson(jsonResult, new TypeToken<Results<FlowNodeMeta>>() {
                    }.getType());
                    resultData = results.toResultData();
                } catch (Exception e) {
                    e.printStackTrace();
                    resultData = new ResultData<>();
                    resultData.ResultCode = -100;
                    resultData.ResultMessage = e.getMessage();
                }

                return resultData;
            }

            @Override
            protected void onSuccess(ResultData<FlowNodeMeta> resultData) {

                initView();
                initBottomView();

                if (resultData.ResultCode != 200) {
                    Toast.makeText(DeviceFBEventReportActivity.this, resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                FlowNodeMeta fullFlowNodeMeta = resultData.getSingleData();
                for (FlowNodeMeta.TableValue tableValue : fullFlowNodeMeta.Values) {
                    tableFields.add(tableValue.FieldName);
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    protected Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case TO_DEFAULT_NEXT:
                        flowInfoPostParam.caseInfo.Undertakeman = msg.getData().getString("undertakeman");
                        flowInfoPostParam.caseInfo.Opinion = msg.getData().getString("option");

                        reportEvent();
                        break;

                    case TO_SELF_NEXT:
                        flowInfoPostParam.caseInfo.Undertakeman = String.valueOf(userId);
                        flowInfoPostParam.caseInfo.Opinion
                                = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName + "自处理";
                        reportEvent();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void initView() {

        // actionbar
        getBaseTextView().setText(mFlowCenterData.EventName);

        // mainContent
        fragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", flowNodeMeta.mapToGDFormBean());
        fragment.setArguments(args);
        fragment.setOnAsyncSelectorLoadFinishedListener(new OnAsyncSelectorLoadFinishedListener() {

            List<String> eventTypes = new ArrayList<>();

            @Override
            public void onSingleSelectorLoaded(String fieldName, List<String> fieldValues) {
                if ("事件类型".equals(fieldName) && fieldValues != null) {
                    eventTypes.addAll(fieldValues);
                }
            }

            @Override
            public void onAllSelectorLoaded() {
                if (eventTypes.contains(triggerControlName)) {
                    ((ImageButtonView) fragment.findViewByName("事件类型")).setValue(triggerControlName);
                }
            }
        });

        addFragment(fragment);
    }

    private void initBottomView() {

        BottomUnitView reportUnitView = new BottomUnitView(DeviceFBEventReportActivity.this);
        reportUnitView.setContent("上报");
        reportUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(reportUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {

                if (fragment == null) {
                    return;
                }

                isCreateWorkFlow = (mFlowCenterData.IsCreate == 1);
                feedbackItems = fragment.getFeedbackItems(ReportInBackEntity.REPORTING);

                if (feedbackItems == null) {
                    return;
                }

                if (mFlowCenterData.IsCreate == 2) {
                    OkCancelDialogFragment dialogFragment = new OkCancelDialogFragment("是否自己处理该事件？");

                    dialogFragment.setRightBottonText("是");
                    dialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                        @Override
                        public void onRightButtonClick(View view) {
                            isCreateWorkFlow = true;
                            handler.sendEmptyMessage(TO_SELF_NEXT);
                        }
                    });
                    dialogFragment.setLeftBottonText("否");
                    dialogFragment.setOnLeftButtonClickListener(new OkCancelDialogFragment.OnLeftButtonClickListener() {
                        @Override
                        public void onLeftButtonClick(View view) {
                            isCreateWorkFlow = false;
                            handler.sendEmptyMessage(TO_DEFAULT_NEXT);
                        }
                    });
                    dialogFragment.show(getSupportFragmentManager(), "");

                } else if (mFlowCenterData.IsCreate == 1) { // 上报事件并创建流程

                    // 移交默认人 Undertakeman传空
                    if (mFlowCenterData.HandoverMode.equals("移交默认人")) {
                        handler.sendEmptyMessage(TO_DEFAULT_NEXT);

                    } else if (mFlowCenterData.HandoverMode.equals("自处理")) {
                        // 自处理,Undertakeman传入userId
                        handler.sendEmptyMessage(TO_SELF_NEXT);

                    } else {
                        // 移交选择人
                        String stationName = "";
                        for (FeedItem item : feedbackItems) {
                            if (item.Name.equals("处理站点") || item.Name.equals("站点名称")) {
                                stationName = item.Value;
                                break;
                            }
                        }
                        new GetHandoverUsersTask(DeviceFBEventReportActivity.this, true, new MmtBaseTask.OnWxyhTaskListener<Node>() {
                            @Override
                            public void doAfter(Node node) {
                                if (node == null) {
                                    Toast.makeText(DeviceFBEventReportActivity.this, "获取上报人员失败", Toast.LENGTH_SHORT).show();
                                } else {
                                    HandoverUserFragment fragment
                                            = new HandoverUserFragment(handler, TO_DEFAULT_NEXT, node);
                                    fragment.setIsContainNodeId(false);
                                    fragment.show(getSupportFragmentManager(), "");
                                }
                            }
                        }).executeOnExecutor(MyApplication.executorService, mFlowCenterData.FlowName, userId + "", stationName);
                    }

                } else { // 只上报事件不创建流程

                    handler.sendEmptyMessage(TO_DEFAULT_NEXT);
                }
            }
        });
    }

    private boolean isAddAdditionalSchema = false;

    private void reportEvent() {

        if (fragment == null) {
            return;
        }

        // Flow Report
        flowInfoPostParam.caseInfo.FlowName = mFlowCenterData.FlowName;
        flowInfoPostParam.caseInfo.NodeName = mFlowCenterData.NodeName;
        flowInfoPostParam.caseInfo.IsCreate = mFlowCenterData.IsCreate;
        flowInfoPostParam.caseInfo.EventName = mFlowCenterData.EventName;
        flowInfoPostParam.caseInfo.FieldGroup = mFlowCenterData.FieldGroup;
        flowInfoPostParam.caseInfo.BizCode = mFlowCenterData.BizCode;
        flowInfoPostParam.caseInfo.EventMainTable = mFlowCenterData.TableName;
        flowInfoPostParam.caseInfo.TableGroup = "";
        flowInfoPostParam.caseInfo.UserID = userId;

        if (feedbackItems == null) {
            return;
        }

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

        // 当前界面结构中现有的字段集合
        ArrayList<String> fieldList = new ArrayList<>();
        for (FlowNodeMeta.TableGroup tableGroup : flowInfoPostParam.flowNodeMeta.Groups) {
            for (FlowNodeMeta.FieldSchema fieldSchema : tableGroup.Schema) {
                fieldList.add(fieldSchema.FieldName);
            }
        }

        // 点击上报没成功再次点击防止重复添加
        if (!isAddAdditionalSchema) {
            isAddAdditionalSchema = true;

            Intent outerIntent = getIntent();
            String address = outerIntent.getStringExtra("Address");
            String coordinate = outerIntent.getStringExtra("Coordinate");
            String gisCode = outerIntent.getStringExtra("GISCode");
            String deviceName = outerIntent.getStringExtra("DeviceName");
            String code = outerIntent.getStringExtra("Code");

            ArrayList<FlowNodeMeta.FieldSchema> firstSchemas = flowInfoPostParam.flowNodeMeta.Groups.get(0).Schema;
            final String tableName = firstSchemas.get(0).TableName;

            if (tableFields.contains("位置") && !fieldList.contains("位置")) {
                firstSchemas.add(flowInfoPostParam.flowNodeMeta.new FieldSchema("位置", "地址", tableName));
                flowInfoPostParam.flowNodeMeta.Values.add(flowInfoPostParam.flowNodeMeta.new TableValue("位置", address));
            }
            if (tableFields.contains("坐标位置") && !fieldList.contains("坐标位置")) {
                firstSchemas.add(flowInfoPostParam.flowNodeMeta.new FieldSchema("坐标位置", "坐标控件", tableName));
                flowInfoPostParam.flowNodeMeta.Values.add(flowInfoPostParam.flowNodeMeta.new TableValue("坐标位置", coordinate));
            }
            if (tableFields.contains("GIS编号") && !fieldList.contains("GIS编号")) {
                firstSchemas.add(flowInfoPostParam.flowNodeMeta.new FieldSchema("GIS编号", "设备选择", tableName));
                flowInfoPostParam.flowNodeMeta.Values.add(flowInfoPostParam.flowNodeMeta.new TableValue("GIS编号", gisCode));
            }
            if (tableFields.contains("设备名称") && !fieldList.contains("设备名称")) {
                firstSchemas.add(flowInfoPostParam.flowNodeMeta.new FieldSchema("设备名称", "文本", tableName));
                flowInfoPostParam.flowNodeMeta.Values.add(flowInfoPostParam.flowNodeMeta.new TableValue("设备名称", deviceName));
            }
            if (tableFields.contains("编号") && !fieldList.contains("编号")) {
                firstSchemas.add(flowInfoPostParam.flowNodeMeta.new FieldSchema("编号", "文本", tableName));
                flowInfoPostParam.flowNodeMeta.Values.add(flowInfoPostParam.flowNodeMeta.new TableValue("编号", code));
            }
            if (tableFields.contains("派单方式") && !fieldList.contains("派单方式")) {
                firstSchemas.add(flowInfoPostParam.flowNodeMeta.new FieldSchema("派单方式", "文本", tableName));
                String dispatchMethod = null;
                if (!TextUtils.isEmpty(flowInfoPostParam.caseInfo.Opinion) && flowInfoPostParam.caseInfo.Opinion.contains("自处理")) {
                    dispatchMethod = "自处理";
                }
                flowInfoPostParam.flowNodeMeta.Values.add(flowInfoPostParam.flowNodeMeta.new TableValue("派单方式", dispatchMethod));
            }
        }

        // 创建服务路径、将信息转换为JSON字符串
        String uri;
        String reportData;

        // 创建流程
        if (isCreateWorkFlow) {

            uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostFlowNodeData";

            reportData = new Gson().toJson(flowInfoPostParam, new TypeToken<FlowInfoPostParam>() {
            }.getType());

        } else {
            // 不创建流程
            uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostEventData";

            // Event Report Only
            eventInfoPostParam.DataParam = flowInfoPostParam;
            eventInfoPostParam.BizCode = mFlowCenterData.BizCode;
            eventInfoPostParam.EventName = mFlowCenterData.EventName;
            eventInfoPostParam.TableName = mFlowCenterData.TableName;

            reportData = new Gson().toJson(eventInfoPostParam, new TypeToken<EventInfoPostParam>() {
            }.getType());
        }

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                reportData,
                userId,
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                mFlowCenterData.EventName,
                fragment.getAbsolutePaths(),
                fragment.getRelativePaths());

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {

            @Override
            public void doAfter(ResultData<Integer> result) {
                try {

                    if (result.ResultCode > 0) {
                        Toast.makeText(DeviceFBEventReportActivity.this, "上报保存成功", Toast.LENGTH_SHORT).show();

                        eventToTask(result.ResultCode); // 事件关联到任务

                    } else if (result.getSingleData() == 200) { // 降低要求，只要网络传输没问题就认为成功
                        Toast.makeText(DeviceFBEventReportActivity.this, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    } else {
                        OkCancelDialogFragment fragment = new OkCancelDialogFragment(
                                result.ResultMessage + "，是否保存至后台等待上传？");

                        fragment.setLeftBottonText("放弃");
                        fragment.setRightBottonText("保存");
                        fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                            @Override
                            public void onRightButtonClick(View view) {
                                entity.insert();

                                backByReorder();
                            }
                        });

                        fragment.show(getSupportFragmentManager(), "");

                        // Toast.makeText(ZSEventReportActivity.this, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);

    }

    private void eventToTask(final int eventId) {

        final String taskCode = getIntent().getStringExtra("TaskCode");
        final String bizType = getIntent().getStringExtra("BizType");

        MmtBaseTask<Void, Void, String> mmtBaseTask = new MmtBaseTask<Void, Void, String>(DeviceFBEventReportActivity.this) {
            @Override
            protected String doInBackground(Void... params) {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EventManage/RelateEventToTask?bizType="
                        + bizType + "&eventName=" + mFlowCenterData.EventName + "&eventID=" + eventId + "&taskCode=" + taskCode;
                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String jsonResult) {
//                if (Utils.json2ResultToast(DeviceFBEventReportActivity.this, jsonResult, "事件关联任务失败")) {
//                    Toast.makeText(DeviceFBEventReportActivity.this, "事件关联任务成功", Toast.LENGTH_SHORT).show();
//                }

                backByReorder();
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }
}
