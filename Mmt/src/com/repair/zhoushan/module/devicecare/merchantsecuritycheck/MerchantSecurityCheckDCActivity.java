package com.repair.zhoushan.module.devicecare.merchantsecuritycheck;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultStatus;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.entity.FeedbackData;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.devicecare.MaintenanceFeedBack;
import com.repair.zhoushan.module.devicecare.carehistory.CareHistoryListActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 工商户安检：调压器养护/工商户表具养护
 */
public class MerchantSecurityCheckDCActivity extends BaseActivity {

    private String tableName;

    private FlowNodeMeta flowNodeMeta;
    private FlowBeanFragment flowBeanFragment;

    private String deviceID;
    private String gisCode;
    private String bizName;
    private ArrayList<MaintenanceFeedBack> maintenanceFBConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent outerIntent = getIntent();
        this.deviceID = outerIntent.getStringExtra("ID");
        this.gisCode = outerIntent.getStringExtra("GISCode");
        this.maintenanceFBConfig = outerIntent.getParcelableArrayListExtra("MaintenanceFBConfig");

        this.bizName = maintenanceFBConfig.get(0).bizName;
        this.tableName = maintenanceFBConfig.get(0).deviceFBTbl;

        getBaseTextView().setText(bizName);

        initData();
    }

    private void initData() {

        MmtBaseTask<Void, Void, ResultData<FlowNodeMeta>> mmtBaseTask
                = new MmtBaseTask<Void, Void, ResultData<FlowNodeMeta>>(MerchantSecurityCheckDCActivity.this) {
            @Override
            protected ResultData<FlowNodeMeta> doInBackground(Void... params) {

                ResultData<FlowNodeMeta> resultData;

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetTableGroupMeta?tableName="
                        + Uri.encode(tableName);

                try {
                    String jsonResult = NetUtil.executeHttpGet(url);
                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取数据失败:网络请求错误");
                    }

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

                if (resultData.ResultCode != 200) {
                    showErrorMsg(resultData.ResultMessage);
                } else {
                    flowNodeMeta = resultData.getSingleData();

                    initView();
                    initBottomView();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();

    }

    private void initView() {

        GDFormBean gdFormBean = flowNodeMeta.mapToGDFormBean();

        flowBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", gdFormBean);
        flowBeanFragment.setArguments(args);
        flowBeanFragment.setFilterCriteria(maintenanceFBConfig);

        addFragment(flowBeanFragment);
    }

    private void initBottomView() {

        BottomUnitView historyUnitView = new BottomUnitView(MerchantSecurityCheckDCActivity.this);
        historyUnitView.setContent("历史");
        historyUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(historyUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                Intent intent = new Intent(MerchantSecurityCheckDCActivity.this, CareHistoryListActivity.class);

                intent.putExtra("BizName", bizName);
                intent.putExtra("GisCode", gisCode);
                intent.putExtra("Title", "操作历史");
                startActivity(intent);
            }
        });

        BottomUnitView feedbackUnitView = new BottomUnitView(MerchantSecurityCheckDCActivity.this);
        feedbackUnitView.setContent("反馈");
        feedbackUnitView.setImageResource(com.mapgis.mmt.R.drawable.handoverform_report);
        addBottomUnitView(feedbackUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                feedback();
            }
        });
    }

    private void feedback() {

        if (flowBeanFragment == null) {
            return;
        }
        List<FeedItem> feedItemList = flowBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

        if (feedItemList == null) {
            return;
        }

        // 上报信息
        final FeedbackData feedbackData = new FeedbackData();

        // 把Feedback的值映射到Value上
        ArrayList<FlowNodeMeta.TableValue> values = flowNodeMeta.Values;
        for (FlowNodeMeta.TableValue value : values) {
            for (FeedItem item : feedItemList) {
                if (value.FieldName.equals(item.Name)) {
                    value.FieldValue = item.Value;
                    break;
                }
            }
        }

        feedbackData.DataParam.flowNodeMeta = flowNodeMeta;
        feedbackData.TableName = tableName;
        feedbackData.DefaultParam = " : ";

        // 创建服务路径
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SaveFeedbackData";

        // 将对信息转换为JSON字符串
        String data = new Gson().toJson(feedbackData, new TypeToken<FeedbackData>() {
        }.getType());

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                data,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                "",
                flowBeanFragment.getAbsolutePaths(),
                flowBeanFragment.getRelativePaths());

        new MmtBaseTask<ReportInBackEntity, String, ResultWithoutData>(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultWithoutData>() {

            @Override
            public void doAfter(ResultWithoutData data) {
                if (data == null) {
                    Toast.makeText(MerchantSecurityCheckDCActivity.this, "反馈失败", Toast.LENGTH_SHORT).show();
                } else if (data.ResultCode < 0) {
                    String errorMsg = "反馈失败";
                    if (!BaseClassUtil.isNullOrEmptyString(data.ResultMessage)) {
                        errorMsg = data.ResultMessage;
                    }
                    Toast.makeText(MerchantSecurityCheckDCActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MerchantSecurityCheckDCActivity.this, "反馈成功", Toast.LENGTH_SHORT).show();

                    MerchantSecurityCheckDCActivity.this.finish();
                }
            }
        }) {
            @Override
            protected ResultWithoutData doInBackground(ReportInBackEntity... params) {
                ResultWithoutData resultWithoutData = null;

                try {
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
                    final int feedbackId = resultDatas.DataList.get(1);

                    // ==0：feedbackID已存在不需要更新；==-1：反馈失败；>0：返回新插入的id
                    if (feedbackId <= 0) {
                        resultDatas.ResultCode = -111;
                        return resultDatas;
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                            .append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/CreateMaintenanceTask")
                            .append("?fbID=").append(feedbackId)
                            .append("&deviceID=").append(deviceID)
                            .append("&bizType=").append(bizName)
                            .append("&userID=").append(userID)
                            .append("&userName=").append(MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName);

                    String jsonResult = NetUtil.executeHttpGet(sb.toString());

                    if (BaseClassUtil.isNullOrEmptyString(jsonResult)) {
                        resultWithoutData = new ResultWithoutData();
                        resultWithoutData.ResultMessage = "添加任务失败";
                        return resultWithoutData;
                    }

                    ResultStatus resultStatus = new Gson().fromJson(jsonResult, ResultStatus.class);
                    resultWithoutData = resultStatus.toResultWithoutData();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return resultWithoutData;
            }
        }.mmtExecute(entity);
    }
}
