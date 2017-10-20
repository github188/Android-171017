package com.repair.zhoushan.module.devicecare.stationaccount.stationdeviceselect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.customform.entity.StationDeviceEvent;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.CusBottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.entity.FlowTableInfo;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.eventreport.ZSEventReportActivity;

import org.greenrobot.eventbus.EventBus;

public class StationDevicePropertyActivity extends BaseActivity {

    private FlowBeanFragment mFlowBeanFragment;
    private FlowNodeMeta mFlowNodeMeta;

    private String bizName = "场站设备";
    private String deviceName;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.deviceName = getIntent().getStringExtra("DeviceName");
        this.deviceId = getIntent().getStringExtra("DeviceId");

        if (TextUtils.isEmpty(deviceName) || TextUtils.isEmpty(deviceId)) {
            showErrorMsg("获取参数失败");
            return;
        }

        initView();
    }

    private void initView() {

        getBaseTextView().setText("设备详情");

        MmtBaseTask<String, Void, String> mmtBaseTask = new MmtBaseTask<String, Void, String>(this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {
            @Override
            public void doAfter(String resultJson) {

                ResultData<FlowTableInfo> newData = Utils.json2ResultDataActivity(FlowTableInfo.class,
                        StationDevicePropertyActivity.this, resultJson, "获取设备详情失败", false);
                if (newData == null) return;

                mFlowNodeMeta = newData.getSingleData().TableMetaDatas.get(0).FlowNodeMeta;

                if (mFlowNodeMeta != null) {
                    createView();
                    createBottom();
                }
            }
        }) {
            @Override
            protected String doInBackground(String... params) {

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeviceManage/")
                        .append(userID).append("/DeviceProperty")
                        .append("?bizName=").append(bizName)
                        .append("&deviceName=").append(deviceName)
                        .append("&deviceID=").append(deviceId);

                return NetUtil.executeHttpGet(sb.toString());
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void createView() {

        this.mFlowBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", mFlowNodeMeta.mapToGDFormBean());
        mFlowBeanFragment.setArguments(args);
        mFlowBeanFragment.setCls(StationDevicePropertyActivity.class);
        mFlowBeanFragment.setFormOnlyShow();

        addFragment(mFlowBeanFragment);

    }

    private void createBottom() {

        LinearLayout bottomView = getBottomView();
        bottomView.setBackgroundResource(0);
        bottomView.setMinimumHeight(DimenTool.dip2px(StationDevicePropertyActivity.this, 45));

        CusBottomUnitView feedbackUnitView = new CusBottomUnitView(StationDevicePropertyActivity.this);
        feedbackUnitView.setContent("选 取");

        addBottomUnitView(feedbackUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(StationDevicePropertyActivity.this, ZSEventReportActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

                EventBus.getDefault().post(new StationDeviceEvent(bizName, deviceName, deviceId));
            }
        });
    }
}
