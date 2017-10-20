package com.repair.shaoxin.water.repairtask;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.ArrayList;

public class RepairTaskDetailFragment extends Fragment {

    private RepairTaskEntity taskEntity;

    public RepairTaskEntity getTaskEntity() {
        return taskEntity;
    }

    public static Fragment newInstance(String eventSource, int taskID, int orderID) {

        RepairTaskDetailFragment fragment = new RepairTaskDetailFragment();

        Bundle args = new Bundle();
        args.putString("Type", eventSource);
        args.putInt("TaskID", taskID);
        args.putInt("OrderID", orderID);

        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.repair_task_detail_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getTaskDetailInfo();
    }

    private void getTaskDetailInfo() {

        Bundle args = getArguments();
        final String type = args.getString("Type");
        final int taskID = args.getInt("TaskID");
        final int orderID = args.getInt("OrderID");
        final String position = GpsReceiver.getInstance().getLastLocalLocation().toXY();

        MmtBaseTask<String, Void, ResultData<RepairTaskEntity>> mmtBaseTask = new MmtBaseTask<String, Void, ResultData<RepairTaskEntity>>(getActivity()) {
            @Override
            protected ResultData<RepairTaskEntity> doInBackground(String... params) {

                ResultData<RepairTaskEntity> resultData = new ResultData<>();

                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                            .append("/Services/Zondy_MapGISCitySvr_Mobile/REST/MobileREST.svc/MobileService/GetRepairTaskDetail")
                            .append("?userId=").append(MyApplication.getInstance().getUserId())
                            .append("&Type=").append(type)
                            .append("&TaskID=").append(taskID)
                            .append("&orderId=").append(orderID)
                            .append("&position=").append(position);

                    String rawJsonResult = NetUtil.executeHttpGet(sb.toString());

                    if (TextUtils.isEmpty(rawJsonResult)) {
                        throw new Exception("获取任务列表失败：网络错误");
                    }

                    ArrayList<RepairTaskEntity> taskList;

                    if (rawJsonResult.startsWith("\"") && rawJsonResult.endsWith("\"") && rawJsonResult.length() > 1
                            && (rawJsonResult.charAt(1) == '[' || rawJsonResult.charAt(1) == '{')) {
                        rawJsonResult = rawJsonResult.substring(1, rawJsonResult.length() - 1);
                    }
                    rawJsonResult = rawJsonResult.replace("\\", "");

                    taskList = new Gson().fromJson(rawJsonResult, new TypeToken<ArrayList<RepairTaskEntity>>() {
                    }.getType());

                    resultData.ResultCode = 200;
                    resultData.DataList.addAll(taskList);

                } catch (Exception e) {
                    e.printStackTrace();
                    resultData.ResultCode = -100;
                    resultData.ResultMessage = e.getMessage();
                }

                return resultData;
            }

            @Override
            protected void onSuccess(ResultData<RepairTaskEntity> resultData) {

                if (resultData.ResultCode != 200) {
                    Toast.makeText(getActivity(), resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Empty
                if (resultData.DataList.size() == 0) {
                    Toast.makeText(getActivity(), "获取抢修工单信息失败", Toast.LENGTH_SHORT).show();
                } else {

                    taskEntity = resultData.DataList.get(0);
                    showData();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void showData() {

        Activity activity = getActivity();

        ((TextView) activity.findViewById(R.id.repairDetailNum)).setText(taskEntity.no);
        ((TextView) activity.findViewById(R.id.repairDetailRecieveTime)).setText(taskEntity.receiveTime);
        ((TextView) activity.findViewById(R.id.repairDetailReadTime)).setText(taskEntity.readTimeLimit + "分钟");
        ((TextView) activity.findViewById(R.id.repairDetailDoTime)).setText(taskEntity.handleTimeLimit + "小时");
        ((TextView) activity.findViewById(R.id.repairDetailCompany)).setText(taskEntity.belongCompany);
        ((TextView) activity.findViewById(R.id.repairDetailInfoFrom)).setText(taskEntity.infoSource);
        ((TextView) activity.findViewById(R.id.repairDetailDepartment)).setText(taskEntity.dutyDept);
        ((TextView) activity.findViewById(R.id.repairDetailUserName)).setText(taskEntity.userName);
        ((TextView) activity.findViewById(R.id.repairDetailUserTel)).setText(taskEntity.userTel);
        ((TextView) activity.findViewById(R.id.repairDetailUserAddr)).setText(taskEntity.address);
        ((TextView) activity.findViewById(R.id.repairDetailType)).setText(taskEntity.GONG == 1 ? "公" : "用");
        ((TextView) activity.findViewById(R.id.repairDetailRadius)).setText(
                taskEntity.leakPointDiameter == 0 ? "" : String.valueOf(taskEntity.leakPointDiameter));
        ((TextView) activity.findViewById(R.id.repairDetailReciever)).setText(taskEntity.receivePerson);
        ((TextView) activity.findViewById(R.id.repairDetailSutiation)).setText(taskEntity.damagedInfo);
        ((TextView) activity.findViewById(R.id.repairDetailContenxt)).setText(taskEntity.notes);
    }
}
