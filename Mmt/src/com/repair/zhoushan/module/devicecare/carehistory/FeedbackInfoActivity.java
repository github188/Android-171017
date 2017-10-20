package com.repair.zhoushan.module.devicecare.carehistory;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.entity.FlowTableInfo;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.devicecare.ScheduleTask;

public class FeedbackInfoActivity extends BaseActivity {

    private ScheduleTask mScheduleTask;

    private FlowTableInfo mFlowTableInfo;
    private FlowBeanFragment mFlowBeanFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScheduleTask = getIntent().getParcelableExtra("ListItemEntity");
        if (mScheduleTask == null) {
            this.showErrorMsg("未获取到参数");
            return;
        }
        getBaseTextView().setText("反馈详情");

        initView();
    }

    private void initView() {

        MmtBaseTask<String, Void, String> mmtBaseTask =
                new MmtBaseTask<String, Void, String>(FeedbackInfoActivity.this, true, new MmtBaseTask.OnWxyhTaskListener<String>() {

                    @Override
                    public void doAfter(String resultStr) {

                        ResultData<FlowTableInfo> newData = Utils.json2ResultDataActivity(FlowTableInfo.class, FeedbackInfoActivity.this, resultStr, "获取反馈详情失败", false);
                        if (newData == null) return;

                        mFlowTableInfo = newData.getSingleData();
                        createView();
                    }
                }) {
                    @Override
                    protected String doInBackground(String... params) {

                        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                        sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                                .append(userID).append("/FeedBackInfo")
                                .append("?bizTaskTable=").append(mScheduleTask.BizTaskTable)
                                .append("&bizFeedBackTable=").append(mScheduleTask.BizFeedBackTable)
                                .append("&taskCode=").append(mScheduleTask.TaskCode);

                        return NetUtil.executeHttpGet(sb.toString());
                    }
                };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void createView() {

        FlowNodeMeta flowNodeMeta = null;

        try {
            flowNodeMeta = mFlowTableInfo.TableMetaDatas.get(0).FlowNodeMeta;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (flowNodeMeta == null) {
            return;
        }

        mFlowBeanFragment = new FlowBeanFragment();

        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", flowNodeMeta.mapToGDFormBean());
        mFlowBeanFragment.setArguments(args);

        mFlowBeanFragment.setCls(FeedbackInfoActivity.class);
        mFlowBeanFragment.setAddEnable(false);
        mFlowBeanFragment.setFormOnlyShow();

        addFragment(mFlowBeanFragment);

    }
}
