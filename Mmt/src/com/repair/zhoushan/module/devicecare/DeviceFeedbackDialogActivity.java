package com.repair.zhoushan.module.devicecare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.module.BaseDialogActivity;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DeviceFeedbackDialogActivity extends BaseDialogActivity {

    private ScheduleTask mScheduleTask;
    private int userId;
    private String trueName;

    private FlowBeanFragment flowBeanFragment;
    private ImageButtonView delayTimeView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mScheduleTask = getIntent().getParcelableExtra("ListItemEntity");
        this.userId = MyApplication.getInstance().getUserId();
        this.trueName = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;
    }

    @Override
    protected void onViewCreated() {

        // "申请延期"的特殊逻辑
        if (!TextUtils.isEmpty(getmTag()) && getmTag().equals(TaskDetailActivity.OperType.REQUEST_DELAY)) {

            this.flowBeanFragment = getFlowBeanFragment();
            this.delayTimeView = (ImageButtonView) flowBeanFragment.findViewByName("延期时间");

            final Date endTime = (Date) getIntent().getSerializableExtra("EndTime");
            final Date terminalTime = (Date) getIntent().getSerializableExtra("TerminalTime");

            final TextView textView = delayTimeView.getValueTextView();
            textView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String dateStr = s.toString();
                    if (!TextUtils.isEmpty(dateStr)) {
                        try {
                            Date newDate = dateFormat.parse(dateStr);
                            if (newDate.getTime() <= endTime.getTime()) {
                                textView.setText("");
                                Toast.makeText(DeviceFeedbackDialogActivity.this, "延期时间不能小于结束时间", Toast.LENGTH_SHORT).show();
                            } else if (newDate.after(terminalTime)) {
                                textView.setText(dateFormat.format(terminalTime));
                                Toast.makeText(DeviceFeedbackDialogActivity.this, "延期时间不能大于最迟完成时间", Toast.LENGTH_SHORT).show();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void handleOkEvent(String tag, List<FeedItem> feedItemList) {

        String timeStr;
        String reason;

        switch (tag) {

            case TaskDetailActivity.OperType.DELAY_STOP_GAS:

                timeStr = "";
                reason = "";
                for (FeedItem feedItem : feedItemList) {
                    if (feedItem.Name.equals("延长时间")) {
                        timeStr = feedItem.Value;
                    } else if (feedItem.Name.equals("延长原因")) {
                        reason = feedItem.Value;
                    }
                }

                delayStopTask(timeStr, reason);
                break;

            case TaskDetailActivity.OperType.REQUEST_DELAY:

                timeStr = "";
                reason = "";
                for (FeedItem feedItem : feedItemList) {
                    if (feedItem.Name.equals("延期时间")) {
                        timeStr = feedItem.Value;
                    } else if (feedItem.Name.equals("延期原因")) {
                        reason = feedItem.Value;
                    }
                }
                requestDelay(timeStr, reason);
                break;

            case TaskDetailActivity.OperType.ROLLBACK_CASE:

                reason = "";

                for (FeedItem feedItem : feedItemList) {
                    if (feedItem.Name.equals("退单原因")) {
                        reason = feedItem.Value;
                    }
                }
                rollbackCase(reason);
                break;
        }
    }

    private void delayStopTask(final String time, final String reason) {

        MmtBaseTask<String, Void, ResultWithoutData> mmtBaseTask = new MmtBaseTask<String, Void, ResultWithoutData>(DeviceFeedbackDialogActivity.this) {
            @Override
            protected ResultWithoutData doInBackground(String... params) {

                ResultWithoutData data;

                StringBuilder sb = new StringBuilder();
                sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                        .append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DelayStopGasForTask")
                        .append("?bizName=").append(mScheduleTask.BizName)
                        .append("&caseNo=").append(mScheduleTask.TaskCode)
                        .append("&reason=").append(reason)
                        .append("&man=").append(trueName);

                try {
                    // Time格式必须要经过 URI格式化
                    String resultStr = NetUtil.executeHttpGet(sb.toString(), "time", time);

                    data = new Gson().fromJson(resultStr, new TypeToken<ResultWithoutData>() {
                    }.getType());

                } catch (Exception e) {
                    e.printStackTrace();
                    data = new ResultWithoutData();
                    data.ResultCode = -200;
                    data.ResultMessage = e.getMessage();
                }

                return data;
            }

            @Override
            protected void onSuccess(ResultWithoutData resultWithoutData) {
                if (resultWithoutData == null) {
                    Toast.makeText(DeviceFeedbackDialogActivity.this, "申请失败", Toast.LENGTH_SHORT).show();
                } else if (resultWithoutData.ResultCode != 200) {
                    Toast.makeText(DeviceFeedbackDialogActivity.this, TextUtils.isEmpty(resultWithoutData.ResultMessage) ? "申请失败" : resultWithoutData.ResultMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DeviceFeedbackDialogActivity.this, "申请成功", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);

                    DeviceFeedbackDialogActivity.this.onSuccess();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void requestDelay(final String time, final String reason) {

        MmtBaseTask<String, Void, ResultWithoutData> mmtBaseTask = new MmtBaseTask<String, Void, ResultWithoutData>(DeviceFeedbackDialogActivity.this) {
            @Override
            protected ResultWithoutData doInBackground(String... params) {

                ResultWithoutData data;

                StringBuilder sb = new StringBuilder();
                sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                        .append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DelayTask")
                        .append("?bizName=").append(mScheduleTask.BizName)
                        .append("&caseNo=").append(mScheduleTask.TaskCode)
                        .append("&reason=").append(reason)
                        .append("&man=").append(trueName);

                try {
                    // Time格式必须要经过 URI格式化
                    String resultStr = NetUtil.executeHttpGet(sb.toString(), "time", time);

                    data = new Gson().fromJson(resultStr, new TypeToken<ResultWithoutData>() {
                    }.getType());

                } catch (Exception e) {
                    e.printStackTrace();
                    data = new ResultWithoutData();
                    data.ResultCode = -200;
                    data.ResultMessage = e.getMessage();
                }

                return data;
            }

            @Override
            protected void onSuccess(ResultWithoutData resultWithoutData) {
                if (resultWithoutData == null) {
                    Toast.makeText(DeviceFeedbackDialogActivity.this, "申请延期失败", Toast.LENGTH_SHORT).show();
                } else if (resultWithoutData.ResultCode != 200) {
                    Toast.makeText(DeviceFeedbackDialogActivity.this, TextUtils.isEmpty(resultWithoutData.ResultMessage) ? "申请延期失败" : resultWithoutData.ResultMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DeviceFeedbackDialogActivity.this, "申请延期成功", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);

                    DeviceFeedbackDialogActivity.this.onSuccess();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void rollbackCase(final String reason) {

        MmtBaseTask<String, Void, ResultWithoutData> mmtBaseTask = new MmtBaseTask<String, Void, ResultWithoutData>(DeviceFeedbackDialogActivity.this) {
            @Override
            protected ResultWithoutData doInBackground(String... params) {

                ResultWithoutData data;

                StringBuilder sb = new StringBuilder();
                sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                        .append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/RollbackTask")
                        .append("?bizName=").append(mScheduleTask.BizName)
                        .append("&caseNo=").append(mScheduleTask.TaskCode)
                        .append("&reason=").append(reason)
                        .append("&man=").append(trueName);

                try {
                    // Time格式必须要经过 URI格式化
                    String resultStr = NetUtil.executeHttpGet(sb.toString(), "time", BaseClassUtil.getSystemTime());

                    data = new Gson().fromJson(resultStr, new TypeToken<ResultWithoutData>() {
                    }.getType());

                } catch (Exception e) {
                    e.printStackTrace();
                    data = new ResultWithoutData();
                    data.ResultCode = -200;
                    data.ResultMessage = e.getMessage();
                }

                return data;
            }

            @Override
            protected void onSuccess(ResultWithoutData resultWithoutData) {
                if (resultWithoutData == null) {
                    Toast.makeText(DeviceFeedbackDialogActivity.this, "退单失败", Toast.LENGTH_SHORT).show();
                } else if (resultWithoutData.ResultCode != 200) {
                    Toast.makeText(DeviceFeedbackDialogActivity.this, TextUtils.isEmpty(resultWithoutData.ResultMessage) ? "退单失败" : resultWithoutData.ResultMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DeviceFeedbackDialogActivity.this, "退单成功", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);

                    // 退单成功后跳转至列表界面刷新列表
                    Intent intent = new Intent(DeviceFeedbackDialogActivity.this, DeviceCareListActivity.class);
                    intent.putExtra("BizName", mScheduleTask.BizName);
                    intent.addCategory(Constants.CATEGORY_BACK_TO_LIST);

                    // Finish detail activity
                    AppManager.finishActivity(AppManager.secondLastActivity());
                    // Bring list activity to front
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);

                    DeviceFeedbackDialogActivity.this.onSuccess();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    @Override
    protected void onSuccess() {
        super.onSuccess();
    }
}
