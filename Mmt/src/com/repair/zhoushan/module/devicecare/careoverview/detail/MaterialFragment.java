package com.repair.zhoushan.module.devicecare.careoverview.detail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.repair.zhoushan.module.BaseDetailFragment;
import com.repair.zhoushan.module.devicecare.consumables.MaterialListAdapter;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoBean;

/**
 * 物料清单
 */
public class MaterialFragment extends BaseDetailFragment<WuLiaoBean> {

    private String caseNo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            caseNo = args.getString("CaseNo");
        }
    }

    @Override
    protected void fillContentView(ResultData<WuLiaoBean> resultData) {

        ListView listView = new ListView(getActivity());
        listView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        MaterialListAdapter listAdapter = new MaterialListAdapter(getActivity(), resultData.DataList, true);
        listView.setDividerHeight(DimenTool.dip2px(getActivity(), 8));
        listView.setAdapter(listAdapter);

        addContentView(listView);
    }

    @Override
    protected ResultData<WuLiaoBean> parseResultData(@NonNull String jsonResult) throws Exception {

        ResultData<WuLiaoBean> resultData = new Gson().fromJson(jsonResult, new TypeToken<ResultData<WuLiaoBean>>() {
        }.getType());

        // 特殊处理：当查询到记录为空的时候，状态码修改为失败情况下的状态码，以用现有的界面结构显示提示信息
        if (resultData.DataList == null || resultData.DataList.size() == 0) {
            resultData.ResultCode = -100;
            resultData.ResultMessage = "物料清单为空";
        }
        return resultData;
    }

    @Override
    protected String getRequestUrl() {

        return ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/FetchUsedWuliaoList"
                + "?caseNo=" + caseNo;
    }
}
