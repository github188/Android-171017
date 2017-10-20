package com.repair.zhoushan.module.devicecare;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.slidingtab.SlidingTabLayout;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.repair.common.BaseTaskResults;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowCenterData;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.entity.FlowTableInfo;
import com.repair.zhoushan.entity.TableMetaData;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.devicecare.carehistory.CareHistoryListActivity;
import com.repair.zhoushan.module.devicecare.careoverview.detail.ConsumableFragment;
import com.repair.zhoushan.module.devicecare.careoverview.detail.FeedbackInfoFragment;
import com.repair.zhoushan.module.devicecare.careoverview.detail.MaterialFragment;
import com.repair.zhoushan.module.devicecare.careoverview.detail.PurchaseOrderFragment;
import com.repair.zhoushan.module.devicecare.merchantsecuritycheck.RelatedDeviceListFragment;
import com.repair.zhoushan.module.eventreport.ZSEventReportActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class TaskDetailActivity extends BaseActivity {

    public static final String MSC_RELATED_REGULATOR = "所属调压器";
    public static final String MSC_RELATED_METER = "关联表具";

    // 数组 9：1"能否停气",2"能否延期",3"容忍延期天数",4"能否退单", 5"能否添加耗材",
    // 6"能否添加材料", 7"能否编辑台账", 8"能否多次编辑台账"（1表示不可），9"能否隐患上报"

    private static final int MASK_GAS_STOPPABLE = 1 << 16; // 能否停气
    private static final int MASK_DELAY_ENABLE = 1 << 17; // 能否延期
    private static final int MASK_ROLLBACK_ENABLE = 1 << 18; // 能否退单
    private static final int MASK_CONSUMABLE_ADDABLE = 1 << 19; // 能否添加耗材
    private static final int MASK_MATERIAL_ADDABLE = 1 << 20; // 能否添加材料
    private static final int MASK_ACCOUNT_EDITABLE = 1 << 21; // 能否编辑台账
    private static final int MASK_ACCOUNT_MULTI_EDITABLE = 1 << 22; // 能否多次编辑台账
    private static final int MASK_DANGER_REPORTABLE = 1 << 23; // 能否隐患上报
    private static final int MASK_DELAY_TOLERATE_DAYS = 0x00000FFF; // 容忍延期天数

    private int innerFlags = 0;

    private ScheduleTask scheduleTask;
    private Source comeFrom;
    private FlowTableInfo flowTableInfo;

    public enum Source {
        FromMap,  // List -> Map -> Detail
        FromList, // List -> Detail
        FromCareHistory, // CareHistoryList -> Detail
        FromTempMap // List -> Detail -> Map ->Detail
    }

    private LinearLayout mainBottomView;
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    @Override
    protected void setDefaultContentView() {
        setSwipeBackEnable(false);

        this.scheduleTask = getIntent().getParcelableExtra("ListItemEntity");
        this.comeFrom = (Source) getIntent().getSerializableExtra("ComeFrom");

        setContentView(R.layout.activity_care_overview_detail);

        initView();
        initData();
    }

    private void initView() {

        // actionbar
        getBaseTextView().setText("任务详情");
        addBackBtnListener(getBaseLeftImageView());

        ImageView rightImageView = getBaseRightImageView();
        if (comeFrom == Source.FromList) {

            rightImageView.setVisibility(View.VISIBLE);
            rightImageView.setImageResource(R.drawable.navigation_locate);
            rightImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseMapCallback callback = new ShowMapPointCallback(TaskDetailActivity.this,
                            scheduleTask.Position, scheduleTask.TaskCode, "", -1);
                    MyApplication.getInstance().sendToBaseMapHandle(callback);

                    comeFrom = Source.FromTempMap;
                }
            });

        } else {
            rightImageView.setVisibility(View.GONE);
        }

        // middle content.
        this.slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        this.viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(6);

        // bottom action
        this.mainBottomView = (LinearLayout) findViewById(R.id.baseBottomView);
    }

    private MmtBaseTask<String, Void, String[]> fetchBaseInfoTask;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fetchBaseInfoTask != null) {
            fetchBaseInfoTask.cancel(true);
        }
    }

    private void initData() {

        fetchBaseInfoTask = new MmtBaseTask<String, Void, String[]>(this) {
            @Override
            protected String[] doInBackground(String... params) {

                String[] resultStrArr = new String[2];

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                        .append(userID).append("/TaskTableInfo")
                        .append("?bizName=").append(scheduleTask.BizName)
                        .append("&taskCode=").append(scheduleTask.TaskCode)
                        .append("&gisCode=").append(scheduleTask.GisCode);

                resultStrArr[0] = NetUtil.executeHttpGet(sb.toString());

                String operTypeUrl = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/FetchYangHuTaskOpers?bizName="
                        + scheduleTask.BizName;
                resultStrArr[1] = NetUtil.executeHttpGet(operTypeUrl);

                return resultStrArr;
            }

            @Override
            protected void onSuccess(String[] resultStrArr) {

                ResultData<FlowTableInfo> mData = Utils.json2ResultDataToast(FlowTableInfo.class,
                        TaskDetailActivity.this, resultStrArr[0], "获取任务详情失败", false);
                if (mData == null) return;

                ResultData<Integer> operTypeResult = new Gson().fromJson(resultStrArr[1], new TypeToken<ResultData<Integer>>() {
                }.getType());

                String defErrMsg = "获取可操作类型列表失败";
                if (operTypeResult == null) {
                    Toast.makeText(TaskDetailActivity.this, defErrMsg, Toast.LENGTH_SHORT).show();
                } else if (operTypeResult.ResultCode != 200) {
                    Toast.makeText(TaskDetailActivity.this,
                            TextUtils.isEmpty(operTypeResult.ResultMessage) ? defErrMsg : operTypeResult.ResultMessage,
                            Toast.LENGTH_LONG).show();
                } else {
                    resolveOperType(Collections.unmodifiableList(operTypeResult.DataList));
                }

                flowTableInfo = mData.getSingleData();
                createView();
            }
        };
        fetchBaseInfoTask.setCancellable(false);
        fetchBaseInfoTask.mmtExecute();

    }

    private void resolveOperType(List<Integer> operTypes) {
        innerFlags = 0;
        try {
            if (operTypes.get(0) == 1) {
                innerFlags |= MASK_GAS_STOPPABLE;
            }
            if (operTypes.get(1) == 1) {
                innerFlags |= MASK_DELAY_ENABLE;
            }
            innerFlags |= (operTypes.get(2) & MASK_DELAY_TOLERATE_DAYS);

            if (operTypes.get(3) == 1) {
                innerFlags |= MASK_ROLLBACK_ENABLE;
            }
            if (operTypes.get(4) == 1) {
                innerFlags |= MASK_CONSUMABLE_ADDABLE;
            }
            if (operTypes.get(5) == 1) {
                innerFlags |= MASK_MATERIAL_ADDABLE;
            }
            if (operTypes.get(6) == 1) {
                innerFlags |= MASK_ACCOUNT_EDITABLE;
            }
            if (operTypes.get(7) == 1) {
                innerFlags |= MASK_ACCOUNT_MULTI_EDITABLE;
            }
            if (operTypes.get(8) == 1) {
                innerFlags |= MASK_DANGER_REPORTABLE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createView() {

        if (comeFrom != Source.FromCareHistory) {
            mainBottomView.setVisibility(View.GONE);
        }
        viewPager.setAdapter(new TaskDetailActivity.ViewPagerAdapter(getSupportFragmentManager()));
        slidingTabLayout.setViewPager(viewPager);

        createBottomView();
    }

    private String getValueByName(String name) {

        String value = "";

        if (TextUtils.isEmpty(name) || flowTableInfo == null) {
            return value;
        }

        for (TableMetaData tableMetaData : flowTableInfo.TableMetaDatas) {
            value = tableMetaData.FlowNodeMeta.getValueByName(name);
        }

        return value;
    }

    private HashMap<String, String> getKeyValueMap() {
        if (flowTableInfo == null) {
            return null;
        }

        HashMap<String, String> map = new HashMap<>();

        for (TableMetaData tableMetaData : flowTableInfo.TableMetaDatas) {
            for (FlowNodeMeta.TableValue tv : tableMetaData.FlowNodeMeta.Values) {
                map.put(tv.FieldName, tv.FieldValue);
            }
        }
        return map;
    }

    private String[] getAvailableOperType() {

        List<String> availableTypeStr = new LinkedList<>();

        if ((innerFlags & MASK_GAS_STOPPABLE) == MASK_GAS_STOPPABLE) {
            availableTypeStr.add(OperType.DELAY_STOP_GAS);
        }
        if ((innerFlags & MASK_DELAY_ENABLE) == MASK_DELAY_ENABLE) {
            availableTypeStr.add(OperType.REQUEST_DELAY);
        }
        if ((innerFlags & MASK_ROLLBACK_ENABLE) == MASK_ROLLBACK_ENABLE) {
            availableTypeStr.add(OperType.ROLLBACK_CASE);
        }
        return availableTypeStr.toArray(new String[availableTypeStr.size()]);
    }

    private void createBottomView() {

        // 从养护历史界面跳转过来的不显示底部的操作按钮
        if (comeFrom == Source.FromCareHistory) {
            return;
        }

        findViewById(R.id.bottomViewContainer).setVisibility(View.VISIBLE);

        // 编辑台账
        if ((innerFlags & MASK_ACCOUNT_EDITABLE) == MASK_ACCOUNT_EDITABLE) {

            if (!getIntent().getBooleanExtra("IsDone", false) ||  // 已完成的任务
                    ((innerFlags & MASK_ACCOUNT_MULTI_EDITABLE) == MASK_ACCOUNT_MULTI_EDITABLE)) { // 允许多次编辑
                addBottomUnitView("编辑", false, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TaskDetailActivity.this, EditAccountTableActivity.class);
                        intent.putExtra("ListItemEntity", scheduleTask);
                        startActivity(intent);
                    }
                });
            }
        }

        // 停气，延期，退单（只有一个则直接显示，否则弹出列表供选择）
        final String[] availOperTypeArr = getAvailableOperType();
        if (availOperTypeArr.length == 1) {

            BottomUnitView handleUnitView = new BottomUnitView(TaskDetailActivity.this);
            handleUnitView.setContent(availOperTypeArr[0]);
            addBottomUnitView(handleUnitView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleEvent(availOperTypeArr[0]);
                }
            });

        } else if (availOperTypeArr.length > 1) {
            addBottomUnitView("操作", false, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListDialogFragment fragment = new ListDialogFragment("操作", availOperTypeArr);
                    fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int arg2, String value) {
                            handleEvent(value);
                        }
                    });
                    fragment.show(getSupportFragmentManager(), "");
                }
            });
        }

        addBottomUnitView("历史记录", false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskDetailActivity.this, CareHistoryListActivity.class);
                intent.putExtra("ScheduleTask", scheduleTask);
                // "工商户" 相关业务提供"隐患上报"功能,历史记录中要显示隐患上报的事件
                if (scheduleTask.BizName.startsWith("工商户")) {
                    intent.putExtra("HasRiskEvent", true);
                }
                startActivity(intent);
            }
        });

        if (scheduleTask.BizName.startsWith("工商户安检")) { // "工商户安检" 的特殊业务逻辑
            marchantSecurityCheck();
        }
        // else if (scheduleTask.BizName.startsWith("工商户")) {  // "工商户"相关业务提供"隐患上报"功能
        // 以前仅仅是"工商户"相关业务提供"隐患上报"功能，现在设备养护都要提供,服务可配置
        else if ((innerFlags & MASK_DANGER_REPORTABLE) == MASK_DANGER_REPORTABLE) {
            addBottomUnitView("隐患上报", false, new OnNoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    navigateToRiskReport();
                }
            });
        }

        addBottomUnitView("反馈", false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doFeedback();
            }
        });
    }

    private HashMap<String, ArrayList<MaintenanceFeedBack>> feedbackConfigs = new HashMap<>();

    private void doFeedback() {

        if (feedbackConfigs.size() > 0) {
            resolveConfigs(feedbackConfigs);
            return;
        }

        // Fetch feedback configuration
        BaseTaskResults<String, Integer, MaintenanceFeedBack> task
                = new BaseTaskResults<String, Integer, MaintenanceFeedBack>(TaskDetailActivity.this) {
            @NonNull
            @Override
            protected String getRequestUrl() throws Exception {
                return ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetMaintenanceFBConfigList?BizName="
                        + scheduleTask.BizName;
            }

            @Override
            protected void onSuccess(Results<MaintenanceFeedBack> results) {
                ResultData<MaintenanceFeedBack> resultData = results.toResultData();
                if (resultData.ResultCode != 200) {
                    Toast.makeText(TaskDetailActivity.this, resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                // Feedback configuration associated with a specified bizName cannot be empty
                if (resultData.DataList == null || resultData.DataList.size() == 0) {
                    Toast.makeText(TaskDetailActivity.this, "反馈配置错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Group by feedBackType
                for (int i = 0, length = resultData.DataList.size(); i < length; i++) {
                    final MaintenanceFeedBack item = resultData.DataList.get(i);
                    if (!feedbackConfigs.containsKey(item.feedBackType)) {
                        ArrayList<MaintenanceFeedBack> items = new ArrayList<>();
                        items.add(item);
                        feedbackConfigs.put(item.feedBackType, items);
                    } else {
                        feedbackConfigs.get(item.feedBackType).add(item);
                    }
                }
                resolveConfigs(feedbackConfigs);
            }
        };
        task.setCancellable(false);
        task.mmtExecute();
    }

    private int selectedIndex = 0;

    private void resolveConfigs(final HashMap<String, ArrayList<MaintenanceFeedBack>> configs) {
        // No feedBackType configured ("") or only a single feedbackType
        if (configs.size() == 1) {
            navigateToFeedback(configs.get(configs.keySet().toArray()[0].toString()));
        } else {
            final String[] feedbackTypes = configs.keySet().toArray(new String[configs.size()]);
            new AlertDialog.Builder(TaskDetailActivity.this, R.style.MmtBaseThemeAlertDialog)
                    .setTitle("请选择反馈类型")
                    .setSingleChoiceItems(feedbackTypes, selectedIndex, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedIndex = which;
                        }
                    })
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ArrayList<MaintenanceFeedBack> feedbackConfigs = configs.get(feedbackTypes[selectedIndex]);
                            navigateToFeedback(feedbackConfigs);
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(true)
                    .show();
        }
    }

    private void navigateToFeedback(ArrayList<MaintenanceFeedBack> feedbackConfigs) {
        Intent intent = new Intent(this, DeviceFeedbackActivity.class);
        intent.putParcelableArrayListExtra("FeedbackConfigs", feedbackConfigs);
        intent.putExtra("ListItemEntity", scheduleTask);
        intent.putExtra("KeyValueMap", getKeyValueMap());
        intent.putExtra("ComeFrom", comeFrom);
        intent.putExtra("AllowAddConsumable",
                ((innerFlags & MASK_CONSUMABLE_ADDABLE) == MASK_CONSUMABLE_ADDABLE));
        intent.putExtra("AllowAddMaterial",
                ((innerFlags & MASK_MATERIAL_ADDABLE) == MASK_MATERIAL_ADDABLE));

        startActivityForResult(intent, DeviceCareListFragment.TASK_DETAIL_REQUEST_CODE);
    }

    // 跳转流程中心中隐患上报界面
    private void navigateToRiskReport() {

        MmtBaseTask<Void, Void, FlowCenterData> mmtBaseTask = new MmtBaseTask<Void, Void, FlowCenterData>(this) {

            // 工商户相关的特有字段
            private String userName; // 用户名称
            private String userTel; // 联系电话
            private String gasLocation; // 用气地址

            // Common fields
            private String gisNo; // GIS编号 filedVal
            private String gisLayer; // 图层名称 layerName
            private String coordinate; // 坐标 position

            @Override
            protected FlowCenterData doInBackground(Void... params) {

                // 1. 从网络上获取"隐患上报"的流程中心信息
                FlowCenterData flowCenterData = null;

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/" + userID
                        + "/GetFlowCenterData?type=mobile";

                String jsonResult = NetUtil.executeHttpGet(url);

                Results<FlowCenterData> result = new Gson().fromJson(jsonResult, new TypeToken<Results<FlowCenterData>>() {
                }.getType());

                // 2. 查找流程中心中是否存在“隐患上报”流程
                if (result != null && result.getMe.size() > 0) {
                    for (FlowCenterData item : result.getMe) {
                        if ("隐患上报".equals(item.EventName)) {
                            flowCenterData = item;
                            break;
                        }
                    }
                }

                // 3. 提取当前界面的"用户名称，联系电话，用气地址"信息
                if (flowCenterData != null) {
                    for (TableMetaData tableMetaData : flowTableInfo.TableMetaDatas) {
                        for (FlowNodeMeta.TableValue tableValue : tableMetaData.FlowNodeMeta.Values) {

                            switch (tableValue.FieldName) {
                                case "用户名称":
                                    userName = tableValue.FieldValue;
                                    break;
                                case "联系电话":
                                    userTel = tableValue.FieldValue;
                                    break;
                                case "用气地址":
                                    gasLocation = tableValue.FieldValue;
                                    break;
                                case "GIS编号":
                                    gisNo = tableValue.FieldValue;
                                    break;
                                case "GIS图层":
                                    gisLayer = tableValue.FieldValue;
                                    break;
                                case "坐标位置":
                                    coordinate = tableValue.FieldValue;
                                    break;
                            }
                        }
                    }
                }

                return flowCenterData;
            }

            @Override
            protected void onSuccess(FlowCenterData flowCenterData) {
                if (flowCenterData == null) {
                    Toast.makeText(TaskDetailActivity.this, "未能获取到流程信息", Toast.LENGTH_SHORT).show();
                } else {

                    // "用户名称，联系电话，用气地址"传到上报界面
                    Intent intent = new Intent(TaskDetailActivity.this, ZSEventReportActivity.class);
                    intent.putExtra("FlowCenterData", flowCenterData);

                    intent.putExtra("UserName", userName);
                    intent.putExtra("UserTel", userTel);
                    intent.putExtra("GasLocation", gasLocation);

                    intent.putExtra("GisNo", gisNo);
                    intent.putExtra("GisLayer", gisLayer);
                    intent.putExtra("Coordinate", coordinate);

                    intent.putExtra("TaskCode", scheduleTask.TaskCode);
                    intent.putExtra("BizType", scheduleTask.BizName);
                    startActivity(intent);
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();

    }

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    private void handleEvent(final String eventTypeStr) {
        GDFormBean gdFormBean;
        Intent intent;
        switch (eventTypeStr) {

            case OperType.DELAY_STOP_GAS:
                gdFormBean = GDFormBean.generateSimpleForm(
                        new String[]{"DisplayName", "延长时间", "Name", "延长时间", "Type", "日期框", "Validate", "1"},
                        new String[]{"DisplayName", "延长原因", "Name", "延长原因", "Type", "短文本", "DisplayColSpan", "100"});
                intent = new Intent(TaskDetailActivity.this, DeviceFeedbackDialogActivity.class);
                intent.putExtra("Title", "申请延长停气");
                intent.putExtra("Tag", OperType.DELAY_STOP_GAS);
                intent.putExtra("GDFormBean", gdFormBean);
                intent.putExtra("ListItemEntity", scheduleTask);
                startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
                break;

            case OperType.REQUEST_DELAY:

                int delayTolerateDays = innerFlags & MASK_DELAY_TOLERATE_DAYS;
                if (delayTolerateDays == 0) {
                    Toast.makeText(TaskDetailActivity.this, "不可延期", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 计算最迟结束时间
                String endTimeStr = getValueByName("结束时间");
                String terminalTimeStr = "";
                Date endTime = null; // 结束时间
                Date terminalTime = null; // 最迟完成时间
                if (!TextUtils.isEmpty(endTimeStr)) {
                    try {
                        endTime = dateFormat.parse(endTimeStr);
                        terminalTime = new Date(endTime.getTime() + (24 * 60 * 60 * 1000 * delayTolerateDays));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if (terminalTime != null) {
                    terminalTimeStr = dateFormat.format(terminalTime);
                }

                gdFormBean = GDFormBean.generateSimpleForm(
                        new String[]{"DisplayName", "结束时间", "Name", "结束时间", "Type", "日期框", "IsRead", "true", "Value", endTimeStr},
                        new String[]{"DisplayName", "最迟时间", "Name", "最迟时间", "Type", "日期框", "IsRead", "true", "Value", terminalTimeStr},
                        new String[]{"DisplayName", "延期时间", "Name", "延期时间", "Type", "日期框V2", "Validate", "1"},
                        new String[]{"DisplayName", "延期原因", "Name", "延期原因", "Type", "短文本", "DisplayColSpan", "100"});
                intent = new Intent(TaskDetailActivity.this, DeviceFeedbackDialogActivity.class);
                intent.putExtra("Title", "申请延期");
                intent.putExtra("Tag", OperType.REQUEST_DELAY);
                intent.putExtra("GDFormBean", gdFormBean);
                intent.putExtra("ListItemEntity", scheduleTask);
                intent.putExtra("EndTime", endTime);
                intent.putExtra("TerminalTime", terminalTime);
                startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
                break;

            case OperType.ROLLBACK_CASE:
                gdFormBean = GDFormBean.generateSimpleForm(
                        new String[]{"DisplayName", "退单原因", "Name", "退单原因", "Type", "短文本", "Validate", "1", "DisplayColSpan", "100"});
                intent = new Intent(TaskDetailActivity.this, DeviceFeedbackDialogActivity.class);
                intent.putExtra("Title", "退单");
                intent.putExtra("Tag", OperType.ROLLBACK_CASE);
                intent.putExtra("GDFormBean", gdFormBean);
                intent.putExtra("ListItemEntity", scheduleTask);
                startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
                break;
        }
    }

    public interface OperType {
        String DELAY_STOP_GAS = "延长停气";
        String REQUEST_DELAY = "申请延期";
        String ROLLBACK_CASE = "退单";
    }

    @Override
    public void onBackPressed() {

        if (comeFrom != Source.FromMap) {
            super.onBackPressed();
        } else {
            // MapView -> this -> MapView
            Intent intent = new Intent(TaskDetailActivity.this, MapGISFrame.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

            AppManager.finishActivity(this);
        }
    }

    //region "工商户安检"业务中对"所属调压器"和"关联表具"的反馈逻辑

    private void marchantSecurityCheck() {

        addBottomUnitView("设备操作", false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ListDialogFragment fragment = new ListDialogFragment("操作",
                        new String[]{"调压器养护", "工商户表具养护", "隐患上报"});
                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {


                        if ("隐患上报".equals(value)) {
                            navigateToRiskReport();
                        } else {

                            String deviceType = "";
                            if ("调压器养护".equals(value)) {
                                deviceType = MSC_RELATED_REGULATOR;
                            } else if ("工商户表具养护".equals(value)) {
                                deviceType = MSC_RELATED_METER;
                            }

                            RelatedDeviceListFragment fragment = new RelatedDeviceListFragment();
                            Bundle args = new Bundle();
                            args.putString("ID", String.valueOf(scheduleTask.ID));
                            args.putString("DeviceType", deviceType);
                            fragment.setArguments(args);
                            fragment.show(getSupportFragmentManager(), "");
                        }

                    }
                });
                fragment.show(getSupportFragmentManager(), "");
            }
        });
    }
    //endregion

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private final ArrayList<String> pageTitles = new ArrayList<>();

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);

            for (TableMetaData tableMetaData : flowTableInfo.TableMetaDatas) {
                String title = TextUtils.isEmpty(tableMetaData.TableAlias) ? tableMetaData.TableName : tableMetaData.TableAlias;
                if (title.endsWith("表")) {
                    title = title.substring(0, title.length() - 1);
                }
                pageTitles.add(title);
            }

            if (comeFrom == Source.FromCareHistory) {
                pageTitles.add(0, "反馈信息"); // 历史记录查看详情“反馈信息”要在第一个显示位置

                if ((innerFlags & MASK_MATERIAL_ADDABLE) == MASK_MATERIAL_ADDABLE) {
                    pageTitles.add("物料清单");
                    pageTitles.add("采购订单");
                }
                if ((innerFlags & MASK_CONSUMABLE_ADDABLE) == MASK_CONSUMABLE_ADDABLE) {
                    pageTitles.add("耗材详情");
                }
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles.get(position);
        }

        @Override
        public int getCount() {
            return pageTitles.size();
        }

        @Override
        public Fragment getItem(int position) {

            Bundle args = null;

            final String tabName = pageTitles.get(position);

            switch (tabName) {

                case "反馈信息":
                    args = new Bundle();
                    args.putString("BizName", scheduleTask.BizName);
                    args.putString("TaskCode", scheduleTask.TaskCode);
                    args.putString("BizTaskTable", scheduleTask.BizTaskTable);
                    args.putString("BizFeedBackTable", scheduleTask.BizFeedBackTable);
                    return Fragment.instantiate(TaskDetailActivity.this, FeedbackInfoFragment.class.getName(), args);

                case "物料清单":
                    args = new Bundle();
                    args.putString("CaseNo", scheduleTask.TaskCode);
                    return Fragment.instantiate(TaskDetailActivity.this, MaterialFragment.class.getName(), args);

                case "采购订单":
                    args = new Bundle();
                    args.putString("CaseNo", scheduleTask.TaskCode);
                    return Fragment.instantiate(TaskDetailActivity.this, PurchaseOrderFragment.class.getName(), args);

                case "耗材详情":
                    args = new Bundle();
                    args.putString("BizName", scheduleTask.BizName);
                    args.putString("CaseNo", scheduleTask.TaskCode);
                    return Fragment.instantiate(TaskDetailActivity.this, ConsumableFragment.class.getName(), args);

                default:

                    FlowNodeMeta flowNodeMeta = flowTableInfo.TableMetaDatas.get(
                            comeFrom != Source.FromCareHistory ? position : position - 1).FlowNodeMeta;

                    if (flowNodeMeta != null) {
                        args = new Bundle();
                        args.putParcelable("GDFormBean", flowNodeMeta.mapToGDFormBean());
                    }
                    return Fragment.instantiate(TaskDetailActivity.this, FlowBeanFragment.class.getName(), args);
            }
        }
    }

}
