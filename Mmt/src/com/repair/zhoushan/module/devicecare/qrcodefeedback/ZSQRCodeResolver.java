package com.repair.zhoushan.module.devicecare.qrcodefeedback;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultTableData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.mapgis.mmt.global.OnResultListener;
import com.repair.zhoushan.module.QRCodeResolver;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.TaskDetailActivity;

import java.lang.ref.WeakReference;

import static com.repair.zhoushan.module.devicecare.DeviceCareListFragment.TASK_DETAIL_REQUEST_CODE;

/**
 * 舟山设备反馈二维码扫描
 */
public class ZSQRCodeResolver implements QRCodeResolver {

    private final QRCodeResolverParams resolverParams;

    public ZSQRCodeResolver(QRCodeResolverParams params) {
        this.resolverParams = params;
    }

    @Override
    public Object resolve(String codeData) {
        Context context = resolverParams.getContext();
        if (context == null) {
            return null;
        }
        resolveQRCode(context, codeData);
        return null;
    }

    @Override
    public void resolve(String codeData, OnResultListener listener) {
        // Noop
    }

    private void resolveQRCode(Context context, String code) {
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(context, "扫码结果为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String separator = null;
        if (code.contains(",")) {
            separator = ",";
        } else if (code.contains("|")) {
            separator = "\\|";
        }
        if (separator == null) {
            Toast.makeText(context, "无法解析：" + code, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] snippets = code.split(separator);
        String deviceID = snippets[1];

        if (TextUtils.isEmpty(deviceID)) {
            Toast.makeText(context, "无效的设备编号", Toast.LENGTH_SHORT).show();
        } else {
            getTaskByDeviceID(deviceID);
        }
    }

    private void getTaskByDeviceID(final String deviceID) {

        Context context = resolverParams.getContext();
        if (context == null) {
            return;
        }

        // TODO: 2017/6/15 Leak latency, refine later.
        MmtBaseTask<Void, Void, ResultTableData<ScheduleTask>> mmtBaseTask
                = new MmtBaseTask<Void, Void, ResultTableData<ScheduleTask>>(context) {
            @Override
            protected ResultTableData<ScheduleTask> doInBackground(Void... params) {

                ResultTableData<ScheduleTask> resultData;

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/FetchYHTaskList";

                try {
                    String jsonResult = NetUtil.executeHttpGet(url,
                            "bizName", resolverParams.getBizName(),
                            "condition", deviceID,
                            "taskType", resolverParams.getTaskType(),
                            "userID", String.valueOf(resolverParams.getUserId()),
                            "pageSize", Integer.toString(10),
                            "pageIndex", Integer.toString(1));

                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取任务失败：网络错误");
                    }
                    resultData = new Gson().fromJson(jsonResult, new TypeToken<ResultTableData<ScheduleTask>>() {
                    }.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                    resultData = new ResultTableData<>();
                    resultData.ResultCode = -100;
                    resultData.ResultMessage = e.getMessage();
                }

                return resultData;
            }

            @Override
            protected void onSuccess(ResultTableData<ScheduleTask> result) {
                if (result.ResultCode != 200) {
                    Toast.makeText(context, result.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (result.DataList.size() == 0) {
                    Toast.makeText(context, "未查询到相应设备的任务信息", Toast.LENGTH_SHORT).show();
                    return;
                }

                Context tContext = resolverParams.getContext();
                Fragment tFragment = resolverParams.getHostFragment();
                if (tContext == null || tFragment == null) {
                    return;
                }

                ScheduleTask scheduleTask = result.getSingleData();
                Intent intent = new Intent(tContext, TaskDetailActivity.class);
                intent.putExtra("ListItemEntity", scheduleTask);
                intent.putExtra("ComeFrom", TaskDetailActivity.Source.FromList);
                tFragment.startActivityForResult(intent, TASK_DETAIL_REQUEST_CODE);
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    public static class QRCodeResolverParams {

        private final WeakReference<Fragment> hostFragment;
        private final WeakReference<Context> context;

        private final String bizName;
        private final int userId;
        private final String taskType;

        public QRCodeResolverParams(Fragment hostFragment, String bizName, int userId, String taskType) {
            this.hostFragment = new WeakReference<>(hostFragment);
            this.context = new WeakReference<>(hostFragment.getContext());
            this.bizName = bizName;
            this.userId = userId;
            this.taskType = taskType;
        }

        public Fragment getHostFragment() {
            return hostFragment.get();
        }

        public Context getContext() {
            return context.get();
        }

        public String getBizName() {
            return bizName;
        }

        public int getUserId() {
            return userId;
        }

        public String getTaskType() {
            return taskType;
        }
    }

}
