package com.repair.zhoushan.module.devicecare.careoverview.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.BaseDetailFragment;
import com.repair.zhoushan.module.FlowBeanFragment;

/**
 * 设备详情
 */
public class StationTaskTableInfoFragment extends BaseDetailFragment<FlowNodeMeta> {

    private String bizName = "";
    private String taskCode = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            bizName = args.getString("BizName");
            taskCode = args.getString("TaskCode");
        }
    }

    @Override
    protected void fillContentView(ResultData<FlowNodeMeta> resultData) {

        FlowNodeMeta flowNodeMeta = null;
        for (FlowNodeMeta item : resultData.DataList) {
            if (flowNodeMeta == null) {
                flowNodeMeta = item;
            } else {
                flowNodeMeta.Groups.addAll(item.Groups);
                flowNodeMeta.Values.addAll(item.Values);
            }
        }

        Bundle args = null;
        if (flowNodeMeta != null) {
            args = new Bundle();
            args.putParcelable("GDFormBean", flowNodeMeta.mapToGDFormBean());
        }

        addContentFragment(Fragment.instantiate(getActivity(), FlowBeanFragment.class.getName(), args));
    }

    @Override
    protected String getRequestUrl() {

        StringBuilder sb = new StringBuilder();
        sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                .append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/")
                .append(getUserId()).append("/StationTaskTableInfo")
                .append("?bizName=").append(bizName)
                .append("&taskCode=").append(taskCode);

        return sb.toString();
    }
}
