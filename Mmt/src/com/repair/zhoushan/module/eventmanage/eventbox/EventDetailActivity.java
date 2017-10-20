package com.repair.zhoushan.module.eventmanage.eventbox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.DealFlowInfo;
import com.repair.zhoushan.entity.EventFlowItem;
import com.repair.zhoushan.entity.EventInfoPostParam;
import com.repair.zhoushan.entity.EventItem;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventDetailActivity extends BaseActivity {

    // 0:调度箱模式(默认)  1：领单箱模式
    private int mMode;

    private EventItem mEventItem;
    private DealFlowInfo mDealFlowInfo;

    private EventInfoPostParam mEventInfoPostParam = new EventInfoPostParam();
    private FlowInfoPostParam mFlowInfoPostParam = mEventInfoPostParam.DataParam;
    private List<FeedItem> feedbackItems = null;

    private FlowBeanFragment mFlowBeanFragment;

    private String[] availableFlows;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent argIntent = getIntent();
        mMode = argIntent.getIntExtra("MODE", EventListActivity.Mode.DISPATCH);
        mEventItem = argIntent.getParcelableExtra("ListItemEntity");
        if (mEventItem == null) {
            this.showErrorMsg("未获取到参数");
            return;
        }

        this.userId = MyApplication.getInstance().getUserId();

        getBaseTextView().setText("事件详情");

        initView();
    }

    private void initView() {

        new MmtBaseTask<String, Void, String>(this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {
            @Override
            public void doAfter(String resultJson) {

                ResultData<DealFlowInfo> newData = Utils.json2ResultDataActivity(DealFlowInfo.class,
                        EventDetailActivity.this, resultJson, "获取事件详情失败", false);
                if (newData == null) return;

                mDealFlowInfo = newData.getSingleData();
                mFlowInfoPostParam.flowNodeMeta = mDealFlowInfo.EventInfo;
                createView();
            }
        }) {
            @Override
            protected String doInBackground(String... params) {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetEventMetaData?userID="
                        + userID;
                try {

                    return NetUtil.executeHttpPost(url, params[0], "Content-Type", "application/json; charset=utf-8");

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }.mmtExecute(new Gson().toJson(mEventItem));
    }

    private void createView() {

        this.mFlowBeanFragment = new FlowBeanFragment();

        // "调度箱", "领单箱"存在可编辑字段
        this.availableFlows = new String[mDealFlowInfo.FlowInfoConfig.size()];
        for (int i = 0; i < availableFlows.length; i++) {
            availableFlows[i] = mDealFlowInfo.FlowInfoConfig.get(i).FlowName;
        }

        // 除可编辑字段外，其他均设为不可编辑
        List<String> editableFieldList = null;
        if (!BaseClassUtil.isNullOrEmptyString(mDealFlowInfo.EditableFields)) {
            editableFieldList = BaseClassUtil.StringToList(mDealFlowInfo.EditableFields, ",");
        }
        for (FlowNodeMeta.TableGroup tableGroup : mDealFlowInfo.EventInfo.Groups) {
            for (FlowNodeMeta.FieldSchema fieldSchema : tableGroup.Schema) {
                if (editableFieldList != null && editableFieldList.contains(fieldSchema.FieldName)) {
                    fieldSchema.ReadOnly = 0;
                } else {
                    fieldSchema.ReadOnly = 1;
                }
            }
        }

        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", mDealFlowInfo.EventInfo.mapToGDFormBean());
        mFlowBeanFragment.setArguments(args);
        mFlowBeanFragment.setCls(EventDetailActivity.class);

        addFragment(mFlowBeanFragment);

        createBottomView();
    }

    private void createBottomView() {

        switch (mEventItem.EventState) {
            case "已处理":
                // do nothing.
                break;

            case "无效":
                addBottomRecoveryFunc();
                break;

            case "待处理":
                addBottomDisableFunc();
                addBottomCloseFunc();
                addBottomSaveFunc();
                addBottomDispatchFunc();
                break;

            case "待审核":
            case "处理中":
                // 是否允许事件多次分派，默认允许
                boolean disableMultiDispatch = MyApplication.getInstance().getConfigValue("DisableMultiDispatch", 0) != 0;
                if (disableMultiDispatch) {
                    addBottomSaveFunc();
                } else {
                    addBottomDisableFunc();
                    addBottomCloseFunc();
                    addBottomSaveFunc();
                    addBottomDispatchFunc();
                }
                break;
        }
    }

    private void checkCanShutdown() {

        new MmtBaseTask<String, Void, String>(EventDetailActivity.this, true) {

            @Override
            protected String doInBackground(String... params) {
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetEventFlowLog?eventCode="
                        + mEventItem.EventCode;
                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String jsonResult) {
                ResultData<EventFlowItem> newData = Utils.json2ResultDataToast(EventFlowItem.class, EventDetailActivity.this, jsonResult, "获取数据失败", true);
                if (newData == null) return;

                if (newData.DataList.size() == 0) {
                    shutDownEvent();
                } else {
                    boolean existNoClosed = false;
                    for (EventFlowItem item : newData.DataList) {
                        if (item.IsOver.equals("0")) {
                            existNoClosed = true;
                            break;
                        }
                    }
                    if (existNoClosed) {
                        // TODO: 2016/1/11 Show flow list and not allowed to shut down current event.
                        Toast.makeText(EventDetailActivity.this, "无法关闭: 事件存在未办结的流程!", Toast.LENGTH_LONG).show();
                    } else {
                        shutDownEvent();
                    }
                }
            }
        }.mmtExecute();
    }



    private void shutDownEvent() {

        final boolean closeReason = MyApplication.getInstance().getConfigValue("EventBox_NeedCloseReason", false);

        final View reasonView = getLayoutInflater().inflate(R.layout.maintenance_back, null);
        OkCancelDialogFragment fragment = new OkCancelDialogFragment("关闭原因", reasonView);
        if (!closeReason) {
            fragment = new OkCancelDialogFragment("是否关闭该事件？");
        }
        fragment.setLeftBottonText("取消");
        fragment.setRightBottonText("关闭");
        fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {

            @Override
            public void onRightButtonClick(View view) {
                String opinion = "";
                if (closeReason) {
                    opinion = ((EditText) reasonView.findViewById(R.id.maintenanceBackReason)).getText().toString();
                }
                new UpdateEventStateTask(EventDetailActivity.this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {
                    @Override
                    public void doAfter(String jsonResult) {
                        // 关闭事件成功后，关闭当前页并刷新列表
                        if (Utils.json2ResultToast(EventDetailActivity.this, jsonResult, "关闭事件失败")) {
                            Toast.makeText(EventDetailActivity.this, "关闭事件成功", Toast.LENGTH_SHORT);
                            setResult(Activity.RESULT_OK);
                            EventDetailActivity.this.finish();
                        }
                    }
                }).mmtExecute(mEventItem.EventMainTable, mEventItem.EventCode, "已处理", opinion);
            }
        });
        fragment.show(EventDetailActivity.this.getSupportFragmentManager(), "");
    }

    private void saveEvent() {

        if (mFlowBeanFragment == null) {
            return;
        }

        mFlowInfoPostParam.caseInfo.UserID = userId;
        mFlowInfoPostParam.caseInfo.IsCreate = Integer.parseInt(mEventItem.IsCreate);
        mFlowInfoPostParam.caseInfo.EventName = mEventItem.EventName;
        mFlowInfoPostParam.caseInfo.EventCode = mEventItem.EventCode;
        mFlowInfoPostParam.caseInfo.BizCode = mEventItem.BizCode;
        mFlowInfoPostParam.caseInfo.Station = mEventItem.DealStation;

        feedbackItems = mFlowBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);
        if (feedbackItems == null) {
            return;
        }

        // 把Feedback的值映射到Value上
        ArrayList<FlowNodeMeta.TableValue> values = new ArrayList<FlowNodeMeta.TableValue>();

        List<String> editableFieldList = BaseClassUtil.StringToList(mDealFlowInfo.EditableFields, ",");
        for (FlowNodeMeta.TableValue tableValue : mDealFlowInfo.EventInfo.Values) {
            if (editableFieldList.contains(tableValue.FieldName)) {
                values.add(tableValue);
            }
        }

        for (FlowNodeMeta.TableValue value : values) {
            for (FeedItem item : feedbackItems) {
                if (value.FieldName.equals(item.Name)) {
                    value.FieldValue = item.Value;
                }
            }
        }

        mFlowInfoPostParam.flowNodeMeta.Values = values;

        mEventInfoPostParam.DataParam = mFlowInfoPostParam;
        mEventInfoPostParam.BizCode = mEventItem.BizCode;
        mEventInfoPostParam.EventName = mEventItem.EventName;
        mEventInfoPostParam.TableName = mEventItem.EventMainTable;

        // 创建服务路径、将信息转换为JSON字符串
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EditEventData?eventCode=" + mEventItem.EventCode;

        String reportData = new Gson().toJson(mEventInfoPostParam, new TypeToken<EventInfoPostParam>() {
        }.getType());

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                reportData,
                userId,
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                mEventItem.EventName,
                mFlowBeanFragment.getAbsolutePaths(),
                mFlowBeanFragment.getRelativePaths());

        new EventReportTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {

            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {
                        Toast.makeText(EventDetailActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EventDetailActivity.this, "保存失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 有数据处理完毕后,会到该界刷新数据
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.DEFAULT_REQUEST_CODE) {
            setResult(Activity.RESULT_OK);
        }
    }

    private void addBottomRecoveryFunc() {
        BottomUnitView recoveryUnitView = new BottomUnitView(EventDetailActivity.this);
        recoveryUnitView.setContent("恢复");
        recoveryUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(recoveryUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OkCancelDialogFragment fragment = new OkCancelDialogFragment("确定恢复该事件吗?");
                fragment.setLeftBottonText("取消");
                fragment.setRightBottonText("恢复");
                fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {

                    @Override
                    public void onRightButtonClick(View view) {
                        new UpdateEventStateTask(EventDetailActivity.this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {
                            @Override
                            public void doAfter(String jsonResult) {
                                // 关闭事件成功后，关闭当前页并刷新列表
                                if (Utils.json2ResultToast(EventDetailActivity.this, jsonResult, "恢复事件失败")) {
                                    Toast.makeText(EventDetailActivity.this, "恢复事件成功", Toast.LENGTH_SHORT);
                                    setResult(Activity.RESULT_OK);
                                    EventDetailActivity.this.finish();
                                }
                            }
                        }).mmtExecute(mEventItem.EventMainTable, mEventItem.EventCode, "待处理", "");
                    }
                });
                fragment.show(EventDetailActivity.this.getSupportFragmentManager(), "");
            }
        });
    }

    private void addBottomSaveFunc() {
        if (!BaseClassUtil.isNullOrEmptyString(mDealFlowInfo.EditableFields)
                && mMode != EventListActivity.Mode.RECEIVE) {
            addBottomUnitView("保存", false, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveEvent();
                }
            });
        }
    }

    private void addBottomDispatchFunc() {
        String handleMethodType = mMode == EventListActivity.Mode.RECEIVE ? "领单" : "分派";
        addBottomUnitView(handleMethodType, false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (availableFlows != null) {
                    ListDialogFragment listDialogFragment = new ListDialogFragment("请选择流程", availableFlows);
                    listDialogFragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int index, String value) {
                            Intent intent = new Intent(EventDetailActivity.this, EventDispatchActivity.class);
                            intent.putExtra("FlowInfoConfigItem", mDealFlowInfo.FlowInfoConfig.get(index));
                            intent.putExtra("ListItemEntity", mEventItem);
                            intent.putExtra("MODE", mMode);
                            startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
                            MyApplication.getInstance().startActivityAnimation(EventDetailActivity.this);
                        }
                    });
                    listDialogFragment.show(getSupportFragmentManager(), "");
                }
            }
        });
    }

    private void addBottomDisableFunc() {

        // 无效、关闭 按钮在领单箱中不存在
        if (mMode == EventListActivity.Mode.DISPATCH) {
            // 需填写无效原因
            addBottomUnitView("无效", false, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final View reasonView = getLayoutInflater().inflate(R.layout.maintenance_back, null);
                    final OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("无效原因", reasonView);
                    okCancelDialogFragment.setLeftBottonText("取消");
                    okCancelDialogFragment.setRightBottonText("无效");
                    okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                        @Override
                        public void onRightButtonClick(View view) {

                            String opinion = ((EditText) reasonView.findViewById(R.id.maintenanceBackReason)).getText().toString();
                            new UpdateEventStateTask(EventDetailActivity.this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {
                                @Override
                                public void doAfter(String jsonResult) {
                                    // 关闭事件成功后，关闭当前页并刷新列表
                                    if (Utils.json2ResultToast(EventDetailActivity.this, jsonResult, "操作失败")) {
                                        Toast.makeText(EventDetailActivity.this, "操作成功", Toast.LENGTH_SHORT);
                                        setResult(Activity.RESULT_OK);
                                        EventDetailActivity.this.finish();
                                    }
                                }
                            }).mmtExecute(mEventItem.EventMainTable, mEventItem.EventCode, "无效", opinion);
                        }
                    });
                    okCancelDialogFragment.setCancelable(true);
                    okCancelDialogFragment.show(getSupportFragmentManager(), "1");
                }
            });
        }
    }

    private void addBottomCloseFunc() {
        // 无效、关闭 按钮在领单箱中不存在
        if (mMode == EventListActivity.Mode.DISPATCH) {
            addBottomUnitView("关闭", false, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 1.查询事件相关流程  2.存在 IsOver为0的流程则不允许关闭，否则调用关闭服务关闭
                    checkCanShutdown();
                }
            });
        }
    }

    public static final class UpdateEventStateTask extends MmtBaseTask<String, Void, String> {

        public UpdateEventStateTask(Context context, boolean showLoading, OnWxyhTaskListener<String> listener) {
            super(context, showLoading, listener);
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EventManage/EditEventState")
                        .append("?eventMainTable=").append(params[0])
                        .append("&eventCode=").append(params[1])
                        .append("&eventState=").append(params[2])
                        .append("&opinion=").append(params[3])
                        .append("&_token=").append(UUID.randomUUID().toString());

                return NetUtil.executeHttpGet(sb.toString());

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
