package com.repair.zhoushan.module.projectmanage.waterplantpatrolhistory;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;

public class WaterPlantPatrolHistoryDetailActivity extends BaseActivity {

    private FeedbackMobileModel mFeedbackMobileModel;

    private FlowBeanFragment mFlowBeanFragment;
    private FlowNodeMeta mFlowNodeMeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mFeedbackMobileModel = getIntent().getParcelableExtra("ListItemEntity");
        if (mFeedbackMobileModel == null) {
            showErrorMsg("获取参数失败");
            return;
        }

        initView();
    }

    private void initView() {

        getBaseTextView().setText("反馈详情");

        MmtBaseTask<String, Void, String> mmtBaseTask = new MmtBaseTask<String, Void, String>(this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {
            @Override
            public void doAfter(String resultJson) {

                ResultData<FlowNodeMeta> newData = Utils.json2ResultDataActivity(FlowNodeMeta.class,
                        WaterPlantPatrolHistoryDetailActivity.this, resultJson, "获取反馈详情失败", false);
                if (newData == null) return;

                mFlowNodeMeta = newData.getSingleData();

                createView();
            }
        }) {
            @Override
            protected String doInBackground(String... params) {

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeviceManage/")
                        .append(userID).append("/StationTaskFeedBackDetail")
                        .append("?bizName=").append("场站养护")
                        .append("&taskCode=").append(mFeedbackMobileModel.TaskCode)
                        .append("&feedBackType=").append(mFeedbackMobileModel.Name);

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
        mFlowBeanFragment.setCls(WaterPlantPatrolHistoryDetailActivity.class);
        mFlowBeanFragment.setFormOnlyShow();

        addFragment(mFlowBeanFragment);
    }
}
