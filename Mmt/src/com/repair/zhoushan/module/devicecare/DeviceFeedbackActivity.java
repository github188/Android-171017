package com.repair.zhoushan.module.devicecare;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.common.BaseTaskResults;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.entity.FeedbackData;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.entity.TriggerEventData;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.devicecare.consumables.AddHaoCaiActivity;
import com.repair.zhoushan.module.devicecare.consumables.MaterialListActivity;
import com.repair.zhoushan.module.devicecare.consumables.PurchaseOrderListActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DeviceFeedbackActivity extends BaseActivity {

    private ScheduleTask mScheduleTask;

    // 过滤条件
    private ArrayList<MaintenanceFeedBack> filterCriteria;

    // 界面结构数据
    private FlowNodeMeta mFlowNodeMeta;

    private FlowBeanFragment mFlowBeanFragment;

    // 上报信息
    private final FeedbackData mFeedbackData = new FeedbackData();

    private int userId;
    private boolean isSuccess = false;

    private TaskDetailActivity.Source comeFrom;

    // 调压器养护添加耗材，需要根据当前选中的养护类型获取可添加的耗材列表，阀门养护传空即可
    private ImageButtonView careTypeView;

    // "工商户抄表"业务中，当前录入的表底数应该与上期录入的表底数比对
    private ImageEditView meterReadingView;

    private final HashMap<String, String> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.userId = MyApplication.getInstance().getUserId();

        Intent outerIntent = getIntent();
        this.mScheduleTask = outerIntent.getParcelableExtra("ListItemEntity");
        this.comeFrom = (TaskDetailActivity.Source) outerIntent.getSerializableExtra("ComeFrom");
        this.filterCriteria = outerIntent.getParcelableArrayListExtra("FeedbackConfigs");

        if (outerIntent.hasExtra("KeyValueMap")) {
            map.putAll((HashMap<String, String>) outerIntent.getSerializableExtra("KeyValueMap"));
        }
        getBaseTextView().setText("设备反馈");

        initView();
    }

    private void initView() {

        BaseTaskResults<String, Integer, FlowNodeMeta> task = new BaseTaskResults<String, Integer, FlowNodeMeta>(DeviceFeedbackActivity.this) {
            @NonNull
            @Override
            protected String getRequestUrl() throws Exception {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetFeedbackTableSchema")
                        .append("?bizName=").append(mScheduleTask.BizName)
                        .append("&taskCode=").append(mScheduleTask.TaskCode)
                        .append("&feedbackType=").append(filterCriteria.get(0).feedBackType);
                return sb.toString();
            }

            @Override
            protected void onSuccess(Results<FlowNodeMeta> results) {
                ResultData<FlowNodeMeta> resultData = results.toResultData();
                if (resultData.ResultCode != 200) {
                    Toast.makeText(DeviceFeedbackActivity.this, resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (resultData.DataList == null || resultData.DataList.size() == 0) {
                    Toast.makeText(DeviceFeedbackActivity.this, "获取界面结构失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                mFlowNodeMeta = resultData.getSingleData();
                createView();
                createBottomView();
            }
        };
        task.setCancellable(false);
        task.mmtExecute();
    }

    private String material = ""; // 材质
    private float lastMeterReading; // 上期表底数

    private void createView() {

        String tempStr;
        if (map.containsKey("材质") && !TextUtils.isEmpty(tempStr = map.get("材质"))) {
            material = tempStr;
        }

        String fieldName;
        // 养护类型默认值取任务表中的计划养护类型
        for (FlowNodeMeta.TableValue value : mFlowNodeMeta.Values) {

            fieldName = value.FieldName;
            if (fieldName.equals("GIS图层")) {
                value.FieldValue = mScheduleTask.GisLayer;
            } else if (fieldName.equals("GIS编号")) {
                value.FieldValue = mScheduleTask.GisCode;
            } else if (fieldName.equals("坐标位置")) {
                value.FieldValue = mScheduleTask.Position;
            } else if (fieldName.equals("养护类型") || fieldName.equals("材质")) {
                if (map.containsKey(value.FieldName) && !TextUtils.isEmpty(tempStr = map.get(value.FieldName))) {
                    value.FieldValue = tempStr;
                }
            } else if (fieldName.equals("上期表底数")) {
                if ("工商户抄表".equals(mScheduleTask.BizName) && !TextUtils.isEmpty(tempStr = map.get("上期表底数"))) {
                    value.FieldValue = tempStr;
                    try {
                        lastMeterReading = Float.parseFloat(tempStr);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // 维护执行人(人员选择器)要求默认取任务表中的养护人
        boolean matched = false;
        for (FlowNodeMeta.TableGroup tableGroup : mFlowNodeMeta.Groups) {
            for (FlowNodeMeta.FieldSchema schema : tableGroup.Schema) {
                if ("人员选择器".equals(schema.Shape) && "维护执行人".equals(schema.FieldName)) {
                    if (map.containsKey("任务养护人")) {
                        schema.PresetValue = map.get("任务养护人");
                        matched = true;
                        break;
                    }
                }
            }
            if (matched) break;
        }

        this.mFlowBeanFragment = new FlowBeanFragment();

        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", mFlowNodeMeta.mapToGDFormBean());
        args.putString("CacheSearchParam", ("userId=" + userId + " and key='" + mScheduleTask.TaskCode + "'"));
        mFlowBeanFragment.setArguments(args);

        mFlowBeanFragment.setFragmentFileRelativePath(mScheduleTask.TaskCode);
        mFlowBeanFragment.setFilterCriteria(filterCriteria, map);
        mFlowBeanFragment.setExceptionValueSelectedListener(new FlowBeanFragment.OnExceptionValueSelectedListener() {
            @Override
            public void onExceptionValueSelected(String controlName, String eventName, String eventFieldGroup) {
                doEventReport(controlName, eventName, eventFieldGroup);
            }
        });
        mFlowBeanFragment.setBeanFragmentOnCreate(new FlowBeanFragment.BeanFragmentOnCreate() {
            @Override
            public void onCreated() {

                careTypeView = (ImageButtonView) mFlowBeanFragment.findViewByName("养护类型");

                // "工商户抄表"业务，表底数比较逻辑
                if ("工商户抄表".equals(mScheduleTask.BizName)) {
                    meterReadingView = (ImageEditView) mFlowBeanFragment.findViewByName("表底数");
                    if (meterReadingView != null) {
                        meterReadingView.getEditText().addTextChangedListener(new TextWatcher() {

                            boolean isRed = false;

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                                String valueStr = s.toString();
                                if (lastMeterReading == 0 || TextUtils.isEmpty(valueStr)) return;

                                try {
                                    float curMeterReading = Float.parseFloat(valueStr);
                                    if (curMeterReading <= lastMeterReading) {
                                        if (!isRed) {
                                            meterReadingView.getEditText().setTextColor(Color.parseColor("#ff0000"));
                                            isRed = true;
                                        }
                                    } else if (isRed) {
                                        meterReadingView.getEditText().setTextColor(Color.parseColor("#000000"));
                                        isRed = false;
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        });

        addFragment(mFlowBeanFragment);

    }

    private boolean allowAddConsumable;

    private void createBottomView() {

        this.allowAddConsumable = getIntent().getBooleanExtra("AllowAddConsumable", false);

        if (allowAddConsumable) {

            BottomUnitView consumableUnitView = new BottomUnitView(DeviceFeedbackActivity.this);
            consumableUnitView.setContent("耗材");
            addBottomUnitView(consumableUnitView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(DeviceFeedbackActivity.this, AddHaoCaiActivity.class);
                    intent.putExtra("ListItemEntity", mScheduleTask);

                    String careType;
                    if (careTypeView != null) {
                        careType = careTypeView.getValue();
                    } else {
                        careType = material;
                    }

                    intent.putExtra("CareType", careType);

                    startActivity(intent);
                }
            });
        }

        if (getIntent().getBooleanExtra("AllowAddMaterial", false)) {

            BottomUnitView materialUnitView = new BottomUnitView(DeviceFeedbackActivity.this);
            materialUnitView.setContent("材料");
            addBottomUnitView(materialUnitView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ListDialogFragment listDialogFragment = new ListDialogFragment("材料选择", new String[]{"物料清单", "采购订单"});
                    listDialogFragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int arg2, String value) {
                            if (value.equals("物料清单")) {
                                Intent intent = new Intent(DeviceFeedbackActivity.this, MaterialListActivity.class);
                                intent.putExtra("ListItemEntity", mScheduleTask);
                                startActivity(intent);

                            } else if (value.equals("采购订单")) {
                                Intent intent = new Intent(DeviceFeedbackActivity.this, PurchaseOrderListActivity.class);
                                intent.putExtra("ListItemEntity", mScheduleTask);
                                startActivity(intent);
                            }
                        }
                    });
                    listDialogFragment.show(getSupportFragmentManager(), "");

                }
            });
        }

        BottomUnitView saveUnitView = new BottomUnitView(DeviceFeedbackActivity.this);
        saveUnitView.setContent("保存");
        addBottomUnitView(saveUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                saveFB();
            }
        });


        BottomUnitView feedbackUnitView = new BottomUnitView(DeviceFeedbackActivity.this);
        feedbackUnitView.setContent("完成");
        addBottomUnitView(feedbackUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {

                OkCancelDialogFragment fragment = new OkCancelDialogFragment("是否确认提交？");
                fragment.setLeftBottonText("取消");
                fragment.setRightBottonText("提交");
                fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                    @Override
                    public void onRightButtonClick(View view) {
                        doFeedback();
                    }
                });
                fragment.show(DeviceFeedbackActivity.this.getSupportFragmentManager(), "");
            }
        });

    }

    // 将所有信息封装成后台上传的数据模型
    private ReportInBackEntity createSaveOrFeedbackReportEntity(String reportUrl) {

        if (mFlowBeanFragment == null) {
            return null;
        }

        try {
            List<FeedItem> feedItemList = mFlowBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

            if (feedItemList == null) {
                return null;
            }

            // 把Feedback的值映射到Value上,并去除不需要反馈的Value，效果是使Value的个数与FeedbackItem的数目保持一致
            // 两方面考虑： 1.缓存的是B状态的界面数据，重新打开界面会是A状态，B状态下的多余数据需要清除
            // 2.数据库中NULL与空字符串的区别
            Iterator<FlowNodeMeta.TableValue> valueIterator = mFlowNodeMeta.Values.iterator();
            FlowNodeMeta.TableValue value;
            boolean containItem;

            boolean[] missedIndexes = new boolean[feedItemList.size()];
            Arrays.fill(missedIndexes, true);
            FeedItem feedItem;

            // FeedbackItem -> Form (可能会有Form字段的缺失，Form中多余的字段删掉)
            while (valueIterator.hasNext()) {
                value = valueIterator.next();
                containItem = false;

                for (int i = 0; i < feedItemList.size(); i++) {
                    feedItem = feedItemList.get(i);
                    if (value.FieldName.equals(feedItem.Name)) {
                        value.FieldValue = feedItem.Value;
                        containItem = true;
                        missedIndexes[i] = false;
                        break;
                    }
                }
                if (!containItem) {
                    valueIterator.remove();
                }
            }

            // 将缺失的 Value加进来
            for (int i = 0; i < missedIndexes.length; i++) {
                if (missedIndexes[i]) {
                    feedItem = feedItemList.get(i);
                    mFlowNodeMeta.Values.add(mFlowNodeMeta.new TableValue(feedItem.Name, feedItem.Value));
                }
            }

            mFeedbackData.DataParam.flowNodeMeta = mFlowNodeMeta;
            mFeedbackData.TableName = mScheduleTask.BizFeedBackTable;

            mFeedbackData.DefaultParam = "GIS图层:" + mScheduleTask.GisLayer + ";GIS编号:" + mScheduleTask.GisCode;

            String data = new Gson().toJson(mFeedbackData, new TypeToken<FeedbackData>() {
            }.getType());

            // 将所有信息封装成后台上传的数据模型
            return new ReportInBackEntity(
                    data,
                    MyApplication.getInstance().getUserId(),
                    ReportInBackEntity.REPORTING,
                    reportUrl,
                    UUID.randomUUID().toString(),
                    "",
                    mFlowBeanFragment.getAbsolutePaths(),
                    mFlowBeanFragment.getRelativePaths());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void saveFB() {

        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
        sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SaveOrUpdateFeedbackData")
                .append("?bizName=").append(mScheduleTask.BizName)
                .append("&taskCode=").append(mScheduleTask.TaskCode);

        final ReportInBackEntity entity = createSaveOrFeedbackReportEntity(sb.toString());

        if (entity == null) {
            MyApplication.getInstance().showMessageWithHandle("保存失败");
            return;
        }

        new MmtBaseTask<Void, String, ResultData<Integer>>(this) {
            @Override
            protected ResultData<Integer> doInBackground(Void... params) {
                return entity.report(this);
            }

            @Override
            protected void onSuccess(ResultData<Integer> resultData) {
                super.onSuccess(resultData);

                if (resultData == null) {
                    MyApplication.getInstance().showMessageWithHandle("保存错误");
                    return;
                }
                if (resultData.ResultCode <= 0) {
                    MyApplication.getInstance().showMessageWithHandle(resultData.ResultMessage);
                    return;
                }
                MyApplication.getInstance().showMessageWithHandle("保存成功");
            }
        }.mmtExecute();
    }

    private void doFeedback() {

        try {

            final int userID = MyApplication.getInstance().getUserId();
            final String trueName = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;

            StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
            sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DoneFeedback")
                    .append("?bizName=").append(mScheduleTask.BizName)
                    .append("&taskCode=").append(mScheduleTask.TaskCode)
                    .append("&carePersonID=").append(String.valueOf(userID))
                    .append("&carePersonName=").append(trueName);

            final ReportInBackEntity entity = createSaveOrFeedbackReportEntity(sb.toString());

            if (entity == null) {
                MyApplication.getInstance().showMessageWithHandle("提交失败");
                return;
            }

            // 先上报反馈数据，再更新任务表中的反馈记录Id字段
            new MmtBaseTask<ReportInBackEntity, String, ResultWithoutData>(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultWithoutData>() {

                @Override
                public void doAfter(ResultWithoutData data) {
                    if (data == null) {
                        Toast.makeText(DeviceFeedbackActivity.this, "反馈失败", Toast.LENGTH_SHORT).show();
                    } else if (data.ResultCode < 0) {
                        Toast.makeText(DeviceFeedbackActivity.this,
                                TextUtils.isEmpty(data.ResultMessage) ? "反馈失败" : data.ResultMessage,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DeviceFeedbackActivity.this, "反馈成功", Toast.LENGTH_SHORT).show();
                        success();
                    }
                }
            }) {

                String filterValue = "";

                @Override
                protected void onPreExecute() {
                    setCancellable(false);
                    super.onPreExecute();

                    if (careTypeView != null) {
                        filterValue = careTypeView.getValue();
                        if (TextUtils.isEmpty(filterValue)) {
                            filterValue = "";
                        }
                    }
                }

                @Override
                protected ResultWithoutData doInBackground(ReportInBackEntity... params) {
                    ResultWithoutData resultWithoutData = null;

                    try {
                        String trueName = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;

                        if (allowAddConsumable) {
                            StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                            sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CheckMaterial")
                                    .append("?caseNo=").append(mScheduleTask.TaskCode)
                                    .append("&bizName=").append(mScheduleTask.BizName)
                                    .append("&filterValue=").append(filterValue)
                                    .append("&deviceType=").append(mScheduleTask.EquipmentType == null ? "" : mScheduleTask.EquipmentType)
                                    .append("&man=").append(trueName)
                                    .append("&time=").append(Uri.encode(BaseClassUtil.getSystemTime()));

                            String checkResultStr = NetUtil.executeHttpGet(sb.toString());

                            if (BaseClassUtil.isNullOrEmptyString(checkResultStr)) {
                                resultWithoutData = new ResultWithoutData();
                                resultWithoutData.ResultMessage = "耗材信息检查失败";
                                return resultWithoutData;
                            }
                            resultWithoutData = new Gson().fromJson(checkResultStr, new TypeToken<ResultWithoutData>() {
                            }.getType());
                            if (resultWithoutData.ResultCode != 200) {
                                return resultWithoutData;
                            }
                        }

                        // 反馈信息上报
                        ResultData<Integer> resultDatas = params[0].report(this);
                        return resultDatas;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return resultWithoutData;
                }
            }.mmtExecute(entity);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void success() {

        try {
            isSuccess = true;

            // 上报成功后清除当前记录的缓存数据
            mFlowBeanFragment.deleteCacheData(userId, mScheduleTask.TaskCode);

            Intent intent = new Intent();
            intent.setClass(DeviceFeedbackActivity.this, DeviceCareListActivity.class);
            intent.putExtra("BizName", mScheduleTask.BizName);
            intent.addCategory(Constants.CATEGORY_BACK_TO_LIST);

//            if (TaskDetailActivity.Source.FromMap == comeFrom || TaskDetailActivity.Source.FromTempMap == comeFrom) {
//                EventBus.getDefault().post(new com.mapgis.mmt.constant.Constants.HandleToListWithMapEvent());
//
//                // Finish detail activity
//                AppManager.finishActivity(AppManager.secondLastActivity());
//                // Bring list activity to front
//                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                startActivity(intent);
//                AppManager.finishActivity(this);
//
//            } else {
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                startActivity(intent);
//            }

            if (TaskDetailActivity.Source.FromMap == comeFrom || TaskDetailActivity.Source.FromTempMap == comeFrom) {
                EventBus.getDefault().post(new com.mapgis.mmt.constant.Constants.HandleToListWithMapEvent());
            }

            // Finish detail activity
            AppManager.finishActivity(AppManager.secondLastActivity());
            // Bring list activity to front
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            AppManager.finishActivity(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doEventReport(final String controlName, final String eventName, final String eventFieldGroup) {

        MmtBaseTask<Void, Void, ResultData<TriggerEventData>> mmtBaseTask
                = new MmtBaseTask<Void, Void, ResultData<TriggerEventData>>(DeviceFeedbackActivity.this) {
            @Override
            protected ResultData<TriggerEventData> doInBackground(Void... params) {

                ResultData<TriggerEventData> resultData;

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc"
                        + String.format(Locale.CHINA, "/TiggerEventManage/%d/GetTiggerEventData?eventName=%s&eventFieldGroup=%s", userId, eventName, eventFieldGroup);

                try {
                    String jsonResult = NetUtil.executeHttpGet(url);

                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取数据失败：网络错误");
                    }

                    Results<TriggerEventData> results = new Gson().fromJson(jsonResult, new TypeToken<Results<TriggerEventData>>() {
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
            protected void onSuccess(ResultData<TriggerEventData> resultData) {
                if (resultData.ResultCode != 200) {
                    Toast.makeText(DeviceFeedbackActivity.this, resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(DeviceFeedbackActivity.this, DeviceFBEventReportActivity.class);
                intent.putExtra("FlowNodeMetaStr", new Gson().toJson(resultData.getSingleData().nodeMeta));
                intent.putExtra("FlowCenterData", resultData.getSingleData().flowData);
                intent.putExtra("TriggerControlName", controlName);

                intent.putExtra("TaskCode", mScheduleTask.TaskCode);
                intent.putExtra("BizType", mScheduleTask.BizName);

                intent.putExtra("Address", map.get("位置"));
                intent.putExtra("Coordinate", mScheduleTask.Position);
                intent.putExtra("GISCode", map.get("GIS编号"));
                intent.putExtra("DeviceName", map.get("设备名称"));
                intent.putExtra("Code", map.get("编号"));

                startActivity(intent);
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    @Override
    protected void onPause() {
        // 上报成功后就不需要再缓存
        if (!isSuccess && mFlowBeanFragment != null) {
            mFlowBeanFragment.saveCacheData(userId, mScheduleTask.TaskCode);
        }
        super.onPause();
    }

}
