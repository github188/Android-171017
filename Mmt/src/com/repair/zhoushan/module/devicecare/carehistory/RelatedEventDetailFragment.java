package com.repair.zhoushan.module.devicecare.carehistory;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.repair.zhoushan.entity.DealFlowInfo;
import com.repair.zhoushan.entity.EventItem;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.BaseDetailFragment;
import com.repair.zhoushan.module.FlowBeanFragment;

public class RelatedEventDetailFragment extends BaseDetailFragment<DealFlowInfo> {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventItem eventItem = getArguments().getParcelable("ListItemEntity");
        setPostJsonContent(new Gson().toJson(eventItem));
    }

    @Override
    protected void fillContentView(ResultData<DealFlowInfo> resultData) {

        FlowNodeMeta flowNodeMeta = resultData.getSingleData().EventInfo;

        FlowBeanFragment fragment = new FlowBeanFragment();
        fragment.setFormOnlyShow();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", flowNodeMeta.mapToGDFormBean());
        fragment.setArguments(args);

        addContentFragment(fragment);

    }

    @Override
    protected String getRequestUrl() {

        return ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetEventMetaData?userID="
                + getUserId();
    }

}
