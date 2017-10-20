package com.repair.zhoushan.module.devicecare.careoverview.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.entity.FlowTableInfo;
import com.repair.zhoushan.entity.TableMetaData;
import com.repair.zhoushan.module.BaseDetailFragment;
import com.repair.zhoushan.module.FlowBeanFragment;

/**
 * 任务详情
 */
public class FeedbackInfoFragment extends BaseDetailFragment<FlowTableInfo> {

    private String bizName = "";
    private String taskCode = "";
    private String bizTaskTable = "";
    private String bizFeedBackTable = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            bizName = args.getString("BizName");
            taskCode = args.getString("TaskCode");
            bizTaskTable = args.getString("BizTaskTable");
            bizFeedBackTable = args.getString("BizFeedBackTable");
        }
    }

    @Override
    protected void fillContentView(ResultData<FlowTableInfo> resultData) {

        FlowTableInfo flowTableInfo = resultData.getSingleData();
        FlowNodeMeta flowNodeMeta = null;
        for (TableMetaData tableMetaData : flowTableInfo.TableMetaDatas) {
            if (flowNodeMeta == null) {
                flowNodeMeta = tableMetaData.FlowNodeMeta;
            } else {
                flowNodeMeta.Groups.addAll(tableMetaData.FlowNodeMeta.Groups);
                flowNodeMeta.Values.addAll(tableMetaData.FlowNodeMeta.Values);
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
                .append(getUserId()).append("/FeedBackInfo")
                .append("?bizName=").append(bizName)
                .append("&taskCode=").append(taskCode)
                .append("&bizTaskTable=").append(bizTaskTable)
                .append("&bizFeedBackTable=").append(bizFeedBackTable);

        return sb.toString();
    }
}
