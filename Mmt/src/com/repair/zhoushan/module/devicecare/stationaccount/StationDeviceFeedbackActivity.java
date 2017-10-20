package com.repair.zhoushan.module.devicecare.stationaccount;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultStatus;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FeedbackData;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.consumables.AddHaoCaiActivity;
import com.repair.zhoushan.module.devicecare.consumables.MaterialListActivity;
import com.repair.zhoushan.module.devicecare.consumables.PurchaseOrderListActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class StationDeviceFeedbackActivity extends BaseActivity {

    private ScheduleTask mScheduleTask;
    private int userId;

    // 界面结构数据
    private FlowNodeMeta mFlowNodeMeta;
    private FlowBeanFragment mFlowBeanFragment;

    // 上报信息
    private final FeedbackData mFeedbackData = new FeedbackData();

    private List<Integer> availableOperType = new ArrayList<Integer>();

    private ImageButtonView careTypeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mScheduleTask = getIntent().getParcelableExtra("ListItemEntity");
        this.userId = MyApplication.getInstance().getUserId();

        getBaseTextView().setText("场站设备反馈");

        getBaseRightImageView().setVisibility(View.VISIBLE);
        getBaseRightImageView().setImageResource(R.drawable.ic_detail_white);
        getBaseRightImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(StationDeviceFeedbackActivity.this, StationDeviceDetailActivity.class);
                intent.putExtra("ListItemEntity", mScheduleTask);
                startActivity(intent);
            }
        });

        initView();
    }

    private void initView() {

        MmtBaseTask<String, Void, String[]> mmtBaseTask = new MmtBaseTask<String, Void, String[]>(StationDeviceFeedbackActivity.this) {
            @Override
            protected String[] doInBackground(String... params) {

                String[] resultStrArr = new String[2];

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeviceManage/")
                        .append(userId).append("/StationTaskFeedBackDetail")
                        .append("?bizName=").append(mScheduleTask.BizName)
                        .append("&feedBackType=").append(mScheduleTask.FeedBackType)
                        .append("&taskCode=").append(mScheduleTask.TaskCode);

                resultStrArr[0] = NetUtil.executeHttpGet(sb.toString());

                String operTypeUrl = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/FetchYangHuTaskOpers?bizName="
                        + mScheduleTask.BizName;

                resultStrArr[1] = NetUtil.executeHttpGet(operTypeUrl);

                return resultStrArr;
            }

            @Override
            protected void onSuccess(String[] resultStrArr) {

                ResultData<FlowNodeMeta> tableResult = Utils.json2ResultDataActivity(FlowNodeMeta.class,
                        StationDeviceFeedbackActivity.this, resultStrArr[0], "获取界面结构失败", false);
                if (tableResult == null) return;

                ResultData<Integer> operTypeResult = new Gson().fromJson(resultStrArr[1], new TypeToken<ResultData<Integer>>() {
                }.getType());

                String defErrMsg = "获取可操作类型列表失败";
                if (operTypeResult == null) {
                    Toast.makeText(StationDeviceFeedbackActivity.this, defErrMsg, Toast.LENGTH_SHORT).show();

                } else if (operTypeResult.ResultCode != 200) {
                    Toast.makeText(StationDeviceFeedbackActivity.this,
                            TextUtils.isEmpty(operTypeResult.ResultMessage) ? defErrMsg : operTypeResult.ResultMessage, Toast.LENGTH_LONG).show();
                } else {
                    availableOperType.addAll(operTypeResult.DataList);
                }

                mFlowNodeMeta = tableResult.getSingleData();

                createView();
                createBottomView();
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void createView() {

        this.mFlowBeanFragment = new FlowBeanFragment();

        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", mFlowNodeMeta.mapToGDFormBean());
        args.putString("CacheSearchParam", ("userId=" + userId + " and key='" + mScheduleTask.TaskCode + "'"));
        mFlowBeanFragment.setArguments(args);
        mFlowBeanFragment.setBeanFragmentOnCreate(new FlowBeanFragment.BeanFragmentOnCreate() {
            @Override
            public void onCreated() {
                // "养护类型"字段
                careTypeView = (ImageButtonView) mFlowBeanFragment.findViewByName("养护类型");
            }
        });

        addFragment(mFlowBeanFragment);
    }

    private void createBottomView() {

        // 允许添加耗材
        if (availableOperType.size() > 5 && availableOperType.get(4) == 1) {

            BottomUnitView consumableUnitView = new BottomUnitView(StationDeviceFeedbackActivity.this);
            consumableUnitView.setContent("耗材");
            addBottomUnitView(consumableUnitView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(StationDeviceFeedbackActivity.this, AddHaoCaiActivity.class);
                    intent.putExtra("ListItemEntity", mScheduleTask);

                    String careType = "";
                    if (careTypeView != null) {
                        careType = careTypeView.getValue();
                    }
                    // 养护类型,没有这个字段则传空
                    intent.putExtra("CareType", careType);

                    startActivity(intent);
                }
            });
        }

        // 允许添加材料
        if (availableOperType.size() > 5 && availableOperType.get(5) == 1) {

            BottomUnitView materialUnitView = new BottomUnitView(StationDeviceFeedbackActivity.this);
            materialUnitView.setContent("材料");
            addBottomUnitView(materialUnitView, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ListDialogFragment listDialogFragment = new ListDialogFragment("材料选择", new String[]{"物料清单", "采购订单"});
                    listDialogFragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int arg2, String value) {
                            if (value.equals("物料清单")) {
                                Intent intent = new Intent(StationDeviceFeedbackActivity.this, MaterialListActivity.class);
                                intent.putExtra("ListItemEntity", mScheduleTask);
                                startActivity(intent);

                            } else if (value.equals("采购订单")) {
                                Intent intent = new Intent(StationDeviceFeedbackActivity.this, PurchaseOrderListActivity.class);
                                intent.putExtra("ListItemEntity", mScheduleTask);
                                startActivity(intent);
                            }
                        }
                    });
                    listDialogFragment.show(getSupportFragmentManager(), "");

                }
            });
        }

        BottomUnitView saveUnitView = new BottomUnitView(StationDeviceFeedbackActivity.this);
        saveUnitView.setContent("保存");
        addBottomUnitView(saveUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                saveFB();
            }
        });

        BottomUnitView feedbackUnitView = new BottomUnitView(StationDeviceFeedbackActivity.this);
        feedbackUnitView.setContent("完成");
        addBottomUnitView(feedbackUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                doFeedback();
            }
        });

    }

    private void saveFB() {

        final ReportInBackEntity entity = createReportEnt();

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

                if (resultData.DataList.size() != 2 || (recordId = resultData.DataList.get(1)) < 1) {
                    MyApplication.getInstance().showMessageWithHandle(resultData.ResultMessage);
                    return;
                }

                if (mFlowBeanFragment != null) {
                    mFlowBeanFragment.saveCacheData(userId, mScheduleTask.TaskCode, recordId);
                }

                MyApplication.getInstance().showMessageWithHandle("保存成功");

            }
        }.mmtExecute();

    }

    private int isUpdate() {

        if (recordId > 0) {
            return recordId;
        }

        if (mFlowBeanFragment == null) {
            return -1;
        }

        return mFlowBeanFragment.getCacheRecordId(userId, mScheduleTask.TaskCode);
    }

    private ReportInBackEntity createReportEnt() {

        if (mFlowBeanFragment == null) {
            return null;
        }

        List<FeedItem> feedItemList = mFlowBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);
        if (feedItemList == null) {
            return null;
        }

        // 把Feedback的值映射到Value上
        LinkedList<FlowNodeMeta.TableValue> valueList = new LinkedList<>();
        for (FeedItem item : feedItemList) {
            valueList.add(mFlowNodeMeta.new TableValue(item.Name, item.Value));
        }
        mFlowNodeMeta.Values.clear();
        mFlowNodeMeta.Values.addAll(valueList);

        mFeedbackData.DataParam.flowNodeMeta = mFlowNodeMeta;
        mFeedbackData.TableName = mScheduleTask.BizFeedBackTable;
        mFeedbackData.DefaultParam = " : ";

        // 创建服务路径
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SaveFeedbackData";

        int recordId = -1;
        if ((recordId = isUpdate()) > 0) {
            uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/UpdateFeedbackData/" + recordId;

        }

        String data = new Gson().toJson(mFeedbackData, new TypeToken<FeedbackData>() {
        }.getType());

        // 将所有信息封装成后台上传的数据模型
        return new ReportInBackEntity(
                data,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                "",
                mFlowBeanFragment.getAbsolutePaths(),
                mFlowBeanFragment.getRelativePaths());
    }

    private void doFeedback() {

        final ReportInBackEntity entity = createReportEnt();

        if (entity == null) {
            MyApplication.getInstance().showMessageWithHandle("提交失败");
            return;
        }

        // 先上报反馈数据，再更新任务表中的反馈记录Id字段
        new MmtBaseTask<ReportInBackEntity, String, ResultWithoutData>(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultWithoutData>() {

            @Override
            public void doAfter(ResultWithoutData data) {
                if (data == null) {

                    Toast.makeText(StationDeviceFeedbackActivity.this, "反馈失败", Toast.LENGTH_SHORT).show();

                } else if (data.ResultCode < 0) {

                    String errorMsg = "反馈失败";
                    if (!BaseClassUtil.isNullOrEmptyString(data.ResultMessage)) {
                        errorMsg = data.ResultMessage;
                    }
                    Toast.makeText(StationDeviceFeedbackActivity.this, errorMsg, Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(StationDeviceFeedbackActivity.this, "反馈成功", Toast.LENGTH_SHORT).show();
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

                String trueName = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CheckMaterial")
                        .append("?caseNo=").append(mScheduleTask.TaskCode)
                        .append("&bizName=").append(mScheduleTask.BizName)
                        .append("&filterValue=").append(filterValue)
                        .append("&deviceType=").append(mScheduleTask.EquipmentType == null ? "" : mScheduleTask.EquipmentType)
                        .append("&man=").append(trueName)
                        .append("&time=").append(Uri.encode(BaseClassUtil.getSystemTime()));

                try {

                    String checkResultStr = NetUtil.executeHttpGet(sb.toString());

                    if (BaseClassUtil.isNullOrEmptyString(checkResultStr)) {
                        resultWithoutData = new ResultWithoutData();
                        resultWithoutData.ResultMessage = "更新工单材料信息失败";
                        return resultWithoutData;
                    }
                    resultWithoutData = new Gson().fromJson(checkResultStr, new TypeToken<ResultWithoutData>() {
                    }.getType());
                    if (resultWithoutData.ResultCode != 200) {
                        return resultWithoutData;
                    }

                    // 反馈信息上报
                    ResultData<Integer> resultDatas = params[0].report(this);

                    if (resultDatas.ResultCode < 0) {
                        return resultDatas;
                    }

                    // ReportInBackEntity的上报方法返回的DataList第一个值是网络返回的状态码
                    if (resultDatas.DataList.size() < 2 || resultDatas.DataList.get(1) < 0) {
                        resultDatas.ResultCode = -100;
                        return resultDatas;
                    }

                    // 更新反馈表
                    int feedbackId = resultDatas.DataList.get(1);

                    // ==0：feedbackID已存在不需要更新；==-1：反馈失败；>0：返回新插入的id
                    if (feedbackId <= 0) {
                        resultDatas.ResultCode = -100;
                        return resultDatas;
                    }

                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/UpdateYHDeviceFBID";
                    String json = NetUtil.executeHttpGet(url, "bizName", mScheduleTask.BizName, "taskNO", mScheduleTask.TaskCode, "fbID", String.valueOf(feedbackId),
                            "carePersonID", String.valueOf(userId), "carePersonName", trueName);

                    if (BaseClassUtil.isNullOrEmptyString(json)) {
                        resultWithoutData = new ResultWithoutData();
                        resultWithoutData.ResultMessage = "更新反馈信息失败";
                        return resultWithoutData;
                    }

                    ResultStatus rawData = new Gson().fromJson(json, new TypeToken<ResultStatus>() {
                    }.getType());
                    resultWithoutData = rawData.toResultWithoutData();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return resultWithoutData;
            }
        }.mmtExecute(entity);

    }

    private boolean isSuccess = false;

    private void success() {

        isSuccess = true;

//        // 养护反馈对已经反馈的设备进行二次反馈时，二次反馈的界面中的信息应该继承第一次反馈的信息，这时不再删缓存
//        // 上报成功后清除当前记录的缓存数据
//        mFlowBeanFragment.deleteCacheData(userId, mScheduleTask.TaskCode);

        Intent intent = new Intent(StationDeviceFeedbackActivity.this, StationAccountListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("BizName", mScheduleTask.BizName);
        intent.addCategory(Constants.CATEGORY_BACK_TO_LIST);
        startActivity(intent);

        this.finish();
    }

    private int recordId = -1;

    @Override
    protected void onPause() {
        super.onPause();

//        // 养护反馈对已经反馈的设备进行二次反馈时，二次反馈的界面中的信息应该继承第一次反馈的信息，这时不再删缓存
//        // 上报成功后就不需要再缓存
//        if (!isSuccess && mFlowBeanFragment != null) {
//            mFlowBeanFragment.saveCacheData(userId, mScheduleTask.TaskCode);
//        }

        if (mFlowBeanFragment != null) {
            mFlowBeanFragment.saveCacheData(userId, mScheduleTask.TaskCode, recordId);
        }
    }
}
